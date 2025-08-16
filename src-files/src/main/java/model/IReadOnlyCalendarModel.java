package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a read-only view of the calendar model.
 * Only provides methods for querying data without modification capabilities.
 */
public interface IReadOnlyCalendarModel {


  static IReadOnlyCalendarModel createInstance(ICalendarModel model) {
    return new ReadOnlyCalendarModel(model);
  }

  /**
   * Gets events within a specified date-time range.
   */
  List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                           LocalDateTime fromDateTime,
                                           LocalDateTime toDateTime);

  /**
   * Gets events at a specific date and time.
   */
  List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                      LocalDateTime dateTime);

  /**
   * Checks if a calendar with the given name exists.
   */
  boolean isCalendarPresent(String calName);

  /**
   * Checks if a calendar is available on a specific date.
   */
  boolean isCalendarAvailable(String calName, LocalDate date);

  List<String> getCalendarNames();

  String getCalendarTimeZone(String calendarName);
}