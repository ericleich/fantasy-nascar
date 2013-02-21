package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Team;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class DriverSelectionServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Owner owner = DatastoreManager.getCurrentOwner();
    if (owner == null) {
      UserService userService = UserServiceFactory.getUserService();
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
      return;
    }
    resp.sendRedirect("/driverselection.jsp");
  }
  
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String[] driverStrings = null;
    if (req.getParameterValues("driver") != null) {
      driverStrings = req.getParameterValues("driver");
    }
    if (driverStrings != null && driverStrings.length > 5) {
      resp.setStatus(400);
    } else {
      NascarConfig config = NascarConfigSingleton.get();
      List<Driver> drivers = DatastoreManager.getDriversByNamesAndRace
          (driverStrings, config.getRaceKey());
      Owner owner = DatastoreManager.getCurrentOwner();
      Lineup lineup = new Lineup(config.getRaceKey());
      boolean driversAdded = lineup.setDrivers(drivers);
      if (driversAdded) {
        Team team = owner.getTeam();
        team.setLineup(lineup);
        DatastoreManager.persistObject(team);
      }
    }
    resp.sendRedirect(req.getRequestURI());
  }
}
