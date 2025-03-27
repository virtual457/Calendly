package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CopyEventsCommandTest {

  private static class MockModel implements ICalendarModel {
    boolean called = false;
    LocalDateTime start, end;
    String source, target;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName, LocalDateTime fromDateTime, String newValue, boolean editAll) {
      return false;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
      return false;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      return false;
    }

    @Override
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime) {
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendar, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                              String targetCalendar, java.time.LocalDate targetStartDate) {
      called = true;
      this.source = sourceCalendar;
      this.target = targetCalendar;
      this.start = rangeStart;
      this.end = rangeEnd;
      return true;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName, String targetCalendarName, LocalDateTime targetStart) {
      return false;
    }

    @Override
    public boolean isCalendarPresent(String calName) {
      return false;
    }

    @Override
    public boolean editCalendar(String calendarName, String property, String newValue) {
      return false;
    }

  }

  @Test
  public void testCopyEventsOnValid() {
    MockModel model = new MockModel();
    CopyEventsCommand command = new CopyEventsCommand(
          Arrays.asList("on", "2025-05-01", "--target", "Work", "to", "2025-05-02"),
          model,
          "Personal"
    );
    String result = command.execute();
    assertEquals("Events copied successfully.", result);
    assertTrue(model.called);
    assertEquals("Personal", model.source);
    assertEquals("Work", model.target);
    assertEquals(LocalDateTime.parse("2025-05-01T00:00"), model.start);
    assertEquals(LocalDateTime.parse("2025-05-01T23:59:59.999999999"), model.end);
  }

  @Test
  public void testCopyEventsBetweenValid() {
    MockModel model = new MockModel();
    CopyEventsCommand command = new CopyEventsCommand(
          Arrays.asList("between", "2025-05-01", "and", "2025-05-03", "--target", "Work", "to", "2025-06-01"),
          model,
          "Default"
    );
    String result = command.execute();
    assertEquals("Events copied successfully.", result);
    assertTrue(model.called);
    assertEquals(LocalDateTime.parse("2025-05-01T00:00"), model.start);
    assertEquals(LocalDateTime.parse("2025-05-03T23:59:59.999999999"), model.end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArgs() {
    new CopyEventsCommand(Collections.emptyList(), new MockModel(), "Test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTargetAfterOn() {
    new CopyEventsCommand(
          Arrays.asList("on", "2025-05-01", "WRONG", "Work", "to", "2025-05-02"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingToKeyword() {
    new CopyEventsCommand(
          Arrays.asList("on", "2025-05-01", "--target", "Work", "WRONG", "2025-05-02"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSourceDate() {
    new CopyEventsCommand(
          Arrays.asList("on", "not-a-date", "--target", "Work", "to", "2025-05-02"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTargetDate() {
    new CopyEventsCommand(
          Arrays.asList("on", "2025-05-01", "--target", "Work", "to", "bad-date"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidStartDateBetween() {
    new CopyEventsCommand(
          Arrays.asList("between", "bad", "and", "2025-05-03", "--target", "Work", "to", "2025-06-01"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingAndKeyword() {
    new CopyEventsCommand(
          Arrays.asList("between", "2025-05-01", "WRONG", "2025-05-03", "--target", "Work", "to", "2025-06-01"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownTypeKeyword() {
    new CopyEventsCommand(
          Arrays.asList("badkeyword", "2025-05-01", "--target", "Work", "to", "2025-06-01"),
          new MockModel(),
          "Test"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTargetDateAfterOn() {
    List<String> args = Arrays.asList("on", "2025-05-10", "--target", "Work", "to", "not-a-date");

    new CopyEventsCommand(args, new CopyEventsCommandTest.MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTargetStartDateInBetween() {
    List<String> args = Arrays.asList(
          "between", "2025-05-01", "and", "2025-05-10",
          "--target", "Work", "to", "not-a-date"
    );

    new CopyEventsCommand(args, new CopyEventsCommandTest.MockModel(), "Default");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingEventName_EmptyArgsList() {
    new CopyEventCommand(Collections.emptyList(),new CopyEventsCommandTest.MockModel() , "CalA");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingEventName() {
    new CopyEventCommand(Collections.emptyList(), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingOnKeyword() {
    List<String> args = Arrays.asList("EventName");
    new CopyEventCommand(args, new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingSourceDateTime() {
    List<String> args = Arrays.asList("EventName", "on");
    new CopyEventCommand(args, new MockModel(), "Cal");
  }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingTargetCalendarName() {
      List<String> args = Arrays.asList("EventName", "on", "2025-05-01T10:00", "--target");
      new CopyEventCommand(args, new MockModel(), "Cal");
    }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTargetDateTime() {
    List<String> args = Arrays.asList(
          "EventName", "on", "2025-05-01T10:00", "--target", "TargetCal", "to"
    );
    new CopyEventCommand(args, new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTargetDateTime() {
    List<String> args = Arrays.asList(
          "EventName", "on", "2025-05-01T10:00", "--target", "TargetCal", "to", "not-a-date"
    );
    new CopyEventCommand(args, new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncompleteCommand_TriggersIndexOutOfBoundsFallback() {
    List<String> args = Arrays.asList(
          "EventName", "on", "2025-05-01T10:00", "--target", "TargetCal", "to"
    );

    new CopyEventCommand(args, new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOn_MissingTargetDate() {
    List<String> args = Arrays.asList("on", "2025-05-01", "--target", "TargetCal", "to");
    new CopyEventsCommand(args, new MockModel(), "SourceCal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetween_MissingTargetStartDate() {
    List<String> args = Arrays.asList("between", "2025-05-01", "and", "2025-05-05",
          "--target", "TargetCal", "to");
    new CopyEventsCommand(args, new MockModel(), "SourceCal");
  }








}



