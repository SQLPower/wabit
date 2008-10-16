grammar SQLANTLR;


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
	
	/* **************************** */
	//see rule for a_expr in  http://anoncvs.postgresql.org/cvsweb.cgi/pgsql/src/backend/parser/gram.y?rev=2.625;content-type=text%2Fplain
}


@members 
{ 
    public static void main(String[] args) throws Exception {
        SQLANTLRLexer lex = new SQLANTLRLexer(new ANTLRFileStream(args[0]));
       	
       	//ParseTreeBuilder builder = new ParseTreeBuilder("stmtblock");
       	CommonTokenStream tokens = new CommonTokenStream(lex);	

	List listTokens = tokens.getTokens();
	/*for(int i = 0; i < listTokens.size(); i++) {
		Token tok = (Token) listTokens.get(i);
	
		if ( tok.getType() == 20 ){
			System.out.print("//"+tok.getText()+"\\");
		}else if ( tok.getChannel() == HIDDEN ) {
			System.out.print("..."+tok.getText()+"..." );
		}else {
			System.out.print(tok.getText() );
		}
	}*/
		
	SQLANTLRParser parser = new SQLANTLRParser(tokens);//, builder);

        try {
        
            parser.stmtblock();
            //System.out.println(builder.getTree().toStringTree());
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
 *------------------------------------------------------------------*/
stmtblock	: stmtmulti 
		;

stmtmulti	: (select) (';' select)* (';')?
		;

select    	: select_no_parens 
		| select_with_parens 
		;

select_with_parens	: '(' select_no_parens ')' 
			| '(' select_with_parens ')' 
			;

select_no_parens 	: SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause opt_limit 
			;

/*------------------------------------------------------------------
 * DISTINCT
 *------------------------------------------------------------------*/

opt_distinct	: DISTINCT 
		| DISTINCT ON '(' expr_list ')' 
		| ALL 
		| //EMPTY  
		;

expr_list       : (a_expr) (',' a_expr)* 
		;

/*------------------------------------------------------------------
 * TARGET LIST
 *------------------------------------------------------------------*/

target_list     : (target) (',' target)*	
		;

target	: (a_expr ( AS id )? | STAR) 
	; //TODO Support aliasing to keywords, and without as keyword. See alias_clause.


/*------------------------------------------------------------------
 * INTO
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
		| /*EMPTY*/ 
		;

qualified_name	: relation_name indirections? 
		;

relation_name	: id  
		| special_rule_relation 
		; 

special_rule_relation	: OLD 
			| NEW 
			;

indirections	: (indirection) (indirection)* 
		;

indirection	: DOT id 
		| DOT STAR 
		; //TODO support for arrays in postgres '[' a_expr ']' | '[' a_expr ':' a_expr ']' ;	

columnref	: relation_name indirections?
		;

/*------------------------------------------------------------------
 * FROM
 *------------------------------------------------------------------*/

from_clause	: FROM from_list 
		| //EMPTY 
		;
 
from_list 	: ( table_ref (',' table_ref )+ )=> refs_list
		| ( table_ref table_join+ )=> table_joins
		| ( table_ref ) => table_ref 
		;
   
refs_list	: table_ref (',' table_ref )+	
		;
		
table_joins 	: table_ref table_join+ 
		;
 
table_ref 	: relation_expr opt_alias
		| func_expr opt_alias
		| select_with_parens opt_alias
		; 

table_join 	: joined_table
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
		
opt_alias 	: (alias_clause) => alias_clause 
		| //EMPTY
		;
				 				
alias_clause	: AS id ('(' name_list ')')?
		| (id ('(' name_list ')')? )=> names		
		; 

names		: id '(' name_list ')'
		| id  
		;
		
name_list	: (id) (',' id)* 
		;

func_expr 	: ( func_name ) => func_name '(' 
		( ')' 
		| expr_list ( ')' | ',' VARIADIC a_expr ')')
		| VARIADIC a_expr ')' 
		| ALL expr_list ')' 
		| DISTINCT expr_list ')' 
		| STAR ')'
		)
		; 
				
func_name	: id //| AVG | COUNT | FIRST | LAST | MAX | MIN | NVL | SUM | UCASE | LCASE | MID | LEN | ROUND | NOW | FORMAT 
		;
				
relation_expr	: qualified_name STAR? 
		| ONLY  qualified_name 
		| ONLY '(' qualified_name ')' 
		;

/*------------------------------------------------------------------
 * EXPRESSIONS
 *------------------------------------------------------------------*/
//For this application, precedence of operators is being ignored; to actually be able to execute expressions, more work needs to be done.
a_expr		: c_expr ( binary_op c_expr)*		
		; 
		
binary_op 	: STAR | PLUS | DIV | MINUS | PCT | GT | LT | EQ | HAT | AND | OR | LIKE | NOT LIKE | ILIKE | NOT ILIKE | SIMILAR TO | NOT SIMILAR TO 
		;

b_expr		: 	;

c_expr		: columnref 
		|'(' a_expr ')'
		| func_expr 
		| (select_with_parens) => select_with_parens
		| EXISTS select_with_parens 
		| ARRAY select_with_parens 
		| factor
		| QUOTEDSTRING
		;

where_clause 	: ( WHERE a_expr | /*EMPTY */ );

having_clause 	: ( HAVING  | /*EMPTY */ );	

groupby_clause  : ( GROUP BY  | /*EMPTY */ ) ;	

orderby_clause  : ( ORDER BY  (DESC|ASC| ) | /*EMPTY*/ ) ;

opt_limit  	: ( limit | /*EMPTY*/ ) ;	

limit 		: LIMIT NUMBER OFFSET NUMBER 
		| OFFSET NUMBER LIMIT NUMBER
		| LIMIT NUMBER 
		| OFFSET NUMBER
		| LIMIT NUMBER ',' NUMBER  ;
			
id 		: ID ; //TODO support quoted ids| QUOTEID;

//ids 		: ( (QUOTEID | id | aliasid) ( alias_clause | ) ( ',' (QUOTEID | id | aliasid) ( alias_clause | )  )*  ) ;

//and 		: AND comp ;

//comp 		: expr (EQUALS | GT | LT) expr ;

/* **************************** */
//need a general-purpose a_expr statement, like in http://anoncvs.postgresql.org/cvsweb.cgi/pgsql/src/backend/parser/gram.y?rev=2.625;content-type=text%2Fplain

//expr		: term ( ( PLUS | MINUS )  term )* ;

//term		: ( factor | aliasid | ID | QUOTEDSTRING ) ( ( STAR | DIV ) ( factor | aliasid | ID | QUOTEDSTRING ) )* ;

factor		: NUMBER ;

//ident 		: id;

//keyword         : ( ALL | AND | ARRAY | AS | ASC | AVG | BY | COUNT | CROSS | DESC | DISTINCT | EQUALS | EXISTS | FIRST | FORMAT | FROM | FULL | GLOBAL | GROUP | HAVING | INNER | INTO | JOIN | LAST | LCASE | LEFT | LEN | LIMIT | LOCAL | MAX | MID | MIN | NATURAL | NEW | NOW | NUMBER | OFFSET | OLD | ON | ONLY | ORDER | OUTER | RIGHT | ROUND | SELECT | SUM | TABLE | TEMP | TEMPORARY | UCASE | USING | VARIADIC | WHERE ) ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

ALL 	: ('A'|'a')('L'|'l')('L'|'l');
AND 	: ('A'|'a')('N'|'n')('D'|'d');
ARRAY 	: ('A'|'a')('R'|'r')('R'|'r')('A'|'a')('Y'|'y');
AS 	: ('A'|'a')('S'|'s');
ASC	: ('A'|'a')('S'|'s')('C'|'c');
AVG     : ('A'|'a')('V'|'v')('G'|'g') ;
BY      : ('B'|'b')('Y'|'y');
COUNT   : ('C'|'c')('O'|'o')('U'|'u')('N'|'n')('T'|'t') ;
CROSS   : ('C'|'c')('R'|'r')('O'|'o')('S'|'s')('S'|'s') ;
DESC 	: ('D'|'d')('E'|'e')('S'|'s')('C'|'c');
DISTINCT: ('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t') ;
EXISTS	: ('E'|'e')('X'|'x')('I'|'i')('S'|'s')('T'|'t')('S'|'s');
FIRST   : ('F'|'f')('I'|'i')('R'|'r')('S'|'s')('T'|'t') ;
FORMAT  : ('F'|'f')('O'|'o')('R'|'r')('M'|'m')('A'|'a')('T'|'t') ;
FROM 	: ('F'|'f')('R'|'r')('O'|'o')('M'|'m');
FULL    : ('F'|'f')('U'|'u')('L'|'l')('L'|'l') ;
GLOBAL	: ('G'|'g')('L'|'l')('O'|'o')('B'|'b')('A'|'a')('L'|'l') ;
GROUP 	: ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p') ;
HAVING 	: ('H'|'h')('A'|'a')('V'|'v')('I'|'i')('N'|'n')('G'|'g') ;
ILIKE   : ('I'|'i')('L'|'l')('I'|'i')('K'|'k')('E'|'e') ;
INTO 	: ('I'|'i')('N'|'n')('T'|'t')('O'|'o');
INNER   : ('I'|'i')('N'|'n')('N'|'n')('E'|'e')('R'|'r') ;
JOIN    : ('J'|'j')('O'|'o')('I'|'i')('N'|'n') ;
LAST    : ('L'|'l')('A'|'a')('S'|'s')('T'|'t') ;
LCASE   : ('L'|'l')('C'|'c')('A'|'a')('S'|'s')('E'|'e') ;
LEFT    : ('L'|'l')('E'|'e')('F'|'f')('T'|'t') ;
LEN     : ('L'|'l')('E'|'e')('N'|'n') ;
LIKE    : ('L'|'l')('I'|'i')('K'|'k')('E'|'e') ;
LIMIT 	: ('L'|'l')('I'|'i')('M'|'m')('I'|'i')('T'|'t') ;
LOCAL	: ('L'|'l')('O'|'o')('C'|'c')('A'|'a')('L'|'l') ;
MAX     : ('M'|'m')('A'|'a')('X'|'x') ;
MID     : ('M'|'m')('I'|'i')('D'|'d') ;
MIN     : ('M'|'m')('I'|'i')('N'|'n') ;
NATURAL : ('N'|'n')('A'|'a')('T'|'t')('U'|'u')('R'|'r')('A'|'a')('L'|'l') ;
NEW     : ('N'|'n')('E'|'e')('W'|'w') ;
NOW     : ('N'|'n')('O'|'o')('W'|'w') ;
NOT     : ('N'|'n')('O'|'o')('T'|'t') ;
NVL     : ('N'|'n')('V'|'v')('L'|'l') ;
OFFSET 	: ('O'|'o')('F'|'f')('F'|'f')('S'|'s')('E'|'e')('T'|'t');
OLD     : ('O'|'o')('L'|'l')('D'|'d') ;
ON      : ('O'|'o')('N'|'n') ;
ONLY    : ('O'|'o')('N'|'n')('L'|'l')('Y'|'y');
OR      : ('O'|'o')('R'|'r') ;
ORDER   : ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r') ;
OUTER   : ('O'|'o')('U'|'u')('T'|'t')('E'|'e')('R'|'r') ;
RIGHT   : ('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t') ;
ROUND   : ('R'|'r')('O'|'o')('U'|'u')('N'|'n')('D'|'d') ;
SELECT 	: ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');
SIMILAR : ('S'|'s')('I'|'i')('M'|'m')('I'|'i')('L'|'l')('A'|'a')('R'|'r') ;
SUM     : ('S'|'s')('U'|'u')('M'|'m') ;
TABLE 	: ('T'|'t')('A'|'a')('B'|'b')('L'|'l')('E'|'e');
TEMP	: ('T'|'t')('E'|'e')('M'|'m')('P'|'p');
TEMPORARY	: ('T'|'t')('E'|'e')('M'|'m')('P'|'p')('O'|'o')('R'|'r')('A'|'a')('R'|'r')('Y'|'y') ;
TO      : ('T'|'t')('O'|'o') ;
UCASE   : ('U'|'u')('C'|'c')('A'|'a')('S'|'s')('E'|'e') ;
USING   : ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('G'|'g') ;
VARIADIC: ('V'|'v')('A'|'a')('R'|'r')('I'|'i')('A'|'a')('D'|'d')('I'|'i')('C'|'c');
WHERE 	: ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

QUOTEDSTRING : Q ( options{greedy=false;} : ( ~Q ) )* Q ;

QUOTEID : ('['ID']'|'"'ID'"') ;

NUMBER	: (DIGIT)+ ;

ID      : LETTER ( LETTER | NUMBER | '_')* 
	;
	
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ 	{ $channel = HIDDEN; } ;

fragment Q 	: '\'' | '$$'	;

fragment DIGIT	: '0'..'9' ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;

//HANDLES COMMENTS 
SL_COMMENT	: '--' (~('\n'|'\r'))* ('\n'| EOF |'\r'('\n')?)  { $channel = HIDDEN; }  ;

ML_COMMENT      : '/*' (options {greedy=false;} : .)* '*/' { $channel = HIDDEN; }    ;
    

