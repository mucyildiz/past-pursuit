package org.pastpursuit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="results")
@Getter
@Setter
public class GameResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long matchId;

  private Long userId;

  @Enumerated(EnumType.ORDINAL)
  private MatchResult matchResult;

  private enum MatchResult {
    WIN,
    LOSS
  }
}
