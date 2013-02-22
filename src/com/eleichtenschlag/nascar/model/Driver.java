package com.eleichtenschlag.nascar.model;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class Driver {
  @Id private Long id;
  private String name;
  private Integer rank;
  private Key<Race> raceKey;
  private Integer score;

  private static final int[] values = 
    {190, 180, 175, 165, 160, 155, 150, 145, 140, 135,
     132, 128, 124, 121, 118, 115, 112, 109, 106, 103,
     100, 97, 94, 91, 88, 85, 82, 79, 76, 73,
     70, 67, 64, 61, 58, 55, 52, 49, 46, 43,
     40, 37, 34, 10};
  
  public Driver() {
    this("Unknown");
  }
  
  public Driver(String name) {
    this.name = name.trim();
    this.rank = 1000; // 1000 >> 43 so driver essentially has no ranking.
    this.score = 0;
  }
  
  public Driver(String name, int year, int raceNum) {
    this(name);
    this.raceKey = DatastoreManager.getRaceKeyByYearAndWeek(year, raceNum);
    if (this.raceKey == null) {
      // Create the race and populate the key.
      Race race = new Race(year, raceNum);
      race = DatastoreManager.persistObject(race);
      this.raceKey = new Key<Race>(Race.class, race.getId());
    }
  }
  
  public Long getId() {
    return this.id;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name.trim();
  }
  
  public Integer getRank() {
    return this.rank;
  }
  
  public void setRank(Integer rank) {
    this.rank = rank;
  }
  
  public Integer getScore() {
    return this.score;
  }
  
  public void setScore(Integer score) {
    this.score = score;
  }
  
  public int getValue() {
    // Default value is 10.
    int value = 10;
    if ((this.rank != null) && (this.rank >= 0) && (this.rank < values.length)) {
      value = values[this.rank - 1];
    }
    return value;
  }
  
  public String toString() {
    return this.name + " - " + this.getValue() + "\n";
  }
  
  public boolean hasSameNameAs(Driver otherDriver) {
    return this.name.toLowerCase().trim().equals(
        otherDriver.getName().toLowerCase().trim());
  }
}
