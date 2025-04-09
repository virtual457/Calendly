package view;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.ICalendarCommandAdapter;
import controller.ICommandExecutor;
import model.ICalendarEventDTO;
import model.IReadOnlyCalendarModel;
import view.components.CalendarGridPanel;
import view.components.CalendarNavigationPanel;
import view.components.CalendarSelectorPanel;
import view.components.SidebarPanel;
import view.dialogs.DayEventsDialog;
import view.dialogs.EditEventsDialog;
import view.dialogs.EventCreationDialog;
import view.dialogs.EventDetailsDialog;
import view.dialogs.EventEditDialog;

/**
 * Main GUI view for the calendar application.
 */
public class GuiView extends JFrame implements IView {
  // Model and controller references
  private IReadOnlyCalendarModel model;
  private ICalendarCommandAdapter commandAdapter;

  // UI components
  private CalendarNavigationPanel navigationPanel;
  private CalendarGridPanel calendarGrid;
  private CalendarSelectorPanel calendarSelector;
  private SidebarPanel sidebar;

  // State
  private String selectedCalendar;
  private YearMonth currentYearMonth;
  private boolean displayEnabled;

  /**
   * Creates a new GUI view with the specified model.
   *
   * @param model The calendar model to display
   */
  public GuiView(IReadOnlyCalendarModel model) {
    this.model = model;
    this.currentYearMonth = YearMonth.now();
    this.selectedCalendar = "Default";
    this.displayEnabled = false;
  }

  /**
   * Initializes the UI components and layout.
   */
  private void initializeUI() {
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLayout(new BorderLayout());

    // Initialize core UI components
    initializeTopPanel();
    calendarGrid = new CalendarGridPanel(currentYearMonth);
    sidebar = new SidebarPanel();

    // Set up component interactions
    setupEventHandlers();

    // Add components to the frame
    add(calendarGrid.getPanel(), BorderLayout.CENTER);
    add(sidebar.getPanel(), BorderLayout.EAST);

    // Initialize default calendar
    commandAdapter.createCalendar("Default", "UTC");
    commandAdapter.useCalendar("Default");

    // Display the UI
    setVisible(true);
    refreshView();
    this.displayEnabled = true;
  }

  /**
   * Initializes the top panel with navigation controls first, followed by calendar selector.
   */
  private void initializeTopPanel() {
    // Create a single panel with FlowLayout for better spacing and alignment
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

    // Create navigation panel FIRST (leftmost position)
    navigationPanel = new CalendarNavigationPanel(currentYearMonth);
    navigationPanel.setMonthChangeListener(month -> {
      currentYearMonth = month;
      calendarGrid.setMonth(month);
      refreshCalendarView();
    });

    // Create calendar selector SECOND (rightmost position)
    calendarSelector = new CalendarSelectorPanel();
    updateCalendarSelector();

    // Set up listeners for the calendar selector
    calendarSelector.setSelectionListener(calendar -> {
      selectedCalendar = calendar;
      commandAdapter.useCalendar(calendar);
      refreshCalendarView();
    });

    calendarSelector.setCreateCalendarListener(this::createNewCalendar);

    // Add panels to top panel in the desired order: navigation FIRST, then selector
    topPanel.add(navigationPanel.getPanel());
    topPanel.add(calendarSelector.getPanel());

    // Add completed panel to frame
    add(topPanel, BorderLayout.NORTH);
  }

  /**
   * Sets up event handlers for UI components.
   */
  private void setupEventHandlers() {
    // Navigation panel handlers
    navigationPanel.setMonthChangeListener(month -> {
      currentYearMonth = month;
      calendarGrid.setMonth(month);
      refreshCalendarView();
    });

    // Calendar grid handlers
    calendarGrid.setDayClickListener((date, event) -> {
      showDayEvents(date);
    });

    // Calendar selector handlers
    calendarSelector.setSelectionListener(calendar -> {
      selectedCalendar = calendar;
      commandAdapter.useCalendar(calendar);
      refreshCalendarView();
    });

    calendarSelector.setCreateCalendarListener(() -> {
      createNewCalendar();
    });

    // Sidebar handlers
    sidebar.setCreateEventListener(() -> {
      createNewEvent();
    });

    sidebar.setEditEventsListener(() -> {
      editEvents();
    });

    sidebar.setExportListener(() -> {
      exportCalendar();
    });

    sidebar.setImportListener(() -> {
      importCalendar();
    });
  }

