// $ANTLR 3.0.1 /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g 2008-10-27 12:13:48

package ca.sqlpower.wabit.sql.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SQLANTLRLexer extends Lexer {
    public static final int EXISTS=66;
    public static final int MINUS=5;
    public static final int AS=35;
    public static final int USING=53;
    public static final int ARRAY=67;
    public static final int INTO=36;
    public static final int EXPR=22;
    public static final int TARGET=26;
    public static final int NUMBER=77;
    public static final int VARIADIC=81;
    public static final int TARGETS=27;
    public static final int VALUES=30;
    public static final int NATURAL=47;
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
    public static final int PLUS=4;
    public static final int LEFT=49;
    public static final int ML_COMMENT=87;
    public static final int DIGIT=84;
    public static final int LEN=80;
    public static final int JOIN=46;
    public static final int SL_COMMENT=86;
    public static final int TEMPORARY=37;
    public static final int QUOTEDSTRING=68;
    public static final int OUTER=52;
    public static final int NULL_ARG=28;
    public static final int FROM=44;
    public static final int ID=79;
    public static final int DISTINCT=32;
    public static final int LETTER=85;
    public static final int SELECT_START=25;
    public static final int IS=63;
    public static final int NEW=43;
    public static final int EQ=12;
    public static final int ONLY=54;
    public static final int OFFSET=78;
    public static final int NOTNULL=62;
    public static final int LT=9;
    public static final int GT=8;
    public static final int RELATION=19;
    public static final int T88=88;
    public static final int ASC=75;
    public static final int LIKE=58;
    public static final int INDIRECTION=21;
    public static final int WHITESPACE=82;
    public static final int GE=14;
    public static final int TABLE=41;
    public static final int FROM_START=24;
    public static final int ILIKE=59;
    public static final int HAT=13;
    public static final int TEMP=38;
    public static final int T89=89;
    public static final int EOF=-1;
    public static final int CROSS=45;
    public static final int NULL=64;
    public static final int ISNULL=65;
    public static final int Tokens=90;
    public static final int DEFAULT=31;
    public static final int DIV=7;
    public static final int HAVING=72;
    public static final int ALL=34;
    public static final int STAR=6;
    public static final int NOT=57;
    public static final int LIMIT=76;
    public static final int WHERE=69;
    public SQLANTLRLexer() {;} 
    public SQLANTLRLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "/Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g"; }

    // $ANTLR start PLUS
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:6:6: ( '+' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:6:8: '+'
            {
            match('+'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PLUS

    // $ANTLR start MINUS
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:7:7: ( '-' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:7:9: '-'
            {
            match('-'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MINUS

    // $ANTLR start STAR
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:8:6: ( '*' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:8:8: '*'
            {
            match('*'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STAR

    // $ANTLR start DIV
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:9:5: ( '/' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:9:7: '/'
            {
            match('/'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DIV

    // $ANTLR start GT
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:10:4: ( '>' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:10:6: '>'
            {
            match('>'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GT

    // $ANTLR start LT
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:11:4: ( '<' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:11:6: '<'
            {
            match('<'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LT

    // $ANTLR start DOT
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:12:5: ( '.' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:12:7: '.'
            {
            match('.'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DOT

    // $ANTLR start PCT
    public final void mPCT() throws RecognitionException {
        try {
            int _type = PCT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:13:5: ( '%' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:13:7: '%'
            {
            match('%'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PCT

    // $ANTLR start EQ
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:14:4: ( '=' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:14:6: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EQ

    // $ANTLR start HAT
    public final void mHAT() throws RecognitionException {
        try {
            int _type = HAT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:15:5: ( '^' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:15:7: '^'
            {
            match('^'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end HAT

    // $ANTLR start GE
    public final void mGE() throws RecognitionException {
        try {
            int _type = GE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:16:4: ( '>=' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:16:6: '>='
            {
            match(">="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GE

    // $ANTLR start LE
    public final void mLE() throws RecognitionException {
        try {
            int _type = LE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:17:4: ( '<=' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:17:6: '<='
            {
            match("<="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LE

    // $ANTLR start LPAREN
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:18:8: ( '(' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:18:10: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LPAREN

    // $ANTLR start RPAREN
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:19:8: ( ')' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:19:10: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RPAREN

    // $ANTLR start T88
    public final void mT88() throws RecognitionException {
        try {
            int _type = T88;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:20:5: ( ';' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:20:7: ';'
            {
            match(';'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T88

    // $ANTLR start T89
    public final void mT89() throws RecognitionException {
        try {
            int _type = T89;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:21:5: ( ',' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:21:7: ','
            {
            match(','); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T89

    // $ANTLR start ALL
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:361:6: ( ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:361:8: ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ALL

    // $ANTLR start AND
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:362:6: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:362:8: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AND

    // $ANTLR start ARRAY
    public final void mARRAY() throws RecognitionException {
        try {
            int _type = ARRAY;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:363:8: ( ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'Y' | 'y' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:363:10: ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ARRAY

    // $ANTLR start AS
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:364:5: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:364:7: ( 'A' | 'a' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AS

    // $ANTLR start ASC
    public final void mASC() throws RecognitionException {
        try {
            int _type = ASC;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:365:5: ( ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:365:7: ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ASC

    // $ANTLR start BY
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:367:9: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:367:11: ( 'B' | 'b' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BY

    // $ANTLR start CROSS
    public final void mCROSS() throws RecognitionException {
        try {
            int _type = CROSS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:369:9: ( ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'S' | 's' ) ( 'S' | 's' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:369:11: ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'S' | 's' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CROSS

    // $ANTLR start DEFAULT
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:370:9: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:370:11: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DEFAULT

    // $ANTLR start DESC
    public final void mDESC() throws RecognitionException {
        try {
            int _type = DESC;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:371:7: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:371:9: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DESC

    // $ANTLR start DISTINCT
    public final void mDISTINCT() throws RecognitionException {
        try {
            int _type = DISTINCT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:372:9: ( ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:372:11: ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DISTINCT

    // $ANTLR start EXISTS
    public final void mEXISTS() throws RecognitionException {
        try {
            int _type = EXISTS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:373:8: ( ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:373:10: ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EXISTS

    // $ANTLR start FROM
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:376:7: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:376:9: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FROM

    // $ANTLR start FULL
    public final void mFULL() throws RecognitionException {
        try {
            int _type = FULL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:377:9: ( ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:377:11: ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FULL

    // $ANTLR start GLOBAL
    public final void mGLOBAL() throws RecognitionException {
        try {
            int _type = GLOBAL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:378:8: ( ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:378:10: ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GLOBAL

    // $ANTLR start GROUP
    public final void mGROUP() throws RecognitionException {
        try {
            int _type = GROUP;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:379:8: ( ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:379:10: ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GROUP

    // $ANTLR start HAVING
    public final void mHAVING() throws RecognitionException {
        try {
            int _type = HAVING;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:380:9: ( ( 'H' | 'h' ) ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:380:11: ( 'H' | 'h' ) ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end HAVING

    // $ANTLR start ILIKE
    public final void mILIKE() throws RecognitionException {
        try {
            int _type = ILIKE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:381:9: ( ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:381:11: ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ILIKE

    // $ANTLR start INTO
    public final void mINTO() throws RecognitionException {
        try {
            int _type = INTO;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:382:7: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'O' | 'o' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:382:9: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'O' | 'o' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INTO

    // $ANTLR start INNER
    public final void mINNER() throws RecognitionException {
        try {
            int _type = INNER;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:383:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:383:11: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INNER

    // $ANTLR start IS
    public final void mIS() throws RecognitionException {
        try {
            int _type = IS;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:384:9: ( ( 'I' | 'i' ) ( 'S' | 's' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:384:11: ( 'I' | 'i' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end IS

    // $ANTLR start ISNULL
    public final void mISNULL() throws RecognitionException {
        try {
            int _type = ISNULL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:385:9: ( ( 'I' | 'i' ) ( 'S' | 's' ) ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:385:11: ( 'I' | 'i' ) ( 'S' | 's' ) ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ISNULL

    // $ANTLR start JOIN
    public final void mJOIN() throws RecognitionException {
        try {
            int _type = JOIN;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:386:9: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:386:11: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end JOIN

    // $ANTLR start LEFT
    public final void mLEFT() throws RecognitionException {
        try {
            int _type = LEFT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:389:9: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:389:11: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEFT

    // $ANTLR start LEN
    public final void mLEN() throws RecognitionException {
        try {
            int _type = LEN;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:390:9: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'N' | 'n' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:390:11: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEN

    // $ANTLR start LIKE
    public final void mLIKE() throws RecognitionException {
        try {
            int _type = LIKE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:391:9: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:391:11: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LIKE

    // $ANTLR start LIMIT
    public final void mLIMIT() throws RecognitionException {
        try {
            int _type = LIMIT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:392:8: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:392:10: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LIMIT

    // $ANTLR start LOCAL
    public final void mLOCAL() throws RecognitionException {
        try {
            int _type = LOCAL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:393:7: ( ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:393:9: ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LOCAL

    // $ANTLR start NATURAL
    public final void mNATURAL() throws RecognitionException {
        try {
            int _type = NATURAL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:397:9: ( ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:397:11: ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NATURAL

    // $ANTLR start NEW
    public final void mNEW() throws RecognitionException {
        try {
            int _type = NEW;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:398:9: ( ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'W' | 'w' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:398:11: ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'W' | 'w' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NEW

    // $ANTLR start NOT
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:400:9: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:400:11: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NOT

    // $ANTLR start NOTNULL
    public final void mNOTNULL() throws RecognitionException {
        try {
            int _type = NOTNULL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:401:9: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:401:11: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NOTNULL

    // $ANTLR start NULL
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:402:9: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:402:11: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NULL

    // $ANTLR start OFFSET
    public final void mOFFSET() throws RecognitionException {
        try {
            int _type = OFFSET;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:404:9: ( ( 'O' | 'o' ) ( 'F' | 'f' ) ( 'F' | 'f' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:404:11: ( 'O' | 'o' ) ( 'F' | 'f' ) ( 'F' | 'f' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OFFSET

    // $ANTLR start OLD
    public final void mOLD() throws RecognitionException {
        try {
            int _type = OLD;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:405:9: ( ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:405:11: ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OLD

    // $ANTLR start ON
    public final void mON() throws RecognitionException {
        try {
            int _type = ON;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:406:9: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:406:11: ( 'O' | 'o' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ON

    // $ANTLR start ONLY
    public final void mONLY() throws RecognitionException {
        try {
            int _type = ONLY;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:407:9: ( ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'L' | 'l' ) ( 'Y' | 'y' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:407:11: ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'L' | 'l' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ONLY

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:408:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:408:11: ( 'O' | 'o' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OR

    // $ANTLR start ORDER
    public final void mORDER() throws RecognitionException {
        try {
            int _type = ORDER;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:409:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:409:11: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ORDER

    // $ANTLR start OUTER
    public final void mOUTER() throws RecognitionException {
        try {
            int _type = OUTER;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:410:9: ( ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:410:11: ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OUTER

    // $ANTLR start RIGHT
    public final void mRIGHT() throws RecognitionException {
        try {
            int _type = RIGHT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:411:9: ( ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:411:11: ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RIGHT

    // $ANTLR start SELECT
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:413:9: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:413:11: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SELECT

    // $ANTLR start SIMILAR
    public final void mSIMILAR() throws RecognitionException {
        try {
            int _type = SIMILAR;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:414:9: ( ( 'S' | 's' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'R' | 'r' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:414:11: ( 'S' | 's' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SIMILAR

    // $ANTLR start TABLE
    public final void mTABLE() throws RecognitionException {
        try {
            int _type = TABLE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:416:8: ( ( 'T' | 't' ) ( 'A' | 'a' ) ( 'B' | 'b' ) ( 'L' | 'l' ) ( 'E' | 'e' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:416:10: ( 'T' | 't' ) ( 'A' | 'a' ) ( 'B' | 'b' ) ( 'L' | 'l' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TABLE

    // $ANTLR start TEMP
    public final void mTEMP() throws RecognitionException {
        try {
            int _type = TEMP;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:417:6: ( ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:417:8: ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TEMP

    // $ANTLR start TEMPORARY
    public final void mTEMPORARY() throws RecognitionException {
        try {
            int _type = TEMPORARY;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:418:11: ( ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:418:13: ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TEMPORARY

    // $ANTLR start TO
    public final void mTO() throws RecognitionException {
        try {
            int _type = TO;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:419:9: ( ( 'T' | 't' ) ( 'O' | 'o' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:419:11: ( 'T' | 't' ) ( 'O' | 'o' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TO

    // $ANTLR start USING
    public final void mUSING() throws RecognitionException {
        try {
            int _type = USING;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:421:9: ( ( 'U' | 'u' ) ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:421:11: ( 'U' | 'u' ) ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end USING

    // $ANTLR start VALUES
    public final void mVALUES() throws RecognitionException {
        try {
            int _type = VALUES;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:422:9: ( ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'U' | 'u' ) ( 'E' | 'e' ) ( 'S' | 's' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:422:11: ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'U' | 'u' ) ( 'E' | 'e' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VALUES

    // $ANTLR start VARIADIC
    public final void mVARIADIC() throws RecognitionException {
        try {
            int _type = VARIADIC;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:423:9: ( ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'C' | 'c' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:423:11: ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'C' | 'c' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VARIADIC

    // $ANTLR start WHERE
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:424:8: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:424:10: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WHERE

    // $ANTLR start WHITESPACE
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:427:12: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+ )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:427:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:427:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\t' && LA1_0<='\n')||(LA1_0>='\f' && LA1_0<='\r')||LA1_0==' ') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WHITESPACE

    // $ANTLR start QUOTEDSTRING
    public final void mQUOTEDSTRING() throws RecognitionException {
        try {
            int _type = QUOTEDSTRING;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:14: ( ( '\\'' | '$$' ) ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )* ( '\\'' | '$$' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:17: ( '\\'' | '$$' ) ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )* ( '\\'' | '$$' )
            {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:17: ( '\\'' | '$$' )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\'') ) {
                alt2=1;
            }
            else if ( (LA2_0=='$') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("428:17: ( '\\'' | '$$' )", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:18: '\\''
                    {
                    match('\''); 

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:25: '$$'
                    {
                    match("$$"); 


                    }
                    break;

            }

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:31: ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\'') ) {
                    alt3=2;
                }
                else if ( (LA3_0=='$') ) {
                    int LA3_2 = input.LA(2);

                    if ( (LA3_2=='$') ) {
                        alt3=2;
                    }
                    else if ( ((LA3_2>='\u0000' && LA3_2<='#')||(LA3_2>='%' && LA3_2<='\uFFFE')) ) {
                        alt3=1;
                    }


                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='#')||(LA3_0>='%' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFE')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:58: (~ ( '\\'' | '$$' ) )
            	    {
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:58: (~ ( '\\'' | '$$' ) )
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:60: ~ ( '\\'' | '$$' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:81: ( '\\'' | '$$' )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\'') ) {
                alt4=1;
            }
            else if ( (LA4_0=='$') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("428:81: ( '\\'' | '$$' )", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:82: '\\''
                    {
                    match('\''); 

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:428:89: '$$'
                    {
                    match("$$"); 


                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end QUOTEDSTRING

    // $ANTLR start QUOTEID
    public final void mQUOTEID() throws RecognitionException {
        try {
            int _type = QUOTEID;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:429:9: ( ( '[' ID ']' | '\"' ID '\"' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:429:11: ( '[' ID ']' | '\"' ID '\"' )
            {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:429:11: ( '[' ID ']' | '\"' ID '\"' )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='[') ) {
                alt5=1;
            }
            else if ( (LA5_0=='\"') ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("429:11: ( '[' ID ']' | '\"' ID '\"' )", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:429:12: '[' ID ']'
                    {
                    match('['); 
                    mID(); 
                    match(']'); 

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:429:21: '\"' ID '\"'
                    {
                    match('\"'); 
                    mID(); 
                    match('\"'); 

                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end QUOTEID

    // $ANTLR start NUMBER
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:430:8: ( ( DIGIT )+ )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:430:10: ( DIGIT )+
            {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:430:10: ( DIGIT )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:430:11: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NUMBER

    // $ANTLR start ID
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:9: ( LETTER ( LETTER | NUMBER | '_' )* )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:11: LETTER ( LETTER | NUMBER | '_' )*
            {
            mLETTER(); 
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:18: ( LETTER | NUMBER | '_' )*
            loop7:
            do {
                int alt7=4;
                switch ( input.LA(1) ) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt7=1;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt7=2;
                    }
                    break;
                case '_':
                    {
                    alt7=3;
                    }
                    break;

                }

                switch (alt7) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:20: LETTER
            	    {
            	    mLETTER(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:29: NUMBER
            	    {
            	    mNUMBER(); 

            	    }
            	    break;
            	case 3 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:431:38: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ID

    // $ANTLR start DIGIT
    public final void mDIGIT() throws RecognitionException {
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:434:16: ( '0' .. '9' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:434:18: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end DIGIT

    // $ANTLR start LETTER
    public final void mLETTER() throws RecognitionException {
        try {
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:435:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:435:19: ( 'a' .. 'z' | 'A' .. 'Z' )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

        }
        finally {
        }
    }
    // $ANTLR end LETTER

    // $ANTLR start SL_COMMENT
    public final void mSL_COMMENT() throws RecognitionException {
        try {
            int _type = SL_COMMENT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:12: ( '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | EOF | '\\r' ( '\\n' )? ) )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:14: '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | EOF | '\\r' ( '\\n' )? )
            {
            match("--"); 

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:19: (~ ( '\\n' | '\\r' ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='\u0000' && LA8_0<='\t')||(LA8_0>='\u000B' && LA8_0<='\f')||(LA8_0>='\u000E' && LA8_0<='\uFFFE')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:20: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:35: ( '\\n' | EOF | '\\r' ( '\\n' )? )
            int alt10=3;
            switch ( input.LA(1) ) {
            case '\n':
                {
                alt10=1;
                }
                break;
            case '\r':
                {
                alt10=3;
                }
                break;
            default:
                alt10=2;}

            switch (alt10) {
                case 1 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:36: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 2 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:42: EOF
                    {
                    match(EOF); 

                    }
                    break;
                case 3 :
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:47: '\\r' ( '\\n' )?
                    {
                    match('\r'); 
                    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:51: ( '\\n' )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='\n') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:438:52: '\\n'
                            {
                            match('\n'); 

                            }
                            break;

                    }


                    }
                    break;

            }

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SL_COMMENT

    // $ANTLR start ML_COMMENT
    public final void mML_COMMENT() throws RecognitionException {
        try {
            int _type = ML_COMMENT;
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:439:17: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:439:19: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:439:24: ( options {greedy=false; } : . )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0=='*') ) {
                    int LA11_1 = input.LA(2);

                    if ( (LA11_1=='/') ) {
                        alt11=2;
                    }
                    else if ( ((LA11_1>='\u0000' && LA11_1<='.')||(LA11_1>='0' && LA11_1<='\uFFFE')) ) {
                        alt11=1;
                    }


                }
                else if ( ((LA11_0>='\u0000' && LA11_0<=')')||(LA11_0>='+' && LA11_0<='\uFFFE')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:439:51: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            match("*/"); 

             channel = HIDDEN; 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ML_COMMENT

    public void mTokens() throws RecognitionException {
        // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:8: ( PLUS | MINUS | STAR | DIV | GT | LT | DOT | PCT | EQ | HAT | GE | LE | LPAREN | RPAREN | T88 | T89 | ALL | AND | ARRAY | AS | ASC | BY | CROSS | DEFAULT | DESC | DISTINCT | EXISTS | FROM | FULL | GLOBAL | GROUP | HAVING | ILIKE | INTO | INNER | IS | ISNULL | JOIN | LEFT | LEN | LIKE | LIMIT | LOCAL | NATURAL | NEW | NOT | NOTNULL | NULL | OFFSET | OLD | ON | ONLY | OR | ORDER | OUTER | RIGHT | SELECT | SIMILAR | TABLE | TEMP | TEMPORARY | TO | USING | VALUES | VARIADIC | WHERE | WHITESPACE | QUOTEDSTRING | QUOTEID | NUMBER | ID | SL_COMMENT | ML_COMMENT )
        int alt12=73;
        switch ( input.LA(1) ) {
        case '+':
            {
            alt12=1;
            }
            break;
        case '-':
            {
            int LA12_2 = input.LA(2);

            if ( (LA12_2=='-') ) {
                alt12=72;
            }
            else {
                alt12=2;}
            }
            break;
        case '*':
            {
            alt12=3;
            }
            break;
        case '/':
            {
            int LA12_4 = input.LA(2);

            if ( (LA12_4=='*') ) {
                alt12=73;
            }
            else {
                alt12=4;}
            }
            break;
        case '>':
            {
            int LA12_5 = input.LA(2);

            if ( (LA12_5=='=') ) {
                alt12=11;
            }
            else {
                alt12=5;}
            }
            break;
        case '<':
            {
            int LA12_6 = input.LA(2);

            if ( (LA12_6=='=') ) {
                alt12=12;
            }
            else {
                alt12=6;}
            }
            break;
        case '.':
            {
            alt12=7;
            }
            break;
        case '%':
            {
            alt12=8;
            }
            break;
        case '=':
            {
            alt12=9;
            }
            break;
        case '^':
            {
            alt12=10;
            }
            break;
        case '(':
            {
            alt12=13;
            }
            break;
        case ')':
            {
            alt12=14;
            }
            break;
        case ';':
            {
            alt12=15;
            }
            break;
        case ',':
            {
            alt12=16;
            }
            break;
        case 'A':
        case 'a':
            {
            switch ( input.LA(2) ) {
            case 'R':
            case 'r':
                {
                int LA12_47 = input.LA(3);

                if ( (LA12_47=='R'||LA12_47=='r') ) {
                    int LA12_86 = input.LA(4);

                    if ( (LA12_86=='A'||LA12_86=='a') ) {
                        int LA12_134 = input.LA(5);

                        if ( (LA12_134=='Y'||LA12_134=='y') ) {
                            int LA12_177 = input.LA(6);

                            if ( ((LA12_177>='0' && LA12_177<='9')||(LA12_177>='A' && LA12_177<='Z')||LA12_177=='_'||(LA12_177>='a' && LA12_177<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=19;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'S':
            case 's':
                {
                switch ( input.LA(3) ) {
                case 'C':
                case 'c':
                    {
                    int LA12_87 = input.LA(4);

                    if ( ((LA12_87>='0' && LA12_87<='9')||(LA12_87>='A' && LA12_87<='Z')||LA12_87=='_'||(LA12_87>='a' && LA12_87<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=21;}
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt12=71;
                    }
                    break;
                default:
                    alt12=20;}

                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_49 = input.LA(3);

                if ( (LA12_49=='L'||LA12_49=='l') ) {
                    int LA12_89 = input.LA(4);

                    if ( ((LA12_89>='0' && LA12_89<='9')||(LA12_89>='A' && LA12_89<='Z')||LA12_89=='_'||(LA12_89>='a' && LA12_89<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=17;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'N':
            case 'n':
                {
                int LA12_50 = input.LA(3);

                if ( (LA12_50=='D'||LA12_50=='d') ) {
                    int LA12_90 = input.LA(4);

                    if ( ((LA12_90>='0' && LA12_90<='9')||(LA12_90>='A' && LA12_90<='Z')||LA12_90=='_'||(LA12_90>='a' && LA12_90<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=18;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'B':
        case 'b':
            {
            int LA12_16 = input.LA(2);

            if ( (LA12_16=='Y'||LA12_16=='y') ) {
                int LA12_51 = input.LA(3);

                if ( ((LA12_51>='0' && LA12_51<='9')||(LA12_51>='A' && LA12_51<='Z')||LA12_51=='_'||(LA12_51>='a' && LA12_51<='z')) ) {
                    alt12=71;
                }
                else {
                    alt12=22;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'C':
        case 'c':
            {
            int LA12_17 = input.LA(2);

            if ( (LA12_17=='R'||LA12_17=='r') ) {
                int LA12_52 = input.LA(3);

                if ( (LA12_52=='O'||LA12_52=='o') ) {
                    int LA12_92 = input.LA(4);

                    if ( (LA12_92=='S'||LA12_92=='s') ) {
                        int LA12_138 = input.LA(5);

                        if ( (LA12_138=='S'||LA12_138=='s') ) {
                            int LA12_178 = input.LA(6);

                            if ( ((LA12_178>='0' && LA12_178<='9')||(LA12_178>='A' && LA12_178<='Z')||LA12_178=='_'||(LA12_178>='a' && LA12_178<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=23;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'D':
        case 'd':
            {
            switch ( input.LA(2) ) {
            case 'E':
            case 'e':
                {
                switch ( input.LA(3) ) {
                case 'F':
                case 'f':
                    {
                    int LA12_93 = input.LA(4);

                    if ( (LA12_93=='A'||LA12_93=='a') ) {
                        int LA12_139 = input.LA(5);

                        if ( (LA12_139=='U'||LA12_139=='u') ) {
                            int LA12_179 = input.LA(6);

                            if ( (LA12_179=='L'||LA12_179=='l') ) {
                                int LA12_216 = input.LA(7);

                                if ( (LA12_216=='T'||LA12_216=='t') ) {
                                    int LA12_241 = input.LA(8);

                                    if ( ((LA12_241>='0' && LA12_241<='9')||(LA12_241>='A' && LA12_241<='Z')||LA12_241=='_'||(LA12_241>='a' && LA12_241<='z')) ) {
                                        alt12=71;
                                    }
                                    else {
                                        alt12=24;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case 'S':
                case 's':
                    {
                    int LA12_94 = input.LA(4);

                    if ( (LA12_94=='C'||LA12_94=='c') ) {
                        int LA12_140 = input.LA(5);

                        if ( ((LA12_140>='0' && LA12_140<='9')||(LA12_140>='A' && LA12_140<='Z')||LA12_140=='_'||(LA12_140>='a' && LA12_140<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=25;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                default:
                    alt12=71;}

                }
                break;
            case 'I':
            case 'i':
                {
                int LA12_54 = input.LA(3);

                if ( (LA12_54=='S'||LA12_54=='s') ) {
                    int LA12_95 = input.LA(4);

                    if ( (LA12_95=='T'||LA12_95=='t') ) {
                        int LA12_141 = input.LA(5);

                        if ( (LA12_141=='I'||LA12_141=='i') ) {
                            int LA12_181 = input.LA(6);

                            if ( (LA12_181=='N'||LA12_181=='n') ) {
                                int LA12_217 = input.LA(7);

                                if ( (LA12_217=='C'||LA12_217=='c') ) {
                                    int LA12_242 = input.LA(8);

                                    if ( (LA12_242=='T'||LA12_242=='t') ) {
                                        int LA12_256 = input.LA(9);

                                        if ( ((LA12_256>='0' && LA12_256<='9')||(LA12_256>='A' && LA12_256<='Z')||LA12_256=='_'||(LA12_256>='a' && LA12_256<='z')) ) {
                                            alt12=71;
                                        }
                                        else {
                                            alt12=26;}
                                    }
                                    else {
                                        alt12=71;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'E':
        case 'e':
            {
            int LA12_19 = input.LA(2);

            if ( (LA12_19=='X'||LA12_19=='x') ) {
                int LA12_55 = input.LA(3);

                if ( (LA12_55=='I'||LA12_55=='i') ) {
                    int LA12_96 = input.LA(4);

                    if ( (LA12_96=='S'||LA12_96=='s') ) {
                        int LA12_142 = input.LA(5);

                        if ( (LA12_142=='T'||LA12_142=='t') ) {
                            int LA12_182 = input.LA(6);

                            if ( (LA12_182=='S'||LA12_182=='s') ) {
                                int LA12_218 = input.LA(7);

                                if ( ((LA12_218>='0' && LA12_218<='9')||(LA12_218>='A' && LA12_218<='Z')||LA12_218=='_'||(LA12_218>='a' && LA12_218<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=27;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'F':
        case 'f':
            {
            switch ( input.LA(2) ) {
            case 'R':
            case 'r':
                {
                int LA12_56 = input.LA(3);

                if ( (LA12_56=='O'||LA12_56=='o') ) {
                    int LA12_97 = input.LA(4);

                    if ( (LA12_97=='M'||LA12_97=='m') ) {
                        int LA12_143 = input.LA(5);

                        if ( ((LA12_143>='0' && LA12_143<='9')||(LA12_143>='A' && LA12_143<='Z')||LA12_143=='_'||(LA12_143>='a' && LA12_143<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=28;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'U':
            case 'u':
                {
                int LA12_57 = input.LA(3);

                if ( (LA12_57=='L'||LA12_57=='l') ) {
                    int LA12_98 = input.LA(4);

                    if ( (LA12_98=='L'||LA12_98=='l') ) {
                        int LA12_144 = input.LA(5);

                        if ( ((LA12_144>='0' && LA12_144<='9')||(LA12_144>='A' && LA12_144<='Z')||LA12_144=='_'||(LA12_144>='a' && LA12_144<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=29;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'G':
        case 'g':
            {
            switch ( input.LA(2) ) {
            case 'R':
            case 'r':
                {
                int LA12_58 = input.LA(3);

                if ( (LA12_58=='O'||LA12_58=='o') ) {
                    int LA12_99 = input.LA(4);

                    if ( (LA12_99=='U'||LA12_99=='u') ) {
                        int LA12_145 = input.LA(5);

                        if ( (LA12_145=='P'||LA12_145=='p') ) {
                            int LA12_185 = input.LA(6);

                            if ( ((LA12_185>='0' && LA12_185<='9')||(LA12_185>='A' && LA12_185<='Z')||LA12_185=='_'||(LA12_185>='a' && LA12_185<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=31;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_59 = input.LA(3);

                if ( (LA12_59=='O'||LA12_59=='o') ) {
                    int LA12_100 = input.LA(4);

                    if ( (LA12_100=='B'||LA12_100=='b') ) {
                        int LA12_146 = input.LA(5);

                        if ( (LA12_146=='A'||LA12_146=='a') ) {
                            int LA12_186 = input.LA(6);

                            if ( (LA12_186=='L'||LA12_186=='l') ) {
                                int LA12_220 = input.LA(7);

                                if ( ((LA12_220>='0' && LA12_220<='9')||(LA12_220>='A' && LA12_220<='Z')||LA12_220=='_'||(LA12_220>='a' && LA12_220<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=30;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'H':
        case 'h':
            {
            int LA12_22 = input.LA(2);

            if ( (LA12_22=='A'||LA12_22=='a') ) {
                int LA12_60 = input.LA(3);

                if ( (LA12_60=='V'||LA12_60=='v') ) {
                    int LA12_101 = input.LA(4);

                    if ( (LA12_101=='I'||LA12_101=='i') ) {
                        int LA12_147 = input.LA(5);

                        if ( (LA12_147=='N'||LA12_147=='n') ) {
                            int LA12_187 = input.LA(6);

                            if ( (LA12_187=='G'||LA12_187=='g') ) {
                                int LA12_221 = input.LA(7);

                                if ( ((LA12_221>='0' && LA12_221<='9')||(LA12_221>='A' && LA12_221<='Z')||LA12_221=='_'||(LA12_221>='a' && LA12_221<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=32;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'I':
        case 'i':
            {
            switch ( input.LA(2) ) {
            case 'S':
            case 's':
                {
                switch ( input.LA(3) ) {
                case 'N':
                case 'n':
                    {
                    int LA12_102 = input.LA(4);

                    if ( (LA12_102=='U'||LA12_102=='u') ) {
                        int LA12_148 = input.LA(5);

                        if ( (LA12_148=='L'||LA12_148=='l') ) {
                            int LA12_188 = input.LA(6);

                            if ( (LA12_188=='L'||LA12_188=='l') ) {
                                int LA12_222 = input.LA(7);

                                if ( ((LA12_222>='0' && LA12_222<='9')||(LA12_222>='A' && LA12_222<='Z')||LA12_222=='_'||(LA12_222>='a' && LA12_222<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=37;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt12=71;
                    }
                    break;
                default:
                    alt12=36;}

                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_62 = input.LA(3);

                if ( (LA12_62=='I'||LA12_62=='i') ) {
                    int LA12_104 = input.LA(4);

                    if ( (LA12_104=='K'||LA12_104=='k') ) {
                        int LA12_149 = input.LA(5);

                        if ( (LA12_149=='E'||LA12_149=='e') ) {
                            int LA12_189 = input.LA(6);

                            if ( ((LA12_189>='0' && LA12_189<='9')||(LA12_189>='A' && LA12_189<='Z')||LA12_189=='_'||(LA12_189>='a' && LA12_189<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=33;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'N':
            case 'n':
                {
                switch ( input.LA(3) ) {
                case 'N':
                case 'n':
                    {
                    int LA12_105 = input.LA(4);

                    if ( (LA12_105=='E'||LA12_105=='e') ) {
                        int LA12_150 = input.LA(5);

                        if ( (LA12_150=='R'||LA12_150=='r') ) {
                            int LA12_190 = input.LA(6);

                            if ( ((LA12_190>='0' && LA12_190<='9')||(LA12_190>='A' && LA12_190<='Z')||LA12_190=='_'||(LA12_190>='a' && LA12_190<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=35;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case 'T':
                case 't':
                    {
                    int LA12_106 = input.LA(4);

                    if ( (LA12_106=='O'||LA12_106=='o') ) {
                        int LA12_151 = input.LA(5);

                        if ( ((LA12_151>='0' && LA12_151<='9')||(LA12_151>='A' && LA12_151<='Z')||LA12_151=='_'||(LA12_151>='a' && LA12_151<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=34;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                default:
                    alt12=71;}

                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'J':
        case 'j':
            {
            int LA12_24 = input.LA(2);

            if ( (LA12_24=='O'||LA12_24=='o') ) {
                int LA12_64 = input.LA(3);

                if ( (LA12_64=='I'||LA12_64=='i') ) {
                    int LA12_107 = input.LA(4);

                    if ( (LA12_107=='N'||LA12_107=='n') ) {
                        int LA12_152 = input.LA(5);

                        if ( ((LA12_152>='0' && LA12_152<='9')||(LA12_152>='A' && LA12_152<='Z')||LA12_152=='_'||(LA12_152>='a' && LA12_152<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=38;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'L':
        case 'l':
            {
            switch ( input.LA(2) ) {
            case 'O':
            case 'o':
                {
                int LA12_65 = input.LA(3);

                if ( (LA12_65=='C'||LA12_65=='c') ) {
                    int LA12_108 = input.LA(4);

                    if ( (LA12_108=='A'||LA12_108=='a') ) {
                        int LA12_153 = input.LA(5);

                        if ( (LA12_153=='L'||LA12_153=='l') ) {
                            int LA12_193 = input.LA(6);

                            if ( ((LA12_193>='0' && LA12_193<='9')||(LA12_193>='A' && LA12_193<='Z')||LA12_193=='_'||(LA12_193>='a' && LA12_193<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=43;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'I':
            case 'i':
                {
                switch ( input.LA(3) ) {
                case 'M':
                case 'm':
                    {
                    int LA12_109 = input.LA(4);

                    if ( (LA12_109=='I'||LA12_109=='i') ) {
                        int LA12_154 = input.LA(5);

                        if ( (LA12_154=='T'||LA12_154=='t') ) {
                            int LA12_194 = input.LA(6);

                            if ( ((LA12_194>='0' && LA12_194<='9')||(LA12_194>='A' && LA12_194<='Z')||LA12_194=='_'||(LA12_194>='a' && LA12_194<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=42;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case 'K':
                case 'k':
                    {
                    int LA12_110 = input.LA(4);

                    if ( (LA12_110=='E'||LA12_110=='e') ) {
                        int LA12_155 = input.LA(5);

                        if ( ((LA12_155>='0' && LA12_155<='9')||(LA12_155>='A' && LA12_155<='Z')||LA12_155=='_'||(LA12_155>='a' && LA12_155<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=41;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                default:
                    alt12=71;}

                }
                break;
            case 'E':
            case 'e':
                {
                switch ( input.LA(3) ) {
                case 'N':
                case 'n':
                    {
                    int LA12_111 = input.LA(4);

                    if ( ((LA12_111>='0' && LA12_111<='9')||(LA12_111>='A' && LA12_111<='Z')||LA12_111=='_'||(LA12_111>='a' && LA12_111<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=40;}
                    }
                    break;
                case 'F':
                case 'f':
                    {
                    int LA12_112 = input.LA(4);

                    if ( (LA12_112=='T'||LA12_112=='t') ) {
                        int LA12_157 = input.LA(5);

                        if ( ((LA12_157>='0' && LA12_157<='9')||(LA12_157>='A' && LA12_157<='Z')||LA12_157=='_'||(LA12_157>='a' && LA12_157<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=39;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                default:
                    alt12=71;}

                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'N':
        case 'n':
            {
            switch ( input.LA(2) ) {
            case 'O':
            case 'o':
                {
                int LA12_68 = input.LA(3);

                if ( (LA12_68=='T'||LA12_68=='t') ) {
                    switch ( input.LA(4) ) {
                    case 'N':
                    case 'n':
                        {
                        int LA12_158 = input.LA(5);

                        if ( (LA12_158=='U'||LA12_158=='u') ) {
                            int LA12_197 = input.LA(6);

                            if ( (LA12_197=='L'||LA12_197=='l') ) {
                                int LA12_227 = input.LA(7);

                                if ( (LA12_227=='L'||LA12_227=='l') ) {
                                    int LA12_247 = input.LA(8);

                                    if ( ((LA12_247>='0' && LA12_247<='9')||(LA12_247>='A' && LA12_247<='Z')||LA12_247=='_'||(LA12_247>='a' && LA12_247<='z')) ) {
                                        alt12=71;
                                    }
                                    else {
                                        alt12=47;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                        }
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case '_':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z':
                        {
                        alt12=71;
                        }
                        break;
                    default:
                        alt12=46;}

                }
                else {
                    alt12=71;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_69 = input.LA(3);

                if ( (LA12_69=='W'||LA12_69=='w') ) {
                    int LA12_114 = input.LA(4);

                    if ( ((LA12_114>='0' && LA12_114<='9')||(LA12_114>='A' && LA12_114<='Z')||LA12_114=='_'||(LA12_114>='a' && LA12_114<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=45;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'A':
            case 'a':
                {
                int LA12_70 = input.LA(3);

                if ( (LA12_70=='T'||LA12_70=='t') ) {
                    int LA12_115 = input.LA(4);

                    if ( (LA12_115=='U'||LA12_115=='u') ) {
                        int LA12_161 = input.LA(5);

                        if ( (LA12_161=='R'||LA12_161=='r') ) {
                            int LA12_198 = input.LA(6);

                            if ( (LA12_198=='A'||LA12_198=='a') ) {
                                int LA12_228 = input.LA(7);

                                if ( (LA12_228=='L'||LA12_228=='l') ) {
                                    int LA12_248 = input.LA(8);

                                    if ( ((LA12_248>='0' && LA12_248<='9')||(LA12_248>='A' && LA12_248<='Z')||LA12_248=='_'||(LA12_248>='a' && LA12_248<='z')) ) {
                                        alt12=71;
                                    }
                                    else {
                                        alt12=44;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'U':
            case 'u':
                {
                int LA12_71 = input.LA(3);

                if ( (LA12_71=='L'||LA12_71=='l') ) {
                    int LA12_116 = input.LA(4);

                    if ( (LA12_116=='L'||LA12_116=='l') ) {
                        int LA12_162 = input.LA(5);

                        if ( ((LA12_162>='0' && LA12_162<='9')||(LA12_162>='A' && LA12_162<='Z')||LA12_162=='_'||(LA12_162>='a' && LA12_162<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=48;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'O':
        case 'o':
            {
            switch ( input.LA(2) ) {
            case 'R':
            case 'r':
                {
                switch ( input.LA(3) ) {
                case 'D':
                case 'd':
                    {
                    int LA12_117 = input.LA(4);

                    if ( (LA12_117=='E'||LA12_117=='e') ) {
                        int LA12_163 = input.LA(5);

                        if ( (LA12_163=='R'||LA12_163=='r') ) {
                            int LA12_200 = input.LA(6);

                            if ( ((LA12_200>='0' && LA12_200<='9')||(LA12_200>='A' && LA12_200<='Z')||LA12_200=='_'||(LA12_200>='a' && LA12_200<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=54;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt12=71;
                    }
                    break;
                default:
                    alt12=53;}

                }
                break;
            case 'N':
            case 'n':
                {
                switch ( input.LA(3) ) {
                case 'L':
                case 'l':
                    {
                    int LA12_119 = input.LA(4);

                    if ( (LA12_119=='Y'||LA12_119=='y') ) {
                        int LA12_164 = input.LA(5);

                        if ( ((LA12_164>='0' && LA12_164<='9')||(LA12_164>='A' && LA12_164<='Z')||LA12_164=='_'||(LA12_164>='a' && LA12_164<='z')) ) {
                            alt12=71;
                        }
                        else {
                            alt12=52;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt12=71;
                    }
                    break;
                default:
                    alt12=51;}

                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_74 = input.LA(3);

                if ( (LA12_74=='D'||LA12_74=='d') ) {
                    int LA12_121 = input.LA(4);

                    if ( ((LA12_121>='0' && LA12_121<='9')||(LA12_121>='A' && LA12_121<='Z')||LA12_121=='_'||(LA12_121>='a' && LA12_121<='z')) ) {
                        alt12=71;
                    }
                    else {
                        alt12=50;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'F':
            case 'f':
                {
                int LA12_75 = input.LA(3);

                if ( (LA12_75=='F'||LA12_75=='f') ) {
                    int LA12_122 = input.LA(4);

                    if ( (LA12_122=='S'||LA12_122=='s') ) {
                        int LA12_166 = input.LA(5);

                        if ( (LA12_166=='E'||LA12_166=='e') ) {
                            int LA12_202 = input.LA(6);

                            if ( (LA12_202=='T'||LA12_202=='t') ) {
                                int LA12_230 = input.LA(7);

                                if ( ((LA12_230>='0' && LA12_230<='9')||(LA12_230>='A' && LA12_230<='Z')||LA12_230=='_'||(LA12_230>='a' && LA12_230<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=49;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'U':
            case 'u':
                {
                int LA12_76 = input.LA(3);

                if ( (LA12_76=='T'||LA12_76=='t') ) {
                    int LA12_123 = input.LA(4);

                    if ( (LA12_123=='E'||LA12_123=='e') ) {
                        int LA12_167 = input.LA(5);

                        if ( (LA12_167=='R'||LA12_167=='r') ) {
                            int LA12_203 = input.LA(6);

                            if ( ((LA12_203>='0' && LA12_203<='9')||(LA12_203>='A' && LA12_203<='Z')||LA12_203=='_'||(LA12_203>='a' && LA12_203<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=55;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'R':
        case 'r':
            {
            int LA12_28 = input.LA(2);

            if ( (LA12_28=='I'||LA12_28=='i') ) {
                int LA12_77 = input.LA(3);

                if ( (LA12_77=='G'||LA12_77=='g') ) {
                    int LA12_124 = input.LA(4);

                    if ( (LA12_124=='H'||LA12_124=='h') ) {
                        int LA12_168 = input.LA(5);

                        if ( (LA12_168=='T'||LA12_168=='t') ) {
                            int LA12_204 = input.LA(6);

                            if ( ((LA12_204>='0' && LA12_204<='9')||(LA12_204>='A' && LA12_204<='Z')||LA12_204=='_'||(LA12_204>='a' && LA12_204<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=56;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'S':
        case 's':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                int LA12_78 = input.LA(3);

                if ( (LA12_78=='M'||LA12_78=='m') ) {
                    int LA12_125 = input.LA(4);

                    if ( (LA12_125=='I'||LA12_125=='i') ) {
                        int LA12_169 = input.LA(5);

                        if ( (LA12_169=='L'||LA12_169=='l') ) {
                            int LA12_205 = input.LA(6);

                            if ( (LA12_205=='A'||LA12_205=='a') ) {
                                int LA12_233 = input.LA(7);

                                if ( (LA12_233=='R'||LA12_233=='r') ) {
                                    int LA12_250 = input.LA(8);

                                    if ( ((LA12_250>='0' && LA12_250<='9')||(LA12_250>='A' && LA12_250<='Z')||LA12_250=='_'||(LA12_250>='a' && LA12_250<='z')) ) {
                                        alt12=71;
                                    }
                                    else {
                                        alt12=58;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_79 = input.LA(3);

                if ( (LA12_79=='L'||LA12_79=='l') ) {
                    int LA12_126 = input.LA(4);

                    if ( (LA12_126=='E'||LA12_126=='e') ) {
                        int LA12_170 = input.LA(5);

                        if ( (LA12_170=='C'||LA12_170=='c') ) {
                            int LA12_206 = input.LA(6);

                            if ( (LA12_206=='T'||LA12_206=='t') ) {
                                int LA12_234 = input.LA(7);

                                if ( ((LA12_234>='0' && LA12_234<='9')||(LA12_234>='A' && LA12_234<='Z')||LA12_234=='_'||(LA12_234>='a' && LA12_234<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=57;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'T':
        case 't':
            {
            switch ( input.LA(2) ) {
            case 'A':
            case 'a':
                {
                int LA12_80 = input.LA(3);

                if ( (LA12_80=='B'||LA12_80=='b') ) {
                    int LA12_127 = input.LA(4);

                    if ( (LA12_127=='L'||LA12_127=='l') ) {
                        int LA12_171 = input.LA(5);

                        if ( (LA12_171=='E'||LA12_171=='e') ) {
                            int LA12_207 = input.LA(6);

                            if ( ((LA12_207>='0' && LA12_207<='9')||(LA12_207>='A' && LA12_207<='Z')||LA12_207=='_'||(LA12_207>='a' && LA12_207<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=59;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            case 'O':
            case 'o':
                {
                int LA12_81 = input.LA(3);

                if ( ((LA12_81>='0' && LA12_81<='9')||(LA12_81>='A' && LA12_81<='Z')||LA12_81=='_'||(LA12_81>='a' && LA12_81<='z')) ) {
                    alt12=71;
                }
                else {
                    alt12=62;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_82 = input.LA(3);

                if ( (LA12_82=='M'||LA12_82=='m') ) {
                    int LA12_129 = input.LA(4);

                    if ( (LA12_129=='P'||LA12_129=='p') ) {
                        switch ( input.LA(5) ) {
                        case 'O':
                        case 'o':
                            {
                            int LA12_208 = input.LA(6);

                            if ( (LA12_208=='R'||LA12_208=='r') ) {
                                int LA12_236 = input.LA(7);

                                if ( (LA12_236=='A'||LA12_236=='a') ) {
                                    int LA12_252 = input.LA(8);

                                    if ( (LA12_252=='R'||LA12_252=='r') ) {
                                        int LA12_260 = input.LA(9);

                                        if ( (LA12_260=='Y'||LA12_260=='y') ) {
                                            int LA12_263 = input.LA(10);

                                            if ( ((LA12_263>='0' && LA12_263<='9')||(LA12_263>='A' && LA12_263<='Z')||LA12_263=='_'||(LA12_263>='a' && LA12_263<='z')) ) {
                                                alt12=71;
                                            }
                                            else {
                                                alt12=61;}
                                        }
                                        else {
                                            alt12=71;}
                                    }
                                    else {
                                        alt12=71;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                            }
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'I':
                        case 'J':
                        case 'K':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case '_':
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'i':
                        case 'j':
                        case 'k':
                        case 'l':
                        case 'm':
                        case 'n':
                        case 'p':
                        case 'q':
                        case 'r':
                        case 's':
                        case 't':
                        case 'u':
                        case 'v':
                        case 'w':
                        case 'x':
                        case 'y':
                        case 'z':
                            {
                            alt12=71;
                            }
                            break;
                        default:
                            alt12=60;}

                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
                }
                break;
            default:
                alt12=71;}

            }
            break;
        case 'U':
        case 'u':
            {
            int LA12_31 = input.LA(2);

            if ( (LA12_31=='S'||LA12_31=='s') ) {
                int LA12_83 = input.LA(3);

                if ( (LA12_83=='I'||LA12_83=='i') ) {
                    int LA12_130 = input.LA(4);

                    if ( (LA12_130=='N'||LA12_130=='n') ) {
                        int LA12_173 = input.LA(5);

                        if ( (LA12_173=='G'||LA12_173=='g') ) {
                            int LA12_210 = input.LA(6);

                            if ( ((LA12_210>='0' && LA12_210<='9')||(LA12_210>='A' && LA12_210<='Z')||LA12_210=='_'||(LA12_210>='a' && LA12_210<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=63;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case 'V':
        case 'v':
            {
            int LA12_32 = input.LA(2);

            if ( (LA12_32=='A'||LA12_32=='a') ) {
                switch ( input.LA(3) ) {
                case 'L':
                case 'l':
                    {
                    int LA12_131 = input.LA(4);

                    if ( (LA12_131=='U'||LA12_131=='u') ) {
                        int LA12_174 = input.LA(5);

                        if ( (LA12_174=='E'||LA12_174=='e') ) {
                            int LA12_211 = input.LA(6);

                            if ( (LA12_211=='S'||LA12_211=='s') ) {
                                int LA12_238 = input.LA(7);

                                if ( ((LA12_238>='0' && LA12_238<='9')||(LA12_238>='A' && LA12_238<='Z')||LA12_238=='_'||(LA12_238>='a' && LA12_238<='z')) ) {
                                    alt12=71;
                                }
                                else {
                                    alt12=64;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                case 'R':
                case 'r':
                    {
                    int LA12_132 = input.LA(4);

                    if ( (LA12_132=='I'||LA12_132=='i') ) {
                        int LA12_175 = input.LA(5);

                        if ( (LA12_175=='A'||LA12_175=='a') ) {
                            int LA12_212 = input.LA(6);

                            if ( (LA12_212=='D'||LA12_212=='d') ) {
                                int LA12_239 = input.LA(7);

                                if ( (LA12_239=='I'||LA12_239=='i') ) {
                                    int LA12_254 = input.LA(8);

                                    if ( (LA12_254=='C'||LA12_254=='c') ) {
                                        int LA12_261 = input.LA(9);

                                        if ( ((LA12_261>='0' && LA12_261<='9')||(LA12_261>='A' && LA12_261<='Z')||LA12_261=='_'||(LA12_261>='a' && LA12_261<='z')) ) {
                                            alt12=71;
                                        }
                                        else {
                                            alt12=65;}
                                    }
                                    else {
                                        alt12=71;}
                                }
                                else {
                                    alt12=71;}
                            }
                            else {
                                alt12=71;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                    }
                    break;
                default:
                    alt12=71;}

            }
            else {
                alt12=71;}
            }
            break;
        case 'W':
        case 'w':
            {
            int LA12_33 = input.LA(2);

            if ( (LA12_33=='H'||LA12_33=='h') ) {
                int LA12_85 = input.LA(3);

                if ( (LA12_85=='E'||LA12_85=='e') ) {
                    int LA12_133 = input.LA(4);

                    if ( (LA12_133=='R'||LA12_133=='r') ) {
                        int LA12_176 = input.LA(5);

                        if ( (LA12_176=='E'||LA12_176=='e') ) {
                            int LA12_213 = input.LA(6);

                            if ( ((LA12_213>='0' && LA12_213<='9')||(LA12_213>='A' && LA12_213<='Z')||LA12_213=='_'||(LA12_213>='a' && LA12_213<='z')) ) {
                                alt12=71;
                            }
                            else {
                                alt12=66;}
                        }
                        else {
                            alt12=71;}
                    }
                    else {
                        alt12=71;}
                }
                else {
                    alt12=71;}
            }
            else {
                alt12=71;}
            }
            break;
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            {
            alt12=67;
            }
            break;
        case '$':
        case '\'':
            {
            alt12=68;
            }
            break;
        case '\"':
        case '[':
            {
            alt12=69;
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt12=70;
            }
            break;
        case 'K':
        case 'M':
        case 'P':
        case 'Q':
        case 'X':
        case 'Y':
        case 'Z':
        case 'k':
        case 'm':
        case 'p':
        case 'q':
        case 'x':
        case 'y':
        case 'z':
            {
            alt12=71;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( PLUS | MINUS | STAR | DIV | GT | LT | DOT | PCT | EQ | HAT | GE | LE | LPAREN | RPAREN | T88 | T89 | ALL | AND | ARRAY | AS | ASC | BY | CROSS | DEFAULT | DESC | DISTINCT | EXISTS | FROM | FULL | GLOBAL | GROUP | HAVING | ILIKE | INTO | INNER | IS | ISNULL | JOIN | LEFT | LEN | LIKE | LIMIT | LOCAL | NATURAL | NEW | NOT | NOTNULL | NULL | OFFSET | OLD | ON | ONLY | OR | ORDER | OUTER | RIGHT | SELECT | SIMILAR | TABLE | TEMP | TEMPORARY | TO | USING | VALUES | VARIADIC | WHERE | WHITESPACE | QUOTEDSTRING | QUOTEID | NUMBER | ID | SL_COMMENT | ML_COMMENT );", 12, 0, input);

            throw nvae;
        }

        switch (alt12) {
            case 1 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:10: PLUS
                {
                mPLUS(); 

                }
                break;
            case 2 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:15: MINUS
                {
                mMINUS(); 

                }
                break;
            case 3 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:21: STAR
                {
                mSTAR(); 

                }
                break;
            case 4 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:26: DIV
                {
                mDIV(); 

                }
                break;
            case 5 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:30: GT
                {
                mGT(); 

                }
                break;
            case 6 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:33: LT
                {
                mLT(); 

                }
                break;
            case 7 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:36: DOT
                {
                mDOT(); 

                }
                break;
            case 8 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:40: PCT
                {
                mPCT(); 

                }
                break;
            case 9 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:44: EQ
                {
                mEQ(); 

                }
                break;
            case 10 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:47: HAT
                {
                mHAT(); 

                }
                break;
            case 11 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:51: GE
                {
                mGE(); 

                }
                break;
            case 12 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:54: LE
                {
                mLE(); 

                }
                break;
            case 13 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:57: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 14 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:64: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 15 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:71: T88
                {
                mT88(); 

                }
                break;
            case 16 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:75: T89
                {
                mT89(); 

                }
                break;
            case 17 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:79: ALL
                {
                mALL(); 

                }
                break;
            case 18 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:83: AND
                {
                mAND(); 

                }
                break;
            case 19 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:87: ARRAY
                {
                mARRAY(); 

                }
                break;
            case 20 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:93: AS
                {
                mAS(); 

                }
                break;
            case 21 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:96: ASC
                {
                mASC(); 

                }
                break;
            case 22 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:100: BY
                {
                mBY(); 

                }
                break;
            case 23 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:103: CROSS
                {
                mCROSS(); 

                }
                break;
            case 24 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:109: DEFAULT
                {
                mDEFAULT(); 

                }
                break;
            case 25 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:117: DESC
                {
                mDESC(); 

                }
                break;
            case 26 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:122: DISTINCT
                {
                mDISTINCT(); 

                }
                break;
            case 27 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:131: EXISTS
                {
                mEXISTS(); 

                }
                break;
            case 28 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:138: FROM
                {
                mFROM(); 

                }
                break;
            case 29 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:143: FULL
                {
                mFULL(); 

                }
                break;
            case 30 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:148: GLOBAL
                {
                mGLOBAL(); 

                }
                break;
            case 31 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:155: GROUP
                {
                mGROUP(); 

                }
                break;
            case 32 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:161: HAVING
                {
                mHAVING(); 

                }
                break;
            case 33 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:168: ILIKE
                {
                mILIKE(); 

                }
                break;
            case 34 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:174: INTO
                {
                mINTO(); 

                }
                break;
            case 35 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:179: INNER
                {
                mINNER(); 

                }
                break;
            case 36 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:185: IS
                {
                mIS(); 

                }
                break;
            case 37 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:188: ISNULL
                {
                mISNULL(); 

                }
                break;
            case 38 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:195: JOIN
                {
                mJOIN(); 

                }
                break;
            case 39 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:200: LEFT
                {
                mLEFT(); 

                }
                break;
            case 40 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:205: LEN
                {
                mLEN(); 

                }
                break;
            case 41 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:209: LIKE
                {
                mLIKE(); 

                }
                break;
            case 42 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:214: LIMIT
                {
                mLIMIT(); 

                }
                break;
            case 43 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:220: LOCAL
                {
                mLOCAL(); 

                }
                break;
            case 44 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:226: NATURAL
                {
                mNATURAL(); 

                }
                break;
            case 45 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:234: NEW
                {
                mNEW(); 

                }
                break;
            case 46 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:238: NOT
                {
                mNOT(); 

                }
                break;
            case 47 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:242: NOTNULL
                {
                mNOTNULL(); 

                }
                break;
            case 48 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:250: NULL
                {
                mNULL(); 

                }
                break;
            case 49 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:255: OFFSET
                {
                mOFFSET(); 

                }
                break;
            case 50 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:262: OLD
                {
                mOLD(); 

                }
                break;
            case 51 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:266: ON
                {
                mON(); 

                }
                break;
            case 52 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:269: ONLY
                {
                mONLY(); 

                }
                break;
            case 53 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:274: OR
                {
                mOR(); 

                }
                break;
            case 54 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:277: ORDER
                {
                mORDER(); 

                }
                break;
            case 55 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:283: OUTER
                {
                mOUTER(); 

                }
                break;
            case 56 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:289: RIGHT
                {
                mRIGHT(); 

                }
                break;
            case 57 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:295: SELECT
                {
                mSELECT(); 

                }
                break;
            case 58 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:302: SIMILAR
                {
                mSIMILAR(); 

                }
                break;
            case 59 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:310: TABLE
                {
                mTABLE(); 

                }
                break;
            case 60 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:316: TEMP
                {
                mTEMP(); 

                }
                break;
            case 61 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:321: TEMPORARY
                {
                mTEMPORARY(); 

                }
                break;
            case 62 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:331: TO
                {
                mTO(); 

                }
                break;
            case 63 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:334: USING
                {
                mUSING(); 

                }
                break;
            case 64 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:340: VALUES
                {
                mVALUES(); 

                }
                break;
            case 65 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:347: VARIADIC
                {
                mVARIADIC(); 

                }
                break;
            case 66 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:356: WHERE
                {
                mWHERE(); 

                }
                break;
            case 67 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:362: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;
            case 68 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:373: QUOTEDSTRING
                {
                mQUOTEDSTRING(); 

                }
                break;
            case 69 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:386: QUOTEID
                {
                mQUOTEID(); 

                }
                break;
            case 70 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:394: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 71 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:401: ID
                {
                mID(); 

                }
                break;
            case 72 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:404: SL_COMMENT
                {
                mSL_COMMENT(); 

                }
                break;
            case 73 :
                // /Users/jiafu/Documents/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/sql/parser/SQLANTLR.g:1:415: ML_COMMENT
                {
                mML_COMMENT(); 

                }
                break;

        }

    }


 

}