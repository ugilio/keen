---
title: DDL Language Grammar
hide_sidebar: true
toc: false
permalink: ddl
---
This is the definition of the "DDL.3" language as it is understood by <span class="sc">KeeN</span>, in EBNF form.

```ebnf
(* WHITESPACE, ML_COMMENT and SL_COMMENT may occur anywhere *)

Ddl = (Domain [Problem]) | Problem;

Domain = 'DOMAIN' ID '{' TemporalModule {DomainElement} '}';

TemporalModule = 'TEMPORAL_MODULE' ID '='
    '[' PosNumber ',' PosNumber ']' ',' INT ';';

DomainElement = ParType | ComponentType | Component | TimelineSynchronization;

ParType = NumericParameterType | EnumerationParameterType;

NumericParameterType = 'PAR_TYPE' 'NumericParameterType' ID '=' URange ';';

EnumerationParameterType = 'PAR_TYPE' 'EnumerationParameterType' ID '='
    '{' EnumLiteral {',' EnumLiteral} '}' ';';

ParameterConstraint = LValue AritComparisonOperator RValue;

LValue = [Number '*'] VarRef;
RValue = (LValue | Number | EnumLiteral)
    { ('+' | '-') ([INT '*'] VarRef | INT) };

AritComparisonOperator = '=' | '!=' | '<' | '<=' | '>' | '>=';

ComponentType = 
    StateVariableComponentType |
    RenewableResourceComponentType |
    ConsumableResourceComponentType;

StateVariableComponentType =
    'COMP_TYPE' StateVariableType ID
    '(' [ ComponentDecisionType {',' ComponentDecisionType} ] ')'
    '{' {TransitionConstraint} '}';

StateVariableType = SingletonStateVariable | SimpleGroundStateVariable;

ComponentDecisionType = ID '(' [ ID {',' ID} ] ')';

TransitionConstraint =
    'VALUE' SVComponentDecision Range
    'MEETS' '{' { TransitionElement ';' } '}';

TransitionElement =
    SVComponentDecision | ParameterConstraint;

RenewableResourceComponentType =
    'COMP_TYPE' 'RenewableResource' ID '(' PosNumber ')';

ConsumableResourceComponentType =
    'COMP_TYPE' 'ConsumableResource' ID '(' PosNumber ',' PosNumber ')';

Component =
    'COMPONENT' ID '{' {Timeline} '}' ':' ComponentType ';';

Timeline = TimelineType ID '(' [ ID {',' ID} ] ')';

TimelineType =
    'FLEXIBLE' | 'BOUNDED' | 'ESTA_LIGHT' | 'ESTA_LIGHT_MAX_CONSUMPTION';

TimelineSynchronization =
    'SYNCHRONIZE' QualifiedName
    '{' {Synchronization} '}';

Synchronization =
    'VALUE' ComponentDecision
    '{' {SynchronizationElement} '}';

ComponentDecision = 
    SVComponentDecision |
    RenewableResourceComponentDecision |
    ConsumableResourceComponentDecision;

SVComponentDecision =
    [ '<' Parameter {',' Parameter} '>' ]
    ID
    '(' [ ParValue {',' ParValue} ] ')';

RenewableResourceComponentDecision =
    [ '<' Parameter {',' Parameter} '>' ]
    'REQUIREMENT' '(' ParValue ')';
    
ConsumableResourceComponentDecision =
    CRProductionComponentDecision | CRConsumptionComponentDecision;

CRProductionComponentDecision =
    [ '<' Parameter {',' Parameter} '>' ] 'PRODUCTION' '(' ParValue ')';

CRConsumptionComponentDecision =
    [ '<' Parameter {',' Parameter} '>' ] 'CONSUMPTION' '(' ParValue ')';

SynchronizationElement =
    InstantiatedComponentDecision ';' |
    TemporalConstraint ';' |
    ParameterConstraint ';';

InstantiatedComponentDecision =
    ID [ '<' Parameter {',' Parameter} '>' ]
    QualifiedName2 '.' ComponentDecision
    [ 'AT' Range Range Range ];

TemporalConstraint =
    [ID] TemporalRelationType ID;

Parameter = ID | '!' | '?' | 'c' | 'u';

ParValue = VARID [ '=' ( Number | ID ) ];

(* Temporal relations *)
TemporalRelationType =
     'MEETS'
    |'MET-BY'
    |'BEFORE' Range
    |'AFTER' Range
    |'EQUALS'
    |'STARTS'
    |'STARTED-BY'
    |'FINISHES'
    |'FINISHED-BY'
    |'DURING' Range Range
    |'CONTAINS' Range Range
    |'OVERLAPS' Range
    |'OVERLAPPED-BY' Range
    |'STARTS-AT'
    |'ENDS-AT'
    |'AT-START'
    |'AT-END'
    |'BEFORE-START' Range
    |'AFTER-END' Range
    |'START-START' Range
    |'START-END' Range
    |'END-START' Range
    |'END-END' Range
    |'CONTAINS-START' Range Range
    |'CONTAINS-END' Range Range
    |'STARTS-DURING' Range Range
    |'ENDS-DURING' Range Range;

Problem = 'PROBLEM' ID [ '(' 'DOMAIN' ID ')' ]
    '{' { ProblemElement } '}';

ProblemElement =
    InstantiatedComponentDecision ';' |
    ProblemTemporalConstraint ';' |
    ParameterConstraint ';';

ProblemTemporalConstraint =
    ID TemporalRelationType ID;

VarRef = VARID;
EnumLiteral = ID;

QualifiedName = ID{'.'ID};
QualifiedName2 = ID'.'ID;

URange      = '[' Number ',' Number ']';
Range       = '[' PosNumber ',' PosNumber ']';
Number      = PosNumber | NegNumber;
PosNumber   = ['+'] (INT |'INF');
NegNumber   = '-' (INT |'INF');

VARID = '?' ID;

(* Terminals *)

ID    = ('a'..'z'|'A'..'Z'|'_') {'a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-'|'@'};
INT   = ('0'..'9') {'0'..'9'};

ML_COMMENT = ? '/*' -> '*/' ?;
SL_COMMENT = '//' { ANYCHAR - ('\n'|'\r') } [['\r'] '\n'];

WHITESPACE  = (' '|'\t'|'\r'|'\n') {' '|'\t'|'\r'|'\n'};

ANYCHAR     = ? any ASCII character ?
```
