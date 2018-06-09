#!/bin/sh

#Generate test cases for several instructions from the MOVD test case
#These should all share the same set of valid operands, so why rewrite it?

for inst in MOVD MOVDN IADD IMAD IAA ISUB IMUL IDIV AND OR; do
  sed "s/\\\$/$inst/g" _arithmetic.template >`echo $inst | tr [A-Z] [a-z]`.urban
done
