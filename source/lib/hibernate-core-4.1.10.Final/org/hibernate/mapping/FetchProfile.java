package org.hibernate.mapping;

import java.util.LinkedHashSet;

public class FetchProfile {
   private final String name;
   private final MetadataSource source;
   private LinkedHashSet fetches = new LinkedHashSet();

   public FetchProfile(String name, MetadataSource source) {
      this.name = name;
      this.source = source;
   }

   public String getName() {
      return this.name;
   }

   public MetadataSource getSource() {
      return this.source;
   }

   public LinkedHashSet getFetches() {
      return this.fetches;
   }

   public void addFetch(String entity, String association, String style) {
      this.fetches.add(new Fetch(entity, association, style));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FetchProfile that = (FetchProfile)o;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public static class Fetch {
      private final String entity;
      private final String association;
      private final String style;

      public Fetch(String entity, String association, String style) {
         this.entity = entity;
         this.association = association;
         this.style = style;
      }

      public String getEntity() {
         return this.entity;
      }

      public String getAssociation() {
         return this.association;
      }

      public String getStyle() {
         return this.style;
      }
   }
}
