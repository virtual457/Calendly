package controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import model.ICalendarEventDTO;

/**
 * Adapts high-level object operations to command strings
 * and executes them through a command executor.
 */
public class ObjectToCommandAdapter implements ICalendarCommandAdapter {
  private final ICommandExecutor executor;

  public ObjectToCommandAdapter(ICommandExecutor executor) {
    this.executor = executor;
  }

  /**
   * Creates a calendar with the given name and timezone.
   * @return true if successful
   */
  public boolean createCalendar(String name, String timezone) {
    try {
      String command = String.format("create calendar --name \"%s\" --timezone \"%s\"", name, timezone);
      executor.executeCommand(command);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Switches to the specified calendar.
   * @return true if successful
   */
  public boolean useCalendar(String calendarName) {
    try {
      String command = String.format("use calendar --name \"%s\"", calendarName);
      executor.executeCommand(command);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Creates an event from the provided event DTO.
   * @return true if successful
   */
  public boolean createEvent(ICalendarEventDTO event) {
    try {
      StringBuilder command = new StringBuilder();
      command.append("create event \"")
            .append(event.getEventName())
            .append("\"");

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

      // Check if all-day event
      boolean isAllDay = isAllDayEvent(event.getStartDateTime(), event.getEndDateTime());

      if (isAllDay) {
        // Format for all-day events
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        command.append(" on ")
              .append(event.getStartDateTime().toLocalDate().format(dateFormatter));
      } else {
        // Format for timed events
        command.append(" from ")
              .append(event.getStartDateTime().format(formatter))
              .append(" to ")
              .append(event.getEndDateTime().format(formatter));
      }

      // Add recurring info if needed
      if (Boolean.TRUE.equals(event.isRecurring()) && event.getRecurrenceDays() != null) {
        // Build recurrence pattern
        StringBuilder pattern = new StringBuilder();
        for (DayOfWeek day : event.getRecurrenceDays()) {
          pattern.append(getDayCode(day));
        }
        command.append(" repeats ").append(pattern);

        // Add termination
        if (event.getRecurrenceCount() != null) {
          command.append(" for ").append(event.getRecurrenceCount()).append(" times");
        } else if (event.getRecurrenceEndDate() != null) {
          DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          command.append(" until ").append(event.getRecurrenceEndDate().format(dateFormatter));
        }
      }

      // Add optional properties
      if (event.getEventDescription() != null && !event.getEventDescription().isEmpty()) {
        command.append(" --description \"")
              .append(event.getEventDescription())
              .append("\"");
      }

      if (event.getEventLocation() != null && !event.getEventLocation().isEmpty()) {
        command.append(" --location \"")
              .append(event.getEventLocation())
              .append("\"");
      }

      if (Boolean.TRUE.equals(event.isPrivate())) {
        command.append(" --private");
      }

      executor.executeCommand(command.toString());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Edits event properties.
   */
  public boolean editEvent(String property, String eventName,
                           LocalDateTime fromDateTime, LocalDateTime toDateTime,
                           String newValue) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

      StringBuilder command = new StringBuilder();
      command.append("edit event ")
            .append(property)
            .append(" \"")
            .append(eventName)
            .append("\" from ")
            .append(fromDateTime.format(formatter))
            .append(" to ")
            .append(toDateTime.format(formatter))
            .append(" with \"")
            .append(newValue)
            .append("\"");

      executor.executeCommand(command.toString());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Edits multiple events.
   */
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    try {
      StringBuilder command = new StringBuilder();
      command.append("edit events ")
            .append(property)
            .append(" \"")
            .append(eventName)
            .append("\"");

      if (fromDateTime != null) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        command.append(" from ")
              .append(fromDateTime.format(formatter));
      }

      command.append(" with \"")
            .append(newValue)
            .append("\"");

      executor.executeCommand(command.toString());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean editEventsNoStartDate(String property, String eventName, String newValue) {
    try {
      StringBuilder command = new StringBuilder();
      command.append("edit events ")
            .append(property)
            .append(" \"")
            .append(eventName)
            .append("\" \"")
            .append(newValue)
            .append("\"");

      executor.executeCommand(command.toString());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Exports events to a file.
   */
  public boolean exportCalendar(String filePath) {
    try {
      String command = "export cal \"" + filePath + "\"";
      executor.executeCommand(command);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Imports events from a file.
   */
  public boolean importCalendar(String filePath) {
    try {
      String command = "import cal \"" + filePath + "\"";
      executor.executeCommand(command);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // Helper methods
  private boolean isAllDayEvent(LocalDateTime start, LocalDateTime end) {
    return start.toLocalTime().equals(LocalTime.MIDNIGHT) &&
          end.toLocalTime().equals(LocalTime.of(23, 59, 59)) &&
          start.toLocalDate().equals(end.toLocalDate());
  }

  private char getDayCode(DayOfWeek day) {
    switch (day) {
      case MONDAY: return 'M';
      case TUESDAY: return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY: return 'R';
      case FRIDAY: return 'F';
      case SATURDAY: return 'S';
      case SUNDAY: return 'U';
      default: throw new IllegalArgumentException("Unknown day: " + day);
    }
  }
}