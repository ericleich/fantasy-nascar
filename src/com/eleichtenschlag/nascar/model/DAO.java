package com.eleichtenschlag.nascar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

public class DAO extends DAOBase
{
    /* Register the classes for Objectify. */
    static {
        ObjectifyService.register(Owner.class);
        ObjectifyService.register(Team.class);
        ObjectifyService.register(Driver.class);
        ObjectifyService.register(Lineup.class);
        ObjectifyService.register(Race.class);
        ObjectifyService.register(NascarConfig.class);
        ObjectifyService.register(Result.class);
    }
    
    /* Generic */
    public <T> T getObjectWithKey(Key<T> key) {
      return ofy().get(key);
    }
    
    public <T> List<T> getObjectsWithKeys(List<Key<T>> keys) {
      Map<Key<T>, T> objectsMap = ofy().get(keys);
      List<T> objectsList = new ArrayList<T>(objectsMap.values());
      return objectsList;
    }
    
    public <T> List<T> getAllObjects(Class<T> type) {
      List<T> objects = ofy().query(type).list();
      return objects;
    }
    
    public <T> List<T> getAllObjectsWithFilters(Class<T> type, Map<String, Object> filters) {
      Query<T> query = ofy().query(type);
      for (String fieldName : filters.keySet()) {
        Object fieldValue = filters.get(fieldName);
        query.filter(fieldName, fieldValue);
      }
      List<T> objects = query.list();
      return objects;
    }
    
    public <T> List<T> getAllObjectsWithOrder(Class<T> type, String order) {
      List<T> objects = ofy().query(type).order(order).list();
      return objects;
    }
    
    public <T> List<T> getAllObjectsWithFiltersAndOrder
        (Class<T> type, Map<String, Object> filters, String order) {
      Query<T> query = ofy().query(type);
      for (String fieldName : filters.keySet()) {
        Object fieldValue = filters.get(fieldName);
        query.filter(fieldName, fieldValue);
      }
      return query.order(order).list();
    }
    
    public <T> T persistObject(T object) {
      Key<T> objectKey = ofy().put(object);
      object = ofy().get(objectKey);
      return object;
    }
    
    public <T> void persistObjects(List<T> objects) {
      ofy().put(objects);
      
      //Optional: use this instead to be able to return objects.
      //Map<Key<T>, T> obs = ofy().put(objects);
      //List<T> objectsList = new ArrayList<T>(obs.values());
      //return objectsList;
    }
    
    public <T> void deleteObjectWithKey(Key<T> key) {
      ofy().delete(key);
    }
    
    public <T> void deleteAllObjects(Class<T> type) {
      QueryResultIterable<Key<T>> objects = ofy().query(type).fetchKeys();
      ofy().delete(objects);
    }
    
    public <T> void deleteAllObjectsWithFilters(Class<T> type, Map<String, Object> filters) {
      Query<T> query = ofy().query(type);
      for (String fieldName : filters.keySet()) {
        Object fieldValue = filters.get(fieldName);
        query.filter(fieldName, fieldValue);
      }
      QueryResultIterable<Key<T>> objects = query.fetchKeys();
      ofy().delete(objects);
    }

    /* Owners */
    public Owner getCurrentOwner()
    {
      Owner owner = null;
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();
      if (user != null) {
        owner = ofy().query(Owner.class).filter("email", user.getEmail()).get();
        if (owner == null) {
          owner = new Owner(user.getEmail());
          Key<Owner> ownerKey = ofy().put(owner);
          owner = ofy().find(ownerKey);
        }
      }
      return owner;
    }
    
    public List<Team> getTeamsByOwner(Long ownerId) {
      List<Team> teams = new ArrayList<Team>();
      if (ownerId != null) {
        Key<Owner> ownerKey = new Key<Owner>(Owner.class, ownerId);
        // This gets all teams that belong to a specific owner.
        teams = ofy().query(Team.class).filter("ownerKey", ownerKey).list();
      }
      return teams;
    }
    
    public List<Lineup> getLineupsByTeamAndRace(Long teamId, Key<Race> raceKey) {
      List<Lineup> lineups = new ArrayList<Lineup>();
      if (teamId != null) {
        Key<Team> teamKey = new Key<Team>(Team.class, teamId);
        // This gets all lineups that belong to a specific team.
        Map<String, Object> filters = new HashMap<String, Object>();
        filters.put("raceKey", raceKey);
        filters.put("teamKey", teamKey);
        lineups = this.getAllObjectsWithFilters(Lineup.class, filters);
        //lineups = ofy().query(Lineup.class).filter("teamKey", teamKey).list();
      }
      return lineups;
    }
    
    // Returns whether or not operation was successful.
    public boolean addTeamToOwner(Team team, Long ownerId) {
      Owner owner = getCurrentOwner();
      // Constraint - owner must not have team (temporary).
      List<Team> teams = getTeamsByOwner(owner.getId());
      if (teams.size() > 0) {
        return false; }
      // Constraint - team name can't exist (in future, team name can't exist in league.
      teams = getAllObjects(Team.class);
      for (Team t : teams) {
        if (t.getTeamName().equalsIgnoreCase(team.getTeamName())) {
          return false;
        }
      }
      // Otherwise, add team.
      team.setOwnerKey(new Key<Owner>(Owner.class, owner.getId()));
      ofy().put(team);
      return true;
    }
    
    public void addLineupToTeam(Lineup lineup, Long teamId) {
      List<Lineup> lineups = getLineupsByTeamAndRace(teamId, lineup.getRaceKey());
      Lineup l = null;
      if (lineups.size() > 0) {
        l = lineups.get(0);
      }
      if (l == null) {
        // Add the lineup.
        lineup.setTeamKey(new Key<Team>(Team.class, teamId));
        ofy().put(lineup);
      } else {
        // Modify the existing lineup.
        l.setDriverKeys(lineup.getDriverKeys());
        l.setPointsUsed(lineup.getPointsUsed());
        l.setLastUpdatedIfMoreRecent(lineup.getLastUpdated());
        ofy().put(l);
      }
    }
    
    /* Drivers */
    public List<Driver> getEligibleDrivers(Key<Race> raceKey) {
      return ofy().query(Driver.class)
                  .filter("raceKey", raceKey)
                  .order("rank")
                  .list();
    }
    
    public List<Driver> getDriversByNames(String[] driverStrings, Key<Race> raceKey) {
      return ofy()
          .query(Driver.class)
          .filter("raceKey", raceKey)
          .filter("name in ", driverStrings)
          .list();
    }
    
    /* Races */
    // Gets race object from year and week.
    public Key<Race> getRaceKeyByYearAndWeek(int year, int raceNum) {
      return ofy()
          .query(Race.class)
          .filter("year", year)
          .filter("week", raceNum)
          .listKeys()
          .get(0);
    }
}
