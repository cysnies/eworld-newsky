package org.hibernate.engine.profile;

public class Fetch {
   private final Association association;
   private final Style style;

   public Fetch(Association association, Style style) {
      this.association = association;
      this.style = style;
   }

   public Association getAssociation() {
      return this.association;
   }

   public Style getStyle() {
      return this.style;
   }

   public String toString() {
      return "Fetch[" + this.style + "{" + this.association.getRole() + "}]";
   }

   public static enum Style {
      JOIN("join"),
      SELECT("select");

      private final String name;

      private Style(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public static Style parse(String name) {
         return SELECT.name.equals(name) ? SELECT : JOIN;
      }
   }
}
