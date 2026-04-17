package org.hibernate.hql.internal.antlr;

import antlr.LLkParser;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.hql.internal.ast.ErrorReporter;

public class SqlStatementParser extends LLkParser implements SqlStatementParserTokenTypes {
   private ErrorHandler errorHandler;
   private List statementList;
   private StringBuilder current;
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "NOT_STMT_END", "QUOTED_STRING", "STMT_END", "ESCqs", "LINE_COMMENT", "MULTILINE_COMMENT"};
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

   public void reportError(RecognitionException e) {
      this.errorHandler.reportError(e);
   }

   public void reportError(String s) {
      this.errorHandler.reportError(s);
   }

   public void reportWarning(String s) {
      this.errorHandler.reportWarning(s);
   }

   public void throwExceptionIfErrorOccurred() {
      if (this.errorHandler.hasErrors()) {
         throw new StatementParserException(this.errorHandler.getErrorMessage());
      }
   }

   protected void out(String stmt) {
      this.current.append(stmt);
   }

   protected void out(Token token) {
      this.out(token.getText());
   }

   public List getStatementList() {
      return this.statementList;
   }

   protected void statementEnd() {
      this.statementList.add(this.current.toString().trim());
      this.current = new StringBuilder();
   }

   protected SqlStatementParser(TokenBuffer tokenBuf, int k) {
      super(tokenBuf, k);
      this.errorHandler = new ErrorHandler();
      this.statementList = new LinkedList();
      this.current = new StringBuilder();
      this.tokenNames = _tokenNames;
   }

   public SqlStatementParser(TokenBuffer tokenBuf) {
      this((TokenBuffer)tokenBuf, 1);
   }

   protected SqlStatementParser(TokenStream lexer, int k) {
      super(lexer, k);
      this.errorHandler = new ErrorHandler();
      this.statementList = new LinkedList();
      this.current = new StringBuilder();
      this.tokenNames = _tokenNames;
   }

   public SqlStatementParser(TokenStream lexer) {
      this((TokenStream)lexer, 1);
   }

   public SqlStatementParser(ParserSharedInputState state) {
      super(state, 1);
      this.errorHandler = new ErrorHandler();
      this.statementList = new LinkedList();
      this.current = new StringBuilder();
      this.tokenNames = _tokenNames;
   }

   public final void script() throws RecognitionException, TokenStreamException {
      try {
         while(this.LA(1) >= 4 && this.LA(1) <= 6) {
            this.statement();
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

   }

   public final void statement() throws RecognitionException, TokenStreamException {
      Token s = null;
      Token q = null;

      try {
         while(true) {
            switch (this.LA(1)) {
               case 4:
                  s = this.LT(1);
                  this.match(4);
                  this.out(s);
                  break;
               case 5:
                  q = this.LT(1);
                  this.match(5);
                  this.out(q);
                  break;
               default:
                  this.match(6);
                  this.statementEnd();
                  return;
            }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_1);
      }
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[]{2L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[]{114L, 0L};
      return data;
   }

   public class StatementParserException extends RuntimeException {
      public StatementParserException(String message) {
         super(message);
      }
   }

   private class ErrorHandler implements ErrorReporter {
      private List errorList;

      private ErrorHandler() {
         this.errorList = new LinkedList();
      }

      public void reportError(RecognitionException e) {
         this.reportError(e.toString());
      }

      public void reportError(String s) {
         this.errorList.add(s);
      }

      public void reportWarning(String s) {
      }

      public boolean hasErrors() {
         return !this.errorList.isEmpty();
      }

      public String getErrorMessage() {
         StringBuilder buf = new StringBuilder();
         Iterator iterator = this.errorList.iterator();

         while(iterator.hasNext()) {
            buf.append((String)iterator.next());
            if (iterator.hasNext()) {
               buf.append("\n");
            }
         }

         return buf.toString();
      }
   }
}
