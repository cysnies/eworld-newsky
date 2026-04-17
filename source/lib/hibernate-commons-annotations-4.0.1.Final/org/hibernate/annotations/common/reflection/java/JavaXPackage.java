package org.hibernate.annotations.common.reflection.java;

import org.hibernate.annotations.common.reflection.XPackage;

class JavaXPackage extends JavaXAnnotatedElement implements XPackage {
   public JavaXPackage(Package pkg, JavaReflectionManager factory) {
      super(pkg, factory);
   }

   public String getName() {
      return ((Package)this.toAnnotatedElement()).getName();
   }
}
