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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

  /**
   * Creates a new event edit dialog.
   */
  public EventEditDialog(Frame parent, ICalendarEventDTO event) {
    super(parent, "Edit Event", true);
    this.event = event;

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

    String[] properties = {"Name", "Description", "Location", "Start Time", "End Time", "Privacy"};
    propertySelector = new JComboBox<>(properties);
    propertyPanel.add(propertySelector);

    // Input panel (will be updated based on selection)
    inputPanel = new JPanel(new BorderLayout());
    inputField = new JTextField(20);
    privacyDropdown = new JComboBox<>(new String[]{"Private", "Public"});

    // Initialize with first property (Name)
    updateInputPanel();

    // Property change listener
    propertySelector.addActionListener(e -> updateInputPanel());

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancelButton = new JButton("Cancel");
    JButton saveButton = new JButton("Save");

    cancelButton.addActionListener(e -> dispose());
    saveButton.addActionListener(e -> saveChanges());

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    // Assemble all panels
    mainPanel.add(detailsPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(propertyPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(inputPanel);

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

      case "Start Time":
        inputPanel.add(new JLabel("New start time (HH:MM):"), BorderLayout.WEST);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        inputField.setText(event.getStartDateTime().format(timeFormatter));
        inputPanel.add(inputField, BorderLayout.CENTER);
        break;

      case "End Time":
        inputPanel.add(new JLabel("New end time (HH:MM):"), BorderLayout.WEST);
        DateTimeFormatter endTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        inputField.setText(event.getEndDateTime().format(endTimeFormatter));
        inputPanel.add(inputField, BorderLayout.CENTER);
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
      String apiPropertyName;
      String newValue;

      // Map UI property name to API property name and get new value
      switch (selectedProperty) {
        case "Name":
          apiPropertyName = "name";
          newValue = inputField.getText().trim();
          if (newValue.isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty");
          }
          break;

        case "Description":
          apiPropertyName = "description";
          newValue = inputField.getText().trim();
          break;

        case "Location":
          apiPropertyName = "location";
          newValue = inputField.getText().trim();
          break;

        case "Start Time":
          apiPropertyName = "start";
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

        case "End Time":
          apiPropertyName = "end";
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

        case "Privacy":
          apiPropertyName = "isprivate";
          newValue = "Private".equals(privacyDropdown.getSelectedItem()) ? "true" : "false";
          break;

        default:
          throw new IllegalArgumentException("Invalid property selected");
      }

      // Format command and execute it via parent GuiView
      // This would call commandAdapter.editEvent or similar
      // For now, we'll just set the edited flag and close the dialog
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