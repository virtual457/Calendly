package view;

public class ConsoleView implements IView {
  @Override
  public void display(String message) {
    System.out.println(message == null ? "" : message);
  }
}