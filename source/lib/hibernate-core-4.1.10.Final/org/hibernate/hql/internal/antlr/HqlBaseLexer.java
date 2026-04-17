package org.hibernate.hql.internal.antlr;

import antlr.ANTLRHashString;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.CharScanner;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.NoViableAltForCharException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.impl.BitSet;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

public class HqlBaseLexer extends CharScanner implements HqlTokenTypes, TokenStream {
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

   protected void setPossibleID(boolean possibleID) {
   }

   public HqlBaseLexer(InputStream in) {
      this((InputBuffer)(new ByteBuffer(in)));
   }

   public HqlBaseLexer(Reader in) {
      this((InputBuffer)(new CharBuffer(in)));
   }

   public HqlBaseLexer(InputBuffer ib) {
      this(new LexerSharedInputState(ib));
   }

   public HqlBaseLexer(LexerSharedInputState state) {
      super(state);
      this.caseSensitiveLiterals = false;
      this.setCaseSensitive(false);
      this.literals = new Hashtable();
      this.literals.put(new ANTLRHashString("between", this), new Integer(10));
      this.literals.put(new ANTLRHashString("case", this), new Integer(54));
      this.literals.put(new ANTLRHashString("delete", this), new Integer(13));
      this.literals.put(new ANTLRHashString("new", this), new Integer(37));
      this.literals.put(new ANTLRHashString("end", this), new Integer(55));
      this.literals.put(new ANTLRHashString("object", this), new Integer(65));
      this.literals.put(new ANTLRHashString("insert", this), new Integer(29));
      this.literals.put(new ANTLRHashString("distinct", this), new Integer(16));
      this.literals.put(new ANTLRHashString("where", this), new Integer(53));
      this.literals.put(new ANTLRHashString("trailing", this), new Integer(67));
      this.literals.put(new ANTLRHashString("then", this), new Integer(57));
      this.literals.put(new ANTLRHashString("select", this), new Integer(45));
      this.literals.put(new ANTLRHashString("and", this), new Integer(6));
      this.literals.put(new ANTLRHashString("outer", this), new Integer(42));
      this.literals.put(new ANTLRHashString("not", this), new Integer(38));
      this.literals.put(new ANTLRHashString("fetch", this), new Integer(21));
      this.literals.put(new ANTLRHashString("from", this), new Integer(22));
      this.literals.put(new ANTLRHashString("null", this), new Integer(39));
      this.literals.put(new ANTLRHashString("count", this), new Integer(12));
      this.literals.put(new ANTLRHashString("like", this), new Integer(34));
      this.literals.put(new ANTLRHashString("when", this), new Integer(58));
      this.literals.put(new ANTLRHashString("class", this), new Integer(11));
      this.literals.put(new ANTLRHashString("inner", this), new Integer(28));
      this.literals.put(new ANTLRHashString("leading", this), new Integer(63));
      this.literals.put(new ANTLRHashString("with", this), new Integer(60));
      this.literals.put(new ANTLRHashString("set", this), new Integer(46));
      this.literals.put(new ANTLRHashString("escape", this), new Integer(18));
      this.literals.put(new ANTLRHashString("join", this), new Integer(32));
      this.literals.put(new ANTLRHashString("elements", this), new Integer(17));
      this.literals.put(new ANTLRHashString("of", this), new Integer(66));
      this.literals.put(new ANTLRHashString("is", this), new Integer(31));
      this.literals.put(new ANTLRHashString("member", this), new Integer(64));
      this.literals.put(new ANTLRHashString("or", this), new Integer(40));
      this.literals.put(new ANTLRHashString("any", this), new Integer(5));
      this.literals.put(new ANTLRHashString("full", this), new Integer(23));
      this.literals.put(new ANTLRHashString("min", this), new Integer(36));
      this.literals.put(new ANTLRHashString("as", this), new Integer(7));
      this.literals.put(new ANTLRHashString("by", this), new Integer(105));
      this.literals.put(new ANTLRHashString("all", this), new Integer(4));
      this.literals.put(new ANTLRHashString("union", this), new Integer(50));
      this.literals.put(new ANTLRHashString("order", this), new Integer(41));
      this.literals.put(new ANTLRHashString("both", this), new Integer(61));
      this.literals.put(new ANTLRHashString("some", this), new Integer(47));
      this.literals.put(new ANTLRHashString("properties", this), new Integer(43));
      this.literals.put(new ANTLRHashString("ascending", this), new Integer(106));
      this.literals.put(new ANTLRHashString("descending", this), new Integer(107));
      this.literals.put(new ANTLRHashString("false", this), new Integer(20));
      this.literals.put(new ANTLRHashString("exists", this), new Integer(19));
      this.literals.put(new ANTLRHashString("asc", this), new Integer(8));
      this.literals.put(new ANTLRHashString("left", this), new Integer(33));
      this.literals.put(new ANTLRHashString("desc", this), new Integer(14));
      this.literals.put(new ANTLRHashString("max", this), new Integer(35));
      this.literals.put(new ANTLRHashString("empty", this), new Integer(62));
      this.literals.put(new ANTLRHashString("sum", this), new Integer(48));
      this.literals.put(new ANTLRHashString("on", this), new Integer(59));
      this.literals.put(new ANTLRHashString("into", this), new Integer(30));
      this.literals.put(new ANTLRHashString("else", this), new Integer(56));
      this.literals.put(new ANTLRHashString("right", this), new Integer(44));
      this.literals.put(new ANTLRHashString("versioned", this), new Integer(52));
      this.literals.put(new ANTLRHashString("in", this), new Integer(26));
      this.literals.put(new ANTLRHashString("avg", this), new Integer(9));
      this.literals.put(new ANTLRHashString("update", this), new Integer(51));
      this.literals.put(new ANTLRHashString("true", this), new Integer(49));
      this.literals.put(new ANTLRHashString("group", this), new Integer(24));
      this.literals.put(new ANTLRHashString("having", this), new Integer(25));
      this.literals.put(new ANTLRHashString("indices", this), new Integer(27));
   }

