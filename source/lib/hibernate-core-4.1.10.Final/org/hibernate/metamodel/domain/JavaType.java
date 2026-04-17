package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;
import org.hibernate.service.classloading.spi.ClassLoaderService;

public class JavaType {
   private final String name;
   private final ValueHolder classReference;

   public JavaType(final String name, final ClassLoaderService classLoaderService) {
      this.name = name;
      this.classReference = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public Class initialize() {
            return classLoaderService.classForName(name);
         }
      });
   }

   public JavaType(Class theClass) {
      this.name = theClass.getName();
      this.classReference = new ValueHolder(theClass);
   }

   public String getName() {
      return this.name;
   }

   public Class getClassReference() {
      return (Class)this.classReference.getValue();
   }

   public String toString() {
      return super.toString() + "[name=" + this.name + "]";
   }
}
