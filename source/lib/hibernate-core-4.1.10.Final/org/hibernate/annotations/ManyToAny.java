package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.persistence.Column;
import javax.persistence.FetchType;

@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToAny {
   String metaDef() default "";

   Column metaColumn();

   FetchType fetch() default FetchType.EAGER;
}
