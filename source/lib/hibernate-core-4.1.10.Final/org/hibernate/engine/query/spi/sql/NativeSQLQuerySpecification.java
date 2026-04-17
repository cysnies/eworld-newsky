package org.hibernate.engine.query.spi.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.internal.util.collections.ArrayHelper;

public class NativeSQLQuerySpecification {
   private final String queryString;
   private final NativeSQLQueryReturn[] queryReturns;
   private final Set querySpaces;
   private final int hashCode;

   public NativeSQLQuerySpecification(String queryString, NativeSQLQueryReturn[] queryReturns, Collection querySpaces) {
      this.queryString = queryString;
      this.queryReturns = queryReturns;
      if (querySpaces == null) {
         this.querySpaces = Collections.EMPTY_SET;
      } else {
         Set tmp = new HashSet();
         tmp.addAll(querySpaces);
         this.querySpaces = Collections.unmodifiableSet(tmp);
      }

      int hashCode = queryString.hashCode();
      hashCode = 29 * hashCode + this.querySpaces.hashCode();
      if (this.queryReturns != null) {
         hashCode = 29 * hashCode + ArrayHelper.toList((Object)this.queryReturns).hashCode();
      }

      this.hashCode = hashCode;
   }

   public String getQueryString() {
      return this.queryString;
   }

   public NativeSQLQueryReturn[] getQueryReturns() {
      return this.queryReturns;
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NativeSQLQuerySpecification that = (NativeSQLQuerySpecification)o;
         return this.querySpaces.equals(that.querySpaces) && this.queryString.equals(that.queryString) && Arrays.equals(this.queryReturns, that.queryReturns);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }
}
