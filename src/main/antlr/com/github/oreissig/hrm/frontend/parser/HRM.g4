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
           | bumpup
           | bumpdown
           | label
           | jump
           | jumpz
           | jumpn
           | dump
           ;

inbox    : 'INBOX';
outbox   : 'OUTBOX';
copyfrom : 'COPYFROM' NUMBER;
copyto   : 'COPYTO' NUMBER;
add      : 'ADD' NUMBER;
sub      : 'SUB' NUMBER;
bumpup   : 'BUMPUP' NUMBER;
bumpdown : 'BUMPDN' NUMBER;
label    : ID ':';
jump     : 'JUMP' ID;
jumpz    : 'JUMPZ' ID;
jumpn    : 'JUMPN' ID;
dump     : 'DUMP';

ID      : [a-z]+ ;
NUMBER  : [0-9]+ ;
// skip spaces, tabs, newlines, brackets (used by asm)
WS      : [ \t\r\n\[\]]+ -> skip;
Comment : ('...'|'--'|'COMMENT') ~('\n')* -> skip;
Blob    : 'DEFINE ' .*? ';' -> skip;
