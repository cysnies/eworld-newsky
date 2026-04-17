package javax.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapKeyJoinColumn {
   String name() default "";

   String referencedColumnName() default "";

   boolean unique() default false;

   boolean nullable() default false;

   boolean insertable() default true;

   boolean updatable() default true;

   String columnDefinition() default "";

   String table() default "";
}
