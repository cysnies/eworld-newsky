package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** @deprecated */
@java.lang.annotation.Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Entity {
   /** @deprecated */
   boolean mutable() default true;

   /** @deprecated */
   boolean dynamicInsert() default false;

   /** @deprecated */
   boolean dynamicUpdate() default false;

   /** @deprecated */
   boolean selectBeforeUpdate() default false;

   /** @deprecated */
   PolymorphismType polymorphism() default PolymorphismType.IMPLICIT;

   /** @deprecated */
   OptimisticLockType optimisticLock() default OptimisticLockType.VERSION;

   /** @deprecated */
   String persister() default "";
}
