package com.eleichtenschlag.nascar.model;

import java.util.List;

import com.googlecode.objectify.Key;

public final class NascarConfigSingleton {
  private static NascarConfig config = null;
  private static final int DEFAULT_YEAR = 2011;
  private static final int DEFAULT_WEEK = 1;
  
  public static NascarConfig get() {
    if (config == null) {
      List<NascarConfig> configs = DatastoreManager.getAllObjects(NascarConfig.class);
      if (configs != null && configs.size() > 0) {
        config = configs.get(0);
      } else {
        // Populate races for default year and set config to default value.
        DatastoreManager.populateRaces(DEFAULT_YEAR);
        Key<Race> raceKey = DatastoreManager.getRaceKeyByYearAndWeek(DEFAULT_YEAR, DEFAULT_WEEK);
        config = new NascarConfig(raceKey, true);
      }
    }
    return config;
  }
}
