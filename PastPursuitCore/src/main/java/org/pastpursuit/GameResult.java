package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "results")
@Getter
@Setter
public class GameResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  @Enumerated(EnumType.ORDINAL)
  private MatchResult matchResult;

  public enum MatchResult {
    WIN, LOSS
  }
}
