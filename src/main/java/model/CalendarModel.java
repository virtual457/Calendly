package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class CalendarModel implements ICalendarModel {
  private List<CalendarEvent> events;

  public CalendarModel() {
    this.events = new ArrayList<>();
  }

  @Override
  public boolean addEvent(CalendarEventDTO eventDTO) {

    events.add(null);
    return true;
  }

  @Override
  public boolean editEvent(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    //TODO write logic to find and edit the event
    return false;
  }

  @Override
  public boolean editEvents(String property, String eventName, LocalDateTime fromDateTime, LocalDateTime toDateTime, String newValue) {
    //Todo write logic to find and edit events
    return false;
  }

  @Override
  public String printEvents(LocalDate fromDate, LocalDate toDate) {
    //Todo to wrrite logic to create print events
    return "";
  }

  @Override
  public String exportEvents(String filename) {
    //TODo write logic to create a file and export all events into it and return the file path
    return "";
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    //Todo write logic to show status on that datetime free or not
    return "";
  }

  private Boolean doesEventConflict(CalendarEventDTO eventDTO){
    //TODO check existing events with the given DTO
    return false;
  }

  private Boolean checkStatus(LocalDateTime dateTime){
    //Todo check if there are events for that datetime
    return false;
  }
}