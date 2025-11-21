# Mini Pascal Compiler

A compiler for Mini Pascal written in Java. This project implements lexical analysis, parsing, and code generation for a subset of the Pascal programming language.

## Features

- Lexical Analysis using keyword-based tokenization
- Recursive descent parser
- Symbol table management
- Code generation to P-code
- Support for variables, arrays, procedures, and control structures

## Project Structure

- `Driver.java` - Main entry point for the compiler
- `LexAnalyzer.java` - Lexical analyzer/scanner
- `Parser.java` - Recursive descent parser
- `CodeGenerator.java` - P-code generator and simulator
- `SymbolTable.java` - Symbol table implementation
- `Token.java` - Token representation
- `Symbol.java` - Symbol representation for symbol table
- `StackHandler.java` - Stack operations handler
- `keywords.txt` - Reserved keywords list
- `array.pas` - Sample Pascal program

## How to Build

```bash
javac *.java
```

## How to Run

```bash
java Driver <input-file.pas>
```

Example:
```bash
java Driver array.pas
```

## Note

This is a work in progress. The `labelStat()` method is not fully implemented yet.

## Author

Pedro Schmidt - CSCI 465 (Principles of Translation)
