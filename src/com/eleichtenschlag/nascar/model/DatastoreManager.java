package com.eleichtenschlag.nascar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;

public final class DatastoreManager {
  //private static PersistenceManager persistenceManager = null;
  
  /* Templates */
  public static <T> T getObjectWithKey(Key<T> key) {
    DAO dao = new DAO();
    return dao.getObjectWithKey(key);
  }
  
  public static <T> List<T> getObjectWithKeys(List<Key<T>> keys) {
    DAO dao = new DAO();
    return dao.getObjectsWithKeys(keys);
  }
  
  public static <T> List<T> getAllObjects(Class<T> type) {
    DAO dao = new DAO();
    return dao.getAllObjects(type);
  }
  
  public static <T> List<T> getAllObjectsWithFilters(Class<T> type, Map<String, Object> filters) {
    DAO dao = new DAO();
    return dao.getAllObjectsWithFilters(type, filters);
  }
  
  public static <T> List<T> getAllObjectsWithOrder(Class<T> type, String order) {
    DAO dao = new DAO();
    return dao.getAllObjectsWithOrder(type, order);
  }
  
  public static <T> List<T> getAllObjectsWithFiltersAndOrder
      (Class<T> type, Map<String, Object> filters, String order) {
    DAO dao = new DAO();
    return dao.getAllObjectsWithFiltersAndOrder(type, filters, order);
  }
  
  public static <T> T persistObject(T object) {
    DAO dao = new DAO();
    return dao.persistObject(object);
  }
  
  public static <T> void persistObjects(List<T> objects) {
    DAO dao = new DAO();
    dao.persistObjects(objects);
  }

  public static <T> void deleteObjectWithKey(Key<T> key) {
    DAO dao = new DAO();
    dao.deleteObjectWithKey(key);
  }
  
  public static <T> void deleteAllObjects(Class<T> type) {
    DAO dao = new DAO();
    dao.deleteAllObjects(type);
  }
  
  public static <T> void deleteAllObjectsWithFilters(Class<T> type, Map<String, Object> filters) {
    DAO dao = new DAO();
    dao.deleteAllObjectsWithFilters(type, filters);
  }
  
  /* Owners */
  public static Owner getCurrentOwner() {
    DAO dao = new DAO();
    Owner owner = dao.getCurrentOwner();
    return owner;
  }
  
  /* Drivers */
  public static List<Driver> getEligibleDrivers(Key<Race> raceKey) {
    DAO dao = new DAO();
    return dao.getEligibleDrivers(raceKey);
  }
  
  public static void populateDriverData(int year, int raceNum) {
    List<Driver> enteringDrivers = DriverManager.getEntries(year, raceNum);
    int standingsYear = 0;
    int standingsRaceNum = 0;
    if (raceNum > 1) {
      standingsYear = year;
      standingsRaceNum = raceNum - 1;
    } else {
      standingsYear = year - 1;
      standingsRaceNum = 36; //TODO: how to remove hardcoding?
    }
    List<Driver> standingsDrivers = DriverManager.getStandings(standingsYear, standingsRaceNum);
    
    // Get all datastore drivers already populated from this race.
    Key<Race> raceKey = getRaceKeyByYearAndWeek(year, raceNum);
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", raceKey);
    List<Driver> datastoreDrivers = getAllObjectsWithFilters(Driver.class, filters);
    //Update properties for each driver entering.
    for (Driver driverEntering : enteringDrivers) {
      Driver standingsDriver = null;
      for (Driver d : standingsDrivers) {
        if (d.hasSameNameAs(driverEntering)) {
          standingsDriver = d;
          break;
        }
      }
      if (standingsDriver == null) {
        standingsDriver = new Driver(driverEntering.getName(), year, raceNum);
      }
      
      Driver datastoreDriver = null;
      for (Driver d : datastoreDrivers) {
        if (d.hasSameNameAs(driverEntering)) {
          datastoreDriver = d;
          break;
        }
      }
      if (datastoreDriver == null) {
        datastoreDriver = new Driver(driverEntering.getName(), year, raceNum);
        datastoreDrivers.add(datastoreDriver);
      }
      // Populate driver from datastore with rank from previous week standings.
      datastoreDriver.setRank(standingsDriver.getRank());
    }
    
    // Update the drivers in the database.
    persistObjects(datastoreDrivers);
  }
  
