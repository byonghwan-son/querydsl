package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
@Transactional
class MemberTest {
  @PersistenceContext
  private EntityManager em;

  @BeforeEach
  void setUp() {
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
  void testEntity() {
    List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
    for (Member member : result) {
      log.info("member = {}", member);
      log.info("member.team = {}", member.getTeam());
    }
  }
}