package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Data
@Getter
@Setter
@QueryProjection
@AllArgsConstructor
@NoArgsConstructor
public class MemberTeamDto {
  private Long memberId;
  private String username;
  private int age;
  private Long teamId;
  private String teamName;
}
