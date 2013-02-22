package com.eleichtenschlag.nascar.model;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class Result {
  @Id private Long id;
  private Key<Team> teamKey;
  private Key<Race> raceKey;
  private Integer score;
  // This number ranges from 0 to 1 based on number of winners.
  private Double winnerScore;
  
  public Result() {
    this.score = 0;
    this.winnerScore = 0.0;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public Key<Team> getTeamKey() {
    return this.teamKey;
  }
  
  public void setTeamKey(Key<Team> teamKey) {
    this.teamKey = teamKey;
  }
  
  public Key<Race> getRaceKey() {
    return this.raceKey;
  }
  
  public void setRaceKey(Key<Race> raceKey) {
    this.raceKey = raceKey;
  }
  
  public Integer getScore() {
    return this.score;
  }
  
  public void setScore(Integer score) {
    this.score = score;
  }
  
  public Double getWinnerScore() {
    return this.winnerScore;
  }
  
  public void setWinnerScore(Double winnerScore) {
    this.winnerScore = winnerScore;
  }
}
