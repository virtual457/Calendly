package view;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import controller.ICalendarCommandAdapter;
import controller.ICommandExecutor;

public class HeadlessModeTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private PrintStream originalOut;
  private ByteArrayOutputStream outContent;
  private File validScriptFile;
  private SecurityManager originalSecurityManager;

  private static class TestCommandExecutor implements ICommandExecutor {
    private boolean exitCalled = false;

    @Override
    public void executeCommand(String command) {
      if (command.equals("exit")) {
        exitCalled = true;
      }
    }

    @Override
    public ICalendarCommandAdapter getCommandAdapter() {
      return null;
    }

    public boolean wasExitCalled() {
      return exitCalled;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    private Integer lastExitStatus = null;

    @Override
    public void checkExit(int status) {
      lastExitStatus = status;
      throw new SecurityException("System.exit called with status: " + status);
    }

    @Override
    public void checkPermission(java.security.Permission perm) {
    }

    public Integer getLastExitStatus() {
      return lastExitStatus;
    }
  }

  @Before
  public void setUp() throws IOException {
    validScriptFile = tempFolder.newFile("validScript.txt");
    try (FileWriter writer = new FileWriter(validScriptFile)) {
      writer.write("command1\ncommand2\nexit");
    }

    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    originalSecurityManager = System.getSecurityManager();
    NoExitSecurityManager securityManager = new NoExitSecurityManager();
    System.setSecurityManager(securityManager);
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.clearProperty("run_mode");
    System.setSecurityManager(originalSecurityManager);
  }

  @Test
  public void testErrorDisplayWithRunModeFalse() {
    System.setProperty("run_mode", "false");

    HeadlessConsoleView view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    TestCommandExecutor executor = new TestCommandExecutor();
    view.start(executor);

    try {
      view.display("Error: Test error message");
      fail("Should have thrown SecurityException due to System.exit call");
    } catch (SecurityException e) {
      assertTrue(executor.wasExitCalled());
      assertTrue(e.getMessage().contains("System.exit called"));
    }
  }

  @Test
  public void testDifferentCaseErrorMessageWithRunModeFalse() {
    System.setProperty("run_mode", "false");

    HeadlessConsoleView view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    TestCommandExecutor executor = new TestCommandExecutor();
    view.start(executor);

    try {
      view.display("ErRoR: Test message");
      fail("Should have thrown SecurityException due to System.exit call");
    } catch (SecurityException e) {
      assertTrue(executor.wasExitCalled());
    }
  }

  @Test
  public void testRunModeCaseInsensitivity() {
    System.setProperty("run_mode", "False");

    HeadlessConsoleView view = new HeadlessConsoleView(validScriptFile.getAbsolutePath());
    TestCommandExecutor executor = new TestCommandExecutor();
    view.start(executor);

    try {
      view.display("Error: Test error message");
      fail("Should have thrown SecurityException due to System.exit call");
    } catch (SecurityException e) {
      assertTrue(executor.wasExitCalled());
    }
  }

}