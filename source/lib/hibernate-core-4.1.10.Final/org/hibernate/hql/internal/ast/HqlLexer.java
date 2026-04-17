package org.hibernate.hql.internal.ast;

import antlr.Token;
import java.io.InputStream;
import java.io.Reader;
import org.hibernate.QueryException;
import org.hibernate.hql.internal.antlr.HqlBaseLexer;

class HqlLexer extends HqlBaseLexer {
   private boolean possibleID = false;

   public HqlLexer(InputStream in) {
      super(in);
   }

   public HqlLexer(Reader in) {
      super(in);
   }

   public void setTokenObjectClass(String cl) {
      Thread thread = null;
      ClassLoader contextClassLoader = null;

      try {
         thread = Thread.currentThread();
         contextClassLoader = thread.getContextClassLoader();
         thread.setContextClassLoader(HqlToken.class.getClassLoader());
         super.setTokenObjectClass(HqlToken.class.getName());
      } finally {
         thread.setContextClassLoader(contextClassLoader);
      }

   }

   protected void setPossibleID(boolean possibleID) {
      this.possibleID = possibleID;
   }

   protected Token makeToken(int i) {
      HqlToken token = (HqlToken)super.makeToken(i);
      token.setPossibleID(this.possibleID);
      this.possibleID = false;
      return token;
   }

   public int testLiteralsTable(int i) {
      int ttype = super.testLiteralsTable(i);
      return ttype;
   }

   public void panic() {
      this.panic("CharScanner: panic");
   }

   public void panic(String s) {
      throw new QueryException(s);
   }
}
