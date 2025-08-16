package controller.command;

import controller.command.EditEventsCalendarCommand;
import model.ICalendarEventDTO;
import model.ICalendarModel;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link EditEventsCalendarCommand} class.
 * This class ensures correct behavior of editing events in the calendar via commands,
 * including property updates, error handling, and validation.
 */

public class EditEventsCalendarCommandTest {

  private static class MockModel implements ICalendarModel {
    boolean called = false;
    String lastProperty;
    String lastEventName;
    String lastNewValue;
    LocalDateTime lastFrom;
    boolean lastHasFrom;

    @Override
    public boolean createCalendar(String calName, String timezone) {
      return false;
    }

    @Override
    public boolean addEvent(String calendarName, ICalendarEventDTO event) {
      return false;
    }

    @Override
    public boolean editEvents(String calendarName, String property, String eventName,
                              LocalDateTime fromDateTime, String newValue, boolean strict) {
      this.called = true;
      this.lastProperty = property;
      this.lastEventName = eventName;
      this.lastNewValue = newValue;
      this.lastFrom = fromDateTime;
      this.lastHasFrom = (fromDateTime != null);
      return true;
    }

    @Override
    public boolean editEvent(String calendarName, String property, String eventName,
                             LocalDateTime fromDateTime, LocalDateTime toDateTime,
                             String newValue) {
      return false;
    }

    @Override
    public boolean isCalendarAvailable(String calName, LocalDate date) {
      return false;
    }

    @Override
    public List<String> getCalendarNames() {
      return List.of();
    }

    @Override
    public String getCalendarTimeZone(String calendarName) {
      return "";
    }

    @Override
    public boolean deleteCalendar(String calName) {
      return false;
    }

    @Override
    public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                    LocalDateTime fromDateTime,
                                                    LocalDateTime toDateTime) {
      return List.of();
    }

    @Override
    public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                               LocalDateTime dateTime) {
      return List.of();
    }

    @Override
    public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                              LocalDateTime sourceEnd, String targetCalendarName,
                              LocalDate targetStart) {
      return false;
    }

    @Override
    public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                             String eventName, String targetCalendarName,
                             LocalDateTime targetStart) {
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

    @Override
    public boolean addEvents(String calendarName, List<ICalendarEventDTO> events,
                             String timezone) {
      return false;
    }
  }

  @Test
  public void testEditWithoutFrom() {
    MockModel model = new MockModel();
    EditEventsCalendarCommand command = new EditEventsCalendarCommand(
        Arrays.asList("location", "EventName", "NewRoom"),
        model, "Default"
    );

    String result = command.execute();
    assertEquals("Events updated successfully.", result);
    assertTrue(model.called);
    assertEquals(LocalDateTime.MIN,model.lastFrom);
    assertEquals("location", model.lastProperty);
    assertEquals("NewRoom", model.lastNewValue);
  }

  @Test
  public void testEditWithFrom() {
    MockModel model = new MockModel();
    EditEventsCalendarCommand command = new EditEventsCalendarCommand(
        Arrays.asList("name", "ProjectSync", "from", "2025-05-01T10:00", "with", "Team Sync"),
        model, "WorkCal"
    );

    String result = command.execute();
    assertEquals("Events updated successfully.", result);
    assertTrue(model.called);
    assertTrue(model.lastHasFrom);
    assertEquals(LocalDateTime.parse("2025-05-01T10:00"), model.lastFrom);
    assertEquals("Team Sync", model.lastNewValue);
  }

  @Test
  public void testEditFailureResponse() {
    ICalendarModel model = new ICalendarModel() {
      @Override
      public boolean createCalendar(String calName, String timezone) {
        return false;
      }

      @Override
      public boolean addEvent(String calendarName, ICalendarEventDTO event) {
        return false;
      }

      @Override
      public boolean editEvents(String c, String p, String e, LocalDateTime d, String v,
                                boolean s) {
        return false;
      }

      @Override
      public boolean editEvent(String calendarName, String property, String eventName,
                               LocalDateTime fromDateTime, LocalDateTime toDateTime,
                               String newValue) {
        return false;
      }

      @Override
      public boolean isCalendarAvailable(String calName, LocalDate date) {
        return false;
      }

      @Override
      public List<String> getCalendarNames() {
        return List.of();
      }

      @Override
      public String getCalendarTimeZone(String calendarName) {
        return "";
      }

      @Override
      public boolean deleteCalendar(String calName) {
        return false;
      }

      @Override
      public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                      LocalDateTime fromDateTime,
                                                      LocalDateTime toDateTime) {
        return List.of();
      }

      @Override
      public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                                 LocalDateTime dateTime) {
        return List.of();
      }

      @Override
      public boolean copyEvents(String sourceCalendarName, LocalDateTime sourceStart,
                                LocalDateTime sourceEnd, String targetCalendarName,
                                LocalDate targetStart) {
        return false;
      }

      @Override
      public boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart,
                               String eventName, String targetCalendarName,
                               LocalDateTime targetStart) {
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

      @Override
      public boolean addEvents(String calendarName, List<ICalendarEventDTO> events,String timezone) {
        return false;
      }
    };

    EditEventsCalendarCommand command = new EditEventsCalendarCommand(
        Arrays.asList("name", "ProjectSync", "from", "2025-05-01T10:00", "with", "Team Sync"),
        model, "Any"
    );

    String result = command.execute();
    assertEquals("No matching events found to update.", result);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testMissingProperty() {
    new EditEventsCalendarCommand(Collections.emptyList(), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingEventName() {
    new EditEventsCalendarCommand(Collections.singletonList("location"), new MockModel(), "Cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingFromDatetime() {
    new EditEventsCalendarCommand(
        Arrays.asList("name", "Meeting", "from"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingWithKeyword() {
    new EditEventsCalendarCommand(
        Arrays.asList("name", "Meeting", "from", "2025-01-01T10:00"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNewValueAfterWith() {
    new EditEventsCalendarCommand(
        Arrays.asList("name", "Meeting", "from", "2025-01-01T10:00", "with"),
        new MockModel(), "Cal"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingNewValueWithoutFrom() {
    new EditEventsCalendarCommand(
        Arrays.asList("name", "Meeting"),
        new MockModel(), "Cal"
    );
  }
}
