package org.hibernate.sql.ordering.antlr;

public class OrderingSpecification extends NodeSupport {
   private boolean resolved;
   private Ordering ordering;

   public Ordering getOrdering() {
      if (!this.resolved) {
         this.ordering = resolve(this.getText());
         this.resolved = true;
      }

      return this.ordering;
   }

   private static Ordering resolve(String text) {
      if (OrderingSpecification.Ordering.ASCENDING.name.equals(text)) {
         return OrderingSpecification.Ordering.ASCENDING;
      } else if (OrderingSpecification.Ordering.DESCENDING.name.equals(text)) {
         return OrderingSpecification.Ordering.DESCENDING;
      } else {
         throw new IllegalStateException("Unknown ordering [" + text + "]");
      }
   }

   public String getRenderableText() {
      return this.getOrdering().name;
   }

   public static class Ordering {
      public static final Ordering ASCENDING = new Ordering("asc");
      public static final Ordering DESCENDING = new Ordering("desc");
      private final String name;

      private Ordering(String name) {
         this.name = name;
      }
   }
}
