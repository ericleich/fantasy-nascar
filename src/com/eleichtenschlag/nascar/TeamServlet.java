package com.eleichtenschlag.nascar;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.*;

import com.eleichtenschlag.nascar.model.DAO;
import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Team;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class TeamServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    
    DAO dao = new DAO();
    Owner owner = dao.getCurrentOwner();
    if (owner == null) {
      UserService userService = UserServiceFactory.getUserService();
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
      return;
    }
    List<Team> teams = dao.getTeamsByOwner(owner.getId());
    if (teams == null || teams.size() == 0) {
      resp.getWriter().println("Hello, " + owner.getEmail() + "! "
          + "Please create a team to get started.");
      resp.getWriter().println("<form method='POST'>");
      resp.getWriter().println("<label>Team Name</label>");
      resp.getWriter().println("<input type='text' name='teamname' required/>");
      resp.getWriter().println("<button type='submit'>Create team</button></form>");
    } else {
      resp.getWriter().println("<A HREF='/'>Home</A>&nbsp;");
      resp.getWriter().println("<A HREF='/team'>Create/Edit Team</A>&nbsp;");
      resp.getWriter().println("<A HREF='/driverselection'>Select Drivers</A>&nbsp;");
      resp.getWriter().println("<A HREF='/lineup'>View Lineups</A>&nbsp;");
      resp.getWriter().println("<A HREF='/results'>View Results</A>&nbsp;");
      resp.getWriter().println("<A HREF='/standings'>View Standings</A><BR/>");
      resp.getWriter().println("Your team is:<br/>");
      for (Team team: teams) {
        resp.getWriter().println(team.getTeamName() + "<br/>");
        resp.getWriter().println("<form method='POST'>");
        resp.getWriter().println("<label>Change Team Name</label>");
        resp.getWriter().println("<input type='text' name='teamname' required/>");
        resp.getWriter().println("<button type='submit'>Change team name</button></form>");
      }
    }
  }
  
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {   
    if (req.getParameter("teamname") != null) {
      Owner owner = DatastoreManager.getCurrentOwner();
      Team team = owner.getTeam();
      String teamName = req.getParameter("teamname");
      if (team == null) {
        team = new Team(teamName);
        owner.addTeam(team);
      } else {
        team.setTeamName(teamName);
        DatastoreManager.persistObject(team);
      }
    }
    //DatastoreManager.persistObject(owner);
    
    // Go back to same webpage.
    resp.sendRedirect("/team");
  }
}
