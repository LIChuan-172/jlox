package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The Lox class represents the entry point of the Lox interpreter.
 * It provides methods to run the interpreter on a file or in interactive mode.
 */
public class Lox {
  /**
   * The entry point of the Lox interpreter.
   * 
   * @param args The command line arguments passed to the program.
   * @throws IOException If an I/O error occurs while running the interpreter.
   */

  private static final Interpreter interpreter = new Interpreter();

  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  /**
   * Runs the Lox interpreter on a file.
   *
   * @param path The path to the file to be executed.
   * @throws IOException If an I/O error occurs while reading the file.
   */
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    // Indicate an error in the exit code.
    if (hadError)
      System.exit(65);
    if (hadRuntimeError)
      System.exit(70);
  }

  /**
   * Runs the interactive prompt, allowing the user to enter and execute Lox code
   * line by line.
   * This method reads input from the standard input stream and executes each line
   * of code entered
   * by calling the `run` method.
   *
   * @throws IOException if an I/O error occurs while reading the input
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {

    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error
    if (hadError)
      return;

    // System.out.println(new AstPrinter().print(expression));
    interpreter.interpret(statements);

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }
}
