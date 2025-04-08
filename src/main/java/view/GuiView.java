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
import model.ICalendarEventDTOBuilder;
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
    useCalendar(selectedCalendar);
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


  private void createNewEvent() {
    // Create event on currently selected day or today
    createEventOnDay(LocalDate.now());
  }

  private void createEventOnDay(LocalDate date) {
    // Create event dialog
    JPanel panel = new JPanel(new GridLayout(0, 2));

    JTextField nameField = new JTextField();
    JTextField descriptionField = new JTextField();
    JTextField locationField = new JTextField();


    JTextField startDateField = new JTextField(date.toString());
    JTextField endDateField = new JTextField(date.toString());

    JTextField startTimeField = new JTextField("09:00");
    JTextField endTimeField = new JTextField("10:00");

    JPanel daysPanel = new JPanel(new GridLayout(1, 7));
    JCheckBox[] dayCheckboxes = new JCheckBox[7];
    String[] dayLabels = {"M", "T", "W", "Th", "F", "S", "Su"};
    char[] dayCodes = {'M', 'T', 'W', 'R', 'F', 'S', 'U'};

    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = new JCheckBox(dayLabels[i]);
      dayCheckboxes[i].setEnabled(false); // Initially disabled
      daysPanel.add(dayCheckboxes[i]);
    }
    JCheckBox isPrivate = new JCheckBox("Private Event");

    // Recurring event options
    JCheckBox recurringCheck = new JCheckBox("Recurring Event");

    String[] recurrenceOptions = {"Select...", "Daily", "Weekdays", "Weekends", "Weekly", "Custom"};
    JComboBox<String> recurrenceType = new JComboBox<>(recurrenceOptions);

    recurrenceType.setEnabled(false);

    recurrenceType.addActionListener(e -> {
      String selected = (String) recurrenceType.getSelectedItem();

      // Reset all checkboxes first
      for (JCheckBox dayBox : dayCheckboxes) {
        dayBox.setSelected(false);
      }

      // Enable/disable checkboxes based on selection
      boolean enableCheckboxes = "Custom".equals(selected);
      for (JCheckBox dayBox : dayCheckboxes) {
        dayBox.setEnabled(enableCheckboxes);
      }

      // Set checkboxes based on pattern
      switch (selected) {
        case "Daily":
          // Select all days
          for (JCheckBox dayBox : dayCheckboxes) {
            dayBox.setSelected(true);
          }
          break;
        case "Weekdays":
          // Select Monday-Friday
          for (int i = 0; i < 5; i++) {
            dayCheckboxes[i].setSelected(true);
          }
          break;
        case "Weekends":
          // Select Saturday-Sunday
          dayCheckboxes[5].setSelected(true);
          dayCheckboxes[6].setSelected(true);
          break;
        case "Weekly":
          // Select the current day of week
          LocalDate selectedDate = LocalDate.parse(startDateField.getText());
          int dayIndex = selectedDate.getDayOfWeek().getValue() - 1; // 0-6 (Monday-Sunday)
          dayCheckboxes[dayIndex].setSelected(true);
          break;
        case "Custom":
          // All checkboxes are enabled, none selected by default
          break;
      }
    });


    panel.add(new JLabel("Event Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Description:"));
    panel.add(descriptionField);
    panel.add(new JLabel("location:"));
    panel.add(locationField);
    panel.add(new JLabel("IsPrivate"));
    panel.add(isPrivate);
    panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    panel.add(startDateField);
    panel.add(new JLabel("End Date (YYYY-MM-DD):"));
    panel.add(endDateField);
    panel.add(new JLabel("Start Time (HH:MM):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End Time (HH:MM):"));
    panel.add(endTimeField);
    panel.add(recurringCheck);
    panel.add(recurrenceType);
    panel.add(new JLabel("Repeat on:"));
    panel.add(daysPanel);

    // Additional fields for recurring events (initially hidden)
    JPanel recurringPanel = new JPanel(new GridLayout(0, 2));

    // Create radio buttons for recurrence termination
    JRadioButton occurrencesRadio = new JRadioButton("Number of occurrences:");
    JRadioButton endDateRadio = new JRadioButton("End date:");
    ButtonGroup terminationGroup = new ButtonGroup();
    terminationGroup.add(occurrencesRadio);
    terminationGroup.add(endDateRadio);
    occurrencesRadio.setSelected(false);

    JTextField occurrencesField = new JTextField();
    JTextField recurrenceEndDateField = new JTextField();
    recurrenceEndDateField.setEnabled(false);
    occurrencesField.setEnabled(false);

    recurringPanel.add(occurrencesRadio);
    recurringPanel.add(occurrencesField);
    recurringPanel.add(endDateRadio);
    recurringPanel.add(recurrenceEndDateField);

    occurrencesRadio.setEnabled(false);
    endDateRadio.setEnabled(false);
    occurrencesField.setEnabled(false);
    recurrenceEndDateField.setEnabled(false);

    occurrencesRadio.addActionListener(e -> {
      occurrencesField.setEnabled(true);
      occurrencesField.setText("10");
      recurrenceEndDateField.setEnabled(false);
      recurrenceEndDateField.setText(null);
    });

    endDateRadio.addActionListener(e -> {
      occurrencesField.setEnabled(false);
      occurrencesField.setText(null);
      recurrenceEndDateField.setEnabled(true);
      recurrenceEndDateField.setText(date.plusMonths(1).toString());
    });





    recurringCheck.addActionListener(e -> {
      boolean isRecurring = recurringCheck.isSelected();
      recurrenceType.setEnabled(isRecurring);

      // Enable/disable recurrence termination options
      occurrencesRadio.setEnabled(isRecurring);
      endDateRadio.setEnabled(isRecurring);

      occurrencesField.setEnabled(isRecurring && occurrencesRadio.isSelected());
      recurrenceEndDateField.setEnabled(isRecurring && endDateRadio.isSelected());


      // Reset and disable day checkboxes when recurring is unchecked
      if (!isRecurring) {
        recurrenceType.setSelectedIndex(0); // Reset to "Select..."
        for (JCheckBox dayBox : dayCheckboxes) {
          dayBox.setSelected(false);
          dayBox.setEnabled(false);
        }
      }

      occurrencesField.setEnabled(isRecurring);
      recurrenceEndDateField.setEnabled(isRecurring);
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
        String location = locationField.getText();

        // Parse dates and times
        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = LocalDate.parse(endDateField.getText());
        LocalTime startTime = LocalTime.parse(startTimeField.getText());
        LocalTime endTime = LocalTime.parse(endTimeField.getText());

        // Build full date-times
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        if (endDateTime.isBefore(startDateTime)) {
          throw new IllegalArgumentException("End date/time must be after start date/time");
        }

        // Create event builder with common properties
        ICalendarEventDTOBuilder<?> builder = ICalendarEventDTO.builder()
              .setEventName(name)
              .setEventDescription(description)
              .setEventLocation(location)
              .setPrivate(isPrivate.isSelected())
              .setStartDateTime(startDateTime)
              .setEndDateTime(endDateTime);

        // Handle recurring events
        if (recurringCheck.isSelected()) {
          // Get selected days
          List<DayOfWeek> selectedDays = new ArrayList<>();
          for (int i = 0; i < dayCheckboxes.length; i++) {
            if (dayCheckboxes[i].isSelected()) {
              selectedDays.add(getDayOfWeekFromIndex(i));
            }
          }

          if (selectedDays.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one day for recurring events");
          }

          // Set recurrence properties
          builder.setRecurring(true)
                .setRecurrenceDays(selectedDays);

          // Add either count or end date, not both
          if (!occurrencesField.getText().isEmpty()) {
            builder.setRecurrenceCount(Integer.parseInt(occurrencesField.getText()));
          } else if (!recurrenceEndDateField.getText().isEmpty()) {
            LocalDate recurrenceEndDate =
                  LocalDate.parse(recurrenceEndDateField.getText());
            builder.setRecurrenceEndDate(recurrenceEndDate.atTime(23, 59, 59));
          } else {
            throw new IllegalArgumentException("Please specify either occurrence count or end date for recurring events");
          }
        } else {
          builder.setRecurring(false);
        }

        // Build the event and create it
        ICalendarEventDTO event = builder.build();
        boolean created = createEvent(event);

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
  // Helper to convert index to DayOfWeek
  private DayOfWeek getDayOfWeekFromIndex(int index) {
    switch (index) {
      case 0: return DayOfWeek.MONDAY;
      case 1: return DayOfWeek.TUESDAY;
      case 2: return DayOfWeek.WEDNESDAY;
      case 3: return DayOfWeek.THURSDAY;
      case 4: return DayOfWeek.FRIDAY;
      case 5: return DayOfWeek.SATURDAY;
      case 6: return DayOfWeek.SUNDAY;
      default: throw new IllegalArgumentException("Invalid day index: " + index);
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
    // Get filename from user
    String filename = JOptionPane.showInputDialog(
          this,
          "Enter export filename (no extension needed):",
          "Export Calendar",
          JOptionPane.QUESTION_MESSAGE
    );

    // Check if user canceled
    if (filename == null) {
      return;
    }

    // Validate filename
    if (filename.trim().isEmpty()) {
      JOptionPane.showMessageDialog(
            this,
            "Filename cannot be empty.",
            "Invalid Filename",
            JOptionPane.ERROR_MESSAGE
      );
      return;
    }

    // Remove any extension if user added one
    if (filename.contains(".")) {
      filename = filename.substring(0, filename.lastIndexOf('.'));
    }

    // Add csv extension
    String outputFile = filename + ".csv";

    try {
      // Export directly with the provided filename
      controller.executeCommand("export cal " + outputFile);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
            this,
            "Error exporting calendar: " + ex.getMessage(),
            "Export Error",
            JOptionPane.ERROR_MESSAGE
      );
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

  // Helper method to determine if an event is an all-day event
  private boolean isAllDayEvent(LocalDateTime start, LocalDateTime end) {
    return start.toLocalTime().equals(LocalTime.MIDNIGHT) &&
          end.toLocalTime().equals(LocalTime.of(23, 59, 59)) &&
          start.toLocalDate().equals(end.toLocalDate());
  }

  private boolean createEvent(ICalendarEventDTO event) {
    try {
      StringBuilder command = new StringBuilder();
      command.append("create event ")
            .append(event.getEventName());

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

      // Check if the event is a full-day event or a timed event
      boolean isAllDayEvent = isAllDayEvent(event.getStartDateTime(), event.getEndDateTime());

      if (isAllDayEvent) {
        // Format for all-day events
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        command.append(" on ")
              .append(event.getStartDateTime().toLocalDate().format(dateFormatter));
      } else {
        // Format for timed events
        command.append(" from ")
              .append(event.getStartDateTime().format(formatter))
              .append(" to ")
              .append(event.getEndDateTime().format(formatter));
      }

      // For recurring events
      if (Boolean.TRUE.equals(event.isRecurring()) && event.getRecurrenceDays() != null) {
        // Create recurrence pattern
        StringBuilder pattern = new StringBuilder();
        for (DayOfWeek day : event.getRecurrenceDays()) {
          pattern.append(getDayCode(day));
        }
        command.append(" repeats ").append(pattern);

        // Add recurrence termination (count or end date)
        if (event.getRecurrenceCount() != null) {
          command.append(" for ").append(event.getRecurrenceCount()).append(" times");
        } else if (event.getRecurrenceEndDate() != null) {
          DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          command.append(" until ").append(event.getRecurrenceEndDate().format(formatter));
        }
      }

      // Add description if provided
      if (event.getEventDescription() != null && !event.getEventDescription().isEmpty()) {
        command.append(" --description \"")
              .append(event.getEventDescription())
              .append("\"");
      }

      // Add location if provided
      if (event.getEventLocation() != null && !event.getEventLocation().isEmpty()) {
        command.append(" --location \"")
              .append(event.getEventLocation())
              .append("\"");
      }

      // Add privacy flag if needed
      if (Boolean.TRUE.equals(event.isPrivate())) {
        command.append(" --private");
      }

      System.out.println("Executing command: " + command.toString());
      controller.executeCommand(command.toString());
      return true;
    }
    catch (Exception ex) {
      System.err.println("Error creating event: " + ex.getMessage());
      return false;
    }
  }

  // Helper method to determine if an event is an all-day event


  // Helper to convert DayOfWeek to your specific code format
  private char getDayCode(DayOfWeek day) {
    switch (day) {
      case MONDAY: return 'M';
      case TUESDAY: return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY: return 'R';
      case FRIDAY: return 'F';
      case SATURDAY: return 'S';
      case SUNDAY: return 'U';
      default: throw new IllegalArgumentException("Unknown day: " + day);
    }
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

  private boolean useCalendar(String calendarName) {
    try {
      controller.executeCommand("use calendar --name \"" + calendarName + "\"");
    }
    catch (Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public void display(String message) {

    // Show message in a dialog box instead of console
    SwingUtilities.invokeLater(() -> {
      // Determine message type based on content
      int messageType = JOptionPane.INFORMATION_MESSAGE;
      if (message.contains("Error") || message.contains("Failed")) {
        messageType = JOptionPane.ERROR_MESSAGE;
      } else if (message.contains("Warning")) {
        messageType = JOptionPane.WARNING_MESSAGE;
      }

      JOptionPane.showMessageDialog(
            this,  // parent component
            message,
            "Calendar Notification",
            messageType
      );
    });
    System.out.println(message);
  }

  @Override
  public void start(ICommandExecutor commandExecutor, Readable readable) {
    this.controller = commandExecutor;
    initializeUI();
  }


  // Modification to your GuiView class

  /**
   * Shows all events for a specific day with options to view details, edit, or add events.
   *
   * @param date The date to show events for
   */
  private void showDayEvents(LocalDate date) {
    List<ICalendarEventDTO> events = getEventsForDay(selectedCalendar, date);

    // Create a custom dialog for better control over size and behavior
    JDialog dialog = new JDialog(this, "Events on " + date.toString(), true);
    dialog.setLayout(new BorderLayout());

    // Main panel with scrolling capability for many events
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel eventPanel = new JPanel();
    eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));
    eventPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Date header
    JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    dateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Events list or message
    if (events.isEmpty()) {
      JLabel noEventsLabel = new JLabel("No events scheduled for this day");
      noEventsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
      noEventsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
      eventPanel.add(noEventsLabel);
    } else {
      // Add a header or instructions
      JLabel instructionLabel = new JLabel("Click View to see full event details");
      instructionLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
      instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
      eventPanel.add(instructionLabel);

      // Add each event with view/edit buttons
      for (ICalendarEventDTO event : events) {
        JPanel singleEventPanel = new JPanel(new BorderLayout());
        singleEventPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(3, 0, 3, 0),
              BorderFactory.createLineBorder(new Color(200, 200, 200))
        ));

        // Event name and icon based on privacy
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel titleLabel = new JLabel(event.getEventName());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Add icon for private events
        if (Boolean.TRUE.equals(event.isPrivate())) {
          titleLabel.setIcon(UIManager.getIcon("FileView.fileIcon"));
        }

        titlePanel.add(titleLabel);

        // Time information
        String timeStr = event.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
              " - " +
              event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Button panel with View and Edit options
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton viewButton = new JButton("View");
        viewButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        viewButton.addActionListener(e -> showEventDetails(event));

        JButton editButton = new JButton("Edit");
        editButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        editButton.addActionListener(e -> {
          dialog.dispose();  // Close current dialog
          editEvent(event);
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);

        // Add a preview of location or description if available
        JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        if (event.getEventLocation() != null && !event.getEventLocation().isEmpty()) {
          JLabel locationLabel = new JLabel("📍 " + event.getEventLocation());
          locationLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
          locationLabel.setForeground(new Color(100, 100, 100));
          previewPanel.add(locationLabel);
        } else if (event.getEventDescription() != null && !event.getEventDescription().isEmpty()) {
          // Show truncated description if no location
          String descPreview = event.getEventDescription();
          if (descPreview.length() > 30) {
            descPreview = descPreview.substring(0, 27) + "...";
          }
          JLabel descLabel = new JLabel(descPreview);
          descLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
          descLabel.setForeground(new Color(100, 100, 100));
          previewPanel.add(descLabel);
        }

        // Recurring event indicator
        if (Boolean.TRUE.equals(event.isRecurring())) {
          JLabel recurringLabel = new JLabel("🔄");
          recurringLabel.setToolTipText("Recurring event");
          previewPanel.add(recurringLabel);
        }

        // Combine all elements
        JPanel eventInfoPanel = new JPanel(new BorderLayout());
        eventInfoPanel.add(titlePanel, BorderLayout.NORTH);
        eventInfoPanel.add(timeLabel, BorderLayout.WEST);
        eventInfoPanel.add(previewPanel, BorderLayout.SOUTH);

        singleEventPanel.add(eventInfoPanel, BorderLayout.CENTER);
        singleEventPanel.add(buttonPanel, BorderLayout.EAST);

        eventPanel.add(singleEventPanel);
        eventPanel.add(Box.createVerticalStrut(5));
      }
    }

    // Button panel for adding new events
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton addButton = new JButton("Add New Event");
    addButton.addActionListener(e -> {
      dialog.dispose();
      createEventOnDay(date);
    });

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(addButton);
    buttonPanel.add(closeButton);

    // Assemble all parts
    JScrollPane scrollPane = new JScrollPane(eventPanel);
    scrollPane.setBorder(null);

    mainPanel.add(dateLabel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    dialog.add(mainPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Size and display the dialog
    dialog.setSize(450, 400);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  /**
   * Shows the detailed view of an event.
   *
   * @param event The event to show details for
   */
  private void showEventDetails(ICalendarEventDTO event) {
    EventDetailsView detailsView = new EventDetailsView(this, event);
    detailsView.setVisible(true);
  }

  // Add this method to view the details of all events in a calendar
  private void viewAllEvents() {
    JPanel optionsPanel = new JPanel(new GridLayout(0, 1));

    JComboBox<String> viewTypeCombo = new JComboBox<>(new String[]{"Current Month", "Date Range", "All Events"});
    optionsPanel.add(new JLabel("View events by:"));
    optionsPanel.add(viewTypeCombo);

    JTextField startDateField = new JTextField(10);
    JTextField endDateField = new JTextField(10);

    JPanel dateRangePanel = new JPanel(new GridLayout(0, 2));
    dateRangePanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    dateRangePanel.add(startDateField);
    dateRangePanel.add(new JLabel("End Date (YYYY-MM-DD):"));
    dateRangePanel.add(endDateField);

    startDateField.setText(LocalDate.now().toString());
    endDateField.setText(LocalDate.now().plusDays(7).toString());
    dateRangePanel.setVisible(false);

    optionsPanel.add(dateRangePanel);

    viewTypeCombo.addActionListener(e -> {
      String selected = (String) viewTypeCombo.getSelectedItem();
      dateRangePanel.setVisible("Date Range".equals(selected));
      optionsPanel.revalidate();
      optionsPanel.repaint();
    });

    int result = JOptionPane.showConfirmDialog(
          this,
          optionsPanel,
          "View Events",
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String viewType = (String) viewTypeCombo.getSelectedItem();
      List<ICalendarEventDTO> events;

      if ("Current Month".equals(viewType)) {
        events = model.getEventsInRange(
              selectedCalendar,
              currentYearMonth.atDay(1).atStartOfDay(),
              currentYearMonth.atEndOfMonth().atTime(23, 59, 59));
      } else if ("Date Range".equals(viewType)) {
        try {
          LocalDate startDate = LocalDate.parse(startDateField.getText());
          LocalDate endDate = LocalDate.parse(endDateField.getText());

          events = model.getEventsInRange(
                selectedCalendar,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
                this,
                "Invalid date format. Please use YYYY-MM-DD format.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
          return;
        }
      } else { // All Events
        events = model.getEventsInRange(
              selectedCalendar,
              LocalDateTime.MIN,
              LocalDateTime.MAX);
      }

      displayEventsInTable(events, viewType);
    }
  }

  /**
   * Display a list of events in a table format.
   *
   * @param events The events to display
   * @param viewTitle The title for the view window
   */
  private void displayEventsInTable(List<ICalendarEventDTO> events, String viewTitle) {
    if (events.isEmpty()) {
      JOptionPane.showMessageDialog(
            this,
            "No events found in the selected range.",
            "No Events",
            JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // Define table column names
    String[] columnNames = {"Event Name", "Start Date", "Start Time", "End Date", "End Time", "Location"};

    // Create table data
    Object[][] data = new Object[events.size()][columnNames.length];
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    for (int i = 0; i < events.size(); i++) {
      ICalendarEventDTO event = events.get(i);
      data[i][0] = event.getEventName();
      data[i][1] = event.getStartDateTime().format(dateFormatter);
      data[i][2] = event.getStartDateTime().format(timeFormatter);
      data[i][3] = event.getEndDateTime().format(dateFormatter);
      data[i][4] = event.getEndDateTime().format(timeFormatter);
      data[i][5] = event.getEventLocation() != null ? event.getEventLocation() : "";
    }

    // Create table
    JTable table = new JTable(data, columnNames);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);

    // Make columns an appropriate width
    table.getColumnModel().getColumn(0).setPreferredWidth(150);  // Event name
    table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Start date
    table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Start time
    table.getColumnModel().getColumn(3).setPreferredWidth(100);  // End date
    table.getColumnModel().getColumn(4).setPreferredWidth(80);   // End time
    table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Location

    // Add double-click listener to show event details
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          JTable target = (JTable) e.getSource();
          int row = target.getSelectedRow();
          // Convert view row index to model row index if the table is sorted
          if (row >= 0) {
            row = table.convertRowIndexToModel(row);
            showEventDetails(events.get(row));
          }
        }
      }
    });

    // Create panel with table and instructions
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel("Double-click an event to view details"), BorderLayout.NORTH);
    panel.add(new JScrollPane(table), BorderLayout.CENTER);

    // Add buttons for actions
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton viewButton = new JButton("View Details");
    viewButton.addActionListener(e -> {
      int selectedRow = table.getSelectedRow();
      if (selectedRow >= 0) {
        selectedRow = table.convertRowIndexToModel(selectedRow);
        showEventDetails(events.get(selectedRow));
      } else {
        JOptionPane.showMessageDialog(
              this,
              "Please select an event to view.",
              "No Selection",
              JOptionPane.INFORMATION_MESSAGE);
      }
    });

    JButton editButton = new JButton("Edit Event");
    editButton.addActionListener(e -> {
      int selectedRow = table.getSelectedRow();
      if (selectedRow >= 0) {
        selectedRow = table.convertRowIndexToModel(selectedRow);
        editEvent(events.get(selectedRow));
      } else {
        JOptionPane.showMessageDialog(
              this,
              "Please select an event to edit.",
              "No Selection",
              JOptionPane.INFORMATION_MESSAGE);
      }
    });

    buttonPanel.add(viewButton);
    buttonPanel.add(editButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    // Create and show dialog
    JDialog dialog = new JDialog(this, "Events - " + viewTitle, true);
    dialog.setContentPane(panel);
    dialog.setSize(800, 400);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }
}