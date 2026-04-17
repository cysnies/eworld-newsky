package com.earth2me.essentials;

import com.earth2me.essentials.api.II18n;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class I18n implements II18n {
   private static I18n instance;
   private static final String MESSAGES = "messages";
   private final transient Locale defaultLocale = Locale.getDefault();
   private transient Locale currentLocale;
   private transient ResourceBundle customBundle;
   private transient ResourceBundle localeBundle;
   private final transient ResourceBundle defaultBundle;
   private final transient Map messageFormatCache;
   private final transient IEssentials ess;

   public I18n(IEssentials ess) {
      this.currentLocale = this.defaultLocale;
      this.messageFormatCache = new HashMap();
      this.ess = ess;
      this.customBundle = ResourceBundle.getBundle("messages", this.defaultLocale, new FileResClassLoader(I18n.class.getClassLoader(), ess));
      this.localeBundle = ResourceBundle.getBundle("messages", this.defaultLocale);
      this.defaultBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
   }

   public void onEnable() {
      instance = this;
   }

   public void onDisable() {
      instance = null;
   }

   public Locale getCurrentLocale() {
      return this.currentLocale;
   }

   public String translate(String string) {
      try {
         try {
            return this.customBundle.getString(string);
         } catch (MissingResourceException var3) {
            return this.localeBundle.getString(string);
         }
      } catch (MissingResourceException ex) {
         Logger.getLogger("Minecraft").log(Level.WARNING, String.format("Missing translation key \"%s\" in translation file %s", ex.getKey(), this.localeBundle.getLocale().toString()), ex);
         return this.defaultBundle.getString(string);
      }
   }

   public static String _(String string, Object... objects) {
      if (instance == null) {
         return "";
      } else {
         return objects.length == 0 ? instance.translate(string) : instance.format(string, objects);
      }
   }

   public String format(String string, Object... objects) {
      String format = this.translate(string);
      MessageFormat messageFormat = (MessageFormat)this.messageFormatCache.get(format);
      if (messageFormat == null) {
         try {
            messageFormat = new MessageFormat(format);
         } catch (IllegalArgumentException e) {
            this.ess.getLogger().log(Level.SEVERE, "Invalid Translation key for '" + string + "': " + e.getMessage());
            format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
            messageFormat = new MessageFormat(format);
         }

         this.messageFormatCache.put(format, messageFormat);
      }

      return messageFormat.format(objects);
   }

   public void updateLocale(String loc) {
      if (loc != null && !loc.isEmpty()) {
         String[] parts = loc.split("[_\\.]");
         if (parts.length == 1) {
            this.currentLocale = new Locale(parts[0]);
         }

         if (parts.length == 2) {
            this.currentLocale = new Locale(parts[0], parts[1]);
         }

         if (parts.length == 3) {
            this.currentLocale = new Locale(parts[0], parts[1], parts[2]);
         }

         ResourceBundle.clearCache();
         Logger.getLogger("Minecraft").log(Level.INFO, String.format("Using locale %s", this.currentLocale.toString()));
         this.customBundle = ResourceBundle.getBundle("messages", this.currentLocale, new FileResClassLoader(I18n.class.getClassLoader(), this.ess));
         this.localeBundle = ResourceBundle.getBundle("messages", this.currentLocale);
      }
   }

   public static String capitalCase(String input) {
      return input != null && input.length() != 0 ? input.toUpperCase(Locale.ENGLISH).charAt(0) + input.toLowerCase(Locale.ENGLISH).substring(1) : input;
   }

   private static class FileResClassLoader extends ClassLoader {
      private final transient File dataFolder;

      FileResClassLoader(ClassLoader classLoader, IEssentials ess) {
         super(classLoader);
         this.dataFolder = ess.getDataFolder();
      }

      public URL getResource(String string) {
         File file = new File(this.dataFolder, string);
         if (file.exists()) {
            try {
               return file.toURI().toURL();
            } catch (MalformedURLException var4) {
            }
         }

         return super.getResource(string);
      }

      public InputStream getResourceAsStream(String string) {
         File file = new File(this.dataFolder, string);
         if (file.exists()) {
            try {
               return new FileInputStream(file);
            } catch (FileNotFoundException var4) {
            }
         }

         return super.getResourceAsStream(string);
      }
   }
}
