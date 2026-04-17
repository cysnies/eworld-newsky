package com.earth2me.essentials;

import com.earth2me.essentials.commands.WarpNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Server;

public class Warps implements IConf {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private final Map warpPoints = new HashMap();
   private final File warpsFolder;
   private final Server server;

   public Warps(Server server, File dataFolder) {
      this.server = server;
      this.warpsFolder = new File(dataFolder, "warps");
      if (!this.warpsFolder.exists()) {
         this.warpsFolder.mkdirs();
      }

      this.reloadConfig();
   }

   public boolean isEmpty() {
      return this.warpPoints.isEmpty();
   }

   public Collection getWarpNames() {
      List<String> keys = new ArrayList();

      for(StringIgnoreCase stringIgnoreCase : this.warpPoints.keySet()) {
         keys.add(stringIgnoreCase.getString());
      }

      Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
      return keys;
   }

   public Location getWarp(String warp) throws Exception {
      EssentialsConf conf = (EssentialsConf)this.warpPoints.get(new StringIgnoreCase(warp));
      if (conf == null) {
         throw new WarpNotFoundException();
      } else {
         return conf.getLocation((String)null, this.server);
      }
   }

   public void setWarp(String name, Location loc) throws Exception {
      String filename = Util.sanitizeFileName(name);
      EssentialsConf conf = (EssentialsConf)this.warpPoints.get(new StringIgnoreCase(name));
      if (conf == null) {
         File confFile = new File(this.warpsFolder, filename + ".yml");
         if (confFile.exists()) {
            throw new Exception(I18n._("similarWarpExist"));
         }

         conf = new EssentialsConf(confFile);
         this.warpPoints.put(new StringIgnoreCase(name), conf);
      }

      conf.setProperty((String)null, (Location)loc);
      conf.setProperty("name", (Object)name);

      try {
         conf.saveWithError();
      } catch (IOException var6) {
         throw new IOException(I18n._("invalidWarpName"));
      }
   }

   public void delWarp(String name) throws Exception {
      EssentialsConf conf = (EssentialsConf)this.warpPoints.get(new StringIgnoreCase(name));
      if (conf == null) {
         throw new Exception(I18n._("warpNotExist"));
      } else if (!conf.getFile().delete()) {
         throw new Exception(I18n._("warpDeleteError"));
      } else {
         this.warpPoints.remove(new StringIgnoreCase(name));
      }
   }

   public final void reloadConfig() {
      this.warpPoints.clear();
      File[] listOfFiles = this.warpsFolder.listFiles();
      if (listOfFiles.length >= 1) {
         for(int i = 0; i < listOfFiles.length; ++i) {
            String filename = listOfFiles[i].getName();
            if (listOfFiles[i].isFile() && filename.endsWith(".yml")) {
               try {
                  EssentialsConf conf = new EssentialsConf(listOfFiles[i]);
                  conf.load();
                  String name = conf.getString("name");
                  if (name != null) {
                     this.warpPoints.put(new StringIgnoreCase(name), conf);
                  }
               } catch (Exception ex) {
                  logger.log(Level.WARNING, I18n._("loadWarpError", filename), ex);
               }
            }
         }
      }

   }

   private static class StringIgnoreCase {
      private final String string;

      public StringIgnoreCase(String string) {
         this.string = string;
      }

      public int hashCode() {
         return this.getString().toLowerCase(Locale.ENGLISH).hashCode();
      }

      public boolean equals(Object o) {
         return o instanceof StringIgnoreCase ? this.getString().equalsIgnoreCase(((StringIgnoreCase)o).getString()) : false;
      }

      public String getString() {
         return this.string;
      }
   }
}
