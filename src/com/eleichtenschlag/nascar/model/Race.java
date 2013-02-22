package com.eleichtenschlag.nascar.model;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class Race {
  @Id private Long id;
  private Integer year;
  private Integer week;
  private String raceName;
  private static final String[] RACES = {
    "Daytona", "Phoenix", "Las Vegas", "Bristol", "Fontana",
    "Martinsville", "Texas", "Kansas", "Richmond", "Talladega",
    "Darlington", "Charlotte", "Dover", "Pocono", "Michigan",
    "Sonoma", "Kentucky", "Daytona", "Loudon", "Indianapolis",
    "Pocono", "Watkins Glen", "Michigan", "Bristol", "Atlanta",
    "Richmond", "Chicago", "Loudon", "Dover", "Kansas",
    "Charlotte", "Talladega", "Martinsville", "Texas", "Phoenix", "Homestead" };
  
  public Race() {
    // Empty.
  }
  
  public Race(Integer year, Integer week) {
    this.year = year;
    this.week = week;
    this.raceName = RACES[week - 1];
  }
  
  public Long getId() {
    return this.id;
  }
  
  public Key<Race> getKey() {
    return new Key<Race>(Race.class, this.id);
  }
  
  public Integer getYear() {
    return this.year;
  }
  
  public Integer getWeek() {
    return this.week;
  }
  
  public String getRaceName() {
    return this.raceName;
  }
}
