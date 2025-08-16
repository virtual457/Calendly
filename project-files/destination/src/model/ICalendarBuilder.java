package model;



interface ICalendarBuilder<T extends ICalendar> {
  ICalendarBuilder<T> setCalendarName(String name);

  ICalendarBuilder<T> setTimezone(String timezone);

  ICalendar build();
}