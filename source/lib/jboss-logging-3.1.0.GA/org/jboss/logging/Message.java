package org.jboss.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Message {
   int NONE = 0;
   int INHERIT = -1;

   int id() default -1;

   String value();

   Format format() default Message.Format.PRINTF;

   public static enum Format {
      PRINTF,
      MESSAGE_FORMAT;
   }
}
