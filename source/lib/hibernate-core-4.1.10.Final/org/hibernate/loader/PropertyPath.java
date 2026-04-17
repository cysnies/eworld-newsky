package org.hibernate.loader;

import org.hibernate.internal.util.StringHelper;

public class PropertyPath {
   private final PropertyPath parent;
   private final String property;
   private final String fullPath;

   public PropertyPath(PropertyPath parent, String property) {
      this.parent = parent;
      this.property = property;
      if ("_identifierMapper".equals(property)) {
         this.fullPath = parent != null ? parent.getFullPath() : "";
      } else {
         String prefix;
         if (parent != null) {
            String resolvedParent = parent.getFullPath();
            if (StringHelper.isEmpty(resolvedParent)) {
               prefix = "";
            } else {
               prefix = resolvedParent + '.';
            }
         } else {
            prefix = "";
         }

         this.fullPath = prefix + property;
      }

   }

   public PropertyPath(String property) {
      this((PropertyPath)null, property);
   }

   public PropertyPath() {
      this("");
   }

   public PropertyPath append(String property) {
      return new PropertyPath(this, property);
   }

   public PropertyPath getParent() {
      return this.parent;
   }

   public String getProperty() {
      return this.property;
   }

   public String getFullPath() {
      return this.fullPath;
   }

   public boolean isRoot() {
      return this.parent == null && StringHelper.isEmpty(this.property);
   }

   public String toString() {
      return this.getClass().getSimpleName() + '[' + this.fullPath + ']';
   }
}
