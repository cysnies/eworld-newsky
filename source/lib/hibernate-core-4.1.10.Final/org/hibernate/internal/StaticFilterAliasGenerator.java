package org.hibernate.internal;

public class StaticFilterAliasGenerator implements FilterAliasGenerator {
   private final String alias;

   public StaticFilterAliasGenerator(String alias) {
      this.alias = alias;
   }

   public String getAlias(String table) {
      return this.alias;
   }
}
