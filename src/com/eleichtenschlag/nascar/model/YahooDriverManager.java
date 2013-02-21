package com.eleichtenschlag.nascar.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Implementation of DriverManager to pull data from Yahoo. We can no longer pull data from the
 * Nascar website due to the site design.
 *
 * @author Eric Leichtenschlag
 */
public final class YahooDriverManager {
  private static final String STANDINGS_URL =
    "http://sports.yahoo.com/nascar/sprint/standings"; // no known week-by-week standings data.

  private static final String QUALIFYING_DIV_ID = "nascar-qualify";
  private static final String UNFORMATTED_QUALIFYING_URL =
    "http://sports.yahoo.com/nascar/sprint/races/%d/qualify?year=%d";
  private static final String RESULTS_DIV_ID = "leaderboard";
  private static final String UNFORMATTED_RESULTS_URL =
    "http://sports.yahoo.com/nascar/sprint/races/%d/results?year=%d";
  
  public static List<Driver> getStandings(int year, int raceNum) {
    List<Driver> drivers = new ArrayList<Driver>();
    Document document = null;
    try {
      document = Jsoup.connect(generateStandingsUrl(year, raceNum)).get();
      // Note - this is about to get real hacky.
      Elements tables = document.getElementsByTag("table");
      Element standingsTable = tables.get(6);
      System.out.println(standingsTable.html());
      Elements rows = standingsTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      if (rowsIterator.hasNext()) {
        // consume header row.
        rowsIterator.next();
      }
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        Driver driver = new Driver();
        // Rank is first item in the table.
        String rankString = rowElements.get(0).html();
        Integer rank = Integer.parseInt(rankString.replace("&nbsp;", "").trim());
        driver.setRank(rank);
        // Name is third item in the table.
        Elements driverNames = rowElements.get(2).getElementsByTag("a");
        if (driverNames.size() > 0) {
          String driverName = driverNames.get(0).html();
          driver.setName(driverName);
        }
        drivers.add(driver);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return drivers;
  }

  public static List<Driver> getResults(int year, int raceNum) {
    List<Driver> driversList = new ArrayList<Driver>();
    Document document = null;
    try {
      document = Jsoup.connect(generateResultsUrl(year, raceNum)).get();
      Element results = document.getElementById(RESULTS_DIV_ID);
      Element resultsTable = results.getElementsByTag("table").get(0);
      Elements rows = resultsTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      if (rowsIterator.hasNext()) {
        // consume header row.
        rowsIterator.next();
      }
      System.out.println(resultsTable.html());
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements tdElements = row.getElementsByTag("td");
        Driver driver = new Driver();
        // Name is first td in the table.
        Elements driverNames = tdElements.get(0).getElementsByTag("a");
        String driverName = driverNames.get(0).html();
        driver.setName(driverName);
        // Points are third td in the table.
        String pointsString = tdElements.get(2).html();
        // Only care about total points, not bonus count. Ex: "48/5" should be just "48".
        int slashIndex = pointsString.indexOf('/');
        if (slashIndex >= 0) {
          pointsString = pointsString.substring(0, slashIndex);
        }
        driver.setScore(Integer.parseInt(pointsString));
        driversList.add(driver);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return driversList;
  }
  
  public static List<Driver> getEntries(int year, int raceNum) {
    List<Driver> drivers = new ArrayList<Driver>();
    Document document = null;
    try {
      document = Jsoup.connect(generateQualifyingUrl(year, raceNum)).get();
      Element results = document.getElementById(QUALIFYING_DIV_ID);
      Element resultsTable = results.getElementsByTag("table").get(0);
      Elements rows = resultsTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      if (rowsIterator.hasNext()) {
        // consume header row.
        rowsIterator.next();
      }
      //System.out.println(resultsTable.html());
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        Driver driver = new Driver();
        // Name is first tr item in the row.
        Elements driverNames = rowElements.get(0).getElementsByTag("a");
        String driverName = driverNames.get(0).html();
        driver.setName(driverName);
        drivers.add(driver);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return drivers;
  }

//  private static Integer calculatePoints(String pointsString, Integer finishPosition) {
//    Integer points = 0;
//    if (pointsString.equals("0")) {
//      points = 44 - finishPosition;
//      if (finishPosition == 1) { // Non-scoring racer was winner.
//        points += 4; // 3 for winning, 1 for leading a lap.
//      }
//    } else if (pointsString.contains("/")) {
//      points = Integer.parseInt(
//          pointsString.substring(0, pointsString.indexOf("/")));
//    }
//    return points;
//  }

  private static String generateStandingsUrl(int year, int raceNum) {
    return STANDINGS_URL;
  }

  private static String generateResultsUrl(int year, int raceNum) {
    return String.format(UNFORMATTED_RESULTS_URL, raceNum, year);
  }

  private static String generateQualifyingUrl(int year, int raceNum) {
    return String.format(UNFORMATTED_QUALIFYING_URL, raceNum, year);
  }

  public static String generateDriverSelectionFormHtml(List<Driver> drivers) {
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
    
    String formHtml = "<form action='/driverselection' method='POST'>";
    formHtml += "<table border='1' style='font-size: 18px;'><tbody>";
    formHtml += "<tr><th>Check</th><th>Driver</th><th>Value</th><th></th>";
    formHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
    formHtml += "<th>Check</th><th>Driver</th><th>Value</th><th></th>";
    formHtml += "<th>Check</th><th>Driver</th><th>Value</th></tr>";
    formHtml += tableBody;
    formHtml += "</tbody></table>";
    formHtml += "<button id='submit' type='submit'>Enter Picks</button></form>";
    return formHtml;
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
}
