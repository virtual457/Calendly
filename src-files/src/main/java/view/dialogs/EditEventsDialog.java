package view.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.ICalendarCommandAdapter;
import model.ICalendarEventDTO;
import model.IReadOnlyCalendarModel;

/**
 * Dialog for editing multiple events at once.
 */
public class EditEventsDialog extends JDialog {
  private final IReadOnlyCalendarModel model;
  private final String calendarName;
  private final ICalendarCommandAdapter commandAdapter;
  private boolean edited = false;

  // UI Components
  private JTextField nameField;
  private JTextField startDateField;
  private JTextField endDateField;
  private DefaultListModel<String> listModel;
  private JLabel resultsLabel;

  // Selected events
  private List<ICalendarEventDTO> matchingEvents = new ArrayList<>();

  /**
   * Creates a new dialog for editing multiple events.
   */
  public EditEventsDialog(JFrame parent, IReadOnlyCalendarModel model,
                          String calendarName, ICalendarCommandAdapter commandAdapter) {
    super(parent, "Edit Multiple Events", true);
    this.model = model;
    this.calendarName = calendarName;
    this.commandAdapter = commandAdapter;

    initializeUI();
    setSize(500, 400);
    setLocationRelativeTo(parent);
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Main panel
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Create search criteria panel
    JPanel searchPanel = new JPanel();
    searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
    searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));

    // Name field
    JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel nameLabel = new JLabel("Event Name (required):");
    nameField = new JTextField(15);
    namePanel.add(nameLabel);
    namePanel.add(nameField);

    // Start date field
    JPanel startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
    startDateField = new JTextField(10);
    startDatePanel.add(startDateLabel);
    startDatePanel.add(startDateField);

    // End date field
    JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
    endDateField = new JTextField(10);
    endDatePanel.add(endDateLabel);
    endDatePanel.add(endDateField);

    // Add fields to search panel
    searchPanel.add(namePanel);
    searchPanel.add(startDatePanel);
    searchPanel.add(endDatePanel);

    // Create results list
    listModel = new DefaultListModel<>();
    JList<String> eventList = new JList<>(listModel);
    eventList.setEnabled(false); // Non-selectable
    JScrollPane scrollPane = new JScrollPane(eventList);
    scrollPane.setPreferredSize(new Dimension(400, 200));

    // Results panel
    JPanel resultsPanel = new JPanel(new BorderLayout());
    resultsPanel.setBorder(BorderFactory.createTitledBorder("Events to Edit"));
    resultsLabel = new JLabel("Matching events that will be edited:");
    resultsPanel.add(resultsLabel, BorderLayout.NORTH);
    resultsPanel.add(scrollPane, BorderLayout.CENTER);

    // Add panels to main panel
    mainPanel.add(searchPanel, BorderLayout.NORTH);
    mainPanel.add(resultsPanel, BorderLayout.CENTER);

    // Create document listeners for auto-filtering
    DocumentListener documentListener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        filterEvents();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        filterEvents();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        filterEvents();
      }
    };

    // Add listeners to fields
    nameField.getDocument().addDocumentListener(documentListener);
    startDateField.getDocument().addDocumentListener(documentListener);
    endDateField.getDocument().addDocumentListener(documentListener);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton editButton = new JButton("Edit Selected Events");
    JButton cancelButton = new JButton("Cancel");

    editButton.addActionListener(e -> editSelectedEvents());
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(editButton);
    buttonPanel.add(cancelButton);

    // Add panels to dialog
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    // Initial filter
    filterEvents();
  }

  /**
   * Filters events based on criteria.
   */
  private void filterEvents() {
    try {
      // Get filter criteria
      String eventNameFilter = nameField.getText().trim();
      String startDateStr = startDateField.getText().trim();
      String endDateStr = endDateField.getText().trim();

      // Get events from model
      List<ICalendarEventDTO> allEvents;

      if (!startDateStr.isEmpty() || !endDateStr.isEmpty()) {
        // Use date range if specified
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

        // Get events in range
        allEvents = model.getEventsInRange(
              calendarName,
              startFilterDate.atStartOfDay(),
              endFilterDate.atTime(23, 59, 59)
        );
      } else {
        // Get all events if no date range
        allEvents = model.getEventsInRange(
              calendarName,
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

      // Update list model
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      for (ICalendarEventDTO event : matchingEvents) {
        listModel.addElement(event.getEventName() + " (" +
              event.getStartDateTime().format(formatter) + " - " +
              event.getEndDateTime().format(formatter) + ")");
      }

      // Update results label
      resultsLabel.setText("Matching events that will be edited: " + matchingEvents.size() + " found");

    } catch (DateTimeParseException ex) {
      // Update label with error
      resultsLabel.setText("Invalid date format. Please use YYYY-MM-DD format.");
      matchingEvents.clear();
      listModel.clear();
    } catch (Exception ex) {
      // Update label with error
      resultsLabel.setText("Error: " + ex.getMessage());
      matchingEvents.clear();
      listModel.clear();
    }
  }

  /**
   * Shows property selection dialog and edits selected events.
   */
  private void editSelectedEvents() {
    // Check if name is provided
    if (nameField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
            "Please enter an event name to filter events.",
            "Event Name Required",
            JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Check if there are matching events
    if (matchingEvents.isEmpty()) {
      JOptionPane.showMessageDialog(this,
            "No events found matching your criteria.",
            "No Events Found",
            JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // Create property selection dialog
    JPanel propertyPanel = new JPanel(new BorderLayout());

    // Property selection dropdown
    JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel propertyLabel = new JLabel("Select property to edit:");
    String[] properties = {"Edit Name", "Edit Description", "Edit Location", "Edit Start DateTime",
          "Edit End DateTime", "Set Private/Public"};
    JComboBox<String> propertyDropdown = new JComboBox<>(properties);

    dropdownPanel.add(propertyLabel);
    dropdownPanel.add(propertyDropdown);

    // Input panel
    JPanel inputPanel = new JPanel(new BorderLayout());
    JLabel inputLabel = new JLabel("New value:");
    JTextField inputField = new JTextField(20);

    // Privacy dropdown
    JComboBox<String> privacyDropdown = new JComboBox<>(new String[]{"Private", "Public"});

    // Property change handler

    propertyDropdown.addActionListener(e -> {
      inputPanel.removeAll();
      String selectedProperty = (String) propertyDropdown.getSelectedItem();

      if ("Set Private/Public".equals(selectedProperty)) {
        inputPanel.add(new JLabel("Set events to:"), BorderLayout.WEST);
        inputPanel.add(privacyDropdown, BorderLayout.CENTER);
      }
      else if ("Edit Start DateTime".equals(selectedProperty)) {
        inputPanel.add(new JLabel("New start time (HH:MM):"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Set example format
        JLabel formatLabel = new JLabel("Enter time only in 24-hour format (e.g., 09:30)");
        formatLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        inputPanel.add(formatLabel, BorderLayout.SOUTH);

        // Set default value to current time
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        inputField.setText(now.format(timeFormatter));
      }
      else if ("Edit End DateTime".equals(selectedProperty)) {
        inputPanel.add(new JLabel("New end time (HH:MM):"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Set example format
        JLabel formatLabel = new JLabel("Enter time only in 24-hour format (e.g., 17:30)");
        formatLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        inputPanel.add(formatLabel, BorderLayout.SOUTH);

        // Set default value to current time
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        inputField.setText(now.format(timeFormatter));
      }
      else {
        inputPanel.add(new JLabel("New value:"), BorderLayout.WEST);
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

    // Add message about number of events
    JLabel eventsLabel = new JLabel("This will update " + matchingEvents.size() + " events");
    eventsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    // Add components to panel
    propertyPanel.add(dropdownPanel, BorderLayout.NORTH);
    propertyPanel.add(inputPanel, BorderLayout.CENTER);
    propertyPanel.add(eventsLabel, BorderLayout.SOUTH);

    // Show property edit dialog
    int editResult = JOptionPane.showConfirmDialog(
          this, propertyPanel, "Edit Events Property", JOptionPane.OK_CANCEL_OPTION);

    if (editResult == JOptionPane.OK_OPTION) {
      String selectedProperty = (String) propertyDropdown.getSelectedItem();
      String propertyName;
      String newValue;

      // Map UI property to API property
      switch (selectedProperty) {
        case "Edit Name":
          propertyName = "name";
          newValue = inputField.getText().trim();

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

        case "Edit Start DateTime":
          propertyName = "start";

          try {
            // Parse just the time portion
            LocalTime timeInput = LocalTime.parse(inputField.getText().trim(),
                  DateTimeFormatter.ofPattern("HH:mm"));

            // Combine with today's date
            LocalDate today = LocalDate.now();
            LocalDateTime fullDateTime = LocalDateTime.of(today, timeInput);

            // Format for the command in the required format
            DateTimeFormatter cmdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            newValue = fullDateTime.format(cmdFormatter);
          } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                  "Invalid time format. Use HH:MM (e.g., 09:30)",
                  "Invalid Input",
                  JOptionPane.WARNING_MESSAGE);
            return;
          }
          break;

        case "Edit End DateTime":
          propertyName = "end";

          try {
            // Parse just the time portion
            LocalTime timeInput = LocalTime.parse(inputField.getText().trim(),
                  DateTimeFormatter.ofPattern("HH:mm"));

            // Combine with today's date
            LocalDate today = LocalDate.now();
            LocalDateTime fullDateTime = LocalDateTime.of(today, timeInput);

            // Format for the command in the required format
            DateTimeFormatter cmdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            newValue = fullDateTime.format(cmdFormatter);
          } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                  "Invalid time format. Use HH:MM (e.g., 17:30)",
                  "Invalid Input",
                  JOptionPane.WARNING_MESSAGE);
            return;
          }
          break;

        case "Set Private/Public":
          propertyName = "isprivate";
          newValue = "Private".equals(privacyDropdown.getSelectedItem()) ? "true" : "false";
          break;

        default:
          JOptionPane.showMessageDialog(this,
                "Invalid property selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
          return;
      }

      // Confirm for many events
      if (matchingEvents.size() > 10) {
        int confirmResult = JOptionPane.showConfirmDialog(this,
              "You are about to edit " + matchingEvents.size() + " events. Continue?",
              "Confirm Batch Edit",
              JOptionPane.YES_NO_OPTION);

        if (confirmResult != JOptionPane.YES_OPTION) {
          return;
        }
      }

      // Execute the edit
      try {
        String userStartDateStr = startDateField.getText().trim();

        boolean success;
        if (userStartDateStr.isEmpty()) {
          // No start date specified
          success = commandAdapter.editEventsNoStartDate(
                propertyName,
                nameField.getText().trim(),
                newValue
          );
        } else {
          // Start date specified
          LocalDate startDate = LocalDate.parse(userStartDateStr);
          LocalDateTime startDateTime = startDate.atStartOfDay();
          success = commandAdapter.editEvents(
                propertyName,
                nameField.getText().trim(),
                startDateTime,
                newValue
          );
        }

        if (success) {
          edited = true;
          dispose();
        } else {
          JOptionPane.showMessageDialog(this,
                "Failed to update events.",
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
              "Error updating events: " + ex.getMessage(),
              "Update Error",
              JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * Returns whether events were edited.
   */
  public boolean isEdited() {
    return edited;
  }
}