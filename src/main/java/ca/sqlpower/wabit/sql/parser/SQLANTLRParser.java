// $ANTLR 3.0.1 /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g 2008-10-20 11:21:23

package ca.sqlpower.wabit.sql.parser;


import java.util.HashMap;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
public class SQLANTLRParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PLUS", "MINUS", "STAR", "DIV", "GT", "LT", "DOT", "PCT", "EQ", "HAT", "GE", "LE", "SELECT", "VALUES", "DEFAULT", "DISTINCT", "ON", "ALL", "AS", "INTO", "TEMPORARY", "TEMP", "LOCAL", "GLOBAL", "TABLE", "OLD", "NEW", "FROM", "CROSS", "JOIN", "NATURAL", "FULL", "LEFT", "RIGHT", "INNER", "OUTER", "USING", "ONLY", "VARIADIC", "AVG", "COUNT", "FIRST", "LAST", "MAX", "MIN", "NVL", "SUM", "UCASE", "LCASE", "MID", "LEN", "ROUND", "NOW", "FORMAT", "AND", "OR", "LIKE", "NOT", "ILIKE", "SIMILAR", "TO", "EXISTS", "ARRAY", "QUOTEDSTRING", "WHERE", "GROUP", "BY", "HAVING", "ORDER", "DESC", "ASC", "LIMIT", "NUMBER", "OFFSET", "ID", "WHITESPACE", "QUOTEID", "DIGIT", "LETTER", "SL_COMMENT", "ML_COMMENT", "';'", "'('", "')'", "','"
    };
    public static final int WHERE=68;
    public static final int LT=9;
    public static final int STAR=6;
    public static final int INNER=38;
    public static final int ORDER=72;
    public static final int LETTER=82;
    public static final int LIMIT=75;
    public static final int ONLY=41;
    public static final int ILIKE=62;
    public static final int LCASE=52;
    public static final int NEW=30;
    public static final int MAX=47;
    public static final int TABLE=28;
    public static final int NOW=56;
    public static final int COUNT=44;
    public static final int NOT=61;
    public static final int ID=78;
    public static final int AND=58;
    public static final int SUM=50;
    public static final int EOF=-1;
    public static final int CROSS=32;
    public static final int HAT=13;
    public static final int ML_COMMENT=84;
    public static final int AS=22;
    public static final int FULL=35;
    public static final int QUOTEID=80;
    public static final int USING=40;
    public static final int OFFSET=77;
    public static final int LEFT=36;
    public static final int AVG=43;
    public static final int VARIADIC=42;
    public static final int ALL=21;
    public static final int NVL=49;
    public static final int PLUS=4;
    public static final int DIGIT=81;
    public static final int EXISTS=65;
    public static final int LAST=46;
    public static final int EQ=12;
    public static final int DOT=10;
    public static final int SELECT=16;
    public static final int INTO=23;
    public static final int ARRAY=66;
    public static final int LIKE=60;
    public static final int QUOTEDSTRING=67;
    public static final int OUTER=39;
    public static final int BY=70;
    public static final int GE=14;
    public static final int ASC=74;
    public static final int TO=64;
    public static final int TEMP=25;
    public static final int MID=53;
    public static final int DEFAULT=18;
    public static final int NUMBER=76;
    public static final int VALUES=17;
    public static final int ON=20;
    public static final int OLD=29;
    public static final int WHITESPACE=79;
    public static final int RIGHT=37;
    public static final int HAVING=71;
    public static final int MIN=48;
    public static final int MINUS=5;
    public static final int LOCAL=26;
    public static final int JOIN=33;
    public static final int SIMILAR=63;
    public static final int UCASE=51;
    public static final int GROUP=69;
    public static final int SL_COMMENT=83;
    public static final int OR=59;
    public static final int ROUND=55;
    public static final int LEN=54;
    public static final int GT=8;
    public static final int FORMAT=57;
    public static final int PCT=11;
    public static final int NATURAL=34;
    public static final int DESC=73;
    public static final int DIV=7;
    public static final int GLOBAL=27;
    public static final int FROM=31;
    public static final int DISTINCT=19;
    public static final int TEMPORARY=24;
    public static final int LE=15;
    public static final int FIRST=45;

        public SQLANTLRParser(TokenStream input) {
            super(input);
            ruleMemo = new HashMap[56+1];
         }
        

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "/home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g"; }

     
        public static void main(String[] args) throws Exception {
            SQLANTLRLexer lex = new SQLANTLRLexer(new ANTLRFileStream(args[0]));
           	CommonTokenStream tokens = new CommonTokenStream(lex);	
    	List listTokens = tokens.getTokens();
    	
    		
    	SQLANTLRParser parser = new SQLANTLRParser(tokens);
            try {
                parser.stmtblock();
            } catch (RecognitionException e)  {
                e.printStackTrace();
            }
        }       



    // $ANTLR start stmtblock
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:61:1: stmtblock : stmtmulti ;
    public final void stmtblock() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:61:11: ( stmtmulti )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:61:13: stmtmulti
            {
            pushFollow(FOLLOW_stmtmulti_in_stmtblock180);
            stmtmulti();
            _fsp--;
            if (failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end stmtblock


    // $ANTLR start stmtmulti
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:1: stmtmulti : ( select ) ( ';' select )* ( ';' )? ;
    public final void stmtmulti() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:11: ( ( select ) ( ';' select )* ( ';' )? )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:13: ( select ) ( ';' select )* ( ';' )?
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:13: ( select )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:14: select
            {
            pushFollow(FOLLOW_select_in_stmtmulti193);
            select();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:22: ( ';' select )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==85) ) {
                    int LA1_1 = input.LA(2);

                    if ( ((LA1_1>=SELECT && LA1_1<=VALUES)||LA1_1==86) ) {
                        alt1=1;
                    }


                }


                switch (alt1) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:23: ';' select
            	    {
            	    match(input,85,FOLLOW_85_in_stmtmulti197); if (failed) return ;
            	    pushFollow(FOLLOW_select_in_stmtmulti199);
            	    select();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:36: ( ';' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==85) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:64:37: ';'
                    {
                    match(input,85,FOLLOW_85_in_stmtmulti204); if (failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end stmtmulti


    // $ANTLR start select
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:67:1: select : ( select_no_parens | select_with_parens );
    public final void select() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:67:12: ( select_no_parens | select_with_parens )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=SELECT && LA3_0<=VALUES)) ) {
                alt3=1;
            }
            else if ( (LA3_0==86) ) {
                alt3=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("67:1: select : ( select_no_parens | select_with_parens );", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:67:14: select_no_parens
                    {
                    pushFollow(FOLLOW_select_no_parens_in_select221);
                    select_no_parens();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:68:5: select_with_parens
                    {
                    pushFollow(FOLLOW_select_with_parens_in_select228);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end select


    // $ANTLR start select_with_parens
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:71:1: select_with_parens : ( '(' select_no_parens ')' | '(' select_with_parens ')' );
    public final void select_with_parens() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:71:20: ( '(' select_no_parens ')' | '(' select_with_parens ')' )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==86) ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1>=SELECT && LA4_1<=VALUES)) ) {
                    alt4=1;
                }
                else if ( (LA4_1==86) ) {
                    alt4=2;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("71:1: select_with_parens : ( '(' select_no_parens ')' | '(' select_with_parens ')' );", 4, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("71:1: select_with_parens : ( '(' select_no_parens ')' | '(' select_with_parens ')' );", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:71:22: '(' select_no_parens ')'
                    {
                    match(input,86,FOLLOW_86_in_select_with_parens240); if (failed) return ;
                    pushFollow(FOLLOW_select_no_parens_in_select_with_parens242);
                    select_no_parens();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_select_with_parens244); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:72:6: '(' select_with_parens ')'
                    {
                    match(input,86,FOLLOW_86_in_select_with_parens252); if (failed) return ;
                    pushFollow(FOLLOW_select_with_parens_in_select_with_parens254);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_select_with_parens256); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end select_with_parens


    // $ANTLR start select_no_parens
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:75:1: select_no_parens : ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause opt_limit | values_clause );
    public final void select_no_parens() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:75:17: ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause opt_limit | values_clause )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==SELECT) ) {
                alt5=1;
            }
            else if ( (LA5_0==VALUES) ) {
                alt5=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("75:1: select_no_parens : ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause opt_limit | values_clause );", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:75:19: SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause opt_limit
                    {
                    match(input,SELECT,FOLLOW_SELECT_in_select_no_parens268); if (failed) return ;
                    pushFollow(FOLLOW_opt_distinct_in_select_no_parens270);
                    opt_distinct();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_target_list_in_select_no_parens272);
                    target_list();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_into_clause_in_select_no_parens274);
                    into_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_from_clause_in_select_no_parens276);
                    from_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_where_clause_in_select_no_parens278);
                    where_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_groupby_clause_in_select_no_parens280);
                    groupby_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_having_clause_in_select_no_parens282);
                    having_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_orderby_clause_in_select_no_parens284);
                    orderby_clause();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_opt_limit_in_select_no_parens286);
                    opt_limit();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:76:5: values_clause
                    {
                    pushFollow(FOLLOW_values_clause_in_select_no_parens293);
                    values_clause();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end select_no_parens


    // $ANTLR start values_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:79:1: values_clause : VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )* ;
    public final void values_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:79:16: ( VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:79:18: VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )*
            {
            match(input,VALUES,FOLLOW_VALUES_in_values_clause308); if (failed) return ;
            match(input,86,FOLLOW_86_in_values_clause310); if (failed) return ;
            pushFollow(FOLLOW_values_expr_list_in_values_clause312);
            values_expr_list();
            _fsp--;
            if (failed) return ;
            match(input,87,FOLLOW_87_in_values_clause314); if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:79:50: ( ',' '(' values_expr_list ')' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==88) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:79:51: ',' '(' values_expr_list ')'
            	    {
            	    match(input,88,FOLLOW_88_in_values_clause317); if (failed) return ;
            	    match(input,86,FOLLOW_86_in_values_clause319); if (failed) return ;
            	    pushFollow(FOLLOW_values_expr_list_in_values_clause321);
            	    values_expr_list();
            	    _fsp--;
            	    if (failed) return ;
            	    match(input,87,FOLLOW_87_in_values_clause323); if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end values_clause


    // $ANTLR start values_expr_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:1: values_expr_list : ( values_expr ) ( ',' values_expr )* ;
    public final void values_expr_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:17: ( ( values_expr ) ( ',' values_expr )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:19: ( values_expr ) ( ',' values_expr )*
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:19: ( values_expr )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:20: values_expr
            {
            pushFollow(FOLLOW_values_expr_in_values_expr_list339);
            values_expr();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:33: ( ',' values_expr )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==88) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:82:34: ',' values_expr
            	    {
            	    match(input,88,FOLLOW_88_in_values_expr_list343); if (failed) return ;
            	    pushFollow(FOLLOW_values_expr_in_values_expr_list345);
            	    values_expr();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end values_expr_list


    // $ANTLR start values_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:85:1: values_expr : ( a_expr | DEFAULT );
    public final void values_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:85:13: ( a_expr | DEFAULT )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0>=OLD && LA8_0<=NEW)||(LA8_0>=AVG && LA8_0<=FORMAT)||(LA8_0>=EXISTS && LA8_0<=QUOTEDSTRING)||LA8_0==NUMBER||LA8_0==ID||LA8_0==86) ) {
                alt8=1;
            }
            else if ( (LA8_0==DEFAULT) ) {
                alt8=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("85:1: values_expr : ( a_expr | DEFAULT );", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:85:15: a_expr
                    {
                    pushFollow(FOLLOW_a_expr_in_values_expr358);
                    a_expr();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:86:5: DEFAULT
                    {
                    match(input,DEFAULT,FOLLOW_DEFAULT_in_values_expr364); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end values_expr


    // $ANTLR start opt_distinct
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:94:1: opt_distinct : ( DISTINCT | DISTINCT ON '(' expr_list ')' | ALL | );
    public final void opt_distinct() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:94:14: ( DISTINCT | DISTINCT ON '(' expr_list ')' | ALL | )
            int alt9=4;
            switch ( input.LA(1) ) {
            case DISTINCT:
                {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==ON) ) {
                    alt9=2;
                }
                else if ( (LA9_1==STAR||(LA9_1>=OLD && LA9_1<=NEW)||(LA9_1>=AVG && LA9_1<=FORMAT)||(LA9_1>=EXISTS && LA9_1<=QUOTEDSTRING)||LA9_1==NUMBER||LA9_1==ID||LA9_1==86) ) {
                    alt9=1;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("94:1: opt_distinct : ( DISTINCT | DISTINCT ON '(' expr_list ')' | ALL | );", 9, 1, input);

                    throw nvae;
                }
                }
                break;
            case ALL:
                {
                alt9=3;
                }
                break;
            case STAR:
            case OLD:
            case NEW:
            case AVG:
            case COUNT:
            case FIRST:
            case LAST:
            case MAX:
            case MIN:
            case NVL:
            case SUM:
            case UCASE:
            case LCASE:
            case MID:
            case LEN:
            case ROUND:
            case NOW:
            case FORMAT:
            case EXISTS:
            case ARRAY:
            case QUOTEDSTRING:
            case NUMBER:
            case ID:
            case 86:
                {
                alt9=4;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("94:1: opt_distinct : ( DISTINCT | DISTINCT ON '(' expr_list ')' | ALL | );", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:94:16: DISTINCT
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_opt_distinct379); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:95:5: DISTINCT ON '(' expr_list ')'
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_opt_distinct386); if (failed) return ;
                    match(input,ON,FOLLOW_ON_in_opt_distinct388); if (failed) return ;
                    match(input,86,FOLLOW_86_in_opt_distinct390); if (failed) return ;
                    pushFollow(FOLLOW_expr_list_in_opt_distinct392);
                    expr_list();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_opt_distinct394); if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:96:5: ALL
                    {
                    match(input,ALL,FOLLOW_ALL_in_opt_distinct401); if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:98:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_distinct


    // $ANTLR start expr_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:1: expr_list : ( a_expr ) ( ',' a_expr )* ;
    public final void expr_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:17: ( ( a_expr ) ( ',' a_expr )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:19: ( a_expr ) ( ',' a_expr )*
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:19: ( a_expr )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:20: a_expr
            {
            pushFollow(FOLLOW_a_expr_in_expr_list425);
            a_expr();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:28: ( ',' a_expr )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==88) ) {
                    int LA10_2 = input.LA(2);

                    if ( ((LA10_2>=OLD && LA10_2<=NEW)||(LA10_2>=AVG && LA10_2<=FORMAT)||(LA10_2>=EXISTS && LA10_2<=QUOTEDSTRING)||LA10_2==NUMBER||LA10_2==ID||LA10_2==86) ) {
                        alt10=1;
                    }


                }


                switch (alt10) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:100:29: ',' a_expr
            	    {
            	    match(input,88,FOLLOW_88_in_expr_list429); if (failed) return ;
            	    pushFollow(FOLLOW_a_expr_in_expr_list431);
            	    a_expr();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end expr_list


    // $ANTLR start target_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:1: target_list : ( target ) ( ',' target )* ;
    public final void target_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:17: ( ( target ) ( ',' target )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:19: ( target ) ( ',' target )*
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:19: ( target )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:20: target
            {
            pushFollow(FOLLOW_target_in_target_list453);
            target();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:28: ( ',' target )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==88) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:109:29: ',' target
            	    {
            	    match(input,88,FOLLOW_88_in_target_list457); if (failed) return ;
            	    pushFollow(FOLLOW_target_in_target_list459);
            	    target();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end target_list


    // $ANTLR start target
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:1: target : ( a_expr ( AS id )? | STAR ) ;
    public final void target() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:8: ( ( a_expr ( AS id )? | STAR ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:10: ( a_expr ( AS id )? | STAR )
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:10: ( a_expr ( AS id )? | STAR )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>=OLD && LA13_0<=NEW)||(LA13_0>=AVG && LA13_0<=FORMAT)||(LA13_0>=EXISTS && LA13_0<=QUOTEDSTRING)||LA13_0==NUMBER||LA13_0==ID||LA13_0==86) ) {
                alt13=1;
            }
            else if ( (LA13_0==STAR) ) {
                alt13=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("112:10: ( a_expr ( AS id )? | STAR )", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:11: a_expr ( AS id )?
                    {
                    pushFollow(FOLLOW_a_expr_in_target474);
                    a_expr();
                    _fsp--;
                    if (failed) return ;
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:18: ( AS id )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==AS) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:20: AS id
                            {
                            match(input,AS,FOLLOW_AS_in_target478); if (failed) return ;
                            pushFollow(FOLLOW_id_in_target480);
                            id();
                            _fsp--;
                            if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:112:31: STAR
                    {
                    match(input,STAR,FOLLOW_STAR_in_target487); if (failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end target


    // $ANTLR start into_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:121:1: into_clause : ( INTO opt_temp_table_name | );
    public final void into_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:121:13: ( INTO opt_temp_table_name | )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==INTO) ) {
                alt14=1;
            }
            else if ( (LA14_0==EOF||LA14_0==FROM||(LA14_0>=WHERE && LA14_0<=GROUP)||(LA14_0>=HAVING && LA14_0<=ORDER)||LA14_0==LIMIT||LA14_0==OFFSET||LA14_0==85||LA14_0==87) ) {
                alt14=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("121:1: into_clause : ( INTO opt_temp_table_name | );", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:121:15: INTO opt_temp_table_name
                    {
                    match(input,INTO,FOLLOW_INTO_in_into_clause502); if (failed) return ;
                    pushFollow(FOLLOW_opt_temp_table_name_in_into_clause504);
                    opt_temp_table_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:123:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end into_clause


    // $ANTLR start opt_temp_table_name
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:125:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );
    public final void opt_temp_table_name() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:125:21: ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name )
            int alt15=8;
            switch ( input.LA(1) ) {
            case TEMPORARY:
                {
                alt15=1;
                }
                break;
            case TEMP:
                {
                alt15=2;
                }
                break;
            case LOCAL:
                {
                int LA15_3 = input.LA(2);

                if ( (LA15_3==TEMPORARY) ) {
                    alt15=3;
                }
                else if ( (LA15_3==TEMP) ) {
                    alt15=4;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("125:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 15, 3, input);

                    throw nvae;
                }
                }
                break;
            case GLOBAL:
                {
                int LA15_4 = input.LA(2);

                if ( (LA15_4==TEMPORARY) ) {
                    alt15=5;
                }
                else if ( (LA15_4==TEMP) ) {
                    alt15=6;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("125:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 15, 4, input);

                    throw nvae;
                }
                }
                break;
            case TABLE:
                {
                alt15=7;
                }
                break;
            case OLD:
            case NEW:
            case ID:
                {
                alt15=8;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("125:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 15, 0, input);

                throw nvae;
            }

            switch (alt15) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:125:23: TEMPORARY opt_table qualified_name
                    {
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name524); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name526);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name528);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:126:6: TEMP opt_table qualified_name
                    {
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name536); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name538);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name540);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:127:6: LOCAL TEMPORARY opt_table qualified_name
                    {
                    match(input,LOCAL,FOLLOW_LOCAL_in_opt_temp_table_name548); if (failed) return ;
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name550); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name552);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name554);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:128:6: LOCAL TEMP opt_table qualified_name
                    {
                    match(input,LOCAL,FOLLOW_LOCAL_in_opt_temp_table_name562); if (failed) return ;
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name564); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name566);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name568);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:129:6: GLOBAL TEMPORARY opt_table qualified_name
                    {
                    match(input,GLOBAL,FOLLOW_GLOBAL_in_opt_temp_table_name575); if (failed) return ;
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name577); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name579);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name581);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 6 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:130:6: GLOBAL TEMP opt_table qualified_name
                    {
                    match(input,GLOBAL,FOLLOW_GLOBAL_in_opt_temp_table_name589); if (failed) return ;
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name591); if (failed) return ;
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name593);
                    opt_table();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name595);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 7 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:131:6: TABLE qualified_name
                    {
                    match(input,TABLE,FOLLOW_TABLE_in_opt_temp_table_name603); if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name605);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 8 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:132:6: qualified_name
                    {
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name613);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_temp_table_name


    // $ANTLR start opt_table
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:135:1: opt_table : ( TABLE | );
    public final void opt_table() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:135:11: ( TABLE | )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==TABLE) ) {
                alt16=1;
            }
            else if ( ((LA16_0>=OLD && LA16_0<=NEW)||LA16_0==ID) ) {
                alt16=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("135:1: opt_table : ( TABLE | );", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:135:13: TABLE
                    {
                    match(input,TABLE,FOLLOW_TABLE_in_opt_table629); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:137:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_table


    // $ANTLR start qualified_name
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:139:1: qualified_name : relation_name ( indirections )? ;
    public final void qualified_name() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:139:16: ( relation_name ( indirections )? )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:139:18: relation_name ( indirections )?
            {
            pushFollow(FOLLOW_relation_name_in_qualified_name646);
            relation_name();
            _fsp--;
            if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:139:32: ( indirections )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==DOT) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:139:32: indirections
                    {
                    pushFollow(FOLLOW_indirections_in_qualified_name648);
                    indirections();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end qualified_name


    // $ANTLR start relation_name
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:142:1: relation_name : ( id | special_rule_relation );
    public final void relation_name() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:142:15: ( id | special_rule_relation )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==ID) ) {
                alt18=1;
            }
            else if ( ((LA18_0>=OLD && LA18_0<=NEW)) ) {
                alt18=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("142:1: relation_name : ( id | special_rule_relation );", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:142:17: id
                    {
                    pushFollow(FOLLOW_id_in_relation_name661);
                    id();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:143:5: special_rule_relation
                    {
                    pushFollow(FOLLOW_special_rule_relation_in_relation_name669);
                    special_rule_relation();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end relation_name


    // $ANTLR start special_rule_relation
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:146:1: special_rule_relation : ( OLD | NEW );
    public final void special_rule_relation() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:146:23: ( OLD | NEW )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:
            {
            if ( (input.LA(1)>=OLD && input.LA(1)<=NEW) ) {
                input.consume();
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_special_rule_relation0);    throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end special_rule_relation


    // $ANTLR start indirections
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:1: indirections : ( indirection ) ( indirection )* ;
    public final void indirections() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:14: ( ( indirection ) ( indirection )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:16: ( indirection ) ( indirection )*
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:16: ( indirection )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:17: indirection
            {
            pushFollow(FOLLOW_indirection_in_indirections704);
            indirection();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:30: ( indirection )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==DOT) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:150:31: indirection
            	    {
            	    pushFollow(FOLLOW_indirection_in_indirections708);
            	    indirection();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end indirections


    // $ANTLR start indirection
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:153:1: indirection : ( DOT id | DOT STAR );
    public final void indirection() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:153:13: ( DOT id | DOT STAR )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==DOT) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==STAR) ) {
                    alt20=2;
                }
                else if ( (LA20_1==ID) ) {
                    alt20=1;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("153:1: indirection : ( DOT id | DOT STAR );", 20, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("153:1: indirection : ( DOT id | DOT STAR );", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:153:15: DOT id
                    {
                    match(input,DOT,FOLLOW_DOT_in_indirection722); if (failed) return ;
                    pushFollow(FOLLOW_id_in_indirection724);
                    id();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:154:5: DOT STAR
                    {
                    match(input,DOT,FOLLOW_DOT_in_indirection731); if (failed) return ;
                    match(input,STAR,FOLLOW_STAR_in_indirection733); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end indirection


    // $ANTLR start columnref
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:157:1: columnref : relation_name ( indirections )? ;
    public final void columnref() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:157:11: ( relation_name ( indirections )? )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:157:13: relation_name ( indirections )?
            {
            pushFollow(FOLLOW_relation_name_in_columnref747);
            relation_name();
            _fsp--;
            if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:157:27: ( indirections )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==DOT) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:157:27: indirections
                    {
                    pushFollow(FOLLOW_indirections_in_columnref749);
                    indirections();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end columnref


    // $ANTLR start from_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:168:1: from_clause : ( FROM from_list | );
    public final void from_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:168:13: ( FROM from_list | )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==FROM) ) {
                alt22=1;
            }
            else if ( (LA22_0==EOF||(LA22_0>=WHERE && LA22_0<=GROUP)||(LA22_0>=HAVING && LA22_0<=ORDER)||LA22_0==LIMIT||LA22_0==OFFSET||LA22_0==85||LA22_0==87) ) {
                alt22=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("168:1: from_clause : ( FROM from_list | );", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:168:15: FROM from_list
                    {
                    match(input,FROM,FOLLOW_FROM_in_from_clause764); if (failed) return ;
                    pushFollow(FOLLOW_from_list_in_from_clause766);
                    from_list();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:170:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end from_clause


    // $ANTLR start from_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:1: from_list : ( ( table_ref ( ',' table_ref )+ )=> refs_list | ( table_ref ( table_join )+ )=> table_joins | ( table_ref )=> table_ref );
    public final void from_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:12: ( ( table_ref ( ',' table_ref )+ )=> refs_list | ( table_ref ( table_join )+ )=> table_joins | ( table_ref )=> table_ref )
            int alt23=3;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:14: ( table_ref ( ',' table_ref )+ )=> refs_list
                    {
                    pushFollow(FOLLOW_refs_list_in_from_list802);
                    refs_list();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:174:5: ( table_ref ( table_join )+ )=> table_joins
                    {
                    pushFollow(FOLLOW_table_joins_in_from_list818);
                    table_joins();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:175:5: ( table_ref )=> table_ref
                    {
                    pushFollow(FOLLOW_table_ref_in_from_list832);
                    table_ref();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end from_list


    // $ANTLR start refs_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:178:1: refs_list : table_ref ( ',' table_ref )+ ;
    public final void refs_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:178:11: ( table_ref ( ',' table_ref )+ )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:178:13: table_ref ( ',' table_ref )+
            {
            pushFollow(FOLLOW_table_ref_in_refs_list847);
            table_ref();
            _fsp--;
            if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:178:23: ( ',' table_ref )+
            int cnt24=0;
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==88) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:178:24: ',' table_ref
            	    {
            	    match(input,88,FOLLOW_88_in_refs_list850); if (failed) return ;
            	    pushFollow(FOLLOW_table_ref_in_refs_list852);
            	    table_ref();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt24 >= 1 ) break loop24;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(24, input);
                        throw eee;
                }
                cnt24++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end refs_list


    // $ANTLR start table_joins
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:181:1: table_joins : table_ref ( table_join )+ ;
    public final void table_joins() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:181:14: ( table_ref ( table_join )+ )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:181:16: table_ref ( table_join )+
            {
            pushFollow(FOLLOW_table_ref_in_table_joins870);
            table_ref();
            _fsp--;
            if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:181:26: ( table_join )+
            int cnt25=0;
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( ((LA25_0>=CROSS && LA25_0<=INNER)||LA25_0==86) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:181:26: table_join
            	    {
            	    pushFollow(FOLLOW_table_join_in_table_joins872);
            	    table_join();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt25 >= 1 ) break loop25;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end table_joins


    // $ANTLR start table_ref
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:184:1: table_ref : ( relation_expr opt_alias | func_expr opt_alias | select_with_parens opt_alias );
    public final void table_ref() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:184:12: ( relation_expr opt_alias | func_expr opt_alias | select_with_parens opt_alias )
            int alt26=3;
            switch ( input.LA(1) ) {
            case OLD:
            case NEW:
            case ONLY:
            case ID:
                {
                alt26=1;
                }
                break;
            case AVG:
            case COUNT:
            case FIRST:
            case LAST:
            case MAX:
            case MIN:
            case NVL:
            case SUM:
            case UCASE:
            case LCASE:
            case MID:
            case LEN:
            case ROUND:
            case NOW:
            case FORMAT:
                {
                alt26=2;
                }
                break;
            case 86:
                {
                alt26=3;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("184:1: table_ref : ( relation_expr opt_alias | func_expr opt_alias | select_with_parens opt_alias );", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:184:14: relation_expr opt_alias
                    {
                    pushFollow(FOLLOW_relation_expr_in_table_ref887);
                    relation_expr();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_opt_alias_in_table_ref889);
                    opt_alias();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:185:5: func_expr opt_alias
                    {
                    pushFollow(FOLLOW_func_expr_in_table_ref895);
                    func_expr();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_opt_alias_in_table_ref897);
                    opt_alias();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:186:5: select_with_parens opt_alias
                    {
                    pushFollow(FOLLOW_select_with_parens_in_table_ref903);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_opt_alias_in_table_ref905);
                    opt_alias();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end table_ref


    // $ANTLR start table_join
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:189:1: table_join : ( joined_table | '(' joined_table ')' alias_clause );
    public final void table_join() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:189:13: ( joined_table | '(' joined_table ')' alias_clause )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( ((LA27_0>=CROSS && LA27_0<=INNER)) ) {
                alt27=1;
            }
            else if ( (LA27_0==86) ) {
                alt27=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("189:1: table_join : ( joined_table | '(' joined_table ')' alias_clause );", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:189:15: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_join918);
                    joined_table();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:190:5: '(' joined_table ')' alias_clause
                    {
                    match(input,86,FOLLOW_86_in_table_join924); if (failed) return ;
                    pushFollow(FOLLOW_joined_table_in_table_join926);
                    joined_table();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_table_join928); if (failed) return ;
                    pushFollow(FOLLOW_alias_clause_in_table_join930);
                    alias_clause();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end table_join


    // $ANTLR start joined_table
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:193:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );
    public final void joined_table() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:193:15: ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref )
            int alt28=5;
            switch ( input.LA(1) ) {
            case CROSS:
                {
                alt28=1;
                }
                break;
            case FULL:
            case LEFT:
            case RIGHT:
            case INNER:
                {
                alt28=2;
                }
                break;
            case JOIN:
                {
                alt28=3;
                }
                break;
            case NATURAL:
                {
                int LA28_4 = input.LA(2);

                if ( (LA28_4==JOIN) ) {
                    alt28=5;
                }
                else if ( ((LA28_4>=FULL && LA28_4<=INNER)) ) {
                    alt28=4;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("193:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );", 28, 4, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("193:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:193:17: CROSS JOIN table_ref
                    {
                    match(input,CROSS,FOLLOW_CROSS_in_joined_table943); if (failed) return ;
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table945); if (failed) return ;
                    pushFollow(FOLLOW_table_ref_in_joined_table947);
                    table_ref();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:194:5: join_type JOIN table_ref join_qual
                    {
                    pushFollow(FOLLOW_join_type_in_joined_table953);
                    join_type();
                    _fsp--;
                    if (failed) return ;
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table955); if (failed) return ;
                    pushFollow(FOLLOW_table_ref_in_joined_table957);
                    table_ref();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_join_qual_in_joined_table959);
                    join_qual();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:195:5: JOIN table_ref join_qual
                    {
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table965); if (failed) return ;
                    pushFollow(FOLLOW_table_ref_in_joined_table967);
                    table_ref();
                    _fsp--;
                    if (failed) return ;
                    pushFollow(FOLLOW_join_qual_in_joined_table969);
                    join_qual();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:196:5: NATURAL join_type JOIN table_ref
                    {
                    match(input,NATURAL,FOLLOW_NATURAL_in_joined_table975); if (failed) return ;
                    pushFollow(FOLLOW_join_type_in_joined_table977);
                    join_type();
                    _fsp--;
                    if (failed) return ;
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table979); if (failed) return ;
                    pushFollow(FOLLOW_table_ref_in_joined_table981);
                    table_ref();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:197:5: NATURAL JOIN table_ref
                    {
                    match(input,NATURAL,FOLLOW_NATURAL_in_joined_table987); if (failed) return ;
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table989); if (failed) return ;
                    pushFollow(FOLLOW_table_ref_in_joined_table991);
                    table_ref();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end joined_table


    // $ANTLR start join_type
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:200:1: join_type : ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER );
    public final void join_type() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:200:11: ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER )
            int alt29=4;
            switch ( input.LA(1) ) {
            case FULL:
                {
                alt29=1;
                }
                break;
            case LEFT:
                {
                alt29=2;
                }
                break;
            case RIGHT:
                {
                alt29=3;
                }
                break;
            case INNER:
                {
                alt29=4;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("200:1: join_type : ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER );", 29, 0, input);

                throw nvae;
            }

            switch (alt29) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:200:13: FULL join_outer
                    {
                    match(input,FULL,FOLLOW_FULL_in_join_type1006); if (failed) return ;
                    pushFollow(FOLLOW_join_outer_in_join_type1008);
                    join_outer();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:201:5: LEFT join_outer
                    {
                    match(input,LEFT,FOLLOW_LEFT_in_join_type1015); if (failed) return ;
                    pushFollow(FOLLOW_join_outer_in_join_type1017);
                    join_outer();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:202:5: RIGHT join_outer
                    {
                    match(input,RIGHT,FOLLOW_RIGHT_in_join_type1024); if (failed) return ;
                    pushFollow(FOLLOW_join_outer_in_join_type1026);
                    join_outer();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:203:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1033); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end join_type


    // $ANTLR start join_outer
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:207:1: join_outer : ( OUTER | );
    public final void join_outer() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:207:12: ( OUTER | )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==OUTER) ) {
                alt30=1;
            }
            else if ( (LA30_0==JOIN) ) {
                alt30=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("207:1: join_outer : ( OUTER | );", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:207:14: OUTER
                    {
                    match(input,OUTER,FOLLOW_OUTER_in_join_outer1049); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:209:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end join_outer


    // $ANTLR start join_qual
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:211:1: join_qual : ( USING '(' name_list ')' | ON a_expr );
    public final void join_qual() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:211:11: ( USING '(' name_list ')' | ON a_expr )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==USING) ) {
                alt31=1;
            }
            else if ( (LA31_0==ON) ) {
                alt31=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("211:1: join_qual : ( USING '(' name_list ')' | ON a_expr );", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:211:13: USING '(' name_list ')'
                    {
                    match(input,USING,FOLLOW_USING_in_join_qual1066); if (failed) return ;
                    match(input,86,FOLLOW_86_in_join_qual1068); if (failed) return ;
                    pushFollow(FOLLOW_name_list_in_join_qual1070);
                    name_list();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_join_qual1072); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:212:5: ON a_expr
                    {
                    match(input,ON,FOLLOW_ON_in_join_qual1079); if (failed) return ;
                    pushFollow(FOLLOW_a_expr_in_join_qual1081);
                    a_expr();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end join_qual


    // $ANTLR start opt_alias
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:216:1: opt_alias : ( ( alias_clause )=> alias_clause | );
    public final void opt_alias() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:216:12: ( ( alias_clause )=> alias_clause | )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==AS) && (synpred4())) {
                alt32=1;
            }
            else if ( (LA32_0==ID) && (synpred4())) {
                alt32=1;
            }
            else if ( (LA32_0==EOF||LA32_0==ON||(LA32_0>=CROSS && LA32_0<=INNER)||LA32_0==USING||(LA32_0>=WHERE && LA32_0<=GROUP)||(LA32_0>=HAVING && LA32_0<=ORDER)||LA32_0==LIMIT||LA32_0==OFFSET||(LA32_0>=85 && LA32_0<=88)) ) {
                alt32=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("216:1: opt_alias : ( ( alias_clause )=> alias_clause | );", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:216:14: ( alias_clause )=> alias_clause
                    {
                    pushFollow(FOLLOW_alias_clause_in_opt_alias1101);
                    alias_clause();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:218:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_alias


    // $ANTLR start alias_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:220:1: alias_clause : ( AS id ( '(' name_list ')' )? | ( id ( '(' name_list ')' )? )=> names );
    public final void alias_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:220:14: ( AS id ( '(' name_list ')' )? | ( id ( '(' name_list ')' )? )=> names )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==AS) ) {
                alt34=1;
            }
            else if ( (LA34_0==ID) && (synpred5())) {
                alt34=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("220:1: alias_clause : ( AS id ( '(' name_list ')' )? | ( id ( '(' name_list ')' )? )=> names );", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:220:16: AS id ( '(' name_list ')' )?
                    {
                    match(input,AS,FOLLOW_AS_in_alias_clause1127); if (failed) return ;
                    pushFollow(FOLLOW_id_in_alias_clause1129);
                    id();
                    _fsp--;
                    if (failed) return ;
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:220:22: ( '(' name_list ')' )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==86) ) {
                        int LA33_1 = input.LA(2);

                        if ( (LA33_1==ID) ) {
                            alt33=1;
                        }
                    }
                    switch (alt33) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:220:23: '(' name_list ')'
                            {
                            match(input,86,FOLLOW_86_in_alias_clause1132); if (failed) return ;
                            pushFollow(FOLLOW_name_list_in_alias_clause1134);
                            name_list();
                            _fsp--;
                            if (failed) return ;
                            match(input,87,FOLLOW_87_in_alias_clause1136); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:221:5: ( id ( '(' name_list ')' )? )=> names
                    {
                    pushFollow(FOLLOW_names_in_alias_clause1159);
                    names();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end alias_clause


    // $ANTLR start names
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:224:1: names : ( id '(' name_list ')' | id );
    public final void names() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:224:8: ( id '(' name_list ')' | id )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==ID) ) {
                int LA35_1 = input.LA(2);

                if ( (LA35_1==EOF||LA35_1==ON||(LA35_1>=CROSS && LA35_1<=INNER)||LA35_1==USING||(LA35_1>=WHERE && LA35_1<=GROUP)||(LA35_1>=HAVING && LA35_1<=ORDER)||LA35_1==LIMIT||LA35_1==OFFSET||LA35_1==85||(LA35_1>=87 && LA35_1<=88)) ) {
                    alt35=2;
                }
                else if ( (LA35_1==86) ) {
                    int LA35_3 = input.LA(3);

                    if ( ((LA35_3>=CROSS && LA35_3<=INNER)) ) {
                        alt35=2;
                    }
                    else if ( (LA35_3==ID) ) {
                        alt35=1;
                    }
                    else {
                        if (backtracking>0) {failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("224:1: names : ( id '(' name_list ')' | id );", 35, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("224:1: names : ( id '(' name_list ')' | id );", 35, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("224:1: names : ( id '(' name_list ')' | id );", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:224:10: id '(' name_list ')'
                    {
                    pushFollow(FOLLOW_id_in_names1174);
                    id();
                    _fsp--;
                    if (failed) return ;
                    match(input,86,FOLLOW_86_in_names1176); if (failed) return ;
                    pushFollow(FOLLOW_name_list_in_names1178);
                    name_list();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_names1180); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:225:5: id
                    {
                    pushFollow(FOLLOW_id_in_names1186);
                    id();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end names


    // $ANTLR start name_list
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:1: name_list : ( id ) ( ',' id )* ;
    public final void name_list() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:11: ( ( id ) ( ',' id )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:13: ( id ) ( ',' id )*
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:13: ( id )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:14: id
            {
            pushFollow(FOLLOW_id_in_name_list1202);
            id();
            _fsp--;
            if (failed) return ;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:18: ( ',' id )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==88) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:228:19: ',' id
            	    {
            	    match(input,88,FOLLOW_88_in_name_list1206); if (failed) return ;
            	    pushFollow(FOLLOW_id_in_name_list1208);
            	    id();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end name_list


    // $ANTLR start relation_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:231:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );
    public final void relation_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:231:15: ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' )
            int alt38=3;
            int LA38_0 = input.LA(1);

            if ( ((LA38_0>=OLD && LA38_0<=NEW)||LA38_0==ID) ) {
                alt38=1;
            }
            else if ( (LA38_0==ONLY) ) {
                int LA38_2 = input.LA(2);

                if ( (LA38_2==86) ) {
                    alt38=3;
                }
                else if ( ((LA38_2>=OLD && LA38_2<=NEW)||LA38_2==ID) ) {
                    alt38=2;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("231:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );", 38, 2, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("231:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:231:17: qualified_name ( STAR )?
                    {
                    pushFollow(FOLLOW_qualified_name_in_relation_expr1224);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:231:32: ( STAR )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==STAR) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:231:32: STAR
                            {
                            match(input,STAR,FOLLOW_STAR_in_relation_expr1226); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:232:5: ONLY qualified_name
                    {
                    match(input,ONLY,FOLLOW_ONLY_in_relation_expr1234); if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_relation_expr1237);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:233:5: ONLY '(' qualified_name ')'
                    {
                    match(input,ONLY,FOLLOW_ONLY_in_relation_expr1244); if (failed) return ;
                    match(input,86,FOLLOW_86_in_relation_expr1246); if (failed) return ;
                    pushFollow(FOLLOW_qualified_name_in_relation_expr1248);
                    qualified_name();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_relation_expr1250); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end relation_expr


    // $ANTLR start func_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:237:1: func_expr : func_name '(' ( ')' | expr_list ( ')' | ',' VARIADIC a_expr ')' ) | VARIADIC a_expr ')' | ALL expr_list ')' | DISTINCT expr_list ')' | STAR ')' ) ;
    public final void func_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:237:12: ( func_name '(' ( ')' | expr_list ( ')' | ',' VARIADIC a_expr ')' ) | VARIADIC a_expr ')' | ALL expr_list ')' | DISTINCT expr_list ')' | STAR ')' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:237:15: func_name '(' ( ')' | expr_list ( ')' | ',' VARIADIC a_expr ')' ) | VARIADIC a_expr ')' | ALL expr_list ')' | DISTINCT expr_list ')' | STAR ')' )
            {
            pushFollow(FOLLOW_func_name_in_func_expr1265);
            func_name();
            _fsp--;
            if (failed) return ;
            match(input,86,FOLLOW_86_in_func_expr1267); if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:238:3: ( ')' | expr_list ( ')' | ',' VARIADIC a_expr ')' ) | VARIADIC a_expr ')' | ALL expr_list ')' | DISTINCT expr_list ')' | STAR ')' )
            int alt40=6;
            switch ( input.LA(1) ) {
            case 87:
                {
                alt40=1;
                }
                break;
            case OLD:
            case NEW:
            case AVG:
            case COUNT:
            case FIRST:
            case LAST:
            case MAX:
            case MIN:
            case NVL:
            case SUM:
            case UCASE:
            case LCASE:
            case MID:
            case LEN:
            case ROUND:
            case NOW:
            case FORMAT:
            case EXISTS:
            case ARRAY:
            case QUOTEDSTRING:
            case NUMBER:
            case ID:
            case 86:
                {
                alt40=2;
                }
                break;
            case VARIADIC:
                {
                alt40=3;
                }
                break;
            case ALL:
                {
                alt40=4;
                }
                break;
            case DISTINCT:
                {
                alt40=5;
                }
                break;
            case STAR:
                {
                alt40=6;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("238:3: ( ')' | expr_list ( ')' | ',' VARIADIC a_expr ')' ) | VARIADIC a_expr ')' | ALL expr_list ')' | DISTINCT expr_list ')' | STAR ')' )", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:238:5: ')'
                    {
                    match(input,87,FOLLOW_87_in_func_expr1274); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:239:5: expr_list ( ')' | ',' VARIADIC a_expr ')' )
                    {
                    pushFollow(FOLLOW_expr_list_in_func_expr1281);
                    expr_list();
                    _fsp--;
                    if (failed) return ;
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:239:15: ( ')' | ',' VARIADIC a_expr ')' )
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==87) ) {
                        alt39=1;
                    }
                    else if ( (LA39_0==88) ) {
                        alt39=2;
                    }
                    else {
                        if (backtracking>0) {failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("239:15: ( ')' | ',' VARIADIC a_expr ')' )", 39, 0, input);

                        throw nvae;
                    }
                    switch (alt39) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:239:17: ')'
                            {
                            match(input,87,FOLLOW_87_in_func_expr1285); if (failed) return ;

                            }
                            break;
                        case 2 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:239:23: ',' VARIADIC a_expr ')'
                            {
                            match(input,88,FOLLOW_88_in_func_expr1289); if (failed) return ;
                            match(input,VARIADIC,FOLLOW_VARIADIC_in_func_expr1291); if (failed) return ;
                            pushFollow(FOLLOW_a_expr_in_func_expr1293);
                            a_expr();
                            _fsp--;
                            if (failed) return ;
                            match(input,87,FOLLOW_87_in_func_expr1295); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:240:5: VARIADIC a_expr ')'
                    {
                    match(input,VARIADIC,FOLLOW_VARIADIC_in_func_expr1302); if (failed) return ;
                    pushFollow(FOLLOW_a_expr_in_func_expr1304);
                    a_expr();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_func_expr1306); if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:241:5: ALL expr_list ')'
                    {
                    match(input,ALL,FOLLOW_ALL_in_func_expr1313); if (failed) return ;
                    pushFollow(FOLLOW_expr_list_in_func_expr1315);
                    expr_list();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_func_expr1317); if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:242:5: DISTINCT expr_list ')'
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_func_expr1324); if (failed) return ;
                    pushFollow(FOLLOW_expr_list_in_func_expr1326);
                    expr_list();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_func_expr1328); if (failed) return ;

                    }
                    break;
                case 6 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:243:5: STAR ')'
                    {
                    match(input,STAR,FOLLOW_STAR_in_func_expr1335); if (failed) return ;
                    match(input,87,FOLLOW_87_in_func_expr1337); if (failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end func_expr


    // $ANTLR start func_name
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:247:1: func_name : ( AVG | COUNT | FIRST | LAST | MAX | MIN | NVL | SUM | UCASE | LCASE | MID | LEN | ROUND | NOW | FORMAT );
    public final void func_name() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:247:11: ( AVG | COUNT | FIRST | LAST | MAX | MIN | NVL | SUM | UCASE | LCASE | MID | LEN | ROUND | NOW | FORMAT )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:
            {
            if ( (input.LA(1)>=AVG && input.LA(1)<=FORMAT) ) {
                input.consume();
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_func_name0);    throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end func_name


    // $ANTLR start a_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:256:1: a_expr : c_expr ( binary_op c_expr )* ;
    public final void a_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:256:9: ( c_expr ( binary_op c_expr )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:256:11: c_expr ( binary_op c_expr )*
            {
            pushFollow(FOLLOW_c_expr_in_a_expr1429);
            c_expr();
            _fsp--;
            if (failed) return ;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:256:18: ( binary_op c_expr )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( ((LA41_0>=PLUS && LA41_0<=LT)||(LA41_0>=PCT && LA41_0<=LE)||(LA41_0>=AND && LA41_0<=SIMILAR)) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:256:20: binary_op c_expr
            	    {
            	    pushFollow(FOLLOW_binary_op_in_a_expr1433);
            	    binary_op();
            	    _fsp--;
            	    if (failed) return ;
            	    pushFollow(FOLLOW_c_expr_in_a_expr1435);
            	    c_expr();
            	    _fsp--;
            	    if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end a_expr


    // $ANTLR start binary_op
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:1: binary_op : ( STAR | PLUS | DIV | MINUS | PCT | GT | LT | GE | LE | EQ | HAT | AND | OR | LIKE | NOT LIKE | ILIKE | NOT ILIKE | SIMILAR TO | NOT SIMILAR TO );
    public final void binary_op() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:12: ( STAR | PLUS | DIV | MINUS | PCT | GT | LT | GE | LE | EQ | HAT | AND | OR | LIKE | NOT LIKE | ILIKE | NOT ILIKE | SIMILAR TO | NOT SIMILAR TO )
            int alt42=19;
            switch ( input.LA(1) ) {
            case STAR:
                {
                alt42=1;
                }
                break;
            case PLUS:
                {
                alt42=2;
                }
                break;
            case DIV:
                {
                alt42=3;
                }
                break;
            case MINUS:
                {
                alt42=4;
                }
                break;
            case PCT:
                {
                alt42=5;
                }
                break;
            case GT:
                {
                alt42=6;
                }
                break;
            case LT:
                {
                alt42=7;
                }
                break;
            case GE:
                {
                alt42=8;
                }
                break;
            case LE:
                {
                alt42=9;
                }
                break;
            case EQ:
                {
                alt42=10;
                }
                break;
            case HAT:
                {
                alt42=11;
                }
                break;
            case AND:
                {
                alt42=12;
                }
                break;
            case OR:
                {
                alt42=13;
                }
                break;
            case LIKE:
                {
                alt42=14;
                }
                break;
            case NOT:
                {
                switch ( input.LA(2) ) {
                case SIMILAR:
                    {
                    alt42=19;
                    }
                    break;
                case LIKE:
                    {
                    alt42=15;
                    }
                    break;
                case ILIKE:
                    {
                    alt42=17;
                    }
                    break;
                default:
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("259:1: binary_op : ( STAR | PLUS | DIV | MINUS | PCT | GT | LT | GE | LE | EQ | HAT | AND | OR | LIKE | NOT LIKE | ILIKE | NOT ILIKE | SIMILAR TO | NOT SIMILAR TO );", 42, 15, input);

                    throw nvae;
                }

                }
                break;
            case ILIKE:
                {
                alt42=16;
                }
                break;
            case SIMILAR:
                {
                alt42=18;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("259:1: binary_op : ( STAR | PLUS | DIV | MINUS | PCT | GT | LT | GE | LE | EQ | HAT | AND | OR | LIKE | NOT LIKE | ILIKE | NOT ILIKE | SIMILAR TO | NOT SIMILAR TO );", 42, 0, input);

                throw nvae;
            }

            switch (alt42) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:14: STAR
                    {
                    match(input,STAR,FOLLOW_STAR_in_binary_op1454); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:21: PLUS
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_binary_op1458); if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:28: DIV
                    {
                    match(input,DIV,FOLLOW_DIV_in_binary_op1462); if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:34: MINUS
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_binary_op1466); if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:42: PCT
                    {
                    match(input,PCT,FOLLOW_PCT_in_binary_op1470); if (failed) return ;

                    }
                    break;
                case 6 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:48: GT
                    {
                    match(input,GT,FOLLOW_GT_in_binary_op1474); if (failed) return ;

                    }
                    break;
                case 7 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:53: LT
                    {
                    match(input,LT,FOLLOW_LT_in_binary_op1478); if (failed) return ;

                    }
                    break;
                case 8 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:58: GE
                    {
                    match(input,GE,FOLLOW_GE_in_binary_op1482); if (failed) return ;

                    }
                    break;
                case 9 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:63: LE
                    {
                    match(input,LE,FOLLOW_LE_in_binary_op1486); if (failed) return ;

                    }
                    break;
                case 10 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:68: EQ
                    {
                    match(input,EQ,FOLLOW_EQ_in_binary_op1490); if (failed) return ;

                    }
                    break;
                case 11 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:73: HAT
                    {
                    match(input,HAT,FOLLOW_HAT_in_binary_op1494); if (failed) return ;

                    }
                    break;
                case 12 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:79: AND
                    {
                    match(input,AND,FOLLOW_AND_in_binary_op1498); if (failed) return ;

                    }
                    break;
                case 13 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:85: OR
                    {
                    match(input,OR,FOLLOW_OR_in_binary_op1502); if (failed) return ;

                    }
                    break;
                case 14 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:90: LIKE
                    {
                    match(input,LIKE,FOLLOW_LIKE_in_binary_op1506); if (failed) return ;

                    }
                    break;
                case 15 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:97: NOT LIKE
                    {
                    match(input,NOT,FOLLOW_NOT_in_binary_op1510); if (failed) return ;
                    match(input,LIKE,FOLLOW_LIKE_in_binary_op1512); if (failed) return ;

                    }
                    break;
                case 16 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:108: ILIKE
                    {
                    match(input,ILIKE,FOLLOW_ILIKE_in_binary_op1516); if (failed) return ;

                    }
                    break;
                case 17 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:116: NOT ILIKE
                    {
                    match(input,NOT,FOLLOW_NOT_in_binary_op1520); if (failed) return ;
                    match(input,ILIKE,FOLLOW_ILIKE_in_binary_op1522); if (failed) return ;

                    }
                    break;
                case 18 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:128: SIMILAR TO
                    {
                    match(input,SIMILAR,FOLLOW_SIMILAR_in_binary_op1526); if (failed) return ;
                    match(input,TO,FOLLOW_TO_in_binary_op1528); if (failed) return ;

                    }
                    break;
                case 19 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:259:141: NOT SIMILAR TO
                    {
                    match(input,NOT,FOLLOW_NOT_in_binary_op1532); if (failed) return ;
                    match(input,SIMILAR,FOLLOW_SIMILAR_in_binary_op1534); if (failed) return ;
                    match(input,TO,FOLLOW_TO_in_binary_op1536); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end binary_op


    // $ANTLR start b_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:263:1: b_expr : ;
    public final void b_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:263:9: ()
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:263:12: 
            {
            }

        }
        finally {
        }
        return ;
    }
    // $ANTLR end b_expr


    // $ANTLR start c_expr
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:266:1: c_expr : ( columnref | '(' a_expr ')' | func_expr | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );
    public final void c_expr() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:266:9: ( columnref | '(' a_expr ')' | func_expr | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING )
            int alt43=8;
            switch ( input.LA(1) ) {
            case OLD:
            case NEW:
            case ID:
                {
                alt43=1;
                }
                break;
            case 86:
                {
                int LA43_2 = input.LA(2);

                if ( ((LA43_2>=OLD && LA43_2<=NEW)||(LA43_2>=AVG && LA43_2<=FORMAT)||(LA43_2>=EXISTS && LA43_2<=QUOTEDSTRING)||LA43_2==NUMBER||LA43_2==ID) ) {
                    alt43=2;
                }
                else if ( (LA43_2==86) ) {
                    int LA43_9 = input.LA(3);

                    if ( (true) ) {
                        alt43=2;
                    }
                    else if ( (synpred6()) ) {
                        alt43=4;
                    }
                    else {
                        if (backtracking>0) {failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("266:1: c_expr : ( columnref | '(' a_expr ')' | func_expr | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 43, 9, input);

                        throw nvae;
                    }
                }
                else if ( (LA43_2==SELECT) && (synpred6())) {
                    alt43=4;
                }
                else if ( (LA43_2==VALUES) && (synpred6())) {
                    alt43=4;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("266:1: c_expr : ( columnref | '(' a_expr ')' | func_expr | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 43, 2, input);

                    throw nvae;
                }
                }
                break;
            case AVG:
            case COUNT:
            case FIRST:
            case LAST:
            case MAX:
            case MIN:
            case NVL:
            case SUM:
            case UCASE:
            case LCASE:
            case MID:
            case LEN:
            case ROUND:
            case NOW:
            case FORMAT:
                {
                alt43=3;
                }
                break;
            case EXISTS:
                {
                alt43=5;
                }
                break;
            case ARRAY:
                {
                alt43=6;
                }
                break;
            case NUMBER:
                {
                alt43=7;
                }
                break;
            case QUOTEDSTRING:
                {
                alt43=8;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("266:1: c_expr : ( columnref | '(' a_expr ')' | func_expr | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 43, 0, input);

                throw nvae;
            }

            switch (alt43) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:266:11: columnref
                    {
                    pushFollow(FOLLOW_columnref_in_c_expr1560);
                    columnref();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:267:4: '(' a_expr ')'
                    {
                    match(input,86,FOLLOW_86_in_c_expr1566); if (failed) return ;
                    pushFollow(FOLLOW_a_expr_in_c_expr1568);
                    a_expr();
                    _fsp--;
                    if (failed) return ;
                    match(input,87,FOLLOW_87_in_c_expr1570); if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:268:5: func_expr
                    {
                    pushFollow(FOLLOW_func_expr_in_c_expr1576);
                    func_expr();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:269:5: ( select_with_parens )=> select_with_parens
                    {
                    pushFollow(FOLLOW_select_with_parens_in_c_expr1589);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:270:5: EXISTS select_with_parens
                    {
                    match(input,EXISTS,FOLLOW_EXISTS_in_c_expr1595); if (failed) return ;
                    pushFollow(FOLLOW_select_with_parens_in_c_expr1597);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 6 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:271:5: ARRAY select_with_parens
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_c_expr1604); if (failed) return ;
                    pushFollow(FOLLOW_select_with_parens_in_c_expr1606);
                    select_with_parens();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 7 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:272:5: factor
                    {
                    pushFollow(FOLLOW_factor_in_c_expr1613);
                    factor();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 8 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:273:5: QUOTEDSTRING
                    {
                    match(input,QUOTEDSTRING,FOLLOW_QUOTEDSTRING_in_c_expr1619); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end c_expr


    // $ANTLR start where_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:276:1: where_clause : ( WHERE a_expr | );
    public final void where_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:276:15: ( WHERE a_expr | )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==WHERE) ) {
                alt44=1;
            }
            else if ( (LA44_0==EOF||LA44_0==GROUP||(LA44_0>=HAVING && LA44_0<=ORDER)||LA44_0==LIMIT||LA44_0==OFFSET||LA44_0==85||LA44_0==87) ) {
                alt44=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("276:1: where_clause : ( WHERE a_expr | );", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:276:17: WHERE a_expr
                    {
                    match(input,WHERE,FOLLOW_WHERE_in_where_clause1631); if (failed) return ;
                    pushFollow(FOLLOW_a_expr_in_where_clause1633);
                    a_expr();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:278:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end where_clause


    // $ANTLR start groupby_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:280:1: groupby_clause : ( GROUP BY expr_list | );
    public final void groupby_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:280:17: ( GROUP BY expr_list | )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==GROUP) ) {
                alt45=1;
            }
            else if ( (LA45_0==EOF||(LA45_0>=HAVING && LA45_0<=ORDER)||LA45_0==LIMIT||LA45_0==OFFSET||LA45_0==85||LA45_0==87) ) {
                alt45=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("280:1: groupby_clause : ( GROUP BY expr_list | );", 45, 0, input);

                throw nvae;
            }
            switch (alt45) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:280:19: GROUP BY expr_list
                    {
                    match(input,GROUP,FOLLOW_GROUP_in_groupby_clause1651); if (failed) return ;
                    match(input,BY,FOLLOW_BY_in_groupby_clause1653); if (failed) return ;
                    pushFollow(FOLLOW_expr_list_in_groupby_clause1655);
                    expr_list();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:282:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end groupby_clause


    // $ANTLR start having_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:284:1: having_clause : ( HAVING a_expr | );
    public final void having_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:284:16: ( HAVING a_expr | )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==HAVING) ) {
                alt46=1;
            }
            else if ( (LA46_0==EOF||LA46_0==ORDER||LA46_0==LIMIT||LA46_0==OFFSET||LA46_0==85||LA46_0==87) ) {
                alt46=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("284:1: having_clause : ( HAVING a_expr | );", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:284:18: HAVING a_expr
                    {
                    match(input,HAVING,FOLLOW_HAVING_in_having_clause1674); if (failed) return ;
                    pushFollow(FOLLOW_a_expr_in_having_clause1676);
                    a_expr();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:286:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end having_clause


    // $ANTLR start orderby_clause
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:288:1: orderby_clause : ( ORDER BY ( DESC | ASC )? | );
    public final void orderby_clause() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:288:17: ( ORDER BY ( DESC | ASC )? | )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==ORDER) ) {
                alt48=1;
            }
            else if ( (LA48_0==EOF||LA48_0==LIMIT||LA48_0==OFFSET||LA48_0==85||LA48_0==87) ) {
                alt48=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("288:1: orderby_clause : ( ORDER BY ( DESC | ASC )? | );", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:288:19: ORDER BY ( DESC | ASC )?
                    {
                    match(input,ORDER,FOLLOW_ORDER_in_orderby_clause1694); if (failed) return ;
                    match(input,BY,FOLLOW_BY_in_orderby_clause1696); if (failed) return ;
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:288:28: ( DESC | ASC )?
                    int alt47=2;
                    int LA47_0 = input.LA(1);

                    if ( ((LA47_0>=DESC && LA47_0<=ASC)) ) {
                        alt47=1;
                    }
                    switch (alt47) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:
                            {
                            if ( (input.LA(1)>=DESC && input.LA(1)<=ASC) ) {
                                input.consume();
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return ;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_orderby_clause1698);    throw mse;
                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:290:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end orderby_clause


    // $ANTLR start opt_limit
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:292:1: opt_limit : ( limit | );
    public final void opt_limit() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:292:13: ( limit | )
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==LIMIT||LA49_0==OFFSET) ) {
                alt49=1;
            }
            else if ( (LA49_0==EOF||LA49_0==85||LA49_0==87) ) {
                alt49=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("292:1: opt_limit : ( limit | );", 49, 0, input);

                throw nvae;
            }
            switch (alt49) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:292:15: limit
                    {
                    pushFollow(FOLLOW_limit_in_opt_limit1722);
                    limit();
                    _fsp--;
                    if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:294:3: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_limit


    // $ANTLR start limit
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );
    public final void limit() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:296:9: ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER )
            int alt50=5;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==LIMIT) ) {
                int LA50_1 = input.LA(2);

                if ( (LA50_1==NUMBER) ) {
                    switch ( input.LA(3) ) {
                    case OFFSET:
                        {
                        alt50=1;
                        }
                        break;
                    case 88:
                        {
                        alt50=5;
                        }
                        break;
                    case EOF:
                    case 85:
                    case 87:
                        {
                        alt50=3;
                        }
                        break;
                    default:
                        if (backtracking>0) {failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );", 50, 3, input);

                        throw nvae;
                    }

                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );", 50, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA50_0==OFFSET) ) {
                int LA50_2 = input.LA(2);

                if ( (LA50_2==NUMBER) ) {
                    int LA50_4 = input.LA(3);

                    if ( (LA50_4==LIMIT) ) {
                        alt50=2;
                    }
                    else if ( (LA50_4==EOF||LA50_4==85||LA50_4==87) ) {
                        alt50=4;
                    }
                    else {
                        if (backtracking>0) {failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );", 50, 4, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );", 50, 2, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("296:1: limit : ( LIMIT NUMBER OFFSET NUMBER | OFFSET NUMBER LIMIT NUMBER | LIMIT NUMBER | OFFSET NUMBER | LIMIT NUMBER ',' NUMBER );", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:296:11: LIMIT NUMBER OFFSET NUMBER
                    {
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit1742); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1744); if (failed) return ;
                    match(input,OFFSET,FOLLOW_OFFSET_in_limit1746); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1748); if (failed) return ;

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:297:5: OFFSET NUMBER LIMIT NUMBER
                    {
                    match(input,OFFSET,FOLLOW_OFFSET_in_limit1755); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1757); if (failed) return ;
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit1759); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1761); if (failed) return ;

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:298:5: LIMIT NUMBER
                    {
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit1767); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1769); if (failed) return ;

                    }
                    break;
                case 4 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:299:5: OFFSET NUMBER
                    {
                    match(input,OFFSET,FOLLOW_OFFSET_in_limit1776); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1778); if (failed) return ;

                    }
                    break;
                case 5 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:300:5: LIMIT NUMBER ',' NUMBER
                    {
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit1784); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1786); if (failed) return ;
                    match(input,88,FOLLOW_88_in_limit1788); if (failed) return ;
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit1790); if (failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end limit


    // $ANTLR start id
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:303:1: id : ID ;
    public final void id() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:303:6: ( ID )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:303:8: ID
            {
            match(input,ID,FOLLOW_ID_in_id1808); if (failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end id


    // $ANTLR start factor
    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:306:1: factor : NUMBER ;
    public final void factor() throws RecognitionException {
        try {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:306:9: ( NUMBER )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:306:11: NUMBER
            {
            match(input,NUMBER,FOLLOW_NUMBER_in_factor1822); if (failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end factor

    // $ANTLR start synpred1
    public final void synpred1_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:14: ( table_ref ( ',' table_ref )+ )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:16: table_ref ( ',' table_ref )+
        {
        pushFollow(FOLLOW_table_ref_in_synpred1789);
        table_ref();
        _fsp--;
        if (failed) return ;
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:26: ( ',' table_ref )+
        int cnt51=0;
        loop51:
        do {
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==88) ) {
                alt51=1;
            }


            switch (alt51) {
        	case 1 :
        	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:173:27: ',' table_ref
        	    {
        	    match(input,88,FOLLOW_88_in_synpred1792); if (failed) return ;
        	    pushFollow(FOLLOW_table_ref_in_synpred1794);
        	    table_ref();
        	    _fsp--;
        	    if (failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt51 >= 1 ) break loop51;
        	    if (backtracking>0) {failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(51, input);
                    throw eee;
            }
            cnt51++;
        } while (true);


        }
    }
    // $ANTLR end synpred1

    // $ANTLR start synpred2
    public final void synpred2_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:174:5: ( table_ref ( table_join )+ )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:174:7: table_ref ( table_join )+
        {
        pushFollow(FOLLOW_table_ref_in_synpred2810);
        table_ref();
        _fsp--;
        if (failed) return ;
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:174:17: ( table_join )+
        int cnt52=0;
        loop52:
        do {
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( ((LA52_0>=CROSS && LA52_0<=INNER)||LA52_0==86) ) {
                alt52=1;
            }


            switch (alt52) {
        	case 1 :
        	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:174:17: table_join
        	    {
        	    pushFollow(FOLLOW_table_join_in_synpred2812);
        	    table_join();
        	    _fsp--;
        	    if (failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt52 >= 1 ) break loop52;
        	    if (backtracking>0) {failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(52, input);
                    throw eee;
            }
            cnt52++;
        } while (true);


        }
    }
    // $ANTLR end synpred2

    // $ANTLR start synpred3
    public final void synpred3_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:175:5: ( table_ref )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:175:7: table_ref
        {
        pushFollow(FOLLOW_table_ref_in_synpred3826);
        table_ref();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred3

    // $ANTLR start synpred4
    public final void synpred4_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:216:14: ( alias_clause )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:216:15: alias_clause
        {
        pushFollow(FOLLOW_alias_clause_in_synpred41096);
        alias_clause();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred4

    // $ANTLR start synpred5
    public final void synpred5_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:221:5: ( id ( '(' name_list ')' )? )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:221:6: id ( '(' name_list ')' )?
        {
        pushFollow(FOLLOW_id_in_synpred51145);
        id();
        _fsp--;
        if (failed) return ;
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:221:9: ( '(' name_list ')' )?
        int alt53=2;
        int LA53_0 = input.LA(1);

        if ( (LA53_0==86) ) {
            alt53=1;
        }
        switch (alt53) {
            case 1 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:221:10: '(' name_list ')'
                {
                match(input,86,FOLLOW_86_in_synpred51148); if (failed) return ;
                pushFollow(FOLLOW_name_list_in_synpred51150);
                name_list();
                _fsp--;
                if (failed) return ;
                match(input,87,FOLLOW_87_in_synpred51152); if (failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred5

    // $ANTLR start synpred6
    public final void synpred6_fragment() throws RecognitionException {   
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:269:5: ( select_with_parens )
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:269:6: select_with_parens
        {
        pushFollow(FOLLOW_select_with_parens_in_synpred61584);
        select_with_parens();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred6

    public final boolean synpred5() {
        backtracking++;
        int start = input.mark();
        try {
            synpred5_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred6() {
        backtracking++;
        int start = input.mark();
        try {
            synpred6_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred1() {
        backtracking++;
        int start = input.mark();
        try {
            synpred1_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred2() {
        backtracking++;
        int start = input.mark();
        try {
            synpred2_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred3() {
        backtracking++;
        int start = input.mark();
        try {
            synpred3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred4() {
        backtracking++;
        int start = input.mark();
        try {
            synpred4_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }


    protected DFA23 dfa23 = new DFA23(this);
    static final String DFA23_eotS =
        "\u0084\uffff";
    static final String DFA23_eofS =
        "\1\uffff\2\21\4\uffff\1\21\1\uffff\1\21\23\uffff\2\21\4\uffff\3"+
        "\21\4\uffff\1\21\131\uffff";
    static final String DFA23_minS =
        "\1\35\2\6\1\35\1\126\1\20\1\6\1\26\1\116\1\40\22\uffff\1\35\2\12"+
        "\1\6\1\0\1\6\1\126\2\6\2\40\2\12\1\6\1\26\2\4\1\0\3\126\2\4\3\35"+
        "\1\127\1\6\15\0\7\uffff\70\0";
    static final String DFA23_maxS =
        "\1\126\2\130\3\126\1\116\1\130\1\116\1\130\22\uffff\1\116\2\130"+
        "\1\127\1\0\2\126\3\130\1\116\2\127\1\116\3\130\1\0\3\126\2\130\3"+
        "\126\1\127\1\126\15\0\7\uffff\70\0";
    static final String DFA23_acceptS =
        "\12\uffff\11\3\10\2\1\1\51\uffff\7\2\70\uffff";
    static final String DFA23_specialS =
        "\1\uffff\1\3\1\13\4\uffff\1\7\1\uffff\1\4\23\uffff\1\12\1\14\1\uffff"+
        "\1\5\2\uffff\1\10\1\0\1\11\1\2\3\uffff\1\1\2\uffff\1\6\126\uffff}>";
    static final String[] DFA23_transitionS = {
            "\2\2\12\uffff\1\3\1\uffff\17\4\24\uffff\1\1\7\uffff\1\5",
            "\1\7\3\uffff\1\6\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24"+
            "\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff"+
            "\1\16\1\uffff\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\1\7\3\uffff\1\6\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24"+
            "\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff"+
            "\1\16\1\uffff\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\2\36\57\uffff\1\35\7\uffff\1\34",
            "\1\37",
            "\1\41\1\42\104\uffff\1\40",
            "\1\43\107\uffff\1\44",
            "\1\10\11\uffff\1\23\1\30\1\31\1\24\1\25\1\26\1\27\35\uffff\1"+
            "\12\1\13\1\uffff\1\14\1\15\2\uffff\1\16\1\uffff\1\17\1\11\6"+
            "\uffff\1\20\1\32\1\22\1\33",
            "\1\45",
            "\1\23\1\30\1\31\1\24\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff"+
            "\1\14\1\15\2\uffff\1\16\1\uffff\1\17\7\uffff\1\20\1\46\1\22"+
            "\1\33",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\50\57\uffff\1\47",
            "\1\51\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24\1\25\1\26\1"+
            "\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff\1\16\1\uffff"+
            "\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\1\51\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24\1\25\1\26\1"+
            "\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff\1\16\1\uffff"+
            "\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\1\66\14\uffff\1\65\1\uffff\1\64\7\uffff\2\54\13\uffff\1\63"+
            "\17\56\7\uffff\1\57\1\60\1\62\10\uffff\1\61\1\uffff\1\53\7\uffff"+
            "\1\55\1\52",
            "\1\uffff",
            "\1\101\14\uffff\1\67\1\uffff\1\70\7\uffff\2\72\14\uffff\17\74"+
            "\7\uffff\1\75\1\76\1\100\10\uffff\1\77\1\uffff\1\71\7\uffff"+
            "\1\73",
            "\1\102",
            "\1\7\3\uffff\1\103\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24"+
            "\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff"+
            "\1\16\1\uffff\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\1\7\3\uffff\1\103\13\uffff\1\10\11\uffff\1\23\1\30\1\31\1\24"+
            "\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff\1\14\1\15\2\uffff"+
            "\1\16\1\uffff\1\17\1\11\6\uffff\1\20\1\32\1\22\1\33",
            "\1\23\1\30\1\31\1\24\1\25\1\26\1\27\35\uffff\1\12\1\13\1\uffff"+
            "\1\14\1\15\2\uffff\1\16\1\uffff\1\17\7\uffff\1\20\1\104\1\22"+
            "\1\33",
            "\1\105\1\112\1\113\1\106\1\107\1\110\1\111\47\uffff\1\114",
            "\1\115\114\uffff\1\116",
            "\1\115\114\uffff\1\116",
            "\1\117\107\uffff\1\120",
            "\1\121\11\uffff\1\23\1\30\1\31\1\24\1\25\1\26\1\27\35\uffff"+
            "\1\12\1\13\1\uffff\1\14\1\15\2\uffff\1\16\1\uffff\1\17\1\122"+
            "\6\uffff\1\20\1\32\1\22\1\33",
            "\1\125\1\127\1\124\1\126\1\131\1\132\1\123\1\130\1\135\1\136"+
            "\1\133\1\134\52\uffff\1\137\1\140\1\141\1\142\1\143\1\144\27"+
            "\uffff\1\146\1\145",
            "\1\125\1\127\1\124\1\126\1\131\1\132\1\123\1\130\1\135\1\136"+
            "\1\133\1\134\52\uffff\1\137\1\140\1\141\1\142\1\143\1\144\27"+
            "\uffff\1\146\1\145",
            "\1\uffff",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\125\1\127\1\124\1\126\1\131\1\132\1\uffff\1\130\1\135\1\136"+
            "\1\133\1\134\52\uffff\1\137\1\140\1\141\1\142\1\143\1\144\27"+
            "\uffff\1\146\1\145",
            "\1\125\1\127\1\124\1\126\1\131\1\132\1\uffff\1\130\1\135\1\136"+
            "\1\133\1\134\52\uffff\1\137\1\140\1\141\1\142\1\143\1\144\27"+
            "\uffff\1\146\1\145",
            "\2\153\14\uffff\17\155\7\uffff\1\156\1\157\1\161\10\uffff\1"+
            "\160\1\uffff\1\152\7\uffff\1\154",
            "\2\163\14\uffff\17\165\7\uffff\1\166\1\167\1\171\10\uffff\1"+
            "\170\1\uffff\1\162\7\uffff\1\164",
            "\2\173\14\uffff\17\175\7\uffff\1\176\1\177\1\u0081\10\uffff"+
            "\1\u0080\1\uffff\1\172\7\uffff\1\174",
            "\1\u0082",
            "\1\101\15\uffff\1\u0083\10\uffff\2\72\14\uffff\17\74\7\uffff"+
            "\1\75\1\76\1\100\10\uffff\1\77\1\uffff\1\71\7\uffff\1\73",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "173:1: from_list : ( ( table_ref ( ',' table_ref )+ )=> refs_list | ( table_ref ( table_join )+ )=> table_joins | ( table_ref )=> table_ref );";
        }
        public int specialStateTransition(int s, IntStream input) throws NoViableAltException {
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_36 = input.LA(1);

                         
                        int index23_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_36==DOT) ) {s = 67;}

                        else if ( (LA23_36==STAR) ) {s = 7;}

                        else if ( (LA23_36==AS) ) {s = 8;}

                        else if ( (LA23_36==ID) ) {s = 9;}

                        else if ( (LA23_36==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_36==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_36==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_36==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_36==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_36==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_36==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_36==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_36==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_36==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_36==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_36==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_36==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_36==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_36==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_36==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_36==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_36==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_36);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA23_42 = input.LA(1);

                         
                        int index23_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_42==AS) ) {s = 81;}

                        else if ( (LA23_42==ID) ) {s = 82;}

                        else if ( (LA23_42==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_42==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_42==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_42==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_42==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_42==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_42==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_42==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_42==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_42==88) && (synpred1())) {s = 27;}

                        else if ( (LA23_42==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_42==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_42==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_42==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_42==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_42==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_42==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_42==86) && (synpred2())) {s = 26;}

                         
                        input.seek(index23_42);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA23_38 = input.LA(1);

                         
                        int index23_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_38==CROSS) && (synpred2())) {s = 69;}

                        else if ( (LA23_38==FULL) && (synpred2())) {s = 70;}

                        else if ( (LA23_38==LEFT) && (synpred2())) {s = 71;}

                        else if ( (LA23_38==RIGHT) && (synpred2())) {s = 72;}

                        else if ( (LA23_38==INNER) && (synpred2())) {s = 73;}

                        else if ( (LA23_38==JOIN) && (synpred2())) {s = 74;}

                        else if ( (LA23_38==NATURAL) && (synpred2())) {s = 75;}

                        else if ( (LA23_38==ID) ) {s = 76;}

                         
                        input.seek(index23_38);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA23_1 = input.LA(1);

                         
                        int index23_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_1==DOT) ) {s = 6;}

                        else if ( (LA23_1==STAR) ) {s = 7;}

                        else if ( (LA23_1==AS) ) {s = 8;}

                        else if ( (LA23_1==ID) ) {s = 9;}

                        else if ( (LA23_1==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_1==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_1==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_1==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_1==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_1==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_1==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_1==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_1==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_1==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_1==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_1==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_1==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_1==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_1==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_1==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_1==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_1==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_1);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA23_9 = input.LA(1);

                         
                        int index23_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_9==88) && (synpred1())) {s = 27;}

                        else if ( (LA23_9==86) ) {s = 38;}

                        else if ( (LA23_9==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_9==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_9==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_9==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_9==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_9==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_9==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_9==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_9==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_9==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_9==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_9==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_9==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_9==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_9==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_9==NATURAL) && (synpred2())) {s = 25;}

                         
                        input.seek(index23_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA23_32 = input.LA(1);

                         
                        int index23_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1()) ) {s = 27;}

                        else if ( (synpred2()) ) {s = 26;}

                        else if ( (synpred3()) ) {s = 18;}

                         
                        input.seek(index23_32);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA23_45 = input.LA(1);

                         
                        int index23_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1()) ) {s = 27;}

                        else if ( (synpred2()) ) {s = 75;}

                        else if ( (synpred3()) ) {s = 18;}

                         
                        input.seek(index23_45);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA23_7 = input.LA(1);

                         
                        int index23_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_7==AS) ) {s = 8;}

                        else if ( (LA23_7==ID) ) {s = 9;}

                        else if ( (LA23_7==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_7==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_7==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_7==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_7==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_7==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_7==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_7==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_7==88) && (synpred1())) {s = 27;}

                        else if ( (LA23_7==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_7==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_7==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_7==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_7==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_7==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_7==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_7==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_7==87) && (synpred3())) {s = 18;}

                         
                        input.seek(index23_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA23_35 = input.LA(1);

                         
                        int index23_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_35==DOT) ) {s = 67;}

                        else if ( (LA23_35==STAR) ) {s = 7;}

                        else if ( (LA23_35==AS) ) {s = 8;}

                        else if ( (LA23_35==ID) ) {s = 9;}

                        else if ( (LA23_35==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_35==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_35==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_35==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_35==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_35==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_35==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_35==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_35==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_35==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_35==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_35==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_35==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_35==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_35==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_35==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_35==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_35==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_35);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA23_37 = input.LA(1);

                         
                        int index23_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_37==86) ) {s = 68;}

                        else if ( (LA23_37==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_37==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_37==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_37==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_37==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_37==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_37==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_37==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_37==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_37==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_37==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_37==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_37==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_37==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_37==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_37==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_37==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_37);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA23_29 = input.LA(1);

                         
                        int index23_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_29==DOT) ) {s = 41;}

                        else if ( (LA23_29==AS) ) {s = 8;}

                        else if ( (LA23_29==ID) ) {s = 9;}

                        else if ( (LA23_29==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_29==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_29==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_29==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_29==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_29==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_29==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_29==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_29==88) && (synpred1())) {s = 27;}

                        else if ( (LA23_29==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_29==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_29==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_29==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_29==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_29==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_29==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_29==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_29==87) && (synpred3())) {s = 18;}

                         
                        input.seek(index23_29);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA23_2 = input.LA(1);

                         
                        int index23_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_2==DOT) ) {s = 6;}

                        else if ( (LA23_2==STAR) ) {s = 7;}

                        else if ( (LA23_2==AS) ) {s = 8;}

                        else if ( (LA23_2==ID) ) {s = 9;}

                        else if ( (LA23_2==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_2==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_2==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_2==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_2==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_2==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_2==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_2==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_2==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_2==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_2==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_2==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_2==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_2==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_2==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_2==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_2==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_2==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_2);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA23_30 = input.LA(1);

                         
                        int index23_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_30==DOT) ) {s = 41;}

                        else if ( (LA23_30==AS) ) {s = 8;}

                        else if ( (LA23_30==ID) ) {s = 9;}

                        else if ( (LA23_30==CROSS) && (synpred2())) {s = 19;}

                        else if ( (LA23_30==FULL) && (synpred2())) {s = 20;}

                        else if ( (LA23_30==LEFT) && (synpred2())) {s = 21;}

                        else if ( (LA23_30==RIGHT) && (synpred2())) {s = 22;}

                        else if ( (LA23_30==INNER) && (synpred2())) {s = 23;}

                        else if ( (LA23_30==JOIN) && (synpred2())) {s = 24;}

                        else if ( (LA23_30==NATURAL) && (synpred2())) {s = 25;}

                        else if ( (LA23_30==86) && (synpred2())) {s = 26;}

                        else if ( (LA23_30==WHERE) && (synpred3())) {s = 10;}

                        else if ( (LA23_30==GROUP) && (synpred3())) {s = 11;}

                        else if ( (LA23_30==HAVING) && (synpred3())) {s = 12;}

                        else if ( (LA23_30==ORDER) && (synpred3())) {s = 13;}

                        else if ( (LA23_30==LIMIT) && (synpred3())) {s = 14;}

                        else if ( (LA23_30==OFFSET) && (synpred3())) {s = 15;}

                        else if ( (LA23_30==85) && (synpred3())) {s = 16;}

                        else if ( (LA23_30==EOF) && (synpred3())) {s = 17;}

                        else if ( (LA23_30==87) && (synpred3())) {s = 18;}

                        else if ( (LA23_30==88) && (synpred1())) {s = 27;}

                         
                        input.seek(index23_30);
                        if ( s>=0 ) return s;
                        break;
            }
            if (backtracking>0) {failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_stmtmulti_in_stmtblock180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_in_stmtmulti193 = new BitSet(new long[]{0x0000000000000002L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_stmtmulti197 = new BitSet(new long[]{0x0000000000030000L,0x0000000000400000L});
    public static final BitSet FOLLOW_select_in_stmtmulti199 = new BitSet(new long[]{0x0000000000000002L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_stmtmulti204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_no_parens_in_select221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_select228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_select_with_parens240 = new BitSet(new long[]{0x0000000000030000L});
    public static final BitSet FOLLOW_select_no_parens_in_select_with_parens242 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_select_with_parens244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_select_with_parens252 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_select_with_parens_in_select_with_parens254 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_select_with_parens256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_select_no_parens268 = new BitSet(new long[]{0x03FFF80060280040L,0x000000000040500EL});
    public static final BitSet FOLLOW_opt_distinct_in_select_no_parens270 = new BitSet(new long[]{0x03FFF80060000040L,0x000000000040500EL});
    public static final BitSet FOLLOW_target_list_in_select_no_parens272 = new BitSet(new long[]{0x0000000080800002L,0x00000000000029B0L});
    public static final BitSet FOLLOW_into_clause_in_select_no_parens274 = new BitSet(new long[]{0x0000000080000002L,0x00000000000029B0L});
    public static final BitSet FOLLOW_from_clause_in_select_no_parens276 = new BitSet(new long[]{0x0000000000000002L,0x00000000000029B0L});
    public static final BitSet FOLLOW_where_clause_in_select_no_parens278 = new BitSet(new long[]{0x0000000000000002L,0x00000000000029A0L});
    public static final BitSet FOLLOW_groupby_clause_in_select_no_parens280 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002980L});
    public static final BitSet FOLLOW_having_clause_in_select_no_parens282 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002900L});
    public static final BitSet FOLLOW_orderby_clause_in_select_no_parens284 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002800L});
    public static final BitSet FOLLOW_opt_limit_in_select_no_parens286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_values_clause_in_select_no_parens293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_values_clause308 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_values_clause310 = new BitSet(new long[]{0x03FFF80060040000L,0x000000000040500EL});
    public static final BitSet FOLLOW_values_expr_list_in_values_clause312 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_values_clause314 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_values_clause317 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_values_clause319 = new BitSet(new long[]{0x03FFF80060040000L,0x000000000040500EL});
    public static final BitSet FOLLOW_values_expr_list_in_values_clause321 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_values_clause323 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_values_expr_in_values_expr_list339 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_values_expr_list343 = new BitSet(new long[]{0x03FFF80060040000L,0x000000000040500EL});
    public static final BitSet FOLLOW_values_expr_in_values_expr_list345 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_a_expr_in_values_expr358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_values_expr364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_opt_distinct379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_opt_distinct386 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_ON_in_opt_distinct388 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_opt_distinct390 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_expr_list_in_opt_distinct392 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_opt_distinct394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_opt_distinct401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_a_expr_in_expr_list425 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_expr_list429 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_expr_list431 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_target_in_target_list453 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_target_list457 = new BitSet(new long[]{0x03FFF80060000040L,0x000000000040500EL});
    public static final BitSet FOLLOW_target_in_target_list459 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_a_expr_in_target474 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_AS_in_target478 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_id_in_target480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_target487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTO_in_into_clause502 = new BitSet(new long[]{0x000000007F000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_temp_table_name_in_into_clause504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name524 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name526 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name536 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name538 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_in_opt_temp_table_name548 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name550 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name552 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_in_opt_temp_table_name562 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name564 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name566 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GLOBAL_in_opt_temp_table_name575 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name577 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name579 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GLOBAL_in_opt_temp_table_name589 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name591 = new BitSet(new long[]{0x0000000070000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name593 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TABLE_in_opt_temp_table_name603 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TABLE_in_opt_table629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_name_in_qualified_name646 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_indirections_in_qualified_name648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_relation_name661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_special_rule_relation_in_relation_name669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_special_rule_relation0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indirection_in_indirections704 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_indirection_in_indirections708 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_DOT_in_indirection722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_id_in_indirection724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_indirection731 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_STAR_in_indirection733 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_name_in_columnref747 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_indirections_in_columnref749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause764 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_from_list_in_from_clause766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_refs_list_in_from_list802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_joins_in_from_list818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_ref_in_from_list832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_ref_in_refs_list847 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_refs_list850 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_refs_list852 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_table_ref_in_table_joins870 = new BitSet(new long[]{0x0000007F00000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_table_join_in_table_joins872 = new BitSet(new long[]{0x0000007F00000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_relation_expr_in_table_ref887 = new BitSet(new long[]{0x0000000000400002L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_alias_in_table_ref889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_expr_in_table_ref895 = new BitSet(new long[]{0x0000000000400002L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_alias_in_table_ref897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_table_ref903 = new BitSet(new long[]{0x0000000000400002L,0x0000000000004000L});
    public static final BitSet FOLLOW_opt_alias_in_table_ref905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joined_table_in_table_join918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_table_join924 = new BitSet(new long[]{0x0000007F00000000L});
    public static final BitSet FOLLOW_joined_table_in_table_join926 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_table_join928 = new BitSet(new long[]{0x0000000000400000L,0x0000000000004000L});
    public static final BitSet FOLLOW_alias_clause_in_table_join930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CROSS_in_joined_table943 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table945 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table947 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_type_in_joined_table953 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table955 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table957 = new BitSet(new long[]{0x0000010000100000L});
    public static final BitSet FOLLOW_join_qual_in_joined_table959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JOIN_in_joined_table965 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table967 = new BitSet(new long[]{0x0000010000100000L});
    public static final BitSet FOLLOW_join_qual_in_joined_table969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NATURAL_in_joined_table975 = new BitSet(new long[]{0x0000007800000000L});
    public static final BitSet FOLLOW_join_type_in_joined_table977 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table979 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NATURAL_in_joined_table987 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table989 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FULL_in_join_type1006 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_join_type1015 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHT_in_join_type1024 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1026 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_join_type1033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_outer1049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_join_qual1066 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_join_qual1068 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_name_list_in_join_qual1070 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_join_qual1072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_qual1079 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_join_qual1081 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alias_clause_in_opt_alias1101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_alias_clause1127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_id_in_alias_clause1129 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_alias_clause1132 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_name_list_in_alias_clause1134 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_alias_clause1136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_names_in_alias_clause1159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_names1174 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_names1176 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_name_list_in_names1178 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_names1180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_names1186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_name_list1202 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_name_list1206 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_id_in_name_list1208 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1224 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_STAR_in_relation_expr1226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONLY_in_relation_expr1234 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONLY_in_relation_expr1244 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_relation_expr1246 = new BitSet(new long[]{0x0000000060000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1248 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_relation_expr1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_name_in_func_expr1265 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_func_expr1267 = new BitSet(new long[]{0x03FFFC0060280040L,0x0000000000C0500EL});
    public static final BitSet FOLLOW_87_in_func_expr1274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_list_in_func_expr1281 = new BitSet(new long[]{0x0000000000000000L,0x0000000001800000L});
    public static final BitSet FOLLOW_87_in_func_expr1285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_88_in_func_expr1289 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_VARIADIC_in_func_expr1291 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_func_expr1293 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_func_expr1295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARIADIC_in_func_expr1302 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_func_expr1304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_func_expr1306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_func_expr1313 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_expr_list_in_func_expr1315 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_func_expr1317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_func_expr1324 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_expr_list_in_func_expr1326 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_func_expr1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_func_expr1335 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_func_expr1337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_func_name0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_c_expr_in_a_expr1429 = new BitSet(new long[]{0xFC0000000000FBF2L});
    public static final BitSet FOLLOW_binary_op_in_a_expr1433 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_c_expr_in_a_expr1435 = new BitSet(new long[]{0xFC0000000000FBF2L});
    public static final BitSet FOLLOW_STAR_in_binary_op1454 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_binary_op1458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DIV_in_binary_op1462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_binary_op1466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PCT_in_binary_op1470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_binary_op1474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_binary_op1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GE_in_binary_op1482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LE_in_binary_op1486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQ_in_binary_op1490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HAT_in_binary_op1494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_binary_op1498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_binary_op1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIKE_in_binary_op1506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_binary_op1510 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_LIKE_in_binary_op1512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ILIKE_in_binary_op1516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_binary_op1520 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_ILIKE_in_binary_op1522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIMILAR_in_binary_op1526 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_TO_in_binary_op1528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_binary_op1532 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_SIMILAR_in_binary_op1534 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_TO_in_binary_op1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnref_in_c_expr1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_c_expr1566 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_c_expr1568 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_c_expr1570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_expr_in_c_expr1576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_c_expr1589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_c_expr1595 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_select_with_parens_in_c_expr1597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_c_expr1604 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_select_with_parens_in_c_expr1606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_factor_in_c_expr1613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTEDSTRING_in_c_expr1619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause1631 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_where_clause1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GROUP_in_groupby_clause1651 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_BY_in_groupby_clause1653 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_expr_list_in_groupby_clause1655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HAVING_in_having_clause1674 = new BitSet(new long[]{0x03FFF80060000000L,0x000000000040500EL});
    public static final BitSet FOLLOW_a_expr_in_having_clause1676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderby_clause1694 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_BY_in_orderby_clause1696 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_set_in_orderby_clause1698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_limit_in_opt_limit1722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit1742 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1744 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_OFFSET_in_limit1746 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1748 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OFFSET_in_limit1755 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1757 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_LIMIT_in_limit1759 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit1767 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OFFSET_in_limit1776 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit1784 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1786 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_limit1788 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_limit1790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_id1808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_factor1822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_ref_in_synpred1789 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_synpred1792 = new BitSet(new long[]{0x03FFFA0060000000L,0x0000000000404000L});
    public static final BitSet FOLLOW_table_ref_in_synpred1794 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_table_ref_in_synpred2810 = new BitSet(new long[]{0x0000007F00000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_table_join_in_synpred2812 = new BitSet(new long[]{0x0000007F00000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_table_ref_in_synpred3826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alias_clause_in_synpred41096 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_synpred51145 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_synpred51148 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_name_list_in_synpred51150 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_synpred51152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_synpred61584 = new BitSet(new long[]{0x0000000000000002L});

}