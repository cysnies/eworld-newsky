package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedNativeQuery {
   String name();

   String query();

   Class resultClass() default void.class;

   String resultSetMapping() default "";

   FlushModeType flushMode() default FlushModeType.PERSISTENCE_CONTEXT;

   boolean cacheable() default false;

   String cacheRegion() default "";

   int fetchSize() default -1;

   int timeout() default -1;

   boolean callable() default false;

   String comment() default "";

   CacheModeType cacheMode() default CacheModeType.NORMAL;

   boolean readOnly() default false;
}