  /**
   * Refreshes all UI components.
   */
  private void refreshView() {
    updateCalendarSelector();
    refreshCalendarView();
  }

  /**
   * Updates the calendar selector with current calendars.
   */
  private void updateCalendarSelector() {
    List<String> calendarNames = model.getCalendarNames();
    calendarSelector.updateCalendars(calendarNames, selectedCalendar);
  }

  /**
   * Refreshes the calendar grid with current events.
   */
  private void refreshCalendarView() {
    // Get events for current month
    Map<LocalDate, List<ICalendarEventDTO>> events = getEventsInMonthRange(
          selectedCalendar,
          currentYearMonth.atDay(1),
          currentYearMonth.atEndOfMonth()
    );

    // Update grid
    calendarGrid.updateEvents(events);
  }

  /**
   * Gets events for the specified date range grouped by day.
   */
  private Map<LocalDate, List<ICalendarEventDTO>> getEventsInMonthRange(
        String calendarName, LocalDate startOfMonth, LocalDate endOfMonth) {

    Map<LocalDate, List<ICalendarEventDTO>> eventsByDay = new HashMap<>();

    // Get all events in the month range
    List<ICalendarEventDTO> allEvents = model.getEventsInRange(
          calendarName,
          startOfMonth.atStartOfDay(),
          endOfMonth.atTime(23, 59, 59)
    );

    // Group events by day
    for (ICalendarEventDTO event : allEvents) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      eventsByDay.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(event);
    }

