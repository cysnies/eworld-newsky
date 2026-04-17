package org.hibernate.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.Version;
import org.hibernate.bytecode.internal.javassist.BytecodeProviderImpl;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ConfigHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.jboss.logging.Logger;

public final class Environment implements AvailableSettings {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Environment.class.getName());
   private static final BytecodeProvider BYTECODE_PROVIDER_INSTANCE;
   private static final boolean ENABLE_BINARY_STREAMS;
   private static final boolean ENABLE_REFLECTION_OPTIMIZER;
   private static final boolean JVM_HAS_TIMESTAMP_BUG;
   private static final Properties GLOBAL_PROPERTIES;
   private static final Map ISOLATION_LEVELS;
   private static final Map OBSOLETE_PROPERTIES = new HashMap();
   private static final Map RENAMED_PROPERTIES = new HashMap();

   public static void verifyProperties(Map configurationValues) {
      Map propertiesToAdd = new HashMap();

      for(Map.Entry entry : configurationValues.entrySet()) {
         Object replacementKey = OBSOLETE_PROPERTIES.get(entry.getKey());
         if (replacementKey != null) {
            LOG.unsupportedProperty(entry.getKey(), replacementKey);
         }

         Object renamedKey = RENAMED_PROPERTIES.get(entry.getKey());
         if (renamedKey != null) {
            LOG.renamedProperty(entry.getKey(), renamedKey);
            propertiesToAdd.put(renamedKey, entry.getValue());
         }
      }

      configurationValues.putAll(propertiesToAdd);
   }

   public static BytecodeProvider getBytecodeProvider() {
      return BYTECODE_PROVIDER_INSTANCE;
   }

   public static boolean jvmHasTimestampBug() {
      return JVM_HAS_TIMESTAMP_BUG;
   }

   public static boolean useStreamsForBinary() {
      return ENABLE_BINARY_STREAMS;
   }

   public static boolean useReflectionOptimizer() {
      return ENABLE_REFLECTION_OPTIMIZER;
   }

   private Environment() {
      throw new UnsupportedOperationException();
   }

   public static Properties getProperties() {
      Properties copy = new Properties();
      copy.putAll(GLOBAL_PROPERTIES);
      return copy;
   }

   public static String isolationLevelToString(int isolation) {
      return (String)ISOLATION_LEVELS.get(isolation);
   }

   public static BytecodeProvider buildBytecodeProvider(Properties properties) {
      String provider = ConfigurationHelper.getString("hibernate.bytecode.provider", properties, "javassist");
      LOG.bytecodeProvider(provider);
      return buildBytecodeProvider(provider);
   }

   private static BytecodeProvider buildBytecodeProvider(String providerName) {
      if ("javassist".equals(providerName)) {
         return new BytecodeProviderImpl();
      } else {
         LOG.unknownBytecodeProvider(providerName);
         return new BytecodeProviderImpl();
      }
   }

   static {
      Version.logVersion();
      Map<Integer, String> temp = new HashMap();
      temp.put(0, "NONE");
      temp.put(1, "READ_UNCOMMITTED");
      temp.put(2, "READ_COMMITTED");
      temp.put(4, "REPEATABLE_READ");
      temp.put(8, "SERIALIZABLE");
      ISOLATION_LEVELS = Collections.unmodifiableMap(temp);
      GLOBAL_PROPERTIES = new Properties();
      GLOBAL_PROPERTIES.setProperty("hibernate.bytecode.use_reflection_optimizer", Boolean.FALSE.toString());

      try {
         InputStream stream = ConfigHelper.getResourceAsStream("/hibernate.properties");

         try {
            GLOBAL_PROPERTIES.load(stream);
            LOG.propertiesLoaded(ConfigurationHelper.maskOut(GLOBAL_PROPERTIES, "hibernate.connection.password"));
         } catch (Exception var14) {
            LOG.unableToLoadProperties();
         } finally {
            try {
               stream.close();
            } catch (IOException ioe) {
               LOG.unableToCloseStreamError(ioe);
            }

         }
      } catch (HibernateException var16) {
         LOG.propertiesNotFound();
      }

      try {
         GLOBAL_PROPERTIES.putAll(System.getProperties());
      } catch (SecurityException var12) {
         LOG.unableToCopySystemProperties();
      }

      verifyProperties(GLOBAL_PROPERTIES);
      ENABLE_BINARY_STREAMS = ConfigurationHelper.getBoolean("hibernate.jdbc.use_streams_for_binary", GLOBAL_PROPERTIES);
      if (ENABLE_BINARY_STREAMS) {
         LOG.usingStreams();
      }

      ENABLE_REFLECTION_OPTIMIZER = ConfigurationHelper.getBoolean("hibernate.bytecode.use_reflection_optimizer", GLOBAL_PROPERTIES);
      if (ENABLE_REFLECTION_OPTIMIZER) {
         LOG.usingReflectionOptimizer();
      }

      BYTECODE_PROVIDER_INSTANCE = buildBytecodeProvider(GLOBAL_PROPERTIES);
      long x = 123456789L;
      JVM_HAS_TIMESTAMP_BUG = (new Timestamp(x)).getTime() != x;
      if (JVM_HAS_TIMESTAMP_BUG) {
         LOG.usingTimestampWorkaround();
      }

   }
}
