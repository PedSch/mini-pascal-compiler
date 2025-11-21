# Example Programs

This directory contains working Mini Pascal programs that demonstrate the compiler's capabilities.

## Array Operations (`array.pas`)

Demonstrates array declaration and access with both integer and character indexing.

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

**Output:**
```
8
10
```

## Arithmetic Operations (`simple_arithmetic.pas`)

Shows basic arithmetic operations: addition, subtraction, and multiplication.

```pascal
program arithmetic;
var a, b, sum, diff, prod: integer;

begin
    a := 20;
    b := 4;
    
    sum := a + b;
    diff := a - b;
    prod := a * b;
    
    writeln(sum);
    writeln(diff);
    writeln(prod)
end.
```

**Output:**
```
24
-16
80
```

## Conditional Statements (`if_else.pas`)

Demonstrates if-then-else control flow.

```pascal
program conditionals;
var x, y, max: integer;

begin
    x := 15;
    y := 20;
    
    if x > y then
        max := x
    else
        max := y;
    
    writeln(max)
end.
```

**Output:**
```
20
```

## While Loop (`while_loop.pas`)

Shows loop control with a while-do statement. Computes sum of 1+2+3+4+5.

```pascal
program loops;
var i, sum: integer;

begin
    sum := 0;
    i := 1;
    
    while i < 6 do
    begin
        sum := sum + i;
        i := i + 1
    end;
    
    writeln(sum)
end.
```

**Output:**
```
15
```

## Running the Examples

To run any example:
```bash
java Driver examples/<filename>.pas
```

Or run all examples at once:
```bash
cd ..
./test.sh
```
