package org.hibernate.hql.internal.classic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.QueryException;

public class ClauseParser implements Parser {
   private Parser child;
   private List selectTokens;
   private boolean cacheSelectTokens = false;
   private boolean byExpected = false;
   private int parenCount = 0;

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      String lcToken = token.toLowerCase();
      if ("(".equals(token)) {
         ++this.parenCount;
      } else if (")".equals(token)) {
         --this.parenCount;
      }

      if (this.byExpected && !lcToken.equals("by")) {
         throw new QueryException("BY expected after GROUP or ORDER: " + token);
      } else {
         boolean isClauseStart = this.parenCount == 0;
         if (isClauseStart) {
            if (lcToken.equals("select")) {
               this.selectTokens = new ArrayList();
               this.cacheSelectTokens = true;
            } else if (lcToken.equals("from")) {
               this.child = new FromParser();
               this.child.start(q);
               this.cacheSelectTokens = false;
            } else if (lcToken.equals("where")) {
               this.endChild(q);
               this.child = new WhereParser();
               this.child.start(q);
            } else if (lcToken.equals("order")) {
               this.endChild(q);
               this.child = new OrderByParser();
               this.byExpected = true;
            } else if (lcToken.equals("having")) {
               this.endChild(q);
               this.child = new HavingParser();
               this.child.start(q);
            } else if (lcToken.equals("group")) {
               this.endChild(q);
               this.child = new GroupByParser();
               this.byExpected = true;
            } else if (lcToken.equals("by")) {
               if (!this.byExpected) {
                  throw new QueryException("GROUP or ORDER expected before BY");
               }

               this.child.start(q);
               this.byExpected = false;
            } else {
               isClauseStart = false;
            }
         }

         if (!isClauseStart) {
            if (this.cacheSelectTokens) {
               this.selectTokens.add(token);
            } else {
               if (this.child == null) {
                  throw new QueryException("query must begin with SELECT or FROM: " + token);
               }

               this.child.token(token, q);
            }
         }

      }
   }

   private void endChild(QueryTranslatorImpl q) throws QueryException {
      if (this.child == null) {
         this.cacheSelectTokens = false;
      } else {
         this.child.end(q);
      }

   }

   public void start(QueryTranslatorImpl q) {
   }

   public void end(QueryTranslatorImpl q) throws QueryException {
      this.endChild(q);
      if (this.selectTokens != null) {
         this.child = new SelectParser();
         this.child.start(q);
         Iterator iter = this.selectTokens.iterator();

         while(iter.hasNext()) {
            this.token((String)iter.next(), q);
         }

         this.child.end(q);
      }

      this.byExpected = false;
      this.parenCount = 0;
      this.cacheSelectTokens = false;
   }
}
