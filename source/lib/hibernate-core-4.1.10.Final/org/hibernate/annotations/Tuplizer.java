package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.hibernate.EntityMode;

@java.lang.annotation.Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Tuplizer {
   Class impl();

   /** @deprecated */
   @Deprecated
   String entityMode() default "pojo";

   EntityMode entityModeType() default EntityMode.POJO;
}
