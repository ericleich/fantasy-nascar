package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Result;
import com.eleichtenschlag.nascar.model.Team;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class ResultsServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Make sure user is logged in.
    Owner owner = DatastoreManager.getCurrentOwner();
    if (owner == null) {
      UserService userService = UserServiceFactory.getUserService();
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
      return;
    }
    
    resp.setContentType("text/html");
    resp.getWriter().println("<A HREF='/'>Home</A>&nbsp;");
    resp.getWriter().println("<A HREF='/team'>Create/Edit Team</A>&nbsp;");
    resp.getWriter().println("<A HREF='/driverselection'>Select Drivers</A>&nbsp;");
    resp.getWriter().println("<A HREF='/lineup'>View Lineups</A>&nbsp;");
    resp.getWriter().println("<A HREF='/results'>View Results</A>&nbsp;");
    resp.getWriter().println("<A HREF='/standings'>View Standings</A><BR/>");
    
    NascarConfig config = NascarConfigSingleton.get();
    Race race = config.getRace();
    resp.getWriter().println("<BR/>Week: ");
    for (int index = 1; index < race.getWeek(); index++) {
      resp.getWriter().println(
          String.format("<a href='/results?week=%d'>%d</a>", index, index));
    }
    resp.getWriter().println("<BR/>");

    int week = race.getWeek() > 1 ? race.getWeek() - 1 : 1;
    // Optional week parameter can override default.
    try {
      int weekParam = Integer.parseInt(req.getParameter("week"));
      if (weekParam > 0 && weekParam < week) {
        week = weekParam;
      }
    } catch (Exception ignored) {}
    
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("year", race.getYear());
    filters.put("week", week);
    List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
    if (races.size() > 0) {
      race = races.get(0);
    }

    resp.getWriter().println(String.format(
        "<h3>Results for %d - %d (%s)</h3>", race.getYear(),
            race.getWeek(), race.getRaceName()));

    // Print a results table.
    filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    List<Result> results = DatastoreManager.getAllObjectsWithFilters(Result.class, filters);
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    String tableHtml = createResultsTableHtml(teams, lineups, results);
    resp.getWriter().println(tableHtml);
  }
  
  private String createResultsTableHtml(List<Team> teams, List<Lineup> lineups, List<Result> results) {
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
          html += String.format("<td>%s - %d</td>",
              driver.getName(), driver.getScore());
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
}