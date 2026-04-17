package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FetchProfile {
   String name();

   FetchOverride[] fetchOverrides();

   @java.lang.annotation.Target({ElementType.TYPE, ElementType.PACKAGE})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface FetchOverride {
      Class entity();

      String association();

      FetchMode mode();
   }
}
