package org.hibernate.hql.internal.ast.util;

import org.hibernate.internal.util.StringHelper;

public class AliasGenerator {
   private int next = 0;

   private int nextCount() {
      return this.next++;
   }

   public String createName(String name) {
      return StringHelper.generateAlias(name, this.nextCount());
   }
}
