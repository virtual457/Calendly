# 📅 Calendly

A robust, enterprise-grade calendar application built with Java, featuring advanced design patterns, comprehensive test coverage, and multiple user interfaces.

## 🛠️ Technical Stack

### **Core Technologies**
- **Language**: Java 11
- **Build Tool**: Maven 4.0.0
- **Testing**: JUnit 4.13.2
- **Mutation Testing**: PIT 1.17.2

### **Key Features**
- **Multi-Interface Support**: GUI, Interactive Console, and Headless modes
- **Event Management**: Create, edit, copy, and delete calendar events
- **Calendar Operations**: Multiple calendar support with timezone handling
- **Data Import/Export**: CSV file support for calendar data persistence
- **Command Processing**: Robust command parsing and execution system

## 📁 Project Structure

```
src/
├── main/java/
│   ├── model/           # Business logic and data models
│   ├── view/            # UI implementations (GUI, Console)
│   ├── controller/      # Command processing and coordination
│   └── calendarapp/     # Application entry point
└── test/java/
    ├── model/           # Model layer tests
    ├── view/            # View layer tests
    ├── controller/      # Controller and command tests
    └── calendarapp/     # Integration tests
```

### **📖 Usage Documentation**
For detailed usage instructions, command reference, and examples, see **[USAGE.md](USAGE.md)**.

The application supports three modes:
- **GUI Mode**: Graphical user interface (default)
- **Interactive Console**: Command-line interface
- **Headless Mode**: Automated batch processing

## 📊 Code Quality Metrics

- **Lines of Code**: ~15,000+ lines
- **Test Coverage**: Comprehensive unit and integration testing
- **Design Patterns**: 6+ patterns implemented
- **Interface Segregation**: Clean separation of concerns
- **Dependency Inversion**: Interface-based design throughout

## 🎯 Key Achievements

- **Scalable Architecture**: Easy to extend with new commands and views
- **Maintainable Code**: Clear separation of concerns and comprehensive documentation
- **Robust Testing**: Extensive test suite ensuring reliability
- **Multiple Interfaces**: Support for different user interaction modes
- **Enterprise-Ready**: Production-quality code with proper error handling

## 🔧 Development Practices

- **Clean Code**: Meaningful naming, small methods, and clear documentation
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Error Handling**: Comprehensive exception handling and user feedback
- **Documentation**: Extensive JavaDoc comments and inline documentation

## 🏗️ Architecture & Design Patterns

### **MVC (Model-View-Controller) Architecture**
- **Model Layer**: Core business logic with `ICalendarModel` interface and `CalendarModel` implementation
- **View Layer**: Multiple UI implementations (GUI, Interactive Console, Headless Console) via `IView` interface
- **Controller Layer**: Command processing and user interaction coordination through `ICalendarController`

### **Design Patterns Implemented**

#### 1. **Command Pattern**
- **Purpose**: Encapsulates user actions as objects for flexible command execution
- **Implementation**: `ICommand` interface with concrete commands like `CreateEventCommand`, `EditEventCommand`, `ExportEventsCommand`
- **Benefits**: Undo/redo capability, command queuing, and extensible command system

#### 2. **Factory Pattern**
- **Model Factory**: `ICalendarModel.createInstance()` for different model implementations
- **View Factory**: `ViewFactory.createView()` for creating appropriate UI components
- **Controller Factory**: `ICalendarController.createInstance()` for controller instantiation

#### 3. **Adapter Pattern**
- **Object-to-Command Adapter**: `ObjectToCommandAdapter` bridges object-oriented operations to command-based execution
- **Command Executor Adapter**: `CommandExecutorAdaptor` provides unified command execution interface

#### 4. **Builder Pattern**
- **Event Builder**: `ICalendarEventBuilder` for constructing complex calendar events
- **DTO Builder**: `ICalendarEventDTOBuilder` for creating data transfer objects

#### 5. **Observer Pattern**
- **Read-Only Model**: `IReadOnlyCalendarModel` provides controlled access to model data
- **View Updates**: Views observe model changes through read-only interfaces

#### 6. **Strategy Pattern**
- **Command Invoker**: `CommandInvoker` dynamically selects and executes commands based on user input
- **View Selection**: Different view strategies (GUI, Console, Headless) based on runtime configuration

## 🧪 Test-Driven Development (TDD)

### **Comprehensive Test Coverage**
- **31 Test Classes** with extensive unit and integration tests
- **Test-to-Code Ratio**: ~60% (31 test files vs 52 source files)
- **Testing Framework**: JUnit 4.13.2 with advanced assertions and mocking

### **Test Categories**
- **Unit Tests**: Individual component testing (Model, Controller, View layers)
- **Integration Tests**: End-to-end workflow testing
- **Command Tests**: Each command implementation thoroughly tested
- **Adapter Tests**: Adapter pattern implementations validated

### **Mutation Testing**
- **PIT Framework**: Mutation testing for code quality assurance
- **Configuration**: Automated mutation testing in Maven build pipeline

---

*This project demonstrates advanced software engineering principles, design patterns, and testing methodologies suitable for enterprise-level applications.*
