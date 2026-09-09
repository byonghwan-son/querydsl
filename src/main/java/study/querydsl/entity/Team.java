package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@ToString(exclude = "member")
public class Team extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "team_id")
  private Long id;

  private String name;

  @OneToMany(mappedBy = "team")
  private List<Member> member = new ArrayList<>();

  public static Team createTeam(String name) {
    Team team = new Team();
    team.setName(name);
    return team;
  }
}
