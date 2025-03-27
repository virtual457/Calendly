package model;


// Generic builder interface for flexibility
interface ICalendarBuilder<T extends ICalendar> {
  ICalendarBuilder<T> setCalendarName(String name);
  ICalendarBuilder<T> setTimezone(String timezone);
  ICalendar build();
}