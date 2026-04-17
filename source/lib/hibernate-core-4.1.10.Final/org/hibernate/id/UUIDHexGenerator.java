package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class UUIDHexGenerator extends AbstractUUIDGenerator implements Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, UUIDHexGenerator.class.getName());
   private static boolean warned = false;
   private String sep = "";

   public UUIDHexGenerator() {
      if (!warned) {
         warned = true;
         LOG.usingUuidHexGenerator(this.getClass().getName(), UUIDGenerator.class.getName());
      }

   }

   public void configure(Type type, Properties params, Dialect d) {
      this.sep = ConfigurationHelper.getString("separator", params, "");
   }

   public Serializable generate(SessionImplementor session, Object obj) {
      return (new StringBuilder(36)).append(this.format(this.getIP())).append(this.sep).append(this.format(this.getJVM())).append(this.sep).append(this.format(this.getHiTime())).append(this.sep).append(this.format(this.getLoTime())).append(this.sep).append(this.format(this.getCount())).toString();
   }

   protected String format(int intValue) {
      String formatted = Integer.toHexString(intValue);
      StringBuilder buf = new StringBuilder("00000000");
      buf.replace(8 - formatted.length(), 8, formatted);
      return buf.toString();
   }

   protected String format(short shortValue) {
      String formatted = Integer.toHexString(shortValue);
      StringBuilder buf = new StringBuilder("0000");
      buf.replace(4 - formatted.length(), 4, formatted);
      return buf.toString();
   }
}
