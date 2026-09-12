package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

  private final EntityManager em;
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Member> findAll_Querydsl() {
    return em
        .createQuery("select m from Member m", Member.class)
        .getResultList();
  }

  @Override
  public Member findByUsername_Querydsl(String username) {
    return queryFactory
        .selectFrom(member)
        .where(member.username.eq(username))
        .fetchOne();
  }

  @Override
  public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    if (hasText(condition.getUsername())) {
      builder.and(member.username.eq(condition.getUsername()));
    }

    if(hasText(condition.getTeamName())) {
      builder.and(team.name.eq(condition.getTeamName()));
    }

    if(condition.getAgeGoe() != null) {
      builder.and(member.age.goe(condition.getAgeGoe()));
    }

    if(condition.getAgeLoe() != null) {
      builder.and(member.age.loe(condition.getAgeLoe()));
    }

    return queryFactory
        .select(new QMemberTeamDto(member.id.as("memberId")
            , member.username
            , member.age
            , team.id.as("teamId")
            , team.name.as("teamName")))
        .from(member)
        .leftJoin(member.team, team)
        .where(builder)
        .fetch();
  }

  @Override
  public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition) {
    return queryFactory
        .select(new QMemberTeamDto(
            member.id.as("memberId")
            , member.username
            , member.age
            , team.id.as("teamId")
            , team.name.as("teamName")))
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        )
        .fetch();
  }

  private BooleanExpression usernameEq(String username) {
    return !hasText(username) ? null : member.username.eq(username);
  }

  private BooleanExpression teamNameEq(String teamName) {
    return !hasText(teamName) ? null : team.name.eq(teamName);
  }

  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe == null ? null : member.age.loe(ageLoe);
  }

  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe == null ? null : member.age.goe(ageGoe);
  }

  @Override
  public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
    QueryResults<MemberTeamDto> results = queryFactory
        .select(new QMemberTeamDto(
            member.id.as("memberId")
            , member.username
            , member.age
            , team.id.as("teamId")
            , team.name.as("teamName")))
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<MemberTeamDto> content = results.getResults();
    long total = results.getTotal();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> content = queryFactory
        .select(new QMemberTeamDto(
            member.id.as("memberId")
            , member.username
            , member.age
            , team.id.as("teamId")
            , team.name.as("teamName")))
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(member.count())
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }

  @Override
  public Page<MemberTeamDto> searchPageEnhancedComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> content = queryFactory
        .select(new QMemberTeamDto(
            member.id.as("memberId")
            , member.username
            , member.age
            , team.id.as("teamId")
            , team.name.as("teamName")))
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(member.count())
        .from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername())
            , teamNameEq(condition.getTeamName())
            , ageGoe(condition.getAgeGoe())
            , ageLoe(condition.getAgeLoe())
        );

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
  }
}
