package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamDef {
   String name();

   String type();
}
