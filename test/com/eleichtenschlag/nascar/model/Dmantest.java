package com.eleichtenschlag.nascar.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eleichtenschlag.nascar.PMF;
import com.eleichtenschlag.nascar.model.Driver;
import com.eleichtenschlag.nascar.model.Result;

public class Dmantest {
  private static final String tableBodyId = "cnnDataBody";
  private static final String unformattedStandingsUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/standings_official.html";
  private static final String unformattedEntryUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/entry_list.html";
  private static final String unformattedResultsUrl =
    "http://www.nascar.com/races/cup/%d/%d/data/results_official.html";
  public static void main(String args[]) {
    int year = 2011;
    int raceNum = 23;
    //DatastoreManager.populateDriverData(year, raceNum);
    DAO dao = new DAO();
    Team team = new Team("Testing objectify");
    dao.ofy().put(team);
  }
  
  public static void jsoupGetStandings() {
    Document document = null;
    try {
      document = Jsoup.connect(generateStandingsUrl(2011, 17)).get();
      Element leadersTable = document.getElementById(tableBodyId);
      Elements rows = leadersTable.getElementsByTag("tr");
      Iterator<Element> rowsIterator = rows.iterator();
      //System.out.println(leadersTable.html());
      while (rowsIterator.hasNext()) {
        Element row = rowsIterator.next();
        Elements rowElements = row.getElementsByTag("td");
        Iterator<Element> rowElementsIterator = rowElements.iterator();
        while (rowElementsIterator.hasNext()) {
          Element rowElement = rowElementsIterator.next();
          System.out.print(rowElement.html() + " ");
        }
        System.out.println();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static List<Result>jsoupGetResults() {
    List<Result> resultsList = new ArrayList<Result>();
    Document document = null;
    try {
      document = Jsoup.connect(generateResultsUrl(2011, 17)).get();
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
            driver.setName(driverNames.get(0).html());
          } else { //some drivers don't have links.
            driver.setName(rowElements.get(3).html());
          }
          // Points are seventh item in the table.
          String pointsString = rowElements.get(6).html();
          //Results results = new Results(driver);
          //results.setPoints(calculatePoints(pointsString, finishPosition));
          //resultsList.add(results);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return resultsList;
  }
  
  public static List<Driver> jsoupGetEntries() {
    List<Driver> drivers = new ArrayList<Driver>();
    Document document = null;
    try {
      document = Jsoup.connect(generateEntryUrl(2011, 17)).get();
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
          if (driverNames.size() > 0) {
            driver.setName(driverNames.get(0).html());
          } else { //some drivers don't have links.
            driver.setName(rowElements.get(1).html());
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
}
