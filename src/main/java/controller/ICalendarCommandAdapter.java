package controller;


import java.time.LocalDateTime;
import model.ICalendarEventDTO;

/**
 * Interface defining high-level calendar operations.
 * Adapts object-oriented operations to command strings.
 */
public interface ICalendarCommandAdapter {
  /**
   * Creates a calendar with the given name and timezone.
   * @return true if successful
   */
  boolean createCalendar(String name, String timezone);

  /**
   * Switches to the specified calendar.
   * @return true if successful
   */
  boolean useCalendar(String calendarName);

  /**
   * Creates an event from the provided event DTO.
   * @return true if successful
   */
  boolean createEvent(ICalendarEventDTO event);

  /**
   * Edits event properties.
   */
  boolean editEvent(String property, String eventName,
                    LocalDateTime fromDateTime, LocalDateTime toDateTime,
                    String newValue);

  /**
   * Edits multiple events.
   */
  boolean editEvents(String property, String eventName,
                     LocalDateTime fromDateTime, String newValue);

  /**
   * Edits events properties without specifying a start date.
   *
   * @param property The property to edit (name, description, location, etc.)
   * @param eventName The name of the event(s) to edit
   * @param newValue The new value for the property
   * @return true if successful
   */
  boolean editEventsNoStartDate(String property, String eventName, String newValue);
  /**
   * Exports events to a file.
   */
  boolean exportCalendar(String filePath);

  // In ICalendarCommandAdapter interface - we can keep it simple
  /**
   * Imports events from a CSV file.
   * All events will be validated first and import will only proceed if all events can be added.
   *
   * @param filePath The path to the CSV file to import
   * @return true if all events were successfully imported, false otherwise
   */
  boolean importCalendar(String filePath,String timezone);

  /**
  exits the program
   **/
  void exit();

}