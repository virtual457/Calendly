package view.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import controller.ICalendarCommandAdapter;
import model.ICalendarEventDTO;

/**
 * Dialog for editing an existing event.
 */
public class EventEditDialog extends JDialog {
  private final ICalendarEventDTO event;
  private boolean edited = false;

  // UI components for each property
  private JComboBox<String> propertySelector;
  private JPanel inputPanel;
  private JTextField inputField;
  private JComboBox<String> privacyDropdown;

  // For date-time editing
  private JTextField dateTimeField;

  private ICalendarCommandAdapter commandAdapter;

  /**
   * Constructs an EventEditDialog.
   *
   * @param parent The parent frame
   * @param event The event to edit
   */
  public EventEditDialog(Frame parent, ICalendarEventDTO event,
                         ICalendarCommandAdapter adapter) {
    super(parent, "Edit Event", true);
    this.event = event;
    commandAdapter = adapter;

    initializeUI();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Main panel
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Event details section
    JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    JLabel eventDetailsLabel = new JLabel(
          "Editing: " + event.getEventName() + " (" +
                event.getStartDateTime().format(formatter) + " - " +
                event.getEndDateTime().format(formatter) + ")"
    );
    eventDetailsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    detailsPanel.add(eventDetailsLabel);

    // Property selection
    JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    propertyPanel.add(new JLabel("Select property to edit:"));

    String[] properties = {"Name", "Description", "Location", "Start", "End", "Privacy"};
    propertySelector = new JComboBox<>(properties);
    propertyPanel.add(propertySelector);

    // Input panel (will be updated based on selection)
    inputPanel = new JPanel(new BorderLayout());
    inputField = new JTextField(20);
    dateTimeField = new JTextField(20);
    privacyDropdown = new JComboBox<>(new String[]{"Private", "Public"});

    // Initialize with first property (Name)
    updateInputPanel();

    // Property change listener
    propertySelector.addActionListener(e -> updateInputPanel());

    // For recurring events, add checkbox at the bottom
    JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    if (Boolean.TRUE.equals(event.isRecurring())) {
      JRadioButton editOneRadio = new JRadioButton("Edit only this occurrence");
      JRadioButton editAllRadio = new JRadioButton("Edit all occurrences");
      ButtonGroup radioGroup = new ButtonGroup();
      radioGroup.add(editOneRadio);
      radioGroup.add(editAllRadio);
      editOneRadio.setSelected(true);

      checkboxPanel.add(editOneRadio);
      checkboxPanel.add(editAllRadio);
    }

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancelButton = new JButton("Cancel");
    JButton saveButton = new JButton("Save");

    cancelButton.addActionListener(e -> dispose());
    saveButton.addActionListener(e -> saveChanges());

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    // Add components to main panel
    mainPanel.add(detailsPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(propertyPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(inputPanel);
    if (Boolean.TRUE.equals(event.isRecurring())) {
      mainPanel.add(Box.createVerticalStrut(10));
      mainPanel.add(checkboxPanel);
    }

    // Add to dialog
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    setPreferredSize(new Dimension(450, 250));
  }

  /**
   * Updates the input panel based on selected property.
   */
  private void updateInputPanel() {
    inputPanel.removeAll();
    String selectedProperty = (String) propertySelector.getSelectedItem();

    // Pre-fill field with current value
    switch (selectedProperty) {
      case "Name":
        inputPanel.add(new JLabel("New name:"), BorderLayout.WEST);
        inputField.setText(event.getEventName());
        inputPanel.add(inputField, BorderLayout.CENTER);
        break;
      case "Description":
        inputPanel.add(new JLabel("New description:"), BorderLayout.WEST);
        inputField.setText(event.getEventDescription() != null ? event.getEventDescription() : "");
        inputPanel.add(inputField, BorderLayout.CENTER);
        break;
      case "Location":
        inputPanel.add(new JLabel("New location:"), BorderLayout.WEST);
        inputField.setText(event.getEventLocation() != null ? event.getEventLocation() : "");
        inputPanel.add(inputField, BorderLayout.CENTER);
        break;
      case "Start":
        inputPanel.add(new JLabel("New start date/time:"), BorderLayout.WEST);
        // Format date/time in required format
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        dateTimeField.setText(event.getStartDateTime().format(dateTimeFormatter));
        inputPanel.add(dateTimeField, BorderLayout.CENTER);

        // Add help text
        JLabel startTimeHelpLabel = new JLabel("Format: YYYY-MM-DDThh:mm (e.g. 2025-06-10T09:30)");
        startTimeHelpLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        inputPanel.add(startTimeHelpLabel, BorderLayout.SOUTH);
        break;
      case "End":
        inputPanel.add(new JLabel("New end date/time:"), BorderLayout.WEST);
        // Format date/time in required format
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        dateTimeField.setText(event.getEndDateTime().format(endFormatter));
        inputPanel.add(dateTimeField, BorderLayout.CENTER);

        // Add help text
        JLabel endTimeHelpLabel = new JLabel("Format: YYYY-MM-DDThh:mm (e.g. 2025-06-10T10:30)");
        endTimeHelpLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        inputPanel.add(endTimeHelpLabel, BorderLayout.SOUTH);
        break;
      case "Privacy":
        inputPanel.add(new JLabel("Set event to:"), BorderLayout.WEST);
        boolean isPrivate = Boolean.TRUE.equals(event.isPrivate());
        privacyDropdown.setSelectedItem(isPrivate ? "Private" : "Public");
        inputPanel.add(privacyDropdown, BorderLayout.CENTER);
        break;
    }

    inputPanel.revalidate();
    inputPanel.repaint();
  }

  /**
   * Saves the changes to the event.
   */
  private void saveChanges() {
    try {
      String selectedProperty = (String) propertySelector.getSelectedItem();
      String propertyName;
      String newValue;

      // Determine if this is editing all occurrences or just this one
      boolean editAll = false;
      if (Boolean.TRUE.equals(event.isRecurring())) {
        JPanel checkboxPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(4);
        JRadioButton editAllRadio = (JRadioButton) checkboxPanel.getComponent(1);
        editAll = editAllRadio.isSelected();
      }

      // Validate and process input based on selected property
      switch (selectedProperty) {
        case "Name":
          propertyName = "name";
          newValue = inputField.getText().trim();
          if (newValue.isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty");
          }
          break;
        case "Description":
          propertyName = "description";
          newValue = inputField.getText().trim();
          break;
        case "Location":
          propertyName = "location";
          newValue = inputField.getText().trim();
          break;
        case "Start":
          propertyName = "start";
          // Validate date/time format and logic
          try {
            LocalDateTime newStartDateTime = LocalDateTime.parse(dateTimeField.getText().trim(),
                  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

            if (newStartDateTime.isAfter(event.getEndDateTime())) {
              throw new IllegalArgumentException("Start date/time must be before end date/time");
            }

            newValue = dateTimeField.getText().trim();
          } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid format. Use YYYY-MM-DDThh:mm");
          }
          break;
        case "End":
          propertyName = "end";
          // Validate date/time format and logic
          try {
            LocalDateTime newEndDateTime = LocalDateTime.parse(dateTimeField.getText().trim(),
                  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

            if (newEndDateTime.isBefore(event.getStartDateTime())) {
              throw new IllegalArgumentException("End date/time must be after start date/time");
            }

            newValue = dateTimeField.getText().trim();
          } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid format. Use YYYY-MM-DDThh:mm");
          }
          break;
        case "Privacy":
          propertyName = "isprivate";
          newValue = "Private".equals(privacyDropdown.getSelectedItem()) ? "true" : "false";
          break;
        default:
          throw new IllegalArgumentException("Invalid property selected");
      }


      commandAdapter.editEvent(propertyName, event.getEventName(),
          event.getStartDateTime(), event.getEndDateTime(), newValue);

      // For now, just mark as edited and close dialog
      edited = true;
      dispose();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
            this,
            "Error updating event: " + ex.getMessage(),
            "Update Error",
            JOptionPane.ERROR_MESSAGE
      );
    }
  }

  /**
   * Returns whether the event was edited.
   */
  public boolean isEdited() {
    return edited;
  }
}