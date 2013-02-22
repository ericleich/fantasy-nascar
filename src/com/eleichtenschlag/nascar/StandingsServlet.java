package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Result;
import com.eleichtenschlag.nascar.model.Team;
import com.googlecode.objectify.Key;

@SuppressWarnings("serial")
public class StandingsServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    resp.getWriter().println("<A HREF='/'>Home</A>&nbsp;");
    resp.getWriter().println("<A HREF='/team'>Create/Edit Team</A>&nbsp;");
    resp.getWriter().println("<A HREF='/driverselection'>Select Drivers</A>&nbsp;");
    resp.getWriter().println("<A HREF='/lineup'>View Lineups</A>&nbsp;");
    resp.getWriter().println("<A HREF='/results'>View Results</A>&nbsp;");
    resp.getWriter().println("<A HREF='/standings'>View Standings</A><BR/>");
    resp.getWriter().println("<H2>Standings</H2>");
    
    String firstHalf = "";
    if (req.getParameter("firsthalf") != null) {
      firstHalf = req.getParameter("firsthalf");
    }
    NascarConfig config = NascarConfigSingleton.get();
    boolean showFirstHalfOfSeason = firstHalf.equals("1") ||
                                    config.isFirstHalfOfSeason();
    if (!showFirstHalfOfSeason) {
      resp.getWriter().println("<A HREF='/standings?firsthalf=1'>View Standings from first half of season</A><BR/><BR/>");
    }
    
    // Populate a race mapping.  Only look at races in given half of season.
    Map<String, Object> raceFilters = new HashMap<String, Object>();
    raceFilters.put("year", config.getRace().getYear());
    if (showFirstHalfOfSeason) {
      raceFilters.put("week <=" , NascarConfig.WEEKS_IN_SEASON / 2);
    } else {
      raceFilters.put("week >" , NascarConfig.WEEKS_IN_SEASON / 2);
    }
    List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, raceFilters);
    List<Key<Race>> raceKeys = new ArrayList<Key<Race>>();
    for (Race race: races) {
      raceKeys.add(race.getKey());
    }

    // Populate a team mapping.
    Map<String, Object> resultsFilters = new HashMap<String, Object>();
    resultsFilters.put("raceKey in", raceKeys);
    List<Result> results = DatastoreManager.getAllObjectsWithFilters(Result.class, resultsFilters);
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    Map<Long, Team> teamMap = new HashMap<Long, Team>();
    for (Team team: teams) {
      teamMap.put(team.getId(), team);
    }
    // Populate a Team id to Result List mapping.
    Map<Long, List<Result>> resultsMap = new HashMap<Long, List<Result>>();
    for (Result result: results) {
      Long teamId = result.getTeamKey().getId();
      if (resultsMap.get(teamId) == null) {
        resultsMap.put(teamId, new ArrayList<Result>());
      }
      resultsMap.get(teamId).add(result); //do I need to put again to save the list?  My guess is no.
    }
    
    // Print a standings table.
    String tableHtml = createStandingsTableHtml(
        races, teams, resultsMap, showFirstHalfOfSeason);
    resp.getWriter().println(tableHtml);
  }

  /**
   * Creates the html table on the standings page.
   * @param races The list of race objects for the table.
   * @param teams The list of teams.
   * @param resultsMap A map of team ids to a list of results to populate the
   *                   standings table.
   * @param isFirstHalfOfSeason boolean indicating if the season is still in the
   *                            first half.
   * @return
   */
  private String createStandingsTableHtml(List<Race> races,
                                          List<Team> teams,
                                          Map<Long, List<Result>> resultsMap,
                                          boolean showFirstHalfOfSeason) {
    int firstWeekOfHalfSeason = showFirstHalfOfSeason ? 1: 19;
    int lastWeekOfHalfSeason = showFirstHalfOfSeason ? 18: 36;
    
    Map<Long, Race> raceMap = new HashMap<Long, Race>();
    for (Race race: races) {
      raceMap.put(race.getId(), race);
    }
    
    String html = "<table border='1'><tbody><tr><th>Team Name</th>";
    for (int i = firstWeekOfHalfSeason; i <= lastWeekOfHalfSeason; i++) {
      html += String.format("<th>%d</th>", i);
    }
    html += "<th>Weeks Won</th><th>Total</th></tr>";
    
    for (Team team: teams) {
      Map<Integer, Result> resultMap = new HashMap<Integer, Result>();
      List<Result> results = resultsMap.get(team.getId());
      if (results != null) {
        // Populates a scores map.
        for (Result result: results) {
          Race race = raceMap.get(result.getRaceKey().getId());
          // Map a score for that week.
          resultMap.put(race.getWeek(), result);
        }
      }
      html += String.format("<tr><th>%s</th>", team.getTeamName());
      int total = 0;
      double weeksWon = 0;
      for (int week = firstWeekOfHalfSeason; week <= lastWeekOfHalfSeason; week++) {
        Integer score = 0;
        Result result = resultMap.get(week);
        if (result != null) {
          score = result.getScore();
          weeksWon += result.getWinnerScore();
        } 
        html += String.format("<th>%d</th>", score);
        total += score;
      }
      html += String.format("<th>%.1f</th><th>%d</th></tr>", weeksWon, total);
    }
    html += "</tbody></table>";
    return html;
  }
}