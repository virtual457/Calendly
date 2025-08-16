package view.components;

import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Sidebar panel containing action buttons.
 */
public class SidebarPanel extends CalendarUIComponent {
  private JButton createEventButton;
  private JButton editEventsButton;
  private JButton exportButton;
  private JButton importButton;

  private Runnable createEventListener;
  private Runnable editEventsListener;
  private Runnable exportListener;
  private Runnable importListener;

  /**
   * Creates a new sidebar panel.
   */
  public SidebarPanel() {
    initialize();
  }

  @Override
  protected void initialize() {
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    // Create buttons
    createEventButton = new JButton("Create Event");
    editEventsButton = new JButton("Edit Events");
    exportButton = new JButton("Export Calendar");
    importButton = new JButton("Import Calendar");

    // Add event handlers
    createEventButton.addActionListener(e -> {
      if (createEventListener != null) {
        createEventListener.run();
      }
    });

    editEventsButton.addActionListener(e -> {
      if (editEventsListener != null) {
        editEventsListener.run();
      }
    });

    exportButton.addActionListener(e -> {
      if (exportListener != null) {
        exportListener.run();
      }
    });

    importButton.addActionListener(e -> {
      if (importListener != null) {
        importListener.run();
      }
    });

    // Add buttons to panel with spacing
    panel.add(createEventButton);
    panel.add(Box.createVerticalStrut(10));
    panel.add(editEventsButton);
    panel.add(Box.createVerticalStrut(10));
    panel.add(exportButton);
    panel.add(Box.createVerticalStrut(10));
    panel.add(importButton);
  }

  @Override
  protected void refresh() {
    // No action needed for refresh
  }

  /**
   * Sets a listener for create event button clicks.
   */
  public void setCreateEventListener(Runnable listener) {
    this.createEventListener = listener;
  }

  /**
   * Sets a listener for edit events button clicks.
   */
  public void setEditEventsListener(Runnable listener) {
    this.editEventsListener = listener;
  }

  /**
   * Sets a listener for export button clicks.
   */
  public void setExportListener(Runnable listener) {
    this.exportListener = listener;
  }

  /**
   * Sets a listener for import button clicks.
   */
  public void setImportListener(Runnable listener) {
    this.importListener = listener;
  }
}