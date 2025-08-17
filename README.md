<!-- Improved compatibility of back to top link: See: https://github.com/dhmnr/skipr/pull/73 -->
<a id="readme-top"></a>

<!-- *** Thanks for checking out the Best-README-Template. If you have a suggestion *** that would make this better, please fork the repo and create a pull request *** or simply open an issue with the tag "enhancement". *** Don't forget to give the project a star! *** Thanks again! Now go create something AMAZING! :D -->

<!-- PROJECT SHIELDS -->
<!-- *** I'm using markdown "reference style" links for readability. *** Reference links are enclosed in brackets [ ] instead of parentheses ( ). *** See the bottom of this document for the declaration of the reference variables *** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use. *** https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
<!-- [![LinkedIn][linkedin-shield]][linkedin-url] -->

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h3 align="center">📅 Calendly</h3>

  <p align="center">
    A robust, enterprise-grade calendar application built with Java, featuring advanced design patterns, comprehensive test coverage, and multiple user interfaces.
    <br />
    <a href="https://github.com/virtual457/Calendly"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/virtual457/Calendly">View Demo</a>
    ·
    <a href="https://github.com/virtual457/Calendly/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    ·
    <a href="https://github.com/virtual457/Calendly/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About The Project

This project exemplifies **enterprise-level software engineering** through strict adherence to **SOLID principles** and implementation of **more than 6 design patterns** (Command, Factory, Adapter, Builder, Observer, Strategy, Template Method). Built using **MVC architecture** and **Test-Driven Development (TDD)** methodology, it achieves **98% line coverage** and **95% mutation coverage** through comprehensive JUnit and PIT mutation testing. The application features **multiple user interfaces** (GUI, Console, Headless), **clean code practices**, **interface segregation**, **dependency inversion**, and **production-ready error handling**. With **31 test classes**, **extensive documentation**, and **scalable architecture**, this project demonstrates professional Java development practices suitable for enterprise environments.

### Key Features

- **Multi-Interface Support**: GUI, Interactive Console, and Headless modes
- **Event Management**: Create, edit, copy, and delete calendar events
- **Calendar Operations**: Multiple calendar support with timezone handling
- **Data Import/Export**: CSV file support for calendar data persistence
- **Command Processing**: Robust command parsing and execution system
- **Scalable Architecture**: Easy to extend with new commands and views
- **Maintainable Code**: Clear separation of concerns and comprehensive documentation
- **Robust Testing**: Extensive test suite ensuring reliability

### Architecture & Design Patterns

#### **MVC (Model-View-Controller) Architecture**
- **Model Layer**: Core business logic with `ICalendarModel` interface and `CalendarModel` implementation
- **View Layer**: Multiple UI implementations (GUI, Interactive Console, Headless Console) via `IView` interface
- **Controller Layer**: Command processing and user interaction coordination through `ICalendarController`

#### **Design Patterns Implemented**
1. **Command Pattern**: Encapsulates user actions as objects for flexible command execution
   - **Purpose**: Encapsulates user actions as objects for flexible command execution
   - **Implementation**: `ICommand` interface with concrete commands like `CreateEventCommand`, `EditEventCommand`, `ExportEventsCommand`
   - **Benefits**: Undo/redo capability, command queuing, and extensible command system

2. **Factory Pattern**: Creates appropriate instances of models, views, and controllers
   - **Model Factory**: `ICalendarModel.createInstance()` for different model implementations
   - **View Factory**: `ViewFactory.createView()` for creating appropriate UI components
   - **Controller Factory**: `ICalendarController.createInstance()` for controller instantiation

3. **Adapter Pattern**: Bridges object-oriented operations to command-based execution
   - **Object-to-Command Adapter**: `ObjectToCommandAdapter` bridges object-oriented operations to command-based execution
   - **Command Executor Adapter**: `CommandExecutorAdaptor` provides unified command execution interface

4. **Builder Pattern**: Constructs complex calendar events and DTOs
   - **Event Builder**: `ICalendarEventBuilder` for constructing complex calendar events
   - **DTO Builder**: `ICalendarEventDTOBuilder` for creating data transfer objects

5. **Observer Pattern**: Provides controlled access to model data through read-only interfaces
   - **Read-Only Model**: `IReadOnlyCalendarModel` provides controlled access to model data
   - **View Updates**: Views observe model changes through read-only interfaces

6. **Strategy Pattern**: Dynamically selects and executes commands based on user input
   - **Command Invoker**: `CommandInvoker` dynamically selects and executes commands based on user input
   - **View Selection**: Different view strategies (GUI, Console, Headless) based on runtime configuration

### Test-Driven Development (TDD)
- **31 Test Classes** with extensive unit and integration tests
- **Test-to-Code Ratio**: ~60% (31 test files vs 52 source files)
- **Testing Framework**: JUnit 4.13.2 with advanced assertions and mocking
- **Line Coverage**: **98%** - Near-perfect code coverage
- **Mutation Coverage**: **95%** - Excellent mutation testing results

#### **Test Categories**
- **Unit Tests**: Individual component testing (Model, Controller, View layers)
- **Integration Tests**: End-to-end workflow testing
- **Command Tests**: Each command implementation thoroughly tested
- **Adapter Tests**: Adapter pattern implementations validated

