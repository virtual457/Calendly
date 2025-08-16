package view.components;

import javax.swing.JPanel;

/**
 * Base component for all calendar UI elements.
 * Provides common functionality and interface for UI components.
 */
abstract class CalendarUIComponent {
  protected JPanel panel;

  /**
   * Gets the panel containing this component.
   *
   * @return The JPanel for this component
   */
  public JPanel getPanel() {
    return panel;
  }

  /**
   * Initializes the component and its UI elements.
   * Called during construction.
   */
  protected abstract void initialize();

  /**
   * Refreshes the component with current data.
   * Should be called when data changes.
   */
  protected abstract void refresh();
}