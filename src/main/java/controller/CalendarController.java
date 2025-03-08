package controller;


import java.time.LocalDateTime;
import java.util.Arrays;

import model.CalendarEvent;


import model.ICalendarModel;

class CalendarController implements ICalendarController {
  private final ICalendarModel model;

  public CalendarController(ICalendarModel model) {
    this.model = model;
  }

  public void processCommand(final String command) {
    final String[] parts = command.split(" ");
    if (parts[0].equals("create") && parts[1].equals("event")) {
      boolean autoDecline = Arrays.asList(parts).contains("--autoDecline");
      LocalDateTime start = LocalDateTime.parse(parts[3] + "T" + parts[4]);
      LocalDateTime end = LocalDateTime.parse(parts[6] + "T" + parts[7]);
      CalendarEvent event = new CalendarEvent(parts[2], start, end, "", "", true, false, null);
      model.addEvent(event, autoDecline);
      System.out.println("Event created: " + event.getEventName());
    } else {
      System.out.println("Invalid command.");
    }
  }
}
