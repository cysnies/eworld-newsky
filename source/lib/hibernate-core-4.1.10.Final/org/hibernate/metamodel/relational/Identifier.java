package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;

public class Identifier {
   private final String name;
   private final boolean isQuoted;

   public static Identifier toIdentifier(String name) {
      if (StringHelper.isEmpty(name)) {
         return null;
      } else {
         String trimmedName = name.trim();
         if (isQuoted(trimmedName)) {
            String bareName = trimmedName.substring(1, trimmedName.length() - 1);
            return new Identifier(bareName, true);
         } else {
            return new Identifier(trimmedName, false);
         }
      }
   }

   public static boolean isQuoted(String name) {
      return name.startsWith("`") && name.endsWith("`");
   }

   public Identifier(String name, boolean quoted) {
      if (StringHelper.isEmpty(name)) {
         throw new IllegalIdentifierException("Identifier text cannot be null");
      } else if (isQuoted(name)) {
         throw new IllegalIdentifierException("Identifier text should not contain quote markers (`)");
      } else {
         this.name = name;
         this.isQuoted = quoted;
      }
   }

   public String getName() {
      return this.name;
   }

   public boolean isQuoted() {
      return this.isQuoted;
   }

   public String encloseInQuotesIfQuoted(Dialect dialect) {
      return this.isQuoted ? (new StringBuilder(this.name.length() + 2)).append(dialect.openQuote()).append(this.name).append(dialect.closeQuote()).toString() : this.name;
   }

   public String toString() {
      return this.isQuoted ? '`' + this.getName() + '`' : this.getName();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Identifier that = (Identifier)o;
         return this.isQuoted == that.isQuoted && this.name.equals(that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }
}
