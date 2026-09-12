package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@Slf4j
@SpringBootTest
@Transactional
class MemberRepositoryTest {

  @PersistenceContext
  EntityManager em;

  @Autowired
  private MemberRepository memberRepository;

  @BeforeEach
  void setUp() {

  }

  @Test
  void basicTest() {
    Team teamA = Team.createTeam("teamA");
    em.persist(teamA);

    Member member = memberRepository.save(Member.createMember("member1", 10, teamA));

    log.info("member = {}", member);
    log.info("member.getTeam() = {}", member.getTeam());

    Member findMember = memberRepository.findById(member.getId()).orElseThrow();
    assertThat(findMember).isEqualTo(member);

    List<Member> all = memberRepository.findAll();
    assertThat(all).containsExactly(member);

    List<Member> member1 = memberRepository.findByUsername("member1");
    assertThat(member1).containsExactly(member);
  }


  @Test
  void basicQuerydslTest() {
    Team teamA = Team.createTeam("teamA");
    em.persist(teamA);

    Member member = memberRepository.save(Member.createMember("member1", 10, teamA));

    log.info("member = {}", member);
    log.info("member.getTeam() = {}", member.getTeam());

    Member findMember = memberRepository.findById(member.getId()).orElseThrow();
    assertThat(findMember).isEqualTo(member);

    List<Member> all = memberRepository.findAll_Querydsl();
    assertThat(all).containsExactly(member);

    Member member1 = memberRepository.findByUsername_Querydsl("member1");
    assertThat(member1).isEqualTo(member);
  }

  @Test
  void searchByBooleanBuilderTest() {
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

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    List<MemberTeamDto> results = memberRepository.searchByBuilder(condition);

    assertThat(results).extracting("username").containsExactly("member4");

    extracted(results);
  }

  @Test
  void searchByWhereTest() {
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

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    List<MemberTeamDto> results = memberRepository.searchByWhere(condition);

    assertThat(results).extracting("username").containsExactly("member4");

    extracted(results);
  }

  @Test
  void searchByWherePagingSimpleTest() {
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

    MemberSearchCondition condition = new MemberSearchCondition();

    Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageSimple(condition, PageRequest.of(0, 3));

    assertThat(memberTeamDtos.getSize()).isEqualTo(3);

    List<MemberTeamDto> content = memberTeamDtos.getContent();
    assertThat(content).extracting("username").containsExactly("member1", "member2", "member3");

    extracted(content);
  }

  @Test
  void searchByWherePagingComplexTest() {
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

    MemberSearchCondition condition = new MemberSearchCondition();

    Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageComplex(condition, PageRequest.of(0, 3));

    assertThat(memberTeamDtos.getSize()).isEqualTo(3);

    List<MemberTeamDto> content = memberTeamDtos.getContent();
    assertThat(content).extracting("username").containsExactly("member1", "member2", "member3");

    extracted(content);
  }

  @Test
  void searchByWherePagingEnhancedComplexTest() {
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

    MemberSearchCondition condition = new MemberSearchCondition();

    Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageEnhancedComplex(condition, PageRequest.of(0, 3));

    assertThat(memberTeamDtos.getSize()).isEqualTo(3);

    List<MemberTeamDto> content = memberTeamDtos.getContent();
    assertThat(content).extracting("username").containsExactly("member1", "member2", "member3");

    extracted(content);
  }

  @Test
  void querydslPredicateExecutorTest() {
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

    Iterable<Member> results = memberRepository
        .findAll(member.age.between(20, 40).and(member.username.eq("member4")));

    extracted(results);
  }

  private static <T> void extracted(Iterable<T> fetch) {
    for (T fetch1 : fetch) {
      log.info("Extracted Data = {}", fetch1);
    }
  }

}