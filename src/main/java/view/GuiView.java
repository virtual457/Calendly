package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.io.*;
import java.util.List;

import controller.ICommandExecutor;
import controller.ICalendarController;
import model.ICalendarEventDTO;
import model.IReadOnlyCalendarModel;
import model.ICalendarModel;
import view.IView;

public class GuiView extends JFrame implements IView {
  // Model references
  private IReadOnlyCalendarModel model;
  private ICommandExecutor controller;

  // GUI components
  private JPanel mainPanel;
  private JPanel calendarPanel;
  private JPanel controlPanel;
  private JLabel monthYearLabel;
  private JComboBox<String> calendarSelector;
  private JButton prevButton, nextButton, createEventButton, editEventButton ,exportButton, importButton;
  private JButton createCalendarButton;
  private Map<LocalDate, JPanel> dayPanels;

  // Current view state
  private YearMonth currentYearMonth;
  private String selectedCalendar;
  private ZoneId currentTimezone;

  public GuiView(IReadOnlyCalendarModel model) {
    this.model = model;
    this.currentYearMonth = YearMonth.now();
    this.currentTimezone = ZoneId.systemDefault();
    this.selectedCalendar = "Default";
    this.dayPanels = new HashMap<>();


  }

  private void initializeUI() {
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLayout(new BorderLayout());

    controller.executeCommand("create calendar --name Default --timezone UTC");
    controller.executeCommand("use calendar --name Default");

    // Initialize control panel (top)
    initializeControlPanel();

    // Initialize calendar panel (center)
    initializeCalendarPanel();

    // Initialize side panel for options
    initializeSidePanel();

    // Load initial calendar data
    refreshCalendarView();

    setVisible(true);
  }

  private void initializeControlPanel() {
    controlPanel = new JPanel(new FlowLayout());

    // Month navigation
    prevButton = new JButton("<");
    prevButton.addActionListener(e -> navigateMonth(-1));

    monthYearLabel = new JLabel();
    updateMonthYearLabel();

    nextButton = new JButton(">");
    nextButton.addActionListener(e -> navigateMonth(1));

    // Calendar selection
    calendarSelector = new JComboBox<>();
    updateCalendarSelector();
    calendarSelector.addActionListener(e -> switchCalendar());

    // Create calendar button
    createCalendarButton = new JButton("New Calendar");
    createCalendarButton.addActionListener(e -> createNewCalendar());

    controlPanel.add(prevButton);
    controlPanel.add(monthYearLabel);
    controlPanel.add(nextButton);
    controlPanel.add(calendarSelector);
    controlPanel.add(createCalendarButton);

    add(controlPanel, BorderLayout.NORTH);
  }

