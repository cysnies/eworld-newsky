package org.hibernate.internal.jaxb;

public class JaxbRoot {
   private final Object root;
   private final Origin origin;

   public JaxbRoot(Object root, Origin origin) {
      this.root = root;
      this.origin = origin;
   }

   public Object getRoot() {
      return this.root;
   }

   public Origin getOrigin() {
      return this.origin;
   }
}
