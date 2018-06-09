#!/bin/sh

#Generate test cases for several instructions from the MOVD test case
#These should all share the same set of valid operands, so why rewrite it?

for inst in TREQ TRLT TRGT TRDR; do
  sed "s/\\\$/$inst/g" _transfer.template >`echo $inst | tr [A-Z] [a-z]`.urban
done
