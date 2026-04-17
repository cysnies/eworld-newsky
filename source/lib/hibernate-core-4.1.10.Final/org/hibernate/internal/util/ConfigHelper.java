package org.hibernate.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public final class ConfigHelper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ConfigHelper.class.getName());

   public static URL locateConfig(String path) {
      try {
         return new URL(path);
      } catch (MalformedURLException var2) {
         return findAsResource(path);
      }
   }

   public static URL findAsResource(String path) {
      URL url = null;
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      if (contextClassLoader != null) {
         url = contextClassLoader.getResource(path);
      }

      if (url != null) {
         return url;
      } else {
         url = ConfigHelper.class.getClassLoader().getResource(path);
         if (url != null) {
            return url;
         } else {
            url = ClassLoader.getSystemClassLoader().getResource(path);
            return url;
         }
      }
   }

   public static InputStream getConfigStream(String path) throws HibernateException {
      URL url = locateConfig(path);
      if (url == null) {
         String msg = LOG.unableToLocateConfigFile(path);
         LOG.error(msg);
         throw new HibernateException(msg);
      } else {
         try {
            return url.openStream();
         } catch (IOException e) {
            throw new HibernateException("Unable to open config file: " + path, e);
         }
      }
   }

   public static Reader getConfigStreamReader(String path) throws HibernateException {
      return new InputStreamReader(getConfigStream(path));
   }

   public static Properties getConfigProperties(String path) throws HibernateException {
      try {
         Properties properties = new Properties();
         properties.load(getConfigStream(path));
         return properties;
      } catch (IOException e) {
         throw new HibernateException("Unable to load properties from specified config file: " + path, e);
      }
   }

   private ConfigHelper() {
   }

   public static InputStream getResourceAsStream(String resource) {
      String stripped = resource.startsWith("/") ? resource.substring(1) : resource;
      InputStream stream = null;
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) {
         stream = classLoader.getResourceAsStream(stripped);
      }

      if (stream == null) {
         stream = Environment.class.getResourceAsStream(resource);
      }

      if (stream == null) {
         stream = Environment.class.getClassLoader().getResourceAsStream(stripped);
      }

      if (stream == null) {
         throw new HibernateException(resource + " not found");
      } else {
         return stream;
      }
   }

   public static InputStream getUserResourceAsStream(String resource) {
      boolean hasLeadingSlash = resource.startsWith("/");
      String stripped = hasLeadingSlash ? resource.substring(1) : resource;
      InputStream stream = null;
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (classLoader != null) {
         stream = classLoader.getResourceAsStream(resource);
         if (stream == null && hasLeadingSlash) {
            stream = classLoader.getResourceAsStream(stripped);
         }
      }

      if (stream == null) {
         stream = Environment.class.getClassLoader().getResourceAsStream(resource);
      }

      if (stream == null && hasLeadingSlash) {
         stream = Environment.class.getClassLoader().getResourceAsStream(stripped);
      }

      if (stream == null) {
         throw new HibernateException(resource + " not found");
      } else {
         return stream;
      }
   }
}
