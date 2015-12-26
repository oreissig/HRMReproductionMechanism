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
           | comment
           ;

inbox    : 'INBOX';
outbox   : 'OUTBOX';
copyfrom : 'COPYFROM' address;
copyto   : 'COPYTO' address;
add      : 'ADD' address;
sub      : 'SUB' address;
bumpup   : 'BUMPUP' address;
bumpdown : 'BUMPDN' address;
label    : ID ':';
jump     : 'JUMP' ID;
jumpz    : 'JUMPZ' ID;
jumpn    : 'JUMPN' ID;
dump     : 'DUMP';
comment  : 'COMMENT' NUMBER;

address      : directAddr | indirectAddr;
directAddr   : NUMBER;
indirectAddr : '[' NUMBER ']';

ID      : [a-z]+ ;
NUMBER  : [0-9]+ ;
// skip spaces, tabs, newlines
WS      : [ \t\r\n]+ -> skip;
Comment : '--' ~('\n')* -> skip;
Blob    : 'DEFINE ' .*? ';' -> skip;
