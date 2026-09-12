package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;
import study.querydsl.entity.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Slf4j
@SpringBootTest
@Transactional
public class QuerydslAdvanceTest {

  @PersistenceUnit
  private EntityManagerFactory emf;

  @PersistenceContext
  private EntityManager em;

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
  void simpleProjection() {
    List<String> fetch = queryFactory
        .select(member.username)
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void tupleProjection() {
    List<Tuple> fetch = queryFactory
        .select(member.username, member.age)
        .from(member)
        .fetch();

    for (Tuple tuple : fetch) {
      String username = tuple.get(member.username);
      Integer age = tuple.get(member.age);
      log.info("username={}, age={}", username, age);
    }

    extracted(fetch);
  }

  @Test
  void findDtoByJPQL() {
    List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
        .getResultList();

    extracted(resultList);
  }

  @Test
  void findDtoBySetter() {
    List<MemberDto> fetch = queryFactory
        .select(Projections.bean(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void findDtoByField() {
    List<MemberDto> fetch = queryFactory
        .select(Projections.fields(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void findDtoByConstructor() {
    List<MemberDto> fetch = queryFactory
        .select(Projections.constructor(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

    extracted(fetch);

    List<UserDto> fetchUser = queryFactory
        .select(Projections.constructor(UserDto.class, member.username, member.age))
        .from(member)
        .fetch();

    extracted(fetchUser);
  }

  @Test
  void findUserDtoByField() {
    List<UserDto> fetch = queryFactory
        .select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void findUserDto() {
    List<Tuple> fetch = queryFactory
        .select(Projections.fields(UserDto.class, member.username.as("name"), member.age),
            ExpressionUtils.as(
                JPAExpressions.select(team.name).from(team).where(team.id.eq(member.team.id)), "team")
        )
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void findDtoQueryProjection() {
    List<MemberDto> fetch = queryFactory
        .select(new QMemberDto(member.username, member.age))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void dynamicQueryBooleanBuilder() {
    String userNameParam = "member1";
    Integer ageParam = 10;

    List<Member> result = searchMember1(userNameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);

    extracted(result);
  }

  private List<Member> searchMember1(String userNameCond, Integer ageCond) {
    BooleanBuilder builder = new BooleanBuilder();
    if(userNameCond != null) {
      builder.and(member.username.eq(userNameCond));
    }
    if(ageCond != null) {
      builder.and(member.age.eq(ageCond));
    }

    return queryFactory
        .selectFrom(member)
        .where(builder)
        .fetch();
  }

  @Test
  void dynamicQuery_WhereParam() {
    String userNameParam = "member1";
    Integer ageParam = 10;

    List<Member> result = searchMember2(userNameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);

    extracted(result);
  }

  private List<Member> searchMember2(String usernameCond, Integer ageCond) {

    return queryFactory
        .selectFrom(member)
        .where(usernameEq(usernameCond), ageEq(ageCond))
        .fetch();
  }

  private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond == null ? null : member.username.eq(usernameCond);
  }

  private BooleanExpression ageEq(Integer ageCond) {
    return ageCond == null ? null : member.age.eq(ageCond);
  }

  @Test
  void bulkUpdate() {
    // 영속성 컨텍스트에 올라가 있는 값과 비교를 할 때 달라지는 것에 주의가 필요함.
    long count = queryFactory
        .update(member)
        .set(member.username, "비회원")
        .where(member.age.lt(28))
        .execute();

    // 영속성 컨텍스트를 초기화 하지 않으면 DB의 값과 영속성 컨텍스트의 값이 달라짐.
    em.flush();
    em.clear();

    List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();

    extracted(fetch);
  }

  @Test
  void sqlFunction() {
    List<String> fetch = queryFactory
        .select(Expressions.stringTemplate(
            "function('replace', {0}, {1}, {2})", member.username, "member", "M"
        ))
        .from(member)
        .fetch();

    extracted(fetch);
  }

  private static <T> void extracted(Iterable<T> fetch) {
    for (T fetch1 : fetch) {
      log.info("Extracted Data = {}", fetch1);
    }
  }
}
