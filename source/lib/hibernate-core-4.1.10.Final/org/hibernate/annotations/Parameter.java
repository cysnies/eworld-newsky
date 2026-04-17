package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
   String name();

   String value();
}
