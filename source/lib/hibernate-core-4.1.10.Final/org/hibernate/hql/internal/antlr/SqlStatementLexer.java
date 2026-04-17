package org.hibernate.hql.internal.antlr;

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

public class SqlStatementLexer extends CharScanner implements SqlStatementParserTokenTypes, TokenStream {
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

   public SqlStatementLexer(InputStream in) {
      this((InputBuffer)(new ByteBuffer(in)));
   }

   public SqlStatementLexer(Reader in) {
      this((InputBuffer)(new CharBuffer(in)));
   }

   public SqlStatementLexer(InputBuffer ib) {
      this(new LexerSharedInputState(ib));
   }

   public SqlStatementLexer(LexerSharedInputState state) {
      super(state);
      this.caseSensitiveLiterals = true;
      this.setCaseSensitive(true);
      this.literals = new Hashtable();
   }

   public Token nextToken() throws TokenStreamException {
      Token theRetToken = null;

      while(true) {
         Token _token = null;
         int _ttype = 0;
         this.resetText();

         try {
            try {
               if (this.LA(1) == '\'' && this.LA(2) >= 0 && this.LA(2) <= '\ufffe') {
                  this.mQUOTED_STRING(true);
                  theRetToken = this._returnToken;
               } else if (this.LA(1) != '-' && this.LA(1) != '/' || this.LA(2) != '-' && this.LA(2) != '/') {
                  if (this.LA(1) == '/' && this.LA(2) == '*') {
                     this.mMULTILINE_COMMENT(true);
                     theRetToken = this._returnToken;
                  } else if (this.LA(1) == ';') {
                     this.mSTMT_END(true);
                     theRetToken = this._returnToken;
                  } else if (_tokenSet_0.member(this.LA(1))) {
                     this.mNOT_STMT_END(true);
                     theRetToken = this._returnToken;
                  } else {
                     if (this.LA(1) != '\uffff') {
                        throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
                     }

                     this.uponEOF();
                     this._returnToken = this.makeToken(1);
                  }
               } else {
                  this.mLINE_COMMENT(true);
                  theRetToken = this._returnToken;
               }

               if (this._returnToken != null) {
                  _ttype = this._returnToken.getType();
                  _ttype = this.testLiteralsTable(_ttype);
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

   public final void mSTMT_END(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 6;
      this.match(';');

      while(true) {
         switch (this.LA(1)) {
            case '\t':
               this.match('\t');
               break;
            case '\n':
               this.match('\n');
               break;
            case '\r':
               this.match('\r');
               break;
            case ' ':
               this.match(' ');
               break;
            default:
               if (_createToken && _token == null && _ttype != -1) {
                  _token = this.makeToken(_ttype);
                  _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
               }

               this._returnToken = _token;
               return;
         }
      }
   }

   public final void mNOT_STMT_END(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 4;
      this.match(_tokenSet_0);
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mQUOTED_STRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 5;
      this.match('\'');

      while(true) {
         boolean synPredMatched15 = false;
         if (this.LA(1) == '\'' && this.LA(2) == '\'') {
            int _m15 = this.mark();
            synPredMatched15 = true;
            ++this.inputState.guessing;

            try {
               this.mESCqs(false);
            } catch (RecognitionException var9) {
               synPredMatched15 = false;
            }

            this.rewind(_m15);
            --this.inputState.guessing;
         }

         if (synPredMatched15) {
            this.mESCqs(false);
         } else {
            if (!_tokenSet_1.member(this.LA(1))) {
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
      int _ttype = 7;
      this.match('\'');
      this.match('\'');
      if (_createToken && _token == null && _ttype != -1) {
         _token = this.makeToken(_ttype);
         _token.setText(new String(this.text.getBuffer(), _begin, this.text.length() - _begin));
      }

      this._returnToken = _token;
   }

   public final void mLINE_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 8;
      switch (this.LA(1)) {
         case '-':
            this.match("--");
            break;
         case '/':
            this.match("//");
            break;
         default:
            throw new NoViableAltForCharException(this.LA(1), this.getFilename(), this.getLine(), this.getColumn());
      }

      while(_tokenSet_2.member(this.LA(1))) {
         this.match(_tokenSet_2);
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

   public final void mMULTILINE_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
      Token _token = null;
      int _begin = this.text.length();
      int _ttype = 9;
      this.match("/*");

      while((this.LA(1) != '*' || this.LA(2) != '/') && this.LA(1) >= 0 && this.LA(1) <= '\ufffe' && this.LA(2) >= 0 && this.LA(2) <= '\ufffe') {
         this.matchNot('\uffff');
      }

      this.match("*/");
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
      long[] data = new long[2048];
      data[0] = -576460752303423489L;

      for(int i = 1; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[2048];
      data[0] = -549755813889L;

      for(int i = 1; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }

   private static final long[] mk_tokenSet_2() {
      long[] data = new long[2048];
      data[0] = -9217L;

      for(int i = 1; i <= 1022; ++i) {
         data[i] = -1L;
      }

      data[1023] = Long.MAX_VALUE;
      return data;
   }
}
