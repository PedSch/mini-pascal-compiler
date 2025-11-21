#!/bin/bash

# Test script for Mini Pascal Compiler
# This script compiles the Java source files and runs test cases

echo "========================================="
echo "  Mini Pascal Compiler - Test Suite"
echo "========================================="
echo ""

# Clean and compile
echo "Building compiler..."
rm -f *.class
javac *.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✓ Compilation successful"
echo ""

# Test each example
total_tests=0
passed_tests=0

for file in examples/*.pas; do
    total_tests=$((total_tests + 1))
    echo "Testing: $(basename "$file")"
    
    output=$(java Driver "$file" 2>&1)
    exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo "✓ PASS"
        passed_tests=$((passed_tests + 1))
    else
        echo "❌ FAIL"
        echo "$output"
    fi
    echo ""
done

# Summary
echo "========================================="
echo "  Results: $passed_tests/$total_tests tests passed"
echo "========================================="

if [ $passed_tests -eq $total_tests ]; then
    echo "✓ All tests passed!"
    exit 0
else
    echo "❌ Some tests failed"
    exit 1
fi
