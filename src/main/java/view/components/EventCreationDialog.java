package view.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

  // Form fields
  private JTextField nameField;
  private JTextField descriptionField;
  private JTextField locationField;
  private JCheckBox isPrivate;

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
    panel.add(nameField);

    // Description
    panel.add(new JLabel("Description:"));
    descriptionField = new JTextField();
    panel.add(descriptionField);

    // Location
    panel.add(new JLabel("Location:"));
    locationField = new JTextField();
    panel.add(locationField);

    // Private event
    panel.add(new JLabel("Private Event:"));
    isPrivate = new JCheckBox();
    panel.add(isPrivate);

    return panel;
  }

  private JPanel createDateTimePanel() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Date & Time"));

    // Start date
    panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    startDateField = new JTextField(initialDate.toString());
    panel.add(startDateField);

    // End date
    panel.add(new JLabel("End Date (YYYY-MM-DD):"));
    endDateField = new JTextField(initialDate.toString());
    panel.add(endDateField);

    // Start time
    panel.add(new JLabel("Start Time (HH:MM):"));
    startTimeField = new JTextField("09:00");
    panel.add(startTimeField);

    // End time
    panel.add(new JLabel("End Time (HH:MM):"));
    endTimeField = new JTextField("10:00");
    panel.add(endTimeField);

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
    recurrenceEndDateField = new JTextField(initialDate.plusMonths(1).toString());

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
   * Saves the event by creating an event DTO from the form data.
   */
  private void saveEvent() {
    try {
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
          builder.setRecurrenceCount(Integer.parseInt(occurrencesField.getText()));
        } else if (endDateRadio.isSelected() && !recurrenceEndDateField.getText().isEmpty()) {
          LocalDate recurrenceEndDate = LocalDate.parse(recurrenceEndDateField.getText());
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
}