   public Token nextToken() throws TokenStreamException {
      Token theRetToken = null;

      while(true) {
         Token _token = null;
         int _ttype = 0;
         this.resetText();

         try {
            try {
               switch (this.LA(1)) {
                  case '\t':
                  case '\n':
                  case '\r':
                  case ' ':
                     this.mWS(true);
                     theRetToken = this._returnToken;
                     break;
                  case '\u000b':
                  case '\f':
                  case '\u000e':
                  case '\u000f':
                  case '\u0010':
                  case '\u0011':
                  case '\u0012':
                  case '\u0013':
                  case '\u0014':
                  case '\u0015':
                  case '\u0016':
                  case '\u0017':
                  case '\u0018':
                  case '\u0019':
                  case '\u001a':
                  case '\u001b':
                  case '\u001c':
                  case '\u001d':
                  case '\u001e':
                  case '\u001f':
                  case '"':
                  case '#':
                  case '$':
                  case '&':
                  case ';':
                  case '<':
                  case '>':
                  case '@':
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
                  case '\\':
                  case '_':
                  case '`':
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
                  case '{':
                  default:
                     if (this.LA(1) == '<' && this.LA(2) == '>') {
                        this.mSQL_NE(true);
                        theRetToken = this._returnToken;
                     } else if (this.LA(1) == '<' && this.LA(2) == '=') {
                        this.mLE(true);
                        theRetToken = this._returnToken;
                     } else if (this.LA(1) == '>' && this.LA(2) == '=') {
                        this.mGE(true);
                        theRetToken = this._returnToken;
                     } else if (this.LA(1) == '<') {
                        this.mLT(true);
                        theRetToken = this._returnToken;
                     } else if (this.LA(1) == '>') {
                        this.mGT(true);
                        theRetToken = this._returnToken;
                     } else if (_tokenSet_0.member(this.LA(1))) {
                        this.mIDENT(true);
                        theRetToken = this._returnToken;
                     } else {
                        if (this.LA(1) != '\uffff') {
                           throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                        }

                        this.uponEOF();
                        this._returnToken = this.makeToken(1);
                     }
                     break;
                  case '!':
                  case '^':
                     this.mNE(true);
                     theRetToken = this._returnToken;
                     break;
                  case '%':
                     this.mMOD(true);
                     theRetToken = this._returnToken;
                     break;
                  case '\'':
                     this.mQUOTED_STRING(true);
                     theRetToken = this._returnToken;
                     break;
                  case '(':
                     this.mOPEN(true);
                     theRetToken = this._returnToken;
                     break;
                  case ')':
                     this.mCLOSE(true);
                     theRetToken = this._returnToken;
                     break;
                  case '*':
                     this.mSTAR(true);
                     theRetToken = this._returnToken;
                     break;
                  case '+':
                     this.mPLUS(true);
                     theRetToken = this._returnToken;
                     break;
                  case ',':
                     this.mCOMMA(true);
                     theRetToken = this._returnToken;
                     break;
                  case '-':
                     this.mMINUS(true);
                     theRetToken = this._returnToken;
                     break;
                  case '.':
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
                     this.mNUM_INT(true);
                     theRetToken = this._returnToken;
                     break;
                  case '/':
                     this.mDIV(true);
                     theRetToken = this._returnToken;
                     break;
                  case ':':
                     this.mCOLON(true);
                     theRetToken = this._returnToken;
                     break;
                  case '=':
                     this.mEQ(true);
                     theRetToken = this._returnToken;
                     break;
                  case '?':
                     this.mPARAM(true);
                     theRetToken = this._returnToken;
                     break;
                  case '[':
                     this.mOPEN_BRACKET(true);
                     theRetToken = this._returnToken;
                     break;
                  case ']':
                     this.mCLOSE_BRACKET(true);
                     theRetToken = this._returnToken;
                     break;
                  case '|':
                     this.mCONCAT(true);
                     theRetToken = this._returnToken;
               }

               if (this._returnToken != null) {
                  _ttype = this._returnToken.getType();
                  this._returnToken.setType(_ttype);
                  return this._returnToken;
               }
            } catch (RecognitionException e) {
               throw new TokenStreamRecognitionException(e);
            }
         } catch (CharStreamException cse) {
            if (cse instanceof CharStreamIOException) {
               throw new TokenStreamIOException(((CharStreamIOException)cse).io);
            }

            throw new TokenStreamException(cse.getMessage());
         }
      }
   }

