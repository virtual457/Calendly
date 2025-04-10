package view.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import model.ICalendarEventDTO;
import model.ICalendarEventDTOBuilder;

/**
 * Dialog for creating or editing calendar events.
 */
public class EventCreationDialog extends JDialog {
  private LocalDate initialDate;
  private ICalendarEventDTO result;

  // Map to store original values before editing
  private Map<JTextField, String> originalValues = new HashMap<>();

  // Form fields
  private JTextField nameField;
  private JTextField descriptionField;
  private JTextField locationField;
  private JCheckBox isPrivate;
  private JCheckBox isAllDayEvent;

  private JTextField startDateField;
  private JTextField endDateField;
  private JTextField startTimeField;
  private JTextField endTimeField;

  private JCheckBox recurringCheck;
  private JComboBox<String> recurrenceType;
  private JCheckBox[] dayCheckboxes;

  private JRadioButton occurrencesRadio;
  private JRadioButton endDateRadio;
  private JTextField occurrencesField;
  private JTextField recurrenceEndDateField;

  // Callback for all day event changes
  private Consumer<Boolean> allDayEventChangeListener;

  /**
   * Creates a new event creation dialog.
   *
   * @param parent The parent frame
   * @param initialDate The initial date for the event
   */
  public EventCreationDialog(Frame parent, LocalDate initialDate) {
    super(parent, "Create Event", true);
    this.initialDate = initialDate;
    initializeUI();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initializeUI() {
    // Main panel with padding
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Basic info panel
    JPanel basicInfoPanel = createBasicInfoPanel();

    // Date and time panel
    JPanel dateTimePanel = createDateTimePanel();

    // Recurrence panel
    JPanel recurrencePanel = createRecurrencePanel();

    // Add panels to main panel
    mainPanel.add(basicInfoPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(dateTimePanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(recurrencePanel);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancelButton = new JButton("Cancel");
    JButton saveButton = new JButton("Save");

    cancelButton.addActionListener(e -> dispose());
    saveButton.addActionListener(e -> saveEvent());

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    // Add to dialog
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    // Set size
    setPreferredSize(new Dimension(500, 600));
  }

  private JPanel createBasicInfoPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Event Details"));

    // Event name
    panel.add(new JLabel("Event Name:"));
    nameField = new JTextField();
    storeOriginalValue(nameField);

    // Add validation for name field - cannot be empty
    nameField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        if (nameField.getText().trim().isEmpty()) {
          JOptionPane.showMessageDialog(
                EventCreationDialog.this,
                "Event name cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
          );
          resetToOriginalValue(nameField);
          nameField.requestFocus();
        } else {
          // Valid value, update the original
          updateOriginalValue(nameField);
        }
      }
    });
    panel.add(nameField);

    // Description
    panel.add(new JLabel("Description:"));
    descriptionField = new JTextField();
    storeOriginalValue(descriptionField);
    panel.add(descriptionField);

    // Location
    panel.add(new JLabel("Location:"));
    locationField = new JTextField();
    storeOriginalValue(locationField);
    panel.add(locationField);

    // Private event
    panel.add(new JLabel("Private Event:"));
    isPrivate = new JCheckBox();
    panel.add(isPrivate);

