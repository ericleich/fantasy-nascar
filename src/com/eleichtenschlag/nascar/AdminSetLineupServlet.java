package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Team;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;

@SuppressWarnings("serial")
public class AdminSetLineupServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Owner owner = DatastoreManager.getCurrentOwner();
    if (owner == null) {
      UserService userService = UserServiceFactory.getUserService();
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
      return;
    }
    resp.sendRedirect("/adminsetlineup.jsp");
  }
  
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String[] driverStrings = null;
    String teamName = null;
    int raceId = -1;
    if (req.getParameterValues("driver") != null) {
      driverStrings = req.getParameterValues("driver");
    }
    if (req.getParameter("teamname") != null) {
      teamName = req.getParameter("teamname");
    }
    if (req.getParameter("raceid") != null) {
      raceId = Integer.parseInt(req.getParameter("raceid"));
    }
    if (driverStrings != null &&
        driverStrings.length <= 5 &&
        teamName != null &&
        raceId >= 0) {
      Team team = this.getTeam(teamName);
      Key<Race> raceKey = new Key<Race>(Race.class, raceId);
      List<Driver> drivers = DatastoreManager.getDriversByNamesAndRace
          (driverStrings, raceKey);
      Lineup lineup = new Lineup(raceKey);
      boolean driversAdded = lineup.setDrivers(drivers);
      if (driversAdded) {
        team.setLineup(lineup);
        DatastoreManager.persistObject(team);
      }
    }
    resp.sendRedirect(req.getRequestURI());
  }
  
  private Team getTeam(String teamName) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("teamName", teamName);
    List<Team> teams = DatastoreManager.getAllObjectsWithFilters(Team.class, filters);
    Team team = teams.get(0);
    return team;
  }
}