   public final void mEQ(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 102;
      this.match('=');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 110;
      this.match('<');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 111;
      this.match('>');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mSQL_NE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 109;
      this.match("<>");
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mNE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 108;
      switch (this.LA(1)) {
         case '!':
            this.match("!=");
            break;
         case '^':
            this.match("^=");
            break;
         default:
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 112;
      this.match("<=");
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mGE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 113;
      this.match(">=");
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 101;
      this.match(',');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mOPEN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 103;
      this.match('(');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCLOSE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 104;
      this.match(')');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mOPEN_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 120;
      this.match('[');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCLOSE_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 121;
      this.match(']');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCONCAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 114;
      this.match("||");
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 115;
      this.match('+');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 116;
      this.match('-');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mSTAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 117;
      this.match('*');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 118;
      this.match('/');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mMOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 119;
      this.match('%');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 122;
      this.match(':');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mPARAM(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 123;
      this.match('?');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 126;
      this.mID_START_LETTER(false);

      while(_tokenSet_1.member(this.LA(1))) {
         this.mID_LETTER(false);
      }

      if (this.inputState.guessing == 0) {
         this.setPossibleID(true);
      }

      _ttype = this.testLiteralsTable(_ttype);
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   protected final void mID_START_LETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 127;
      switch (this.LA(1)) {
         case '$':
            this.match('$');
            break;
         case '%':
         case '&':
         case '\'':
         case '(':
         case ')':
         case '*':
         case '+':
         case ',':
         case '-':
         case '.':
         case '/':
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
         case ':':
         case ';':
         case '<':
         case '=':
         case '>':
         case '?':
         case '@':
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
         case '[':
         case '\\':
         case ']':
         case '^':
         case '`':
         default:
            if (this.LA(1) < 128 || this.LA(1) > '\ufffe') {
               throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
            }

            this.matchRange('\u0080', '\ufffe');
            break;
         case '_':
            this.match('_');
            break;
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
            this.matchRange('a', 'z');
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   protected final void mID_LETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 128;
      if (_tokenSet_0.member(this.LA(1))) {
         this.mID_START_LETTER(false);
      } else {
         if (this.LA(1) < '0' || this.LA(1) > '9') {
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
         }

         this.matchRange('0', '9');
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mQUOTED_STRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 125;
      this.match('\'');

      while(true) {
         boolean synPredMatched221 = false;
         if (this.LA(1) == '\'' && this.LA(2) == '\'') {
            int _m221 = this.mark();
            synPredMatched221 = true;
            ++this.inputState.guessing;

            try {
               this.mESCqs(false);
            } catch (RecognitionException var9) {
               synPredMatched221 = false;
            }

            this.rewind(_m221);
            --this.inputState.guessing;
         }

         if (synPredMatched221) {
            this.mESCqs(false);
         } else {
            if (!_tokenSet_2.member(this.LA(1))) {
               this.match('\'');
               if (_createToken && _token == null && _ttype != -1) {
                  _token = this.makeToken(_ttype);
                  _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
               }

               this._returnToken = _token;
               return;
            }

            this.matchNot('\'');
         }
      }
   }

   protected final void mESCqs(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 129;
      this.match('\'');
      this.match('\'');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 130;
      switch (this.LA(1)) {
         case '\t':
            this.match('\t');
            break;
         case '\n':
            this.match('\n');
            if (this.inputState.guessing == 0) {
               this.newline();
            }
            break;
         case ' ':
            this.match(' ');
            break;
         default:
            if (this.LA(1) == '\r' && this.LA(2) == '\n') {
               this.match('\r');
               this.match('\n');
               if (this.inputState.guessing == 0) {
                  this.newline();
               }
            } else {
               if (this.LA(1) != '\r') {
                  throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
               }

               this.match('\r');
               if (this.inputState.guessing == 0) {
                  this.newline();
               }
            }
      }

      if (this.inputState.guessing == 0) {
         _ttype = -1;
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mNUM_INT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      int _ttype;
      Token _token;
      int _begin;
      label237: {
         _token = null;
         _begin = this.text.length();
         _ttype = 124;
         Token f1 = null;
         Token f2 = null;
         Token f3 = null;
         Token f4 = null;
         boolean isDecimal = false;
         Token t = null;
         switch (this.LA(1)) {
            case '.':
               this.match('.');
               if (this.inputState.guessing == 0) {
                  _ttype = 15;
               }

               if (this.LA(1) >= '0' && this.LA(1) <= '9') {
                  int _cnt229;
                  for(_cnt229 = 0; this.LA(1) >= '0' && this.LA(1) <= '9'; ++_cnt229) {
                     this.matchRange('0', '9');
                  }

                  if (_cnt229 < 1) {
                     throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                  }

                  if (this.LA(1) == 'e') {
                     this.mEXPONENT(false);
                  }

                  if (this.LA(1) == 'b' || this.LA(1) == 'd' || this.LA(1) == 'f') {
                     this.mFLOAT_SUFFIX(true);
                     f1 = this._returnToken;
                     if (this.inputState.guessing == 0) {
                        t = f1;
                     }
                  }

                  if (this.inputState.guessing == 0) {
                     if (t != null && t.getText().toUpperCase().indexOf("BD") >= 0) {
                        _ttype = 99;
                     } else if (t != null && t.getText().toUpperCase().indexOf(70) >= 0) {
                        _ttype = 96;
                     } else {
                        _ttype = 95;
                     }
                  }
               }
               break label237;
            case '/':
            default:
               throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
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
         }

         label227:
         switch (this.LA(1)) {
            case '0':
               this.match('0');
               if (this.inputState.guessing == 0) {
                  isDecimal = true;
               }

               switch (this.LA(1)) {
                  case '0':
                  case '1':
                  case '2':
                  case '3':
                  case '4':
                  case '5':
                  case '6':
                  case '7':
                     int _cnt238;
                     for(_cnt238 = 0; this.LA(1) >= '0' && this.LA(1) <= '7'; ++_cnt238) {
                        this.matchRange('0', '7');
                     }

                     if (_cnt238 < 1) {
                        throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                     }
                     break label227;
                  case 'x':
                     this.match('x');

                     int _cnt236;
                     for(_cnt236 = 0; _tokenSet_3.member(this.LA(1)); ++_cnt236) {
                        this.mHEX_DIGIT(false);
                     }

                     if (_cnt236 < 1) {
                        throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                     }
                  default:
                     break label227;
               }
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               this.matchRange('1', '9');

               while(this.LA(1) >= '0' && this.LA(1) <= '9') {
                  this.matchRange('0', '9');
               }

               if (this.inputState.guessing == 0) {
                  isDecimal = true;
               }
               break;
            default:
               throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
         }

         if (this.LA(1) == 'b' && this.LA(2) == 'i') {
            this.match('b');
            this.match('i');
            if (this.inputState.guessing == 0) {
               _ttype = 98;
            }
         } else if (this.LA(1) == 'l') {
            this.match('l');
            if (this.inputState.guessing == 0) {
               _ttype = 97;
            }
         } else if (_tokenSet_4.member(this.LA(1)) && isDecimal) {
            switch (this.LA(1)) {
               case '.':
                  this.match('.');

                  while(this.LA(1) >= '0' && this.LA(1) <= '9') {
                     this.matchRange('0', '9');
                  }

                  if (this.LA(1) == 'e') {
                     this.mEXPONENT(false);
                  }

                  if (this.LA(1) == 'b' || this.LA(1) == 'd' || this.LA(1) == 'f') {
                     this.mFLOAT_SUFFIX(true);
                     f2 = this._returnToken;
                     if (this.inputState.guessing == 0) {
                        t = f2;
                     }
                  }
                  break;
               case 'b':
               case 'd':
               case 'f':
                  this.mFLOAT_SUFFIX(true);
                  f4 = this._returnToken;
                  if (this.inputState.guessing == 0) {
                     t = f4;
                  }
                  break;
               case 'e':
                  this.mEXPONENT(false);
                  if (this.LA(1) == 'b' || this.LA(1) == 'd' || this.LA(1) == 'f') {
                     this.mFLOAT_SUFFIX(true);
                     f3 = this._returnToken;
                     if (this.inputState.guessing == 0) {
                        t = f3;
                     }
                  }
                  break;
               default:
                  throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
            }

            if (this.inputState.guessing == 0) {
               if (t != null && t.getText().toUpperCase().indexOf("BD") >= 0) {
                  _ttype = 99;
               } else if (t != null && t.getText().toUpperCase().indexOf(70) >= 0) {
                  _ttype = 96;
               } else {
                  _ttype = 95;
               }
            }
         }
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 132;
      this.match('e');
      switch (this.LA(1)) {
         case '+':
            this.match('+');
            break;
         case ',':
         case '.':
         case '/':
         default:
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
         case '-':
            this.match('-');
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
      }

      int _cnt257;
      for(_cnt257 = 0; this.LA(1) >= '0' && this.LA(1) <= '9'; ++_cnt257) {
         this.matchRange('0', '9');
      }

      if (_cnt257 >= 1) {
         if (_createToken && _token == null && _ttype != -1) {
            _token = this.makeToken(_ttype);
            _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
         }

         this._returnToken = _token;
      } else {
         throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
      }
   }

   protected final void mFLOAT_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 133;
      switch (this.LA(1)) {
         case 'b':
            this.match('b');
            this.match('d');
            break;
         case 'c':
         case 'e':
         default:
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
         case 'd':
            this.match('d');
            break;
         case 'f':
            this.match('f');
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 131;
      switch (this.LA(1)) {
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
            this.matchRange('0', '9');
            break;
         case ':':
         case ';':
         case '<':
         case '=':
         case '>':
         case '?':
         case '@':
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
         case '[':
         case '\\':
         case ']':
         case '^':
         case '_':
         case '`':
         default:
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
         case 'a':
         case 'b':
         case 'c':
         case 'd':
         case 'e':
         case 'f':
            this.matchRange('a', 'f');
      }

      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[3072];
      data[0] = 68719476736L;
      data[1] = 576460745860972544L;

      for(int i = 2; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[3072];
      data[0] = 287948969894477824L;
      data[1] = 576460745860972544L;

      for(int i = 2; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }

   private static final long[] mk_tokenSet_2() {
      long[] data = new long[2048];
      data[0] = -549755813889L;

      for(int i = 1; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }

   private static final long[] mk_tokenSet_3() {
      long[] data = new long[1025];
      data[0] = 287948901175001088L;
      data[1] = 541165879296L;
      return data;
   }

   private static final long[] mk_tokenSet_4() {
      long[] data = new long[1025];
      data[0] = 70368744177664L;
      data[1] = 498216206336L;
      return data;
   }
}
