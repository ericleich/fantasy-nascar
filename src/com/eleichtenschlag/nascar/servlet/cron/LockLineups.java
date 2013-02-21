package com.eleichtenschlag.nascar.servlet.cron;

import java.io.IOException;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Race;

@SuppressWarnings("serial")
public class LockLineups extends HttpServlet {
  private static String LAST_OPERATION_MESSAGE = "None";
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    resp.getWriter().println("<a href='/'>Back to home</a><br/>");
    resp.getWriter().println("This is the admin page for making system updates.");
    resp.getWriter().println("Please expect the system to take about 15 seconds to perform these actions.");
    
    NascarConfig config = NascarConfigSingleton.get();
    Race race = config.getRace();
    resp.getWriter().println(String.format(
        "<h3>Current race: %d - %d (%s)</h3>", race.getYear(),
            race.getWeek(), race.getRaceName()));
    resp.getWriter().println("<form method='POST'>");
    resp.getWriter().println("<p>Click the following magic button after the race results are in to prepare the system for the new week.</p>");
    resp.getWriter().println("<input type='hidden' name='action' value='nextrace'/>");
    resp.getWriter().println("<button type='submit'>Calculate Results and go to next Race</button></form>");
    
    resp.getWriter().println("<form method='POST'>");
    String lineupString = config.getCanEditLineup() ? "Lineups are unlocked" : "Lineups are locked";
    resp.getWriter().println(lineupString);
    resp.getWriter().println("<input type='hidden' name='action' value='toggleeditable'/>");
    String lineupText = config.getCanEditLineup() ? "Lock lineups" : "Unlock lineups";
    resp.getWriter().println("<button type='submit'>" + lineupText + "</button></form>");
    
    
    resp.getWriter().println("<BR/>Last operation: " + LAST_OPERATION_MESSAGE);
  }
  
  private void nextRace() {
    // Pull results for old week, then set new week.
    NascarConfig config = NascarConfigSingleton.get();
    Race race = config.getRace();
    DatastoreManager.populateResults(race);
    config.goToNextWeek();
    Race newRace = config.getRace();
    
    // Pull drivers for new week.
    DatastoreManager.populateDriverData(newRace.getYear(), newRace.getWeek());
    LAST_OPERATION_MESSAGE = "Went to next race";
  }
}
