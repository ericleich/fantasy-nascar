<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>

<%@ page import="com.eleichtenschlag.nascar.model.DatastoreManager" %>
<%@ page import="com.eleichtenschlag.nascar.model.Driver" %>
<%@ page import="com.eleichtenschlag.nascar.model.DriverManager" %>
<%@ page import="com.eleichtenschlag.nascar.model.Lineup" %>
<%@ page import="com.eleichtenschlag.nascar.model.NascarConfig" %>
<%@ page import="com.eleichtenschlag.nascar.model.NascarConfigSingleton" %>
<%@ page import="com.eleichtenschlag.nascar.model.Owner" %>
<%@ page import="com.eleichtenschlag.nascar.model.Team" %>
<%@ page import="com.eleichtenschlag.nascar.model.Race" %>

<HTML>
<HEAD>
<SCRIPT type="text/javascript" src="driverselection.js"></SCRIPT>
<SCRIPT type="text/javascript" src="js/json2.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/_ah/channel/jsapi"></SCRIPT>

</HEAD>
<%
    NascarConfig configuration = NascarConfigSingleton.get();
    Race race = configuration.getRace();
    Owner owner = DatastoreManager.getCurrentOwner();
    // If owner is null, do something about it.
    Team team = owner.getTeam();
    if (team == null) {
      team = new Team(owner.getEmail());
      team.setOwnerKey(owner.getKey());
      team = DatastoreManager.persistObject(team);
    }
    Boolean canEditLineup = configuration.getCanEditLineup();
    if (canEditLineup) {
%>
      <BODY ONLOAD="getDrivers()">
<%  } else { %>
      <BODY>
<%  } %>

  <A HREF='/'>Home</A>&nbsp;
  <A HREF='/team'>Create/Edit Team</A>&nbsp;
  <A HREF='/driverselection'>Select Drivers</A>&nbsp;
  <A HREF='/lineup'>View Lineups</A>&nbsp;
  <A HREF='/results'>View Results</A>&nbsp;
  <A HREF='/standings'>View Standings</A>
  <BR/>
  <H3>Hello <%= team.getTeamName() %>, select your drivers for year <%= race.getYear()%>
    week <%= race.getWeek() %> (<%= race.getRaceName() %>)</H3>
  <P>Limits: 5 drivers and 600 points. Exceeding the limit results in the drivers
    not being set.</P>
<%
    String html = "";
    if (team == null) {
      html = "<h2>Please go to the team page and create a team first!!</h2>";
    } else if (!canEditLineup) {
      html = "<h2 style='color:red'>Sorry, lineups are locked in for this week. " +
             "Please contact a system administrator in order to change your lineup.</h2>";
    } else {
      List<Driver> drivers = DatastoreManager.getEligibleDrivers(configuration.getRaceKey());
      html += DriverManager.generateDriverSelectionFormHtml(drivers);
    }
%>
  <DIV STYLE="float: left">
    <%= html %>
  </DIV>
<%  if (canEditLineup) { %>
      <DIV STYLE="float: left; padding-left: 20px; font-size: 24px;">
        Your selections:
        <BR/>
        <DIV ID="currentDrivers"></DIV>
        Current value = <DIV ID="totalValue" style="display: inline">0</DIV>
      </DIV>
<%  } %>
</BODY>
</HTML>