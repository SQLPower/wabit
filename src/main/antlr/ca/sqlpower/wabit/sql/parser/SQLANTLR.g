grammar SQLANTLR;

options{ 
output=AST;
}

tokens {
	PLUS 	= '+' ;
	MINUS	= '-' ;
	STAR	= '*' ;
	DIV	= '/' ;
	GT      = '>' ;
	LT      = '<' ;
	DOT     = '.' ;
	PCT 	= '%' ;
	EQ 	= '=' ;
	HAT 	= '^' ;
	GE 	= '>=' ;
	LE 	= '<=' ;
	LPAREN 	= '(' ;
	RPAREN 	= ')' ;
	
	// Imaginary tokens
	ALIAS;
	RELATION;
	RELATION_NAME;
	INDIRECTION;
	EXPR;
	FUNCTION;
	FROM_START;
	SELECT_START;
	TARGET;
	TARGETS;
	NULL_ARG;
}

@lexer::header {
package ca.sqlpower.wabit.parser;
}

@parser::header {
package ca.sqlpower.wabit.parser;
}


@members 
{ 
    public static void main(String[] args) throws Exception {
        SQLANTLR_ASTLexer lex = new SQLANTLR_ASTLexer(new ANTLRFileStream(args[0]));
       	CommonTokenStream tokens = new CommonTokenStream(lex);	
	List listTokens = tokens.getTokens();
	
		
	SQLANTLR_ASTParser parser = new SQLANTLR_ASTParser(tokens);
        try {
            parser.stmtblock();
        } catch (RecognitionException e)  {
            e.printStackTrace();
        }
    }       
} 

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
 /*------------------------------------------------------------------
 * SELECT
 *
 * Currently supported:
 *	SELECT FROM WHERE GROUP BY HAVING 
 *	Multiples, semi separated
 *	Nested Selects
 * TODO:- orderby and limit are a bit funky, need some work.
 * 	- values clause doesn't work as expected
 *	- function names: any id, or a list?
 *	- improve a_expr
 *------------------------------------------------------------------*/
 
 
stmtblock	: stmtmulti
		;

stmtmulti	: (simple_select) (';' simple_select)* (';')?
		;


select_with_parens	: LPAREN simple_select RPAREN
			;

simple_select: SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause// orderby_clause 
			-> ^(SELECT["SELECT"]opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause)// orderby_clause )
		| values_clause
		;
			
values_clause 	: VALUES '(' values_expr_list ')' (',' '(' values_expr_list ')' )*
		;
		
values_expr_list: (values_expr) (',' values_expr)*
		;

values_expr	: a_expr
		| DEFAULT	
		;

/*------------------------------------------------------------------
 * DISTINCT
 * 	Works under Postgres platform, need to check others.
 *------------------------------------------------------------------*/

opt_distinct	: DISTINCT 
		| DISTINCT ON LPAREN expr_list RPAREN 
		| ALL 
		| //EMPTY  
		;

expr_list       : (a_expr) (',' a_expr)* -> a_expr+
		;

/*------------------------------------------------------------------
 * TARGET LIST
 *	Works under Postgres platform, need to check others.
 *	TODO: Support aliasing to keywords (in target:), and without as keyword. See alias_clause.
 *------------------------------------------------------------------*/

target_list     : target (',' target)* -> ^(TARGETS target+)	
		;

target	: a_expr target_alias? -> ^(TARGET a_expr target_alias?)
	| STAR -> ^(TARGET STAR)
	;
	
target_alias	: (AS id) => AS	id -> ^(ALIAS id)
		| (id) => id -> ^(ALIAS id)
		;

/*------------------------------------------------------------------
 * INTO
 *	Works under Postgres platform, need to check others.
 * 	TODO: support for arrays in postgres '[' a_expr ']' | '[' a_expr ':' a_expr ']'  See indirection:
 *------------------------------------------------------------------*/

into_clause	: INTO opt_temp_table_name 
		| /*EMPTY*/  
		;

opt_temp_table_name	: TEMPORARY opt_table qualified_name 
			| TEMP opt_table qualified_name 
			| LOCAL TEMPORARY opt_table qualified_name 
			| LOCAL TEMP opt_table qualified_name
			| GLOBAL TEMPORARY opt_table qualified_name 
			| GLOBAL TEMP opt_table qualified_name 
			| TABLE qualified_name 
			| qualified_name 
			;
			
opt_table	: TABLE 
		| //EMPTY 
		;

