<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

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
<SCRIPT type="text/javascript" src="adminsetlineup.js"></SCRIPT>
<SCRIPT type="text/javascript" src="js/json2.js"></SCRIPT>
</HEAD>
<BODY ONLOAD="getUpdatedDrivers()">
  <A HREF='/admin/advanced'>Back to Admin Advanced</A>
  <A HREF='/'>Back to home</A><BR/>
  <H1>This interface lets you remotely set a lineup.</H1>
<%
    // Team select option.
    String teamSelectHtml = "<select id='teamselect' name='teamname' onchange='requestUpdatedLineup()'>";
    List<Team> teams = DatastoreManager.getAllObjects(Team.class);
    for (Team team: teams) {
      teamSelectHtml += String.format("<option value='%s'>%s</option>", team.getTeamName(), team.getTeamName());
    }
    teamSelectHtml += "</select>";
    
    // Race select option.
    NascarConfig configuration = NascarConfigSingleton.get();
    Race currentRace = configuration.getRace();
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("year", currentRace.getYear());
    List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
    String raceSelectHtml = "<select id='raceselect' name='raceid' onchange='getUpdatedDrivers()'>";
    for (Race race: races) {
      raceSelectHtml += String.format("<option value='%s'>%s - %s</option>", race.getId(), race.getWeek(), race.getRaceName());
    }
    raceSelectHtml += "</select>";
%>
  <FORM ACTION='admin/lineup' STYLE='float: left;' METHOD='POST'>
    Team: <%= teamSelectHtml %>
    Week: <%= raceSelectHtml %>
    <DIV ID='driverTable'>
    </DIV>
    <BUTTON ID='submit' TYPE='submit'>Enter Picks</BUTTON>
  </FORM>
  <DIV STYLE="float: left; padding-left: 20px; font-size: 24px;">
    Current selections:
    <BR/>
    <DIV ID="currentDrivers"></DIV>
    Current value = <DIV ID="totalValue" style="display: inline">0</DIV>
  </DIV>
  </BODY>
</HTML>