There are several templates used to generate similar sets of tests
automatically.  For eaxmple, calling _GenerateArithmetic.sh will create tests
for all of MOVD, MOVDN, IADD, IMAD, IAA, ISUB, IMUL, and IDIV from
_arithmetic.template.  Since these all accept the same arguments, this makes
writing tests faster.  It also improves coverage by preventing the need to
re-implement the same test 8 times.

The following templates are used:
    _arithmetic.template: MOVD MOVDN IADD IMUL IAA ISUB IMUL IDIV
    _constant.template: PWR ISHR ISHL ISRA ISLA ROL ROR
    _noarg.template: CLRA CLRX NOP
    _transfer.template: TREQ TRLT TRGT TRDR
    _io.template: IWSR IRKB CWSR CRKB

The following instructions are individually hand-written:
    ISRG CLR TR TRLK RET SKT PSH POP PST DMP HLT