qualified_name	: relation_name indirection* -> ^(RELATION_NAME relation_name indirection*)
		;

relation_name	: id  
		| special_rule_relation 
		; 

special_rule_relation	: OLD 
			| NEW 
			;


indirection	: DOT id ->^(INDIRECTION id)
		| DOT STAR -> ^(INDIRECTION STAR)
		; 	

/*------------------------------------------------------------------
 * FROM
 *	Supports : from table, table, table
 *		   from table join join
 *		   nested select statements
 *		   functions
 *------------------------------------------------------------------*/

from_clause	: FROM from_list -> ^(FROM["FROM"] from_list)
		| //EMPTY 
		;
 
 //Syntactic predicates are used to determine if the list is comma seperated or not. Either one or the other is possible, not both.
from_list 	: table_joins
		| table_refs
		;

table_refs 	: table_ref (',' table_ref )*
		;

table_joins 	: relation_expr alias_clause? table_join+ 
		;
 
table_ref 	: (func_expr alias_clause?) => func_expr alias_clause? -> ^(FUNCTION func_expr alias_clause?)
		| relation_expr alias_clause? -> ^(RELATION relation_expr alias_clause?)
		| (LPAREN inner_select RPAREN alias_clause?)  => LPAREN inner_select RPAREN alias_clause?
		; 
		
inner_select 	: simple_select 
		;

table_join 	: joined_table -> ^(JOIN["JOIN"] joined_table)
		| '(' joined_table ')' alias_clause 
		;

joined_table 	: CROSS JOIN table_ref
		| join_type JOIN table_ref join_qual
		| JOIN table_ref join_qual
		| NATURAL join_type JOIN table_ref
		| NATURAL JOIN table_ref
		;
				
join_type	: FULL join_outer 
		| LEFT join_outer 
		| RIGHT join_outer 
		| INNER 
		; 		

// OUTER is just noise... 
join_outer	: OUTER 
		| //EMPTY
		;

join_qual	: USING '(' name_list ')' 
		| ON a_expr 
		;

// Syntactic predicates are used to differentiate between "table AS id" and "table id" being valid alias statements
opt_alias 	: (alias_clause) => alias_clause 
		| //EMPTY
		;
				 				
alias_clause	: (id) => id -> ^(ALIAS id)
		| AS id -> ^(ALIAS id )		
		; 
		
name_list	: id (',' id)* -> id+
		;
		
relation_expr	: qualified_name STAR? 
		| ONLY  qualified_name 
		| ONLY '(' qualified_name ')'
		; 

// For now, function names come from a list of possible functions below. This may change if the need arises.
// the VARIADIC keyword/arguments syntax have been ommitted
func_expr 	:  func_name '(' func_args ')'  -> ^(func_name func_args)
		; 

func_args	: (ALL | DISTINCT)? expr_list
		| STAR
			;
				
func_name	: id //AVG |/* COUNT*/  FIRST | LAST | MAX | MIN | NVL | SUM | UCASE | LCASE | MID | LEN | ROUND | NOW | FORMAT 
		;

/*------------------------------------------------------------------
 * EXPRESSIONS
 *	Operator precedence is given in order of increasing presedence, as per the documentation for PostgreSQL, which this application intends to follow.
 *  	OR, AND, NOT, = , < > , LIKE ILIKE SIMILAR TO, (any other), NOTNULL ISNULL IS, +-, *%/, ^
 *	Note: the repetition here is for the sake of simplicity 	
 *------------------------------------------------------------------*/

a_expr		: b_expr ((or=OR) b_expr)* -> {$or==null}? b_expr+
					 -> ^(OR["OR"] b_expr+)
		;	
		
b_expr 		: c_expr ((and=AND) c_expr)* -> {$and==null}? c_expr+
					     -> ^(AND["AND"] c_expr+)
		;
		
c_expr		: (NOT^)* d_expr  
		;
		
d_expr 		: e_expr (EQ ^ e_expr)* 
		;
		
e_expr 		: f_expr ((GT ^ | LT ^) f_expr)*
		;
		
f_expr		: g_expr ((LIKE ^ | ILIKE ^ | SIMILAR TO^ ) g_expr)*
		;
		
g_expr 		: h_expr (binary_op^ h_expr)*
		;
		
h_expr		: i_expr ( NOTNULL ^| IS NULL ^| ISNULL ^| IS NOT NULL ^)*
		;
		
i_expr		: j_expr ((PLUS ^| MINUS ^) j_expr)*
		;
		
