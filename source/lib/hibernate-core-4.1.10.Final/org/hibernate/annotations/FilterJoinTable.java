package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterJoinTable {
   String name();

   String condition() default "";

   boolean deduceAliasInjectionPoints() default true;

   SqlFragmentAlias[] aliases() default {};
}
