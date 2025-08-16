package view.components;

import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Panel for selecting and managing calendars.
 */
public class CalendarSelectorPanel extends CalendarUIComponent {
  private JComboBox<String> calendarSelector;
  private JButton createCalendarButton;
  private Consumer<String> selectionListener;
  private Runnable createCalendarListener;

  /**
   * Creates a new calendar selector panel.
   */
  public CalendarSelectorPanel() {
    initialize();
  }

  @Override
  protected void initialize() {
    // Use FlowLayout with LEFT alignment to ensure components are arranged horizontally
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

    // Create calendar selector dropdown
    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new java.awt.Dimension(120, 25));
    panel.add(calendarSelector);

    // Create "New Calendar" button
    createCalendarButton = new JButton("New Calendar");
    panel.add(createCalendarButton);

    // Add event handlers
    calendarSelector.addActionListener(e -> {
      if (selectionListener != null && calendarSelector.getSelectedItem() != null) {
        selectionListener.accept((String) calendarSelector.getSelectedItem());
      }
    });

    createCalendarButton.addActionListener(e -> {
      if (createCalendarListener != null) {
        createCalendarListener.run();
      }
    });
  }

  @Override
  protected void refresh() {
    // No action needed - updateCalendars handles refreshing the list
  }

  /**
   * Updates the list of available calendars.
   *
   * @param calendarNames The list of calendar names to display
   * @param selectedCalendar The currently selected calendar
   */
  public void updateCalendars(List<String> calendarNames, String selectedCalendar) {
    // Save and remove listeners to prevent unwanted events
    var listeners = calendarSelector.getActionListeners();
    for (var listener : listeners) {
      calendarSelector.removeActionListener(listener);
    }

    // Update the items
    calendarSelector.removeAllItems();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }

    // Restore selection if possible
    if (selectedCalendar != null && calendarNames.contains(selectedCalendar)) {
      calendarSelector.setSelectedItem(selectedCalendar);
    } else if (calendarSelector.getItemCount() > 0) {
      calendarSelector.setSelectedIndex(0);
    }

    // Restore listeners
    for (var listener : listeners) {
      calendarSelector.addActionListener(listener);
    }
  }

  /**
   * Sets a listener to be notified when a calendar is selected.
   *
   * @param listener The listener to call when a calendar is selected
   */
  public void setSelectionListener(Consumer<String> listener) {
    this.selectionListener = listener;
  }

  /**
   * Sets a listener to be notified when the create calendar button is clicked.
   *
   * @param listener The listener to call when create calendar is clicked
   */
  public void setCreateCalendarListener(Runnable listener) {
    this.createCalendarListener = listener;
  }

  /**
   * Gets the currently selected calendar name.
   *
   * @return The selected calendar name or null if none selected
   */
  public String getSelectedCalendar() {
    return (String) calendarSelector.getSelectedItem();
  }
}