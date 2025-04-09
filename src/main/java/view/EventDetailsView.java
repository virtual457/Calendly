package view;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import model.ICalendarEventDTO;

/**
 * A dialog for displaying detailed information about a calendar event.
 */
public class EventDetailsView extends JDialog {
  private final ICalendarEventDTO event;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Constructs an EventDetailsView dialog.
   *
   * @param parent The parent frame
   * @param event The event to display details for
   */
  public EventDetailsView(Frame parent, ICalendarEventDTO event) {
    super(parent, "Event Details", true);
    this.event = event;
    initializeUI();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Main content panel with details
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Event title at the top
    JLabel titleLabel = new JLabel(event.getEventName());
    titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // Create details panel
    JPanel detailsPanel = createDetailsPanel();

    // Add components to main panel
    contentPanel.add(titleLabel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    contentPanel.add(detailsPanel);

    // Add close button at the bottom
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(closeButton);

    // Add everything to the dialog
    add(contentPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    // Set preferred size
    setPreferredSize(new Dimension(500, 400));
  }

  private JPanel createDetailsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    GridBagConstraints labelConstraints = new GridBagConstraints();
    labelConstraints.gridx = 0;
    labelConstraints.anchor = GridBagConstraints.NORTHWEST;
    labelConstraints.insets = new Insets(5, 0, 5, 10);

    GridBagConstraints valueConstraints = new GridBagConstraints();
    valueConstraints.gridx = 1;
    valueConstraints.anchor = GridBagConstraints.NORTHWEST;
    valueConstraints.insets = new Insets(5, 0, 5, 0);
    valueConstraints.weightx = 1.0;
    valueConstraints.fill = GridBagConstraints.HORIZONTAL;

    // Date and time information
    addField(panel, "Start:", formatDateTime(event.getStartDateTime()), labelConstraints, valueConstraints);
    addField(panel, "End:", formatDateTime(event.getEndDateTime()), labelConstraints, valueConstraints);

    // Description
    String description = event.getEventDescription();
    if (description != null && !description.isEmpty()) {
      JLabel descLabel = new JLabel("Description:");
      labelConstraints.gridy++;
      panel.add(descLabel, labelConstraints);

      JTextArea descArea = new JTextArea(description);
      descArea.setLineWrap(true);
      descArea.setWrapStyleWord(true);
      descArea.setEditable(false);
      descArea.setOpaque(false);
      descArea.setFont(UIManager.getFont("Label.font"));

      JScrollPane scrollPane = new JScrollPane(descArea);
      scrollPane.setPreferredSize(new Dimension(300, 80));
      scrollPane.setBorder(BorderFactory.createEmptyBorder());

      valueConstraints.gridy++;
      panel.add(scrollPane, valueConstraints);
    }

    // Location
    String location = event.getEventLocation();
    if (location != null && !location.isEmpty()) {
      addField(panel, "Location:", location, labelConstraints, valueConstraints);
    }

    // Privacy setting
    Boolean isPrivate = event.isPrivate();
    addField(panel, "Private:", isPrivate != null && isPrivate ? "Yes" : "No", labelConstraints, valueConstraints);

    // Recurrence information
    Boolean isRecurring = event.isRecurring();
    if (isRecurring != null && isRecurring) {
      addField(panel, "Recurring:", "Yes", labelConstraints, valueConstraints);

      // Recurrence days
      if (event.getRecurrenceDays() != null && !event.getRecurrenceDays().isEmpty()) {
        StringBuilder daysText = new StringBuilder();
        for (DayOfWeek day : event.getRecurrenceDays()) {
          if (daysText.length() > 0) {
            daysText.append(", ");
          }
          daysText.append(formatDayOfWeek(day));
        }
        addField(panel, "Repeats on:", daysText.toString(), labelConstraints, valueConstraints);
      }

      // Recurrence count
      Integer count = event.getRecurrenceCount();
      if (count != null) {
        addField(panel, "Occurrences:", count.toString(), labelConstraints, valueConstraints);
      }

      // Recurrence end date
      if (event.getRecurrenceEndDate() != null) {
        addField(panel, "Repeats until:", event.getRecurrenceEndDate().format(dateFormatter),
              labelConstraints, valueConstraints);
      }
    }

    // Auto-decline info
    Boolean autoDecline = event.isAutoDecline();
    if (autoDecline != null) {
      addField(panel, "Auto-decline:", autoDecline ? "Yes" : "No", labelConstraints, valueConstraints);
    }

    return panel;
  }

  private void addField(JPanel panel, String label, String value,
                        GridBagConstraints labelConstraints, GridBagConstraints valueConstraints) {
    labelConstraints.gridy++;
    valueConstraints.gridy = labelConstraints.gridy;

    JLabel jLabel = new JLabel(label);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    panel.add(jLabel, labelConstraints);

    JLabel jValue = new JLabel(value);
    panel.add(jValue, valueConstraints);
  }

  private String formatDateTime(java.time.LocalDateTime dateTime) {
    return dateTime.format(dateFormatter) + " at " + dateTime.format(timeFormatter);
  }

  private String formatDayOfWeek(DayOfWeek day) {
    return day.toString().charAt(0) + day.toString().substring(1).toLowerCase();
  }
}