package org.hibernate.sql.ordering.antlr;

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

public class GeneratedOrderByLexer extends CharScanner implements OrderByTemplateTokenTypes, TokenStream {
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());

   public GeneratedOrderByLexer(InputStream in) {
      this((InputBuffer)(new ByteBuffer(in)));
   }

   public GeneratedOrderByLexer(Reader in) {
      this((InputBuffer)(new CharBuffer(in)));
   }

   public GeneratedOrderByLexer(InputBuffer ib) {
      this(new LexerSharedInputState(ib));
   }

   public GeneratedOrderByLexer(LexerSharedInputState state) {
      super(state);
      this.caseSensitiveLiterals = false;
      this.setCaseSensitive(false);
      this.literals = new Hashtable();
      this.literals.put(new ANTLRHashString("asc", this), new Integer(13));
      this.literals.put(new ANTLRHashString("ascending", this), new Integer(25));
      this.literals.put(new ANTLRHashString("collate", this), new Integer(12));
      this.literals.put(new ANTLRHashString("descending", this), new Integer(26));
      this.literals.put(new ANTLRHashString("desc", this), new Integer(14));
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
                  case '!':
                  case '"':
                  case '#':
                  case '$':
                  case '%':
                  case '&':
                  case '*':
                  case '+':
                  case '-':
                  case '/':
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
                  default:
                     if (_tokenSet_0.member(this.LA(1))) {
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
                  case '\'':
                     this.mQUOTED_STRING(true);
                     theRetToken = this._returnToken;
                     break;
                  case '(':
                     this.mOPEN_PAREN(true);
                     theRetToken = this._returnToken;
                     break;
                  case ')':
                     this.mCLOSE_PAREN(true);
                     theRetToken = this._returnToken;
                     break;
                  case ',':
                     this.mCOMMA(true);
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
                  case '`':
                     this.mHARD_QUOTE(true);
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

   public final void mOPEN_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 18;
      this.match('(');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCLOSE_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 19;
      this.match(')');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 15;
      this.match(',');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mHARD_QUOTE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 16;
      this.match('`');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 17;
      this.mID_START_LETTER(false);

      while(_tokenSet_1.member(this.LA(1))) {
         this.mID_LETTER(false);
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
      int _ttype = 27;
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
      int _ttype = 28;
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
      int _ttype = 24;
      this.match('\'');

      while(true) {
         boolean synPredMatched44 = false;
         if (this.LA(1) == '\'' && this.LA(2) == '\'') {
            int _m44 = this.mark();
            synPredMatched44 = true;
            ++this.inputState.guessing;

            try {
               this.mESCqs(false);
            } catch (RecognitionException var9) {
               synPredMatched44 = false;
            }

            this.rewind(_m44);
            --this.inputState.guessing;
         }

         if (synPredMatched44) {
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
      int _ttype = 29;
      this.match('\'');
      this.match('\'');
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
      label209: {
         _token = null;
         _begin = this.text.length();
         _ttype = 22;
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
                  _ttype = 9;
               }

               if (this.LA(1) >= '0' && this.LA(1) <= '9') {
                  int _cnt50;
                  for(_cnt50 = 0; this.LA(1) >= '0' && this.LA(1) <= '9'; ++_cnt50) {
                     this.matchRange('0', '9');
                  }

                  if (_cnt50 < 1) {
                     throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                  }

                  if (this.LA(1) == 'e') {
                     this.mEXPONENT(false);
                  }

                  if (this.LA(1) == 'd' || this.LA(1) == 'f') {
                     this.mFLOAT_SUFFIX(true);
                     f1 = this._returnToken;
                     if (this.inputState.guessing == 0) {
                        t = f1;
                     }
                  }

                  if (this.inputState.guessing == 0) {
                     if (t != null && t.getText().toUpperCase().indexOf(70) >= 0) {
                        _ttype = 21;
                     } else {
                        _ttype = 20;
                     }
                  }
               }
               break label209;
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

         label199:
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
                     int _cnt59;
                     for(_cnt59 = 0; this.LA(1) >= '0' && this.LA(1) <= '7'; ++_cnt59) {
                        this.matchRange('0', '7');
                     }

                     if (_cnt59 < 1) {
                        throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                     }
                     break label199;
                  case 'x':
                     this.match('x');

                     int _cnt57;
                     for(_cnt57 = 0; _tokenSet_3.member(this.LA(1)); ++_cnt57) {
                        this.mHEX_DIGIT(false);
                     }

                     if (_cnt57 < 1) {
                        throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                     }
                  default:
                     break label199;
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

         if (this.LA(1) == 'l') {
            this.match('l');
            if (this.inputState.guessing == 0) {
               _ttype = 23;
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

                  if (this.LA(1) == 'd' || this.LA(1) == 'f') {
                     this.mFLOAT_SUFFIX(true);
                     f2 = this._returnToken;
                     if (this.inputState.guessing == 0) {
                        t = f2;
                     }
                  }
                  break;
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
                  if (this.LA(1) == 'd' || this.LA(1) == 'f') {
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
               if (t != null && t.getText().toUpperCase().indexOf(70) >= 0) {
                  _ttype = 21;
               } else {
                  _ttype = 20;
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
      int _ttype = 31;
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

      int _cnt77;
      for(_cnt77 = 0; this.LA(1) >= '0' && this.LA(1) <= '9'; ++_cnt77) {
         this.matchRange('0', '9');
      }

      if (_cnt77 >= 1) {
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
      int _ttype = 32;
      switch (this.LA(1)) {
         case 'd':
            this.match('d');
            break;
         case 'f':
            this.match('f');
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

   protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 30;
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

   public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 33;
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
      data[1] = 481036337152L;
      return data;
   }
}
