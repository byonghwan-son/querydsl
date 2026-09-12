package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


@Slf4j
@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @PersistenceUnit
  EntityManagerFactory emf;

  @PersistenceContext
  EntityManager em;

  private JPAQueryFactory queryFactory;

  @BeforeEach
  void setUp() {
    queryFactory = new JPAQueryFactory(em);

    Team teamA = Team.createTeam("teamA");
    Team teamB = Team.createTeam("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = Member.createMember("member1", 10, teamA);
    Member member2 = Member.createMember("member2", 20, teamA);
    Member member3 = Member.createMember("member3", 30, teamB);
    Member member4 = Member.createMember("member4", 40, teamB);
    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    // 초기화
    em.flush();
    em.clear();
  }

  @Test
  void startJPQL() {
    // member1을 찾아라
    Member findMember =
        em.createQuery("select m from Member m" +
                " where m.username = :username", Member.class)
            .setParameter("username", "member1")
            .getSingleResult();
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void startQuerydsl() {
    Member findMember = queryFactory
        .select(member)
        .from(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    assert findMember != null;

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void search() {
    Member findMember = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1")
            .and(member.age.eq(10)))
        .fetchOne();

    assert findMember != null;

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void searchAndParam() {
    Member findMember = queryFactory
        .selectFrom(member)
        .where(
            member.username.eq("member1"),
            member.age.eq(10)
        )
        .fetchOne();

    assert findMember != null;

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void resultFetch() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();

    Member fetchOne = queryFactory
        .selectFrom(member)
        .limit(1)
        .fetchOne();

    Member fetchFirst = queryFactory
        .selectFrom(member)
        .fetchFirst();

    // deprecated => fetch 후에 size()로 추출이 가능함.
    QueryResults<Member> results = queryFactory
        .selectFrom(member)
        .fetchResults();

    long total = results.getTotal();
    List<Member> content = results.getResults();

    long fetchCount = queryFactory
        .selectFrom(member)
        .fetchCount();

    long fetchCount1 = queryFactory
        .select(member.team.name, member.age.avg())
        .from(member)
        .groupBy(member.team.name)
        .having(member.age.avg().gt(10))
        .fetchCount();

    System.out.println(fetchCount1);
  }

  @Test
  void sort() {
    em.persist(Member.createMember(null, 100));
    em.persist(Member.createMember("member5", 100));
    em.persist(Member.createMember("member6", 100));
    em.flush();

    List<Member> fetch = queryFactory.selectFrom(member)
        .where(member.age.eq(100))
        .orderBy(member.age.desc(), member.username.asc().nullsLast())
        .fetch();

    assertThat(fetch.get(0).getUsername()).isEqualTo("member5");
    assertThat(fetch.get(1).getUsername()).isEqualTo("member6");
    assertThat(fetch.get(2).getUsername()).isNullOrEmpty();
  }

  @Test
  void paging1() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetch();

    assertThat(fetch.get(0).getUsername()).isEqualTo("member3");
    assertThat(fetch.get(1).getUsername()).isEqualTo("member2");
    assertThat(fetch.size()).isEqualTo(2);
  }

  @Test
  void paging2() {
    QueryResults<Member> results = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetchResults();

    assertThat(results.getTotal()).isEqualTo(4);
    assertThat(results.getLimit()).isEqualTo(2);
    assertThat(results.getOffset()).isEqualTo(1);
    assertThat(results.getResults().size()).isEqualTo(2);
  }

  @Test
  void aggregation() {
    List<Tuple> fetch = queryFactory
        .select(
            member.count(),
            member.age.sumLong(),
            member.age.avg(),
            member.age.max(),
            member.age.min()
        )
        .from(member)
        .fetch();

    Tuple tuple = fetch.getFirst();
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sumLong())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
  }

  /**
   * 팀의 이름과 각 팀의 평균 연령을 구해라.
   *
   */
  @Test
  void group() {
    List<Tuple> fetch = queryFactory
        .select(team.name, member.age.avg())
        .from(member)
        .join(member.team, team)
        .groupBy(team.name)
        .fetch();

    Tuple teamA = fetch.get(0);
    Tuple teamB = fetch.get(1);

    log.info("teamA = {}", teamA.get(team.name));
    log.info("teamB = {}", teamB);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);
  }

  @Test
  void join() {
    List<Member> fetch = queryFactory
        .selectFrom(member)
        .leftJoin(member.team, team)
        .where(team.name.eq("teamA"))
        .fetch();

    assertThat(fetch)
        .extracting("age")
        .containsExactly(10, 20);

    assertThat(fetch)
        .extracting("username")
        .containsExactly("member1", "member2");

    List<Member> fetchLeftJoin = queryFactory
        .selectFrom(member)
        .leftJoin(member.team, team)
        .where(team.name.eq("teamA"))
        .fetch();

    assertThat(fetchLeftJoin)
        .extracting("age")
        .containsExactly(10, 20);

    assertThat(fetchLeftJoin)
        .extracting("username")
        .containsExactly("member1", "member2");
  }

  /**
   * 세타 조인
   * 회원의 이름이 팀 이름과 같은 회원을 조회
   */
  @Test
  void theta_join() {
    em.persist(Member.createMember("teamA"));
    em.persist(Member.createMember("teamB"));
    em.persist(Member.createMember("teamC"));

    List<Member> fetch = queryFactory
        .select(member)
        .from(member, team)
        .where(member.username.eq(team.name))
        .fetch();

    assertThat(fetch)
        .extracting("username")
        .containsExactly("teamA", "teamB");
  }

  /**
   * 조인 대상 필터링 : Join-ON절
   * 일반 조인과 다르게 필터링을 위한 조인을 사용하는 경우
   */
  @Test
  void join_on_filtering() {
    List<Tuple> fetch = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(member.team, team).on(team.name.eq("teamA"))
        //.where(team.name.eq("teamA"))
        .fetch();

    extracted(fetch);
  }

  /**
   * 세타 조인
   * 연관관계가 없는 엔티티 외부 조인
   * 회원의 이름이 팀 이름과 같은 대상 외부 조인
   */
  @Test
  void join_on_no_relation() {
    em.persist(Member.createMember("teamA"));
    em.persist(Member.createMember("teamB"));
    em.persist(Member.createMember("teamC"));

    List<Tuple> fetch = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(team).on(member.username.eq(team.name))
        .fetch();

    extracted(fetch);
  }

  /**
   * 페치 조인 미적용
   */
  @Test
  void fetchJoinNo() {
    Member fetchOne = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1"))
        .fetchOne();

    assert fetchOne != null;

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fetchOne.getTeam());

    assertThat(loaded).as("페치 조인 미적용").isFalse();
  }

  /**
   * 페치 조인 적용
   */
  @Test
  void fetchJoinUse() {
    Member fetchOne = queryFactory
        .selectFrom(member)
        .join(member.team, team).fetchJoin()
        .where(member.username.eq("member1"))
        .fetchOne();

    assert fetchOne != null;

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fetchOne.getTeam());

    assertThat(loaded).as("페치 조인 적용").isTrue();
  }

  /**
   * 서브쿼리
   * 나이가 가장 많은 회원 조회
   */
  @Test
  void subQuery() {
    QMember memberSub = new QMember("memberSub");

    List<Member> fetch = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
            select(memberSub.age.max())
                .from(memberSub)
        ))
        .fetch();

    assertThat(fetch).as("가장 나이가 많은 멤버").extracting("age").containsExactly(40);
    extracted(fetch);
  }

  /**
   * 서브쿼리
   * 나이가 평균 이상인 회원 조회
   */
  @Test
  void subQueryGoe() {
    QMember memberSub = new QMember("memberSub");

    List<Member> fetch = queryFactory
        .selectFrom(member)
        .where(member.age.goe(
            select(memberSub.age.avg())
                .from(memberSub)
        ))
        .fetch();

    assertThat(fetch).extracting("age").containsExactly(30, 40);
    extracted(fetch);
  }

  /**
   * 서브쿼리
   * 나이가 10살 이상인 회원 조회
   */
  @Test
  void subQueryIn() {
    QMember memberSub = new QMember("memberSub");

    List<Member> fetch = queryFactory
        .selectFrom(member)
        .where(member.age.in(
            select(memberSub.age)
                .from(memberSub)
                .where(memberSub.age.gt(10))
        ))
        .fetch();

    assertThat(fetch).extracting("age").containsExactly(20, 30, 40);
    extracted(fetch);
  }

  /**
   * 서브쿼리
   * 회원이 속한 팀명을 가져오기
   */
  @Test
  void selectSubQuery() {
    QTeam teamSub = new QTeam("teamSub");

    List<Tuple> fetch = queryFactory
        .select(member.username,
            select(teamSub.name)
                .from(teamSub)
                .where(teamSub.name.eq(member.team.name)))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void basicCase() {
    List<String> fetch = queryFactory
        .select(member.age
            .when(10).then("열살")
            .when(20).then("스무살")
            .otherwise("기타"))
        .from(member)
        .fetch();

    assertThat(fetch).containsExactly("열살", "스무살", "기타", "기타");
    extracted(fetch);
  }

  @Test
  void complexCase() {
    List<String> fetch = queryFactory
        .select(new CaseBuilder()
            .when(member.age.between(0, 20)).then("0~20살")
            .when(member.age.between(21, 30)).then("21~30살")
            .otherwise("기타"))
        .from(member)
        .fetch();

    assertThat(fetch).containsExactly("0~20살", "0~20살", "21~30살", "기타");
    extracted(fetch);
  }

  @Test
  void orderByCase() {
    NumberExpression<Integer> rankPath = new CaseBuilder()
        .when(member.age.between(0, 20)).then(2)
        .when(member.age.between(21, 30)).then(1)
        .otherwise(3);

    List<Tuple> fetch = queryFactory
        .select(member.username, member.age, rankPath)
        .from(member)
        .orderBy(rankPath.desc())
        .fetch();

    extracted(fetch);
  }

  @Test
  void constant() {
    List<Tuple> fetch = queryFactory
        .select(member.username, Expressions.constant("멤버이름"))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void concat() {
    List<String> fetch = queryFactory
        .select(member.username.concat("_").concat(member.age.stringValue()))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  private static <T> void extracted(Iterable<T> fetch) {
    for (T fetch1 : fetch) {
      log.info("fetch = {}", fetch1);
    }
  }
}