  /** Updates driver results. */
  public static void populateResults(Race race) {
    int year = race.getYear();
    int raceNum = race.getWeek();
    List<Driver> resultsDrivers = DriverManager.getResults(year, raceNum);
    List<Driver> datastoreDrivers = getDriversByYearAndWeek(year, raceNum);
    for (Driver datastoreDriver : datastoreDrivers) {
      for (Driver resultDriver : resultsDrivers) {
        if (datastoreDriver.hasSameNameAs(resultDriver)) {
          datastoreDriver.setScore(resultDriver.getScore());
          break;
        }
      }
    }
    // Update the drivers in the database.
    persistObjects(datastoreDrivers);
  }

  /** 
   * Calculate fantasy scores.  This operation has been separated
   * from populateResults, because scores may need to be recalculated
   * manually if results are incorrect.
   * @param race The current race.
   */
  public static void calculateScores(Race race) {
    // Update scores for Results.
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", race.getKey());
    List<Result> results = getAllObjectsWithFilters(Result.class, filters);
    List<Team> teams = getAllObjects(Team.class);
    // Used for owners who didn't set a lineup.
    List<Result> emptyResults = new ArrayList<Result>();
    int lowestScore = 0;
    for (Team team: teams) {
      Result result = null;
      // Get the result.  If one doesn't exist, create one.
      for (Result r: results) {
        if (r.getTeamKey().getId() == team.getId()) {
          result = r;
          break;
        }
      }
      if (result == null) {
        result = new Result();
        result.setRaceKey(race.getKey());
        Key<Team> teamKey = new Key<Team>(Team.class, team.getId());
        result.setTeamKey(teamKey);
        results.add(result);
      }
      // Reset winner score.  It will be recalculated.
      result.setWinnerScore(0.0);
      
      Integer totalScore = 0;
      Lineup lineup = team.getLineup(race.getKey());
      if (lineup != null && lineup.getDriverKeys().size() > 0) {
        List<Driver> teamDrivers = lineup.getDrivers();
        if (teamDrivers != null) {
          for (Driver driver: teamDrivers) {
            totalScore += driver.getScore();
          }
        }
        result.setScore(totalScore);
        if (totalScore < lowestScore || lowestScore == 0) {
          lowestScore = totalScore;
        }
      } else {
        emptyResults.add(result);
      }
    }
    // Empty results can't be a winner if lowest score = highest score.
    setResultWinners(results);
    for (Result emptyResult: emptyResults) {
      emptyResult.setScore(lowestScore);
    }
    persistObjects(results);
  }
  
  private static void setResultWinners(List<Result> results) {
    int highScore = 0;
    int frequency = 0;
    // Loop once to calculate high score.
    for (Result result: results) {
      int score = result.getScore();
      if (score > highScore) {
        highScore = score;
        frequency = 1;
      } else if(score == highScore) {
        frequency++;
      }
    }
    // Loop again to set winner scores.
    double winnerScore = highScore > 0 ? 1.0 / ((double) frequency) : 0;
    for (Result result: results) {
      if (result.getScore() == highScore) {
        result.setWinnerScore(winnerScore);
      }
    }
  }
  
  public static List<Driver> getDriversByNamesAndRace(String[] driverStrings, Key<Race> raceKey) {
    DAO dao = new DAO();
    return dao.getDriversByNames(driverStrings, raceKey);
  }
  
  public static List<Driver> getDriversByYearAndWeek(int year, int week) {
    Key<Race> raceKey = getRaceKeyByYearAndWeek(year, week);
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("raceKey", raceKey);
    List<Driver> datastoreDrivers = getAllObjectsWithFilters(Driver.class, filters);
    return datastoreDrivers;
  }
  
  /* Races */
  public static Key<Race> getRaceKeyByYearAndWeek(int year, int week) {
    DAO dao = new DAO();
    return dao.getRaceKeyByYearAndWeek(year, week);
  }
  
  public static void populateRaces(int year) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("year", year);
    List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
    if (races.size() < 1) {
      List<Race> racesToPersist = new ArrayList<Race>();
      for (int i = 1; i <= 36; i++) {
        Race race = new Race(year, i);
        racesToPersist.add(race);
      }
      DatastoreManager.persistObjects(racesToPersist);
    }
  }
}
