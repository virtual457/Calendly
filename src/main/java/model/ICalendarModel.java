package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

  static ICalendarModel createInstance(String type) {
    if (type.equalsIgnoreCase("listBased")) {
      return new CalendarModel();
    } else {
      throw new IllegalArgumentException("Invalid CalendarModel type.");
    }
  }

  boolean addEvent(ICalendarEventDTO event);

  boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue);

  boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, String newValue);

  String printEventsOnSpecificDate(LocalDate date);

  String printEventsInSpecificRange(LocalDateTime fromDateTime, LocalDateTime toDateTime);

  String exportEvents(String filename);

  String showStatus(LocalDateTime dateTime);

}
