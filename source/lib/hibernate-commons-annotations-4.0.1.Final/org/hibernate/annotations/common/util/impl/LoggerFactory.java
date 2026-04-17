package org.hibernate.annotations.common.util.impl;

import org.jboss.logging.Logger;

public class LoggerFactory {
   public static Log make(String category) {
      return (Log)Logger.getMessageLogger(Log.class, category);
   }
}