j_expr 		: k_expr ((STAR ^| DIV ^| PCT ^) k_expr)*	
		;
		
k_expr 		: l_expr (HAT l_expr)*
		;
//select_with_parens needs a syntactic predicate to help the parser distinguish between that and " ( a_expr ) "
l_expr		: (func_expr) => func_expr -> ^(FUNCTION func_expr)
		| qualified_name
		| '(' a_expr ')' -> ^(EXPR["EXPR"] a_expr )
		| (select_with_parens) => select_with_parens
		| EXISTS select_with_parens 
		| ARRAY select_with_parens 
		| factor
		| QUOTEDSTRING
		;
				
unary_op 	: NOTNULL | (IS NULL) | ISNULL | (IS NOT NULL)
		;
		
binary_op 	: GE | LE 
		;

/*------------------------------------------------------------------
 * WHERE, GROUP BY, HAVING
 *	The syntax for these three is extremely similar, and is largely dealt with using the expression syntax, or lists thereof.  
 *------------------------------------------------------------------*/

where_clause 	: WHERE a_expr -> ^(WHERE["WHERE"] a_expr)
		| //EMPTY  
		;

groupby_clause  : GROUP BY expr_list  -> ^(GROUP BY expr_list)
		| //EMPTY  
		;
	
having_clause 	: HAVING a_expr -> ^(HAVING a_expr)
		| //EMPTY  
		;

orderby_clause  : ORDER BY (DESC|ASC)? 
		| //EMPTY 
		;

opt_limit  	: limit 
		| //EMPTY
		;	

limit 		: LIMIT NUMBER (OFFSET NUMBER)? 
		| OFFSET NUMBER (LIMIT NUMBER)?
		| LIMIT NUMBER ',' NUMBER  
		;
			
id 		: ID 
		; 

factor		: NUMBER 
		;

/*------------------------------------------------------------------
 * LEXER RULES
 * 	Anything that could appear as a keyword is strung out as a set comparison. This allows 
 *	select, SELECT and SeLeCT to all be matched as the same keyword.
 *------------------------------------------------------------------*/
