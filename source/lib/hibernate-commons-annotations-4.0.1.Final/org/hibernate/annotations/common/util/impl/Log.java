package org.hibernate.annotations.common.util.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.Logger.Level;

@MessageLogger(
   projectCode = "HCANN"
)
public interface Log extends BasicLogger {
   @LogMessage(
      level = Level.INFO
   )
   @Message(
      id = 1,
      value = "Hibernate Commons Annotations {%1$s}"
   )
   void version(String var1);

   @LogMessage(
      level = Level.ERROR
   )
   @Message(
      id = 2,
      value = "An assertion failure occurred (this may indicate a bug in Hibernate)"
   )
   void assertionFailure(@Cause Throwable var1);
}
