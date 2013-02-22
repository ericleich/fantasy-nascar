package com.eleichtenschlag.nascar.manager;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.eleichtenschlag.nascar.model.DatastoreManager;
import com.eleichtenschlag.nascar.model.Owner;
import com.eleichtenschlag.nascar.model.Race;
import com.eleichtenschlag.nascar.model.Team;
import com.eleichtenschlag.nascar.util.TableUtils;

public final class MailManager {
  private final static String FROM_ADDRESS = "ericleich@gmail.com";
  private final static String FROM_USER = "Eric Leichtenschlag";
  
  public static void sendDriversEmail(Race race) {
    String subject = String.format("NASCAR Fantasy Drivers - Week %s (%s)",
        race.getWeek(),
        race.getRaceName());
    String driverTableHtml = TableUtils.createDriversTableHtml(race);
    
    String messageHtml = String.format("The drivers for week %s (%s) are now " +
        "available.  They are as follows: <br><br> %s. <br><br>Check out the " +
        "<a href='http://fantasy-nascar.appspot.com/driverselection'>driver selection</a> " +
        "page to enter your picks."
        , race.getWeek(), race.getRaceName(), driverTableHtml);
    sendHtmlMessage(subject, messageHtml);
  }

  public static void sendLineupsEmail(Race race) {
    String subject = String.format("NASCAR Fantasy Lineups - Week %s (%s)",
                                   race.getWeek(),
                                   race.getRaceName());
    String lineupTableHtml = TableUtils.createLineupTableHtml(race, true);
    String messageHtml = String.format("The lineups for week %s (%s) have just " +
        "been locked in.  They are as follows: <br><br> %s. <br><br>If there " +
        "are any discrepancies, please email Eric and Jay to get them resolved " +
        "as soon as possible.  Good luck everyone!"
        , race.getWeek(), race.getRaceName(), lineupTableHtml);
    sendHtmlMessage(subject, messageHtml);
  }

  public static void sendPreviousWeekResultsEmail(Race race) {
    String subject = String.format("NASCAR Fantasy Results - Week %s (%s)",
        race.getWeek(),
        race.getRaceName());
    String resultsTableHtml = TableUtils.createResultsTableHtml(race);
    
    /* Get team name winners. */
    String winningTeamsString = "";
    List<Team> winningTeams = TableUtils.getWinningTeams(race);
    if (winningTeams != null) {
      Team winningTeam = winningTeams.remove(0);
      winningTeamsString += winningTeam.getTeamName();
      for (Team remainingTeam: winningTeams) {
        winningTeamsString += " and " + remainingTeam.getTeamName();
      }
    }
    
    String messageHtml = String.format("The results for week %s (%s) are in! " +
        "Check out the scores below: <br><br> %s. <br><br>Congratulations to %s " +
        "for getting the top score this week! Check out the " +
        "<a href='http://fantasy-nascar.appspot.com/standings'>standings</a> " +
        "page for the latest standings."
        , race.getWeek(), race.getRaceName(), resultsTableHtml, winningTeamsString);
    sendHtmlMessage(subject, messageHtml);
  }

  private static void sendHtmlMessage(String subject, String messageHtml) {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(FROM_ADDRESS, FROM_USER));
      message = addRecipients(message);
      message.setSubject(subject);
      
      /* Set up the message content. */
      Multipart multipart = new MimeMultipart();
      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(messageHtml, "text/html");
      multipart.addBodyPart(htmlPart);
      message.setContent(multipart);
      /* End setting up message content. */
      
      Transport.send(message);
    } catch (AddressException e) {
      e.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  
  private static Message addRecipients(Message message) throws
      UnsupportedEncodingException, MessagingException {
    List<Owner> owners = DatastoreManager.getAllObjects(Owner.class);
    if (owners != null) {
      for (Owner owner: owners) {
        message.addRecipient(
            Message.RecipientType.TO, new InternetAddress(owner.getEmail()));
      }
    }
    return message;
  }
}
