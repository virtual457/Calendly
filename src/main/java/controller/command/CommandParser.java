package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utility class for parsing command arguments used by various command classes.
 * Centralizes common parsing logic to reduce duplication and improve consistency.
 */
class CommandParser {

  /**
   * Ensures the given list has at least the specified number of arguments.
   *
   * @param args the argument list to check
   * @param minSize the minimum required size
   * @param errorMessage the error message if the check fails
   * @throws IllegalArgumentException if the list has fewer than minSize elements
   */
  public static void requireMinArgs(List<String> args, int minSize, String errorMessage) {
    if (args.size() < minSize) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Checks if the argument at the specified index matches the expected keyword.
   *
   * @param args the argument list
   * @param index the position to check
   * @param expectedKeyword the expected keyword (case-insensitive)
   * @param errorMessage the error message if the check fails
   * @throws IllegalArgumentException if the keyword doesn't match
   */
  public static void requireKeyword(List<String> args, int index, String expectedKeyword, String errorMessage) {
    if (index >= args.size() || !args.get(index).equals(expectedKeyword)) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Parses a LocalDateTime from the argument at the specified index.
   *
   * @param args the argument list
   * @param index the position to parse
   * @param errorMessage the error message if parsing fails
   * @return the parsed LocalDateTime
   * @throws IllegalArgumentException if the parsing fails
   */
  public static LocalDateTime parseDateTime(List<String> args, int index, String errorMessage) {
    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing datetime value at position " + (index + 1));
    }

    try {
      return LocalDateTime.parse(args.get(index));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(errorMessage + ": " + args.get(index));
    }
  }

  /**
   * Parses a LocalDate from the argument at the specified index.
   *
   * @param args the argument list
   * @param index the position to parse
   * @param errorMessage the error message if parsing fails
   * @return the parsed LocalDate
   * @throws IllegalArgumentException if the parsing fails
   */
  public static LocalDate parseDate(List<String> args, int index, String errorMessage) {
    if (index >= args.size()) {
      throw new IllegalArgumentException("Missing date value at position " + (index + 1));
    }

    try {
      return LocalDate.parse(args.get(index));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(errorMessage + ": " + args.get(index));
    }
  }

  /**
   * Gets a required argument string at the specified index.
   *
   * @param args the argument list
   * @param index the position to retrieve
   * @param errorMessage the error message if the argument is missing
   * @return the argument string
   * @throws IllegalArgumentException if the argument is missing
   */
  public static String getRequiredArg(List<String> args, int index, String errorMessage) {
    if (index >= args.size()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return args.get(index);
  }

  /**
   * Validates that a string argument is not empty.
   *
   * @param value the string to check
   * @param errorMessage the error message if the string is empty
   * @throws IllegalArgumentException if the string is empty
   */
  public static void requireNonEmpty(String value, String errorMessage) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Checks that there are exactly the expected number of arguments.
   *
   * @param args the argument list
   * @param expectedSize the exact size required
   * @param errorMessage the error message if the size doesn't match
   * @throws IllegalArgumentException if the size doesn't match
   */
  public static void requireExactArgs(List<String> args, int expectedSize, String errorMessage) {
    if (args.size() != expectedSize) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Safely parse an integer argument.
   */
  public static int parseInt(String value, String errorMessage) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(errorMessage);
    }
  }
}