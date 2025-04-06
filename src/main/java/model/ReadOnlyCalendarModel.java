package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * An adapter that provides read-only access to an ICalendarModel.
 */
public class ReadOnlyCalendarModel implements IReadOnlyCalendarModel {
  private final ICalendarModel model;

  /**
   * Creates a read-only adapter for the given model.
   *
   * @param model the calendar model to adapt
   */
  public ReadOnlyCalendarModel(ICalendarModel model) {
    this.model = model;
  }

  @Override
  public List<ICalendarEventDTO> getEventsInRange(String calendarName,
                                                  LocalDateTime fromDateTime,
                                                  LocalDateTime toDateTime) {
    return model.getEventsInRange(calendarName, fromDateTime, toDateTime);
  }

  @Override
  public List<ICalendarEventDTO> getEventsInSpecificDateTime(String calendarName,
                                                             LocalDateTime dateTime) {
    return model.getEventsInSpecificDateTime(calendarName, dateTime);
  }

  @Override
  public boolean isCalendarPresent(String calName) {
    return model.isCalendarPresent(calName);
  }

  @Override
  public boolean isCalendarAvailable(String calName, LocalDate date) {
    return model.isCalendarAvailable(calName, date);
  }

  @Override
  public List<String> getCalendarNames() {
    return model.getCalendarNames();
  }


}