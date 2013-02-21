package com.eleichtenschlag.nascar.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Unindexed;

@Entity
public class Lineup {
  public static final int MAX_POINTS = 600;
  public static final int MAX_DRIVERS = 5;
  @Id private Long id;
  private Key<Team> teamKey;
  private Key<Race> raceKey;
  private List<Key<Driver>> driverKeys;
  private Integer pointsUsed;
  @Unindexed private Date lastUpdated;
  
  public Lineup() {
    this(null);
  }
  
  public Lineup(Key<Race> raceKey) {
    this.raceKey = raceKey;
    this.pointsUsed = 0;
    this.driverKeys = new ArrayList<Key<Driver>>();
    this.lastUpdated = new Date();
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
  
  public List<Driver> getDrivers() {
    return DatastoreManager.getObjectWithKeys(this.driverKeys);
  }
  
  public List<Key<Driver>> getDriverKeys() {
    return this.driverKeys;
  }
  
  public Date getLastUpdated() {
    return this.lastUpdated;
  }
  
  public void setLastUpdatedIfMoreRecent(Date lastUpdated) {
    if (lastUpdated == null) {
      return;
    }

    if (this.lastUpdated == null || this.lastUpdated.compareTo(lastUpdated) < 0) {
      this.lastUpdated = lastUpdated;
    }
  }
  
  public boolean addDriver(Driver driver) {
    boolean driverAdded = false;
    if (this.driverKeys.size() < 5) {
      Key<Driver> driverKey = new Key<Driver>(Driver.class, driver.getId());
      driverAdded = this.driverKeys.add(driverKey);
      if (driverAdded) {
        this.pointsUsed += driver.getValue();
      }
    }
    return driverAdded;
  }
  
  public boolean setDrivers(List<Driver> drivers) {
    List<Key<Driver>> newDriverKeys = new ArrayList<Key<Driver>>();
    Integer newPointsUsed = 0;
    boolean driversAdded = false;
    if (drivers.size() <= 5) {
      for (Driver driver: drivers) {
        Key<Driver> driverKey = new Key<Driver>(Driver.class, driver.getId());
        driversAdded = newDriverKeys.add(driverKey);
        newPointsUsed += driver.getValue();
        if (!driversAdded || newPointsUsed > 600) {
          driversAdded = false;
          break;
        }
      }
    }
    if (driversAdded) {
      this.driverKeys = newDriverKeys;
      this.pointsUsed = newPointsUsed;
    }
    return driversAdded;
  }
  
  public void setDriverKeys(List<Key<Driver>> driverKeys) {
    this.driverKeys = driverKeys;
  }
  
  public boolean removeDriver(Driver driver) {
    Key<Driver> driverKey = new Key<Driver>(Driver.class, driver.getId());
    boolean driverRemoved = this.driverKeys.remove(driverKey);
    if (driverRemoved) {
      this.pointsUsed -= driver.getValue();
    }
    return driverRemoved;
  }
  
  public Integer getPointsUsed() {
    return this.pointsUsed;
  }
  
  public void setPointsUsed(Integer pointsUsed) {
    this.pointsUsed = pointsUsed;
  }
  
  public Integer getPointsRemaining() {
    return MAX_POINTS - this.pointsUsed;
  }
  
  public Integer getTotalScore() {
    List<Driver> drivers = DatastoreManager.getObjectWithKeys(this.driverKeys);
    int score = 0;
    if (drivers != null) {
      for (Driver driver: drivers) {
        score += driver.getScore();
      }
    }
    return score;
  }
}