    return eventsByDay;
  }

  /**
   * Shows events for a specific day.
   */
  private void showDayEvents(LocalDate date) {
    List<ICalendarEventDTO> events = getEventsForDay(selectedCalendar, date);

    // Create a dialog to show the events
    DayEventsDialog dialog = new DayEventsDialog(this, date, events);
    dialog.setAddEventListener(() -> {
      dialog.dispose();
      createEventOnDay(date);
    });
    dialog.setViewEventListener(event -> {
      showEventDetails(event);
    });
    dialog.setEditEventListener(event -> {
      dialog.dispose();
      editEvent(event);
    });
    dialog.setVisible(true);
  }

  /**
   * Gets events for a specific day.
   */
  private List<ICalendarEventDTO> getEventsForDay(String calendarName, LocalDate date) {
    return model.getEventsInRange(
          calendarName,
          date.atStartOfDay(),
          date.atTime(23, 59, 59)
    );
  }

  /**
   * Shows details for a specific event.
   */
  private void showEventDetails(ICalendarEventDTO event) {
    EventDetailsDialog detailsView = new EventDetailsDialog(this, event);
    detailsView.setVisible(true);
  }

  /**
   * Creates a new calendar.
   */
  private void createNewCalendar() {
    String name = JOptionPane.showInputDialog(this, "Enter calendar name:");
    if (name != null && !name.trim().isEmpty()) {
      // Show timezone selection dialog
      String[] availableZones = getAvailableTimezones();
      String selectedZone = (String) JOptionPane.showInputDialog(
            this,
            "Select timezone:",
            "Timezone Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            availableZones,
            java.time.ZoneId.systemDefault().getId());

      if (selectedZone != null) {
        boolean created = commandAdapter.createCalendar(name, selectedZone);
        if (created) {
          updateCalendarSelector();
          selectedCalendar = name;
          commandAdapter.useCalendar(name);
          refreshCalendarView();
        } else {
          JOptionPane.showMessageDialog(
                this,
                "Failed to create calendar. Name may already exist.",
                "Creation Error",
                JOptionPane.ERROR_MESSAGE
          );
        }
      }
    }
  }

  /**
   * Creates a new event on the current day.
   */
  private void createNewEvent() {
    createEventOnDay(LocalDate.now());
  }

  /**
   * Creates a new event on a specific day.
   */
  private void createEventOnDay(LocalDate date) {
    EventCreationDialog dialog = new EventCreationDialog(this, date);
    dialog.setVisible(true);

    // If dialog closed with a result
    ICalendarEventDTO eventDTO = dialog.getResult();
    if (eventDTO != null) {
      boolean created = commandAdapter.createEvent(eventDTO);
      if (created) {
        refreshCalendarView();
      } else {
        JOptionPane.showMessageDialog(
              this,
              "Failed to create event. There may be a scheduling conflict.",
              "Creation Error",
              JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  /**
   * Edits an existing event.
   */
  private void editEvent(ICalendarEventDTO event) {
    EventEditDialog dialog = new EventEditDialog(this, event);
    dialog.setVisible(true);

    // If dialog closed with edit request
    if (dialog.isEdited()) {
      refreshCalendarView();
    }
  }

  /**
   * Opens the edit events dialog.
   */
  private void editEvents() {
    // Use the dedicated dialog for batch editing
    EditEventsDialog dialog = new EditEventsDialog(this, model, selectedCalendar, commandAdapter);
    dialog.setVisible(true);

    // Refresh after edits
    if (dialog.isEdited()) {
      refreshCalendarView();
    }
  }

  /**
   * Exports the calendar to a file.
   */
  private void exportCalendar() {
    String filename = JOptionPane.showInputDialog(
          this,
          "Enter export filename (no extension needed):",
          "Export Calendar",
          JOptionPane.QUESTION_MESSAGE
    );

    if (filename != null && !filename.trim().isEmpty()) {
      // Remove any extension if user added one
      if (filename.contains(".")) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
      }

      // Add csv extension
      String outputFile = filename + ".csv";

      try {
        commandAdapter.exportCalendar(outputFile);
        JOptionPane.showMessageDialog(
              this,
              "Calendar exported successfully to " + outputFile,
              "Export Complete",
              JOptionPane.INFORMATION_MESSAGE
        );
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
              this,
              "Error exporting calendar: " + ex.getMessage(),
              "Export Error",
              JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  /**
   * Imports calendar data from a file.
   */
  private void importCalendar() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Calendar");
    fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();

      try {
        boolean success = commandAdapter.importCalendar(file.getAbsolutePath().replace("\\", "\\\\"));
        if (success) {
          refreshCalendarView();
          JOptionPane.showMessageDialog(
                this,
                "Calendar imported successfully",
                "Import Complete",
                JOptionPane.INFORMATION_MESSAGE
          );
        } else {
          JOptionPane.showMessageDialog(
                this,
                "Failed to import calendar",
                "Import Error",
                JOptionPane.ERROR_MESSAGE
          );
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
              this,
              "Error importing calendar: " + ex.getMessage(),
              "Import Error",
              JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  /**
   * Gets available timezone IDs.
   */
  private String[] getAvailableTimezones() {
    Set<String> zoneIds = java.time.ZoneId.getAvailableZoneIds();
    ArrayList<String> zones = new ArrayList<>(zoneIds);
    Collections.sort(zones);
    return zones.toArray(new String[0]);
  }

  // IView interface implementation

  @Override
  public void display(String message) {
    if (displayEnabled) {
      SwingUtilities.invokeLater(() -> {
        // Determine message type based on content
        int messageType = JOptionPane.INFORMATION_MESSAGE;
        if (message.contains("Error") || message.contains("Failed")) {
          messageType = JOptionPane.ERROR_MESSAGE;
        } else if (message.contains("Warning")) {
          messageType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(
              this,
              message,
              "Calendar Notification",
              messageType
        );
      });
    }
    System.out.println(message);
  }

  @Override
  public void start(ICommandExecutor commandExecutor) {
    this.commandAdapter = commandExecutor.getCommandAdapter();
    initializeUI();
    display("Welcome to the Calendar App!");
  }
}