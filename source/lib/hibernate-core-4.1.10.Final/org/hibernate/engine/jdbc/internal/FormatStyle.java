package org.hibernate.engine.jdbc.internal;

public enum FormatStyle {
   BASIC("basic", new BasicFormatterImpl()),
   DDL("ddl", new DDLFormatterImpl()),
   NONE("none", new NoFormatImpl());

   private final String name;
   private final Formatter formatter;

   private FormatStyle(String name, Formatter formatter) {
      this.name = name;
      this.formatter = formatter;
   }

   public String getName() {
      return this.name;
   }

   public Formatter getFormatter() {
      return this.formatter;
   }

   private static class NoFormatImpl implements Formatter {
      private NoFormatImpl() {
      }

      public String format(String source) {
         return source;
      }
   }
}