    return panel;
  }

  private JPanel createDateTimePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Date & Time"));

    // All Day Event checkbox
    JPanel allDayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    isAllDayEvent = new JCheckBox("All Day Event");
    isAllDayEvent.addActionListener(e -> {
      boolean isAllDay = isAllDayEvent.isSelected();
      // Enable/disable time fields based on selection
      startTimeField.setEnabled(!isAllDay);
      endTimeField.setEnabled(!isAllDay);
      endDateField.setEnabled(!isAllDay);

      // Set default times for all day events
      if (isAllDay) {
        storeOriginalValue(startTimeField);
        storeOriginalValue(endTimeField);
        storeOriginalValue(endDateField);
        startTimeField.setText("00:00");
        endTimeField.setText("23:59");
        endDateField.setText(startDateField.getText());
        updateOriginalValue(startTimeField);
        updateOriginalValue(endTimeField);
        updateOriginalValue(endDateField);
        endDateField.setEnabled(false);
      } else {
        storeOriginalValue(startTimeField);
        storeOriginalValue(endTimeField);
        startTimeField.setText("09:00");
        endTimeField.setText("10:00");
        updateOriginalValue(startTimeField);
        updateOriginalValue(endTimeField);
      }

      // Notify listener if set
      if (allDayEventChangeListener != null) {
        allDayEventChangeListener.accept(isAllDay);
      }
    });
    allDayPanel.add(isAllDayEvent);
    panel.add(allDayPanel);

    // Date and time fields
    JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5));

    // Start date
    fieldsPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    startDateField = new JTextField(initialDate.toString());
    storeOriginalValue(startDateField);

    // Add focus listeners to track when editing starts
    startDateField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(startDateField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (!validateDateField(startDateField, "start date")) {
          resetToOriginalValue(startDateField);
        } else {
          // Valid value, update the original
          if (isAllDayEvent.isSelected()) {
            endDateField.setText(startDateField.getText());
            updateOriginalValue(endDateField);
          }
          updateOriginalValue(startDateField);
          updateOriginalValue(endDateField);
        }
      }
    });
    fieldsPanel.add(startDateField);

    // End date
    fieldsPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
    endDateField = new JTextField(initialDate.toString());
    storeOriginalValue(endDateField);

    // Add validation for end date
    endDateField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(endDateField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (!validateDateField(endDateField, "end date")) {
          resetToOriginalValue(endDateField);
          return;
        }

        // Validate end date is not before start date
        try {
          LocalDate startDate = LocalDate.parse(startDateField.getText());
          LocalDate endDate = LocalDate.parse(endDateField.getText());

          if (endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(
                  EventCreationDialog.this,
                  "End date cannot be before start date",
                  "Validation Error",
                  JOptionPane.ERROR_MESSAGE
            );
            resetToOriginalValue(endDateField);
            endDateField.requestFocus();
          } else {
            // Valid value, update the original
            updateOriginalValue(endDateField);
          }
        } catch (DateTimeParseException ex) {
          // Skip this validation if either date is invalid
          // (already handled by validateDateField)
        }
      }
    });
    fieldsPanel.add(endDateField);

    // Start time
    fieldsPanel.add(new JLabel("Start Time (HH:MM):"));
    startTimeField = new JTextField("09:00");
    storeOriginalValue(startTimeField);

    // Add validation for start time
    startTimeField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(startTimeField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (!validateTimeField(startTimeField, "start time")) {
          resetToOriginalValue(startTimeField);
        } else {
          // Valid value, update the original
          updateOriginalValue(startTimeField);
        }
      }
    });
    fieldsPanel.add(startTimeField);

    // End time
    fieldsPanel.add(new JLabel("End Time (HH:MM):"));
    endTimeField = new JTextField("10:00");
    storeOriginalValue(endTimeField);

    // Add validation for end time
    endTimeField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(endTimeField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (!validateTimeField(endTimeField, "end time")) {
          resetToOriginalValue(endTimeField);
          return;
        }

        // Only validate time relationship if they're on the same day
        try {
          if (startDateField.getText().equals(endDateField.getText())) {
            LocalTime startTime = LocalTime.parse(startTimeField.getText());
            LocalTime endTime = LocalTime.parse(endTimeField.getText());

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
              JOptionPane.showMessageDialog(
                    EventCreationDialog.this,
                    "End time must be after start time when on the same day",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
              );
              resetToOriginalValue(endTimeField);
              endTimeField.requestFocus();
            } else {
              // Valid value, update the original
              updateOriginalValue(endTimeField);
            }
          } else {
            // Different days, so time order doesn't matter
            updateOriginalValue(endTimeField);
          }
        } catch (DateTimeParseException ex) {
          // Skip this validation if either time is invalid
          // (already handled by validateTimeField)
        }
      }
    });
    fieldsPanel.add(endTimeField);

    panel.add(fieldsPanel);

    return panel;
  }

  private JPanel createRecurrencePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Recurrence"));

    // Recurring checkbox
    JPanel recurringPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    recurringCheck = new JCheckBox("Recurring Event");
    recurringPanel.add(recurringCheck);
    panel.add(recurringPanel);

    // Recurrence type
    JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    typePanel.add(new JLabel("Repeat Pattern:"));
    String[] recurrenceOptions = {"Select...", "Daily", "Weekdays", "Weekends", "Weekly", "Custom"};
    recurrenceType = new JComboBox<>(recurrenceOptions);
    recurrenceType.setEnabled(false);
    typePanel.add(recurrenceType);
    panel.add(typePanel);

    // Days panel
    JPanel daysPanel = new JPanel(new GridLayout(1, 7));
    daysPanel.setBorder(BorderFactory.createTitledBorder("Repeat On:"));
    dayCheckboxes = new JCheckBox[7];
    String[] dayLabels = {"M", "T", "W", "Th", "F", "S", "Su"};

    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = new JCheckBox(dayLabels[i]);
      dayCheckboxes[i].setEnabled(false);
      daysPanel.add(dayCheckboxes[i]);
    }
    panel.add(daysPanel);

    // Recurrence termination panel
    JPanel terminationPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    terminationPanel.setBorder(BorderFactory.createTitledBorder("End Recurrence:"));

    occurrencesRadio = new JRadioButton("Number of occurrences:");
    endDateRadio = new JRadioButton("End date:");
    ButtonGroup terminationGroup = new ButtonGroup();
    terminationGroup.add(occurrencesRadio);
    terminationGroup.add(endDateRadio);

    occurrencesField = new JTextField("10");
    storeOriginalValue(occurrencesField);

    // Add validation for occurrences field
    occurrencesField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(occurrencesField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (occurrencesRadio.isSelected() && recurringCheck.isSelected()) {
          try {
            int occurrences = Integer.parseInt(occurrencesField.getText().trim());
            if (occurrences <= 0) {
              JOptionPane.showMessageDialog(
                    EventCreationDialog.this,
                    "Number of occurrences must be greater than zero",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
              );
              resetToOriginalValue(occurrencesField);
              occurrencesField.requestFocus();
            } else {
              // Valid value, update the original
              updateOriginalValue(occurrencesField);
            }
          } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                  EventCreationDialog.this,
                  "Please enter a valid number for occurrences",
                  "Validation Error",
                  JOptionPane.ERROR_MESSAGE
            );
            resetToOriginalValue(occurrencesField);
            occurrencesField.requestFocus();
          }
        }
      }
    });

    recurrenceEndDateField = new JTextField(initialDate.plusMonths(1).toString());
    storeOriginalValue(recurrenceEndDateField);

    // Add validation for recurrence end date
    recurrenceEndDateField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        storeOriginalValue(recurrenceEndDateField);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (endDateRadio.isSelected() && recurringCheck.isSelected()) {
          if (!validateDateField(recurrenceEndDateField, "recurrence end date")) {
            resetToOriginalValue(recurrenceEndDateField);
            return;
          }

          try {
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate recurrenceEnd = LocalDate.parse(recurrenceEndDateField.getText());

            if (recurrenceEnd.isBefore(startDate)) {
              JOptionPane.showMessageDialog(
                    EventCreationDialog.this,
                    "Recurrence end date must be after the event start date",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
              );
              resetToOriginalValue(recurrenceEndDateField);
              recurrenceEndDateField.requestFocus();
            } else {
              // Valid value, update the original
              updateOriginalValue(recurrenceEndDateField);
            }
          } catch (DateTimeParseException ex) {
            // Skip this validation if either date is invalid
            // (already handled by validateDateField)
          }
        }
      }
    });

    occurrencesRadio.setEnabled(false);
    endDateRadio.setEnabled(false);
    occurrencesField.setEnabled(false);
    recurrenceEndDateField.setEnabled(false);

    terminationPanel.add(occurrencesRadio);
    terminationPanel.add(occurrencesField);
    terminationPanel.add(endDateRadio);
    terminationPanel.add(recurrenceEndDateField);
    panel.add(terminationPanel);

    // Set up event handlers

    // Enable/disable recurrence controls based on recurring checkbox
    recurringCheck.addActionListener(e -> {
      boolean isRecurring = recurringCheck.isSelected();
      recurrenceType.setEnabled(isRecurring);
      occurrencesRadio.setEnabled(isRecurring);
      endDateRadio.setEnabled(isRecurring);

      // Reset selection if unchecked
      if (!isRecurring) {
        recurrenceType.setSelectedIndex(0);
        for (JCheckBox dayBox : dayCheckboxes) {
          dayBox.setSelected(false);
          dayBox.setEnabled(false);
        }
        terminationGroup.clearSelection();
        occurrencesField.setEnabled(false);
        recurrenceEndDateField.setEnabled(false);
      } else if (!occurrencesRadio.isSelected() && !endDateRadio.isSelected()) {
        // Default to occurrences if nothing selected
        occurrencesRadio.setSelected(true);
        occurrencesField.setEnabled(true);
      }
    });

    // Configure day checkboxes based on recurrence type
    recurrenceType.addActionListener(e -> {
      String selected = (String) recurrenceType.getSelectedItem();

      // Reset checkboxes
      for (JCheckBox dayBox : dayCheckboxes) {
        dayBox.setSelected(false);
      }

      // Enable/disable checkboxes and set selections
      boolean enableCustom = "Custom".equals(selected);
      for (JCheckBox dayBox : dayCheckboxes) {
        dayBox.setEnabled(enableCustom);
      }

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
          // Select current day of week
          int dayIndex = initialDate.getDayOfWeek().getValue() - 1;
          if (dayIndex >= 0 && dayIndex < 7) {
            dayCheckboxes[dayIndex].setSelected(true);
          }
          break;
      }
    });

    // Termination type radio buttons
    occurrencesRadio.addActionListener(e -> {
      occurrencesField.setEnabled(true);
      recurrenceEndDateField.setEnabled(false);
    });

    endDateRadio.addActionListener(e -> {
      occurrencesField.setEnabled(false);
      recurrenceEndDateField.setEnabled(true);
    });

    return panel;
  }

  /**
   * Stores the original value of a text field before editing.
   *
   * @param field The text field to store the value for
   */
  private void storeOriginalValue(JTextField field) {
    originalValues.put(field, field.getText());
  }

  /**
   * Updates the stored original value after successful validation.
   *
   * @param field The text field to update the original value for
   */
  private void updateOriginalValue(JTextField field) {
    originalValues.put(field, field.getText());
  }

  /**
   * Resets a text field to its original value before editing.
   *
   * @param field The text field to reset
   */
  private void resetToOriginalValue(JTextField field) {
    String originalValue = originalValues.get(field);
    if (originalValue != null) {
      field.setText(originalValue);
    }
  }

  /**
   * Validates a date field to ensure it contains a valid date.
   *
   * @param field The date field to validate
   * @param fieldName The name of the field for the error message
   * @return true if validation passed, false otherwise
   */
  private boolean validateDateField(JTextField field, String fieldName) {
    try {
      String dateText = field.getText().trim();
      if (dateText.isEmpty()) {
        JOptionPane.showMessageDialog(
              this,
              "Please enter a " + fieldName,
              "Validation Error",
              JOptionPane.ERROR_MESSAGE
        );
        return false;
      }

      // Try to parse the date
      LocalDate.parse(dateText);
      return true;
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(
            this,
            "Invalid " + fieldName + " format. Please use YYYY-MM-DD format (e.g., 2025-04-10)",
            "Validation Error",
            JOptionPane.ERROR_MESSAGE
      );
      return false;
    }
  }

  /**
   * Validates a time field to ensure it contains a valid time.
   *
   * @param field The time field to validate
   * @param fieldName The name of the field for the error message
   * @return true if validation passed, false otherwise
   */
  private boolean validateTimeField(JTextField field, String fieldName) {
    if (isAllDayEvent.isSelected()) {
      // Skip validation for time fields if it's an all-day event
      return true;
    }

    try {
      String timeText = field.getText().trim();
      if (timeText.isEmpty()) {
        JOptionPane.showMessageDialog(
              this,
              "Please enter a " + fieldName,
              "Validation Error",
              JOptionPane.ERROR_MESSAGE
        );
        return false;
      }

      // Try to parse the time
      LocalTime.parse(timeText);
      return true;
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(
            this,
            "Invalid " + fieldName + " format. Please use HH:MM format (e.g., 14:30)",
            "Validation Error",
            JOptionPane.ERROR_MESSAGE
      );
      return false;
    }
  }

  /**
   * Saves the event by creating an event DTO from the form data.
   */
  private void saveEvent() {
    try {
      // Validate required fields
      if (nameField.getText().trim().isEmpty()) {
        throw new IllegalArgumentException("Event name is required");
      }

      // Parse dates and times
      LocalDate startDate = LocalDate.parse(startDateField.getText());
      LocalDate endDate = LocalDate.parse(endDateField.getText());
      LocalTime startTime = LocalTime.parse(startTimeField.getText());
      LocalTime endTime = LocalTime.parse(endTimeField.getText());

      // Special handling for all-day events
      if (isAllDayEvent.isSelected()) {
        startTime = LocalTime.MIDNIGHT; // 00:00
        endTime = LocalTime.of(23, 59, 59); // 23:59:59
      }

      // Build full date-times
      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time must be after start date/time");
      }

      // Create event builder
      ICalendarEventDTOBuilder<?> builder = ICalendarEventDTO.builder()
            .setEventName(nameField.getText())
            .setEventDescription(descriptionField.getText())
            .setEventLocation(locationField.getText())
            .setPrivate(isPrivate.isSelected())
            .setStartDateTime(startDateTime)
            .setEndDateTime(endDateTime)
            .setAutoDecline(true);

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

        // Add either count or end date
        if (occurrencesRadio.isSelected() && !occurrencesField.getText().isEmpty()) {
          int occurrences = Integer.parseInt(occurrencesField.getText());
          if (occurrences <= 0) {
            throw new IllegalArgumentException("Number of occurrences must be greater than zero");
          }
          builder.setRecurrenceCount(occurrences);
        } else if (endDateRadio.isSelected() && !recurrenceEndDateField.getText().isEmpty()) {
          LocalDate recurrenceEndDate = LocalDate.parse(recurrenceEndDateField.getText());
          if (recurrenceEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Recurrence end date must be after the event start date");
          }
          builder.setRecurrenceEndDate(recurrenceEndDate.atTime(23, 59, 59));
        } else {
          throw new IllegalArgumentException("Please specify either occurrence count or end date for recurring events");
        }
      } else {
        builder.setRecurring(false);
      }

      // Build the event
      result = builder.build();
      dispose();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
            this,
            "Error creating event: " + ex.getMessage(),
            "Input Error",
            JOptionPane.ERROR_MESSAGE
      );
    }
  }

  /**
   * Maps index to DayOfWeek.
   */
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

  /**
   * Gets the created event DTO.
   *
   * @return The event DTO or null if cancelled
   */
  public ICalendarEventDTO getResult() {
    return result;
  }

  /**
   * Sets a listener to be notified when the all-day event checkbox is toggled.
   *
   * @param listener The listener to call when the checkbox changes
   */
  public void setAllDayEventChangeListener(Consumer<Boolean> listener) {
    this.allDayEventChangeListener = listener;
  }
}