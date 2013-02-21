package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Team;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class LineupServlet extends HttpServlet {
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
    boolean canViewLineups = !config.getCanEditLineup();
    try {
      if (req.getParameter("week") != null) {
        int week = Integer.parseInt(req.getParameter("week"));
        if (week < race.getWeek()) {
          Map<String, Object> filters = new HashMap<String, Object>();
          filters.put("year", race.getYear());
          filters.put("week", Integer.parseInt(req.getParameter("week")));
          List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
          if (races.size() > 0) {
            race = races.get(0);
            canViewLineups = true;
          }
        }
      }
    } catch (Exception ex) {
      race = config.getRace();
    }

    resp.getWriter().println(String.format(
        "<h3>Lineups for %d - %d (%s)</h3>", race.getYear(),
            race.getWeek(), race.getRaceName()));
    
    if (!canViewLineups) {
      resp.getWriter().println("<h4>All lineups will be viewable once they are locked in</h4>");
    }
    // Print a standings table.
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    List<Lineup> lineups = DatastoreManager.getAllObjectsWithFilters(Lineup.class, filters);
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    String tableHtml = createLineupTableHtml(lineups, teams, canViewLineups);
    resp.getWriter().println(tableHtml);
  }
  
  private String createLineupTableHtml(List<Lineup> lineups, List<Team> teams, boolean canViewLineups) {
    Owner owner = DatastoreManager.getCurrentOwner();
    String html = "<table border='1' style='font-size: 18px;'><tbody><tr><th>Team Name</th><th>Driver 1</th>" +
                  "<th>Driver 2</th><th>Driver 3</th><th>Driver 4</th><th>Driver 5</th><th>Last Updated</th></tr>";
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
        // Create empty columns if player selected < 5 drivers.
        for (int i = drivers.size(); i < 5; i++) {
          html += "<td></td>";
        }
        // Add last updated field.
        Date lastUpdated = currentLineup.getLastUpdated();
        if (lastUpdated != null) {
          DateFormat dateFormat =
              DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
          dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
          html += String.format("<td>%s</td>", dateFormat.format(lastUpdated));
        }
      } else {
        html += "<td></td><td></td><td></td><td></td><td></td>";
      }
      html += "</tr>";
    }
    html += "</tbody></table>";
    return html;
  }
}