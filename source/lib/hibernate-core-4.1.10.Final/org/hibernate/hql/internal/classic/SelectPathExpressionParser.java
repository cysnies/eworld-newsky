package org.hibernate.hql.internal.classic;

import org.hibernate.QueryException;

public class SelectPathExpressionParser extends PathExpressionParser {
   public void end(QueryTranslatorImpl q) throws QueryException {
      if (this.getCurrentProperty() != null && !q.isShallowQuery()) {
         this.token(".", q);
         this.token((String)null, q);
      }

      super.end(q);
   }

   protected void setExpectingCollectionIndex() throws QueryException {
      throw new QueryException("illegal syntax near collection-valued path expression in select: " + this.getCollectionName());
   }

   public String getSelectName() {
      return this.getCurrentName();
   }
}
