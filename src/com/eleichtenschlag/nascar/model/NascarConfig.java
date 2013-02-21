package com.eleichtenschlag.nascar.model;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class NascarConfig {
  public static final int WEEKS_IN_SEASON = 36;
  @Id private Long id;
  private Key<Race> raceKey;
  private Boolean canEditLineup;
  
  NascarConfig() {
  }
  
  NascarConfig(Key<Race> raceKey, Boolean canEditLineup) {
    this.raceKey = raceKey;
    this.canEditLineup = canEditLineup;
    this.storeConfig();
  }
  
  public Long getId() {
    return this.id;
  }
  
  public Key<Race> getRaceKey() {
    return this.raceKey;
  }
  
  public void setRaceKey(Key<Race> raceKey) {
    this.raceKey = raceKey;
  }
  
  public Race getRace() {
    return DatastoreManager.getObjectWithKey(this.raceKey);
  }
  
  public Boolean getCanEditLineup() {
    return this.canEditLineup;
  }
  
  public void toggleCanEditLineup() {
    this.canEditLineup = !this.canEditLineup;
    this.storeConfig();
  }
  
  public NascarConfig setRace(int year, int week) {
    // Defensively populate races so that the race exists.
    DatastoreManager.populateRaces(year);
    Key<Race> raceKey = DatastoreManager.getRaceKeyByYearAndWeek(year, week);
    this.raceKey = raceKey;
    return this.storeConfig();
  }
  
  public NascarConfig goToNextWeek() {
    Race race = DatastoreManager.getObjectWithKey(this.raceKey);
    if (race != null) {
      int week = race.getWeek() + 1;
      int year = race.getYear();
      if (week > WEEKS_IN_SEASON) {
        week = 1;
        year += 1;
      }
      this.raceKey = DatastoreManager.getRaceKeyByYearAndWeek(year, week);
    }
    this.canEditLineup = true;
    return this.storeConfig();
  }
  
  /**
   * Determines whether or not the league is in the first half of the season.
   * Assumes there are 36 weeks in a season, and the league plays 18 games in
   * the first half.
   * @return Boolean indicating if it is the first half of the season.
   */
  public boolean isFirstHalfOfSeason() {
    Race race = this.getRace();
    return race.getWeek() <= WEEKS_IN_SEASON/2;
  }
  
  private NascarConfig storeConfig() {
    return DatastoreManager.persistObject(this);
  }
}
