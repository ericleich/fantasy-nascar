package com.eleichtenschlag.nascar.model;

import java.util.List;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity
public class Owner {
  @Id private Long id;
  private String name;
  private String email;
  
  public Owner() {
    this("unknown", "unknown");
  }
  
  public Owner(String email) {
    //Default owner name is email address.
    this(email, email);
  }
  
  public Owner(String email, String name) {
    this.email = email;
    this.name = name;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public Key<Owner> getKey() {
    return new Key<Owner>(Owner.class, this.id);
  }
  
  public Team getTeam() {
    DAO dao = new DAO();
    List<Team> teams = dao.getTeamsByOwner(this.id);
    Team team = null;
    if (teams.size() > 0) {
      team = teams.get(0);
    }
    return team;
  }
  
  public void addTeam(Team team) {
    DAO dao = new DAO();
    dao.addTeamToOwner(team, this.id);
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getEmail() {
    return this.email;
  }
}
