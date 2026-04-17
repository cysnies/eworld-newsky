package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
   String appliesTo();

   Index[] indexes() default {};

   String comment() default "";

   ForeignKey foreignKey() default @ForeignKey(
   name = ""
);

   FetchMode fetch() default FetchMode.JOIN;

   boolean inverse() default false;

   boolean optional() default true;

   SQLInsert sqlInsert() default @SQLInsert(
   sql = ""
);

   SQLUpdate sqlUpdate() default @SQLUpdate(
   sql = ""
);

   SQLDelete sqlDelete() default @SQLDelete(
   sql = ""
);
}