ALL 	: ('A'|'a')('L'|'l')('L'|'l');
AND 	: ('A'|'a')('N'|'n')('D'|'d');
ARRAY 	: ('A'|'a')('R'|'r')('R'|'r')('A'|'a')('Y'|'y');
AS 	: ('A'|'a')('S'|'s');
ASC	: ('A'|'a')('S'|'s')('C'|'c');
//AVG     : ('A'|'a')('V'|'v')('G'|'g') ;
BY      : ('B'|'b')('Y'|'y');
//COUNT   : ('C'|'c')('O'|'o')('U'|'u')('N'|'n')('T'|'t') ;
CROSS   : ('C'|'c')('R'|'r')('O'|'o')('S'|'s')('S'|'s') ;
DEFAULT : ('D'|'d')('E'|'e')('F'|'f')('A'|'a')('U'|'u')('L'|'l')('T'|'t') ;
DESC 	: ('D'|'d')('E'|'e')('S'|'s')('C'|'c');
DISTINCT: ('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t') ;
EXISTS	: ('E'|'e')('X'|'x')('I'|'i')('S'|'s')('T'|'t')('S'|'s');
//FIRST   : ('F'|'f')('I'|'i')('R'|'r')('S'|'s')('T'|'t') ;
//FORMAT  : ('F'|'f')('O'|'o')('R'|'r')('M'|'m')('A'|'a')('T'|'t') ;
FROM 	: ('F'|'f')('R'|'r')('O'|'o')('M'|'m');
FULL    : ('F'|'f')('U'|'u')('L'|'l')('L'|'l') ;
GLOBAL	: ('G'|'g')('L'|'l')('O'|'o')('B'|'b')('A'|'a')('L'|'l') ;
GROUP 	: ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p') ;
HAVING 	: ('H'|'h')('A'|'a')('V'|'v')('I'|'i')('N'|'n')('G'|'g') ;
ILIKE   : ('I'|'i')('L'|'l')('I'|'i')('K'|'k')('E'|'e') ;
INTO 	: ('I'|'i')('N'|'n')('T'|'t')('O'|'o');
INNER   : ('I'|'i')('N'|'n')('N'|'n')('E'|'e')('R'|'r') ;
IS      : ('I'|'i')('S'|'s') ;
ISNULL  : ('I'|'i')('S'|'s')('N'|'n')('U'|'u')('L'|'l')('L'|'l') ;
JOIN    : ('J'|'j')('O'|'o')('I'|'i')('N'|'n') ;
//LAST    : ('L'|'l')('A'|'a')('S'|'s')('T'|'t') ;
//LCASE   : ('L'|'l')('C'|'c')('A'|'a')('S'|'s')('E'|'e') ;
LEFT    : ('L'|'l')('E'|'e')('F'|'f')('T'|'t') ;
LEN     : ('L'|'l')('E'|'e')('N'|'n') ;
LIKE    : ('L'|'l')('I'|'i')('K'|'k')('E'|'e') ;
LIMIT 	: ('L'|'l')('I'|'i')('M'|'m')('I'|'i')('T'|'t') ;
LOCAL	: ('L'|'l')('O'|'o')('C'|'c')('A'|'a')('L'|'l') ;
//MAX     : ('M'|'m')('A'|'a')('X'|'x') ;
//MID     : ('M'|'m')('I'|'i')('D'|'d') ;
//MIN     : ('M'|'m')('I'|'i')('N'|'n') ;
NATURAL : ('N'|'n')('A'|'a')('T'|'t')('U'|'u')('R'|'r')('A'|'a')('L'|'l') ;
NEW     : ('N'|'n')('E'|'e')('W'|'w') ;
//NOW     : ('N'|'n')('O'|'o')('W'|'w') ;
NOT     : ('N'|'n')('O'|'o')('T'|'t') ;
NOTNULL : ('N'|'n')('O'|'o')('T'|'t')('N'|'n')('U'|'u')('L'|'l')('L'|'l') ;
NULL    : ('N'|'n')('U'|'u')('L'|'l')('L'|'l') ;
//NVL     : ('N'|'n')('V'|'v')('L'|'l') ;
OFFSET 	: ('O'|'o')('F'|'f')('F'|'f')('S'|'s')('E'|'e')('T'|'t');
OLD     : ('O'|'o')('L'|'l')('D'|'d') ;
ON      : ('O'|'o')('N'|'n') ;
ONLY    : ('O'|'o')('N'|'n')('L'|'l')('Y'|'y');
OR      : ('O'|'o')('R'|'r') ;
ORDER   : ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r') ;
OUTER   : ('O'|'o')('U'|'u')('T'|'t')('E'|'e')('R'|'r') ;
RIGHT   : ('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t') ;
//ROUND   : ('R'|'r')('O'|'o')('U'|'u')('N'|'n')('D'|'d') ;
SELECT 	: ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');
SIMILAR : ('S'|'s')('I'|'i')('M'|'m')('I'|'i')('L'|'l')('A'|'a')('R'|'r') ;
//SUM     : ('S'|'s')('U'|'u')('M'|'m') ;
TABLE 	: ('T'|'t')('A'|'a')('B'|'b')('L'|'l')('E'|'e');
TEMP	: ('T'|'t')('E'|'e')('M'|'m')('P'|'p');
TEMPORARY	: ('T'|'t')('E'|'e')('M'|'m')('P'|'p')('O'|'o')('R'|'r')('A'|'a')('R'|'r')('Y'|'y') ;
TO      : ('T'|'t')('O'|'o') ;
//UCASE   : ('U'|'u')('C'|'c')('A'|'a')('S'|'s')('E'|'e') ;
USING   : ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('G'|'g') ;
VALUES  : ('V'|'v')('A'|'a')('L'|'l')('U'|'u')('E'|'e')('S'|'s') ;
VARIADIC: ('V'|'v')('A'|'a')('R'|'r')('I'|'i')('A'|'a')('D'|'d')('I'|'i')('C'|'c');
WHERE 	: ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

//basic id, number, quotation and whitspace rules
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ 	{ $channel = HIDDEN; } ;
QUOTEDSTRING :  ('\'' | '$$') ( options{greedy=false;} : ( ~('\'' | '$$') ) )*  ('\'' | '$$') ;
QUOTEID : ('['ID']'|'"'ID'"') ;
NUMBER	: (DIGIT)+ ;
ID      : LETTER ( LETTER | NUMBER | '_' )* ;	

//quote and character fragments
fragment DIGIT	: '0'..'9' ;
fragment LETTER : ('a'..'z' | 'A'..'Z') ;

//comments
SL_COMMENT	: '--' (~('\n'|'\r'))* ('\n'| EOF |'\r'('\n')?)  { $channel = HIDDEN; } ;
ML_COMMENT      : '/*' (options {greedy=false;} : .)* '*/' { $channel = HIDDEN; } ; 
