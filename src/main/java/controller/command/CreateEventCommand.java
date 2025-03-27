package controller.command;

import model.ICalendarEventDTO;
import model.ICalendarModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to create single or recurring events, all-day or timed,
 * with support for optional fields: description, location, and privacy.
 */
public class CreateEventCommand implements ICommand {
  final ICalendarModel model;
  final String calendarName;
  final String eventName;
  LocalDateTime startDateTime;
  LocalDateTime endDateTime;
  final boolean autoDecline;
  final boolean isRecurring;
  final List<DayOfWeek> recurrenceDays;
  final Integer recurrenceCount;
  LocalDateTime recurrenceEndDate;
  String description;
  String location;
  boolean isPrivate;
  List<String> args;
  Integer index;
  boolean isOn;
  Integer count = null;

  public CreateEventCommand(List<String> fromArgs, ICalendarModel model,
                            String calendarName) {
    this.model = model;
    this.calendarName = calendarName;
    this.recurrenceDays = new ArrayList<>();
    this.args = fromArgs;
    this.index = 0;
    boolean recurring = false;
    boolean autoDeclineFlag = false;
    this.location = "";
    this.isPrivate = false;
    this.description = "";


    if (index < args.size() && args.get(index).equals("--autoDecline")) {
      autoDeclineFlag = true;
      index++;
    }

    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing event name.");
    }
    this.eventName = args.get(index++);

    if (index >= args.size()) {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }

    if (args.get(index).equals("from")) {
      parseFromPart();
    } else if (args.get(index).equals("on")) {
      isOn = true;
      parseOnPart();
    } else {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }

    if (index < args.size() && args.get(index).equals("repeats")) {
      recurring = true;
      parseRepeatsPart();
    }

    parseOptionalParams();


    this.autoDecline = autoDeclineFlag;
    this.isRecurring = recurring;
    this.recurrenceCount = count;
  }

  @Override
  public String execute() {
    try {
      ICalendarEventDTO event = ICalendarEventDTO.builder()
          .setEventName(eventName)
          .setStartDateTime(startDateTime)
          .setEndDateTime(endDateTime)
          .setRecurring(isRecurring)
          .setRecurrenceDays(recurrenceDays)
          .setRecurrenceCount(recurrenceCount)
          .setRecurrenceEndDate(recurrenceEndDate)
          .setAutoDecline(Boolean.TRUE)
          .setEventDescription(description)
          .setEventLocation(location)
          .setPrivate(isPrivate)
          .build();

      boolean success = model.addEvent(calendarName, event);
      return success ? "Event created successfully." : "Error: Event creation failed.";
    } catch (IllegalArgumentException | IllegalStateException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  private DayOfWeek mapDay(char day) {
    switch (day) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        throw new IllegalArgumentException("Invalid weekday character: " + day);
    }
  }

  private void parseFromPart() {
    index++;
    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing start datetime after 'from'");
    }
    this.startDateTime = LocalDateTime.parse(args.get(index++));

    if (index >= args.size() || !args.get(index).equals("to")) {
      throw new IllegalArgumentException("Expected 'to' after start time");
    }
    index++;
    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing end datetime after 'to'");
    }
    this.endDateTime = LocalDateTime.parse(args.get(index++));
  }

  private void parseOnPart() {
    index++;
    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing date after 'on'");
    }
    this.startDateTime = LocalDate.parse(args.get(index)).atStartOfDay();
    this.endDateTime = this.startDateTime.withHour(23).withMinute(59).withSecond(59);
    index++;
  }

  private void parseRepeatsPart() {
    index++;
    if (index >= args.size())
      throw new IllegalArgumentException("Missing weekdays after 'repeats'");
    for (char c : args.get(index++).toCharArray()) {
      recurrenceDays.add(mapDay(c));
    }
    if (index < args.size()) {
      if (args.get(index).equals("for")) {
        index++;
        if (index >= args.size()) {
          throw new IllegalArgumentException("Missing recurrence count after 'for'");
        }
        count = Integer.parseInt(args.get(index++));
        if (index >= args.size() || !args.get(index).equals("times")) {
          throw new IllegalArgumentException("Missing keyword 'times'");
        }
        index++;
      } else if (args.get(index).equals("until")) {
        index++;
        if (index >= args.size()) {
          throw new IllegalArgumentException("Missing end date after 'until'");
        }
        if (isOn) {
          this.recurrenceEndDate =
              LocalDate.parse(args.get(index++)).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
        } else {
          this.recurrenceEndDate = LocalDateTime.parse(args.get(index++));
        }
      }
    }
  }


  private void parseOptionalParams() {
    boolean descSet = false;
    boolean locSet = false;
    boolean privateSet = false;

    while (index < args.size()) {

      String keyword = args.get(index);
      switch (keyword) {
        case "--description":
          if (descSet) {
            throw new IllegalArgumentException("Duplicate --description flag");
          }
          descSet = true;
          index++;
          if (index >= args.size()) {
            throw new IllegalArgumentException("Missing value for --description");
          }
          this.description = args.get(index++);
          break;
        case "--location":
          if (locSet) {
            throw new IllegalArgumentException("Duplicate --location flag");
          }
          locSet = true;
          index++;
          if (index >= args.size()) {
            throw new IllegalArgumentException("Missing value for --location");
          }
          this.location = args.get(index++);
          break;
        case "--private":
          if (privateSet) {
            throw new IllegalArgumentException("Duplicate --private flag");
          }
          privateSet = true;
          this.isPrivate = true;
          index++;
          if (index < args.size() && !args.get(index).startsWith("--")) {
            throw new IllegalArgumentException("--private does not take a value");
          }
          break;
        default:
          throw new IllegalArgumentException("Unrecognized extra argument: " + keyword);
      }
    }
  }
}