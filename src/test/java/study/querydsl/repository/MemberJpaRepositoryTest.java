package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

  @PersistenceContext
  EntityManager em;

  @Autowired
  MemberJpaRepository memberJpaRepository;

  @BeforeEach
  void setUp() {
  }

  @Test
  void basicTest() {
    Team teamA = Team.createTeam("teamA");
    em.persist(teamA);

    Member member = memberJpaRepository.save(Member.createMember("member1", 10, teamA));

    log.info("member = {}", member);
    log.info("member.getTeam() = {}", member.getTeam());

    Member findMember = memberJpaRepository.findById(member.getId()).orElseThrow();
    assertThat(findMember).isEqualTo(member);

    List<Member> all = memberJpaRepository.findAll();
    assertThat(all).containsExactly(member);

    Member member1 = memberJpaRepository.findByUsername("member1");
    assertThat(member1).isEqualTo(member);
  }

  @Test
  void basicQuerydslTest() {
    Team teamA = Team.createTeam("teamA");
    em.persist(teamA);

    Member member = memberJpaRepository.save(Member.createMember("member1", 10, teamA));

    log.info("member = {}", member);
    log.info("member.getTeam() = {}", member.getTeam());

    Member findMember = memberJpaRepository.findById(member.getId()).orElseThrow();
    assertThat(findMember).isEqualTo(member);

    List<Member> all = memberJpaRepository.findAll_Querydsl();
    assertThat(all).containsExactly(member);

    Member member1 = memberJpaRepository.findByUsername_Querydsl("member1");
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

    List<MemberTeamDto> results = memberJpaRepository.searchByBuilder(condition);

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

    List<MemberTeamDto> results = memberJpaRepository.searchByWhere(condition);

    assertThat(results).extracting("username").containsExactly("member4");

    extracted(results);
  }

  private static <T> void extracted(Iterable<T> fetch) {
    for (T fetch1 : fetch) {
      log.info("Extracted Data = {}", fetch1);
    }
  }
}