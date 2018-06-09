#!/bin/sh

#Generate test cases for input/output instructions

for inst in IWSR CWSR; do
  sed -e"s/\\\$/$inst/g" -e"s/\!/D/g" _io.template >`echo $inst | tr [A-Z] [a-z]`.urban
done
for inst in IRKB CRKB; do
  sed -e"s/\\\$/$inst/g" -e"s/\!/F/g" _io.template >`echo $inst | tr [A-Z] [a-z]`.urban
done
