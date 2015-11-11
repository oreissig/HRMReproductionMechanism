grammar HRM;

@header {
package com.github.oreissig.hrm.frontend.parser;
}

program : statement*;

statement : expression '\n'?;

expression : inbox
           | outbox
           | copyfrom
           | copyto
           | add
           | sub
           | increment
           | decrement
           | label
           | jump
           | jumpzero
           | jumpneg
           | dump
           ;

inbox     : 'inbox'|'INBOX';
outbox    : 'outbox'|'OUTBOX';
copyfrom  : ('copyfrom'|'COPYFROM') NUMBER;
copyto    : ('copyto'|'COPYTO') NUMBER;
add       : ('add'|'ADD') NUMBER;
sub       : ('sub'|'SUB') NUMBER;
increment : ('bump+'|'BUMPUP') NUMBER;
decrement : ('bump-'|'BUMPDN') NUMBER;
label     : ID ':';
jump      : ('jump'|'JUMP') ID;
jumpzero  : ('jump' 'if' 'zero'|'JUMPZ') ID;
jumpneg   : ('jump' 'if' 'negative'|'JUMPN') ID;
dump      : 'dump'|'DUMP';

ID      : [a-z]+ ;
NUMBER  : [0-9]+ ;
// skip spaces, tabs, newlines, brackets (used by asm)
WS      : [ \t\r\n\[\]]+ -> skip;
Comment : ('...'|'--'|'COMMENT') ~('\n')* -> skip;
Blob    : 'DEFINE ' .*? ';' -> skip;
