package view.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.ICalendarEventDTO;

/**
 * Panel displaying the calendar grid with days and events.
 */
public class CalendarGridPanel extends CalendarUIComponent {
  private Map<LocalDate, JPanel> dayPanels;
  private YearMonth currentMonth;
  private BiConsumer<LocalDate, MouseEvent> dayClickListener;
  private Map<LocalDate, List<ICalendarEventDTO>> events;

  /**
   * Creates a new calendar grid for the specified month.
   *
   * @param initialMonth The month to display
   */
  public CalendarGridPanel(YearMonth initialMonth) {
    this.currentMonth = initialMonth;
    this.dayPanels = new HashMap<>();
    this.events = new HashMap<>();
    initialize();
  }

  @Override
  protected void initialize() {
    panel = new JPanel(new BorderLayout());

    // Create day header panel (Sun, Mon, Tue, etc.)
    JPanel headerPanel = createHeaderPanel();
    panel.add(headerPanel, BorderLayout.NORTH);

    // Create calendar grid
    JPanel calendarPanel = new JPanel(new GridLayout(0, 7));
    panel.add(calendarPanel, BorderLayout.CENTER);

    // Create initial day panels
    createCalendarGrid(calendarPanel);
  }

  /**
   * Creates the header panel with day names.
   */
  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new GridLayout(1, 7));
    String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    for (String day : daysOfWeek) {
      JLabel label = new JLabel(day, JLabel.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 12));
      headerPanel.add(label);
    }

    return headerPanel;
  }

  /**
   * Creates the calendar grid with day panels.
   */
  private void createCalendarGrid(JPanel calendarPanel) {
    dayPanels.clear();
    calendarPanel.removeAll();

    // Get first day of month
    LocalDate firstDay = currentMonth.atDay(1);

    // Calculate the start of the calendar grid (might be in previous month)
    LocalDate start = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7);

    // Create 6 weeks of calendar panels (42 days)
    for (int i = 0; i < 42; i++) {
      LocalDate date = start.plusDays(i);
      JPanel dayPanel = createDayPanel(date);
      calendarPanel.add(dayPanel);
      dayPanels.put(date, dayPanel);
    }

    // Update with existing events
    updateEventsOnCalendar();
  }

  /**
   * Creates a panel for an individual day.
   */
  private JPanel createDayPanel(LocalDate date) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    // Day number label
    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));

    // Highlight current month days
    if (date.getMonth() != currentMonth.getMonth()) {
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
    final LocalDate clickDate = date;
    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (dayClickListener != null) {
          dayClickListener.accept(clickDate, e);
        }
      }
    });

    return panel;
  }

  /**
   * Updates the event indicators on the calendar.
   */
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

    // Add event indicators
    for (Map.Entry<LocalDate, List<ICalendarEventDTO>> entry : events.entrySet()) {
      LocalDate date = entry.getKey();
      List<ICalendarEventDTO> dayEvents = entry.getValue();

      JPanel dayPanel = dayPanels.get(date);
      if (dayPanel != null && !dayEvents.isEmpty()) {
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));

        // Add up to 3 event indicators
        for (int i = 0; i < Math.min(3, dayEvents.size()); i++) {
          JLabel eventLabel = new JLabel("• " + dayEvents.get(i).getEventName());
          eventLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
          eventsPanel.add(eventLabel);
        }

        // If there are more events, add a "+more" indicator
        if (dayEvents.size() > 3) {
          JLabel moreLabel = new JLabel("+" + (dayEvents.size() - 3) + " more");
          moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
          eventsPanel.add(moreLabel);
        }

        dayPanel.add(eventsPanel, BorderLayout.CENTER);
      }
    }

    panel.revalidate();
    panel.repaint();
  }

  @Override
  protected void refresh() {
    JPanel calendarPanel = (JPanel) panel.getComponent(1);
    createCalendarGrid(calendarPanel);
  }

  /**
   * Sets the current month to display.
   *
   * @param month The month to display
   */
  public void setMonth(YearMonth month) {
    this.currentMonth = month;
    refresh();
  }

  /**
   * Updates the events to display on the calendar.
   *
   * @param events Map of dates to events
   */
  public void updateEvents(Map<LocalDate, List<ICalendarEventDTO>> events) {
    this.events = events;
    updateEventsOnCalendar();
  }

  /**
   * Sets a listener to be notified when a day is clicked.
   *
   * @param listener The listener to call when a day is clicked
   */
  public void setDayClickListener(BiConsumer<LocalDate, MouseEvent> listener) {
    this.dayClickListener = listener;
  }
}