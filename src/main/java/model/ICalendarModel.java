package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the data model interface for the Calendar application.
 * <p>
 * This interface defines the core methods that any calendar model must implement,
 * including functionality for adding, editing, and printing events, as well as
 * exporting event data and determining user availability.
 * </p>
 */

public interface ICalendarModel {

  /**
   * A factory method to create an instance of {@code ICalendarModel} based on the given type.
   * <p>
   * This method checks the specified {@code type} string and returns a corresponding
   * implementation of the {@link ICalendarModel} interface. If the {@code type}
   * string is not recognized, an {@link IllegalArgumentException} is thrown.
   * </p>
   *
   * @param type a string that indicates which model implementation to create
   * @return a new instance of an {@code ICalendarModel} implementation
   * @throws IllegalArgumentException if the given {@code type} is invalid or unsupported
   */
  //TODO:add all these methhods throws exception
  static ICalendarModel createInstance(String type) {
    if (type.equalsIgnoreCase("listBased")) {
      return new CalendarModel();
    } else {
      throw new IllegalArgumentException("Invalid CalendarModel type.");
    }
  }

  boolean createCalendar(String calName, String timezone);

  boolean addEvent(String calendarName, ICalendarEventDTO event);

  boolean editEvents(String calendarName, String property, String eventName,
                     LocalDateTime fromDateTime,
                     String newValue, boolean editAll);

  boolean editEvent(String calendarName, String property, String eventName,
                    LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue);

  boolean isCalendarAvailable(String calName, LocalDate date);

  boolean deleteCalendar(String calName);


  List<ICalendarEventDTO> getEventsInRange(String calendarName, LocalDateTime fromDateTime,
                                           LocalDateTime toDateTime);


  List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName, LocalDateTime dateTime);

  boolean copyEvents(String sourceCalendarName,
                     LocalDateTime sourceStart, LocalDateTime sourceEnd,
                     String targetCalendarName,
                     LocalDate targetStart);

  boolean copyEvent(String sourceCalendarName, LocalDateTime sourceStart, String eventName,
                    String targetCalendarName,
                    LocalDateTime targetStart);

  boolean isCalendarPresent(String calName);

  boolean editCalendar(String calendarName, String property, String newValue);

}
