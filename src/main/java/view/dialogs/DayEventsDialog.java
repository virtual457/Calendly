package view.dialogs;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.ICalendarEventDTO;

/**
 * Dialog for displaying all events on a specific day.
 */
public class DayEventsDialog extends JDialog {
  private LocalDate date;
  private List<ICalendarEventDTO> events;

  private Runnable addEventListener;
  private Consumer<ICalendarEventDTO> viewEventListener;
  private Consumer<ICalendarEventDTO> editEventListener;

  /**
   * Creates a new day events dialog.
   */
  public DayEventsDialog(JFrame parent, LocalDate date, List<ICalendarEventDTO> events) {
    super(parent, "Events on " + date.toString(), true);
    this.date = date;
    this.events = events;

    initializeUI();
    pack();
    setLocationRelativeTo(parent);
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Main panel with scrolling capability
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
      // Add each event with view/edit buttons
      for (ICalendarEventDTO event : events) {
        JPanel eventItemPanel = createEventItemPanel(event);
        eventPanel.add(eventItemPanel);
        eventPanel.add(Box.createVerticalStrut(5));
      }
    }

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton addButton = new JButton("Add New Event");
    JButton closeButton = new JButton("Close");

    addButton.addActionListener(e -> {
      if (addEventListener != null) {
        addEventListener.run();
      }
    });

    closeButton.addActionListener(e -> dispose());

    buttonPanel.add(addButton);
    buttonPanel.add(closeButton);

    // Assemble all parts
    JScrollPane scrollPane = new JScrollPane(eventPanel);
    scrollPane.setBorder(null);

    mainPanel.add(dateLabel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    // Size the dialog
    setSize(450, 400);
  }

  private JPanel createEventItemPanel(ICalendarEventDTO event) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 0, 3, 0),
          BorderFactory.createLineBorder(new Color(200, 200, 200))
    ));

    // Event details
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

    // Event name
    JLabel nameLabel = new JLabel(event.getEventName());
    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    detailsPanel.add(nameLabel);

    // Time
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    JLabel timeLabel = new JLabel(
          event.getStartDateTime().format(timeFormatter) + " - " +
                event.getEndDateTime().format(timeFormatter)
    );
    detailsPanel.add(timeLabel);

    // Location if available
    if (event.getEventLocation() != null && !event.getEventLocation().isEmpty()) {
      JLabel locationLabel = new JLabel("📍 " + event.getEventLocation());
      locationLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
      detailsPanel.add(locationLabel);
    }

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    JButton viewButton = new JButton("View");
    viewButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
    viewButton.addActionListener(e -> {
      if (viewEventListener != null) {
        viewEventListener.accept(event);
      }
    });

    JButton editButton = new JButton("Edit");
    editButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
    editButton.addActionListener(e -> {
      if (editEventListener != null) {
        editEventListener.accept(event);
      }
    });

    buttonPanel.add(viewButton);
    buttonPanel.add(editButton);

    // Add components to panel
    panel.add(detailsPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.EAST);

    return panel;
  }

  public void setAddEventListener(Runnable listener) {
    this.addEventListener = listener;
  }

  public void setViewEventListener(Consumer<ICalendarEventDTO> listener) {
    this.viewEventListener = listener;
  }

  public void setEditEventListener(Consumer<ICalendarEventDTO> listener) {
    this.editEventListener = listener;
  }
}