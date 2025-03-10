package controller;

import java.time.*;
import java.util.*;

import model.CalendarEventDTO;
import model.ICalendarModel;

class CalendarController implements ICalendarController {
  private final ICalendarModel model;

  public CalendarController(ICalendarModel model) {
    this.model = model;
  }

  public void processCommand(String command) {
    Scanner parts = new Scanner(command);

    if (parts.hasNext("create")) {
      parts.next();
      if (parts.hasNext("event")) {
        parts.next();
        boolean autoDecline = false;
        if (parts.hasNext("--autoDecline")) {
          parts.next();
          autoDecline = true;
        }

        if (!parts.hasNext()) {
          System.out.println("Error: Missing event name.");
          return;
        }
        String eventName = parts.next();

        CalendarEventDTO.CalendarEventDTOBuilder eventBuilder = new CalendarEventDTO.CalendarEventDTOBuilder()
                .eventName(eventName)
                .autoDecline(autoDecline);

        if (parts.hasNext("from")) {
          parts.next();
          String startDateTimeStr = parts.next();
          LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr);
          eventBuilder.startDateTime(startDateTime);

          if (parts.hasNext("to")) {
            parts.next();
            String endDateTimeStr = parts.next();
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr);
            eventBuilder.endDateTime(endDateTime);
          } else {
            System.out.println("Error: Missing 'to' keyword and end time.");
            return;
          }
        } else if (parts.hasNext("on")) {
          parts.next();
          String dateString = parts.next();
          LocalDate date = LocalDate.parse(dateString);
          LocalDateTime startDateTime = date.atStartOfDay();
          LocalDateTime endDateTime = startDateTime.plusDays(1).minusSeconds(1);
          eventBuilder.startDateTime(startDateTime).endDateTime(endDateTime);
        }

        if (parts.hasNext("repeats")) {
          parts.next();
          String weekdays = parts.next();
          List<DayOfWeek> recurrenceDays = new ArrayList<>();
          for (char day : weekdays.toCharArray()) {
            switch (day) {
              case 'M': recurrenceDays.add(DayOfWeek.MONDAY); break;
              case 'T': recurrenceDays.add(DayOfWeek.TUESDAY); break;
              case 'W': recurrenceDays.add(DayOfWeek.WEDNESDAY); break;
              case 'R': recurrenceDays.add(DayOfWeek.THURSDAY); break;
              case 'F': recurrenceDays.add(DayOfWeek.FRIDAY); break;
              case 'S': recurrenceDays.add(DayOfWeek.SATURDAY); break;
              case 'U': recurrenceDays.add(DayOfWeek.SUNDAY); break;
              default: System.out.println("Error: Invalid recurrence day."); return;
            }
          }
          eventBuilder.recurring(true, recurrenceDays);

          if (parts.hasNext("for")) {
            parts.next();
            int recurrenceCount = Integer.parseInt(parts.next());
            eventBuilder.recurrenceCount(recurrenceCount);
          } else if (parts.hasNext("until")) {
            parts.next();
            String recurrenceEndStr = parts.next();
            LocalDateTime recurrenceEndDate = LocalDateTime.parse(recurrenceEndStr);
            eventBuilder.recurrenceEndDate(recurrenceEndDate);
          }
        }

        CalendarEventDTO event = eventBuilder.build();
        model.addEvent(event);
        System.out.println("Event created: " + event.getEventName());
      } else {
        System.out.println("Error: Expected 'event' after 'create'.");
      }
    }
    else if (parts.hasNext("edit")) {
      parts.next();
      String eventType = parts.next(); // Consume 'event' or 'events'
      String property = parts.next();
      String eventName = parts.next();

      if (parts.hasNext("from")) {
        parts.next();
        String startDateTimeStr = parts.next();
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr);

        if (parts.hasNext("to")) {
          parts.next();
          String endDateTimeStr = parts.next();
          LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr);

          if (parts.hasNext("with")) {
            parts.next();
            String newValue = parts.next();
            model.editEvent(property, eventName, startDateTime, endDateTime, newValue);
          }
        } else if (parts.hasNext("with")) {
          parts.next();
          String newValue = parts.next();
          model.editEvents(property, eventName, startDateTime, newValue);
        }
      } else if (parts.hasNext("with")) {
        parts.next();
        String newValue = parts.next();
        model.editEvents(property, eventName, null, newValue);
      }
    }
    else if (parts.hasNext("print")) {
      parts.next();
      if (parts.hasNext("events")) {
        parts.next();
        if (parts.hasNext("on")) {
          parts.next();
          String dateStr = parts.next();
          LocalDate date = LocalDate.parse(dateStr);
          model.printEventsOnSpecificDate(date);
        } else if (parts.hasNext("from")) {
          parts.next();
          String startDateTimeStr = parts.next();
          parts.next();
          String endDateTimeStr = parts.next();
          LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr);
          LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr);
          model.printEventsInSpecificRange(startDateTime, endDateTime);
        }
      }
    }
    else if (parts.hasNext("export")) {
      parts.next();
      parts.next();
      String fileName = parts.next();
      model.exportEvents(fileName);
    }
    else if (parts.hasNext("show")) {
      parts.next();
      parts.next();
      String dateTimeStr = parts.next();
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
      model.showStatus(dateTime);
    }
    else {
      System.out.println("Error: Unsupported command.");
    }
    parts.close();
  }
}
