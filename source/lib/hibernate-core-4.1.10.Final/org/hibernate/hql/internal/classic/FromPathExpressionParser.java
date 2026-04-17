package org.hibernate.hql.internal.classic;

import org.hibernate.QueryException;
import org.hibernate.type.Type;

public class FromPathExpressionParser extends PathExpressionParser {
   public void end(QueryTranslatorImpl q) throws QueryException {
      if (!this.isCollectionValued()) {
         Type type = this.getPropertyType();
         if (type.isEntityType()) {
            this.token(".", q);
            this.token((String)null, q);
         } else if (type.isCollectionType()) {
            this.token(".", q);
            this.token("elements", q);
         }
      }

      super.end(q);
   }

   protected void setExpectingCollectionIndex() throws QueryException {
      throw new QueryException("illegal syntax near collection-valued path expression in from: " + this.getCollectionName());
   }
}