  private void initializeCalendarPanel() {
    mainPanel = new JPanel(new BorderLayout());

    // Day headers
    JPanel headerPanel = new JPanel(new GridLayout(1, 7));
    String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : daysOfWeek) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      headerPanel.add(label);
    }

    // Calendar grid
    calendarPanel = new JPanel(new GridLayout(0, 7));

    mainPanel.add(headerPanel, BorderLayout.NORTH);
    mainPanel.add(calendarPanel, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);
  }

  private void initializeSidePanel() {
    JPanel sidePanel = new JPanel();
    sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

    createEventButton = new JButton("Create Event");
    createEventButton.addActionListener(e -> createNewEvent());

    editEventButton = new JButton("Edit Event");
    editEventButton.addActionListener(e -> editEvent());

    exportButton = new JButton("Export Calendar");
    exportButton.addActionListener(e -> exportCalendar());

    importButton = new JButton("Import Calendar");
    importButton.addActionListener(e -> importCalendar());

    sidePanel.add(createEventButton);
    sidePanel.add(Box.createVerticalStrut(10));
    sidePanel.add(editEventButton);
    sidePanel.add(Box.createVerticalStrut(10));
    sidePanel.add(exportButton);
    sidePanel.add(Box.createVerticalStrut(10));
    sidePanel.add(importButton);

    add(sidePanel, BorderLayout.EAST);
  }



  private void updateMonthYearLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthYearLabel.setText(currentYearMonth.format(formatter));
  }

  private void updateCalendarSelector() {
    ActionListener[] listeners = calendarSelector.getActionListeners();

    // Remove all action listeners temporarily
    for (ActionListener listener : listeners) {
      calendarSelector.removeActionListener(listener);
    }

    // Update the items
    calendarSelector.removeAllItems();
    List<String> calendarNames = model.getCalendarNames();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }

    // Set selection to the first calendar or the previously selected one if it still exists
    if (calendarSelector.getItemCount() > 0) {
      if (selectedCalendar != null && calendarNames.contains(selectedCalendar)) {
        calendarSelector.setSelectedItem(selectedCalendar);
      } else {
        calendarSelector.setSelectedIndex(0);
        selectedCalendar = (String) calendarSelector.getSelectedItem();
      }
    }

    // Re-add the action listeners
    for (ActionListener listener : listeners) {
      calendarSelector.addActionListener(listener);
    }
  }

  private void refreshCalendarView() {
    calendarPanel.removeAll();
    dayPanels.clear();

    // Get first day of month
    LocalDate firstDay = currentYearMonth.atDay(1);

    // Calculate the start of the calendar grid (might be in previous month)
    LocalDate start = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7);

    // Create 6 weeks of calendar panels (42 days)
    for (int i = 0; i < 42; i++) {
      LocalDate date = start.plusDays(i);
      JPanel dayPanel = createDayPanel(date);
      calendarPanel.add(dayPanel);
      dayPanels.put(date, dayPanel);
    }

    // Update events on the calendar
    updateEventsOnCalendar();

    calendarPanel.revalidate();
    calendarPanel.repaint();
  }

  private JPanel createDayPanel(LocalDate date) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    // Day number label
    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));

    // Highlight current month days
    if (date.getMonth() != currentYearMonth.getMonth()) {
      dayLabel.setForeground(Color.LIGHT_GRAY);
      panel.setBackground(new Color(245, 245, 245));
    } else {
      panel.setBackground(Color.WHITE);
    }

    // Highlight today
    if (date.equals(LocalDate.now())) {
      panel.setBackground(new Color(230, 230, 255));
      dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
    }

    panel.add(dayLabel, BorderLayout.NORTH);

    // Make the panel clickable to show/edit events
    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        showDayEvents(date);
      }
    });

    return panel;
  }

  private void updateEventsOnCalendar() {
    // Clear existing event indicators
    for (JPanel panel : dayPanels.values()) {
      // Remove all components except the day number label
      Component[] components = panel.getComponents();
      if (components.length > 1) {
        for (int i = 1; i < components.length; i++) {
          panel.remove(components[i]);
        }
      }
    }

    // Add event indicators for the selected calendar
    for (Map.Entry<LocalDate, List<ICalendarEventDTO>> entry :
          getEventsInMonthRange(selectedCalendar,
                currentYearMonth.atDay(1),
                currentYearMonth.atEndOfMonth()).entrySet()) {

      LocalDate date = entry.getKey();
      List<ICalendarEventDTO> events = entry.getValue();

      JPanel dayPanel = dayPanels.get(date);
      if (dayPanel != null && !events.isEmpty()) {
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));

        // Add up to 3 event indicators
        for (int i = 0; i < Math.min(3, events.size()); i++) {
          JLabel eventLabel = new JLabel("• " + events.get(i).getEventName());
          eventLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
          eventsPanel.add(eventLabel);
        }

        // If there are more events, add a "+more" indicator
        if (events.size() > 3) {
          JLabel moreLabel = new JLabel("+" + (events.size() - 3) + " more");
          moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
          eventsPanel.add(moreLabel);
        }

        dayPanel.add(eventsPanel, BorderLayout.CENTER);
      }
    }

    calendarPanel.revalidate();
    calendarPanel.repaint();
  }

  private void navigateMonth(int months) {
    currentYearMonth = currentYearMonth.plusMonths(months);
    updateMonthYearLabel();
    refreshCalendarView();
  }

  private void switchCalendar() {
    selectedCalendar = (String) calendarSelector.getSelectedItem();
    refreshCalendarView();
  }

  private void createNewCalendar() {
    String name = JOptionPane.showInputDialog(this, "Enter calendar name:");
    if (name != null && !name.trim().isEmpty()) {
      // Show timezone selection dialog
      String[] availableZones = getAvailableTimezones();
      String selectedZone = (String) JOptionPane.showInputDialog(
            this,
            "Select timezone:",
            "Timezone Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            availableZones,
            ZoneId.systemDefault().getId());

      if (selectedZone != null) {
        boolean created = createCalendar(name, selectedZone);
        if (created) {
          updateCalendarSelector();
          calendarSelector.setSelectedItem(name);
          selectedCalendar = name;
          refreshCalendarView();
        } else {
          JOptionPane.showMessageDialog(this,
                "Failed to create calendar. Name may already exist.",
                "Creation Error",
                JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private String[] getAvailableTimezones() {
    Set<String> zoneIds = ZoneId.getAvailableZoneIds();
    ArrayList<String> zones = new ArrayList<>(zoneIds);
    Collections.sort(zones);
    return zones.toArray(new String[0]);
  }

  private void showDayEvents(LocalDate date) {
    List<ICalendarEventDTO> events = getEventsForDay(selectedCalendar, date);

    JPanel eventPanel = new JPanel();
    eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));

    JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    dateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    eventPanel.add(dateLabel);
    eventPanel.add(Box.createVerticalStrut(10));

    if (events.isEmpty()) {
      eventPanel.add(new JLabel("No events scheduled for this day"));
    } else {
      for (ICalendarEventDTO event : events) {
        JPanel singleEventPanel = new JPanel(new BorderLayout());
        singleEventPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel titleLabel = new JLabel(event.getEventName());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        String timeStr = event.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
              " - " +
              event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = new JLabel(timeStr);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editEvent(event));

        singleEventPanel.add(titleLabel, BorderLayout.NORTH);
        singleEventPanel.add(timeLabel, BorderLayout.CENTER);
        singleEventPanel.add(editButton, BorderLayout.EAST);

        eventPanel.add(singleEventPanel);
        eventPanel.add(Box.createVerticalStrut(5));
      }
    }

    JButton addButton = new JButton("Add New Event");
    addButton.addActionListener(e -> createEventOnDay(date));
    eventPanel.add(addButton);

    JOptionPane.showMessageDialog(this, eventPanel, "Events on " + date.toString(), JOptionPane.PLAIN_MESSAGE);
  }

  private void createNewEvent() {
    // Create event on currently selected day or today
    createEventOnDay(LocalDate.now());
  }

  private void createEventOnDay(LocalDate date) {
    // Create event dialog
    JPanel panel = new JPanel(new GridLayout(0, 2));

    JTextField nameField = new JTextField();
    JTextField descriptionField = new JTextField();
    JTextField startTimeField = new JTextField("09:00");
    JTextField endTimeField = new JTextField("10:00");

    // Recurring event options
    JCheckBox recurringCheck = new JCheckBox("Recurring Event");
    String[] recurrenceOptions = {"Daily", "Weekly", "Monthly"};
    JComboBox<String> recurrenceType = new JComboBox<>(recurrenceOptions);

    panel.add(new JLabel("Event Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Description:"));
    panel.add(descriptionField);
    panel.add(new JLabel("Start Time (HH:MM):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End Time (HH:MM):"));
    panel.add(endTimeField);
    panel.add(recurringCheck);
    panel.add(recurrenceType);

    // Additional fields for recurring events (initially hidden)
    JPanel recurringPanel = new JPanel(new GridLayout(0, 2));
    JTextField occurrencesField = new JTextField("10");
    JTextField endDateField = new JTextField(date.plusMonths(1).toString());

    recurringPanel.add(new JLabel("Number of Occurrences:"));
    recurringPanel.add(occurrencesField);
    recurringPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
    recurringPanel.add(endDateField);

    recurringCheck.addActionListener(e -> {
      boolean isRecurring = recurringCheck.isSelected();
      recurrenceType.setEnabled(isRecurring);
      occurrencesField.setEnabled(isRecurring);
      endDateField.setEnabled(isRecurring);
    });

    // Create the final dialog panel
    JPanel dialogPanel = new JPanel();
    dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
    dialogPanel.add(panel);
    dialogPanel.add(recurringPanel);

    int result = JOptionPane.showConfirmDialog(
          this, dialogPanel, "Create New Event", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = nameField.getText();
        String description = descriptionField.getText();

        if (name.isEmpty()) {
          throw new IllegalArgumentException("Event name cannot be empty");
        }

        // Parse times
        LocalTime startTime = LocalTime.parse(startTimeField.getText());
        LocalTime endTime = LocalTime.parse(endTimeField.getText());

        if (startTime.isAfter(endTime)) {
          throw new IllegalArgumentException("Start time must be before end time");
        }

        boolean created;

        if (recurringCheck.isSelected()) {
          // Create recurring event
          String recurrence = (String) recurrenceType.getSelectedItem();
          int occurrences = Integer.parseInt(occurrencesField.getText());
          LocalDate endDate = LocalDate.parse(endDateField.getText());
          ICalendarEventDTO event = ICalendarEventDTO.builder().build();

          created = createRecurringEvent(event);
        } else {
          // Create single event
          ICalendarEventDTO event = ICalendarEventDTO.builder().build();
          created = createEvent(event);
        }

        if (created) {
          refreshCalendarView();
        } else {
          JOptionPane.showMessageDialog(this,
                "Failed to create event. There may be a scheduling conflict.",
                "Creation Error",
                JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
              "Error creating event: " + ex.getMessage(),
              "Input Error",
              JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void editEvent() {
    // This method is called from the side panel button
    // We need to first select an event to edit

    // Create a dialog to select a date
    JPanel datePanel = new JPanel(new GridLayout(0, 2));
    JTextField dateField = new JTextField(LocalDate.now().toString());
    datePanel.add(new JLabel("Enter date (YYYY-MM-DD):"));
    datePanel.add(dateField);

    int dateResult = JOptionPane.showConfirmDialog(
        this, datePanel, "Select Date", JOptionPane.OK_CANCEL_OPTION);

    if (dateResult == JOptionPane.OK_OPTION) {
      try {
        LocalDate selectedDate = LocalDate.parse(dateField.getText());
        List<ICalendarEventDTO> events = getEventsForDay(selectedCalendar, selectedDate);

        if (events.isEmpty()) {
          JOptionPane.showMessageDialog(this,
              "No events found on selected date.",
              "No Events",
              JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        // Create a dialog to select an event
        String[] eventNames = events.stream()
            .map(e -> e.getEventName() + " (" +
                e.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " +
                e.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) + ")")
            .toArray(String[]::new);

        String selectedEventString = (String) JOptionPane.showInputDialog(
            this,
            "Select event to edit:",
            "Select Event",
            JOptionPane.QUESTION_MESSAGE,
            null,
            eventNames,
            eventNames[0]);

        if (selectedEventString != null) {
          // Find the selected event
          int eventIndex = -1;
          for (int i = 0; i < eventNames.length; i++) {
            if (eventNames[i].equals(selectedEventString)) {
              eventIndex = i;
              break;
            }
          }

          if (eventIndex >= 0) {
            // Edit the selected event
            editEvent(events.get(eventIndex));
          }
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void editEvent(ICalendarEventDTO event) {
    // Create event dialog with fields pre-filled from the event
    JPanel panel = new JPanel(new GridLayout(0, 2));

    JTextField nameField = new JTextField(event.getEventName());
    JTextField descriptionField = new JTextField(
        event.getEventDescription() != null ? event.getEventDescription() : "");

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    JTextField startTimeField = new JTextField(event.getStartDateTime().format(timeFormatter));
    JTextField endTimeField = new JTextField(event.getEndDateTime().format(timeFormatter));

    // For recurring events
    JCheckBox editAllInstancesCheck = new JCheckBox("Edit all instances of recurring event");
    editAllInstancesCheck.setEnabled(Boolean.TRUE.equals(event.isRecurring()));

    panel.add(new JLabel("Event Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Description:"));
    panel.add(descriptionField);
    panel.add(new JLabel("Start Time (HH:MM):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End Time (HH:MM):"));
    panel.add(endTimeField);

    if (Boolean.TRUE.equals(event.isRecurring())) {
      panel.add(new JLabel("Recurring Event:"));
      panel.add(editAllInstancesCheck);
    }

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Edit Event", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = nameField.getText();
        String description = descriptionField.getText();

        if (name.isEmpty()) {
          throw new IllegalArgumentException("Event name cannot be empty");
        }

        // Parse times
        LocalTime startTime = LocalTime.parse(startTimeField.getText());
        LocalTime endTime = LocalTime.parse(endTimeField.getText());

        if (startTime.isAfter(endTime)) {
          throw new IllegalArgumentException("Start time must be before end time");
        }

        // Create new start and end times, preserving the original date
        LocalDateTime newStartDateTime = LocalDateTime.of(
            event.getStartDateTime().toLocalDate(), startTime);
        LocalDateTime newEndDateTime = LocalDateTime.of(
            event.getEndDateTime().toLocalDate(), endTime);

        // Build the edit command
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        StringBuilder command = new StringBuilder();
        command.append("edit event --calendar \"").append(selectedCalendar).append("\"");
        command.append(" --eventName \"").append(event.getEventName()).append("\"");
        command.append(" --from \"").append(event.getStartDateTime().format(formatter)).append("\"");
        command.append(" --to \"").append(event.getEndDateTime().format(formatter)).append("\"");

        // Add properties to update
        if (!name.equals(event.getEventName())) {
          command.append(" --property name --newValue \"").append(name).append("\"");
        } else if (!description.equals(event.getEventDescription())) {
          command.append(" --property description --newValue \"").append(description).append("\"");
        } else if (!newStartDateTime.equals(event.getStartDateTime())) {
          command.append(" --property start --newValue \"").append(newStartDateTime.format(formatter)).append("\"");
        } else if (!newEndDateTime.equals(event.getEndDateTime())) {
          command.append(" --property end --newValue \"").append(newEndDateTime.format(formatter)).append("\"");
        }

        // Add flag for all instances if applicable
        if (editAllInstancesCheck.isSelected()) {
          command.append(" --all");
        }

        // Execute the command
        controller.executeCommand(command.toString());
        refreshCalendarView();

      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error updating event: " + ex.getMessage(),
            "Update Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }



  private void exportCalendar() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Calendar");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      String path = file.getAbsolutePath();
      if (!path.endsWith(".csv")) {
        path += ".csv";
      }

      try {
        boolean exported = exportCalendar(selectedCalendar, path);
        if (exported) {
          JOptionPane.showMessageDialog(this,
                "Calendar exported successfully to " + path,
                "Export Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(this,
                "Failed to export calendar.",
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
              "Error exporting calendar: " + ex.getMessage(),
              "Export Error",
              JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void importCalendar() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Calendar");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();

      try {
        boolean imported = importCalendar(selectedCalendar, file.getAbsolutePath());
        if (imported) {
          JOptionPane.showMessageDialog(this,
                "Calendar imported successfully",
                "Import Success",
                JOptionPane.INFORMATION_MESSAGE);
          refreshCalendarView();
        } else {
          JOptionPane.showMessageDialog(this,
                "Failed to import calendar. There may be scheduling conflicts.",
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
              "Error importing calendar: " + ex.getMessage(),
              "Import Error",
              JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  // Inside CalendarStarter class
  private Map<LocalDate, List<ICalendarEventDTO>> getEventsInMonthRange(String calendarName,
                                                                        LocalDate startOfMonth,
                                                                        LocalDate endOfMonth) {
    Map<LocalDate, List<ICalendarEventDTO>> eventsByDay = new HashMap<>();

    // Get all events in the month range
    List<ICalendarEventDTO> allEvents = model.getEventsInRange(
          calendarName,
          startOfMonth.atStartOfDay(),
          endOfMonth.atTime(23, 59, 59)
    );

    // Group events by day
    for (ICalendarEventDTO event : allEvents) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      eventsByDay.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(event);
    }

    return eventsByDay;
  }

  private boolean createCalendar(String name, String timezone) {
    try {
      controller.executeCommand("create calendar --name " + name + " --timezone " + timezone);
    }
    catch (Exception ex) {
      System.err.println("Error creating calendar: " + ex.getMessage());
      return false;
    }
    return true;
  }

  private List<ICalendarEventDTO> getEventsForDay(String calendarName, LocalDate date){

    return model.getEventsInRange(
          calendarName,
          date.atStartOfDay(),
          date.atTime(23, 59, 59)
    );
  }


  private boolean createRecurringEvent(ICalendarEventDTO event) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
      String command = "create event --calendar \"" + selectedCalendar + "\" --name \"" + event.getEventName() + "\"" +
          " from \"" + event.getStartDateTime().format(formatter) + "\"" +
          " to \"" + event.getEndDateTime().format(formatter) + "\"" +
          " --recurring";
      // Add more recurring parameters if needed
      controller.executeCommand(command);
    }
    catch (Exception ex) {
      return false;
    }
    return true;
  }

  private boolean createEvent(ICalendarEventDTO event) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
      String command = "create event --calendar \"" + selectedCalendar + "\" --name \"" + event.getEventName() + "\"" +
          " from \"" + event.getStartDateTime().format(formatter) + "\"" +
          " to \"" + event.getEndDateTime().format(formatter) + "\"";
      controller.executeCommand(command);
    }
    catch (Exception ex) {
      return false;
    }
    return true;
  }

  private boolean exportCalendar(String calendarName, String path) {
    try {
      controller.executeCommand("export cal --calendar \"" + calendarName + "\" --file \"" + path + "\"");
    }
    catch (Exception ex) {
      return false;
    }
    return true;
  }

  private boolean importCalendar(String calendarName, String path) {
    try {
      controller.executeCommand("import cal --calendar \"" + calendarName + "\" --file \"" + path + "\"");
    }
    catch (Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public void display(String message) {
    System.out.println(message);
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    this.controller = commandExecutor;
    initializeUI();
  }
}