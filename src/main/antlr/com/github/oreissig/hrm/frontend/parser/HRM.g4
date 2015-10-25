// Define a grammar called Hello
grammar HRM;

@header {
package com.github.oreissig.hrm.frontend.parser;
}

program : statement*;

statement : expression '\r'? '\n';

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
           ;

inbox     : 'inbox';
outbox    : 'outbox';
copyfrom  : 'copyfrom' NUMBER;
copyto    : 'copyto' NUMBER;
add       : 'add' NUMBER;
sub       : 'sub' NUMBER;
increment : 'bump+' NUMBER;
decrement : 'bump-' NUMBER;
label     : ':' ID;
jump      : 'jump' ID;
jumpzero  : 'jump' 'if' 'zero' ID;
jumpneg   : 'jump' 'if' 'negative' ID;

ID     : [a-z]+ ;
NUMBER : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
Comment : '...' ~( '\r' | '\n' )* -> skip;
