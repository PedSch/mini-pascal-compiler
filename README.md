# Mini Pascal Compiler

A complete compiler implementation for a subset of the Pascal programming language, written in Java. This project demonstrates the full compilation pipeline from source code to executable P-code with simulation capabilities.

## Features

- **Lexical Analysis**: Keyword-based tokenization with comprehensive operator and literal support
- **Syntax Analysis**: Recursive descent parser implementing Mini Pascal grammar
- **Semantic Analysis**: Symbol table management with type checking
- **Code Generation**: P-code generation and virtual machine simulator
- **Language Support**:
  - Variables (integer, real, boolean, char)
  - Arrays with integer and character indexing
  - Control structures (if-then-else, while-do)
  - Arithmetic and logical expressions
  - Input/output operations (writeln)

## Project Structure

| File | Description |
|------|-------------|
| `Driver.java` | Main entry point that coordinates the compilation pipeline |
| `LexAnalyzer.java` | Lexical analyzer/scanner with keyword and operator recognition |
| `Parser.java` | Recursive descent parser with integrated code generation |
| `CodeGenerator.java` | P-code generator and virtual machine simulator |
| `SymbolTable.java` | Hash-based symbol table for identifier management |
| `Token.java` | Token representation with type and value |
| `Symbol.java` | Symbol representation for variables, arrays, and procedures |
| `StackHandler.java` | Stack operations for P-code execution |
| `keywords.txt` | Pascal reserved keywords dictionary |
| `array.pas` | Sample program demonstrating array operations |

## How to Build

Compile all Java source files:

```bash
javac *.java
```

## How to Run

Execute the compiler with a Pascal source file:

```bash
java Driver <input-file.pas>
```

### Quick Start

Run the automated test suite:
```bash
./test.sh
```

This will compile the compiler and run all example programs.

### Examples

Run the provided array demonstration:
```bash
java Driver examples/array.pas
```
Output:
```
8
10
```

Run arithmetic operations:
```bash
java Driver examples/simple_arithmetic.pas
```
Output:
```
24
-16
80
```

Run control flow example:
```bash
java Driver examples/if_else.pas
```
Output:
```
20
```

Run loop example:
```bash
java Driver examples/while_loop.pas
```
Output:
```
15
```

Create and run your own program:
```bash
echo 'program hello;
var x: integer;
begin
    x := 42;
    writeln(x)
end.' > hello.pas

java Driver hello.pas
```

Expected output:
```
42

Program finished with exit code 0
```

## Sample Programs

### Array Operations (`array.pas`)
```pascal
program arrayProgram;
var a1: array[20..30] of integer;
var c1: array['a'..'e'] of integer;

begin
    a1[23] := 3;
    a1[24] := a1[23] + 5;
    writeln(a1[24]);

    c1['b'] := 10;
    writeln(c1['b'])
end.
```

### Control Flow
```pascal
program controlFlow;
var x, y: integer;

begin
    x := 10;
    y := 5;
    
    if x > y then
        writeln(x)
    else
        writeln(y);
    
    while x > 0 do
    begin
        x := x - 1
    end;
    
    writeln(x)
end.
```

## Technical Details

### Compilation Pipeline
1. **Lexical Analysis**: Tokenizes source code into meaningful units
2. **Parsing**: Validates syntax and builds intermediate representation
3. **Code Generation**: Emits P-code instructions
4. **Simulation**: Executes P-code on virtual stack machine

### P-Code Operations
The compiler generates stack-based P-code instructions including:
- Arithmetic: `ADD`, `SUB`, `MULT`, `DIV`
- Stack operations: `PUSH`, `POP`, `DUP`, `XCHG`
- Control flow: `JMP`, `JFALSE`, `JTRUE`
- I/O: `PRINT_INT`, `PRINT_CHAR`, `PRINT_REAL`, `PRINT_NEWLINE`
- Array operations: `GET`, `PUT`

## Requirements

- Java Development Kit (JDK) 8 or higher
- No external dependencies

## Author

Pedro Schmidt - CSCI 465 (Principles of Translation)

## License

Educational project for compiler design coursework.
