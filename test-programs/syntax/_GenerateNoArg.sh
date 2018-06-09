#!/bin/sh

#Generate test cases for instructions that don't take arguments

for inst in CLRA CLRX NOP; do
  sed "s/\\\$/$inst/g" _noarg.template >`echo $inst | tr [A-Z] [a-z]`.urban
done
