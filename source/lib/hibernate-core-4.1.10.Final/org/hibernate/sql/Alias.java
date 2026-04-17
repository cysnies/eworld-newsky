package org.hibernate.sql;

public final class Alias {
   private final int length;
   private final String suffix;

   public Alias(int length, String suffix) {
      this.length = suffix == null ? length : length - suffix.length();
      this.suffix = suffix;
   }

   public Alias(String suffix) {
      this.length = Integer.MAX_VALUE;
      this.suffix = suffix;
   }

   public String toAliasString(String sqlIdentifier) {
      char begin = sqlIdentifier.charAt(0);
      int quoteType = "`\"[".indexOf(begin);
      String unquoted = this.getUnquotedAliasString(sqlIdentifier, quoteType);
      if (quoteType >= 0) {
         char endQuote = "`\"]".charAt(quoteType);
         return begin + unquoted + endQuote;
      } else {
         return unquoted;
      }
   }

   public String toUnquotedAliasString(String sqlIdentifier) {
      return this.getUnquotedAliasString(sqlIdentifier);
   }

   private String getUnquotedAliasString(String sqlIdentifier) {
      char begin = sqlIdentifier.charAt(0);
      int quoteType = "`\"[".indexOf(begin);
      return this.getUnquotedAliasString(sqlIdentifier, quoteType);
   }

   private String getUnquotedAliasString(String sqlIdentifier, int quoteType) {
      String unquoted = sqlIdentifier;
      if (quoteType >= 0) {
         unquoted = sqlIdentifier.substring(1, sqlIdentifier.length() - 1);
      }

      if (unquoted.length() > this.length) {
         unquoted = unquoted.substring(0, this.length);
      }

      return this.suffix == null ? unquoted : unquoted + this.suffix;
   }

   public String[] toUnquotedAliasStrings(String[] sqlIdentifiers) {
      String[] aliases = new String[sqlIdentifiers.length];

      for(int i = 0; i < sqlIdentifiers.length; ++i) {
         aliases[i] = this.toUnquotedAliasString(sqlIdentifiers[i]);
      }

      return aliases;
   }

   public String[] toAliasStrings(String[] sqlIdentifiers) {
      String[] aliases = new String[sqlIdentifiers.length];

      for(int i = 0; i < sqlIdentifiers.length; ++i) {
         aliases[i] = this.toAliasString(sqlIdentifiers[i]);
      }

      return aliases;
   }
}
