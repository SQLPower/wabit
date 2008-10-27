// $ANTLR 3.0.1 /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g 2008-10-27 12:13:46

package ca.sqlpower.wabit.sql.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class SQLANTLRParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PLUS", "MINUS", "STAR", "DIV", "GT", "LT", "DOT", "PCT", "EQ", "HAT", "GE", "LE", "LPAREN", "RPAREN", "ALIAS", "RELATION", "RELATION_NAME", "INDIRECTION", "EXPR", "FUNCTION", "FROM_START", "SELECT_START", "TARGET", "TARGETS", "NULL_ARG", "SELECT", "VALUES", "DEFAULT", "DISTINCT", "ON", "ALL", "AS", "INTO", "TEMPORARY", "TEMP", "LOCAL", "GLOBAL", "TABLE", "OLD", "NEW", "FROM", "CROSS", "JOIN", "NATURAL", "FULL", "LEFT", "RIGHT", "INNER", "OUTER", "USING", "ONLY", "OR", "AND", "NOT", "LIKE", "ILIKE", "SIMILAR", "TO", "NOTNULL", "IS", "NULL", "ISNULL", "EXISTS", "ARRAY", "QUOTEDSTRING", "WHERE", "GROUP", "BY", "HAVING", "ORDER", "DESC", "ASC", "LIMIT", "NUMBER", "OFFSET", "ID", "LEN", "VARIADIC", "WHITESPACE", "QUOTEID", "DIGIT", "LETTER", "SL_COMMENT", "ML_COMMENT", "';'", "','"
    };
    public static final int EXISTS=66;
    public static final int MINUS=5;
    public static final int AS=35;
    public static final int ARRAY=67;
    public static final int USING=53;
    public static final int INTO=36;
    public static final int EXPR=22;
    public static final int VARIADIC=81;
    public static final int NUMBER=77;
    public static final int TARGET=26;
    public static final int TARGETS=27;
    public static final int NATURAL=47;
    public static final int VALUES=30;
    public static final int INNER=51;
    public static final int OLD=42;
    public static final int QUOTEID=83;
    public static final int ALIAS=18;
    public static final int OR=55;
    public static final int ON=33;
    public static final int DOT=10;
    public static final int FULL=48;
    public static final int ORDER=73;
    public static final int PCT=11;
    public static final int TO=61;
    public static final int AND=56;
    public static final int SIMILAR=60;
    public static final int BY=71;
    public static final int RELATION_NAME=20;
    public static final int FUNCTION=23;
    public static final int LOCAL=39;
    public static final int RIGHT=50;
    public static final int GLOBAL=40;
    public static final int SELECT=29;
    public static final int GROUP=70;
    public static final int LE=15;
    public static final int RPAREN=17;
    public static final int DESC=74;
    public static final int LPAREN=16;
    public static final int ML_COMMENT=87;
    public static final int DIGIT=84;
    public static final int LEFT=49;
    public static final int PLUS=4;
    public static final int LEN=80;
    public static final int JOIN=46;
    public static final int SL_COMMENT=86;
    public static final int QUOTEDSTRING=68;
    public static final int TEMPORARY=37;
    public static final int OUTER=52;
    public static final int ID=79;
    public static final int FROM=44;
    public static final int NULL_ARG=28;
    public static final int DISTINCT=32;
    public static final int LETTER=85;
    public static final int SELECT_START=25;
    public static final int IS=63;
    public static final int ONLY=54;
    public static final int EQ=12;
    public static final int NEW=43;
    public static final int OFFSET=78;
    public static final int NOTNULL=62;
    public static final int LT=9;
    public static final int RELATION=19;
    public static final int GT=8;
    public static final int ASC=75;
    public static final int LIKE=58;
    public static final int INDIRECTION=21;
    public static final int WHITESPACE=82;
    public static final int GE=14;
    public static final int ILIKE=59;
    public static final int FROM_START=24;
    public static final int TABLE=41;
    public static final int HAT=13;
    public static final int TEMP=38;
    public static final int EOF=-1;
    public static final int NULL=64;
    public static final int CROSS=45;
    public static final int ISNULL=65;
    public static final int DEFAULT=31;
    public static final int DIV=7;
    public static final int HAVING=72;
    public static final int STAR=6;
    public static final int ALL=34;
    public static final int NOT=57;
    public static final int LIMIT=76;
    public static final int WHERE=69;

        public SQLANTLRParser(TokenStream input) {
            super(input);
            ruleMemo = new HashMap[67+1];
         }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "/Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g"; }

     
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


    public static class stmtblock_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start stmtblock
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:81:1: stmtblock : stmtmulti ;
    public final stmtblock_return stmtblock() throws RecognitionException {
        stmtblock_return retval = new stmtblock_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        stmtmulti_return stmtmulti1 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:81:11: ( stmtmulti )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:81:13: stmtmulti
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_stmtmulti_in_stmtblock260);
            stmtmulti1=stmtmulti();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, stmtmulti1.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end stmtblock

    public static class stmtmulti_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start stmtmulti
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:1: stmtmulti : ( simple_select ) ( ';' simple_select )* ( ';' )? ;
    public final stmtmulti_return stmtmulti() throws RecognitionException {
        stmtmulti_return retval = new stmtmulti_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal3=null;
        Token char_literal5=null;
        simple_select_return simple_select2 = null;

        simple_select_return simple_select4 = null;


        Object char_literal3_tree=null;
        Object char_literal5_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:11: ( ( simple_select ) ( ';' simple_select )* ( ';' )? )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:13: ( simple_select ) ( ';' simple_select )* ( ';' )?
            {
            root_0 = (Object)adaptor.nil();

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:13: ( simple_select )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:14: simple_select
            {
            pushFollow(FOLLOW_simple_select_in_stmtmulti272);
            simple_select2=simple_select();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simple_select2.getTree());

            }

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:29: ( ';' simple_select )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==88) ) {
                    int LA1_1 = input.LA(2);

                    if ( ((LA1_1>=SELECT && LA1_1<=VALUES)) ) {
                        alt1=1;
                    }


                }


                switch (alt1) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:30: ';' simple_select
            	    {
            	    char_literal3=(Token)input.LT(1);
            	    match(input,88,FOLLOW_88_in_stmtmulti276); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal3_tree = (Object)adaptor.create(char_literal3);
            	    adaptor.addChild(root_0, char_literal3_tree);
            	    }
            	    pushFollow(FOLLOW_simple_select_in_stmtmulti278);
            	    simple_select4=simple_select();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, simple_select4.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:50: ( ';' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==88) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:84:51: ';'
                    {
                    char_literal5=(Token)input.LT(1);
                    match(input,88,FOLLOW_88_in_stmtmulti283); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal5_tree = (Object)adaptor.create(char_literal5);
                    adaptor.addChild(root_0, char_literal5_tree);
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end stmtmulti

    public static class select_with_parens_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start select_with_parens
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:88:1: select_with_parens : LPAREN simple_select RPAREN ;
    public final select_with_parens_return select_with_parens() throws RecognitionException {
        select_with_parens_return retval = new select_with_parens_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN6=null;
        Token RPAREN8=null;
        simple_select_return simple_select7 = null;


        Object LPAREN6_tree=null;
        Object RPAREN8_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:88:20: ( LPAREN simple_select RPAREN )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:88:22: LPAREN simple_select RPAREN
            {
            root_0 = (Object)adaptor.nil();

            LPAREN6=(Token)input.LT(1);
            match(input,LPAREN,FOLLOW_LPAREN_in_select_with_parens297); if (failed) return retval;
            if ( backtracking==0 ) {
            LPAREN6_tree = (Object)adaptor.create(LPAREN6);
            adaptor.addChild(root_0, LPAREN6_tree);
            }
            pushFollow(FOLLOW_simple_select_in_select_with_parens299);
            simple_select7=simple_select();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simple_select7.getTree());
            RPAREN8=(Token)input.LT(1);
            match(input,RPAREN,FOLLOW_RPAREN_in_select_with_parens301); if (failed) return retval;
            if ( backtracking==0 ) {
            RPAREN8_tree = (Object)adaptor.create(RPAREN8);
            adaptor.addChild(root_0, RPAREN8_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end select_with_parens

    public static class simple_select_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start simple_select
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:91:1: simple_select : ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause -> ^( SELECT[\"SELECT\"] opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause ) | values_clause );
    public final simple_select_return simple_select() throws RecognitionException {
        simple_select_return retval = new simple_select_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SELECT9=null;
        opt_distinct_return opt_distinct10 = null;

        target_list_return target_list11 = null;

        into_clause_return into_clause12 = null;

        from_clause_return from_clause13 = null;

        where_clause_return where_clause14 = null;

        groupby_clause_return groupby_clause15 = null;

        having_clause_return having_clause16 = null;

        orderby_clause_return orderby_clause17 = null;

        values_clause_return values_clause18 = null;


        Object SELECT9_tree=null;
        RewriteRuleTokenStream stream_SELECT=new RewriteRuleTokenStream(adaptor,"token SELECT");
        RewriteRuleSubtreeStream stream_groupby_clause=new RewriteRuleSubtreeStream(adaptor,"rule groupby_clause");
        RewriteRuleSubtreeStream stream_orderby_clause=new RewriteRuleSubtreeStream(adaptor,"rule orderby_clause");
        RewriteRuleSubtreeStream stream_having_clause=new RewriteRuleSubtreeStream(adaptor,"rule having_clause");
        RewriteRuleSubtreeStream stream_where_clause=new RewriteRuleSubtreeStream(adaptor,"rule where_clause");
        RewriteRuleSubtreeStream stream_opt_distinct=new RewriteRuleSubtreeStream(adaptor,"rule opt_distinct");
        RewriteRuleSubtreeStream stream_target_list=new RewriteRuleSubtreeStream(adaptor,"rule target_list");
        RewriteRuleSubtreeStream stream_from_clause=new RewriteRuleSubtreeStream(adaptor,"rule from_clause");
        RewriteRuleSubtreeStream stream_into_clause=new RewriteRuleSubtreeStream(adaptor,"rule into_clause");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:91:14: ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause -> ^( SELECT[\"SELECT\"] opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause ) | values_clause )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==SELECT) ) {
                alt3=1;
            }
            else if ( (LA3_0==VALUES) ) {
                alt3=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("91:1: simple_select : ( SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause -> ^( SELECT[\"SELECT\"] opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause ) | values_clause );", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:91:16: SELECT opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause
                    {
                    SELECT9=(Token)input.LT(1);
                    match(input,SELECT,FOLLOW_SELECT_in_simple_select312); if (failed) return retval;
                    if ( backtracking==0 ) stream_SELECT.add(SELECT9);

                    pushFollow(FOLLOW_opt_distinct_in_simple_select314);
                    opt_distinct10=opt_distinct();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_opt_distinct.add(opt_distinct10.getTree());
                    pushFollow(FOLLOW_target_list_in_simple_select316);
                    target_list11=target_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_target_list.add(target_list11.getTree());
                    pushFollow(FOLLOW_into_clause_in_simple_select318);
                    into_clause12=into_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_into_clause.add(into_clause12.getTree());
                    pushFollow(FOLLOW_from_clause_in_simple_select320);
                    from_clause13=from_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_from_clause.add(from_clause13.getTree());
                    pushFollow(FOLLOW_where_clause_in_simple_select322);
                    where_clause14=where_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_where_clause.add(where_clause14.getTree());
                    pushFollow(FOLLOW_groupby_clause_in_simple_select324);
                    groupby_clause15=groupby_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_groupby_clause.add(groupby_clause15.getTree());
                    pushFollow(FOLLOW_having_clause_in_simple_select326);
                    having_clause16=having_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_having_clause.add(having_clause16.getTree());
                    pushFollow(FOLLOW_orderby_clause_in_simple_select328);
                    orderby_clause17=orderby_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_orderby_clause.add(orderby_clause17.getTree());

                    // AST REWRITE
                    // elements: groupby_clause, SELECT, from_clause, opt_distinct, having_clause, into_clause, target_list, orderby_clause, where_clause
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 92:4: -> ^( SELECT[\"SELECT\"] opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:92:7: ^( SELECT[\"SELECT\"] opt_distinct target_list into_clause from_clause where_clause groupby_clause having_clause orderby_clause )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(SELECT,"SELECT"), root_1);

                        adaptor.addChild(root_1, stream_opt_distinct.next());
                        adaptor.addChild(root_1, stream_target_list.next());
                        adaptor.addChild(root_1, stream_into_clause.next());
                        adaptor.addChild(root_1, stream_from_clause.next());
                        adaptor.addChild(root_1, stream_where_clause.next());
                        adaptor.addChild(root_1, stream_groupby_clause.next());
                        adaptor.addChild(root_1, stream_having_clause.next());
                        adaptor.addChild(root_1, stream_orderby_clause.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:93:5: values_clause
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_values_clause_in_simple_select361);
                    values_clause18=values_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, values_clause18.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end simple_select

    public static class values_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start values_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:96:1: values_clause : VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )* ;
    public final values_clause_return values_clause() throws RecognitionException {
        values_clause_return retval = new values_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token VALUES19=null;
        Token char_literal20=null;
        Token char_literal22=null;
        Token char_literal23=null;
        Token char_literal24=null;
        Token char_literal26=null;
        values_expr_list_return values_expr_list21 = null;

        values_expr_list_return values_expr_list25 = null;


        Object VALUES19_tree=null;
        Object char_literal20_tree=null;
        Object char_literal22_tree=null;
        Object char_literal23_tree=null;
        Object char_literal24_tree=null;
        Object char_literal26_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:96:16: ( VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:96:18: VALUES '(' values_expr_list ')' ( ',' '(' values_expr_list ')' )*
            {
            root_0 = (Object)adaptor.nil();

            VALUES19=(Token)input.LT(1);
            match(input,VALUES,FOLLOW_VALUES_in_values_clause376); if (failed) return retval;
            if ( backtracking==0 ) {
            VALUES19_tree = (Object)adaptor.create(VALUES19);
            adaptor.addChild(root_0, VALUES19_tree);
            }
            char_literal20=(Token)input.LT(1);
            match(input,LPAREN,FOLLOW_LPAREN_in_values_clause378); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal20_tree = (Object)adaptor.create(char_literal20);
            adaptor.addChild(root_0, char_literal20_tree);
            }
            pushFollow(FOLLOW_values_expr_list_in_values_clause380);
            values_expr_list21=values_expr_list();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, values_expr_list21.getTree());
            char_literal22=(Token)input.LT(1);
            match(input,RPAREN,FOLLOW_RPAREN_in_values_clause382); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal22_tree = (Object)adaptor.create(char_literal22);
            adaptor.addChild(root_0, char_literal22_tree);
            }
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:96:50: ( ',' '(' values_expr_list ')' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==89) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:96:51: ',' '(' values_expr_list ')'
            	    {
            	    char_literal23=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_values_clause385); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal23_tree = (Object)adaptor.create(char_literal23);
            	    adaptor.addChild(root_0, char_literal23_tree);
            	    }
            	    char_literal24=(Token)input.LT(1);
            	    match(input,LPAREN,FOLLOW_LPAREN_in_values_clause387); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal24_tree = (Object)adaptor.create(char_literal24);
            	    adaptor.addChild(root_0, char_literal24_tree);
            	    }
            	    pushFollow(FOLLOW_values_expr_list_in_values_clause389);
            	    values_expr_list25=values_expr_list();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, values_expr_list25.getTree());
            	    char_literal26=(Token)input.LT(1);
            	    match(input,RPAREN,FOLLOW_RPAREN_in_values_clause391); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal26_tree = (Object)adaptor.create(char_literal26);
            	    adaptor.addChild(root_0, char_literal26_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end values_clause

    public static class values_expr_list_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start values_expr_list
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:1: values_expr_list : ( values_expr ) ( ',' values_expr )* ;
    public final values_expr_list_return values_expr_list() throws RecognitionException {
        values_expr_list_return retval = new values_expr_list_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal28=null;
        values_expr_return values_expr27 = null;

        values_expr_return values_expr29 = null;


        Object char_literal28_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:17: ( ( values_expr ) ( ',' values_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:19: ( values_expr ) ( ',' values_expr )*
            {
            root_0 = (Object)adaptor.nil();

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:19: ( values_expr )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:20: values_expr
            {
            pushFollow(FOLLOW_values_expr_in_values_expr_list407);
            values_expr27=values_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, values_expr27.getTree());

            }

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:33: ( ',' values_expr )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==89) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:99:34: ',' values_expr
            	    {
            	    char_literal28=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_values_expr_list411); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal28_tree = (Object)adaptor.create(char_literal28);
            	    adaptor.addChild(root_0, char_literal28_tree);
            	    }
            	    pushFollow(FOLLOW_values_expr_in_values_expr_list413);
            	    values_expr29=values_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, values_expr29.getTree());

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end values_expr_list

    public static class values_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start values_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:102:1: values_expr : ( a_expr | DEFAULT );
    public final values_expr_return values_expr() throws RecognitionException {
        values_expr_return retval = new values_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DEFAULT31=null;
        a_expr_return a_expr30 = null;


        Object DEFAULT31_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:102:13: ( a_expr | DEFAULT )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LPAREN||(LA6_0>=OLD && LA6_0<=NEW)||LA6_0==NOT||(LA6_0>=EXISTS && LA6_0<=QUOTEDSTRING)||LA6_0==NUMBER||LA6_0==ID) ) {
                alt6=1;
            }
            else if ( (LA6_0==DEFAULT) ) {
                alt6=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("102:1: values_expr : ( a_expr | DEFAULT );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:102:15: a_expr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_a_expr_in_values_expr426);
                    a_expr30=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, a_expr30.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:103:5: DEFAULT
                    {
                    root_0 = (Object)adaptor.nil();

                    DEFAULT31=(Token)input.LT(1);
                    match(input,DEFAULT,FOLLOW_DEFAULT_in_values_expr432); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DEFAULT31_tree = (Object)adaptor.create(DEFAULT31);
                    adaptor.addChild(root_0, DEFAULT31_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end values_expr

    public static class opt_distinct_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start opt_distinct
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:111:1: opt_distinct : ( DISTINCT | DISTINCT ON LPAREN expr_list RPAREN | ALL | );
    public final opt_distinct_return opt_distinct() throws RecognitionException {
        opt_distinct_return retval = new opt_distinct_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DISTINCT32=null;
        Token DISTINCT33=null;
        Token ON34=null;
        Token LPAREN35=null;
        Token RPAREN37=null;
        Token ALL38=null;
        expr_list_return expr_list36 = null;


        Object DISTINCT32_tree=null;
        Object DISTINCT33_tree=null;
        Object ON34_tree=null;
        Object LPAREN35_tree=null;
        Object RPAREN37_tree=null;
        Object ALL38_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:111:14: ( DISTINCT | DISTINCT ON LPAREN expr_list RPAREN | ALL | )
            int alt7=4;
            switch ( input.LA(1) ) {
            case DISTINCT:
                {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==ON) ) {
                    alt7=2;
                }
                else if ( (LA7_1==STAR||LA7_1==LPAREN||(LA7_1>=OLD && LA7_1<=NEW)||LA7_1==NOT||(LA7_1>=EXISTS && LA7_1<=QUOTEDSTRING)||LA7_1==NUMBER||LA7_1==ID) ) {
                    alt7=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("111:1: opt_distinct : ( DISTINCT | DISTINCT ON LPAREN expr_list RPAREN | ALL | );", 7, 1, input);

                    throw nvae;
                }
                }
                break;
            case ALL:
                {
                alt7=3;
                }
                break;
            case STAR:
            case LPAREN:
            case OLD:
            case NEW:
            case NOT:
            case EXISTS:
            case ARRAY:
            case QUOTEDSTRING:
            case NUMBER:
            case ID:
                {
                alt7=4;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("111:1: opt_distinct : ( DISTINCT | DISTINCT ON LPAREN expr_list RPAREN | ALL | );", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:111:16: DISTINCT
                    {
                    root_0 = (Object)adaptor.nil();

                    DISTINCT32=(Token)input.LT(1);
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_opt_distinct447); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DISTINCT32_tree = (Object)adaptor.create(DISTINCT32);
                    adaptor.addChild(root_0, DISTINCT32_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:112:5: DISTINCT ON LPAREN expr_list RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    DISTINCT33=(Token)input.LT(1);
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_opt_distinct454); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DISTINCT33_tree = (Object)adaptor.create(DISTINCT33);
                    adaptor.addChild(root_0, DISTINCT33_tree);
                    }
                    ON34=(Token)input.LT(1);
                    match(input,ON,FOLLOW_ON_in_opt_distinct456); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ON34_tree = (Object)adaptor.create(ON34);
                    adaptor.addChild(root_0, ON34_tree);
                    }
                    LPAREN35=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_opt_distinct458); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LPAREN35_tree = (Object)adaptor.create(LPAREN35);
                    adaptor.addChild(root_0, LPAREN35_tree);
                    }
                    pushFollow(FOLLOW_expr_list_in_opt_distinct460);
                    expr_list36=expr_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, expr_list36.getTree());
                    RPAREN37=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_opt_distinct462); if (failed) return retval;
                    if ( backtracking==0 ) {
                    RPAREN37_tree = (Object)adaptor.create(RPAREN37);
                    adaptor.addChild(root_0, RPAREN37_tree);
                    }

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:113:5: ALL
                    {
                    root_0 = (Object)adaptor.nil();

                    ALL38=(Token)input.LT(1);
                    match(input,ALL,FOLLOW_ALL_in_opt_distinct469); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ALL38_tree = (Object)adaptor.create(ALL38);
                    adaptor.addChild(root_0, ALL38_tree);
                    }

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:115:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end opt_distinct

    public static class expr_list_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start expr_list
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:1: expr_list : ( a_expr ) ( ',' a_expr )* -> ( a_expr )+ ;
    public final expr_list_return expr_list() throws RecognitionException {
        expr_list_return retval = new expr_list_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal40=null;
        a_expr_return a_expr39 = null;

        a_expr_return a_expr41 = null;


        Object char_literal40_tree=null;
        RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
        RewriteRuleSubtreeStream stream_a_expr=new RewriteRuleSubtreeStream(adaptor,"rule a_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:17: ( ( a_expr ) ( ',' a_expr )* -> ( a_expr )+ )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:19: ( a_expr ) ( ',' a_expr )*
            {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:19: ( a_expr )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:20: a_expr
            {
            pushFollow(FOLLOW_a_expr_in_expr_list493);
            a_expr39=a_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_a_expr.add(a_expr39.getTree());

            }

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:28: ( ',' a_expr )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==89) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:117:29: ',' a_expr
            	    {
            	    char_literal40=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_expr_list497); if (failed) return retval;
            	    if ( backtracking==0 ) stream_89.add(char_literal40);

            	    pushFollow(FOLLOW_a_expr_in_expr_list499);
            	    a_expr41=a_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_a_expr.add(a_expr41.getTree());

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            // AST REWRITE
            // elements: a_expr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 117:42: -> ( a_expr )+
            {
                if ( !(stream_a_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_a_expr.hasNext() ) {
                    adaptor.addChild(root_0, stream_a_expr.next());

                }
                stream_a_expr.reset();

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end expr_list

    public static class target_list_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start target_list
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:1: target_list : target ( ',' target )* -> ^( TARGETS ( target )+ ) ;
    public final target_list_return target_list() throws RecognitionException {
        target_list_return retval = new target_list_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal43=null;
        target_return target42 = null;

        target_return target44 = null;


        Object char_literal43_tree=null;
        RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
        RewriteRuleSubtreeStream stream_target=new RewriteRuleSubtreeStream(adaptor,"rule target");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:17: ( target ( ',' target )* -> ^( TARGETS ( target )+ ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:19: target ( ',' target )*
            {
            pushFollow(FOLLOW_target_in_target_list524);
            target42=target();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_target.add(target42.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:26: ( ',' target )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==89) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:27: ',' target
            	    {
            	    char_literal43=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_target_list527); if (failed) return retval;
            	    if ( backtracking==0 ) stream_89.add(char_literal43);

            	    pushFollow(FOLLOW_target_in_target_list529);
            	    target44=target();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_target.add(target44.getTree());

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            // AST REWRITE
            // elements: target
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 126:40: -> ^( TARGETS ( target )+ )
            {
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:126:43: ^( TARGETS ( target )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(TARGETS, "TARGETS"), root_1);

                if ( !(stream_target.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_target.hasNext() ) {
                    adaptor.addChild(root_1, stream_target.next());

                }
                stream_target.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end target_list

    public static class target_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start target
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:1: target : ( a_expr ( target_alias )? -> ^( TARGET a_expr ( target_alias )? ) | STAR -> ^( TARGET STAR ) );
    public final target_return target() throws RecognitionException {
        target_return retval = new target_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR47=null;
        a_expr_return a_expr45 = null;

        target_alias_return target_alias46 = null;


        Object STAR47_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleSubtreeStream stream_target_alias=new RewriteRuleSubtreeStream(adaptor,"rule target_alias");
        RewriteRuleSubtreeStream stream_a_expr=new RewriteRuleSubtreeStream(adaptor,"rule a_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:8: ( a_expr ( target_alias )? -> ^( TARGET a_expr ( target_alias )? ) | STAR -> ^( TARGET STAR ) )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==LPAREN||(LA11_0>=OLD && LA11_0<=NEW)||LA11_0==NOT||(LA11_0>=EXISTS && LA11_0<=QUOTEDSTRING)||LA11_0==NUMBER||LA11_0==ID) ) {
                alt11=1;
            }
            else if ( (LA11_0==STAR) ) {
                alt11=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("129:1: target : ( a_expr ( target_alias )? -> ^( TARGET a_expr ( target_alias )? ) | STAR -> ^( TARGET STAR ) );", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:10: a_expr ( target_alias )?
                    {
                    pushFollow(FOLLOW_a_expr_in_target552);
                    a_expr45=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_a_expr.add(a_expr45.getTree());
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:17: ( target_alias )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==AS||LA10_0==ID) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:17: target_alias
                            {
                            pushFollow(FOLLOW_target_alias_in_target554);
                            target_alias46=target_alias();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) stream_target_alias.add(target_alias46.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: a_expr, target_alias
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 129:31: -> ^( TARGET a_expr ( target_alias )? )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:34: ^( TARGET a_expr ( target_alias )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(TARGET, "TARGET"), root_1);

                        adaptor.addChild(root_1, stream_a_expr.next());
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:129:50: ( target_alias )?
                        if ( stream_target_alias.hasNext() ) {
                            adaptor.addChild(root_1, stream_target_alias.next());

                        }
                        stream_target_alias.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:130:4: STAR
                    {
                    STAR47=(Token)input.LT(1);
                    match(input,STAR,FOLLOW_STAR_in_target571); if (failed) return retval;
                    if ( backtracking==0 ) stream_STAR.add(STAR47);


                    // AST REWRITE
                    // elements: STAR
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 130:9: -> ^( TARGET STAR )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:130:12: ^( TARGET STAR )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(TARGET, "TARGET"), root_1);

                        adaptor.addChild(root_1, stream_STAR.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end target

    public static class target_alias_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start target_alias
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:1: target_alias : ( ( AS id )=> AS id -> ^( ALIAS id ) | ( id )=> id -> ^( ALIAS id ) );
    public final target_alias_return target_alias() throws RecognitionException {
        target_alias_return retval = new target_alias_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS48=null;
        id_return id49 = null;

        id_return id50 = null;


        Object AS48_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:14: ( ( AS id )=> AS id -> ^( ALIAS id ) | ( id )=> id -> ^( ALIAS id ) )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==AS) && (synpred1())) {
                alt12=1;
            }
            else if ( (LA12_0==ID) && (synpred2())) {
                alt12=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("133:1: target_alias : ( ( AS id )=> AS id -> ^( ALIAS id ) | ( id )=> id -> ^( ALIAS id ) );", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:16: ( AS id )=> AS id
                    {
                    AS48=(Token)input.LT(1);
                    match(input,AS,FOLLOW_AS_in_target_alias598); if (failed) return retval;
                    if ( backtracking==0 ) stream_AS.add(AS48);

                    pushFollow(FOLLOW_id_in_target_alias600);
                    id49=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_id.add(id49.getTree());

                    // AST REWRITE
                    // elements: id
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 133:33: -> ^( ALIAS id )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:36: ^( ALIAS id )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(ALIAS, "ALIAS"), root_1);

                        adaptor.addChild(root_1, stream_id.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:134:5: ( id )=> id
                    {
                    pushFollow(FOLLOW_id_in_target_alias620);
                    id50=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_id.add(id50.getTree());

                    // AST REWRITE
                    // elements: id
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 134:16: -> ^( ALIAS id )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:134:19: ^( ALIAS id )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(ALIAS, "ALIAS"), root_1);

                        adaptor.addChild(root_1, stream_id.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end target_alias

    public static class into_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start into_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:143:1: into_clause : ( INTO opt_temp_table_name | );
    public final into_clause_return into_clause() throws RecognitionException {
        into_clause_return retval = new into_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token INTO51=null;
        opt_temp_table_name_return opt_temp_table_name52 = null;


        Object INTO51_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:143:13: ( INTO opt_temp_table_name | )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==INTO) ) {
                alt13=1;
            }
            else if ( (LA13_0==EOF||LA13_0==RPAREN||LA13_0==FROM||(LA13_0>=WHERE && LA13_0<=GROUP)||(LA13_0>=HAVING && LA13_0<=ORDER)||LA13_0==88) ) {
                alt13=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("143:1: into_clause : ( INTO opt_temp_table_name | );", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:143:15: INTO opt_temp_table_name
                    {
                    root_0 = (Object)adaptor.nil();

                    INTO51=(Token)input.LT(1);
                    match(input,INTO,FOLLOW_INTO_in_into_clause642); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTO51_tree = (Object)adaptor.create(INTO51);
                    adaptor.addChild(root_0, INTO51_tree);
                    }
                    pushFollow(FOLLOW_opt_temp_table_name_in_into_clause644);
                    opt_temp_table_name52=opt_temp_table_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_temp_table_name52.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:145:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end into_clause

    public static class opt_temp_table_name_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start opt_temp_table_name
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:147:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );
    public final opt_temp_table_name_return opt_temp_table_name() throws RecognitionException {
        opt_temp_table_name_return retval = new opt_temp_table_name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TEMPORARY53=null;
        Token TEMP56=null;
        Token LOCAL59=null;
        Token TEMPORARY60=null;
        Token LOCAL63=null;
        Token TEMP64=null;
        Token GLOBAL67=null;
        Token TEMPORARY68=null;
        Token GLOBAL71=null;
        Token TEMP72=null;
        Token TABLE75=null;
        opt_table_return opt_table54 = null;

        qualified_name_return qualified_name55 = null;

        opt_table_return opt_table57 = null;

        qualified_name_return qualified_name58 = null;

        opt_table_return opt_table61 = null;

        qualified_name_return qualified_name62 = null;

        opt_table_return opt_table65 = null;

        qualified_name_return qualified_name66 = null;

        opt_table_return opt_table69 = null;

        qualified_name_return qualified_name70 = null;

        opt_table_return opt_table73 = null;

        qualified_name_return qualified_name74 = null;

        qualified_name_return qualified_name76 = null;

        qualified_name_return qualified_name77 = null;


        Object TEMPORARY53_tree=null;
        Object TEMP56_tree=null;
        Object LOCAL59_tree=null;
        Object TEMPORARY60_tree=null;
        Object LOCAL63_tree=null;
        Object TEMP64_tree=null;
        Object GLOBAL67_tree=null;
        Object TEMPORARY68_tree=null;
        Object GLOBAL71_tree=null;
        Object TEMP72_tree=null;
        Object TABLE75_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:147:21: ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name )
            int alt14=8;
            switch ( input.LA(1) ) {
            case TEMPORARY:
                {
                alt14=1;
                }
                break;
            case TEMP:
                {
                alt14=2;
                }
                break;
            case LOCAL:
                {
                int LA14_3 = input.LA(2);

                if ( (LA14_3==TEMPORARY) ) {
                    alt14=3;
                }
                else if ( (LA14_3==TEMP) ) {
                    alt14=4;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("147:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 14, 3, input);

                    throw nvae;
                }
                }
                break;
            case GLOBAL:
                {
                int LA14_4 = input.LA(2);

                if ( (LA14_4==TEMPORARY) ) {
                    alt14=5;
                }
                else if ( (LA14_4==TEMP) ) {
                    alt14=6;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("147:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 14, 4, input);

                    throw nvae;
                }
                }
                break;
            case TABLE:
                {
                alt14=7;
                }
                break;
            case OLD:
            case NEW:
            case ID:
                {
                alt14=8;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("147:1: opt_temp_table_name : ( TEMPORARY opt_table qualified_name | TEMP opt_table qualified_name | LOCAL TEMPORARY opt_table qualified_name | LOCAL TEMP opt_table qualified_name | GLOBAL TEMPORARY opt_table qualified_name | GLOBAL TEMP opt_table qualified_name | TABLE qualified_name | qualified_name );", 14, 0, input);

                throw nvae;
            }

            switch (alt14) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:147:23: TEMPORARY opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    TEMPORARY53=(Token)input.LT(1);
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name664); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMPORARY53_tree = (Object)adaptor.create(TEMPORARY53);
                    adaptor.addChild(root_0, TEMPORARY53_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name666);
                    opt_table54=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table54.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name668);
                    qualified_name55=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name55.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:148:6: TEMP opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    TEMP56=(Token)input.LT(1);
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name676); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMP56_tree = (Object)adaptor.create(TEMP56);
                    adaptor.addChild(root_0, TEMP56_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name678);
                    opt_table57=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table57.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name680);
                    qualified_name58=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name58.getTree());

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:149:6: LOCAL TEMPORARY opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    LOCAL59=(Token)input.LT(1);
                    match(input,LOCAL,FOLLOW_LOCAL_in_opt_temp_table_name688); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LOCAL59_tree = (Object)adaptor.create(LOCAL59);
                    adaptor.addChild(root_0, LOCAL59_tree);
                    }
                    TEMPORARY60=(Token)input.LT(1);
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name690); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMPORARY60_tree = (Object)adaptor.create(TEMPORARY60);
                    adaptor.addChild(root_0, TEMPORARY60_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name692);
                    opt_table61=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table61.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name694);
                    qualified_name62=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name62.getTree());

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:150:6: LOCAL TEMP opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    LOCAL63=(Token)input.LT(1);
                    match(input,LOCAL,FOLLOW_LOCAL_in_opt_temp_table_name702); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LOCAL63_tree = (Object)adaptor.create(LOCAL63);
                    adaptor.addChild(root_0, LOCAL63_tree);
                    }
                    TEMP64=(Token)input.LT(1);
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name704); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMP64_tree = (Object)adaptor.create(TEMP64);
                    adaptor.addChild(root_0, TEMP64_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name706);
                    opt_table65=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table65.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name708);
                    qualified_name66=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name66.getTree());

                    }
                    break;
                case 5 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:151:6: GLOBAL TEMPORARY opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    GLOBAL67=(Token)input.LT(1);
                    match(input,GLOBAL,FOLLOW_GLOBAL_in_opt_temp_table_name715); if (failed) return retval;
                    if ( backtracking==0 ) {
                    GLOBAL67_tree = (Object)adaptor.create(GLOBAL67);
                    adaptor.addChild(root_0, GLOBAL67_tree);
                    }
                    TEMPORARY68=(Token)input.LT(1);
                    match(input,TEMPORARY,FOLLOW_TEMPORARY_in_opt_temp_table_name717); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMPORARY68_tree = (Object)adaptor.create(TEMPORARY68);
                    adaptor.addChild(root_0, TEMPORARY68_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name719);
                    opt_table69=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table69.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name721);
                    qualified_name70=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name70.getTree());

                    }
                    break;
                case 6 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:152:6: GLOBAL TEMP opt_table qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    GLOBAL71=(Token)input.LT(1);
                    match(input,GLOBAL,FOLLOW_GLOBAL_in_opt_temp_table_name729); if (failed) return retval;
                    if ( backtracking==0 ) {
                    GLOBAL71_tree = (Object)adaptor.create(GLOBAL71);
                    adaptor.addChild(root_0, GLOBAL71_tree);
                    }
                    TEMP72=(Token)input.LT(1);
                    match(input,TEMP,FOLLOW_TEMP_in_opt_temp_table_name731); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TEMP72_tree = (Object)adaptor.create(TEMP72);
                    adaptor.addChild(root_0, TEMP72_tree);
                    }
                    pushFollow(FOLLOW_opt_table_in_opt_temp_table_name733);
                    opt_table73=opt_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, opt_table73.getTree());
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name735);
                    qualified_name74=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name74.getTree());

                    }
                    break;
                case 7 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:153:6: TABLE qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    TABLE75=(Token)input.LT(1);
                    match(input,TABLE,FOLLOW_TABLE_in_opt_temp_table_name743); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TABLE75_tree = (Object)adaptor.create(TABLE75);
                    adaptor.addChild(root_0, TABLE75_tree);
                    }
                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name745);
                    qualified_name76=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name76.getTree());

                    }
                    break;
                case 8 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:154:6: qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_qualified_name_in_opt_temp_table_name753);
                    qualified_name77=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name77.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end opt_temp_table_name

    public static class opt_table_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start opt_table
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:157:1: opt_table : ( TABLE | );
    public final opt_table_return opt_table() throws RecognitionException {
        opt_table_return retval = new opt_table_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token TABLE78=null;

        Object TABLE78_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:157:11: ( TABLE | )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==TABLE) ) {
                alt15=1;
            }
            else if ( ((LA15_0>=OLD && LA15_0<=NEW)||LA15_0==ID) ) {
                alt15=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("157:1: opt_table : ( TABLE | );", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:157:13: TABLE
                    {
                    root_0 = (Object)adaptor.nil();

                    TABLE78=(Token)input.LT(1);
                    match(input,TABLE,FOLLOW_TABLE_in_opt_table769); if (failed) return retval;
                    if ( backtracking==0 ) {
                    TABLE78_tree = (Object)adaptor.create(TABLE78);
                    adaptor.addChild(root_0, TABLE78_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:159:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end opt_table

    public static class qualified_name_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start qualified_name
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:1: qualified_name : relation_name ( indirection )* -> ^( RELATION_NAME relation_name ( indirection )* ) ;
    public final qualified_name_return qualified_name() throws RecognitionException {
        qualified_name_return retval = new qualified_name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        relation_name_return relation_name79 = null;

        indirection_return indirection80 = null;


        RewriteRuleSubtreeStream stream_relation_name=new RewriteRuleSubtreeStream(adaptor,"rule relation_name");
        RewriteRuleSubtreeStream stream_indirection=new RewriteRuleSubtreeStream(adaptor,"rule indirection");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:16: ( relation_name ( indirection )* -> ^( RELATION_NAME relation_name ( indirection )* ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:18: relation_name ( indirection )*
            {
            pushFollow(FOLLOW_relation_name_in_qualified_name786);
            relation_name79=relation_name();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_relation_name.add(relation_name79.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:32: ( indirection )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==DOT) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:32: indirection
            	    {
            	    pushFollow(FOLLOW_indirection_in_qualified_name788);
            	    indirection80=indirection();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_indirection.add(indirection80.getTree());

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);


            // AST REWRITE
            // elements: indirection, relation_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 161:45: -> ^( RELATION_NAME relation_name ( indirection )* )
            {
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:48: ^( RELATION_NAME relation_name ( indirection )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(RELATION_NAME, "RELATION_NAME"), root_1);

                adaptor.addChild(root_1, stream_relation_name.next());
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:161:78: ( indirection )*
                while ( stream_indirection.hasNext() ) {
                    adaptor.addChild(root_1, stream_indirection.next());

                }
                stream_indirection.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end qualified_name

    public static class relation_name_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start relation_name
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:164:1: relation_name : ( id | special_rule_relation );
    public final relation_name_return relation_name() throws RecognitionException {
        relation_name_return retval = new relation_name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        id_return id81 = null;

        special_rule_relation_return special_rule_relation82 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:164:15: ( id | special_rule_relation )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ID) ) {
                alt17=1;
            }
            else if ( ((LA17_0>=OLD && LA17_0<=NEW)) ) {
                alt17=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("164:1: relation_name : ( id | special_rule_relation );", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:164:17: id
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_id_in_relation_name811);
                    id81=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, id81.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:165:5: special_rule_relation
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_special_rule_relation_in_relation_name819);
                    special_rule_relation82=special_rule_relation();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, special_rule_relation82.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end relation_name

    public static class special_rule_relation_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start special_rule_relation
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:168:1: special_rule_relation : ( OLD | NEW );
    public final special_rule_relation_return special_rule_relation() throws RecognitionException {
        special_rule_relation_return retval = new special_rule_relation_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set83=null;

        Object set83_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:168:23: ( OLD | NEW )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:
            {
            root_0 = (Object)adaptor.nil();

            set83=(Token)input.LT(1);
            if ( (input.LA(1)>=OLD && input.LA(1)<=NEW) ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set83));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_special_rule_relation0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end special_rule_relation

    public static class indirection_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start indirection
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:173:1: indirection : ( DOT id -> ^( INDIRECTION id ) | DOT STAR -> ^( INDIRECTION STAR ) );
    public final indirection_return indirection() throws RecognitionException {
        indirection_return retval = new indirection_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT84=null;
        Token DOT86=null;
        Token STAR87=null;
        id_return id85 = null;


        Object DOT84_tree=null;
        Object DOT86_tree=null;
        Object STAR87_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:173:13: ( DOT id -> ^( INDIRECTION id ) | DOT STAR -> ^( INDIRECTION STAR ) )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==DOT) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==STAR) ) {
                    alt18=2;
                }
                else if ( (LA18_1==ID) ) {
                    alt18=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("173:1: indirection : ( DOT id -> ^( INDIRECTION id ) | DOT STAR -> ^( INDIRECTION STAR ) );", 18, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("173:1: indirection : ( DOT id -> ^( INDIRECTION id ) | DOT STAR -> ^( INDIRECTION STAR ) );", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:173:15: DOT id
                    {
                    DOT84=(Token)input.LT(1);
                    match(input,DOT,FOLLOW_DOT_in_indirection854); if (failed) return retval;
                    if ( backtracking==0 ) stream_DOT.add(DOT84);

                    pushFollow(FOLLOW_id_in_indirection856);
                    id85=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_id.add(id85.getTree());

                    // AST REWRITE
                    // elements: id
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 173:22: -> ^( INDIRECTION id )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:173:24: ^( INDIRECTION id )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(INDIRECTION, "INDIRECTION"), root_1);

                        adaptor.addChild(root_1, stream_id.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:174:5: DOT STAR
                    {
                    DOT86=(Token)input.LT(1);
                    match(input,DOT,FOLLOW_DOT_in_indirection869); if (failed) return retval;
                    if ( backtracking==0 ) stream_DOT.add(DOT86);

                    STAR87=(Token)input.LT(1);
                    match(input,STAR,FOLLOW_STAR_in_indirection871); if (failed) return retval;
                    if ( backtracking==0 ) stream_STAR.add(STAR87);


                    // AST REWRITE
                    // elements: STAR
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 174:14: -> ^( INDIRECTION STAR )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:174:17: ^( INDIRECTION STAR )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(INDIRECTION, "INDIRECTION"), root_1);

                        adaptor.addChild(root_1, stream_STAR.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end indirection

    public static class from_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start from_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:185:1: from_clause : ( FROM from_list -> ^( FROM[\"FROM\"] from_list ) | );
    public final from_clause_return from_clause() throws RecognitionException {
        from_clause_return retval = new from_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FROM88=null;
        from_list_return from_list89 = null;


        Object FROM88_tree=null;
        RewriteRuleTokenStream stream_FROM=new RewriteRuleTokenStream(adaptor,"token FROM");
        RewriteRuleSubtreeStream stream_from_list=new RewriteRuleSubtreeStream(adaptor,"rule from_list");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:185:13: ( FROM from_list -> ^( FROM[\"FROM\"] from_list ) | )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==FROM) ) {
                alt19=1;
            }
            else if ( (LA19_0==EOF||LA19_0==RPAREN||(LA19_0>=WHERE && LA19_0<=GROUP)||(LA19_0>=HAVING && LA19_0<=ORDER)||LA19_0==88) ) {
                alt19=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("185:1: from_clause : ( FROM from_list -> ^( FROM[\"FROM\"] from_list ) | );", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:185:15: FROM from_list
                    {
                    FROM88=(Token)input.LT(1);
                    match(input,FROM,FOLLOW_FROM_in_from_clause895); if (failed) return retval;
                    if ( backtracking==0 ) stream_FROM.add(FROM88);

                    pushFollow(FOLLOW_from_list_in_from_clause897);
                    from_list89=from_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_from_list.add(from_list89.getTree());

                    // AST REWRITE
                    // elements: FROM, from_list
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 185:30: -> ^( FROM[\"FROM\"] from_list )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:185:33: ^( FROM[\"FROM\"] from_list )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(FROM,"FROM"), root_1);

                        adaptor.addChild(root_1, stream_from_list.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:187:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end from_clause

    public static class from_list_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start from_list
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:189:1: from_list : ( table_joins | table_refs );
    public final from_list_return from_list() throws RecognitionException {
        from_list_return retval = new from_list_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        table_joins_return table_joins90 = null;

        table_refs_return table_refs91 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:189:12: ( table_joins | table_refs )
            int alt20=2;
            alt20 = dfa20.predict(input);
            switch (alt20) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:189:14: table_joins
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_table_joins_in_from_list923);
                    table_joins90=table_joins();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_joins90.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:190:5: table_refs
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_table_refs_in_from_list929);
                    table_refs91=table_refs();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_refs91.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end from_list

    public static class table_refs_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start table_refs
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:193:1: table_refs : table_ref ( ',' table_ref )* ;
    public final table_refs_return table_refs() throws RecognitionException {
        table_refs_return retval = new table_refs_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal93=null;
        table_ref_return table_ref92 = null;

        table_ref_return table_ref94 = null;


        Object char_literal93_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:193:13: ( table_ref ( ',' table_ref )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:193:15: table_ref ( ',' table_ref )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_table_ref_in_table_refs941);
            table_ref92=table_ref();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, table_ref92.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:193:25: ( ',' table_ref )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==89) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:193:26: ',' table_ref
            	    {
            	    char_literal93=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_table_refs944); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal93_tree = (Object)adaptor.create(char_literal93);
            	    adaptor.addChild(root_0, char_literal93_tree);
            	    }
            	    pushFollow(FOLLOW_table_ref_in_table_refs946);
            	    table_ref94=table_ref();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref94.getTree());

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end table_refs

    public static class table_joins_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start table_joins
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:1: table_joins : relation_expr ( alias_clause )? ( table_join )+ ;
    public final table_joins_return table_joins() throws RecognitionException {
        table_joins_return retval = new table_joins_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        relation_expr_return relation_expr95 = null;

        alias_clause_return alias_clause96 = null;

        table_join_return table_join97 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:14: ( relation_expr ( alias_clause )? ( table_join )+ )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:16: relation_expr ( alias_clause )? ( table_join )+
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_relation_expr_in_table_joins961);
            relation_expr95=relation_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, relation_expr95.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:30: ( alias_clause )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==AS||LA22_0==ID) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:30: alias_clause
                    {
                    pushFollow(FOLLOW_alias_clause_in_table_joins963);
                    alias_clause96=alias_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, alias_clause96.getTree());

                    }
                    break;

            }

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:44: ( table_join )+
            int cnt23=0;
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==LPAREN||(LA23_0>=CROSS && LA23_0<=INNER)) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:196:44: table_join
            	    {
            	    pushFollow(FOLLOW_table_join_in_table_joins966);
            	    table_join97=table_join();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, table_join97.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt23 >= 1 ) break loop23;
            	    if (backtracking>0) {failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(23, input);
                        throw eee;
                }
                cnt23++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end table_joins

    public static class table_ref_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start table_ref
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:1: table_ref : ( ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )? -> ^( FUNCTION func_expr ( alias_clause )? ) | relation_expr ( alias_clause )? -> ^( RELATION relation_expr ( alias_clause )? ) | ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )? );
    public final table_ref_return table_ref() throws RecognitionException {
        table_ref_return retval = new table_ref_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN102=null;
        Token RPAREN104=null;
        func_expr_return func_expr98 = null;

        alias_clause_return alias_clause99 = null;

        relation_expr_return relation_expr100 = null;

        alias_clause_return alias_clause101 = null;

        inner_select_return inner_select103 = null;

        alias_clause_return alias_clause105 = null;


        Object LPAREN102_tree=null;
        Object RPAREN104_tree=null;
        RewriteRuleSubtreeStream stream_relation_expr=new RewriteRuleSubtreeStream(adaptor,"rule relation_expr");
        RewriteRuleSubtreeStream stream_func_expr=new RewriteRuleSubtreeStream(adaptor,"rule func_expr");
        RewriteRuleSubtreeStream stream_alias_clause=new RewriteRuleSubtreeStream(adaptor,"rule alias_clause");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:12: ( ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )? -> ^( FUNCTION func_expr ( alias_clause )? ) | relation_expr ( alias_clause )? -> ^( RELATION relation_expr ( alias_clause )? ) | ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )? )
            int alt27=3;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==ID) ) {
                int LA27_1 = input.LA(2);

                if ( (LA27_1==LPAREN) ) {
                    int LA27_4 = input.LA(3);

                    if ( (LA27_4==DISTINCT||LA27_4==ALL) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==NOT) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==ID) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( ((LA27_4>=OLD && LA27_4<=NEW)) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==LPAREN) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==EXISTS) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==ARRAY) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==NUMBER) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==QUOTEDSTRING) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( (LA27_4==STAR) && (synpred3())) {
                        alt27=1;
                    }
                    else if ( ((LA27_4>=CROSS && LA27_4<=INNER)) ) {
                        alt27=2;
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("199:1: table_ref : ( ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )? -> ^( FUNCTION func_expr ( alias_clause )? ) | relation_expr ( alias_clause )? -> ^( RELATION relation_expr ( alias_clause )? ) | ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )? );", 27, 4, input);

                        throw nvae;
                    }
                }
                else if ( (LA27_1==EOF||LA27_1==STAR||LA27_1==DOT||LA27_1==RPAREN||LA27_1==ON||LA27_1==AS||(LA27_1>=CROSS && LA27_1<=INNER)||LA27_1==USING||(LA27_1>=WHERE && LA27_1<=GROUP)||(LA27_1>=HAVING && LA27_1<=ORDER)||LA27_1==ID||(LA27_1>=88 && LA27_1<=89)) ) {
                    alt27=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("199:1: table_ref : ( ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )? -> ^( FUNCTION func_expr ( alias_clause )? ) | relation_expr ( alias_clause )? -> ^( RELATION relation_expr ( alias_clause )? ) | ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )? );", 27, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA27_0>=OLD && LA27_0<=NEW)||LA27_0==ONLY) ) {
                alt27=2;
            }
            else if ( (LA27_0==LPAREN) && (synpred4())) {
                alt27=3;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("199:1: table_ref : ( ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )? -> ^( FUNCTION func_expr ( alias_clause )? ) | relation_expr ( alias_clause )? -> ^( RELATION relation_expr ( alias_clause )? ) | ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )? );", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:14: ( func_expr ( alias_clause )? )=> func_expr ( alias_clause )?
                    {
                    pushFollow(FOLLOW_func_expr_in_table_ref990);
                    func_expr98=func_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_func_expr.add(func_expr98.getTree());
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:53: ( alias_clause )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==AS||LA24_0==ID) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:53: alias_clause
                            {
                            pushFollow(FOLLOW_alias_clause_in_table_ref992);
                            alias_clause99=alias_clause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) stream_alias_clause.add(alias_clause99.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: alias_clause, func_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 199:67: -> ^( FUNCTION func_expr ( alias_clause )? )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:70: ^( FUNCTION func_expr ( alias_clause )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(FUNCTION, "FUNCTION"), root_1);

                        adaptor.addChild(root_1, stream_func_expr.next());
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:91: ( alias_clause )?
                        if ( stream_alias_clause.hasNext() ) {
                            adaptor.addChild(root_1, stream_alias_clause.next());

                        }
                        stream_alias_clause.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:200:5: relation_expr ( alias_clause )?
                    {
                    pushFollow(FOLLOW_relation_expr_in_table_ref1010);
                    relation_expr100=relation_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_relation_expr.add(relation_expr100.getTree());
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:200:19: ( alias_clause )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==AS||LA25_0==ID) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:200:19: alias_clause
                            {
                            pushFollow(FOLLOW_alias_clause_in_table_ref1012);
                            alias_clause101=alias_clause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) stream_alias_clause.add(alias_clause101.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: alias_clause, relation_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 200:33: -> ^( RELATION relation_expr ( alias_clause )? )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:200:36: ^( RELATION relation_expr ( alias_clause )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(RELATION, "RELATION"), root_1);

                        adaptor.addChild(root_1, stream_relation_expr.next());
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:200:61: ( alias_clause )?
                        if ( stream_alias_clause.hasNext() ) {
                            adaptor.addChild(root_1, stream_alias_clause.next());

                        }
                        stream_alias_clause.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:5: ( LPAREN inner_select RPAREN ( alias_clause )? )=> LPAREN inner_select RPAREN ( alias_clause )?
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN102=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_table_ref1044); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LPAREN102_tree = (Object)adaptor.create(LPAREN102);
                    adaptor.addChild(root_0, LPAREN102_tree);
                    }
                    pushFollow(FOLLOW_inner_select_in_table_ref1046);
                    inner_select103=inner_select();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, inner_select103.getTree());
                    RPAREN104=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_table_ref1048); if (failed) return retval;
                    if ( backtracking==0 ) {
                    RPAREN104_tree = (Object)adaptor.create(RPAREN104);
                    adaptor.addChild(root_0, RPAREN104_tree);
                    }
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:79: ( alias_clause )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==AS||LA26_0==ID) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:79: alias_clause
                            {
                            pushFollow(FOLLOW_alias_clause_in_table_ref1050);
                            alias_clause105=alias_clause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, alias_clause105.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end table_ref

    public static class inner_select_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start inner_select
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:204:1: inner_select : simple_select ;
    public final inner_select_return inner_select() throws RecognitionException {
        inner_select_return retval = new inner_select_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        simple_select_return simple_select106 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:204:15: ( simple_select )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:204:17: simple_select
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_simple_select_in_inner_select1066);
            simple_select106=simple_select();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simple_select106.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end inner_select

    public static class table_join_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start table_join
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:207:1: table_join : ( joined_table -> ^( JOIN[\"JOIN\"] joined_table ) | '(' joined_table ')' alias_clause );
    public final table_join_return table_join() throws RecognitionException {
        table_join_return retval = new table_join_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal108=null;
        Token char_literal110=null;
        joined_table_return joined_table107 = null;

        joined_table_return joined_table109 = null;

        alias_clause_return alias_clause111 = null;


        Object char_literal108_tree=null;
        Object char_literal110_tree=null;
        RewriteRuleSubtreeStream stream_joined_table=new RewriteRuleSubtreeStream(adaptor,"rule joined_table");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:207:13: ( joined_table -> ^( JOIN[\"JOIN\"] joined_table ) | '(' joined_table ')' alias_clause )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0>=CROSS && LA28_0<=INNER)) ) {
                alt28=1;
            }
            else if ( (LA28_0==LPAREN) ) {
                alt28=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("207:1: table_join : ( joined_table -> ^( JOIN[\"JOIN\"] joined_table ) | '(' joined_table ')' alias_clause );", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:207:15: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_join1079);
                    joined_table107=joined_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_joined_table.add(joined_table107.getTree());

                    // AST REWRITE
                    // elements: joined_table
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 207:28: -> ^( JOIN[\"JOIN\"] joined_table )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:207:31: ^( JOIN[\"JOIN\"] joined_table )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(JOIN, "JOIN"), root_1);

                        adaptor.addChild(root_1, stream_joined_table.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:208:5: '(' joined_table ')' alias_clause
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal108=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_table_join1094); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal108_tree = (Object)adaptor.create(char_literal108);
                    adaptor.addChild(root_0, char_literal108_tree);
                    }
                    pushFollow(FOLLOW_joined_table_in_table_join1096);
                    joined_table109=joined_table();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, joined_table109.getTree());
                    char_literal110=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_table_join1098); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal110_tree = (Object)adaptor.create(char_literal110);
                    adaptor.addChild(root_0, char_literal110_tree);
                    }
                    pushFollow(FOLLOW_alias_clause_in_table_join1100);
                    alias_clause111=alias_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, alias_clause111.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end table_join

    public static class joined_table_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start joined_table
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:211:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );
    public final joined_table_return joined_table() throws RecognitionException {
        joined_table_return retval = new joined_table_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token CROSS112=null;
        Token JOIN113=null;
        Token JOIN116=null;
        Token JOIN119=null;
        Token NATURAL122=null;
        Token JOIN124=null;
        Token NATURAL126=null;
        Token JOIN127=null;
        table_ref_return table_ref114 = null;

        join_type_return join_type115 = null;

        table_ref_return table_ref117 = null;

        join_qual_return join_qual118 = null;

        table_ref_return table_ref120 = null;

        join_qual_return join_qual121 = null;

        join_type_return join_type123 = null;

        table_ref_return table_ref125 = null;

        table_ref_return table_ref128 = null;


        Object CROSS112_tree=null;
        Object JOIN113_tree=null;
        Object JOIN116_tree=null;
        Object JOIN119_tree=null;
        Object NATURAL122_tree=null;
        Object JOIN124_tree=null;
        Object NATURAL126_tree=null;
        Object JOIN127_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:211:15: ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref )
            int alt29=5;
            switch ( input.LA(1) ) {
            case CROSS:
                {
                alt29=1;
                }
                break;
            case FULL:
            case LEFT:
            case RIGHT:
            case INNER:
                {
                alt29=2;
                }
                break;
            case JOIN:
                {
                alt29=3;
                }
                break;
            case NATURAL:
                {
                int LA29_4 = input.LA(2);

                if ( (LA29_4==JOIN) ) {
                    alt29=5;
                }
                else if ( ((LA29_4>=FULL && LA29_4<=INNER)) ) {
                    alt29=4;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("211:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );", 29, 4, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("211:1: joined_table : ( CROSS JOIN table_ref | join_type JOIN table_ref join_qual | JOIN table_ref join_qual | NATURAL join_type JOIN table_ref | NATURAL JOIN table_ref );", 29, 0, input);

                throw nvae;
            }

            switch (alt29) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:211:17: CROSS JOIN table_ref
                    {
                    root_0 = (Object)adaptor.nil();

                    CROSS112=(Token)input.LT(1);
                    match(input,CROSS,FOLLOW_CROSS_in_joined_table1113); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CROSS112_tree = (Object)adaptor.create(CROSS112);
                    adaptor.addChild(root_0, CROSS112_tree);
                    }
                    JOIN113=(Token)input.LT(1);
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table1115); if (failed) return retval;
                    if ( backtracking==0 ) {
                    JOIN113_tree = (Object)adaptor.create(JOIN113);
                    adaptor.addChild(root_0, JOIN113_tree);
                    }
                    pushFollow(FOLLOW_table_ref_in_joined_table1117);
                    table_ref114=table_ref();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref114.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:212:5: join_type JOIN table_ref join_qual
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_join_type_in_joined_table1123);
                    join_type115=join_type();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_type115.getTree());
                    JOIN116=(Token)input.LT(1);
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table1125); if (failed) return retval;
                    if ( backtracking==0 ) {
                    JOIN116_tree = (Object)adaptor.create(JOIN116);
                    adaptor.addChild(root_0, JOIN116_tree);
                    }
                    pushFollow(FOLLOW_table_ref_in_joined_table1127);
                    table_ref117=table_ref();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref117.getTree());
                    pushFollow(FOLLOW_join_qual_in_joined_table1129);
                    join_qual118=join_qual();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_qual118.getTree());

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:213:5: JOIN table_ref join_qual
                    {
                    root_0 = (Object)adaptor.nil();

                    JOIN119=(Token)input.LT(1);
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table1135); if (failed) return retval;
                    if ( backtracking==0 ) {
                    JOIN119_tree = (Object)adaptor.create(JOIN119);
                    adaptor.addChild(root_0, JOIN119_tree);
                    }
                    pushFollow(FOLLOW_table_ref_in_joined_table1137);
                    table_ref120=table_ref();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref120.getTree());
                    pushFollow(FOLLOW_join_qual_in_joined_table1139);
                    join_qual121=join_qual();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_qual121.getTree());

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:214:5: NATURAL join_type JOIN table_ref
                    {
                    root_0 = (Object)adaptor.nil();

                    NATURAL122=(Token)input.LT(1);
                    match(input,NATURAL,FOLLOW_NATURAL_in_joined_table1145); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NATURAL122_tree = (Object)adaptor.create(NATURAL122);
                    adaptor.addChild(root_0, NATURAL122_tree);
                    }
                    pushFollow(FOLLOW_join_type_in_joined_table1147);
                    join_type123=join_type();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_type123.getTree());
                    JOIN124=(Token)input.LT(1);
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table1149); if (failed) return retval;
                    if ( backtracking==0 ) {
                    JOIN124_tree = (Object)adaptor.create(JOIN124);
                    adaptor.addChild(root_0, JOIN124_tree);
                    }
                    pushFollow(FOLLOW_table_ref_in_joined_table1151);
                    table_ref125=table_ref();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref125.getTree());

                    }
                    break;
                case 5 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:215:5: NATURAL JOIN table_ref
                    {
                    root_0 = (Object)adaptor.nil();

                    NATURAL126=(Token)input.LT(1);
                    match(input,NATURAL,FOLLOW_NATURAL_in_joined_table1157); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NATURAL126_tree = (Object)adaptor.create(NATURAL126);
                    adaptor.addChild(root_0, NATURAL126_tree);
                    }
                    JOIN127=(Token)input.LT(1);
                    match(input,JOIN,FOLLOW_JOIN_in_joined_table1159); if (failed) return retval;
                    if ( backtracking==0 ) {
                    JOIN127_tree = (Object)adaptor.create(JOIN127);
                    adaptor.addChild(root_0, JOIN127_tree);
                    }
                    pushFollow(FOLLOW_table_ref_in_joined_table1161);
                    table_ref128=table_ref();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, table_ref128.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end joined_table

    public static class join_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start join_type
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:218:1: join_type : ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER );
    public final join_type_return join_type() throws RecognitionException {
        join_type_return retval = new join_type_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FULL129=null;
        Token LEFT131=null;
        Token RIGHT133=null;
        Token INNER135=null;
        join_outer_return join_outer130 = null;

        join_outer_return join_outer132 = null;

        join_outer_return join_outer134 = null;


        Object FULL129_tree=null;
        Object LEFT131_tree=null;
        Object RIGHT133_tree=null;
        Object INNER135_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:218:11: ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER )
            int alt30=4;
            switch ( input.LA(1) ) {
            case FULL:
                {
                alt30=1;
                }
                break;
            case LEFT:
                {
                alt30=2;
                }
                break;
            case RIGHT:
                {
                alt30=3;
                }
                break;
            case INNER:
                {
                alt30=4;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("218:1: join_type : ( FULL join_outer | LEFT join_outer | RIGHT join_outer | INNER );", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:218:13: FULL join_outer
                    {
                    root_0 = (Object)adaptor.nil();

                    FULL129=(Token)input.LT(1);
                    match(input,FULL,FOLLOW_FULL_in_join_type1176); if (failed) return retval;
                    if ( backtracking==0 ) {
                    FULL129_tree = (Object)adaptor.create(FULL129);
                    adaptor.addChild(root_0, FULL129_tree);
                    }
                    pushFollow(FOLLOW_join_outer_in_join_type1178);
                    join_outer130=join_outer();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_outer130.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:219:5: LEFT join_outer
                    {
                    root_0 = (Object)adaptor.nil();

                    LEFT131=(Token)input.LT(1);
                    match(input,LEFT,FOLLOW_LEFT_in_join_type1185); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LEFT131_tree = (Object)adaptor.create(LEFT131);
                    adaptor.addChild(root_0, LEFT131_tree);
                    }
                    pushFollow(FOLLOW_join_outer_in_join_type1187);
                    join_outer132=join_outer();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_outer132.getTree());

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:220:5: RIGHT join_outer
                    {
                    root_0 = (Object)adaptor.nil();

                    RIGHT133=(Token)input.LT(1);
                    match(input,RIGHT,FOLLOW_RIGHT_in_join_type1194); if (failed) return retval;
                    if ( backtracking==0 ) {
                    RIGHT133_tree = (Object)adaptor.create(RIGHT133);
                    adaptor.addChild(root_0, RIGHT133_tree);
                    }
                    pushFollow(FOLLOW_join_outer_in_join_type1196);
                    join_outer134=join_outer();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, join_outer134.getTree());

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:221:5: INNER
                    {
                    root_0 = (Object)adaptor.nil();

                    INNER135=(Token)input.LT(1);
                    match(input,INNER,FOLLOW_INNER_in_join_type1203); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INNER135_tree = (Object)adaptor.create(INNER135);
                    adaptor.addChild(root_0, INNER135_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end join_type

    public static class join_outer_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start join_outer
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:225:1: join_outer : ( OUTER | );
    public final join_outer_return join_outer() throws RecognitionException {
        join_outer_return retval = new join_outer_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OUTER136=null;

        Object OUTER136_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:225:12: ( OUTER | )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==OUTER) ) {
                alt31=1;
            }
            else if ( (LA31_0==JOIN) ) {
                alt31=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("225:1: join_outer : ( OUTER | );", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:225:14: OUTER
                    {
                    root_0 = (Object)adaptor.nil();

                    OUTER136=(Token)input.LT(1);
                    match(input,OUTER,FOLLOW_OUTER_in_join_outer1219); if (failed) return retval;
                    if ( backtracking==0 ) {
                    OUTER136_tree = (Object)adaptor.create(OUTER136);
                    adaptor.addChild(root_0, OUTER136_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:227:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end join_outer

    public static class join_qual_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start join_qual
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:229:1: join_qual : ( USING '(' name_list ')' | ON a_expr );
    public final join_qual_return join_qual() throws RecognitionException {
        join_qual_return retval = new join_qual_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token USING137=null;
        Token char_literal138=null;
        Token char_literal140=null;
        Token ON141=null;
        name_list_return name_list139 = null;

        a_expr_return a_expr142 = null;


        Object USING137_tree=null;
        Object char_literal138_tree=null;
        Object char_literal140_tree=null;
        Object ON141_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:229:11: ( USING '(' name_list ')' | ON a_expr )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==USING) ) {
                alt32=1;
            }
            else if ( (LA32_0==ON) ) {
                alt32=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("229:1: join_qual : ( USING '(' name_list ')' | ON a_expr );", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:229:13: USING '(' name_list ')'
                    {
                    root_0 = (Object)adaptor.nil();

                    USING137=(Token)input.LT(1);
                    match(input,USING,FOLLOW_USING_in_join_qual1236); if (failed) return retval;
                    if ( backtracking==0 ) {
                    USING137_tree = (Object)adaptor.create(USING137);
                    adaptor.addChild(root_0, USING137_tree);
                    }
                    char_literal138=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_join_qual1238); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal138_tree = (Object)adaptor.create(char_literal138);
                    adaptor.addChild(root_0, char_literal138_tree);
                    }
                    pushFollow(FOLLOW_name_list_in_join_qual1240);
                    name_list139=name_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, name_list139.getTree());
                    char_literal140=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_join_qual1242); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal140_tree = (Object)adaptor.create(char_literal140);
                    adaptor.addChild(root_0, char_literal140_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:230:5: ON a_expr
                    {
                    root_0 = (Object)adaptor.nil();

                    ON141=(Token)input.LT(1);
                    match(input,ON,FOLLOW_ON_in_join_qual1249); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ON141_tree = (Object)adaptor.create(ON141);
                    adaptor.addChild(root_0, ON141_tree);
                    }
                    pushFollow(FOLLOW_a_expr_in_join_qual1251);
                    a_expr142=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, a_expr142.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end join_qual

    public static class opt_alias_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start opt_alias
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:234:1: opt_alias : ( ( alias_clause )=> alias_clause | );
    public final opt_alias_return opt_alias() throws RecognitionException {
        opt_alias_return retval = new opt_alias_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        alias_clause_return alias_clause143 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:234:12: ( ( alias_clause )=> alias_clause | )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==ID) && (synpred5())) {
                alt33=1;
            }
            else if ( (LA33_0==AS) && (synpred5())) {
                alt33=1;
            }
            else if ( (LA33_0==EOF) ) {
                alt33=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("234:1: opt_alias : ( ( alias_clause )=> alias_clause | );", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:234:14: ( alias_clause )=> alias_clause
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_alias_clause_in_opt_alias1271);
                    alias_clause143=alias_clause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, alias_clause143.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:236:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end opt_alias

    public static class alias_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start alias_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:1: alias_clause : ( ( id )=> id -> ^( ALIAS id ) | AS id -> ^( ALIAS id ) );
    public final alias_clause_return alias_clause() throws RecognitionException {
        alias_clause_return retval = new alias_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AS145=null;
        id_return id144 = null;

        id_return id146 = null;


        Object AS145_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:14: ( ( id )=> id -> ^( ALIAS id ) | AS id -> ^( ALIAS id ) )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==ID) && (synpred6())) {
                alt34=1;
            }
            else if ( (LA34_0==AS) ) {
                alt34=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("238:1: alias_clause : ( ( id )=> id -> ^( ALIAS id ) | AS id -> ^( ALIAS id ) );", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:16: ( id )=> id
                    {
                    pushFollow(FOLLOW_id_in_alias_clause1303);
                    id144=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_id.add(id144.getTree());

                    // AST REWRITE
                    // elements: id
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 238:27: -> ^( ALIAS id )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:30: ^( ALIAS id )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(ALIAS, "ALIAS"), root_1);

                        adaptor.addChild(root_1, stream_id.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:239:5: AS id
                    {
                    AS145=(Token)input.LT(1);
                    match(input,AS,FOLLOW_AS_in_alias_clause1317); if (failed) return retval;
                    if ( backtracking==0 ) stream_AS.add(AS145);

                    pushFollow(FOLLOW_id_in_alias_clause1319);
                    id146=id();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_id.add(id146.getTree());

                    // AST REWRITE
                    // elements: id
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 239:11: -> ^( ALIAS id )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:239:14: ^( ALIAS id )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(ALIAS, "ALIAS"), root_1);

                        adaptor.addChild(root_1, stream_id.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end alias_clause

    public static class name_list_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start name_list
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:242:1: name_list : id ( ',' id )* -> ( id )+ ;
    public final name_list_return name_list() throws RecognitionException {
        name_list_return retval = new name_list_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal148=null;
        id_return id147 = null;

        id_return id149 = null;


        Object char_literal148_tree=null;
        RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:242:11: ( id ( ',' id )* -> ( id )+ )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:242:13: id ( ',' id )*
            {
            pushFollow(FOLLOW_id_in_name_list1344);
            id147=id();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_id.add(id147.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:242:16: ( ',' id )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==89) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:242:17: ',' id
            	    {
            	    char_literal148=(Token)input.LT(1);
            	    match(input,89,FOLLOW_89_in_name_list1347); if (failed) return retval;
            	    if ( backtracking==0 ) stream_89.add(char_literal148);

            	    pushFollow(FOLLOW_id_in_name_list1349);
            	    id149=id();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_id.add(id149.getTree());

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


            // AST REWRITE
            // elements: id
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 242:26: -> ( id )+
            {
                if ( !(stream_id.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_id.hasNext() ) {
                    adaptor.addChild(root_0, stream_id.next());

                }
                stream_id.reset();

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end name_list

    public static class relation_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start relation_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:245:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );
    public final relation_expr_return relation_expr() throws RecognitionException {
        relation_expr_return retval = new relation_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR151=null;
        Token ONLY152=null;
        Token ONLY154=null;
        Token char_literal155=null;
        Token char_literal157=null;
        qualified_name_return qualified_name150 = null;

        qualified_name_return qualified_name153 = null;

        qualified_name_return qualified_name156 = null;


        Object STAR151_tree=null;
        Object ONLY152_tree=null;
        Object ONLY154_tree=null;
        Object char_literal155_tree=null;
        Object char_literal157_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:245:15: ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' )
            int alt37=3;
            int LA37_0 = input.LA(1);

            if ( ((LA37_0>=OLD && LA37_0<=NEW)||LA37_0==ID) ) {
                alt37=1;
            }
            else if ( (LA37_0==ONLY) ) {
                int LA37_2 = input.LA(2);

                if ( (LA37_2==LPAREN) ) {
                    alt37=3;
                }
                else if ( ((LA37_2>=OLD && LA37_2<=NEW)||LA37_2==ID) ) {
                    alt37=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("245:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );", 37, 2, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("245:1: relation_expr : ( qualified_name ( STAR )? | ONLY qualified_name | ONLY '(' qualified_name ')' );", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:245:17: qualified_name ( STAR )?
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_qualified_name_in_relation_expr1369);
                    qualified_name150=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name150.getTree());
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:245:32: ( STAR )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==STAR) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:245:32: STAR
                            {
                            STAR151=(Token)input.LT(1);
                            match(input,STAR,FOLLOW_STAR_in_relation_expr1371); if (failed) return retval;
                            if ( backtracking==0 ) {
                            STAR151_tree = (Object)adaptor.create(STAR151);
                            adaptor.addChild(root_0, STAR151_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:246:5: ONLY qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    ONLY152=(Token)input.LT(1);
                    match(input,ONLY,FOLLOW_ONLY_in_relation_expr1379); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ONLY152_tree = (Object)adaptor.create(ONLY152);
                    adaptor.addChild(root_0, ONLY152_tree);
                    }
                    pushFollow(FOLLOW_qualified_name_in_relation_expr1382);
                    qualified_name153=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name153.getTree());

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:247:5: ONLY '(' qualified_name ')'
                    {
                    root_0 = (Object)adaptor.nil();

                    ONLY154=(Token)input.LT(1);
                    match(input,ONLY,FOLLOW_ONLY_in_relation_expr1389); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ONLY154_tree = (Object)adaptor.create(ONLY154);
                    adaptor.addChild(root_0, ONLY154_tree);
                    }
                    char_literal155=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_relation_expr1391); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal155_tree = (Object)adaptor.create(char_literal155);
                    adaptor.addChild(root_0, char_literal155_tree);
                    }
                    pushFollow(FOLLOW_qualified_name_in_relation_expr1393);
                    qualified_name156=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name156.getTree());
                    char_literal157=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_relation_expr1395); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal157_tree = (Object)adaptor.create(char_literal157);
                    adaptor.addChild(root_0, char_literal157_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end relation_expr

    public static class func_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start func_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:252:1: func_expr : func_name '(' func_args ')' -> ^( func_name func_args ) ;
    public final func_expr_return func_expr() throws RecognitionException {
        func_expr_return retval = new func_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal159=null;
        Token char_literal161=null;
        func_name_return func_name158 = null;

        func_args_return func_args160 = null;


        Object char_literal159_tree=null;
        Object char_literal161_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_func_name=new RewriteRuleSubtreeStream(adaptor,"rule func_name");
        RewriteRuleSubtreeStream stream_func_args=new RewriteRuleSubtreeStream(adaptor,"rule func_args");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:252:12: ( func_name '(' func_args ')' -> ^( func_name func_args ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:252:15: func_name '(' func_args ')'
            {
            pushFollow(FOLLOW_func_name_in_func_expr1411);
            func_name158=func_name();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_func_name.add(func_name158.getTree());
            char_literal159=(Token)input.LT(1);
            match(input,LPAREN,FOLLOW_LPAREN_in_func_expr1413); if (failed) return retval;
            if ( backtracking==0 ) stream_LPAREN.add(char_literal159);

            pushFollow(FOLLOW_func_args_in_func_expr1415);
            func_args160=func_args();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_func_args.add(func_args160.getTree());
            char_literal161=(Token)input.LT(1);
            match(input,RPAREN,FOLLOW_RPAREN_in_func_expr1417); if (failed) return retval;
            if ( backtracking==0 ) stream_RPAREN.add(char_literal161);


            // AST REWRITE
            // elements: func_args, func_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 252:44: -> ^( func_name func_args )
            {
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:252:47: ^( func_name func_args )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_func_name.nextNode(), root_1);

                adaptor.addChild(root_1, stream_func_args.next());

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end func_expr

    public static class func_args_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start func_args
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:255:1: func_args : ( ( ALL | DISTINCT )? expr_list | STAR );
    public final func_args_return func_args() throws RecognitionException {
        func_args_return retval = new func_args_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set162=null;
        Token STAR164=null;
        expr_list_return expr_list163 = null;


        Object set162_tree=null;
        Object STAR164_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:255:11: ( ( ALL | DISTINCT )? expr_list | STAR )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==LPAREN||LA39_0==DISTINCT||LA39_0==ALL||(LA39_0>=OLD && LA39_0<=NEW)||LA39_0==NOT||(LA39_0>=EXISTS && LA39_0<=QUOTEDSTRING)||LA39_0==NUMBER||LA39_0==ID) ) {
                alt39=1;
            }
            else if ( (LA39_0==STAR) ) {
                alt39=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("255:1: func_args : ( ( ALL | DISTINCT )? expr_list | STAR );", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:255:13: ( ALL | DISTINCT )? expr_list
                    {
                    root_0 = (Object)adaptor.nil();

                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:255:13: ( ALL | DISTINCT )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==DISTINCT||LA38_0==ALL) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:
                            {
                            set162=(Token)input.LT(1);
                            if ( input.LA(1)==DISTINCT||input.LA(1)==ALL ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set162));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_func_args1438);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_expr_list_in_func_args1447);
                    expr_list163=expr_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, expr_list163.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:256:5: STAR
                    {
                    root_0 = (Object)adaptor.nil();

                    STAR164=(Token)input.LT(1);
                    match(input,STAR,FOLLOW_STAR_in_func_args1453); if (failed) return retval;
                    if ( backtracking==0 ) {
                    STAR164_tree = (Object)adaptor.create(STAR164);
                    adaptor.addChild(root_0, STAR164_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end func_args

    public static class func_name_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start func_name
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:259:1: func_name : id ;
    public final func_name_return func_name() throws RecognitionException {
        func_name_return retval = new func_name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        id_return id165 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:259:11: ( id )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:259:13: id
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_id_in_func_name1469);
            id165=id();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, id165.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end func_name

    public static class a_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start a_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:1: a_expr : b_expr ( (or= OR ) b_expr )* -> {$or==null}? ( b_expr )+ -> ^( OR[\"OR\"] ( b_expr )+ ) ;
    public final a_expr_return a_expr() throws RecognitionException {
        a_expr_return retval = new a_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token or=null;
        b_expr_return b_expr166 = null;

        b_expr_return b_expr167 = null;


        Object or_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_b_expr=new RewriteRuleSubtreeStream(adaptor,"rule b_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:9: ( b_expr ( (or= OR ) b_expr )* -> {$or==null}? ( b_expr )+ -> ^( OR[\"OR\"] ( b_expr )+ ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:11: b_expr ( (or= OR ) b_expr )*
            {
            pushFollow(FOLLOW_b_expr_in_a_expr1485);
            b_expr166=b_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_b_expr.add(b_expr166.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:18: ( (or= OR ) b_expr )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==OR) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:19: (or= OR ) b_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:19: (or= OR )
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:269:20: or= OR
            	    {
            	    or=(Token)input.LT(1);
            	    match(input,OR,FOLLOW_OR_in_a_expr1491); if (failed) return retval;
            	    if ( backtracking==0 ) stream_OR.add(or);


            	    }

            	    pushFollow(FOLLOW_b_expr_in_a_expr1494);
            	    b_expr167=b_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_b_expr.add(b_expr167.getTree());

            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            // AST REWRITE
            // elements: OR, b_expr, b_expr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 269:36: -> {$or==null}? ( b_expr )+
            if (or==null) {
                if ( !(stream_b_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_b_expr.hasNext() ) {
                    adaptor.addChild(root_0, stream_b_expr.next());

                }
                stream_b_expr.reset();

            }
            else // 270:7: -> ^( OR[\"OR\"] ( b_expr )+ )
            {
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:270:10: ^( OR[\"OR\"] ( b_expr )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(OR,"OR"), root_1);

                if ( !(stream_b_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_b_expr.hasNext() ) {
                    adaptor.addChild(root_1, stream_b_expr.next());

                }
                stream_b_expr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end a_expr

    public static class b_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start b_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:1: b_expr : c_expr ( (and= AND ) c_expr )* -> {$and==null}? ( c_expr )+ -> ^( AND[\"AND\"] ( c_expr )+ ) ;
    public final b_expr_return b_expr() throws RecognitionException {
        b_expr_return retval = new b_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token and=null;
        c_expr_return c_expr168 = null;

        c_expr_return c_expr169 = null;


        Object and_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_c_expr=new RewriteRuleSubtreeStream(adaptor,"rule c_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:10: ( c_expr ( (and= AND ) c_expr )* -> {$and==null}? ( c_expr )+ -> ^( AND[\"AND\"] ( c_expr )+ ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:12: c_expr ( (and= AND ) c_expr )*
            {
            pushFollow(FOLLOW_c_expr_in_b_expr1535);
            c_expr168=c_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_c_expr.add(c_expr168.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:19: ( (and= AND ) c_expr )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==AND) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:20: (and= AND ) c_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:20: (and= AND )
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:273:21: and= AND
            	    {
            	    and=(Token)input.LT(1);
            	    match(input,AND,FOLLOW_AND_in_b_expr1541); if (failed) return retval;
            	    if ( backtracking==0 ) stream_AND.add(and);


            	    }

            	    pushFollow(FOLLOW_c_expr_in_b_expr1544);
            	    c_expr169=c_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_c_expr.add(c_expr169.getTree());

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);


            // AST REWRITE
            // elements: c_expr, c_expr, AND
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 273:39: -> {$and==null}? ( c_expr )+
            if (and==null) {
                if ( !(stream_c_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_c_expr.hasNext() ) {
                    adaptor.addChild(root_0, stream_c_expr.next());

                }
                stream_c_expr.reset();

            }
            else // 274:11: -> ^( AND[\"AND\"] ( c_expr )+ )
            {
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:274:14: ^( AND[\"AND\"] ( c_expr )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(adaptor.create(AND,"AND"), root_1);

                if ( !(stream_c_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_c_expr.hasNext() ) {
                    adaptor.addChild(root_1, stream_c_expr.next());

                }
                stream_c_expr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end b_expr

    public static class c_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start c_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:277:1: c_expr : ( NOT )* d_expr ;
    public final c_expr_return c_expr() throws RecognitionException {
        c_expr_return retval = new c_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT170=null;
        d_expr_return d_expr171 = null;


        Object NOT170_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:277:9: ( ( NOT )* d_expr )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:277:11: ( NOT )* d_expr
            {
            root_0 = (Object)adaptor.nil();

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:277:11: ( NOT )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==NOT) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:277:12: NOT
            	    {
            	    NOT170=(Token)input.LT(1);
            	    match(input,NOT,FOLLOW_NOT_in_c_expr1588); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    NOT170_tree = (Object)adaptor.create(NOT170);
            	    root_0 = (Object)adaptor.becomeRoot(NOT170_tree, root_0);
            	    }

            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);

            pushFollow(FOLLOW_d_expr_in_c_expr1593);
            d_expr171=d_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, d_expr171.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end c_expr

    public static class d_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start d_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:280:1: d_expr : e_expr ( EQ e_expr )* ;
    public final d_expr_return d_expr() throws RecognitionException {
        d_expr_return retval = new d_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQ173=null;
        e_expr_return e_expr172 = null;

        e_expr_return e_expr174 = null;


        Object EQ173_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:280:10: ( e_expr ( EQ e_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:280:12: e_expr ( EQ e_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_e_expr_in_d_expr1610);
            e_expr172=e_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, e_expr172.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:280:19: ( EQ e_expr )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==EQ) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:280:20: EQ e_expr
            	    {
            	    EQ173=(Token)input.LT(1);
            	    match(input,EQ,FOLLOW_EQ_in_d_expr1613); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    EQ173_tree = (Object)adaptor.create(EQ173);
            	    root_0 = (Object)adaptor.becomeRoot(EQ173_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_e_expr_in_d_expr1617);
            	    e_expr174=e_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, e_expr174.getTree());

            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end d_expr

    public static class e_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start e_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:1: e_expr : f_expr ( ( GT | LT ) f_expr )* ;
    public final e_expr_return e_expr() throws RecognitionException {
        e_expr_return retval = new e_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token GT176=null;
        Token LT177=null;
        f_expr_return f_expr175 = null;

        f_expr_return f_expr178 = null;


        Object GT176_tree=null;
        Object LT177_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:10: ( f_expr ( ( GT | LT ) f_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:12: f_expr ( ( GT | LT ) f_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_f_expr_in_e_expr1635);
            f_expr175=f_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, f_expr175.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:19: ( ( GT | LT ) f_expr )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( ((LA45_0>=GT && LA45_0<=LT)) ) {
                    alt45=1;
                }


                switch (alt45) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:20: ( GT | LT ) f_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:20: ( GT | LT )
            	    int alt44=2;
            	    int LA44_0 = input.LA(1);

            	    if ( (LA44_0==GT) ) {
            	        alt44=1;
            	    }
            	    else if ( (LA44_0==LT) ) {
            	        alt44=2;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("283:20: ( GT | LT )", 44, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt44) {
            	        case 1 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:21: GT
            	            {
            	            GT176=(Token)input.LT(1);
            	            match(input,GT,FOLLOW_GT_in_e_expr1639); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            GT176_tree = (Object)adaptor.create(GT176);
            	            root_0 = (Object)adaptor.becomeRoot(GT176_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:283:28: LT
            	            {
            	            LT177=(Token)input.LT(1);
            	            match(input,LT,FOLLOW_LT_in_e_expr1645); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            LT177_tree = (Object)adaptor.create(LT177);
            	            root_0 = (Object)adaptor.becomeRoot(LT177_tree, root_0);
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_f_expr_in_e_expr1650);
            	    f_expr178=f_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, f_expr178.getTree());

            	    }
            	    break;

            	default :
            	    break loop45;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end e_expr

    public static class f_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start f_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:1: f_expr : g_expr ( ( LIKE | ILIKE | SIMILAR TO ) g_expr )* ;
    public final f_expr_return f_expr() throws RecognitionException {
        f_expr_return retval = new f_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LIKE180=null;
        Token ILIKE181=null;
        Token SIMILAR182=null;
        Token TO183=null;
        g_expr_return g_expr179 = null;

        g_expr_return g_expr184 = null;


        Object LIKE180_tree=null;
        Object ILIKE181_tree=null;
        Object SIMILAR182_tree=null;
        Object TO183_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:9: ( g_expr ( ( LIKE | ILIKE | SIMILAR TO ) g_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:11: g_expr ( ( LIKE | ILIKE | SIMILAR TO ) g_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_g_expr_in_f_expr1666);
            g_expr179=g_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, g_expr179.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:18: ( ( LIKE | ILIKE | SIMILAR TO ) g_expr )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( ((LA47_0>=LIKE && LA47_0<=SIMILAR)) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:19: ( LIKE | ILIKE | SIMILAR TO ) g_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:19: ( LIKE | ILIKE | SIMILAR TO )
            	    int alt46=3;
            	    switch ( input.LA(1) ) {
            	    case LIKE:
            	        {
            	        alt46=1;
            	        }
            	        break;
            	    case ILIKE:
            	        {
            	        alt46=2;
            	        }
            	        break;
            	    case SIMILAR:
            	        {
            	        alt46=3;
            	        }
            	        break;
            	    default:
            	        if (backtracking>0) {failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("286:19: ( LIKE | ILIKE | SIMILAR TO )", 46, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt46) {
            	        case 1 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:20: LIKE
            	            {
            	            LIKE180=(Token)input.LT(1);
            	            match(input,LIKE,FOLLOW_LIKE_in_f_expr1670); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            LIKE180_tree = (Object)adaptor.create(LIKE180);
            	            root_0 = (Object)adaptor.becomeRoot(LIKE180_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:29: ILIKE
            	            {
            	            ILIKE181=(Token)input.LT(1);
            	            match(input,ILIKE,FOLLOW_ILIKE_in_f_expr1676); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            ILIKE181_tree = (Object)adaptor.create(ILIKE181);
            	            root_0 = (Object)adaptor.becomeRoot(ILIKE181_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 3 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:286:39: SIMILAR TO
            	            {
            	            SIMILAR182=(Token)input.LT(1);
            	            match(input,SIMILAR,FOLLOW_SIMILAR_in_f_expr1682); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            SIMILAR182_tree = (Object)adaptor.create(SIMILAR182);
            	            adaptor.addChild(root_0, SIMILAR182_tree);
            	            }
            	            TO183=(Token)input.LT(1);
            	            match(input,TO,FOLLOW_TO_in_f_expr1684); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            TO183_tree = (Object)adaptor.create(TO183);
            	            root_0 = (Object)adaptor.becomeRoot(TO183_tree, root_0);
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_g_expr_in_f_expr1689);
            	    g_expr184=g_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, g_expr184.getTree());

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end f_expr

    public static class g_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start g_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:289:1: g_expr : h_expr ( binary_op h_expr )* ;
    public final g_expr_return g_expr() throws RecognitionException {
        g_expr_return retval = new g_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        h_expr_return h_expr185 = null;

        binary_op_return binary_op186 = null;

        h_expr_return h_expr187 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:289:10: ( h_expr ( binary_op h_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:289:12: h_expr ( binary_op h_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_h_expr_in_g_expr1706);
            h_expr185=h_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, h_expr185.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:289:19: ( binary_op h_expr )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( ((LA48_0>=GE && LA48_0<=LE)) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:289:20: binary_op h_expr
            	    {
            	    pushFollow(FOLLOW_binary_op_in_g_expr1709);
            	    binary_op186=binary_op();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) root_0 = (Object)adaptor.becomeRoot(binary_op186.getTree(), root_0);
            	    pushFollow(FOLLOW_h_expr_in_g_expr1712);
            	    h_expr187=h_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, h_expr187.getTree());

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end g_expr

    public static class h_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start h_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:1: h_expr : i_expr ( NOTNULL | IS NULL | ISNULL | IS NOT NULL )* ;
    public final h_expr_return h_expr() throws RecognitionException {
        h_expr_return retval = new h_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOTNULL189=null;
        Token IS190=null;
        Token NULL191=null;
        Token ISNULL192=null;
        Token IS193=null;
        Token NOT194=null;
        Token NULL195=null;
        i_expr_return i_expr188 = null;


        Object NOTNULL189_tree=null;
        Object IS190_tree=null;
        Object NULL191_tree=null;
        Object ISNULL192_tree=null;
        Object IS193_tree=null;
        Object NOT194_tree=null;
        Object NULL195_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:9: ( i_expr ( NOTNULL | IS NULL | ISNULL | IS NOT NULL )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:11: i_expr ( NOTNULL | IS NULL | ISNULL | IS NOT NULL )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_i_expr_in_h_expr1728);
            i_expr188=i_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, i_expr188.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:18: ( NOTNULL | IS NULL | ISNULL | IS NOT NULL )*
            loop49:
            do {
                int alt49=5;
                switch ( input.LA(1) ) {
                case NOTNULL:
                    {
                    alt49=1;
                    }
                    break;
                case IS:
                    {
                    int LA49_3 = input.LA(2);

                    if ( (LA49_3==NULL) ) {
                        alt49=2;
                    }
                    else if ( (LA49_3==NOT) ) {
                        alt49=4;
                    }


                    }
                    break;
                case ISNULL:
                    {
                    alt49=3;
                    }
                    break;

                }

                switch (alt49) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:20: NOTNULL
            	    {
            	    NOTNULL189=(Token)input.LT(1);
            	    match(input,NOTNULL,FOLLOW_NOTNULL_in_h_expr1732); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    NOTNULL189_tree = (Object)adaptor.create(NOTNULL189);
            	    root_0 = (Object)adaptor.becomeRoot(NOTNULL189_tree, root_0);
            	    }

            	    }
            	    break;
            	case 2 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:31: IS NULL
            	    {
            	    IS190=(Token)input.LT(1);
            	    match(input,IS,FOLLOW_IS_in_h_expr1737); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    IS190_tree = (Object)adaptor.create(IS190);
            	    adaptor.addChild(root_0, IS190_tree);
            	    }
            	    NULL191=(Token)input.LT(1);
            	    match(input,NULL,FOLLOW_NULL_in_h_expr1739); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    NULL191_tree = (Object)adaptor.create(NULL191);
            	    root_0 = (Object)adaptor.becomeRoot(NULL191_tree, root_0);
            	    }

            	    }
            	    break;
            	case 3 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:42: ISNULL
            	    {
            	    ISNULL192=(Token)input.LT(1);
            	    match(input,ISNULL,FOLLOW_ISNULL_in_h_expr1744); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    ISNULL192_tree = (Object)adaptor.create(ISNULL192);
            	    root_0 = (Object)adaptor.becomeRoot(ISNULL192_tree, root_0);
            	    }

            	    }
            	    break;
            	case 4 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:292:52: IS NOT NULL
            	    {
            	    IS193=(Token)input.LT(1);
            	    match(input,IS,FOLLOW_IS_in_h_expr1749); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    IS193_tree = (Object)adaptor.create(IS193);
            	    adaptor.addChild(root_0, IS193_tree);
            	    }
            	    NOT194=(Token)input.LT(1);
            	    match(input,NOT,FOLLOW_NOT_in_h_expr1751); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    NOT194_tree = (Object)adaptor.create(NOT194);
            	    adaptor.addChild(root_0, NOT194_tree);
            	    }
            	    NULL195=(Token)input.LT(1);
            	    match(input,NULL,FOLLOW_NULL_in_h_expr1753); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    NULL195_tree = (Object)adaptor.create(NULL195);
            	    root_0 = (Object)adaptor.becomeRoot(NULL195_tree, root_0);
            	    }

            	    }
            	    break;

            	default :
            	    break loop49;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end h_expr

    public static class i_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start i_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:1: i_expr : j_expr ( ( PLUS | MINUS ) j_expr )* ;
    public final i_expr_return i_expr() throws RecognitionException {
        i_expr_return retval = new i_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS197=null;
        Token MINUS198=null;
        j_expr_return j_expr196 = null;

        j_expr_return j_expr199 = null;


        Object PLUS197_tree=null;
        Object MINUS198_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:9: ( j_expr ( ( PLUS | MINUS ) j_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:11: j_expr ( ( PLUS | MINUS ) j_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_j_expr_in_i_expr1771);
            j_expr196=j_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, j_expr196.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:18: ( ( PLUS | MINUS ) j_expr )*
            loop51:
            do {
                int alt51=2;
                int LA51_0 = input.LA(1);

                if ( ((LA51_0>=PLUS && LA51_0<=MINUS)) ) {
                    alt51=1;
                }


                switch (alt51) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:19: ( PLUS | MINUS ) j_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:19: ( PLUS | MINUS )
            	    int alt50=2;
            	    int LA50_0 = input.LA(1);

            	    if ( (LA50_0==PLUS) ) {
            	        alt50=1;
            	    }
            	    else if ( (LA50_0==MINUS) ) {
            	        alt50=2;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("295:19: ( PLUS | MINUS )", 50, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt50) {
            	        case 1 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:20: PLUS
            	            {
            	            PLUS197=(Token)input.LT(1);
            	            match(input,PLUS,FOLLOW_PLUS_in_i_expr1775); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            PLUS197_tree = (Object)adaptor.create(PLUS197);
            	            root_0 = (Object)adaptor.becomeRoot(PLUS197_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:295:28: MINUS
            	            {
            	            MINUS198=(Token)input.LT(1);
            	            match(input,MINUS,FOLLOW_MINUS_in_i_expr1780); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            MINUS198_tree = (Object)adaptor.create(MINUS198);
            	            root_0 = (Object)adaptor.becomeRoot(MINUS198_tree, root_0);
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_j_expr_in_i_expr1785);
            	    j_expr199=j_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, j_expr199.getTree());

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end i_expr

    public static class j_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start j_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:1: j_expr : k_expr ( ( STAR | DIV | PCT ) k_expr )* ;
    public final j_expr_return j_expr() throws RecognitionException {
        j_expr_return retval = new j_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token STAR201=null;
        Token DIV202=null;
        Token PCT203=null;
        k_expr_return k_expr200 = null;

        k_expr_return k_expr204 = null;


        Object STAR201_tree=null;
        Object DIV202_tree=null;
        Object PCT203_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:10: ( k_expr ( ( STAR | DIV | PCT ) k_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:12: k_expr ( ( STAR | DIV | PCT ) k_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_k_expr_in_j_expr1802);
            k_expr200=k_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, k_expr200.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:19: ( ( STAR | DIV | PCT ) k_expr )*
            loop53:
            do {
                int alt53=2;
                int LA53_0 = input.LA(1);

                if ( ((LA53_0>=STAR && LA53_0<=DIV)||LA53_0==PCT) ) {
                    alt53=1;
                }


                switch (alt53) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:20: ( STAR | DIV | PCT ) k_expr
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:20: ( STAR | DIV | PCT )
            	    int alt52=3;
            	    switch ( input.LA(1) ) {
            	    case STAR:
            	        {
            	        alt52=1;
            	        }
            	        break;
            	    case DIV:
            	        {
            	        alt52=2;
            	        }
            	        break;
            	    case PCT:
            	        {
            	        alt52=3;
            	        }
            	        break;
            	    default:
            	        if (backtracking>0) {failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("298:20: ( STAR | DIV | PCT )", 52, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt52) {
            	        case 1 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:21: STAR
            	            {
            	            STAR201=(Token)input.LT(1);
            	            match(input,STAR,FOLLOW_STAR_in_j_expr1806); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            STAR201_tree = (Object)adaptor.create(STAR201);
            	            root_0 = (Object)adaptor.becomeRoot(STAR201_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:29: DIV
            	            {
            	            DIV202=(Token)input.LT(1);
            	            match(input,DIV,FOLLOW_DIV_in_j_expr1811); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            DIV202_tree = (Object)adaptor.create(DIV202);
            	            root_0 = (Object)adaptor.becomeRoot(DIV202_tree, root_0);
            	            }

            	            }
            	            break;
            	        case 3 :
            	            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:298:36: PCT
            	            {
            	            PCT203=(Token)input.LT(1);
            	            match(input,PCT,FOLLOW_PCT_in_j_expr1816); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            PCT203_tree = (Object)adaptor.create(PCT203);
            	            root_0 = (Object)adaptor.becomeRoot(PCT203_tree, root_0);
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_k_expr_in_j_expr1821);
            	    k_expr204=k_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, k_expr204.getTree());

            	    }
            	    break;

            	default :
            	    break loop53;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end j_expr

    public static class k_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start k_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:301:1: k_expr : l_expr ( HAT l_expr )* ;
    public final k_expr_return k_expr() throws RecognitionException {
        k_expr_return retval = new k_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token HAT206=null;
        l_expr_return l_expr205 = null;

        l_expr_return l_expr207 = null;


        Object HAT206_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:301:10: ( l_expr ( HAT l_expr )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:301:12: l_expr ( HAT l_expr )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_l_expr_in_k_expr1839);
            l_expr205=l_expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, l_expr205.getTree());
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:301:19: ( HAT l_expr )*
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( (LA54_0==HAT) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:301:20: HAT l_expr
            	    {
            	    HAT206=(Token)input.LT(1);
            	    match(input,HAT,FOLLOW_HAT_in_k_expr1842); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    HAT206_tree = (Object)adaptor.create(HAT206);
            	    adaptor.addChild(root_0, HAT206_tree);
            	    }
            	    pushFollow(FOLLOW_l_expr_in_k_expr1844);
            	    l_expr207=l_expr();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, l_expr207.getTree());

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end k_expr

    public static class l_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start l_expr
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:1: l_expr : ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );
    public final l_expr_return l_expr() throws RecognitionException {
        l_expr_return retval = new l_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal210=null;
        Token char_literal212=null;
        Token EXISTS214=null;
        Token ARRAY216=null;
        Token QUOTEDSTRING219=null;
        func_expr_return func_expr208 = null;

        qualified_name_return qualified_name209 = null;

        a_expr_return a_expr211 = null;

        select_with_parens_return select_with_parens213 = null;

        select_with_parens_return select_with_parens215 = null;

        select_with_parens_return select_with_parens217 = null;

        factor_return factor218 = null;


        Object char_literal210_tree=null;
        Object char_literal212_tree=null;
        Object EXISTS214_tree=null;
        Object ARRAY216_tree=null;
        Object QUOTEDSTRING219_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_a_expr=new RewriteRuleSubtreeStream(adaptor,"rule a_expr");
        RewriteRuleSubtreeStream stream_func_expr=new RewriteRuleSubtreeStream(adaptor,"rule func_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:9: ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING )
            int alt55=8;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA55_1 = input.LA(2);

                if ( (LA55_1==LPAREN) ) {
                    int LA55_8 = input.LA(3);

                    if ( (LA55_8==DISTINCT||LA55_8==ALL) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==NOT) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==ID) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( ((LA55_8>=OLD && LA55_8<=NEW)) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==LPAREN) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==EXISTS) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==ARRAY) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==NUMBER) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==QUOTEDSTRING) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( (LA55_8==STAR) && (synpred7())) {
                        alt55=1;
                    }
                    else if ( ((LA55_8>=CROSS && LA55_8<=INNER)) ) {
                        alt55=2;
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("304:1: l_expr : ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 55, 8, input);

                        throw nvae;
                    }
                }
                else if ( (LA55_1==EOF||(LA55_1>=PLUS && LA55_1<=LE)||LA55_1==RPAREN||(LA55_1>=AS && LA55_1<=INTO)||(LA55_1>=FROM && LA55_1<=INNER)||(LA55_1>=OR && LA55_1<=AND)||(LA55_1>=LIKE && LA55_1<=SIMILAR)||(LA55_1>=NOTNULL && LA55_1<=IS)||LA55_1==ISNULL||(LA55_1>=WHERE && LA55_1<=GROUP)||(LA55_1>=HAVING && LA55_1<=ORDER)||LA55_1==ID||(LA55_1>=88 && LA55_1<=89)) ) {
                    alt55=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("304:1: l_expr : ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 55, 1, input);

                    throw nvae;
                }
                }
                break;
            case OLD:
            case NEW:
                {
                alt55=2;
                }
                break;
            case LPAREN:
                {
                int LA55_3 = input.LA(2);

                if ( (LA55_3==SELECT) && (synpred8())) {
                    alt55=4;
                }
                else if ( (LA55_3==VALUES) && (synpred8())) {
                    alt55=4;
                }
                else if ( (LA55_3==LPAREN||(LA55_3>=OLD && LA55_3<=NEW)||LA55_3==NOT||(LA55_3>=EXISTS && LA55_3<=QUOTEDSTRING)||LA55_3==NUMBER||LA55_3==ID) ) {
                    alt55=3;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("304:1: l_expr : ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 55, 3, input);

                    throw nvae;
                }
                }
                break;
            case EXISTS:
                {
                alt55=5;
                }
                break;
            case ARRAY:
                {
                alt55=6;
                }
                break;
            case NUMBER:
                {
                alt55=7;
                }
                break;
            case QUOTEDSTRING:
                {
                alt55=8;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("304:1: l_expr : ( ( func_expr )=> func_expr -> ^( FUNCTION func_expr ) | qualified_name | '(' a_expr ')' -> ^( EXPR[\"EXPR\"] a_expr ) | ( select_with_parens )=> select_with_parens | EXISTS select_with_parens | ARRAY select_with_parens | factor | QUOTEDSTRING );", 55, 0, input);

                throw nvae;
            }

            switch (alt55) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:11: ( func_expr )=> func_expr
                    {
                    pushFollow(FOLLOW_func_expr_in_l_expr1864);
                    func_expr208=func_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_func_expr.add(func_expr208.getTree());

                    // AST REWRITE
                    // elements: func_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 304:36: -> ^( FUNCTION func_expr )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:39: ^( FUNCTION func_expr )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(FUNCTION, "FUNCTION"), root_1);

                        adaptor.addChild(root_1, stream_func_expr.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:305:5: qualified_name
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_qualified_name_in_l_expr1878);
                    qualified_name209=qualified_name();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, qualified_name209.getTree());

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:306:5: '(' a_expr ')'
                    {
                    char_literal210=(Token)input.LT(1);
                    match(input,LPAREN,FOLLOW_LPAREN_in_l_expr1884); if (failed) return retval;
                    if ( backtracking==0 ) stream_LPAREN.add(char_literal210);

                    pushFollow(FOLLOW_a_expr_in_l_expr1886);
                    a_expr211=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_a_expr.add(a_expr211.getTree());
                    char_literal212=(Token)input.LT(1);
                    match(input,RPAREN,FOLLOW_RPAREN_in_l_expr1888); if (failed) return retval;
                    if ( backtracking==0 ) stream_RPAREN.add(char_literal212);


                    // AST REWRITE
                    // elements: a_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 306:20: -> ^( EXPR[\"EXPR\"] a_expr )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:306:23: ^( EXPR[\"EXPR\"] a_expr )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_a_expr.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:307:5: ( select_with_parens )=> select_with_parens
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_select_with_parens_in_l_expr1910);
                    select_with_parens213=select_with_parens();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, select_with_parens213.getTree());

                    }
                    break;
                case 5 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:308:5: EXISTS select_with_parens
                    {
                    root_0 = (Object)adaptor.nil();

                    EXISTS214=(Token)input.LT(1);
                    match(input,EXISTS,FOLLOW_EXISTS_in_l_expr1916); if (failed) return retval;
                    if ( backtracking==0 ) {
                    EXISTS214_tree = (Object)adaptor.create(EXISTS214);
                    adaptor.addChild(root_0, EXISTS214_tree);
                    }
                    pushFollow(FOLLOW_select_with_parens_in_l_expr1918);
                    select_with_parens215=select_with_parens();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, select_with_parens215.getTree());

                    }
                    break;
                case 6 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:309:5: ARRAY select_with_parens
                    {
                    root_0 = (Object)adaptor.nil();

                    ARRAY216=(Token)input.LT(1);
                    match(input,ARRAY,FOLLOW_ARRAY_in_l_expr1925); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ARRAY216_tree = (Object)adaptor.create(ARRAY216);
                    adaptor.addChild(root_0, ARRAY216_tree);
                    }
                    pushFollow(FOLLOW_select_with_parens_in_l_expr1927);
                    select_with_parens217=select_with_parens();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, select_with_parens217.getTree());

                    }
                    break;
                case 7 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:310:5: factor
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_factor_in_l_expr1934);
                    factor218=factor();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, factor218.getTree());

                    }
                    break;
                case 8 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:311:5: QUOTEDSTRING
                    {
                    root_0 = (Object)adaptor.nil();

                    QUOTEDSTRING219=(Token)input.LT(1);
                    match(input,QUOTEDSTRING,FOLLOW_QUOTEDSTRING_in_l_expr1940); if (failed) return retval;
                    if ( backtracking==0 ) {
                    QUOTEDSTRING219_tree = (Object)adaptor.create(QUOTEDSTRING219);
                    adaptor.addChild(root_0, QUOTEDSTRING219_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end l_expr

    public static class unary_op_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start unary_op
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:1: unary_op : ( NOTNULL | ( IS NULL ) | ISNULL | ( IS NOT NULL ) );
    public final unary_op_return unary_op() throws RecognitionException {
        unary_op_return retval = new unary_op_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOTNULL220=null;
        Token IS221=null;
        Token NULL222=null;
        Token ISNULL223=null;
        Token IS224=null;
        Token NOT225=null;
        Token NULL226=null;

        Object NOTNULL220_tree=null;
        Object IS221_tree=null;
        Object NULL222_tree=null;
        Object ISNULL223_tree=null;
        Object IS224_tree=null;
        Object NOT225_tree=null;
        Object NULL226_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:11: ( NOTNULL | ( IS NULL ) | ISNULL | ( IS NOT NULL ) )
            int alt56=4;
            switch ( input.LA(1) ) {
            case NOTNULL:
                {
                alt56=1;
                }
                break;
            case IS:
                {
                int LA56_2 = input.LA(2);

                if ( (LA56_2==NOT) ) {
                    alt56=4;
                }
                else if ( (LA56_2==NULL) ) {
                    alt56=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("314:1: unary_op : ( NOTNULL | ( IS NULL ) | ISNULL | ( IS NOT NULL ) );", 56, 2, input);

                    throw nvae;
                }
                }
                break;
            case ISNULL:
                {
                alt56=3;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("314:1: unary_op : ( NOTNULL | ( IS NULL ) | ISNULL | ( IS NOT NULL ) );", 56, 0, input);

                throw nvae;
            }

            switch (alt56) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:13: NOTNULL
                    {
                    root_0 = (Object)adaptor.nil();

                    NOTNULL220=(Token)input.LT(1);
                    match(input,NOTNULL,FOLLOW_NOTNULL_in_unary_op1956); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NOTNULL220_tree = (Object)adaptor.create(NOTNULL220);
                    adaptor.addChild(root_0, NOTNULL220_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:23: ( IS NULL )
                    {
                    root_0 = (Object)adaptor.nil();

                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:23: ( IS NULL )
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:24: IS NULL
                    {
                    IS221=(Token)input.LT(1);
                    match(input,IS,FOLLOW_IS_in_unary_op1961); if (failed) return retval;
                    if ( backtracking==0 ) {
                    IS221_tree = (Object)adaptor.create(IS221);
                    adaptor.addChild(root_0, IS221_tree);
                    }
                    NULL222=(Token)input.LT(1);
                    match(input,NULL,FOLLOW_NULL_in_unary_op1963); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NULL222_tree = (Object)adaptor.create(NULL222);
                    adaptor.addChild(root_0, NULL222_tree);
                    }

                    }


                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:35: ISNULL
                    {
                    root_0 = (Object)adaptor.nil();

                    ISNULL223=(Token)input.LT(1);
                    match(input,ISNULL,FOLLOW_ISNULL_in_unary_op1968); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ISNULL223_tree = (Object)adaptor.create(ISNULL223);
                    adaptor.addChild(root_0, ISNULL223_tree);
                    }

                    }
                    break;
                case 4 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:44: ( IS NOT NULL )
                    {
                    root_0 = (Object)adaptor.nil();

                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:44: ( IS NOT NULL )
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:314:45: IS NOT NULL
                    {
                    IS224=(Token)input.LT(1);
                    match(input,IS,FOLLOW_IS_in_unary_op1973); if (failed) return retval;
                    if ( backtracking==0 ) {
                    IS224_tree = (Object)adaptor.create(IS224);
                    adaptor.addChild(root_0, IS224_tree);
                    }
                    NOT225=(Token)input.LT(1);
                    match(input,NOT,FOLLOW_NOT_in_unary_op1975); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NOT225_tree = (Object)adaptor.create(NOT225);
                    adaptor.addChild(root_0, NOT225_tree);
                    }
                    NULL226=(Token)input.LT(1);
                    match(input,NULL,FOLLOW_NULL_in_unary_op1977); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NULL226_tree = (Object)adaptor.create(NULL226);
                    adaptor.addChild(root_0, NULL226_tree);
                    }

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end unary_op

    public static class binary_op_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start binary_op
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:317:1: binary_op : ( GE | LE );
    public final binary_op_return binary_op() throws RecognitionException {
        binary_op_return retval = new binary_op_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set227=null;

        Object set227_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:317:12: ( GE | LE )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:
            {
            root_0 = (Object)adaptor.nil();

            set227=(Token)input.LT(1);
            if ( (input.LA(1)>=GE && input.LA(1)<=LE) ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set227));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_binary_op0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end binary_op

    public static class where_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start where_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:325:1: where_clause : ( WHERE a_expr -> ^( WHERE[\"WHERE\"] a_expr ) | );
    public final where_clause_return where_clause() throws RecognitionException {
        where_clause_return retval = new where_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token WHERE228=null;
        a_expr_return a_expr229 = null;


        Object WHERE228_tree=null;
        RewriteRuleTokenStream stream_WHERE=new RewriteRuleTokenStream(adaptor,"token WHERE");
        RewriteRuleSubtreeStream stream_a_expr=new RewriteRuleSubtreeStream(adaptor,"rule a_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:325:15: ( WHERE a_expr -> ^( WHERE[\"WHERE\"] a_expr ) | )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==WHERE) ) {
                alt57=1;
            }
            else if ( (LA57_0==EOF||LA57_0==RPAREN||LA57_0==GROUP||(LA57_0>=HAVING && LA57_0<=ORDER)||LA57_0==88) ) {
                alt57=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("325:1: where_clause : ( WHERE a_expr -> ^( WHERE[\"WHERE\"] a_expr ) | );", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:325:17: WHERE a_expr
                    {
                    WHERE228=(Token)input.LT(1);
                    match(input,WHERE,FOLLOW_WHERE_in_where_clause2012); if (failed) return retval;
                    if ( backtracking==0 ) stream_WHERE.add(WHERE228);

                    pushFollow(FOLLOW_a_expr_in_where_clause2014);
                    a_expr229=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_a_expr.add(a_expr229.getTree());

                    // AST REWRITE
                    // elements: WHERE, a_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 325:30: -> ^( WHERE[\"WHERE\"] a_expr )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:325:33: ^( WHERE[\"WHERE\"] a_expr )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(adaptor.create(WHERE,"WHERE"), root_1);

                        adaptor.addChild(root_1, stream_a_expr.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:327:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end where_clause

    public static class groupby_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start groupby_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:329:1: groupby_clause : ( GROUP BY expr_list -> ^( GROUP BY expr_list ) | );
    public final groupby_clause_return groupby_clause() throws RecognitionException {
        groupby_clause_return retval = new groupby_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token GROUP230=null;
        Token BY231=null;
        expr_list_return expr_list232 = null;


        Object GROUP230_tree=null;
        Object BY231_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleTokenStream stream_GROUP=new RewriteRuleTokenStream(adaptor,"token GROUP");
        RewriteRuleSubtreeStream stream_expr_list=new RewriteRuleSubtreeStream(adaptor,"rule expr_list");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:329:17: ( GROUP BY expr_list -> ^( GROUP BY expr_list ) | )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==GROUP) ) {
                alt58=1;
            }
            else if ( (LA58_0==EOF||LA58_0==RPAREN||(LA58_0>=HAVING && LA58_0<=ORDER)||LA58_0==88) ) {
                alt58=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("329:1: groupby_clause : ( GROUP BY expr_list -> ^( GROUP BY expr_list ) | );", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:329:19: GROUP BY expr_list
                    {
                    GROUP230=(Token)input.LT(1);
                    match(input,GROUP,FOLLOW_GROUP_in_groupby_clause2040); if (failed) return retval;
                    if ( backtracking==0 ) stream_GROUP.add(GROUP230);

                    BY231=(Token)input.LT(1);
                    match(input,BY,FOLLOW_BY_in_groupby_clause2042); if (failed) return retval;
                    if ( backtracking==0 ) stream_BY.add(BY231);

                    pushFollow(FOLLOW_expr_list_in_groupby_clause2044);
                    expr_list232=expr_list();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_expr_list.add(expr_list232.getTree());

                    // AST REWRITE
                    // elements: BY, GROUP, expr_list
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 329:39: -> ^( GROUP BY expr_list )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:329:42: ^( GROUP BY expr_list )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(stream_GROUP.next(), root_1);

                        adaptor.addChild(root_1, stream_BY.next());
                        adaptor.addChild(root_1, stream_expr_list.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:331:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end groupby_clause

    public static class having_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start having_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:333:1: having_clause : ( HAVING a_expr -> ^( HAVING a_expr ) | );
    public final having_clause_return having_clause() throws RecognitionException {
        having_clause_return retval = new having_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token HAVING233=null;
        a_expr_return a_expr234 = null;


        Object HAVING233_tree=null;
        RewriteRuleTokenStream stream_HAVING=new RewriteRuleTokenStream(adaptor,"token HAVING");
        RewriteRuleSubtreeStream stream_a_expr=new RewriteRuleSubtreeStream(adaptor,"rule a_expr");
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:333:16: ( HAVING a_expr -> ^( HAVING a_expr ) | )
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==HAVING) ) {
                alt59=1;
            }
            else if ( (LA59_0==EOF||LA59_0==RPAREN||LA59_0==ORDER||LA59_0==88) ) {
                alt59=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("333:1: having_clause : ( HAVING a_expr -> ^( HAVING a_expr ) | );", 59, 0, input);

                throw nvae;
            }
            switch (alt59) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:333:18: HAVING a_expr
                    {
                    HAVING233=(Token)input.LT(1);
                    match(input,HAVING,FOLLOW_HAVING_in_having_clause2073); if (failed) return retval;
                    if ( backtracking==0 ) stream_HAVING.add(HAVING233);

                    pushFollow(FOLLOW_a_expr_in_having_clause2075);
                    a_expr234=a_expr();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_a_expr.add(a_expr234.getTree());

                    // AST REWRITE
                    // elements: HAVING, a_expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    if ( backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 333:32: -> ^( HAVING a_expr )
                    {
                        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:333:35: ^( HAVING a_expr )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(stream_HAVING.next(), root_1);

                        adaptor.addChild(root_1, stream_a_expr.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    }

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:335:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end having_clause

    public static class orderby_clause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start orderby_clause
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:337:1: orderby_clause : ( ORDER BY ( DESC | ASC )? | );
    public final orderby_clause_return orderby_clause() throws RecognitionException {
        orderby_clause_return retval = new orderby_clause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER235=null;
        Token BY236=null;
        Token set237=null;

        Object ORDER235_tree=null;
        Object BY236_tree=null;
        Object set237_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:337:17: ( ORDER BY ( DESC | ASC )? | )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==ORDER) ) {
                alt61=1;
            }
            else if ( (LA61_0==EOF||LA61_0==RPAREN||LA61_0==88) ) {
                alt61=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("337:1: orderby_clause : ( ORDER BY ( DESC | ASC )? | );", 61, 0, input);

                throw nvae;
            }
            switch (alt61) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:337:19: ORDER BY ( DESC | ASC )?
                    {
                    root_0 = (Object)adaptor.nil();

                    ORDER235=(Token)input.LT(1);
                    match(input,ORDER,FOLLOW_ORDER_in_orderby_clause2100); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ORDER235_tree = (Object)adaptor.create(ORDER235);
                    adaptor.addChild(root_0, ORDER235_tree);
                    }
                    BY236=(Token)input.LT(1);
                    match(input,BY,FOLLOW_BY_in_orderby_clause2102); if (failed) return retval;
                    if ( backtracking==0 ) {
                    BY236_tree = (Object)adaptor.create(BY236);
                    adaptor.addChild(root_0, BY236_tree);
                    }
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:337:28: ( DESC | ASC )?
                    int alt60=2;
                    int LA60_0 = input.LA(1);

                    if ( ((LA60_0>=DESC && LA60_0<=ASC)) ) {
                        alt60=1;
                    }
                    switch (alt60) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:
                            {
                            set237=(Token)input.LT(1);
                            if ( (input.LA(1)>=DESC && input.LA(1)<=ASC) ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set237));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_orderby_clause2104);    throw mse;
                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:339:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end orderby_clause

    public static class opt_limit_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start opt_limit
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:341:1: opt_limit : ( limit | );
    public final opt_limit_return opt_limit() throws RecognitionException {
        opt_limit_return retval = new opt_limit_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        limit_return limit238 = null;



        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:341:13: ( limit | )
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==LIMIT||LA62_0==OFFSET) ) {
                alt62=1;
            }
            else if ( (LA62_0==EOF) ) {
                alt62=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("341:1: opt_limit : ( limit | );", 62, 0, input);

                throw nvae;
            }
            switch (alt62) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:341:15: limit
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_limit_in_opt_limit2128);
                    limit238=limit();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, limit238.getTree());

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:343:3: 
                    {
                    root_0 = (Object)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end opt_limit

    public static class limit_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start limit
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:345:1: limit : ( LIMIT NUMBER ( OFFSET NUMBER )? | OFFSET NUMBER ( LIMIT NUMBER )? | LIMIT NUMBER ',' NUMBER );
    public final limit_return limit() throws RecognitionException {
        limit_return retval = new limit_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LIMIT239=null;
        Token NUMBER240=null;
        Token OFFSET241=null;
        Token NUMBER242=null;
        Token OFFSET243=null;
        Token NUMBER244=null;
        Token LIMIT245=null;
        Token NUMBER246=null;
        Token LIMIT247=null;
        Token NUMBER248=null;
        Token char_literal249=null;
        Token NUMBER250=null;

        Object LIMIT239_tree=null;
        Object NUMBER240_tree=null;
        Object OFFSET241_tree=null;
        Object NUMBER242_tree=null;
        Object OFFSET243_tree=null;
        Object NUMBER244_tree=null;
        Object LIMIT245_tree=null;
        Object NUMBER246_tree=null;
        Object LIMIT247_tree=null;
        Object NUMBER248_tree=null;
        Object char_literal249_tree=null;
        Object NUMBER250_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:345:9: ( LIMIT NUMBER ( OFFSET NUMBER )? | OFFSET NUMBER ( LIMIT NUMBER )? | LIMIT NUMBER ',' NUMBER )
            int alt65=3;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==LIMIT) ) {
                int LA65_1 = input.LA(2);

                if ( (LA65_1==NUMBER) ) {
                    int LA65_3 = input.LA(3);

                    if ( (LA65_3==89) ) {
                        alt65=3;
                    }
                    else if ( (LA65_3==EOF||LA65_3==OFFSET) ) {
                        alt65=1;
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("345:1: limit : ( LIMIT NUMBER ( OFFSET NUMBER )? | OFFSET NUMBER ( LIMIT NUMBER )? | LIMIT NUMBER ',' NUMBER );", 65, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("345:1: limit : ( LIMIT NUMBER ( OFFSET NUMBER )? | OFFSET NUMBER ( LIMIT NUMBER )? | LIMIT NUMBER ',' NUMBER );", 65, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA65_0==OFFSET) ) {
                alt65=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("345:1: limit : ( LIMIT NUMBER ( OFFSET NUMBER )? | OFFSET NUMBER ( LIMIT NUMBER )? | LIMIT NUMBER ',' NUMBER );", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:345:11: LIMIT NUMBER ( OFFSET NUMBER )?
                    {
                    root_0 = (Object)adaptor.nil();

                    LIMIT239=(Token)input.LT(1);
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit2148); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LIMIT239_tree = (Object)adaptor.create(LIMIT239);
                    adaptor.addChild(root_0, LIMIT239_tree);
                    }
                    NUMBER240=(Token)input.LT(1);
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit2150); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NUMBER240_tree = (Object)adaptor.create(NUMBER240);
                    adaptor.addChild(root_0, NUMBER240_tree);
                    }
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:345:24: ( OFFSET NUMBER )?
                    int alt63=2;
                    int LA63_0 = input.LA(1);

                    if ( (LA63_0==OFFSET) ) {
                        alt63=1;
                    }
                    switch (alt63) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:345:25: OFFSET NUMBER
                            {
                            OFFSET241=(Token)input.LT(1);
                            match(input,OFFSET,FOLLOW_OFFSET_in_limit2153); if (failed) return retval;
                            if ( backtracking==0 ) {
                            OFFSET241_tree = (Object)adaptor.create(OFFSET241);
                            adaptor.addChild(root_0, OFFSET241_tree);
                            }
                            NUMBER242=(Token)input.LT(1);
                            match(input,NUMBER,FOLLOW_NUMBER_in_limit2155); if (failed) return retval;
                            if ( backtracking==0 ) {
                            NUMBER242_tree = (Object)adaptor.create(NUMBER242);
                            adaptor.addChild(root_0, NUMBER242_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:346:5: OFFSET NUMBER ( LIMIT NUMBER )?
                    {
                    root_0 = (Object)adaptor.nil();

                    OFFSET243=(Token)input.LT(1);
                    match(input,OFFSET,FOLLOW_OFFSET_in_limit2164); if (failed) return retval;
                    if ( backtracking==0 ) {
                    OFFSET243_tree = (Object)adaptor.create(OFFSET243);
                    adaptor.addChild(root_0, OFFSET243_tree);
                    }
                    NUMBER244=(Token)input.LT(1);
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit2166); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NUMBER244_tree = (Object)adaptor.create(NUMBER244);
                    adaptor.addChild(root_0, NUMBER244_tree);
                    }
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:346:19: ( LIMIT NUMBER )?
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==LIMIT) ) {
                        alt64=1;
                    }
                    switch (alt64) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:346:20: LIMIT NUMBER
                            {
                            LIMIT245=(Token)input.LT(1);
                            match(input,LIMIT,FOLLOW_LIMIT_in_limit2169); if (failed) return retval;
                            if ( backtracking==0 ) {
                            LIMIT245_tree = (Object)adaptor.create(LIMIT245);
                            adaptor.addChild(root_0, LIMIT245_tree);
                            }
                            NUMBER246=(Token)input.LT(1);
                            match(input,NUMBER,FOLLOW_NUMBER_in_limit2171); if (failed) return retval;
                            if ( backtracking==0 ) {
                            NUMBER246_tree = (Object)adaptor.create(NUMBER246);
                            adaptor.addChild(root_0, NUMBER246_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:347:5: LIMIT NUMBER ',' NUMBER
                    {
                    root_0 = (Object)adaptor.nil();

                    LIMIT247=(Token)input.LT(1);
                    match(input,LIMIT,FOLLOW_LIMIT_in_limit2179); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LIMIT247_tree = (Object)adaptor.create(LIMIT247);
                    adaptor.addChild(root_0, LIMIT247_tree);
                    }
                    NUMBER248=(Token)input.LT(1);
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit2181); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NUMBER248_tree = (Object)adaptor.create(NUMBER248);
                    adaptor.addChild(root_0, NUMBER248_tree);
                    }
                    char_literal249=(Token)input.LT(1);
                    match(input,89,FOLLOW_89_in_limit2183); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal249_tree = (Object)adaptor.create(char_literal249);
                    adaptor.addChild(root_0, char_literal249_tree);
                    }
                    NUMBER250=(Token)input.LT(1);
                    match(input,NUMBER,FOLLOW_NUMBER_in_limit2185); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NUMBER250_tree = (Object)adaptor.create(NUMBER250);
                    adaptor.addChild(root_0, NUMBER250_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end limit

    public static class id_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start id
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:350:1: id : ID ;
    public final id_return id() throws RecognitionException {
        id_return retval = new id_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID251=null;

        Object ID251_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:350:6: ( ID )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:350:8: ID
            {
            root_0 = (Object)adaptor.nil();

            ID251=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_id2203); if (failed) return retval;
            if ( backtracking==0 ) {
            ID251_tree = (Object)adaptor.create(ID251);
            adaptor.addChild(root_0, ID251_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end id

    public static class factor_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start factor
    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:353:1: factor : NUMBER ;
    public final factor_return factor() throws RecognitionException {
        factor_return retval = new factor_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NUMBER252=null;

        Object NUMBER252_tree=null;

        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:353:9: ( NUMBER )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:353:11: NUMBER
            {
            root_0 = (Object)adaptor.nil();

            NUMBER252=(Token)input.LT(1);
            match(input,NUMBER,FOLLOW_NUMBER_in_factor2217); if (failed) return retval;
            if ( backtracking==0 ) {
            NUMBER252_tree = (Object)adaptor.create(NUMBER252);
            adaptor.addChild(root_0, NUMBER252_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (Object)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end factor

    // $ANTLR start synpred1
    public final void synpred1_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:16: ( AS id )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:133:17: AS id
        {
        match(input,AS,FOLLOW_AS_in_synpred1591); if (failed) return ;
        pushFollow(FOLLOW_id_in_synpred1593);
        id();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred1

    // $ANTLR start synpred2
    public final void synpred2_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:134:5: ( id )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:134:6: id
        {
        pushFollow(FOLLOW_id_in_synpred2615);
        id();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred2

    // $ANTLR start synpred3
    public final void synpred3_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:14: ( func_expr ( alias_clause )? )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:15: func_expr ( alias_clause )?
        {
        pushFollow(FOLLOW_func_expr_in_synpred3982);
        func_expr();
        _fsp--;
        if (failed) return ;
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:25: ( alias_clause )?
        int alt66=2;
        int LA66_0 = input.LA(1);

        if ( (LA66_0==AS||LA66_0==ID) ) {
            alt66=1;
        }
        switch (alt66) {
            case 1 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:199:25: alias_clause
                {
                pushFollow(FOLLOW_alias_clause_in_synpred3984);
                alias_clause();
                _fsp--;
                if (failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred3

    // $ANTLR start synpred4
    public final void synpred4_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:5: ( LPAREN inner_select RPAREN ( alias_clause )? )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:6: LPAREN inner_select RPAREN ( alias_clause )?
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred41031); if (failed) return ;
        pushFollow(FOLLOW_inner_select_in_synpred41033);
        inner_select();
        _fsp--;
        if (failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred41035); if (failed) return ;
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:33: ( alias_clause )?
        int alt67=2;
        int LA67_0 = input.LA(1);

        if ( (LA67_0==AS||LA67_0==ID) ) {
            alt67=1;
        }
        switch (alt67) {
            case 1 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:201:33: alias_clause
                {
                pushFollow(FOLLOW_alias_clause_in_synpred41037);
                alias_clause();
                _fsp--;
                if (failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred4

    // $ANTLR start synpred5
    public final void synpred5_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:234:14: ( alias_clause )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:234:15: alias_clause
        {
        pushFollow(FOLLOW_alias_clause_in_synpred51266);
        alias_clause();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred5

    // $ANTLR start synpred6
    public final void synpred6_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:16: ( id )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:238:17: id
        {
        pushFollow(FOLLOW_id_in_synpred61298);
        id();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred6

    // $ANTLR start synpred7
    public final void synpred7_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:11: ( func_expr )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:304:12: func_expr
        {
        pushFollow(FOLLOW_func_expr_in_synpred71859);
        func_expr();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred7

    // $ANTLR start synpred8
    public final void synpred8_fragment() throws RecognitionException {   
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:307:5: ( select_with_parens )
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:307:6: select_with_parens
        {
        pushFollow(FOLLOW_select_with_parens_in_synpred81905);
        select_with_parens();
        _fsp--;
        if (failed) return ;

        }
    }
    // $ANTLR end synpred8

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
    public final boolean synpred7() {
        backtracking++;
        int start = input.mark();
        try {
            synpred7_fragment(); // can never throw exception
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
    public final boolean synpred8() {
        backtracking++;
        int start = input.mark();
        try {
            synpred8_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }


    protected DFA20 dfa20 = new DFA20(this);
    static final String DFA20_eotS =
        "\32\uffff";
    static final String DFA20_eofS =
        "\1\uffff\2\4\4\uffff\2\4\3\uffff\5\4\4\uffff\3\4\2\uffff";
    static final String DFA20_minS =
        "\1\20\2\6\1\20\1\uffff\2\6\2\20\1\117\1\uffff\1\52\2\12\2\6\1\20"+
        "\2\12\2\6\1\20\4\12";
    static final String DFA20_maxS =
        "\1\117\2\131\1\117\1\uffff\2\117\2\131\1\117\1\uffff\1\117\5\131"+
        "\2\21\2\117\3\131\2\21";
    static final String DFA20_acceptS =
        "\4\uffff\1\2\5\uffff\1\1\17\uffff";
    static final String DFA20_specialS =
        "\32\uffff}>";
    static final String[] DFA20_transitionS = {
            "\1\4\31\uffff\2\2\12\uffff\1\3\30\uffff\1\1",
            "\1\7\3\uffff\1\6\5\uffff\1\5\1\4\21\uffff\1\11\11\uffff\7\12"+
            "\21\uffff\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\7\3\uffff\1\6\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12"+
            "\21\uffff\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\13\31\uffff\2\15\43\uffff\1\14",
            "",
            "\1\4\11\uffff\1\4\17\uffff\1\4\1\uffff\1\4\7\uffff\2\4\1\uffff"+
            "\7\12\5\uffff\1\4\10\uffff\3\4\10\uffff\1\4\1\uffff\1\4",
            "\1\16\110\uffff\1\17",
            "\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff\2\4\1\uffff\2"+
            "\4\5\uffff\1\10\10\uffff\2\4",
            "\1\12\1\4\33\uffff\7\12\21\uffff\2\4\1\uffff\2\4\16\uffff\2"+
            "\4",
            "\1\20",
            "",
            "\2\22\43\uffff\1\21",
            "\1\23\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff"+
            "\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\23\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff"+
            "\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\7\3\uffff\1\6\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12"+
            "\21\uffff\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\7\3\uffff\1\6\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12"+
            "\21\uffff\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\12\1\4\33\uffff\7\12\21\uffff\2\4\1\uffff\2\4\16\uffff\2"+
            "\4",
            "\1\24\6\uffff\1\25",
            "\1\24\6\uffff\1\25",
            "\1\26\110\uffff\1\27",
            "\1\30\110\uffff\1\31",
            "\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff\2\4\1\uffff\2"+
            "\4\5\uffff\1\10\10\uffff\2\4",
            "\1\23\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff"+
            "\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\23\5\uffff\1\12\1\4\21\uffff\1\11\11\uffff\7\12\21\uffff"+
            "\2\4\1\uffff\2\4\5\uffff\1\10\10\uffff\2\4",
            "\1\24\6\uffff\1\25",
            "\1\24\6\uffff\1\25"
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "189:1: from_list : ( table_joins | table_refs );";
        }
    }
 

    public static final BitSet FOLLOW_stmtmulti_in_stmtblock260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_select_in_stmtmulti272 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_stmtmulti276 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_simple_select_in_stmtmulti278 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_stmtmulti283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_select_with_parens297 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_simple_select_in_select_with_parens299 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_select_with_parens301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_simple_select312 = new BitSet(new long[]{0x02000C0500010040L,0x000000000000A01CL});
    public static final BitSet FOLLOW_opt_distinct_in_simple_select314 = new BitSet(new long[]{0x02000C0000010040L,0x000000000000A01CL});
    public static final BitSet FOLLOW_target_list_in_simple_select316 = new BitSet(new long[]{0x0000101000000002L,0x0000000000000360L});
    public static final BitSet FOLLOW_into_clause_in_simple_select318 = new BitSet(new long[]{0x0000100000000002L,0x0000000000000360L});
    public static final BitSet FOLLOW_from_clause_in_simple_select320 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000360L});
    public static final BitSet FOLLOW_where_clause_in_simple_select322 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000340L});
    public static final BitSet FOLLOW_groupby_clause_in_simple_select324 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_having_clause_in_simple_select326 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_orderby_clause_in_simple_select328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_values_clause_in_simple_select361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_values_clause376 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_values_clause378 = new BitSet(new long[]{0x02000C0080010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_values_expr_list_in_values_clause380 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_values_clause382 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_values_clause385 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_values_clause387 = new BitSet(new long[]{0x02000C0080010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_values_expr_list_in_values_clause389 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_values_clause391 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_values_expr_in_values_expr_list407 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_values_expr_list411 = new BitSet(new long[]{0x02000C0080010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_values_expr_in_values_expr_list413 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_a_expr_in_values_expr426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_values_expr432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_opt_distinct447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_opt_distinct454 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_ON_in_opt_distinct456 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_opt_distinct458 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_expr_list_in_opt_distinct460 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_opt_distinct462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_opt_distinct469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_a_expr_in_expr_list493 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_expr_list497 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_a_expr_in_expr_list499 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_target_in_target_list524 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_target_list527 = new BitSet(new long[]{0x02000C0000010040L,0x000000000000A01CL});
    public static final BitSet FOLLOW_target_in_target_list529 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_a_expr_in_target552 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_target_alias_in_target554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_target571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_target_alias598 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_id_in_target_alias600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_target_alias620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTO_in_into_clause642 = new BitSet(new long[]{0x00000FE000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_temp_table_name_in_into_clause644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name664 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name666 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name676 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name678 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_in_opt_temp_table_name688 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name690 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name692 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_in_opt_temp_table_name702 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name704 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name706 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GLOBAL_in_opt_temp_table_name715 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_TEMPORARY_in_opt_temp_table_name717 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name719 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GLOBAL_in_opt_temp_table_name729 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_TEMP_in_opt_temp_table_name731 = new BitSet(new long[]{0x00000E0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_opt_table_in_opt_temp_table_name733 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TABLE_in_opt_temp_table_name743 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualified_name_in_opt_temp_table_name753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TABLE_in_opt_table769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_name_in_qualified_name786 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_indirection_in_qualified_name788 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_id_in_relation_name811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_special_rule_relation_in_relation_name819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_special_rule_relation0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_indirection854 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_id_in_indirection856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_indirection869 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_STAR_in_indirection871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause895 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_from_list_in_from_clause897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_joins_in_from_list923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_refs_in_from_list929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_ref_in_table_refs941 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_table_refs944 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_table_refs946 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_relation_expr_in_table_joins961 = new BitSet(new long[]{0x000FE00800010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_table_joins963 = new BitSet(new long[]{0x000FE00000010000L});
    public static final BitSet FOLLOW_table_join_in_table_joins966 = new BitSet(new long[]{0x000FE00000010002L});
    public static final BitSet FOLLOW_func_expr_in_table_ref990 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_table_ref992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_expr_in_table_ref1010 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_table_ref1012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_table_ref1044 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_inner_select_in_table_ref1046 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_table_ref1048 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_table_ref1050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_select_in_inner_select1066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joined_table_in_table_join1079 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_table_join1094 = new BitSet(new long[]{0x000FE00000000000L});
    public static final BitSet FOLLOW_joined_table_in_table_join1096 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_table_join1098 = new BitSet(new long[]{0x0000000800000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_table_join1100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CROSS_in_joined_table1113 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1115 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table1117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_type_in_joined_table1123 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1125 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table1127 = new BitSet(new long[]{0x0020000200000000L});
    public static final BitSet FOLLOW_join_qual_in_joined_table1129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1135 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table1137 = new BitSet(new long[]{0x0020000200000000L});
    public static final BitSet FOLLOW_join_qual_in_joined_table1139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NATURAL_in_joined_table1145 = new BitSet(new long[]{0x000F000000000000L});
    public static final BitSet FOLLOW_join_type_in_joined_table1147 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1149 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NATURAL_in_joined_table1157 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1159 = new BitSet(new long[]{0x00400C0000010000L,0x0000000000008000L});
    public static final BitSet FOLLOW_table_ref_in_joined_table1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FULL_in_join_type1176 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_join_type1185 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHT_in_join_type1194 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_join_outer_in_join_type1196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INNER_in_join_type1203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_outer1219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_join_qual1236 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_join_qual1238 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_name_list_in_join_qual1240 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_join_qual1242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_qual1249 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_a_expr_in_join_qual1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alias_clause_in_opt_alias1271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_alias_clause1303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_alias_clause1317 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_id_in_alias_clause1319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_name_list1344 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_name_list1347 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_id_in_name_list1349 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1369 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_STAR_in_relation_expr1371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONLY_in_relation_expr1379 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONLY_in_relation_expr1389 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_relation_expr1391 = new BitSet(new long[]{0x00000C0000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_qualified_name_in_relation_expr1393 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_relation_expr1395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_name_in_func_expr1411 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_func_expr1413 = new BitSet(new long[]{0x02000C0500010040L,0x000000000000A01CL});
    public static final BitSet FOLLOW_func_args_in_func_expr1415 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_func_expr1417 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_func_args1438 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_expr_list_in_func_args1447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_func_args1453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_func_name1469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_b_expr_in_a_expr1485 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_OR_in_a_expr1491 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_b_expr_in_a_expr1494 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_c_expr_in_b_expr1535 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_AND_in_b_expr1541 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_c_expr_in_b_expr1544 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_NOT_in_c_expr1588 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_d_expr_in_c_expr1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_e_expr_in_d_expr1610 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_EQ_in_d_expr1613 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_e_expr_in_d_expr1617 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_f_expr_in_e_expr1635 = new BitSet(new long[]{0x0000000000000302L});
    public static final BitSet FOLLOW_GT_in_e_expr1639 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_LT_in_e_expr1645 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_f_expr_in_e_expr1650 = new BitSet(new long[]{0x0000000000000302L});
    public static final BitSet FOLLOW_g_expr_in_f_expr1666 = new BitSet(new long[]{0x1C00000000000002L});
    public static final BitSet FOLLOW_LIKE_in_f_expr1670 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_ILIKE_in_f_expr1676 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_SIMILAR_in_f_expr1682 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_TO_in_f_expr1684 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_g_expr_in_f_expr1689 = new BitSet(new long[]{0x1C00000000000002L});
    public static final BitSet FOLLOW_h_expr_in_g_expr1706 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_binary_op_in_g_expr1709 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_h_expr_in_g_expr1712 = new BitSet(new long[]{0x000000000000C002L});
    public static final BitSet FOLLOW_i_expr_in_h_expr1728 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_NOTNULL_in_h_expr1732 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_h_expr1737 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_NULL_in_h_expr1739 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_ISNULL_in_h_expr1744 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_h_expr1749 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_NOT_in_h_expr1751 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_NULL_in_h_expr1753 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_j_expr_in_i_expr1771 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_PLUS_in_i_expr1775 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_MINUS_in_i_expr1780 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_j_expr_in_i_expr1785 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_k_expr_in_j_expr1802 = new BitSet(new long[]{0x00000000000008C2L});
    public static final BitSet FOLLOW_STAR_in_j_expr1806 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_DIV_in_j_expr1811 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_PCT_in_j_expr1816 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_k_expr_in_j_expr1821 = new BitSet(new long[]{0x00000000000008C2L});
    public static final BitSet FOLLOW_l_expr_in_k_expr1839 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_HAT_in_k_expr1842 = new BitSet(new long[]{0x00000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_l_expr_in_k_expr1844 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_func_expr_in_l_expr1864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualified_name_in_l_expr1878 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_l_expr1884 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_a_expr_in_l_expr1886 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_l_expr1888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_l_expr1910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_l_expr1916 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_select_with_parens_in_l_expr1918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_l_expr1925 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_select_with_parens_in_l_expr1927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_factor_in_l_expr1934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTEDSTRING_in_l_expr1940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTNULL_in_unary_op1956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_unary_op1961 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_NULL_in_unary_op1963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ISNULL_in_unary_op1968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_unary_op1973 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_NOT_in_unary_op1975 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_NULL_in_unary_op1977 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_binary_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause2012 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_a_expr_in_where_clause2014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GROUP_in_groupby_clause2040 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_BY_in_groupby_clause2042 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_expr_list_in_groupby_clause2044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HAVING_in_having_clause2073 = new BitSet(new long[]{0x02000C0000010000L,0x000000000000A01CL});
    public static final BitSet FOLLOW_a_expr_in_having_clause2075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORDER_in_orderby_clause2100 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_BY_in_orderby_clause2102 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000C00L});
    public static final BitSet FOLLOW_set_in_orderby_clause2104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_limit_in_opt_limit2128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit2148 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2150 = new BitSet(new long[]{0x0000000000000002L,0x0000000000004000L});
    public static final BitSet FOLLOW_OFFSET_in_limit2153 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OFFSET_in_limit2164 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2166 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_LIMIT_in_limit2169 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit2179 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2181 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_limit2183 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_limit2185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_id2203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_factor2217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AS_in_synpred1591 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_id_in_synpred1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_synpred2615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_expr_in_synpred3982 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_synpred3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred41031 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_inner_select_in_synpred41033 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_synpred41035 = new BitSet(new long[]{0x0000000800000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_alias_clause_in_synpred41037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alias_clause_in_synpred51266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_synpred61298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_func_expr_in_synpred71859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_with_parens_in_synpred81905 = new BitSet(new long[]{0x0000000000000002L});

}