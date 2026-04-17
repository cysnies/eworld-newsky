package org.jboss.logging;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

final class Slf4jLoggerProvider extends AbstractLoggerProvider implements LoggerProvider {
   public Logger getLogger(String name) {
      org.slf4j.Logger l = LoggerFactory.getLogger(name);

      try {
         return new Slf4jLocationAwareLogger(name, (LocationAwareLogger)l);
      } catch (Throwable var4) {
         return new Slf4jLogger(name, l);
      }
   }

   public Object putMdc(String key, Object value) {
      String var3;
      try {
         var3 = org.slf4j.MDC.get(key);
      } finally {
         if (value == null) {
            org.slf4j.MDC.remove(key);
         } else {
            org.slf4j.MDC.put(key, String.valueOf(value));
         }

      }

      return var3;
   }

   public Object getMdc(String key) {
      return org.slf4j.MDC.get(key);
   }

   public void removeMdc(String key) {
      org.slf4j.MDC.remove(key);
   }

   public Map getMdcMap() {
      return org.slf4j.MDC.getCopyOfContextMap();
   }
}
