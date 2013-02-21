/**
 * This com.eleichtenschlag.nascar.model.DriverManager class.
 */
package com.eleichtenschlag.nascar.model;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A couple quick tests to verify that the YahooDriverManager data import is working.
 *
 * @author Eric Leichtenschlag
 */
public class DriverManagerTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.eleichtenschlag.nascar.model.YahooDriverManager#getStandings()}.
   */
  @Test
  public final void testGetStandings() {
    List<Driver> drivers = YahooDriverManager.getStandings(2011, 18);
    assertFalse("Drivers should be populated", drivers.size() == 0);
    for (Driver driver: drivers) {
      assertFalse("Driver should have a name", driver.getName().equals("Unknown"));
      assertNotNull("Driver should have a rank", driver.getRank());
    }
  }

  /**
   * Test method for {@link com.eleichtenschlag.nascar.model.YahooDriverManager#getResults()}.
   */
  @Test
  public final void testGetResults() {
    List<Driver> driversList = YahooDriverManager.getResults(2011, 18);
    assertEquals("There should be 43 results",
        driversList.size(), 43);
    for (Driver driver : driversList) {
      assertTrue("Score should be positive", driver.getScore() > 0);
    }
  }

  /**
   * Test method for {@link com.eleichtenschlag.nascar.model.YahooDriverManager#getEntries()}.
   */
  @Test
  public final void testGetEntries() {
    List<Driver> drivers = YahooDriverManager.getEntries(2011, 18);
    assertFalse("Drivers should be populated", drivers.size() == 0);
    for (Driver driver: drivers) {
      assertFalse("Driver should have a name", driver.getName().equals("Unknown"));
    }
  }
}
