package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Lineup;
import com.eleichtenschlag.nascar.model.NascarConfig;
import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Team;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class DriverServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String type = "";
    if (req.getParameterValues("type") != null) {
      type = req.getParameter("type");
    }
    NascarConfig configuration = NascarConfigSingleton.get();
    List<Driver> drivers = new ArrayList<Driver>();
    if (type.equals("selected")) {
      Owner owner = DatastoreManager.getCurrentOwner();
      // If owner is null, do something about it.
      Team team = owner.getTeam();
      Lineup lineup = team.getLineup(configuration.getRaceKey());
      if (lineup != null) {
        drivers = lineup.getDrivers();
      }
    } else if (type.equals("all")) {
      drivers = DatastoreManager.getEligibleDrivers(configuration.getRaceKey());
    }
      
    Gson gson = new Gson();
    String driversJson = gson.toJson(drivers);
    resp.setContentType("application/json");
    resp.getWriter().append(driversJson);
  }
}
