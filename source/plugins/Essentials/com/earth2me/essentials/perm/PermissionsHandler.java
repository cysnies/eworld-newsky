package com.earth2me.essentials.perm;

import com.earth2me.essentials.Util;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class PermissionsHandler implements IPermissionsHandler {
   private transient IPermissionsHandler handler = new NullPermissionsHandler();
   private transient String defaultGroup = "default";
   private final transient Plugin plugin;
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private transient boolean useSuperperms = false;

   public PermissionsHandler(Plugin plugin) {
      this.plugin = plugin;
   }

   public PermissionsHandler(Plugin plugin, boolean useSuperperms) {
      this.plugin = plugin;
      this.useSuperperms = useSuperperms;
   }

   public PermissionsHandler(Plugin plugin, String defaultGroup) {
      this.plugin = plugin;
      this.defaultGroup = defaultGroup;
   }

   public String getGroup(Player base) {
      String group = this.handler.getGroup(base);
      if (group == null) {
         group = this.defaultGroup;
      }

      return group;
   }

   public List getGroups(Player base) {
      List<String> groups = this.handler.getGroups(base);
      if (groups == null || groups.isEmpty()) {
         groups = Collections.singletonList(this.defaultGroup);
      }

      return Collections.unmodifiableList(groups);
   }

   public boolean canBuild(Player base, String group) {
      return this.handler.canBuild(base, group);
   }

   public boolean inGroup(Player base, String group) {
      return this.handler.inGroup(base, group);
   }

   public boolean hasPermission(Player base, String node) {
      return this.handler.hasPermission(base, node);
   }

   public String getPrefix(Player base) {
      String prefix = this.handler.getPrefix(base);
      if (prefix == null) {
         prefix = "";
      }

      return prefix;
   }

   public String getSuffix(Player base) {
      String suffix = this.handler.getSuffix(base);
      if (suffix == null) {
         suffix = "";
      }

      return suffix;
   }

   public void checkPermissions() {
      PluginManager pluginManager = this.plugin.getServer().getPluginManager();
      Plugin permExPlugin = pluginManager.getPlugin("PermissionsEx");
      if (permExPlugin != null && permExPlugin.isEnabled()) {
         if (!(this.handler instanceof PermissionsExHandler)) {
            LOGGER.log(Level.INFO, "Essentials: Using PermissionsEx based permissions.");
            this.handler = new PermissionsExHandler();
         }

      } else {
         Plugin GMplugin = pluginManager.getPlugin("GroupManager");
         if (GMplugin != null && GMplugin.isEnabled()) {
            if (!(this.handler instanceof GroupManagerHandler)) {
               LOGGER.log(Level.INFO, "Essentials: Using GroupManager based permissions.");
               this.handler = new GroupManagerHandler(GMplugin);
            }

         } else {
            Plugin permBukkitPlugin = pluginManager.getPlugin("PermissionsBukkit");
            if (permBukkitPlugin != null && permBukkitPlugin.isEnabled()) {
               if (!(this.handler instanceof PermissionsBukkitHandler)) {
                  LOGGER.log(Level.INFO, "Essentials: Using PermissionsBukkit based permissions.");
                  this.handler = new PermissionsBukkitHandler(permBukkitPlugin);
               }

            } else {
               Plugin simplyPermsPlugin = pluginManager.getPlugin("SimplyPerms");
               if (simplyPermsPlugin != null && simplyPermsPlugin.isEnabled()) {
                  if (!(this.handler instanceof SimplyPermsHandler)) {
                     LOGGER.log(Level.INFO, "Essentials: Using SimplyPerms based permissions.");
                     this.handler = new SimplyPermsHandler(simplyPermsPlugin);
                  }

               } else {
                  Plugin privPlugin = pluginManager.getPlugin("Privileges");
                  if (privPlugin != null && privPlugin.isEnabled()) {
                     if (!(this.handler instanceof PrivilegesHandler)) {
                        LOGGER.log(Level.INFO, "Essentials: Using Privileges based permissions.");
                        this.handler = new PrivilegesHandler(privPlugin);
                     }

                  } else {
                     Plugin bPermPlugin = pluginManager.getPlugin("bPermissions");
                     if (bPermPlugin != null && bPermPlugin.isEnabled()) {
                        String bVer = bPermPlugin.getDescription().getVersion().replace(".", "");
                        if (Util.isInt(bVer) && Integer.parseInt(bVer) < 284) {
                           if (!(this.handler instanceof BPermissionsHandler)) {
                              LOGGER.log(Level.INFO, "Essentials: Using bPermissions based permissions.");
                              this.handler = new BPermissionsHandler();
                           }

                        } else {
                           if (!(this.handler instanceof BPermissions2Handler)) {
                              LOGGER.log(Level.INFO, "Essentials: Using bPermissions2 based permissions.");
                              this.handler = new BPermissions2Handler();
                           }

                        }
                     } else {
                        Plugin permPlugin = pluginManager.getPlugin("Permissions");
                        if (permPlugin != null && permPlugin.isEnabled()) {
                           if (permPlugin.getDescription().getVersion().charAt(0) == '3') {
                              if (!(this.handler instanceof Permissions3Handler)) {
                                 LOGGER.log(Level.INFO, "Essentials: Using Permissions 3 based permissions.");
                                 this.handler = new Permissions3Handler(permPlugin);
                              }
                           } else if (!(this.handler instanceof Permissions2Handler)) {
                              LOGGER.log(Level.INFO, "Essentials: Using Permissions 2 based permissions.");
                              this.handler = new Permissions2Handler(permPlugin);
                           }

                        } else {
                           if (this.useSuperperms) {
                              if (!(this.handler instanceof SuperpermsHandler)) {
                                 LOGGER.log(Level.INFO, "Essentials: Using superperms based permissions.");
                                 this.handler = new SuperpermsHandler();
                              }
                           } else if (!(this.handler instanceof ConfigPermissionsHandler)) {
                              LOGGER.log(Level.INFO, "Essentials: Using config file enhanced permissions.");
                              LOGGER.log(Level.INFO, "Permissions listed in as player-commands will be given to all users.");
                              this.handler = new ConfigPermissionsHandler(this.plugin);
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void setUseSuperperms(boolean useSuperperms) {
      this.useSuperperms = useSuperperms;
   }

   public String getName() {
      return this.handler.getClass().getSimpleName().replace("Handler", "");
   }
}
