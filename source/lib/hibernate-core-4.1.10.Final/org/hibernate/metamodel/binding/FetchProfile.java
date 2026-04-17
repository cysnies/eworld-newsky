package org.hibernate.metamodel.binding;

import java.util.Collections;
import java.util.Set;

public class FetchProfile {
   private final String name;
   private final Set fetches;

   public FetchProfile(String name, Set fetches) {
      this.name = name;
      this.fetches = fetches;
   }

   public String getName() {
      return this.name;
   }

   public Set getFetches() {
      return Collections.unmodifiableSet(this.fetches);
   }

   public void addFetch(String entity, String association, String style) {
      this.fetches.add(new Fetch(entity, association, style));
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
