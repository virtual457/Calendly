package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
  private boolean displayEnabled;

  // GUI components
  private JPanel mainPanel;
  private JPanel calendarPanel;
  private JPanel controlPanel;
  private JLabel monthYearLabel;
  private JComboBox<String> calendarSelector;
  private JButton prevButton, nextButton, createEventButton, editEventsButton ,exportButton, importButton;
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
    this.displayEnabled = false;
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
    this.displayEnabled = true;
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

    editEventsButton = new JButton("Edit Events");
    editEventsButton.addActionListener(e -> editEvents());

    exportButton = new JButton("Export Calendar");
    exportButton.addActionListener(e -> exportCalendar());

    importButton = new JButton("Import Calendar");
    importButton.addActionListener(e -> importCalendar());

    sidePanel.add(createEventButton);
    sidePanel.add(Box.createVerticalStrut(10));
    sidePanel.add(editEventsButton);
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

  private void editEvents() {
    // This method is called from the side panel button to edit multiple events
    // Create a panel with search criteria options and event listing
    JPanel mainPanel = new JPanel(new BorderLayout());

    // Create the search criteria panel
    JPanel searchPanel = new JPanel();
    searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

    // Create input fields with labels
    JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel nameLabel = new JLabel("Event Name (required):");
    JTextField nameField = new JTextField(15);
    namePanel.add(nameLabel);
    namePanel.add(nameField);

    JPanel startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
    JTextField startDateField = new JTextField(10);
    startDatePanel.add(startDateLabel);
    startDatePanel.add(startDateField);

    JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
    JTextField endDateField = new JTextField(10);
    endDatePanel.add(endDateLabel);
    endDatePanel.add(endDateField);

    // Add all panels to the search panel
    searchPanel.add(namePanel);
    searchPanel.add(startDatePanel);
    searchPanel.add(endDatePanel);

    // Create non-selectable events list with scrollbar
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> eventList = new JList<>(listModel);
    eventList.setEnabled(false); // Make list non-selectable
    JScrollPane scrollPane = new JScrollPane(eventList);
    scrollPane.setPreferredSize(new Dimension(400, 200));

    // Add label above event list
    JPanel resultsPanel = new JPanel(new BorderLayout());
    JLabel resultsLabel = new JLabel("Matching events that will be edited:");
    resultsPanel.add(resultsLabel, BorderLayout.NORTH);
    resultsPanel.add(scrollPane, BorderLayout.CENTER);

    // Add components to main panel
    mainPanel.add(searchPanel, BorderLayout.NORTH);
    mainPanel.add(resultsPanel, BorderLayout.CENTER);

    // List to store matching events
    List<ICalendarEventDTO> matchingEvents = new ArrayList<>();

    // Create the filter action
    ActionListener filterAction = e -> {
      try {
        // Get filter criteria from input fields
        String eventNameFilter = nameField.getText().trim();
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();

        // Get events from the model
        List<ICalendarEventDTO> allEvents;

        if (!startDateStr.isEmpty() || !endDateStr.isEmpty()) {
          // Use specified date range
          LocalDate startFilterDate = startDateStr.isEmpty() ?
              LocalDate.of(1900, 1, 1) : LocalDate.parse(startDateStr);

          LocalDate endFilterDate = endDateStr.isEmpty() ?
              LocalDate.of(2100, 12, 31) : LocalDate.parse(endDateStr);

          // Swap dates if end is before start
          if (endFilterDate.isBefore(startFilterDate)) {
            LocalDate temp = startFilterDate;
            startFilterDate = endFilterDate;
            endFilterDate = temp;
          }

          // Get events in the specified range directly from model
          allEvents = model.getEventsInRange(
              selectedCalendar,
              startFilterDate.atStartOfDay(),
              endFilterDate.atTime(23, 59, 59)
          );
        } else {
          // If no date range specified, get all events (using a wide range)
          allEvents = model.getEventsInRange(
              selectedCalendar,
              LocalDate.of(1900, 1, 1).atStartOfDay(),
              LocalDate.of(2100, 12, 31).atTime(23, 59, 59)
          );
        }

        // Clear previous results
        matchingEvents.clear();
        listModel.clear();

        // Filter by name if specified
        for (ICalendarEventDTO event : allEvents) {
          boolean nameMatches = eventNameFilter.isEmpty() ||
              event.getEventName().equals(eventNameFilter);

          if (nameMatches) {
            matchingEvents.add(event);
          }
        }

        // Sort events by start date/time
        matchingEvents.sort(Comparator.comparing(ICalendarEventDTO::getStartDateTime));

        // Update the list with matching events
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ICalendarEventDTO event : matchingEvents) {
          listModel.addElement(event.getEventName() + " (" +
              event.getStartDateTime().format(formatter) + " - " +
              event.getEndDateTime().format(formatter) + ")");
        }

        // Update results label
        resultsLabel.setText("Matching events that will be edited: " + matchingEvents.size() + " found");

      } catch (DateTimeException ex) {
        // Don't show error dialog during live filtering - just update the label
        resultsLabel.setText("Invalid date format. Please use YYYY-MM-DD format.");
        matchingEvents.clear();
        listModel.clear();
      } catch (Exception ex) {
        // Don't show error dialog during live filtering - just update the label
        resultsLabel.setText("Error: " + ex.getMessage());
        matchingEvents.clear();
        listModel.clear();
      }
    };

    // Create document listeners for auto-filtering
    DocumentListener documentListener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        filterAction.actionPerformed(null);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        filterAction.actionPerformed(null);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        filterAction.actionPerformed(null);
      }
    };

    // Add document listeners to all input fields
    nameField.getDocument().addDocumentListener(documentListener);
    startDateField.getDocument().addDocumentListener(documentListener);
    endDateField.getDocument().addDocumentListener(documentListener);

    // Initial filter to populate the list
    filterAction.actionPerformed(null);

    // Show the dialog
    int result = JOptionPane.showConfirmDialog(
        this, mainPanel, "Edit Multiple Events", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      // Check if event name is provided
      if (nameField.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Please enter an event name to filter events.",
            "Event Name Required",
            JOptionPane.WARNING_MESSAGE);

        // Call the method again to reopen the dialog
        editEvents();
        return;
      }

      // Check if there are any matching events
      if (matchingEvents.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "No events found matching your criteria.",
            "No Events Found",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      // Create property selection dialog
      JPanel propertyPanel = new JPanel(new BorderLayout());

      // Create dropdown for property selection
      JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      JLabel propertyLabel = new JLabel("Select property to edit:");
      String[] properties = {"Edit Name", "Edit Description", "Edit Location", "Set Private/Public"};
      JComboBox<String> propertyDropdown = new JComboBox<>(properties);

      dropdownPanel.add(propertyLabel);
      dropdownPanel.add(propertyDropdown);

      // Create panel for input field
      JPanel inputPanel = new JPanel(new BorderLayout());
      JLabel inputLabel = new JLabel("New value:");
      JTextField inputField = new JTextField(20);

      // For private/public option, use a dropdown instead of text field
      JComboBox<String> privacyDropdown = new JComboBox<>(new String[]{"Private", "Public"});

      // Show the appropriate input control based on selected property
      propertyDropdown.addActionListener(e -> {
        inputPanel.removeAll();
        String selectedProperty = (String) propertyDropdown.getSelectedItem();

        if ("Set Private/Public".equals(selectedProperty)) {
          inputPanel.add(new JLabel("Set events to:"), BorderLayout.WEST);
          inputPanel.add(privacyDropdown, BorderLayout.CENTER);
        } else {
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
      });

      // Initialize with first option
      if ("Set Private/Public".equals(propertyDropdown.getSelectedItem())) {
        inputPanel.add(new JLabel("Set events to:"), BorderLayout.WEST);
        inputPanel.add(privacyDropdown, BorderLayout.CENTER);
      } else {
        inputPanel.add(inputLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
      }

      // Add components to property panel
      propertyPanel.add(dropdownPanel, BorderLayout.NORTH);
      propertyPanel.add(inputPanel, BorderLayout.CENTER);

      // Add message about number of events
      JLabel eventsLabel = new JLabel("This will update " + matchingEvents.size() + " events");
      eventsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
      propertyPanel.add(eventsLabel, BorderLayout.SOUTH);

      // Show property edit dialog
      int editResult = JOptionPane.showConfirmDialog(
          this, propertyPanel, "Edit Events Property", JOptionPane.OK_CANCEL_OPTION);

      if (editResult == JOptionPane.OK_OPTION) {
        String selectedProperty = (String) propertyDropdown.getSelectedItem();
        String propertyName;
        String newValue;

        // Determine property name and new value based on selection
        switch (selectedProperty) {
          case "Edit Name":
            propertyName = "name";
            newValue = inputField.getText().trim();

            // Validate name is not empty
            if (newValue.isEmpty()) {
              JOptionPane.showMessageDialog(this,
                  "Event name cannot be empty.",
                  "Invalid Input",
                  JOptionPane.WARNING_MESSAGE);
              return;
            }
            break;
          case "Edit Description":
            propertyName = "description";
            newValue = inputField.getText();
            break;
          case "Edit Location":
            propertyName = "location";
            newValue = inputField.getText();
            break;
          case "Set Private/Public":
            propertyName = "isprivate";
            // Convert dropdown selection to boolean string
            newValue = "Private".equals(privacyDropdown.getSelectedItem()) ? "true" : "false";
            break;
          default:
            JOptionPane.showMessageDialog(this,
                "Invalid property selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm before proceeding with many events - ONLY ONCE
        if (matchingEvents.size() > 10) {
          int confirmResult = JOptionPane.showConfirmDialog(this,
              "You are about to edit " + matchingEvents.size() + " events. Continue?",
              "Confirm Batch Edit",
              JOptionPane.YES_NO_OPTION);

          if (confirmResult != JOptionPane.YES_OPTION) {
            return;
          }
        }

        // Get the user-provided start date/time
        // Get the user-provided start date/time
        String userStartDateStr = startDateField.getText().trim();

// Format the value parameter
        String valueParam = newValue.isEmpty() ? "null" : newValue;

// Create the edit events command using the correct format
        String command;

        if (userStartDateStr.isEmpty()) {
          // If no from date is provided, use the simpler command format
          command = "edit events " + propertyName + " \"" + nameField.getText().trim() + "\" " + valueParam;
        } else {
          // If from date is provided, use the full command format
          LocalDate startDate = LocalDate.parse(userStartDateStr);
          LocalDateTime startDateTime = startDate.atStartOfDay();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
          String fromDateTimeStr = startDateTime.format(formatter);

          command = "edit events " + propertyName + " \"" + nameField.getText().trim() + "\"" +
              " from " + fromDateTimeStr +
              " with " + valueParam;
        }

        try {
          // Execute the single command for all matching events
          controller.executeCommand(command);

          // Refresh view
          refreshCalendarView();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this,
              "Error updating events: " + ex.getMessage(),
              "Update Error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }



  private void editEvent(ICalendarEventDTO event) {
    // Create main panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());

    // Create panel for event details
    JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    JLabel eventDetailsLabel = new JLabel("Editing: " + event.getEventName() + " (" +
        event.getStartDateTime().format(formatter) + " - " +
        event.getEndDateTime().format(formatter) + ")");
    detailsPanel.add(eventDetailsLabel);
    mainPanel.add(detailsPanel, BorderLayout.NORTH);

    // Create property selection panel
    JPanel propertyPanel = new JPanel(new BorderLayout());

    // Create dropdown for property selection
    JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel propertyLabel = new JLabel("Select property to edit:");
    String[] properties = {"Edit Name", "Edit Description", "Edit Location", "Edit Start Time", "Edit End Time", "Set Private/Public"};
    JComboBox<String> propertyDropdown = new JComboBox<>(properties);

    dropdownPanel.add(propertyLabel);
    dropdownPanel.add(propertyDropdown);
    propertyPanel.add(dropdownPanel, BorderLayout.NORTH);

    // Create panel for input field
    JPanel inputPanel = new JPanel(new BorderLayout());
    JLabel inputLabel = new JLabel("New value:");
    JTextField inputField = new JTextField(20);

    // For private/public option, use a dropdown
    JComboBox<String> privacyDropdown = new JComboBox<>(new String[]{"Private", "Public"});

    // For time fields, show a formatted example
    JLabel helpLabel = new JLabel("");
    helpLabel.setFont(helpLabel.getFont().deriveFont(Font.ITALIC, 10f));

    // Show the appropriate input field based on selected property
    propertyDropdown.addActionListener(e -> {
      inputPanel.removeAll();
      String selectedProperty = (String) propertyDropdown.getSelectedItem();

      // Pre-fill field with current value
      switch (selectedProperty) {
        case "Edit Name":
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
          inputField.setText(event.getEventName());
          helpLabel.setText("");
          inputPanel.add(helpLabel, BorderLayout.SOUTH);
          break;
        case "Edit Description":
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
          inputField.setText(event.getEventDescription() != null ? event.getEventDescription() : "");
          helpLabel.setText("");
          inputPanel.add(helpLabel, BorderLayout.SOUTH);
          break;
        case "Edit Location":
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
          inputField.setText(event.getEventLocation() != null ? event.getEventLocation() : "");
          helpLabel.setText("");
          inputPanel.add(helpLabel, BorderLayout.SOUTH);
          break;
        case "Edit Start Time":
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
          DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
          inputField.setText(event.getStartDateTime().format(timeFormatter));
          helpLabel.setText("Format: HH:MM (24-hour)");
          inputPanel.add(helpLabel, BorderLayout.SOUTH);
          break;
        case "Edit End Time":
          inputPanel.add(inputLabel, BorderLayout.WEST);
          inputPanel.add(inputField, BorderLayout.CENTER);
          DateTimeFormatter endTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
          inputField.setText(event.getEndDateTime().format(endTimeFormatter));
          helpLabel.setText("Format: HH:MM (24-hour)");
          inputPanel.add(helpLabel, BorderLayout.SOUTH);
          break;
        case "Set Private/Public":
          inputPanel.add(new JLabel("Set event to:"), BorderLayout.WEST);
          inputPanel.add(privacyDropdown, BorderLayout.CENTER);
          boolean isPrivate = Boolean.TRUE.equals(event.isPrivate());
          privacyDropdown.setSelectedItem(isPrivate ? "Private" : "Public");
          break;
      }

      inputPanel.revalidate();
      inputPanel.repaint();
    });

    // Initialize with first property
    propertyDropdown.setSelectedIndex(0);

    // Add input panel to property panel
    propertyPanel.add(inputPanel, BorderLayout.CENTER);

    // Add property panel to main panel
    mainPanel.add(propertyPanel, BorderLayout.CENTER);

    // For recurring events, add checkbox at the bottom
    if (Boolean.TRUE.equals(event.isRecurring())) {
      JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      JCheckBox editAllInstancesCheck = new JCheckBox("Edit all instances of recurring event");
      editAllInstancesCheck.setEnabled(true);
      checkboxPanel.add(editAllInstancesCheck);
      mainPanel.add(checkboxPanel, BorderLayout.SOUTH);
    }

    // Show the dialog
    int result = JOptionPane.showConfirmDialog(
        this, mainPanel, "Edit Event", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String selectedProperty = (String) propertyDropdown.getSelectedItem();
        String propertyName;
        String newValue;
        boolean editAll = Boolean.TRUE.equals(event.isRecurring()) &&
            ((JCheckBox)((JPanel)mainPanel.getComponent(2)).getComponent(0)).isSelected();

        // Validate and process input based on selected property
        switch (selectedProperty) {
          case "Edit Name":
            propertyName = "name";
            newValue = inputField.getText().trim();
            if (newValue.isEmpty()) {
              throw new IllegalArgumentException("Event name cannot be empty");
            }
            break;
          case "Edit Description":
            propertyName = "description";
            newValue = inputField.getText().trim();
            break;
          case "Edit Location":
            propertyName = "location";
            newValue = inputField.getText().trim();
            break;
          case "Edit Start Time":
            propertyName = "start";
            // Validate time format and logic
            try {
              LocalTime newStartTime = LocalTime.parse(inputField.getText().trim());
              LocalDateTime newStartDateTime = LocalDateTime.of(
                  event.getStartDateTime().toLocalDate(), newStartTime);
              if (newStartDateTime.isAfter(event.getEndDateTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
              }
              // Format for command
              DateTimeFormatter cmdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
              newValue = newStartDateTime.format(cmdFormatter);
            } catch (DateTimeParseException ex) {
              throw new IllegalArgumentException("Invalid time format. Use HH:MM");
            }
            break;
          case "Edit End Time":
            propertyName = "end";
            // Validate time format and logic
            try {
              LocalTime newEndTime = LocalTime.parse(inputField.getText().trim());
              LocalDateTime newEndDateTime = LocalDateTime.of(
                  event.getEndDateTime().toLocalDate(), newEndTime);
              if (newEndDateTime.isBefore(event.getStartDateTime())) {
                throw new IllegalArgumentException("End time must be after start time");
              }
              // Format for command
              DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
              newValue = newEndDateTime.format(endFormatter);
            } catch (DateTimeParseException ex) {
              throw new IllegalArgumentException("Invalid time format. Use HH:MM");
            }
            break;
          case "Set Private/Public":
            propertyName = "isprivate";
            newValue = "Private".equals(privacyDropdown.getSelectedItem()) ? "true" : "false";
            break;
          default:
            throw new IllegalArgumentException("Invalid property selected");
        }

        // Build command: edit event property eventName from startDateTime to endDateTime with newValue
        DateTimeFormatter cmdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String command = "edit event " + propertyName + " " + event.getEventName() +
            " from " + event.getStartDateTime().format(cmdFormatter) +
            " to " + event.getEndDateTime().format(cmdFormatter) +
            " with " + newValue;

        // Add flag for all instances if applicable
        if (editAll) {
          command += " --all";
        }

        // Execute the command
        controller.executeCommand(command);
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
        // Execute import command with the selected calendar and file path
        String command = "import cal --calendar \"" + selectedCalendar + "\" --file \"" +
            file.getAbsolutePath().replace("\\", "\\\\") + "\"";

        controller.executeCommand(command);

        JOptionPane.showMessageDialog(this,
            "Calendar imported successfully",
            "Import Success",
            JOptionPane.INFORMATION_MESSAGE);
        refreshCalendarView();
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
    if(displayEnabled) {
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
    }
    System.out.println(message);
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    this.controller = commandExecutor;
    initializeUI();
  }


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

}