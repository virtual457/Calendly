package view.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Panel for calendar navigation - displays current month/year and navigation buttons.
 */
public class CalendarNavigationPanel extends CalendarUIComponent {
  private JButton prevButton, nextButton;
  private JLabel monthYearLabel;
  private YearMonth currentYearMonth;
  private Consumer<YearMonth> monthChangeListener;

  /**
   * Creates a new navigation panel with the specified initial month.
   *
   * @param initialMonth The month to initially display
   */
  public CalendarNavigationPanel(YearMonth initialMonth) {
    this.currentYearMonth = initialMonth;
    initialize();
  }

  @Override
  protected void initialize() {
    panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    // Create navigation buttons with consistent size
    prevButton = new JButton("<");
    prevButton.setPreferredSize(new Dimension(45, 25));
    nextButton = new JButton(">");
    nextButton.setPreferredSize(new Dimension(45, 25));

    // Create month/year label with larger, bold font
    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    monthYearLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
    updateMonthYearLabel();

    // Add button event handlers
    prevButton.addActionListener(e -> {
      currentYearMonth = currentYearMonth.minusMonths(1);
      updateMonthYearLabel();
      notifyMonthChanged();
    });

    nextButton.addActionListener(e -> {
      currentYearMonth = currentYearMonth.plusMonths(1);
      updateMonthYearLabel();
      notifyMonthChanged();
    });

    // Add components to panel
    panel.add(prevButton);
    panel.add(monthYearLabel);
    panel.add(nextButton);
  }

  @Override
  protected void refresh() {
    updateMonthYearLabel();
  }

  /**
   * Updates the label showing the current month and year.
   */
  private void updateMonthYearLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthYearLabel.setText(currentYearMonth.format(formatter));
  }

  /**
   * Sets a listener to be notified when the month changes.
   *
   * @param listener The listener to call when month changes
   */
  public void setMonthChangeListener(Consumer<YearMonth> listener) {
    this.monthChangeListener = listener;
  }

  /**
   * Gets the currently displayed year-month.
   *
   * @return The current YearMonth
   */
  public YearMonth getCurrentYearMonth() {
    return currentYearMonth;
  }

  /**
   * Notifies listeners that the month has changed.
   */
  private void notifyMonthChanged() {
    if (monthChangeListener != null) {
      monthChangeListener.accept(currentYearMonth);
    }
  }
}