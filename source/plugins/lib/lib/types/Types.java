package lib.types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lib.Lib;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Types implements Listener {
   private Server server;
   private String pn;
   private boolean debug;
   private HashMap typeElementsHash;
   private HashMap cmdHash;
   private HashMap entityHash;
   private HashMap itemHash;

   public Types(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.typeElementsHash = new HashMap();
      this.cmdHash = new HashMap();
      this.entityHash = new HashMap();
      this.itemHash = new HashMap();
      this.typeElementsHash.put("item", ItemElement.class);
      this.typeElementsHash.put("entity", EntityElement.class);
      this.typeElementsHash.put("cmd", CmdElement.class);
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void reloadTypes(String plugin, FileConfiguration config) {
      this.cmdHash.remove(plugin);
      this.entityHash.remove(plugin);
      this.itemHash.remove(plugin);
      if (config.contains("types.cmd")) {
         MemorySection ms = (MemorySection)config.get("types.cmd");
         Map<String, Object> map = ms.getValues(false);

         for(String name : map.keySet()) {
            this.loadCmdType(plugin, config, name);
         }
      }

      if (config.contains("types.entity")) {
         MemorySection ms = (MemorySection)config.get("types.entity");
         Map<String, Object> map = ms.getValues(false);

         for(String name : map.keySet()) {
            this.loadEntityType(plugin, config, name);
         }
      }

      if (config.contains("types.item")) {
         MemorySection ms = (MemorySection)config.get("types.item");
         Map<String, Object> map = ms.getValues(false);

         for(String name : map.keySet()) {
            this.loadItemType(plugin, config, name);
         }
      }

   }

   public boolean checkCmd(String plugin, String type, String cmd) throws InvalidTypeException {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types1", plugin, UtilFormat.format(this.pn, 1200), type));
      }

      if (plugin == null) {
         plugin = this.pn;
      }

      TypeInfo typeInfo = (TypeInfo)((HashMap)this.cmdHash.get(plugin)).get(type);
      if (typeInfo == null) {
         throw new InvalidTypeException();
      } else if (typeInfo.getMode() == 1) {
         return typeInfo.getTypes().has(new CmdElement(cmd));
      } else {
         return !typeInfo.getTypes().has(new CmdElement(cmd));
      }
   }

   public boolean checkEntity(String plugin, String type, String s) throws InvalidTypeException {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types1", plugin, UtilFormat.format(this.pn, 1205), type));
      }

      if (plugin == null) {
         plugin = this.pn;
      }

      TypeInfo typeInfo = (TypeInfo)((HashMap)this.entityHash.get(plugin)).get(type);
      if (typeInfo == null) {
         throw new InvalidTypeException();
      } else if (typeInfo.getMode() == 1) {
         return typeInfo.getTypes().has(new EntityElement(s));
      } else {
         return !typeInfo.getTypes().has(new EntityElement(s));
      }
   }

   public boolean checkItem(String plugin, String type, String s) throws InvalidTypeException {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types1", plugin, UtilFormat.format(this.pn, 1210), type));
      }

      if (plugin == null) {
         plugin = this.pn;
      }

      TypeInfo typeInfo = (TypeInfo)((HashMap)this.itemHash.get(plugin)).get(type);
      if (typeInfo == null) {
         throw new InvalidTypeException();
      } else if (typeInfo.getMode() == 1) {
         return typeInfo.getTypes().has(new ItemElement(s));
      } else {
         return !typeInfo.getTypes().has(new ItemElement(s));
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.debug = config.getBoolean("types.debug");

      try {
         YamlConfiguration typesConfig = new YamlConfiguration();
         typesConfig.load((new File(config.getString("types.path"))).getCanonicalPath());
         this.cmdHash.remove(this.pn);
         this.reloadTypes(this.pn, typesConfig);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private TypeInfo loadCmdType(String plugin, FileConfiguration config, String type) {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types2", plugin, UtilFormat.format(this.pn, 1200), type));
      }

      if (!this.cmdHash.containsKey(plugin)) {
         this.cmdHash.put(plugin, new HashMap());
      }

      if (((HashMap)this.cmdHash.get(plugin)).containsKey(type)) {
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types3", type));
         }

         return (TypeInfo)((HashMap)this.cmdHash.get(plugin)).get(type);
      } else {
         HashList<TypeElement> types = new HashListImpl();
         int mode = config.getInt("types.cmd." + type + ".mode");

         for(String s : config.getStringList("types.cmd." + type + ".types")) {
            types.add(new CmdElement(s));
         }

         TypeInfo typeInfo = new TypeInfo(mode, types);

         for(String need : config.getStringList("types.cmd." + type + ".inherit")) {
            TypeInfo typeInfo2 = this.loadCmdType(plugin, config, need);
            typeInfo.combine(typeInfo2);
         }

         ((HashMap)this.cmdHash.get(plugin)).put(type, typeInfo);
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types4", type));
         }

         return typeInfo;
      }
   }

   private TypeInfo loadEntityType(String plugin, FileConfiguration config, String type) {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types2", plugin, UtilFormat.format(this.pn, 1205), type));
      }

      if (!this.entityHash.containsKey(plugin)) {
         this.entityHash.put(plugin, new HashMap());
      }

      if (((HashMap)this.entityHash.get(plugin)).containsKey(type)) {
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types3", type));
         }

         return (TypeInfo)((HashMap)this.entityHash.get(plugin)).get(type);
      } else {
         HashList<TypeElement> types = new HashListImpl();
         int mode = config.getInt("types.entity." + type + ".mode");

         for(String s : config.getStringList("types.entity." + type + ".types")) {
            types.add(new EntityElement(s));
         }

         TypeInfo typeInfo = new TypeInfo(mode, types);

         for(String need : config.getStringList("types.entity." + type + ".inherit")) {
            TypeInfo typeInfo2 = this.loadEntityType(plugin, config, need);
            typeInfo.combine(typeInfo2);
         }

         ((HashMap)this.entityHash.get(plugin)).put(type, typeInfo);
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types4", type));
         }

         return typeInfo;
      }
   }

   private TypeInfo loadItemType(String plugin, FileConfiguration config, String type) {
      if (this.debug) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "types2", plugin, UtilFormat.format(this.pn, 1210), type));
      }

      if (!this.itemHash.containsKey(plugin)) {
         this.itemHash.put(plugin, new HashMap());
      }

      if (((HashMap)this.itemHash.get(plugin)).containsKey(type)) {
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types3", type));
         }

         return (TypeInfo)((HashMap)this.itemHash.get(plugin)).get(type);
      } else {
         HashList<TypeElement> types = new HashListImpl();
         int mode = config.getInt("types.item." + type + ".mode");

         for(String s : config.getStringList("types.item." + type + ".types")) {
            types.add(new ItemElement(s));
         }

         TypeInfo typeInfo = new TypeInfo(mode, types);

         for(String need : config.getStringList("types.item." + type + ".inherit")) {
            TypeInfo typeInfo2 = this.loadItemType(plugin, config, need);
            typeInfo.combine(typeInfo2);
         }

         ((HashMap)this.itemHash.get(plugin)).put(type, typeInfo);
         if (this.debug) {
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "types4", type));
         }

         return typeInfo;
      }
   }

   public class TypeInfo implements Cloneable {
      private int mode;
      private HashList types;

      public TypeInfo(int mode, HashList ids) {
         this.mode = mode;
         this.types = ids;
      }

      public int getMode() {
         return this.mode;
      }

      public HashList getTypes() {
         return this.types;
      }

      public void combine(TypeInfo typeInfo) {
         if (this.mode == 1) {
            if (typeInfo.getMode() == 1) {
               for(TypeElement id : typeInfo.getTypes()) {
                  this.types.add(id);
               }
            } else {
               HashList<TypeElement> result = typeInfo.getTypes().clone();

               for(TypeElement id : typeInfo.getTypes()) {
                  if (this.types.has(id)) {
                     result.remove(id);
                  }
               }

               this.mode = 2;
               this.types = result;
            }
         } else if (typeInfo.getMode() == 1) {
            for(TypeElement id : this.types) {
               if (typeInfo.getTypes().has(id)) {
                  this.types.remove(id);
               }
            }
         } else {
            HashList<TypeElement> result = new HashListImpl();

            for(TypeElement id : typeInfo.getTypes()) {
               if (this.types.has(id)) {
                  result.add(id);
               }
            }

            this.types = result;
         }

      }

      public TypeInfo clone() {
         try {
            return (TypeInfo)super.clone();
         } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
         }
      }
   }
}
