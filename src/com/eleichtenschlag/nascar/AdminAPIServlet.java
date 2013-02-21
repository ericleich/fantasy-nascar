package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.ArrayList;
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
import com.google.gson.Gson;
import com.googlecode.objectify.Key;

@SuppressWarnings("serial")
public class AdminAPIServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Gson gson = new Gson();
    String json = "";
    String type = "";
    if (req.getParameterValues("item") != null) {
      type = req.getParameter("item");
    }
    
    try {
      if (type.equals("lineup") &&
          req.getParameter("teamname") != null &&
          req.getParameter("raceid") != null) {
        List<Driver> drivers = new ArrayList<Driver>();
        String teamName = req.getParameter("teamname");
        Map<String, Object> filters = new HashMap<String, Object>();
        filters.put("teamName", teamName);
        List<Team> teams = DatastoreManager.getAllObjectsWithFilters(Team.class, filters);
        Team team = teams.get(0);
  
        int raceId = Integer.parseInt(req.getParameter("raceid"));
        Key<Race> raceKey = new Key<Race>(Race.class, raceId);
        Lineup lineup = team.getLineup(raceKey);
        if (lineup != null) {
          drivers = lineup.getDrivers();
        }
        json = gson.toJson(drivers);
      } else if (type.equals("drivers") &&
                 req.getParameter("raceid") != null) {
        Map<String, Object> filters = new HashMap<String, Object>();
        int raceId = Integer.parseInt(req.getParameter("raceid"));
        filters.put("id", raceId);
        List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
        Race race = races.get(0);
        List<Driver> drivers = DatastoreManager.getEligibleDrivers(race.getKey());
        json = gson.toJson(drivers);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    resp.setContentType("application/json");
    resp.getWriter().append(json);
  }
}