#### **Mutation Testing**
- **PIT Framework**: Mutation testing for code quality assurance
- **Configuration**: Automated mutation testing in Maven build pipeline

### Code Quality Metrics
- **Lines of Code**: ~15,000+ lines
- **Test Coverage**: Comprehensive unit and integration testing
- **Design Patterns**: 6+ patterns implemented
- **Interface Segregation**: Clean separation of concerns
- **Dependency Inversion**: Interface-based design throughout

### Key Achievements
- **Scalable Architecture**: Easy to extend with new commands and views
- **Maintainable Code**: Clear separation of concerns and comprehensive documentation
- **Robust Testing**: Extensive test suite ensuring reliability
- **Multiple Interfaces**: Support for different user interaction modes
- **Enterprise-Ready**: Production-quality code with proper error handling

### Development Practices
- **Clean Code**: Meaningful naming, small methods, and clear documentation
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Error Handling**: Comprehensive exception handling and user feedback
- **Documentation**: Extensive JavaDoc comments and inline documentation

### Project Structure
```
Calendar/
├── README.md           # Project documentation and overview
├── USAGE.md            # Detailed usage instructions
├── pom.xml             # Maven build configuration
├── .gitignore          # Git ignore rules
├── src/                # Source code
│   ├── main/java/
│   │   ├── model/      # Business logic and data models
│   │   ├── view/       # UI implementations (GUI, Console)
│   │   ├── controller/ # Command processing and coordination
│   │   └── calendarapp/# Application entry point
│   └── test/java/
│       ├── model/      # Model layer tests
│       ├── view/       # View layer tests
│       ├── controller/ # Controller and command tests
│       └── calendarapp/# Integration tests
└── project-files/      # Additional project files
    ├── *.csv           # Sample data files
    ├── *.jar           # Executable JAR files
    ├── *.png           # Documentation images
    ├── target/         # Build output
    └── .idea/          # IDE configuration
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* [Java 11](https://www.oracle.com/java/)
* [Maven 4.0.0](https://maven.apache.org/)
* [JUnit 4.13.2](https://junit.org/junit4/)
* [PIT Mutation Testing 1.17.2](https://pitest.org/)
* [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
* [Design Patterns](https://en.wikipedia.org/wiki/Design_Patterns)
* [MVC Architecture](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may give instructions on setting up your project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* Java 11 or higher
* Maven 4.0.0 or higher
* Git

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/virtual457/Calendly.git
   ```
2. Navigate to the project directory
   ```sh
   cd Calendly
   ```
3. Build the project
   ```sh
   mvn clean compile
   ```
4. Run tests
   ```sh
   mvn test
   ```
5. Run the application
   ```sh
   mvn exec:java -Dexec.mainClass="calendarapp.CalendarApp"
   ```

For detailed usage instructions, command reference, and examples, see **[USAGE.md](USAGE.md)**.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## Usage

The application supports three modes:
- **GUI Mode**: Graphical user interface (default)
- **Interactive Console**: Command-line interface
- **Headless Mode**: Automated batch processing

_For more examples, please refer to the [Documentation](https://github.com/virtual457/Calendly)_

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ROADMAP -->
## Roadmap

- [ ] Add calendar sharing functionality
- [ ] Implement recurring events
- [ ] Add calendar synchronization
- [ ] Create mobile app version
- [ ] Add cloud storage integration

See the [open issues](https://github.com/virtual457/Calendly/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->
## Contact

Chandan Gowda K S - chandan.keelara@gmail.com

Project Link: [https://github.com/virtual457/Calendly](https://github.com/virtual457/Calendly)

Project Link: [https://github.com/virtual457/Calendly](https://github.com/virtual457/Calendly)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [Choose an Open Source License](https://choosealicense.com)
* [GitHub Emojis](https://gist.github.com/rxaviers/7360908)
* [Malven's Flexbox Cheatsheet](https://flexbox.malven.co/)
* [Malven's Grid Cheatsheet](https://grid.malven.co/)
* [Img Shields](https://shields.io)
* [GitHub Pages](https://pages.github.com)
* [Font Awesome](https://fontawesome.com)
* [React Icons](https://react-icons.github.io/react-icons/search.html?q=search)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/virtual457/Calendly.svg?style=for-the-badge
[forks-shield]: https://img.shields.io/github/forks/virtual457/Calendly.svg?style=for-the-badge
[stars-shield]: https://img.shields.io/github/stars/virtual457/Calendly.svg?style=for-the-badge
[issues-shield]: https://img.shields.io/github/issues/virtual457/Calendly.svg?style=for-the-badge
[license-shield]: https://img.shields.io/github/license/virtual457/Calendly.svg?style=for-the-badge
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[contributors-url]: https://github.com/virtual457/Calendly/graphs/contributors
[forks-url]: https://github.com/virtual457/Calendly/network/members
[stars-url]: https://github.com/virtual457/Calendly/stargazers
[issues-url]: https://github.com/virtual457/Calendly/issues
[license-url]: https://github.com/virtual457/Calendly/blob/master/LICENSE.txt
[linkedin-url]: https://www.linkedin.com/in/chandan-gowda-k-s-765194186/
