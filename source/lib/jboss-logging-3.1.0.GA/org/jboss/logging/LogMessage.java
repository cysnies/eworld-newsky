package org.jboss.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Documented
public @interface LogMessage {
   Logger.Level level() default Logger.Level.INFO;

   Class loggingClass() default Void.class;
}
