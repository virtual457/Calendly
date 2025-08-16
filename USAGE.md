# 📖 Usage Guide

This guide provides detailed instructions for using the Calendar Management System across different modes and interfaces.

## 🚀 Getting Started

### **Prerequisites**
- Java 11 or higher
- Maven 3.6+

### **Build & Run**
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run mutation tests
mvn pitest:mutationCoverage

# Execute the application
java -cp target/classes calendarapp.CalendarApp --mode gui
```

## 🖥️ Application Modes

### **GUI Mode (Default)**
The graphical user interface provides an intuitive way to interact with the calendar system.

```bash
# Launch GUI mode
java -cp target/classes calendarapp.CalendarApp

# Or explicitly specify GUI mode
java -cp target/classes calendarapp.CalendarApp --mode gui
```

**Features:**
- Visual calendar interface
- Drag-and-drop event management
- Real-time updates
- User-friendly controls

### **Interactive Console Mode**
Command-line interface for interactive calendar management.

```bash
# Launch interactive console mode
java -cp target/classes calendarapp.CalendarApp --mode interactive
```

**Available Commands:**
- `create calendar <name> <timezone>` - Create a new calendar
- `use calendar <name>` - Switch to a specific calendar
- `create event <name> <start> <end> <location> <description>` - Add new event
- `edit event <name> <property> <newValue>` - Modify existing event
- `copy event <source> <target>` - Copy event between calendars
- `export events <filename>` - Export events to CSV
- `import calendar <filename>` - Import calendar from CSV
- `print events` - Display all events
- `show status` - Show current calendar status
- `exit` - Exit the application

### **Headless Mode**
Automated processing mode for batch operations and scripting.

```bash
# Launch headless mode with input file
java -cp target/classes calendarapp.CalendarApp --mode headless input.txt

# With additional run mode parameter
java -cp target/classes calendarapp.CalendarApp --mode headless input.txt true
```

**Input File Format:**
```
create calendar "Work Calendar" "America/New_York"
use calendar "Work Calendar"
create event "Team Meeting" "2024-01-15T10:00" "2024-01-15T11:00" "Conference Room A" "Weekly team sync"
create event "Client Call" "2024-01-16T14:00" "2024-01-16T15:00" "Zoom" "Project discussion"
export events "work_events.csv"
```

## 📋 Command Reference

### **Calendar Management**

#### Create Calendar
```bash
create calendar "Calendar Name" "Timezone"
```
- Creates a new calendar with specified name and timezone
- Example: `create calendar "Personal" "America/Los_Angeles"`

#### Use Calendar
```bash
use calendar "Calendar Name"
```
- Switches to the specified calendar for operations
- Example: `use calendar "Work Calendar"`

#### Edit Calendar
```bash
edit calendar "Calendar Name" <property> <newValue>
```
- Modifies calendar properties (name, timezone)
- Example: `edit calendar "Work Calendar" name "Office Calendar"`

#### Delete Calendar
```bash
delete calendar "Calendar Name"
```
- Removes the specified calendar and all its events

### **Event Management**

#### Create Event
```bash
create event <name> <startDateTime> <endDateTime> <location> <description>
```
- Adds a new event to the current calendar
- DateTime format: `YYYY-MM-DDTHH:MM`
- Example: `create event "Meeting" "2024-01-15T10:00" "2024-01-15T11:00" "Room 101" "Team sync"`

#### Edit Event
```bash
edit event <name> <property> <newValue>
```
- Modifies specific event properties
- Properties: name, start, end, location, description
- Example: `edit event "Meeting" location "Room 202"`

#### Edit Events (Bulk)
```bash
edit events <property> <eventName> <startDateTime> <newValue> <editAll>
```
- Bulk edit events matching criteria
- Example: `edit events location "Meeting" "2024-01-15T10:00" "Room 303" true`

#### Copy Event
```bash
copy event <sourceCalendar> <startDateTime> <eventName> <targetCalendar> <targetDateTime>
```
- Copies a single event between calendars
- Example: `copy event "Work" "2024-01-15T10:00" "Meeting" "Personal" "2024-01-16T10:00"`

#### Copy Events (Bulk)
```bash
copy events <sourceCalendar> <startDateTime> <endDateTime> <targetCalendar> <targetDate>
```
- Copies multiple events within a time range
- Example: `copy events "Work" "2024-01-15T00:00" "2024-01-15T23:59" "Personal" "2024-01-16"`

### **Data Operations**

#### Export Events
```bash
export events <filename>
```
- Exports current calendar events to CSV file
- Example: `export events "my_events.csv"`

#### Import Calendar
```bash
import calendar <filename>
```
- Imports calendar data from CSV file
- Example: `import calendar "backup.csv"`

#### Print Events
```bash
print events
```
- Displays all events in the current calendar

#### Show Status
```bash
show status
```
- Shows current calendar information and statistics

## 📁 File Formats

### **CSV Export Format**
```csv
Event Name,Start Date,End Date,Location,Description
Team Meeting,2024-01-15T10:00,2024-01-15T11:00,Conference Room A,Weekly team sync
Client Call,2024-01-16T14:00,2024-01-16T15:00,Zoom,Project discussion
```

### **CSV Import Format**
```csv
Calendar Name,Event Name,Start Date,End Date,Location,Description,Timezone
Work Calendar,Team Meeting,2024-01-15T10:00,2024-01-15T11:00,Conference Room A,Weekly team sync,America/New_York
```

## ⚙️ Configuration

### **Timezone Support**
The application supports all standard timezone identifiers:
- `America/New_York`
- `America/Los_Angeles`
- `Europe/London`
- `Asia/Tokyo`
- `UTC`

### **Date/Time Formats**
- **Date**: `YYYY-MM-DD`
- **DateTime**: `YYYY-MM-DDTHH:MM`
- **Time**: `HH:MM` (24-hour format)

## 🔧 Advanced Features

### **Command Chaining**
Multiple commands can be executed in sequence in headless mode:
```
create calendar "Work" "America/New_York"
use calendar "Work"
create event "Meeting 1" "2024-01-15T10:00" "2024-01-15T11:00" "Room A" "First meeting"
create event "Meeting 2" "2024-01-15T14:00" "2024-01-15T15:00" "Room B" "Second meeting"
export events "work_meetings.csv"
```

### **Error Handling**
The application provides detailed error messages for:
- Invalid date/time formats
- Calendar not found
- Event conflicts
- File I/O errors
- Invalid command syntax

### **Validation**
- Date/time validation ensures logical event scheduling
- Conflict detection prevents overlapping events
- Input validation for all user-provided data

## 🆘 Troubleshooting

### **Common Issues**

1. **"Calendar not found"**
   - Ensure the calendar exists before using it
   - Use `create calendar` command first

2. **"Invalid date format"**
   - Use ISO format: `YYYY-MM-DDTHH:MM`
   - Example: `2024-01-15T10:00`

3. **"Event conflict detected"**
   - Check for overlapping events
   - Adjust start/end times to avoid conflicts

4. **"File not found"**
   - Verify file path is correct
   - Ensure file has proper read permissions

### **Getting Help**
- Use `show status` to check current state
- Review command syntax in this guide
- Check error messages for specific issues

---

*For more information about the application architecture and design patterns, see the main README.md file.*
