package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.persistence.JoinColumn;

@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumnOrFormula {
   JoinFormula formula() default @JoinFormula(
   value = "",
   referencedColumnName = ""
);

   JoinColumn column() default @JoinColumn;
}
