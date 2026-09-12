package study.querydsl.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface MemberRepositoryCustom {
  List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition);
  List<MemberTeamDto> searchByWhere(MemberSearchCondition condition);
  List<Member> findAll_Querydsl();
  Member findByUsername_Querydsl(String username);

  /**
   * 단순 페이징과 복잡한 페이징
   */
  Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
  Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
  Page<MemberTeamDto> searchPageEnhancedComplex(MemberSearchCondition condition, Pageable pageable);
}
