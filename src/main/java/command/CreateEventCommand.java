package command;

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
  private final ICalendarModel model;
  private final String calendarName;
  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean autoDecline;
  private final boolean isRecurring;
  private final List<DayOfWeek> recurrenceDays;
  private final Integer recurrenceCount;
  private final LocalDateTime recurrenceEndDate;
  private final String description;
  private final String location;
  private final boolean isPrivate;

  public CreateEventCommand(List<String> args, ICalendarModel model, String calendarName) {
    this.model = model;
    this.calendarName = calendarName;
    this.recurrenceDays = new ArrayList<>();
    Integer count = null;
    LocalDateTime until = null;
    boolean recurring = false;
    boolean autoDeclineFlag = false;
    boolean isOn = false;

    String tempDescription = "";
    String tempLocation = "";
    boolean tempIsPrivate = false;
    boolean descSet = false;
    boolean locSet = false;
    boolean privateSet = false;

    int index = 0;
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
      index++;
      if (index >= args.size()) {
        throw new IllegalArgumentException("Missing start datetime after 'from'");
      }
      this.startDateTime = LocalDateTime.parse(args.get(index++));

      if (index >= args.size() || !args.get(index).equals("to")) {
        throw new IllegalArgumentException("Expected 'to' after start time");
      }
      index++;
      if (index >= args.size()){
        throw new IllegalArgumentException("Missing end datetime after 'to'");
      }
      this.endDateTime = LocalDateTime.parse(args.get(index++));

    } else if (args.get(index).equals("on")) {
      index++;
      isOn = true;
      if (index >= args.size()){
        throw new IllegalArgumentException("Missing date after 'on'");
      }
      this.startDateTime = LocalDate.parse(args.get(index)).atStartOfDay();
      this.endDateTime = this.startDateTime.withHour(23).withMinute(59).withSecond(59);
      index++;
    } else {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }

    if (index < args.size() && args.get(index).equals("repeats")) {
      recurring = true;
      index++;
      if (index >= args.size()) throw new IllegalArgumentException("Missing weekdays after 'repeats'");
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
          if(isOn){
            until = LocalDate.parse(args.get(index++)).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
          }
          else {
            until = LocalDateTime.parse(args.get(index++));
          }
        }
      }
    }

    while (index < args.size()) {
      String keyword = args.get(index);
      switch (keyword) {
        case "--description":
          if (descSet) {
            throw new IllegalArgumentException("Duplicate --description flag");
          }
          descSet = true;
          index++;
          if (index >= args.size()){
            throw new IllegalArgumentException("Missing value for --description");
          }
          tempDescription = args.get(index++);
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
          tempLocation = args.get(index++);
          break;
        case "--private":
          if (privateSet){
            throw new IllegalArgumentException("Duplicate --private flag");
          }
          privateSet = true;
          tempIsPrivate = true;
          index++;
          if (index < args.size() && !args.get(index).startsWith("--")) {
            throw new IllegalArgumentException("--private does not take a value");
          }
          break;
        default:
          throw new IllegalArgumentException("Unrecognized extra argument: " + keyword);
      }
    }

    this.autoDecline = autoDeclineFlag;
    this.isRecurring = recurring;
    this.recurrenceCount = count;
    this.recurrenceEndDate = until;
    this.description = tempDescription;
    this.location = tempLocation;
    this.isPrivate = tempIsPrivate;
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
              .setAutoDecline(autoDecline)
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
      case 'M': return DayOfWeek.MONDAY;
      case 'T': return DayOfWeek.TUESDAY;
      case 'W': return DayOfWeek.WEDNESDAY;
      case 'R': return DayOfWeek.THURSDAY;
      case 'F': return DayOfWeek.FRIDAY;
      case 'S': return DayOfWeek.SATURDAY;
      case 'U': return DayOfWeek.SUNDAY;
      default: throw new IllegalArgumentException("Invalid weekday character: " + day);
    }
  }
}