package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "team")
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  private String username;

  private int age;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private Team team;

  public static Member createMember(String username) {
    return createMember(username, 0);
  }

  public static Member createMember(String username, int age) {
    return createMember(username, age, null);
  }

  public static Member createMember(String username, int age, Team team) {
    var member = new Member();
    member.setUsername(username);
    member.setAge(age);
    member.setTeam(team);
    if(team != null) {
      member.changeTeam(team);
    }
    return member;
  }

  public void changeTeam(Team team) {
    this.team = team;
    team.getMember().add(this);
  }

}
