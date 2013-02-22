package com.eleichtenschlag.nascar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Result;
import com.eleichtenschlag.nascar.model.Team;
import com.googlecode.objectify.Key;

public final class TableUtils {
  private TableUtils() {
    // Prevent instantiation.
  }
  
  public static String createDriversTableHtml(Race race) {
    List<Driver> drivers = DatastoreManager.getEligibleDrivers(race.getKey());
    
    String tableBody = "";
    int numDrivers = drivers.size();
    int numRows = (int) Math.ceil(numDrivers / 4.0 );
    int index = 0;
    for (; index + 3*numRows < numDrivers; index++) {
      tableBody += "<tr>" + getDriverInputHtml(drivers.get(index)) + "<td></td>"
                          + getDriverInputHtml(drivers.get(index + numRows)) + "<td></td>"
                          + getDriverInputHtml(drivers.get(index + 2*numRows)) + "<td></td>"
                          + getDriverInputHtml(drivers.get(index + 3*numRows))
                          + "</tr>";
    }
    for (; index < numRows; index++) {
      tableBody += "<tr>" + getDriverInputHtml(drivers.get(index)) + "<td></td>"
                          + getDriverInputHtml(drivers.get(index + numRows)) + "<td></td>"
                          + getDriverInputHtml(drivers.get(index + 2*numRows))
                          + "</tr>";
    }
    
    String tableHtml = "<table border='1' style='font-size: 18px;'><tbody>";
    tableHtml += "<tr><th>Check</th><th>Driver</th><th>Value</th><th></th>";
    tableHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
    tableHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
    tableHtml += "<th>Check</th><th>Driver</th><th>Value</th></tr>";
    tableHtml += tableBody;
    tableHtml += "</tbody></table>";
    return tableHtml;
  }
  
  private static String getDriverInputHtml(Driver driver) {
    String name = driver.getName();
    int value = driver.getValue();
    String inputHtml= "<td><input id='" + name + "'type='checkbox' name='driver' value='"
    + name + "' onclick='updateSelection(this, \"" + name + "\", " + value + ")'></td>";
    inputHtml += "<td>" + name + "</td>";
    inputHtml += "<td>" + value + "</td>";
    return inputHtml;
  }
  
  public static String createLineupTableHtml(Race race, boolean canViewLineups) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    
    Owner owner = DatastoreManager.getCurrentOwner();
    String html = "<table border='1' style='font-size: 18px;'><tbody><tr><th>Team Name</th><th>Driver 1</th>" +
                   "<th>Driver 2</th><th>Driver 3</th><th>Driver 4</th><th>Driver 5</th></tr>";
    for (Team team: teams) {
      html += String.format("<tr><td>%s</td>", team.getTeamName());
      Long teamId = team.getId();
      Lineup currentLineup = null;
      for (Lineup lineup: lineups) {
        if (lineup.getTeamKey().getId() == teamId) {
          currentLineup = lineup;
          break;
        }
      }
      if (currentLineup != null) {
        List<Driver> drivers = currentLineup.getDrivers();
        for (Driver driver: drivers) {
          String driverName = driver.getName();
          // If lineups aren't locked in, replace other team lineups with "X" values.
          if (!canViewLineups && team.getOwnerKey().getId() != owner.getId()) {
            driverName = "X";
          }
          html += String.format("<td>%s</td>", driverName);
        }
      }
      html += "</tr>";
    }
    html += "</tbody></table>";
    return html;
  }
  
  public static String createResultsTableHtml(Race race) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    List<Result> results = DatastoreManager.getAllObjectsWithFilters(Result.class, filters);
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    
    String html = "<table border='1' style='font-size: 18px;'><tbody><tr><th>Team Name</th><th>Driver 1</th>" +
    "<th>Driver 2</th><th>Driver 3</th><th>Driver 4</th><th>Driver 5</th><th>Total Score</th></tr>";
    for (Team team: teams) {
      html += String.format("<tr><td>%s</td>", team.getTeamName());
      Long teamId = team.getId();
      Lineup currentLineup = null;
      Result currentResult = null;
      for (Lineup lineup: lineups) {
        if (lineup.getTeamKey().getId() == teamId) {
          currentLineup = lineup;
          break;
        }
      }
      for (Result result: results) {
        if (result.getTeamKey().getId() == teamId) {
          currentResult = result;
          break;
        }
      }
      if (currentLineup != null) {
        List<Driver> drivers = currentLineup.getDrivers();
        for (Driver driver: drivers) {
          html += String.format("<td>%s - %d</td>", driver.getName(), driver.getScore());
        }
        // Create empty columns if player selected < 5 drivers.
        for (int i = drivers.size(); i < 5; i++) {
          html += "<td></td>";
        }
      } else {
        html += "<td></td><td></td><td></td><td></td><td></td>";
      }
      if (currentResult != null) {
        html += String.format("<td>%d</td>", currentResult.getScore());
      }
      html += "</tr>";
    }
    html += "</tbody></table>";
    return html;    
  }
  
  
  /** Other utils placed here for now. */
  
  /**
   * Gets the winning teams.
   * @param race The race.
   */
  public static List<Team> getWinningTeams(Race race) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    filters.put("winnerScore >", 0.0);
    List<Result> results = DatastoreManager.getAllObjectsWithFilters(Result.class, filters);
    if (results == null || results.isEmpty()) {
      return null;
    }
    List<Key<Team>> winningTeamKeys = new ArrayList<Key<Team>>();
    for (Result winningResult: results) {
      winningTeamKeys.add(winningResult.getTeamKey()); 
    }
    return DatastoreManager.getObjectWithKeys(winningTeamKeys);
  }
}
