// $ANTLR 3.0.1 /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g 2008-10-20 11:21:24

package ca.sqlpower.wabit.parser;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

public class SQLANTLRLexer extends Lexer {
    public static final int WHERE=68;
    public static final int LT=9;
    public static final int STAR=6;
    public static final int INNER=38;
    public static final int ORDER=72;
    public static final int LIMIT=75;
    public static final int LETTER=82;
    public static final int ONLY=41;
    public static final int ILIKE=62;
    public static final int NEW=30;
    public static final int LCASE=52;
    public static final int TABLE=28;
    public static final int MAX=47;
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
    public static final int OUTER=39;
    public static final int QUOTEDSTRING=67;
    public static final int GE=14;
    public static final int BY=70;
    public static final int ASC=74;
    public static final int TO=64;
    public static final int TEMP=25;
    public static final int MID=53;
    public static final int DEFAULT=18;
    public static final int VALUES=17;
    public static final int NUMBER=76;
    public static final int OLD=29;
    public static final int ON=20;
    public static final int WHITESPACE=79;
    public static final int RIGHT=37;
    public static final int HAVING=71;
    public static final int MIN=48;
    public static final int LOCAL=26;
    public static final int MINUS=5;
    public static final int Tokens=89;
    public static final int JOIN=33;
    public static final int SIMILAR=63;
    public static final int UCASE=51;
    public static final int T88=88;
    public static final int GROUP=69;
    public static final int T85=85;
    public static final int T86=86;
    public static final int T87=87;
    public static final int OR=59;
    public static final int SL_COMMENT=83;
    public static final int GT=8;
    public static final int LEN=54;
    public static final int ROUND=55;
    public static final int NATURAL=34;
    public static final int PCT=11;
    public static final int FORMAT=57;
    public static final int DIV=7;
    public static final int DESC=73;
    public static final int FROM=31;
    public static final int GLOBAL=27;
    public static final int TEMPORARY=24;
    public static final int DISTINCT=19;
    public static final int LE=15;
    public static final int FIRST=45;
    public SQLANTLRLexer() {;} 
    public SQLANTLRLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "/home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g"; }

    // $ANTLR start PLUS
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:6:6: ( '+' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:6:8: '+'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:7:7: ( '-' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:7:9: '-'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:8:6: ( '*' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:8:8: '*'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:9:5: ( '/' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:9:7: '/'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:10:4: ( '>' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:10:6: '>'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:11:4: ( '<' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:11:6: '<'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:12:5: ( '.' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:12:7: '.'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:13:5: ( '%' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:13:7: '%'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:14:4: ( '=' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:14:6: '='
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:15:5: ( '^' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:15:7: '^'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:16:4: ( '>=' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:16:6: '>='
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:17:4: ( '<-' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:17:6: '<-'
            {
            match("<-"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LE

    // $ANTLR start T85
    public final void mT85() throws RecognitionException {
        try {
            int _type = T85;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:18:5: ( ';' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:18:7: ';'
            {
            match(';'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T85

    // $ANTLR start T86
    public final void mT86() throws RecognitionException {
        try {
            int _type = T86;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:19:5: ( '(' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:19:7: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T86

    // $ANTLR start T87
    public final void mT87() throws RecognitionException {
        try {
            int _type = T87;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:20:5: ( ')' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:20:7: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T87

    // $ANTLR start T88
    public final void mT88() throws RecognitionException {
        try {
            int _type = T88;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:21:5: ( ',' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:21:7: ','
            {
            match(','); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T88

    // $ANTLR start ALL
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:314:6: ( ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:314:8: ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:315:6: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:315:8: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:316:8: ( ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'Y' | 'y' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:316:10: ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'Y' | 'y' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:317:5: ( ( 'A' | 'a' ) ( 'S' | 's' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:317:7: ( 'A' | 'a' ) ( 'S' | 's' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:318:5: ( ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:318:7: ( 'A' | 'a' ) ( 'S' | 's' ) ( 'C' | 'c' )
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

    // $ANTLR start AVG
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:319:9: ( ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'G' | 'g' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:319:11: ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'G' | 'g' )
            {
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
    // $ANTLR end AVG

    // $ANTLR start BY
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:320:9: ( ( 'B' | 'b' ) ( 'Y' | 'y' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:320:11: ( 'B' | 'b' ) ( 'Y' | 'y' )
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

    // $ANTLR start COUNT
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:321:9: ( ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:321:11: ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COUNT

    // $ANTLR start CROSS
    public final void mCROSS() throws RecognitionException {
        try {
            int _type = CROSS;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:322:9: ( ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'S' | 's' ) ( 'S' | 's' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:322:11: ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'S' | 's' ) ( 'S' | 's' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:323:9: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:323:11: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'A' | 'a' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:324:7: ( ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:324:9: ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'S' | 's' ) ( 'C' | 'c' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:325:9: ( ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:325:11: ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'C' | 'c' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:326:8: ( ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:326:10: ( 'E' | 'e' ) ( 'X' | 'x' ) ( 'I' | 'i' ) ( 'S' | 's' ) ( 'T' | 't' ) ( 'S' | 's' )
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

    // $ANTLR start FIRST
    public final void mFIRST() throws RecognitionException {
        try {
            int _type = FIRST;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:327:9: ( ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'R' | 'r' ) ( 'S' | 's' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:327:11: ( 'F' | 'f' ) ( 'I' | 'i' ) ( 'R' | 'r' ) ( 'S' | 's' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FIRST

    // $ANTLR start FORMAT
    public final void mFORMAT() throws RecognitionException {
        try {
            int _type = FORMAT;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:328:9: ( ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:328:11: ( 'F' | 'f' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
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

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FORMAT

    // $ANTLR start FROM
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:329:7: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:329:9: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:330:9: ( ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:330:11: ( 'F' | 'f' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:331:8: ( ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:331:10: ( 'G' | 'g' ) ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'B' | 'b' ) ( 'A' | 'a' ) ( 'L' | 'l' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:332:8: ( ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:332:10: ( 'G' | 'g' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'P' | 'p' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:333:9: ( ( 'H' | 'h' ) ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:333:11: ( 'H' | 'h' ) ( 'A' | 'a' ) ( 'V' | 'v' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:334:9: ( ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:334:11: ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:335:7: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'O' | 'o' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:335:9: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'O' | 'o' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:336:9: ( ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:336:11: ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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

    // $ANTLR start JOIN
    public final void mJOIN() throws RecognitionException {
        try {
            int _type = JOIN;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:337:9: ( ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:337:11: ( 'J' | 'j' ) ( 'O' | 'o' ) ( 'I' | 'i' ) ( 'N' | 'n' )
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

    // $ANTLR start LAST
    public final void mLAST() throws RecognitionException {
        try {
            int _type = LAST;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:338:9: ( ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:338:11: ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'T' | 't' )
            {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LAST

    // $ANTLR start LCASE
    public final void mLCASE() throws RecognitionException {
        try {
            int _type = LCASE;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:339:9: ( ( 'L' | 'l' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:339:11: ( 'L' | 'l' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LCASE

    // $ANTLR start LEFT
    public final void mLEFT() throws RecognitionException {
        try {
            int _type = LEFT;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:340:9: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:340:11: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'F' | 'f' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:341:9: ( ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'N' | 'n' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:341:11: ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'N' | 'n' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:342:9: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:342:11: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'K' | 'k' ) ( 'E' | 'e' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:343:8: ( ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:343:10: ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:344:7: ( ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:344:9: ( 'L' | 'l' ) ( 'O' | 'o' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'L' | 'l' )
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

    // $ANTLR start MAX
    public final void mMAX() throws RecognitionException {
        try {
            int _type = MAX;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:345:9: ( ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'X' | 'x' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:345:11: ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'X' | 'x' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
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

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
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
    // $ANTLR end MAX

    // $ANTLR start MID
    public final void mMID() throws RecognitionException {
        try {
            int _type = MID;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:346:9: ( ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'D' | 'd' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:346:11: ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'D' | 'd' )
            {
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
    // $ANTLR end MID

    // $ANTLR start MIN
    public final void mMIN() throws RecognitionException {
        try {
            int _type = MIN;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:347:9: ( ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:347:11: ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'N' | 'n' )
            {
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
    // $ANTLR end MIN

    // $ANTLR start NATURAL
    public final void mNATURAL() throws RecognitionException {
        try {
            int _type = NATURAL;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:348:9: ( ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:348:11: ( 'N' | 'n' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'U' | 'u' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'L' | 'l' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:349:9: ( ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'W' | 'w' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:349:11: ( 'N' | 'n' ) ( 'E' | 'e' ) ( 'W' | 'w' )
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

    // $ANTLR start NOW
    public final void mNOW() throws RecognitionException {
        try {
            int _type = NOW;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:350:9: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'W' | 'w' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:350:11: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'W' | 'w' )
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
    // $ANTLR end NOW

    // $ANTLR start NOT
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:351:9: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:351:11: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
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

    // $ANTLR start NVL
    public final void mNVL() throws RecognitionException {
        try {
            int _type = NVL;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:352:9: ( ( 'N' | 'n' ) ( 'V' | 'v' ) ( 'L' | 'l' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:352:11: ( 'N' | 'n' ) ( 'V' | 'v' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
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
    // $ANTLR end NVL

    // $ANTLR start OFFSET
    public final void mOFFSET() throws RecognitionException {
        try {
            int _type = OFFSET;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:353:9: ( ( 'O' | 'o' ) ( 'F' | 'f' ) ( 'F' | 'f' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:353:11: ( 'O' | 'o' ) ( 'F' | 'f' ) ( 'F' | 'f' ) ( 'S' | 's' ) ( 'E' | 'e' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:354:9: ( ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:354:11: ( 'O' | 'o' ) ( 'L' | 'l' ) ( 'D' | 'd' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:355:9: ( ( 'O' | 'o' ) ( 'N' | 'n' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:355:11: ( 'O' | 'o' ) ( 'N' | 'n' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:356:9: ( ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'L' | 'l' ) ( 'Y' | 'y' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:356:11: ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'L' | 'l' ) ( 'Y' | 'y' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:357:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:357:11: ( 'O' | 'o' ) ( 'R' | 'r' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:358:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:358:11: ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:359:9: ( ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:359:11: ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:360:9: ( ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:360:11: ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'G' | 'g' ) ( 'H' | 'h' ) ( 'T' | 't' )
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

    // $ANTLR start ROUND
    public final void mROUND() throws RecognitionException {
        try {
            int _type = ROUND;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:361:9: ( ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:361:11: ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
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
    // $ANTLR end ROUND

    // $ANTLR start SELECT
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:362:9: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:362:11: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:363:9: ( ( 'S' | 's' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'R' | 'r' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:363:11: ( 'S' | 's' ) ( 'I' | 'i' ) ( 'M' | 'm' ) ( 'I' | 'i' ) ( 'L' | 'l' ) ( 'A' | 'a' ) ( 'R' | 'r' )
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

    // $ANTLR start SUM
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:364:9: ( ( 'S' | 's' ) ( 'U' | 'u' ) ( 'M' | 'm' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:364:11: ( 'S' | 's' ) ( 'U' | 'u' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
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
    // $ANTLR end SUM

    // $ANTLR start TABLE
    public final void mTABLE() throws RecognitionException {
        try {
            int _type = TABLE;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:365:8: ( ( 'T' | 't' ) ( 'A' | 'a' ) ( 'B' | 'b' ) ( 'L' | 'l' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:365:10: ( 'T' | 't' ) ( 'A' | 'a' ) ( 'B' | 'b' ) ( 'L' | 'l' ) ( 'E' | 'e' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:366:6: ( ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:366:8: ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:367:11: ( ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:367:13: ( 'T' | 't' ) ( 'E' | 'e' ) ( 'M' | 'm' ) ( 'P' | 'p' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'Y' | 'y' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:368:9: ( ( 'T' | 't' ) ( 'O' | 'o' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:368:11: ( 'T' | 't' ) ( 'O' | 'o' )
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

    // $ANTLR start UCASE
    public final void mUCASE() throws RecognitionException {
        try {
            int _type = UCASE;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:369:9: ( ( 'U' | 'u' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:369:11: ( 'U' | 'u' ) ( 'C' | 'c' ) ( 'A' | 'a' ) ( 'S' | 's' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
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


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end UCASE

    // $ANTLR start USING
    public final void mUSING() throws RecognitionException {
        try {
            int _type = USING;
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:370:9: ( ( 'U' | 'u' ) ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:370:11: ( 'U' | 'u' ) ( 'S' | 's' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'G' | 'g' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:371:9: ( ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'U' | 'u' ) ( 'E' | 'e' ) ( 'S' | 's' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:371:11: ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'U' | 'u' ) ( 'E' | 'e' ) ( 'S' | 's' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:372:9: ( ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'C' | 'c' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:372:11: ( 'V' | 'v' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'I' | 'i' ) ( 'A' | 'a' ) ( 'D' | 'd' ) ( 'I' | 'i' ) ( 'C' | 'c' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:373:8: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:373:10: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:376:12: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+ )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:376:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:376:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
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
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:14: ( ( '\\'' | '$$' ) ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )* ( '\\'' | '$$' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:17: ( '\\'' | '$$' ) ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )* ( '\\'' | '$$' )
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:17: ( '\\'' | '$$' )
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
                    new NoViableAltException("377:17: ( '\\'' | '$$' )", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:18: '\\''
                    {
                    match('\''); 

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:25: '$$'
                    {
                    match("$$"); 


                    }
                    break;

            }

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:31: ( options {greedy=false; } : (~ ( '\\'' | '$$' ) ) )*
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
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:58: (~ ( '\\'' | '$$' ) )
            	    {
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:58: (~ ( '\\'' | '$$' ) )
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:60: ~ ( '\\'' | '$$' )
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

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:81: ( '\\'' | '$$' )
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
                    new NoViableAltException("377:81: ( '\\'' | '$$' )", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:82: '\\''
                    {
                    match('\''); 

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:377:89: '$$'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:378:9: ( ( '[' ID ']' | '\"' ID '\"' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:378:11: ( '[' ID ']' | '\"' ID '\"' )
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:378:11: ( '[' ID ']' | '\"' ID '\"' )
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
                    new NoViableAltException("378:11: ( '[' ID ']' | '\"' ID '\"' )", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:378:12: '[' ID ']'
                    {
                    match('['); 
                    mID(); 
                    match(']'); 

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:378:21: '\"' ID '\"'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:379:8: ( ( DIGIT )+ )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:379:10: ( DIGIT )+
            {
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:379:10: ( DIGIT )+
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
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:379:11: DIGIT
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:9: ( LETTER ( LETTER | NUMBER | '_' )* )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:11: LETTER ( LETTER | NUMBER | '_' )*
            {
            mLETTER(); 
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:18: ( LETTER | NUMBER | '_' )*
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
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:20: LETTER
            	    {
            	    mLETTER(); 

            	    }
            	    break;
            	case 2 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:29: NUMBER
            	    {
            	    mNUMBER(); 

            	    }
            	    break;
            	case 3 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:380:38: '_'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:383:16: ( '0' .. '9' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:383:18: '0' .. '9'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:384:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:384:19: ( 'a' .. 'z' | 'A' .. 'Z' )
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:12: ( '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | EOF | '\\r' ( '\\n' )? ) )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:14: '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | EOF | '\\r' ( '\\n' )? )
            {
            match("--"); 

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:19: (~ ( '\\n' | '\\r' ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='\u0000' && LA8_0<='\t')||(LA8_0>='\u000B' && LA8_0<='\f')||(LA8_0>='\u000E' && LA8_0<='\uFFFE')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:20: ~ ( '\\n' | '\\r' )
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

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:35: ( '\\n' | EOF | '\\r' ( '\\n' )? )
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
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:36: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 2 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:42: EOF
                    {
                    match(EOF); 

                    }
                    break;
                case 3 :
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:47: '\\r' ( '\\n' )?
                    {
                    match('\r'); 
                    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:51: ( '\\n' )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='\n') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:387:52: '\\n'
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
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:388:17: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:388:19: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:388:24: ( options {greedy=false; } : . )*
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
            	    // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:388:51: .
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
        // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:8: ( PLUS | MINUS | STAR | DIV | GT | LT | DOT | PCT | EQ | HAT | GE | LE | T85 | T86 | T87 | T88 | ALL | AND | ARRAY | AS | ASC | AVG | BY | COUNT | CROSS | DEFAULT | DESC | DISTINCT | EXISTS | FIRST | FORMAT | FROM | FULL | GLOBAL | GROUP | HAVING | ILIKE | INTO | INNER | JOIN | LAST | LCASE | LEFT | LEN | LIKE | LIMIT | LOCAL | MAX | MID | MIN | NATURAL | NEW | NOW | NOT | NVL | OFFSET | OLD | ON | ONLY | OR | ORDER | OUTER | RIGHT | ROUND | SELECT | SIMILAR | SUM | TABLE | TEMP | TEMPORARY | TO | UCASE | USING | VALUES | VARIADIC | WHERE | WHITESPACE | QUOTEDSTRING | QUOTEID | NUMBER | ID | SL_COMMENT | ML_COMMENT )
        int alt12=83;
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
                alt12=82;
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
                alt12=83;
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

            if ( (LA12_6=='-') ) {
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
        case ';':
            {
            alt12=13;
            }
            break;
        case '(':
            {
            alt12=14;
            }
            break;
        case ')':
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
            case 'N':
            case 'n':
                {
                int LA12_48 = input.LA(3);

                if ( (LA12_48=='D'||LA12_48=='d') ) {
                    int LA12_97 = input.LA(4);

                    if ( ((LA12_97>='0' && LA12_97<='9')||(LA12_97>='A' && LA12_97<='Z')||LA12_97=='_'||(LA12_97>='a' && LA12_97<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=18;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'R':
            case 'r':
                {
                int LA12_49 = input.LA(3);

                if ( (LA12_49=='R'||LA12_49=='r') ) {
                    int LA12_98 = input.LA(4);

                    if ( (LA12_98=='A'||LA12_98=='a') ) {
                        int LA12_157 = input.LA(5);

                        if ( (LA12_157=='Y'||LA12_157=='y') ) {
                            int LA12_210 = input.LA(6);

                            if ( ((LA12_210>='0' && LA12_210<='9')||(LA12_210>='A' && LA12_210<='Z')||LA12_210=='_'||(LA12_210>='a' && LA12_210<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=19;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'S':
            case 's':
                {
                switch ( input.LA(3) ) {
                case 'C':
                case 'c':
                    {
                    int LA12_99 = input.LA(4);

                    if ( ((LA12_99>='0' && LA12_99<='9')||(LA12_99>='A' && LA12_99<='Z')||LA12_99=='_'||(LA12_99>='a' && LA12_99<='z')) ) {
                        alt12=81;
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
                    alt12=81;
                    }
                    break;
                default:
                    alt12=20;}

                }
                break;
            case 'V':
            case 'v':
                {
                int LA12_51 = input.LA(3);

                if ( (LA12_51=='G'||LA12_51=='g') ) {
                    int LA12_101 = input.LA(4);

                    if ( ((LA12_101>='0' && LA12_101<='9')||(LA12_101>='A' && LA12_101<='Z')||LA12_101=='_'||(LA12_101>='a' && LA12_101<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=22;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_52 = input.LA(3);

                if ( (LA12_52=='L'||LA12_52=='l') ) {
                    int LA12_102 = input.LA(4);

                    if ( ((LA12_102>='0' && LA12_102<='9')||(LA12_102>='A' && LA12_102<='Z')||LA12_102=='_'||(LA12_102>='a' && LA12_102<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=17;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'B':
        case 'b':
            {
            int LA12_16 = input.LA(2);

            if ( (LA12_16=='Y'||LA12_16=='y') ) {
                int LA12_53 = input.LA(3);

                if ( ((LA12_53>='0' && LA12_53<='9')||(LA12_53>='A' && LA12_53<='Z')||LA12_53=='_'||(LA12_53>='a' && LA12_53<='z')) ) {
                    alt12=81;
                }
                else {
                    alt12=23;}
            }
            else {
                alt12=81;}
            }
            break;
        case 'C':
        case 'c':
            {
            switch ( input.LA(2) ) {
            case 'O':
            case 'o':
                {
                int LA12_54 = input.LA(3);

                if ( (LA12_54=='U'||LA12_54=='u') ) {
                    int LA12_104 = input.LA(4);

                    if ( (LA12_104=='N'||LA12_104=='n') ) {
                        int LA12_161 = input.LA(5);

                        if ( (LA12_161=='T'||LA12_161=='t') ) {
                            int LA12_211 = input.LA(6);

                            if ( ((LA12_211>='0' && LA12_211<='9')||(LA12_211>='A' && LA12_211<='Z')||LA12_211=='_'||(LA12_211>='a' && LA12_211<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=24;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'R':
            case 'r':
                {
                int LA12_55 = input.LA(3);

                if ( (LA12_55=='O'||LA12_55=='o') ) {
                    int LA12_105 = input.LA(4);

                    if ( (LA12_105=='S'||LA12_105=='s') ) {
                        int LA12_162 = input.LA(5);

                        if ( (LA12_162=='S'||LA12_162=='s') ) {
                            int LA12_212 = input.LA(6);

                            if ( ((LA12_212>='0' && LA12_212<='9')||(LA12_212>='A' && LA12_212<='Z')||LA12_212=='_'||(LA12_212>='a' && LA12_212<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=25;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

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
                    int LA12_106 = input.LA(4);

                    if ( (LA12_106=='A'||LA12_106=='a') ) {
                        int LA12_163 = input.LA(5);

                        if ( (LA12_163=='U'||LA12_163=='u') ) {
                            int LA12_213 = input.LA(6);

                            if ( (LA12_213=='L'||LA12_213=='l') ) {
                                int LA12_254 = input.LA(7);

                                if ( (LA12_254=='T'||LA12_254=='t') ) {
                                    int LA12_282 = input.LA(8);

                                    if ( ((LA12_282>='0' && LA12_282<='9')||(LA12_282>='A' && LA12_282<='Z')||LA12_282=='_'||(LA12_282>='a' && LA12_282<='z')) ) {
                                        alt12=81;
                                    }
                                    else {
                                        alt12=26;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                case 'S':
                case 's':
                    {
                    int LA12_107 = input.LA(4);

                    if ( (LA12_107=='C'||LA12_107=='c') ) {
                        int LA12_164 = input.LA(5);

                        if ( ((LA12_164>='0' && LA12_164<='9')||(LA12_164>='A' && LA12_164<='Z')||LA12_164=='_'||(LA12_164>='a' && LA12_164<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=27;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'I':
            case 'i':
                {
                int LA12_57 = input.LA(3);

                if ( (LA12_57=='S'||LA12_57=='s') ) {
                    int LA12_108 = input.LA(4);

                    if ( (LA12_108=='T'||LA12_108=='t') ) {
                        int LA12_165 = input.LA(5);

                        if ( (LA12_165=='I'||LA12_165=='i') ) {
                            int LA12_215 = input.LA(6);

                            if ( (LA12_215=='N'||LA12_215=='n') ) {
                                int LA12_255 = input.LA(7);

                                if ( (LA12_255=='C'||LA12_255=='c') ) {
                                    int LA12_283 = input.LA(8);

                                    if ( (LA12_283=='T'||LA12_283=='t') ) {
                                        int LA12_296 = input.LA(9);

                                        if ( ((LA12_296>='0' && LA12_296<='9')||(LA12_296>='A' && LA12_296<='Z')||LA12_296=='_'||(LA12_296>='a' && LA12_296<='z')) ) {
                                            alt12=81;
                                        }
                                        else {
                                            alt12=28;}
                                    }
                                    else {
                                        alt12=81;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'E':
        case 'e':
            {
            int LA12_19 = input.LA(2);

            if ( (LA12_19=='X'||LA12_19=='x') ) {
                int LA12_58 = input.LA(3);

                if ( (LA12_58=='I'||LA12_58=='i') ) {
                    int LA12_109 = input.LA(4);

                    if ( (LA12_109=='S'||LA12_109=='s') ) {
                        int LA12_166 = input.LA(5);

                        if ( (LA12_166=='T'||LA12_166=='t') ) {
                            int LA12_216 = input.LA(6);

                            if ( (LA12_216=='S'||LA12_216=='s') ) {
                                int LA12_256 = input.LA(7);

                                if ( ((LA12_256>='0' && LA12_256<='9')||(LA12_256>='A' && LA12_256<='Z')||LA12_256=='_'||(LA12_256>='a' && LA12_256<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=29;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
            }
            else {
                alt12=81;}
            }
            break;
        case 'F':
        case 'f':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                int LA12_59 = input.LA(3);

                if ( (LA12_59=='R'||LA12_59=='r') ) {
                    int LA12_110 = input.LA(4);

                    if ( (LA12_110=='S'||LA12_110=='s') ) {
                        int LA12_167 = input.LA(5);

                        if ( (LA12_167=='T'||LA12_167=='t') ) {
                            int LA12_217 = input.LA(6);

                            if ( ((LA12_217>='0' && LA12_217<='9')||(LA12_217>='A' && LA12_217<='Z')||LA12_217=='_'||(LA12_217>='a' && LA12_217<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=30;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'R':
            case 'r':
                {
                int LA12_60 = input.LA(3);

                if ( (LA12_60=='O'||LA12_60=='o') ) {
                    int LA12_111 = input.LA(4);

                    if ( (LA12_111=='M'||LA12_111=='m') ) {
                        int LA12_168 = input.LA(5);

                        if ( ((LA12_168>='0' && LA12_168<='9')||(LA12_168>='A' && LA12_168<='Z')||LA12_168=='_'||(LA12_168>='a' && LA12_168<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=32;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'O':
            case 'o':
                {
                int LA12_61 = input.LA(3);

                if ( (LA12_61=='R'||LA12_61=='r') ) {
                    int LA12_112 = input.LA(4);

                    if ( (LA12_112=='M'||LA12_112=='m') ) {
                        int LA12_169 = input.LA(5);

                        if ( (LA12_169=='A'||LA12_169=='a') ) {
                            int LA12_219 = input.LA(6);

                            if ( (LA12_219=='T'||LA12_219=='t') ) {
                                int LA12_258 = input.LA(7);

                                if ( ((LA12_258>='0' && LA12_258<='9')||(LA12_258>='A' && LA12_258<='Z')||LA12_258=='_'||(LA12_258>='a' && LA12_258<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=31;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'U':
            case 'u':
                {
                int LA12_62 = input.LA(3);

                if ( (LA12_62=='L'||LA12_62=='l') ) {
                    int LA12_113 = input.LA(4);

                    if ( (LA12_113=='L'||LA12_113=='l') ) {
                        int LA12_170 = input.LA(5);

                        if ( ((LA12_170>='0' && LA12_170<='9')||(LA12_170>='A' && LA12_170<='Z')||LA12_170=='_'||(LA12_170>='a' && LA12_170<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=33;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'G':
        case 'g':
            {
            switch ( input.LA(2) ) {
            case 'L':
            case 'l':
                {
                int LA12_63 = input.LA(3);

                if ( (LA12_63=='O'||LA12_63=='o') ) {
                    int LA12_114 = input.LA(4);

                    if ( (LA12_114=='B'||LA12_114=='b') ) {
                        int LA12_171 = input.LA(5);

                        if ( (LA12_171=='A'||LA12_171=='a') ) {
                            int LA12_221 = input.LA(6);

                            if ( (LA12_221=='L'||LA12_221=='l') ) {
                                int LA12_259 = input.LA(7);

                                if ( ((LA12_259>='0' && LA12_259<='9')||(LA12_259>='A' && LA12_259<='Z')||LA12_259=='_'||(LA12_259>='a' && LA12_259<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=34;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'R':
            case 'r':
                {
                int LA12_64 = input.LA(3);

                if ( (LA12_64=='O'||LA12_64=='o') ) {
                    int LA12_115 = input.LA(4);

                    if ( (LA12_115=='U'||LA12_115=='u') ) {
                        int LA12_172 = input.LA(5);

                        if ( (LA12_172=='P'||LA12_172=='p') ) {
                            int LA12_222 = input.LA(6);

                            if ( ((LA12_222>='0' && LA12_222<='9')||(LA12_222>='A' && LA12_222<='Z')||LA12_222=='_'||(LA12_222>='a' && LA12_222<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=35;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'H':
        case 'h':
            {
            int LA12_22 = input.LA(2);

            if ( (LA12_22=='A'||LA12_22=='a') ) {
                int LA12_65 = input.LA(3);

                if ( (LA12_65=='V'||LA12_65=='v') ) {
                    int LA12_116 = input.LA(4);

                    if ( (LA12_116=='I'||LA12_116=='i') ) {
                        int LA12_173 = input.LA(5);

                        if ( (LA12_173=='N'||LA12_173=='n') ) {
                            int LA12_223 = input.LA(6);

                            if ( (LA12_223=='G'||LA12_223=='g') ) {
                                int LA12_261 = input.LA(7);

                                if ( ((LA12_261>='0' && LA12_261<='9')||(LA12_261>='A' && LA12_261<='Z')||LA12_261=='_'||(LA12_261>='a' && LA12_261<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=36;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
            }
            else {
                alt12=81;}
            }
            break;
        case 'I':
        case 'i':
            {
            switch ( input.LA(2) ) {
            case 'N':
            case 'n':
                {
                switch ( input.LA(3) ) {
                case 'T':
                case 't':
                    {
                    int LA12_117 = input.LA(4);

                    if ( (LA12_117=='O'||LA12_117=='o') ) {
                        int LA12_174 = input.LA(5);

                        if ( ((LA12_174>='0' && LA12_174<='9')||(LA12_174>='A' && LA12_174<='Z')||LA12_174=='_'||(LA12_174>='a' && LA12_174<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=38;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                case 'N':
                case 'n':
                    {
                    int LA12_118 = input.LA(4);

                    if ( (LA12_118=='E'||LA12_118=='e') ) {
                        int LA12_175 = input.LA(5);

                        if ( (LA12_175=='R'||LA12_175=='r') ) {
                            int LA12_225 = input.LA(6);

                            if ( ((LA12_225>='0' && LA12_225<='9')||(LA12_225>='A' && LA12_225<='Z')||LA12_225=='_'||(LA12_225>='a' && LA12_225<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=39;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_67 = input.LA(3);

                if ( (LA12_67=='I'||LA12_67=='i') ) {
                    int LA12_119 = input.LA(4);

                    if ( (LA12_119=='K'||LA12_119=='k') ) {
                        int LA12_176 = input.LA(5);

                        if ( (LA12_176=='E'||LA12_176=='e') ) {
                            int LA12_226 = input.LA(6);

                            if ( ((LA12_226>='0' && LA12_226<='9')||(LA12_226>='A' && LA12_226<='Z')||LA12_226=='_'||(LA12_226>='a' && LA12_226<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=37;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'J':
        case 'j':
            {
            int LA12_24 = input.LA(2);

            if ( (LA12_24=='O'||LA12_24=='o') ) {
                int LA12_68 = input.LA(3);

                if ( (LA12_68=='I'||LA12_68=='i') ) {
                    int LA12_120 = input.LA(4);

                    if ( (LA12_120=='N'||LA12_120=='n') ) {
                        int LA12_177 = input.LA(5);

                        if ( ((LA12_177>='0' && LA12_177<='9')||(LA12_177>='A' && LA12_177<='Z')||LA12_177=='_'||(LA12_177>='a' && LA12_177<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=40;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
            }
            else {
                alt12=81;}
            }
            break;
        case 'L':
        case 'l':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                switch ( input.LA(3) ) {
                case 'K':
                case 'k':
                    {
                    int LA12_121 = input.LA(4);

                    if ( (LA12_121=='E'||LA12_121=='e') ) {
                        int LA12_178 = input.LA(5);

                        if ( ((LA12_178>='0' && LA12_178<='9')||(LA12_178>='A' && LA12_178<='Z')||LA12_178=='_'||(LA12_178>='a' && LA12_178<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=45;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                case 'M':
                case 'm':
                    {
                    int LA12_122 = input.LA(4);

                    if ( (LA12_122=='I'||LA12_122=='i') ) {
                        int LA12_179 = input.LA(5);

                        if ( (LA12_179=='T'||LA12_179=='t') ) {
                            int LA12_229 = input.LA(6);

                            if ( ((LA12_229>='0' && LA12_229<='9')||(LA12_229>='A' && LA12_229<='Z')||LA12_229=='_'||(LA12_229>='a' && LA12_229<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=46;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'O':
            case 'o':
                {
                int LA12_70 = input.LA(3);

                if ( (LA12_70=='C'||LA12_70=='c') ) {
                    int LA12_123 = input.LA(4);

                    if ( (LA12_123=='A'||LA12_123=='a') ) {
                        int LA12_180 = input.LA(5);

                        if ( (LA12_180=='L'||LA12_180=='l') ) {
                            int LA12_230 = input.LA(6);

                            if ( ((LA12_230>='0' && LA12_230<='9')||(LA12_230>='A' && LA12_230<='Z')||LA12_230=='_'||(LA12_230>='a' && LA12_230<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=47;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'E':
            case 'e':
                {
                switch ( input.LA(3) ) {
                case 'N':
                case 'n':
                    {
                    int LA12_124 = input.LA(4);

                    if ( ((LA12_124>='0' && LA12_124<='9')||(LA12_124>='A' && LA12_124<='Z')||LA12_124=='_'||(LA12_124>='a' && LA12_124<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=44;}
                    }
                    break;
                case 'F':
                case 'f':
                    {
                    int LA12_125 = input.LA(4);

                    if ( (LA12_125=='T'||LA12_125=='t') ) {
                        int LA12_182 = input.LA(5);

                        if ( ((LA12_182>='0' && LA12_182<='9')||(LA12_182>='A' && LA12_182<='Z')||LA12_182=='_'||(LA12_182>='a' && LA12_182<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=43;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'C':
            case 'c':
                {
                int LA12_72 = input.LA(3);

                if ( (LA12_72=='A'||LA12_72=='a') ) {
                    int LA12_126 = input.LA(4);

                    if ( (LA12_126=='S'||LA12_126=='s') ) {
                        int LA12_183 = input.LA(5);

                        if ( (LA12_183=='E'||LA12_183=='e') ) {
                            int LA12_232 = input.LA(6);

                            if ( ((LA12_232>='0' && LA12_232<='9')||(LA12_232>='A' && LA12_232<='Z')||LA12_232=='_'||(LA12_232>='a' && LA12_232<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=42;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'A':
            case 'a':
                {
                int LA12_73 = input.LA(3);

                if ( (LA12_73=='S'||LA12_73=='s') ) {
                    int LA12_127 = input.LA(4);

                    if ( (LA12_127=='T'||LA12_127=='t') ) {
                        int LA12_184 = input.LA(5);

                        if ( ((LA12_184>='0' && LA12_184<='9')||(LA12_184>='A' && LA12_184<='Z')||LA12_184=='_'||(LA12_184>='a' && LA12_184<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=41;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'M':
        case 'm':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                switch ( input.LA(3) ) {
                case 'N':
                case 'n':
                    {
                    int LA12_128 = input.LA(4);

                    if ( ((LA12_128>='0' && LA12_128<='9')||(LA12_128>='A' && LA12_128<='Z')||LA12_128=='_'||(LA12_128>='a' && LA12_128<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=50;}
                    }
                    break;
                case 'D':
                case 'd':
                    {
                    int LA12_129 = input.LA(4);

                    if ( ((LA12_129>='0' && LA12_129<='9')||(LA12_129>='A' && LA12_129<='Z')||LA12_129=='_'||(LA12_129>='a' && LA12_129<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=49;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'A':
            case 'a':
                {
                int LA12_75 = input.LA(3);

                if ( (LA12_75=='X'||LA12_75=='x') ) {
                    int LA12_130 = input.LA(4);

                    if ( ((LA12_130>='0' && LA12_130<='9')||(LA12_130>='A' && LA12_130<='Z')||LA12_130=='_'||(LA12_130>='a' && LA12_130<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=48;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'N':
        case 'n':
            {
            switch ( input.LA(2) ) {
            case 'A':
            case 'a':
                {
                int LA12_76 = input.LA(3);

                if ( (LA12_76=='T'||LA12_76=='t') ) {
                    int LA12_131 = input.LA(4);

                    if ( (LA12_131=='U'||LA12_131=='u') ) {
                        int LA12_188 = input.LA(5);

                        if ( (LA12_188=='R'||LA12_188=='r') ) {
                            int LA12_234 = input.LA(6);

                            if ( (LA12_234=='A'||LA12_234=='a') ) {
                                int LA12_267 = input.LA(7);

                                if ( (LA12_267=='L'||LA12_267=='l') ) {
                                    int LA12_288 = input.LA(8);

                                    if ( ((LA12_288>='0' && LA12_288<='9')||(LA12_288>='A' && LA12_288<='Z')||LA12_288=='_'||(LA12_288>='a' && LA12_288<='z')) ) {
                                        alt12=81;
                                    }
                                    else {
                                        alt12=51;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_77 = input.LA(3);

                if ( (LA12_77=='W'||LA12_77=='w') ) {
                    int LA12_132 = input.LA(4);

                    if ( ((LA12_132>='0' && LA12_132<='9')||(LA12_132>='A' && LA12_132<='Z')||LA12_132=='_'||(LA12_132>='a' && LA12_132<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=52;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'O':
            case 'o':
                {
                switch ( input.LA(3) ) {
                case 'W':
                case 'w':
                    {
                    int LA12_133 = input.LA(4);

                    if ( ((LA12_133>='0' && LA12_133<='9')||(LA12_133>='A' && LA12_133<='Z')||LA12_133=='_'||(LA12_133>='a' && LA12_133<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=53;}
                    }
                    break;
                case 'T':
                case 't':
                    {
                    int LA12_134 = input.LA(4);

                    if ( ((LA12_134>='0' && LA12_134<='9')||(LA12_134>='A' && LA12_134<='Z')||LA12_134=='_'||(LA12_134>='a' && LA12_134<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=54;}
                    }
                    break;
                default:
                    alt12=81;}

                }
                break;
            case 'V':
            case 'v':
                {
                int LA12_79 = input.LA(3);

                if ( (LA12_79=='L'||LA12_79=='l') ) {
                    int LA12_135 = input.LA(4);

                    if ( ((LA12_135>='0' && LA12_135<='9')||(LA12_135>='A' && LA12_135<='Z')||LA12_135=='_'||(LA12_135>='a' && LA12_135<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=55;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'O':
        case 'o':
            {
            switch ( input.LA(2) ) {
            case 'U':
            case 'u':
                {
                int LA12_80 = input.LA(3);

                if ( (LA12_80=='T'||LA12_80=='t') ) {
                    int LA12_136 = input.LA(4);

                    if ( (LA12_136=='E'||LA12_136=='e') ) {
                        int LA12_193 = input.LA(5);

                        if ( (LA12_193=='R'||LA12_193=='r') ) {
                            int LA12_235 = input.LA(6);

                            if ( ((LA12_235>='0' && LA12_235<='9')||(LA12_235>='A' && LA12_235<='Z')||LA12_235=='_'||(LA12_235>='a' && LA12_235<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=62;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'N':
            case 'n':
                {
                switch ( input.LA(3) ) {
                case 'L':
                case 'l':
                    {
                    int LA12_137 = input.LA(4);

                    if ( (LA12_137=='Y'||LA12_137=='y') ) {
                        int LA12_194 = input.LA(5);

                        if ( ((LA12_194>='0' && LA12_194<='9')||(LA12_194>='A' && LA12_194<='Z')||LA12_194=='_'||(LA12_194>='a' && LA12_194<='z')) ) {
                            alt12=81;
                        }
                        else {
                            alt12=59;}
                    }
                    else {
                        alt12=81;}
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
                    alt12=81;
                    }
                    break;
                default:
                    alt12=58;}

                }
                break;
            case 'R':
            case 'r':
                {
                switch ( input.LA(3) ) {
                case 'D':
                case 'd':
                    {
                    int LA12_139 = input.LA(4);

                    if ( (LA12_139=='E'||LA12_139=='e') ) {
                        int LA12_195 = input.LA(5);

                        if ( (LA12_195=='R'||LA12_195=='r') ) {
                            int LA12_237 = input.LA(6);

                            if ( ((LA12_237>='0' && LA12_237<='9')||(LA12_237>='A' && LA12_237<='Z')||LA12_237=='_'||(LA12_237>='a' && LA12_237<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=61;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
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
                    alt12=81;
                    }
                    break;
                default:
                    alt12=60;}

                }
                break;
            case 'F':
            case 'f':
                {
                int LA12_83 = input.LA(3);

                if ( (LA12_83=='F'||LA12_83=='f') ) {
                    int LA12_141 = input.LA(4);

                    if ( (LA12_141=='S'||LA12_141=='s') ) {
                        int LA12_196 = input.LA(5);

                        if ( (LA12_196=='E'||LA12_196=='e') ) {
                            int LA12_238 = input.LA(6);

                            if ( (LA12_238=='T'||LA12_238=='t') ) {
                                int LA12_270 = input.LA(7);

                                if ( ((LA12_270>='0' && LA12_270<='9')||(LA12_270>='A' && LA12_270<='Z')||LA12_270=='_'||(LA12_270>='a' && LA12_270<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=56;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'L':
            case 'l':
                {
                int LA12_84 = input.LA(3);

                if ( (LA12_84=='D'||LA12_84=='d') ) {
                    int LA12_142 = input.LA(4);

                    if ( ((LA12_142>='0' && LA12_142<='9')||(LA12_142>='A' && LA12_142<='Z')||LA12_142=='_'||(LA12_142>='a' && LA12_142<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=57;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'R':
        case 'r':
            {
            switch ( input.LA(2) ) {
            case 'I':
            case 'i':
                {
                int LA12_85 = input.LA(3);

                if ( (LA12_85=='G'||LA12_85=='g') ) {
                    int LA12_143 = input.LA(4);

                    if ( (LA12_143=='H'||LA12_143=='h') ) {
                        int LA12_198 = input.LA(5);

                        if ( (LA12_198=='T'||LA12_198=='t') ) {
                            int LA12_239 = input.LA(6);

                            if ( ((LA12_239>='0' && LA12_239<='9')||(LA12_239>='A' && LA12_239<='Z')||LA12_239=='_'||(LA12_239>='a' && LA12_239<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=63;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'O':
            case 'o':
                {
                int LA12_86 = input.LA(3);

                if ( (LA12_86=='U'||LA12_86=='u') ) {
                    int LA12_144 = input.LA(4);

                    if ( (LA12_144=='N'||LA12_144=='n') ) {
                        int LA12_199 = input.LA(5);

                        if ( (LA12_199=='D'||LA12_199=='d') ) {
                            int LA12_240 = input.LA(6);

                            if ( ((LA12_240>='0' && LA12_240<='9')||(LA12_240>='A' && LA12_240<='Z')||LA12_240=='_'||(LA12_240>='a' && LA12_240<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=64;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'S':
        case 's':
            {
            switch ( input.LA(2) ) {
            case 'U':
            case 'u':
                {
                int LA12_87 = input.LA(3);

                if ( (LA12_87=='M'||LA12_87=='m') ) {
                    int LA12_145 = input.LA(4);

                    if ( ((LA12_145>='0' && LA12_145<='9')||(LA12_145>='A' && LA12_145<='Z')||LA12_145=='_'||(LA12_145>='a' && LA12_145<='z')) ) {
                        alt12=81;
                    }
                    else {
                        alt12=67;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'I':
            case 'i':
                {
                int LA12_88 = input.LA(3);

                if ( (LA12_88=='M'||LA12_88=='m') ) {
                    int LA12_146 = input.LA(4);

                    if ( (LA12_146=='I'||LA12_146=='i') ) {
                        int LA12_201 = input.LA(5);

                        if ( (LA12_201=='L'||LA12_201=='l') ) {
                            int LA12_241 = input.LA(6);

                            if ( (LA12_241=='A'||LA12_241=='a') ) {
                                int LA12_273 = input.LA(7);

                                if ( (LA12_273=='R'||LA12_273=='r') ) {
                                    int LA12_290 = input.LA(8);

                                    if ( ((LA12_290>='0' && LA12_290<='9')||(LA12_290>='A' && LA12_290<='Z')||LA12_290=='_'||(LA12_290>='a' && LA12_290<='z')) ) {
                                        alt12=81;
                                    }
                                    else {
                                        alt12=66;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_89 = input.LA(3);

                if ( (LA12_89=='L'||LA12_89=='l') ) {
                    int LA12_147 = input.LA(4);

                    if ( (LA12_147=='E'||LA12_147=='e') ) {
                        int LA12_202 = input.LA(5);

                        if ( (LA12_202=='C'||LA12_202=='c') ) {
                            int LA12_242 = input.LA(6);

                            if ( (LA12_242=='T'||LA12_242=='t') ) {
                                int LA12_274 = input.LA(7);

                                if ( ((LA12_274>='0' && LA12_274<='9')||(LA12_274>='A' && LA12_274<='Z')||LA12_274=='_'||(LA12_274>='a' && LA12_274<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=65;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'T':
        case 't':
            {
            switch ( input.LA(2) ) {
            case 'O':
            case 'o':
                {
                int LA12_90 = input.LA(3);

                if ( ((LA12_90>='0' && LA12_90<='9')||(LA12_90>='A' && LA12_90<='Z')||LA12_90=='_'||(LA12_90>='a' && LA12_90<='z')) ) {
                    alt12=81;
                }
                else {
                    alt12=71;}
                }
                break;
            case 'E':
            case 'e':
                {
                int LA12_91 = input.LA(3);

                if ( (LA12_91=='M'||LA12_91=='m') ) {
                    int LA12_149 = input.LA(4);

                    if ( (LA12_149=='P'||LA12_149=='p') ) {
                        switch ( input.LA(5) ) {
                        case 'O':
                        case 'o':
                            {
                            int LA12_243 = input.LA(6);

                            if ( (LA12_243=='R'||LA12_243=='r') ) {
                                int LA12_275 = input.LA(7);

                                if ( (LA12_275=='A'||LA12_275=='a') ) {
                                    int LA12_292 = input.LA(8);

                                    if ( (LA12_292=='R'||LA12_292=='r') ) {
                                        int LA12_299 = input.LA(9);

                                        if ( (LA12_299=='Y'||LA12_299=='y') ) {
                                            int LA12_302 = input.LA(10);

                                            if ( ((LA12_302>='0' && LA12_302<='9')||(LA12_302>='A' && LA12_302<='Z')||LA12_302=='_'||(LA12_302>='a' && LA12_302<='z')) ) {
                                                alt12=81;
                                            }
                                            else {
                                                alt12=70;}
                                        }
                                        else {
                                            alt12=81;}
                                    }
                                    else {
                                        alt12=81;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
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
                            alt12=81;
                            }
                            break;
                        default:
                            alt12=69;}

                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'A':
            case 'a':
                {
                int LA12_92 = input.LA(3);

                if ( (LA12_92=='B'||LA12_92=='b') ) {
                    int LA12_150 = input.LA(4);

                    if ( (LA12_150=='L'||LA12_150=='l') ) {
                        int LA12_204 = input.LA(5);

                        if ( (LA12_204=='E'||LA12_204=='e') ) {
                            int LA12_245 = input.LA(6);

                            if ( ((LA12_245>='0' && LA12_245<='9')||(LA12_245>='A' && LA12_245<='Z')||LA12_245=='_'||(LA12_245>='a' && LA12_245<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=68;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'U':
        case 'u':
            {
            switch ( input.LA(2) ) {
            case 'C':
            case 'c':
                {
                int LA12_93 = input.LA(3);

                if ( (LA12_93=='A'||LA12_93=='a') ) {
                    int LA12_151 = input.LA(4);

                    if ( (LA12_151=='S'||LA12_151=='s') ) {
                        int LA12_205 = input.LA(5);

                        if ( (LA12_205=='E'||LA12_205=='e') ) {
                            int LA12_246 = input.LA(6);

                            if ( ((LA12_246>='0' && LA12_246<='9')||(LA12_246>='A' && LA12_246<='Z')||LA12_246=='_'||(LA12_246>='a' && LA12_246<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=72;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            case 'S':
            case 's':
                {
                int LA12_94 = input.LA(3);

                if ( (LA12_94=='I'||LA12_94=='i') ) {
                    int LA12_152 = input.LA(4);

                    if ( (LA12_152=='N'||LA12_152=='n') ) {
                        int LA12_206 = input.LA(5);

                        if ( (LA12_206=='G'||LA12_206=='g') ) {
                            int LA12_247 = input.LA(6);

                            if ( ((LA12_247>='0' && LA12_247<='9')||(LA12_247>='A' && LA12_247<='Z')||LA12_247=='_'||(LA12_247>='a' && LA12_247<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=73;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
                }
                break;
            default:
                alt12=81;}

            }
            break;
        case 'V':
        case 'v':
            {
            int LA12_33 = input.LA(2);

            if ( (LA12_33=='A'||LA12_33=='a') ) {
                switch ( input.LA(3) ) {
                case 'R':
                case 'r':
                    {
                    int LA12_153 = input.LA(4);

                    if ( (LA12_153=='I'||LA12_153=='i') ) {
                        int LA12_207 = input.LA(5);

                        if ( (LA12_207=='A'||LA12_207=='a') ) {
                            int LA12_248 = input.LA(6);

                            if ( (LA12_248=='D'||LA12_248=='d') ) {
                                int LA12_279 = input.LA(7);

                                if ( (LA12_279=='I'||LA12_279=='i') ) {
                                    int LA12_293 = input.LA(8);

                                    if ( (LA12_293=='C'||LA12_293=='c') ) {
                                        int LA12_300 = input.LA(9);

                                        if ( ((LA12_300>='0' && LA12_300<='9')||(LA12_300>='A' && LA12_300<='Z')||LA12_300=='_'||(LA12_300>='a' && LA12_300<='z')) ) {
                                            alt12=81;
                                        }
                                        else {
                                            alt12=75;}
                                    }
                                    else {
                                        alt12=81;}
                                }
                                else {
                                    alt12=81;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                case 'L':
                case 'l':
                    {
                    int LA12_154 = input.LA(4);

                    if ( (LA12_154=='U'||LA12_154=='u') ) {
                        int LA12_208 = input.LA(5);

                        if ( (LA12_208=='E'||LA12_208=='e') ) {
                            int LA12_249 = input.LA(6);

                            if ( (LA12_249=='S'||LA12_249=='s') ) {
                                int LA12_280 = input.LA(7);

                                if ( ((LA12_280>='0' && LA12_280<='9')||(LA12_280>='A' && LA12_280<='Z')||LA12_280=='_'||(LA12_280>='a' && LA12_280<='z')) ) {
                                    alt12=81;
                                }
                                else {
                                    alt12=74;}
                            }
                            else {
                                alt12=81;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                    }
                    break;
                default:
                    alt12=81;}

            }
            else {
                alt12=81;}
            }
            break;
        case 'W':
        case 'w':
            {
            int LA12_34 = input.LA(2);

            if ( (LA12_34=='H'||LA12_34=='h') ) {
                int LA12_96 = input.LA(3);

                if ( (LA12_96=='E'||LA12_96=='e') ) {
                    int LA12_155 = input.LA(4);

                    if ( (LA12_155=='R'||LA12_155=='r') ) {
                        int LA12_209 = input.LA(5);

                        if ( (LA12_209=='E'||LA12_209=='e') ) {
                            int LA12_250 = input.LA(6);

                            if ( ((LA12_250>='0' && LA12_250<='9')||(LA12_250>='A' && LA12_250<='Z')||LA12_250=='_'||(LA12_250>='a' && LA12_250<='z')) ) {
                                alt12=81;
                            }
                            else {
                                alt12=76;}
                        }
                        else {
                            alt12=81;}
                    }
                    else {
                        alt12=81;}
                }
                else {
                    alt12=81;}
            }
            else {
                alt12=81;}
            }
            break;
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            {
            alt12=77;
            }
            break;
        case '$':
        case '\'':
            {
            alt12=78;
            }
            break;
        case '\"':
        case '[':
            {
            alt12=79;
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
            alt12=80;
            }
            break;
        case 'K':
        case 'P':
        case 'Q':
        case 'X':
        case 'Y':
        case 'Z':
        case 'k':
        case 'p':
        case 'q':
        case 'x':
        case 'y':
        case 'z':
            {
            alt12=81;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( PLUS | MINUS | STAR | DIV | GT | LT | DOT | PCT | EQ | HAT | GE | LE | T85 | T86 | T87 | T88 | ALL | AND | ARRAY | AS | ASC | AVG | BY | COUNT | CROSS | DEFAULT | DESC | DISTINCT | EXISTS | FIRST | FORMAT | FROM | FULL | GLOBAL | GROUP | HAVING | ILIKE | INTO | INNER | JOIN | LAST | LCASE | LEFT | LEN | LIKE | LIMIT | LOCAL | MAX | MID | MIN | NATURAL | NEW | NOW | NOT | NVL | OFFSET | OLD | ON | ONLY | OR | ORDER | OUTER | RIGHT | ROUND | SELECT | SIMILAR | SUM | TABLE | TEMP | TEMPORARY | TO | UCASE | USING | VALUES | VARIADIC | WHERE | WHITESPACE | QUOTEDSTRING | QUOTEID | NUMBER | ID | SL_COMMENT | ML_COMMENT );", 12, 0, input);

            throw nvae;
        }

        switch (alt12) {
            case 1 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:10: PLUS
                {
                mPLUS(); 

                }
                break;
            case 2 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:15: MINUS
                {
                mMINUS(); 

                }
                break;
            case 3 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:21: STAR
                {
                mSTAR(); 

                }
                break;
            case 4 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:26: DIV
                {
                mDIV(); 

                }
                break;
            case 5 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:30: GT
                {
                mGT(); 

                }
                break;
            case 6 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:33: LT
                {
                mLT(); 

                }
                break;
            case 7 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:36: DOT
                {
                mDOT(); 

                }
                break;
            case 8 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:40: PCT
                {
                mPCT(); 

                }
                break;
            case 9 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:44: EQ
                {
                mEQ(); 

                }
                break;
            case 10 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:47: HAT
                {
                mHAT(); 

                }
                break;
            case 11 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:51: GE
                {
                mGE(); 

                }
                break;
            case 12 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:54: LE
                {
                mLE(); 

                }
                break;
            case 13 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:57: T85
                {
                mT85(); 

                }
                break;
            case 14 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:61: T86
                {
                mT86(); 

                }
                break;
            case 15 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:65: T87
                {
                mT87(); 

                }
                break;
            case 16 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:69: T88
                {
                mT88(); 

                }
                break;
            case 17 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:73: ALL
                {
                mALL(); 

                }
                break;
            case 18 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:77: AND
                {
                mAND(); 

                }
                break;
            case 19 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:81: ARRAY
                {
                mARRAY(); 

                }
                break;
            case 20 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:87: AS
                {
                mAS(); 

                }
                break;
            case 21 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:90: ASC
                {
                mASC(); 

                }
                break;
            case 22 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:94: AVG
                {
                mAVG(); 

                }
                break;
            case 23 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:98: BY
                {
                mBY(); 

                }
                break;
            case 24 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:101: COUNT
                {
                mCOUNT(); 

                }
                break;
            case 25 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:107: CROSS
                {
                mCROSS(); 

                }
                break;
            case 26 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:113: DEFAULT
                {
                mDEFAULT(); 

                }
                break;
            case 27 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:121: DESC
                {
                mDESC(); 

                }
                break;
            case 28 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:126: DISTINCT
                {
                mDISTINCT(); 

                }
                break;
            case 29 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:135: EXISTS
                {
                mEXISTS(); 

                }
                break;
            case 30 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:142: FIRST
                {
                mFIRST(); 

                }
                break;
            case 31 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:148: FORMAT
                {
                mFORMAT(); 

                }
                break;
            case 32 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:155: FROM
                {
                mFROM(); 

                }
                break;
            case 33 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:160: FULL
                {
                mFULL(); 

                }
                break;
            case 34 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:165: GLOBAL
                {
                mGLOBAL(); 

                }
                break;
            case 35 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:172: GROUP
                {
                mGROUP(); 

                }
                break;
            case 36 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:178: HAVING
                {
                mHAVING(); 

                }
                break;
            case 37 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:185: ILIKE
                {
                mILIKE(); 

                }
                break;
            case 38 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:191: INTO
                {
                mINTO(); 

                }
                break;
            case 39 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:196: INNER
                {
                mINNER(); 

                }
                break;
            case 40 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:202: JOIN
                {
                mJOIN(); 

                }
                break;
            case 41 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:207: LAST
                {
                mLAST(); 

                }
                break;
            case 42 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:212: LCASE
                {
                mLCASE(); 

                }
                break;
            case 43 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:218: LEFT
                {
                mLEFT(); 

                }
                break;
            case 44 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:223: LEN
                {
                mLEN(); 

                }
                break;
            case 45 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:227: LIKE
                {
                mLIKE(); 

                }
                break;
            case 46 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:232: LIMIT
                {
                mLIMIT(); 

                }
                break;
            case 47 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:238: LOCAL
                {
                mLOCAL(); 

                }
                break;
            case 48 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:244: MAX
                {
                mMAX(); 

                }
                break;
            case 49 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:248: MID
                {
                mMID(); 

                }
                break;
            case 50 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:252: MIN
                {
                mMIN(); 

                }
                break;
            case 51 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:256: NATURAL
                {
                mNATURAL(); 

                }
                break;
            case 52 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:264: NEW
                {
                mNEW(); 

                }
                break;
            case 53 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:268: NOW
                {
                mNOW(); 

                }
                break;
            case 54 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:272: NOT
                {
                mNOT(); 

                }
                break;
            case 55 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:276: NVL
                {
                mNVL(); 

                }
                break;
            case 56 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:280: OFFSET
                {
                mOFFSET(); 

                }
                break;
            case 57 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:287: OLD
                {
                mOLD(); 

                }
                break;
            case 58 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:291: ON
                {
                mON(); 

                }
                break;
            case 59 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:294: ONLY
                {
                mONLY(); 

                }
                break;
            case 60 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:299: OR
                {
                mOR(); 

                }
                break;
            case 61 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:302: ORDER
                {
                mORDER(); 

                }
                break;
            case 62 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:308: OUTER
                {
                mOUTER(); 

                }
                break;
            case 63 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:314: RIGHT
                {
                mRIGHT(); 

                }
                break;
            case 64 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:320: ROUND
                {
                mROUND(); 

                }
                break;
            case 65 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:326: SELECT
                {
                mSELECT(); 

                }
                break;
            case 66 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:333: SIMILAR
                {
                mSIMILAR(); 

                }
                break;
            case 67 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:341: SUM
                {
                mSUM(); 

                }
                break;
            case 68 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:345: TABLE
                {
                mTABLE(); 

                }
                break;
            case 69 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:351: TEMP
                {
                mTEMP(); 

                }
                break;
            case 70 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:356: TEMPORARY
                {
                mTEMPORARY(); 

                }
                break;
            case 71 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:366: TO
                {
                mTO(); 

                }
                break;
            case 72 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:369: UCASE
                {
                mUCASE(); 

                }
                break;
            case 73 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:375: USING
                {
                mUSING(); 

                }
                break;
            case 74 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:381: VALUES
                {
                mVALUES(); 

                }
                break;
            case 75 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:388: VARIADIC
                {
                mVARIADIC(); 

                }
                break;
            case 76 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:397: WHERE
                {
                mWHERE(); 

                }
                break;
            case 77 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:403: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;
            case 78 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:414: QUOTEDSTRING
                {
                mQUOTEDSTRING(); 

                }
                break;
            case 79 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:427: QUOTEID
                {
                mQUOTEID(); 

                }
                break;
            case 80 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:435: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 81 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:442: ID
                {
                mID(); 

                }
                break;
            case 82 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:445: SL_COMMENT
                {
                mSL_COMMENT(); 

                }
                break;
            case 83 :
                // /home/jeffrey/workspace/wabit/src/main/antlr/ca/sqlpower/wabit/parser/SQLANTLR.g:1:456: ML_COMMENT
                {
                mML_COMMENT(); 

                }
                break;

        }

    }


 

}