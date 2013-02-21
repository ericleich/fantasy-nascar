package com.eleichtenschlag.nascar.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class Team {
  @Id private Long id;
  private Key<Owner> ownerKey;
  private String teamName;

  public Team() {
    this("No-name");
  }
  
  public Team(String teamName) {
    this.teamName = teamName;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public Key<Team> getKey() {
    return new Key<Team>(Team.class, this.id);
  }
  
  public Key<Owner> getOwnerKey() {
    return this.ownerKey;
  }
  
  public void setOwnerKey(Key<Owner> ownerKey) {
    this.ownerKey = ownerKey;
  }
  
  public String getTeamName() {
    return this.teamName;
  }
  
  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }
  
  public List<Lineup> getLineups() {
    Key<Team> teamKey = new Key<Team>(Team.class, this.id);
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("teamKey", teamKey);
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    return lineups;
    //DAO dao = new DAO();
    //List<Lineup> lineups = dao.getLineupsByTeam(this.id);
    //Lineup lineup = null;
    //if (lineups.size() > 0) {
    //  lineup = lineups.get(0);
    //}
    //return lineup;
  }
  
  public Lineup getLineup(Key<Race> raceKey) {
    Map<String, Object> filters = new HashMap<String, Object>();
    Key<Team> teamKey = new Key<Team>(Team.class, this.id);
    filters.put("teamKey", teamKey);
    filters.put("raceKey", raceKey);
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    Lineup lineup = null;
    if (lineups.size() > 0) {
      lineup = lineups.get(0);
    }
    return lineup;
  }

  public void setLineup(Lineup lineup) {
    DAO dao = new DAO();
    dao.addLineupToTeam(lineup, this.id);
  }
}
