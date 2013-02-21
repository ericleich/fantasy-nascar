package com.eleichtenschlag.nascar.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class DriverManager {
  private static final String tableBodyId = "cnnDataBody";
  private static final String unformattedStandingsUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/standings_official.html";
  private static final String unformattedEntryUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/entry_list.html";
  private static final String unformattedResultsUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/results_official.html";
  
  public static List<Driver> getStandings(int year, int raceNum) {
    List<Driver> drivers = new ArrayList<Driver>();
    Document document = null;
    try {
      document = Jsoup.connect(generateStandingsUrl(year, raceNum)).get();
      Element leadersTable = document.getElementById(tableBodyId);
      Elements rows = leadersTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        // Do not parse ad rows.
        if (rowElements.size() > 3) {
          Driver driver = new Driver();
          // Rank is first item in the table.  Watch out for stupid wildcard images.
          String rankString = rowElements.get(0).html();
          if (rankString.indexOf("<img") >= 0) {
            rankString = rankString.substring(0, rankString.indexOf("<img"));
          }
          Integer rank = Integer.parseInt(rankString.trim());
          driver.setRank(rank);
          // Name is third item in the table.
          Elements driverNames = rowElements.get(2).getElementsByTag("a");
          if (driverNames.size() > 0) {
            String driverName = driverNames.get(0).html();
            driverName = driverName.replace("*", "").trim();
            driver.setName(driverName);
          }
          drivers.add(driver);
        }
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
      Element resultsTable = document.getElementById(tableBodyId);
      Elements rows = resultsTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      //System.out.println(leadersTable.html());
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        // Do not parse ad rows.
        if (rowElements.size() > 3) {
          Driver driver = new Driver();
          // Finish is first item in the table.
          Integer finishPosition = Integer.parseInt(rowElements.get(0).html());
          // Name is fourth item in the table.
          Elements driverNames = rowElements.get(3).getElementsByTag("a");
          if (driverNames.size() > 0) {
            String driverName = driverNames.get(0).html();
            driverName = driverName.replace("*", "").trim();
            driver.setName(driverName);
          } else { //some drivers don't have links.
            driver.setName(rowElements.get(3).html());
          }
          // Points are seventh item in the table.
          String pointsString = rowElements.get(6).html();
          driver.setScore(calculatePoints(pointsString, finishPosition));
          driversList.add(driver);
        }
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
      document = Jsoup.connect(generateEntryUrl(year, raceNum)).get();
      Element resultsTable = document.getElementById(tableBodyId);
      Elements rows = resultsTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      //System.out.println(leadersTable.html());
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        // Do not parse ad rows.
        if (rowElements.size() > 3) {
          Driver driver = new Driver();
          // Name is second item in the table.
          Elements driverNames = rowElements.get(1).getElementsByTag("a");
          String driverName = "";
          if (driverNames.size() > 0) {
            driverName = driverNames.get(0).html();
          } else { //some drivers don't have links.
            driverName = rowElements.get(1).html();
          }
          // Remove rookie * symbols.
          driverName = driverName.replace("*", "").trim();
          driver.setName(driverName);
          drivers.add(driver);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return drivers;
  }
  
  private static Integer calculatePoints(String pointsString, Integer finishPosition) {
    Integer points = 0;
    if (pointsString.equals("0")) {
      points = 44 - finishPosition;
      if (finishPosition == 1) { // Non-scoring racer was winner.
        points += 4; // 3 for winning, 1 for leading a lap.
      }
    } else if (pointsString.contains("/")) {
      points = Integer.parseInt(
          pointsString.substring(0, pointsString.indexOf("/")));
    }
    return points;
  }
  
  private static String generateStandingsUrl(int year, int raceNum) {
    return String.format(unformattedStandingsUrl, year, raceNum);
  }
  
  private static String generateResultsUrl(int year, int raceNum) {
    return String.format(unformattedResultsUrl, year, raceNum);
  }
  
  private static String generateEntryUrl(int year, int raceNum) {
    return String.format(unformattedEntryUrl, year, raceNum);
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
