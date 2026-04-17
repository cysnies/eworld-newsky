package org.anjocaido.groupmanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.anjocaido.groupmanager.Tasks.BukkitPermsUpdateTask;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.data.Variables;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.events.GMSystemEvent;
import org.anjocaido.groupmanager.events.GMWorldListener;
import org.anjocaido.groupmanager.events.GroupManagerEventHandler;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.anjocaido.groupmanager.permissions.BukkitPermissions;
import org.anjocaido.groupmanager.utils.GMLoggerHandler;
import org.anjocaido.groupmanager.utils.GroupManagerPermissions;
import org.anjocaido.groupmanager.utils.PermissionCheckResult;
import org.anjocaido.groupmanager.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class GroupManager extends JavaPlugin {
   private File backupFolder;
   private Runnable commiter;
   private ScheduledThreadPoolExecutor scheduler;
   private Map overloadedUsers = new HashMap();
   private Map selectedWorlds = new HashMap();
   private WorldsHolder worldsHolder;
   private boolean validateOnlinePlayer = true;
   private static boolean isLoaded = false;
   protected GMConfiguration config;
   protected static GlobalGroups globalGroups;
   private GMLoggerHandler ch;
   private static GroupManagerEventHandler GMEventHandler;
   public static BukkitPermissions BukkitPermissions;
   private static GMWorldListener WorldEvents;
   public static final Logger logger = Logger.getLogger(GroupManager.class.getName());
   private OverloadedWorldHolder dataHolder = null;
   private AnjoPermissionsHandler permissionHandler = null;
   private String lastError = "";

   public void onDisable() {
      this.onDisable(false);
   }

   public void onEnable() {
      setGMEventHandler(new GroupManagerEventHandler(this));
      this.onEnable(false);
   }

   public void onDisable(boolean restarting) {
      setLoaded(false);
      if (!restarting) {
         this.getServer().getServicesManager().unregister(this.worldsHolder);
      }

      this.disableScheduler();
      if (this.worldsHolder != null) {
         try {
            this.worldsHolder.saveChanges(false);
         } catch (IllegalStateException ex) {
            logger.log(Level.WARNING, ex.getMessage());
         }
      }

      if (BukkitPermissions != null) {
         BukkitPermissions.removeAllAttachments();
      }

      if (!restarting) {
         if (WorldEvents != null) {
            WorldEvents = null;
         }

         BukkitPermissions = null;
      }

      PluginDescriptionFile pdfFile = this.getDescription();
      System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
      if (!restarting) {
         logger.removeHandler(this.ch);
      }

   }

   public void onEnable(boolean restarting) {
      try {
         this.overloadedUsers = new HashMap();
         this.selectedWorlds = new HashMap();
         this.lastError = "";
         if (!restarting) {
            logger.setUseParentHandlers(false);
            this.ch = new GMLoggerHandler();
            logger.addHandler(this.ch);
         }

         logger.setLevel(Level.ALL);
         this.prepareFileFields();
         this.prepareConfig();
         globalGroups = new GlobalGroups(this);
         if (!restarting) {
            this.worldsHolder = new WorldsHolder(this);
         } else {
            this.worldsHolder.resetWorldsHolder();
         }

         PluginDescriptionFile pdfFile = this.getDescription();
         if (this.worldsHolder == null) {
            logger.severe("Can't enable " + pdfFile.getName() + " version " + pdfFile.getVersion() + ", bad loading!");
            this.getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException("An error ocurred while loading GroupManager");
         } else {
            setLoaded(false);
            if (!restarting) {
               WorldEvents = new GMWorldListener(this);
               BukkitPermissions = new BukkitPermissions(this);
            } else {
               BukkitPermissions.reset();
            }

            this.enableScheduler();
            if (this.getServer().getScheduler().scheduleSyncDelayedTask(this, new BukkitPermsUpdateTask(), 1L) == -1) {
               logger.severe("Could not schedule superperms Update.");
               setLoaded(true);
            }

            System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
            if (!restarting) {
               this.getServer().getServicesManager().register(WorldsHolder.class, this.worldsHolder, this, ServicePriority.Lowest);
            }

         }
      } catch (Exception ex) {
         this.saveErrorLog(ex);
         throw new IllegalArgumentException(ex.getMessage(), ex);
      }
   }

   private void saveErrorLog(Exception ex) {
      if (!this.getDataFolder().exists()) {
         this.getDataFolder().mkdirs();
      }

      this.lastError = ex.getMessage();
      logger.severe("===================================================");
      logger.severe("= ERROR REPORT START - " + this.getDescription().getVersion() + " =");
      logger.severe("===================================================");
      logger.severe("=== PLEASE COPY AND PASTE THE ERROR.LOG FROM THE ==");
      logger.severe("= GROUPMANAGER FOLDER TO AN ESSENTIALS  DEVELOPER =");
      logger.severe("===================================================");
      logger.severe(this.lastError);
      logger.severe("===================================================");
      logger.severe("= ERROR REPORT ENDED =");
      logger.severe("===================================================");

      try {
         String error = "=============================== GM ERROR LOG ===============================\n";
         error = error + "= ERROR REPORT START - " + this.getDescription().getVersion() + " =\n\n";
         error = error + Tasks.getStackTraceAsString(ex);
         error = error + "\n============================================================================\n";
         Tasks.appendStringToFile(error, this.getDataFolder() + System.getProperty("file.separator") + "ERROR.LOG");
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public boolean isValidateOnlinePlayer() {
      return this.validateOnlinePlayer;
   }

   public void setValidateOnlinePlayer(boolean validateOnlinePlayer) {
      this.validateOnlinePlayer = validateOnlinePlayer;
   }

   public static boolean isLoaded() {
      return isLoaded;
   }

   public static void setLoaded(boolean isLoaded) {
      GroupManager.isLoaded = isLoaded;
   }

   public InputStream getResourceAsStream(String fileName) {
      return this.getClassLoader().getResourceAsStream(fileName);
   }

   private void prepareFileFields() {
      this.backupFolder = new File(this.getDataFolder(), "backup");
      if (!this.backupFolder.exists()) {
         this.getBackupFolder().mkdirs();
      }

   }

   private void prepareConfig() {
      this.config = new GMConfiguration(this);
   }

   public void enableScheduler() {
      if (this.worldsHolder != null) {
         this.disableScheduler();
         this.commiter = new Runnable() {
            public void run() {
               try {
                  if (GroupManager.this.worldsHolder.saveChanges(false)) {
                     GroupManager.logger.log(Level.INFO, " Data files refreshed.");
                  }
               } catch (IllegalStateException ex) {
                  GroupManager.logger.log(Level.WARNING, ex.getMessage());
               }

            }
         };
         this.scheduler = new ScheduledThreadPoolExecutor(1);
         long minutes = (long)this.getGMConfig().getSaveInterval();
         if (minutes > 0L) {
            this.scheduler.scheduleAtFixedRate(this.commiter, minutes, minutes, TimeUnit.MINUTES);
            logger.info("Scheduled Data Saving is set for every " + minutes + " minutes!");
         } else {
            logger.info("Scheduled Data Saving is Disabled!");
         }

         logger.info("Backups will be retained for " + this.getGMConfig().getBackupDuration() + " hours!");
      }

   }

   public void disableScheduler() {
      if (this.scheduler != null) {
         try {
            this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            this.scheduler.shutdown();
         } catch (Exception var2) {
         }

         this.scheduler = null;
         logger.info("Scheduled Data Saving is disabled!");
      }

   }

   public WorldsHolder getWorldsHolder() {
      return this.worldsHolder;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      boolean playerCanDo = false;
      boolean isConsole = false;
      Player senderPlayer = null;
      Player targetPlayer = null;
      Group senderGroup = null;
      User senderUser = null;
      boolean isOpOverride = this.config.isOpOverride();
      boolean isAllowCommandBlocks = this.config.isAllowCommandBlocks();
      if (sender instanceof BlockCommandSender && !isAllowCommandBlocks) {
         Block block = ((BlockCommandSender)sender).getBlock();
         logger.warning(ChatColor.RED + "GM Commands can not be called from CommandBlocks");
         logger.warning(ChatColor.RED + "Location: " + ChatColor.GREEN + block.getWorld().getName() + ", " + block.getX() + ", " + block.getY() + ", " + block.getZ());
         return true;
      } else {
         if (sender instanceof Player) {
            senderPlayer = (Player)sender;
            if (!this.lastError.isEmpty() && !commandLabel.equalsIgnoreCase("manload")) {
               sender.sendMessage(ChatColor.RED + "All commands are locked due to an error. " + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Check the log" + ChatColor.RESET + "" + ChatColor.RED + " and then try a '/manload'.");
               return true;
            }

            senderUser = this.worldsHolder.getWorldData(senderPlayer).getUser(senderPlayer.getName());
            senderGroup = senderUser.getGroup();
            isOpOverride = isOpOverride && (senderPlayer.isOp() || this.worldsHolder.getWorldPermissions(senderPlayer).has(senderPlayer, "groupmanager.op"));
            if (isOpOverride || this.worldsHolder.getWorldPermissions(senderPlayer).has(senderPlayer, "groupmanager." + cmd.getName())) {
               playerCanDo = true;
            }
         } else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender || sender instanceof BlockCommandSender) {
            if (!this.lastError.isEmpty() && !commandLabel.equalsIgnoreCase("manload")) {
               sender.sendMessage(ChatColor.RED + "All commands are locked due to an error. " + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Check the log" + ChatColor.RESET + "" + ChatColor.RED + " and then try a '/manload'.");
               return true;
            }

            isConsole = true;
         }

         this.dataHolder = null;
         this.permissionHandler = null;
         if (senderPlayer != null) {
            this.dataHolder = this.worldsHolder.getWorldData(senderPlayer);
         }

         String selectedWorld = (String)this.selectedWorlds.get(sender.getName());
         if (selectedWorld != null) {
            this.dataHolder = this.worldsHolder.getWorldData(selectedWorld);
         }

         if (this.dataHolder != null) {
            this.permissionHandler = this.dataHolder.getPermissionsHandler();
         }

         PermissionCheckResult permissionResult = null;
         ArrayList<User> removeList = null;
         String auxString = null;
         List<String> match = null;
         User auxUser = null;
         Group auxGroup = null;
         Group auxGroup2 = null;
         GroupManagerPermissions execCmd = null;

         try {
            execCmd = GroupManagerPermissions.valueOf(cmd.getName());
         } catch (Exception var29) {
            logger.severe("===================================================");
            logger.severe("= ERROR REPORT START =");
            logger.severe("===================================================");
            logger.severe("= COPY AND PASTE THIS TO A GROUPMANAGER DEVELOPER =");
            logger.severe("===================================================");
            logger.severe(this.getDescription().getName());
            logger.severe(this.getDescription().getVersion());
            logger.severe("An error occured while trying to execute command:");
            logger.severe(cmd.getName());
            logger.severe("With " + args.length + " arguments:");

            for(String ar : args) {
               logger.severe(ar);
            }

            logger.severe("The field '" + cmd.getName() + "' was not found in enum.");
            logger.severe("And could not be parsed.");
            logger.severe("FIELDS FOUND IN ENUM:");

            for(GroupManagerPermissions val : GroupManagerPermissions.values()) {
               logger.severe(val.name());
            }

            logger.severe("===================================================");
            logger.severe("= ERROR REPORT ENDED =");
            logger.severe("===================================================");
            sender.sendMessage("An error occurred. Ask the admin to take a look at the console.");
         }

         if (isConsole || playerCanDo) {
            switch (execCmd) {
               case manuadd:
                  if (args.length != 2 && args.length != 3) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manuadd <player> <group> | optional [world])");
                     return true;
                  }

                  if (args.length == 3) {
                     this.dataHolder = this.worldsHolder.getWorldData(args[2]);
                     this.permissionHandler = this.dataHolder.getPermissionsHandler();
                  }

                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = this.dataHolder.getGroup(args[1]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[1] + " Group doesnt exist!");
                     return false;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "Players may not be members of GlobalGroups directly.");
                     return false;
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "Can't modify a player with the same permissions as you, or higher.");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && this.permissionHandler.hasGroupInInheritance(auxGroup, senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "The destination group can't be the same as yours, or higher.");
                     return true;
                  }

                  if (isConsole || isOpOverride || this.permissionHandler.inGroup(senderUser.getName(), auxUser.getGroupName()) && this.permissionHandler.inGroup(senderUser.getName(), auxGroup.getName())) {
                     auxUser.setGroup(auxGroup);
                     if (!sender.hasPermission("groupmanager.notify.other") || isConsole) {
                        sender.sendMessage(ChatColor.YELLOW + "You changed player '" + auxUser.getName() + "' group to '" + auxGroup.getName() + "' in world '" + this.dataHolder.getName() + "'.");
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't modify a player involving a group that you don't inherit.");
                  return true;
               case manudel:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manudel <player>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  this.dataHolder.removeUser(auxUser.getName());
                  sender.sendMessage(ChatColor.YELLOW + "You changed player '" + auxUser.getName() + "' to default settings.");
                  targetPlayer = this.getServer().getPlayer(auxUser.getName());
                  if (targetPlayer != null) {
                     BukkitPermissions.updatePermissions(targetPlayer);
                  }

                  return true;
               case manuaddsub:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     sender.sendMessage(ChatColor.RED + "Couldn't retrieve your world. World selection is needed.");
                     sender.sendMessage(ChatColor.RED + "Use /manselect <world>");
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manuaddsub <player> <group>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = this.dataHolder.getGroup(args[1]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[1] + " Group doesnt exist!");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  if (auxUser.addSubGroup(auxGroup)) {
                     sender.sendMessage(ChatColor.YELLOW + "You added subgroup '" + auxGroup.getName() + "' to player '" + auxUser.getName() + "'.");
                  } else {
                     sender.sendMessage(ChatColor.RED + "The subgroup '" + auxGroup.getName() + "' is already available to '" + auxUser.getName() + "'.");
                  }

                  return true;
               case manudelsub:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manudelsub <user> <group>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = this.dataHolder.getGroup(args[1]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[1] + " Group doesnt exist!");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  auxUser.removeSubGroup(auxGroup);
                  sender.sendMessage(ChatColor.YELLOW + "You removed subgroup '" + auxGroup.getName() + "' from player '" + auxUser.getName() + "' list.");
                  return true;
               case mangadd:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangadd <group>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup != null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group already exist!");
                     return true;
                  }

                  auxGroup = this.dataHolder.createGroup(args[0]);
                  sender.sendMessage(ChatColor.YELLOW + "You created a group named: " + auxGroup.getName());
                  return true;
               case mangdel:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangdel <group>)");
                     return false;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  this.dataHolder.removeGroup(auxGroup.getName());
                  sender.sendMessage(ChatColor.YELLOW + "You deleted a group named " + auxGroup.getName() + ", it's users are default group now.");
                  BukkitPermissions.updateAllPlayers();
                  return true;
               case manuaddp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manuaddp <player> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "Can't modify player with same group than you, or higher.");
                     return true;
                  }

                  permissionResult = this.permissionHandler.checkFullUserPermission(senderUser, args[1]);
                  if (isConsole || isOpOverride || !permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND) && !permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                     permissionResult = this.permissionHandler.checkUserOnlyPermission(auxUser, args[1]);
                     if (auxString.startsWith("+")) {
                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                           sender.sendMessage(ChatColor.RED + "The user already has direct access to that permission.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }
                     } else if (auxString.startsWith("-")) {
                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                           sender.sendMessage(ChatColor.RED + "The user already has an exception for this node.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }

                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                           sender.sendMessage(ChatColor.RED + "The user already has a matching node.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                        sender.sendMessage(ChatColor.RED + "The user already has an exception for this node.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.FOUND)) {
                        sender.sendMessage(ChatColor.RED + "The user already has direct access to that permission.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                        if (permissionResult.accessLevel.equalsIgnoreCase(args[1])) {
                           return true;
                        }
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                        sender.sendMessage(ChatColor.RED + "The user already has a matching Negated node.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                     }

                     auxUser.addPermission(auxString);
                     sender.sendMessage(ChatColor.YELLOW + "You added '" + auxString + "' to player '" + auxUser.getName() + "' permissions.");
                     targetPlayer = this.getServer().getPlayer(auxUser.getName());
                     if (targetPlayer != null) {
                        BukkitPermissions.updatePermissions(targetPlayer);
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't add a permission you don't have.");
                  return true;
               case manudelp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manudelp <player> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same group as you, or higher.");
                     return true;
                  }

                  permissionResult = this.permissionHandler.checkFullUserPermission(senderUser, auxString);
                  if (isConsole || isOpOverride || !permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND) && !permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                     permissionResult = this.permissionHandler.checkUserOnlyPermission(auxUser, auxString);
                     if (permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND)) {
                        sender.sendMessage(ChatColor.RED + "The user doesn't have direct access to that permission.");
                        return true;
                     }

                     if (!auxUser.hasSamePermissionNode(auxString)) {
                        sender.sendMessage(ChatColor.RED + "This permission node doesn't match any node.");
                        sender.sendMessage(ChatColor.RED + "But might match node: " + permissionResult.accessLevel);
                        return true;
                     }

                     auxUser.removePermission(auxString);
                     sender.sendMessage(ChatColor.YELLOW + "You removed '" + auxString + "' from player '" + auxUser.getName() + "' permissions.");
                     targetPlayer = this.getServer().getPlayer(auxUser.getName());
                     if (targetPlayer != null) {
                        BukkitPermissions.updatePermissions(targetPlayer);
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't remove a permission you don't have.");
                  return true;
               case manulistp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 0 && args.length <= 2) {
                     if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                        return false;
                     }

                     if (match != null) {
                        auxUser = this.dataHolder.getUser((String)match.get(0));
                     } else {
                        auxUser = this.dataHolder.getUser(args[0]);
                     }

                     auxString = "";

                     for(String perm : auxUser.getPermissionList()) {
                        auxString = auxString + perm + ", ";
                     }

                     if (auxString.lastIndexOf(",") > 0) {
                        auxString = auxString.substring(0, auxString.lastIndexOf(","));
                        sender.sendMessage(ChatColor.YELLOW + "The player '" + auxUser.getName() + "' has following permissions: " + ChatColor.WHITE + auxString);
                        sender.sendMessage(ChatColor.YELLOW + "And all permissions from group: " + auxUser.getGroupName());
                        auxString = "";

                        for(String subGroup : auxUser.subGroupListStringCopy()) {
                           auxString = auxString + subGroup + ", ";
                        }

                        if (auxString.lastIndexOf(",") > 0) {
                           auxString = auxString.substring(0, auxString.lastIndexOf(","));
                           sender.sendMessage(ChatColor.YELLOW + "And all permissions from subgroups: " + auxString);
                        }
                     } else {
                        sender.sendMessage(ChatColor.YELLOW + "The player '" + auxUser.getName() + "' has no specific permissions.");
                        sender.sendMessage(ChatColor.YELLOW + "Only all permissions from group: " + auxUser.getGroupName());
                        auxString = "";

                        for(String subGroup : auxUser.subGroupListStringCopy()) {
                           auxString = auxString + subGroup + ", ";
                        }

                        if (auxString.lastIndexOf(",") > 0) {
                           auxString = auxString.substring(0, auxString.lastIndexOf(","));
                           sender.sendMessage(ChatColor.YELLOW + "And all permissions from subgroups: " + auxString);
                        }
                     }

                     if (args.length == 2 && args[1].equalsIgnoreCase("+")) {
                        targetPlayer = this.getServer().getPlayer(auxUser.getName());
                        if (targetPlayer != null) {
                           sender.sendMessage(ChatColor.YELLOW + "Superperms reports: ");

                           for(String line : BukkitPermissions.listPerms(targetPlayer)) {
                              sender.sendMessage(ChatColor.YELLOW + line);
                           }
                        }
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manulistp <player> (+))");
                  return true;
               case manucheckp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manucheckp <player> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  targetPlayer = this.getServer().getPlayer(auxUser.getName());
                  permissionResult = this.permissionHandler.checkFullGMPermission(auxUser, auxString, false);
                  if (permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND)) {
                     sender.sendMessage(ChatColor.YELLOW + "The player doesn't have access to that permission");
                  } else if (permissionResult.owner instanceof User) {
                     if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                        sender.sendMessage(ChatColor.YELLOW + "The user has directly a negation node for that permission.");
                     } else {
                        sender.sendMessage(ChatColor.YELLOW + "The user has directly this permission.");
                     }

                     sender.sendMessage(ChatColor.YELLOW + "Permission Node: " + permissionResult.accessLevel);
                  } else if (permissionResult.owner instanceof Group) {
                     if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                        sender.sendMessage(ChatColor.YELLOW + "The user inherits a negation permission from group: " + permissionResult.owner.getName());
                     } else {
                        sender.sendMessage(ChatColor.YELLOW + "The user inherits the permission from group: " + permissionResult.owner.getName());
                     }

                     sender.sendMessage(ChatColor.YELLOW + "Permission Node: " + permissionResult.accessLevel);
                  }

                  if (targetPlayer != null) {
                     sender.sendMessage(ChatColor.YELLOW + "SuperPerms reports Node: " + targetPlayer.hasPermission(args[1]) + (!targetPlayer.hasPermission(args[1]) && targetPlayer.isPermissionSet(args[1]) ? " (Negated)" : ""));
                  }

                  return true;
               case mangaddp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangaaddp <group> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return false;
                  }

                  permissionResult = this.permissionHandler.checkFullUserPermission(senderUser, args[1]);
                  if (isConsole || isOpOverride || !permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND) && !permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                     permissionResult = this.permissionHandler.checkGroupOnlyPermission(auxGroup, args[1]);
                     if (auxString.startsWith("+")) {
                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                           sender.sendMessage(ChatColor.RED + "The group already has direct access to that permission.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }
                     } else if (auxString.startsWith("-")) {
                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                           sender.sendMessage(ChatColor.RED + "The group already has an exception for this node.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }

                        if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                           sender.sendMessage(ChatColor.RED + "The group already has a matching node.");
                           sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                           return true;
                        }
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.EXCEPTION)) {
                        sender.sendMessage(ChatColor.RED + "The group already has an exception for this node.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.FOUND)) {
                        sender.sendMessage(ChatColor.RED + "The group already has direct access to that permission.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                        if (permissionResult.accessLevel.equalsIgnoreCase(args[1])) {
                           return true;
                        }
                     } else if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                        sender.sendMessage(ChatColor.RED + "The group already has a matching Negated node.");
                        sender.sendMessage(ChatColor.RED + "Node: " + permissionResult.accessLevel);
                     }

                     auxGroup.addPermission(auxString);
                     sender.sendMessage(ChatColor.YELLOW + "You added '" + auxString + "' to group '" + auxGroup.getName() + "' permissions.");
                     BukkitPermissions.updateAllPlayers();
                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't add a permission you don't have.");
                  return true;
               case mangdelp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangdelp <group> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  permissionResult = this.permissionHandler.checkFullUserPermission(senderUser, auxString);
                  if (isConsole || !permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND) && !permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                     permissionResult = this.permissionHandler.checkGroupOnlyPermission(auxGroup, auxString);
                     if (permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND)) {
                        sender.sendMessage(ChatColor.RED + "The group doesn't have direct access to that permission.");
                        return false;
                     }

                     if (!auxGroup.hasSamePermissionNode(auxString)) {
                        sender.sendMessage(ChatColor.RED + "This permission node doesn't match any node.");
                        sender.sendMessage(ChatColor.RED + "But might match node: " + permissionResult.accessLevel);
                        return true;
                     }

                     auxGroup.removePermission(auxString);
                     sender.sendMessage(ChatColor.YELLOW + "You removed '" + auxString + "' from group '" + auxGroup.getName() + "' permissions.");
                     BukkitPermissions.updateAllPlayers();
                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "Can't remove a permission you don't have.");
                  return true;
               case manglistp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manglistp <group>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  auxString = "";

                  for(String perm : auxGroup.getPermissionList()) {
                     auxString = auxString + perm + ", ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                     sender.sendMessage(ChatColor.YELLOW + "The group '" + auxGroup.getName() + "' has following permissions: " + ChatColor.WHITE + auxString);
                     auxString = "";

                     for(String grp : auxGroup.getInherits()) {
                        auxString = auxString + grp + ", ";
                     }

                     if (auxString.lastIndexOf(",") > 0) {
                        auxString = auxString.substring(0, auxString.lastIndexOf(","));
                        sender.sendMessage(ChatColor.YELLOW + "And all permissions from groups: " + auxString);
                     }
                  } else {
                     sender.sendMessage(ChatColor.YELLOW + "The group '" + auxGroup.getName() + "' has no specific permissions.");
                     auxString = "";

                     for(String grp : auxGroup.getInherits()) {
                        auxString = auxString + grp + ", ";
                     }

                     if (auxString.lastIndexOf(",") > 0) {
                        auxString = auxString.substring(0, auxString.lastIndexOf(","));
                        sender.sendMessage(ChatColor.YELLOW + "Only all permissions from groups: " + auxString);
                     }
                  }

                  return true;
               case mangcheckp:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangcheckp <group> <permission>)");
                     return true;
                  }

                  auxString = args[1];
                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  permissionResult = this.permissionHandler.checkGroupPermissionWithInheritance(auxGroup, auxString);
                  if (permissionResult.resultType.equals(PermissionCheckResult.Type.NOTFOUND)) {
                     sender.sendMessage(ChatColor.YELLOW + "The group doesn't have access to that permission");
                     return true;
                  }

                  if (permissionResult.owner instanceof Group) {
                     if (permissionResult.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
                        sender.sendMessage(ChatColor.YELLOW + "The group inherits the negation permission from group: " + permissionResult.owner.getName());
                     } else {
                        sender.sendMessage(ChatColor.YELLOW + "The user inherits the permission from group: " + permissionResult.owner.getName());
                     }

                     sender.sendMessage(ChatColor.YELLOW + "Permission Node: " + permissionResult.accessLevel);
                  }

                  return true;
               case mangaddi:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangaddi <group1> <group2>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  auxGroup2 = this.dataHolder.getGroup(args[1]);
                  if (auxGroup2 == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[1] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support inheritance.");
                     return true;
                  }

                  if (this.permissionHandler.hasGroupInInheritance(auxGroup, auxGroup2.getName())) {
                     sender.sendMessage(ChatColor.RED + "Group " + auxGroup.getName() + " already inherits " + auxGroup2.getName() + " (might not be directly)");
                     return true;
                  }

                  auxGroup.addInherits(auxGroup2);
                  sender.sendMessage(ChatColor.RED + "Group " + auxGroup2.getName() + " is now in " + auxGroup.getName() + " inheritance list.");
                  BukkitPermissions.updateAllPlayers();
                  return true;
               case mangdeli:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangdeli <group1> <group2>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  auxGroup2 = this.dataHolder.getGroup(args[1]);
                  if (auxGroup2 == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[1] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support inheritance.");
                     return true;
                  }

                  if (!this.permissionHandler.hasGroupInInheritance(auxGroup, auxGroup2.getName())) {
                     sender.sendMessage(ChatColor.RED + "Group " + auxGroup.getName() + " does not inherits " + auxGroup2.getName() + ".");
                     return true;
                  }

                  if (!auxGroup.getInherits().contains(auxGroup2.getName())) {
                     sender.sendMessage(ChatColor.RED + "Group " + auxGroup.getName() + " does not inherits " + auxGroup2.getName() + " directly.");
                     return true;
                  }

                  auxGroup.removeInherits(auxGroup2.getName());
                  sender.sendMessage(ChatColor.RED + "Group " + auxGroup2.getName() + " was removed from " + auxGroup.getName() + " inheritance list.");
                  BukkitPermissions.updateAllPlayers();
                  return true;
               case manuaddv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length < 3) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manuaddv <user> <variable> <value>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxString = "";

                  for(int i = 2; i < args.length; ++i) {
                     auxString = auxString + args[i];
                     if (i + 1 < args.length) {
                        auxString = auxString + " ";
                     }
                  }

                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  auxUser.getVariables().addVar(args[1], Variables.parseVariableValue(auxString));
                  sender.sendMessage(ChatColor.YELLOW + "Variable " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + ":'" + ChatColor.GREEN + auxString + ChatColor.YELLOW + "' added to the user " + auxUser.getName());
                  return true;
               case manudelv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manudelv <user> <variable>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!auxUser.getVariables().hasVar(args[1])) {
                     sender.sendMessage(ChatColor.RED + "The user doesn't have directly that variable!");
                     return true;
                  }

                  auxUser.getVariables().removeVar(args[1]);
                  sender.sendMessage(ChatColor.YELLOW + "Variable " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " removed from the user " + ChatColor.GREEN + auxUser.getName());
                  return true;
               case manulistv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manulistv <user>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxString = "";

                  for(String varKey : auxUser.getVariables().getVarKeyList()) {
                     Object o = auxUser.getVariables().getVarObject(varKey);
                     auxString = auxString + ChatColor.GOLD + varKey + ChatColor.WHITE + ":'" + ChatColor.GREEN + o.toString() + ChatColor.WHITE + "', ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                  }

                  sender.sendMessage(ChatColor.YELLOW + "Variables of user " + auxUser.getName() + ": ");
                  sender.sendMessage(auxString + ".");
                  sender.sendMessage(ChatColor.YELLOW + "Plus all variables from group: " + auxUser.getGroupName());
                  return true;
               case manucheckv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manucheckv <user> <variable>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = auxUser.getGroup();
                  auxGroup2 = this.permissionHandler.nextGroupWithVariable(auxGroup, args[1]);
                  if (!auxUser.getVariables().hasVar(args[1])) {
                     if (!auxUser.isSubGroupsEmpty() && auxGroup2 == null) {
                        for(Group subGroup : auxUser.subGroupListCopy()) {
                           auxGroup2 = this.permissionHandler.nextGroupWithVariable(subGroup, args[1]);
                           if (auxGroup2 != null) {
                           }
                        }
                     }

                     if (auxGroup2 == null) {
                        sender.sendMessage(ChatColor.YELLOW + "The user doesn't have access to that variable!");
                        return true;
                     }
                  }

                  if (auxUser.getVariables().hasVar(auxString)) {
                     sender.sendMessage(ChatColor.YELLOW + "The value of variable '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "' is: '" + ChatColor.GREEN + auxUser.getVariables().getVarObject(args[1]).toString() + ChatColor.WHITE + "'");
                     sender.sendMessage(ChatColor.YELLOW + "This user own directly the variable");
                  }

                  sender.sendMessage(ChatColor.YELLOW + "The value of variable '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "' is: '" + ChatColor.GREEN + auxGroup2.getVariables().getVarObject(args[1]).toString() + ChatColor.WHITE + "'");
                  if (!auxGroup.equals(auxGroup2)) {
                     sender.sendMessage(ChatColor.YELLOW + "And the value was inherited from group: " + ChatColor.GREEN + auxGroup2.getName());
                  }

                  return true;
               case mangaddv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length < 3) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangaddv <group> <variable> <value>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support Info Nodes.");
                     return true;
                  }

                  auxString = "";

                  for(int i = 2; i < args.length; ++i) {
                     auxString = auxString + args[i];
                     if (i + 1 < args.length) {
                        auxString = auxString + " ";
                     }
                  }

                  if (auxString.startsWith("'") && auxString.endsWith("'")) {
                     auxString = auxString.substring(1, auxString.length() - 1);
                  }

                  auxGroup.getVariables().addVar(args[1], Variables.parseVariableValue(auxString));
                  sender.sendMessage(ChatColor.YELLOW + "Variable " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + ":'" + ChatColor.GREEN + auxString + ChatColor.YELLOW + "' added to the group " + auxGroup.getName());
                  return true;
               case mangdelv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangdelv <group> <variable>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support Info Nodes.");
                     return true;
                  }

                  if (!auxGroup.getVariables().hasVar(args[1])) {
                     sender.sendMessage(ChatColor.RED + "The group doesn't have directly that variable!");
                     return true;
                  }

                  auxGroup.getVariables().removeVar(args[1]);
                  sender.sendMessage(ChatColor.YELLOW + "Variable " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + " removed from the group " + ChatColor.GREEN + auxGroup.getName());
                  return true;
               case manglistv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manglistv <group>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support Info Nodes.");
                     return true;
                  }

                  auxString = "";

                  for(String varKey : auxGroup.getVariables().getVarKeyList()) {
                     Object o = auxGroup.getVariables().getVarObject(varKey);
                     auxString = auxString + ChatColor.GOLD + varKey + ChatColor.WHITE + ":'" + ChatColor.GREEN + o.toString() + ChatColor.WHITE + "', ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                  }

                  sender.sendMessage(ChatColor.YELLOW + "Variables of group " + auxGroup.getName() + ": ");
                  sender.sendMessage(auxString + ".");
                  auxString = "";

                  for(String grp : auxGroup.getInherits()) {
                     auxString = auxString + grp + ", ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                     sender.sendMessage(ChatColor.YELLOW + "Plus all variables from groups: " + auxString);
                  }

                  return true;
               case mangcheckv:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mangcheckv <group> <variable>)");
                     return true;
                  }

                  auxGroup = this.dataHolder.getGroup(args[0]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "GlobalGroups do NOT support Info Nodes.");
                     return true;
                  }

                  auxGroup2 = this.permissionHandler.nextGroupWithVariable(auxGroup, args[1]);
                  if (auxGroup2 == null) {
                     sender.sendMessage(ChatColor.RED + "The group doesn't have access to that variable!");
                  }

                  sender.sendMessage(ChatColor.YELLOW + "The value of variable '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "' is: '" + ChatColor.GREEN + auxGroup2.getVariables().getVarObject(args[1]).toString() + ChatColor.WHITE + "'");
                  if (!auxGroup.equals(auxGroup2)) {
                     sender.sendMessage(ChatColor.YELLOW + "And the value was inherited from group: " + ChatColor.GREEN + auxGroup2.getName());
                  }

                  return true;
               case manwhois:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manwhois <player>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  sender.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.GREEN + auxUser.getName());
                  sender.sendMessage(ChatColor.YELLOW + "Group: " + ChatColor.GREEN + auxUser.getGroup().getName());
                  auxString = "";

                  for(String subGroup : auxUser.subGroupListStringCopy()) {
                     auxString = auxString + subGroup + ", ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                     sender.sendMessage(ChatColor.YELLOW + "subgroups: " + auxString);
                  }

                  sender.sendMessage(ChatColor.YELLOW + "Overloaded: " + ChatColor.GREEN + this.dataHolder.isOverloaded(auxUser.getName()));
                  auxGroup = this.dataHolder.surpassOverload(auxUser.getName()).getGroup();
                  if (!auxGroup.equals(auxUser.getGroup())) {
                     sender.sendMessage(ChatColor.YELLOW + "Original Group: " + ChatColor.GREEN + auxGroup.getName());
                  }

                  return true;
               case tempadd:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/tempadd <player>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "Can't modify player with same permissions than you, or higher.");
                     return true;
                  }

                  if (this.overloadedUsers.get(this.dataHolder.getName().toLowerCase()) == null) {
                     this.overloadedUsers.put(this.dataHolder.getName().toLowerCase(), new ArrayList());
                  }

                  this.dataHolder.overloadUser(auxUser.getName());
                  ((ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())).add(this.dataHolder.getUser(auxUser.getName()));
                  sender.sendMessage(ChatColor.YELLOW + "Player set to overload mode!");
                  return true;
               case tempdel:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/tempdel <player>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  if (this.overloadedUsers.get(this.dataHolder.getName().toLowerCase()) == null) {
                     this.overloadedUsers.put(this.dataHolder.getName().toLowerCase(), new ArrayList());
                  }

                  this.dataHolder.removeOverload(auxUser.getName());
                  if (((ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())).contains(auxUser)) {
                     ((ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())).remove(auxUser);
                  }

                  sender.sendMessage(ChatColor.YELLOW + "Player overload mode is now disabled.");
                  return true;
               case templist:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  auxString = "";
                  removeList = new ArrayList();
                  int count = 0;

                  for(User u : (ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())) {
                     if (!this.dataHolder.isOverloaded(u.getName())) {
                        removeList.add(u);
                     } else {
                        auxString = auxString + u.getName() + ", ";
                        ++count;
                     }
                  }

                  if (count == 0) {
                     sender.sendMessage(ChatColor.YELLOW + "There are no users in overload mode.");
                     return true;
                  }

                  auxString = auxString.substring(0, auxString.lastIndexOf(","));
                  if (this.overloadedUsers.get(this.dataHolder.getName().toLowerCase()) == null) {
                     this.overloadedUsers.put(this.dataHolder.getName().toLowerCase(), new ArrayList());
                  }

                  ((ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())).removeAll(removeList);
                  sender.sendMessage(ChatColor.YELLOW + " " + count + " Users in overload mode: " + ChatColor.WHITE + auxString);
                  return true;
               case tempdelall:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  new ArrayList();
                  int count = 0;

                  for(User u : (ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())) {
                     if (this.dataHolder.isOverloaded(u.getName())) {
                        this.dataHolder.removeOverload(u.getName());
                        ++count;
                     }
                  }

                  if (count == 0) {
                     sender.sendMessage(ChatColor.YELLOW + "There are no users in overload mode.");
                     return true;
                  }

                  if (this.overloadedUsers.get(this.dataHolder.getName().toLowerCase()) == null) {
                     this.overloadedUsers.put(this.dataHolder.getName().toLowerCase(), new ArrayList());
                  }

                  ((ArrayList)this.overloadedUsers.get(this.dataHolder.getName().toLowerCase())).clear();
                  sender.sendMessage(ChatColor.YELLOW + " " + count + "All users in overload mode are now normal again.");
                  return true;
               case mansave:
                  boolean forced = false;
                  if (args.length == 1 && args[0].equalsIgnoreCase("force")) {
                     forced = true;
                  }

                  try {
                     this.worldsHolder.saveChanges(forced);
                     sender.sendMessage(ChatColor.YELLOW + "All changes were saved.");
                  } catch (IllegalStateException ex) {
                     sender.sendMessage(ChatColor.RED + ex.getMessage());
                  }

                  return true;
               case manload:
                  if (args.length > 0) {
                     if (!this.lastError.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "All commands are locked due to an error. " + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Check the log" + ChatColor.RESET + "" + ChatColor.RED + " and then try a '/manload'.");
                        return true;
                     }

                     auxString = "";

                     for(int i = 0; i < args.length; ++i) {
                        auxString = auxString + args[i];
                        if (i + 1 < args.length) {
                           auxString = auxString + " ";
                        }
                     }

                     isLoaded = false;
                     globalGroups.load();
                     this.worldsHolder.loadWorld(auxString);
                     sender.sendMessage("The request to reload world '" + auxString + "' was attempted.");
                     isLoaded = true;
                     BukkitPermissions.reset();
                  } else {
                     this.onDisable(true);
                     this.onEnable(true);
                     sender.sendMessage("All settings and worlds were reloaded!");
                  }

                  if (isLoaded()) {
                     getGMEventHandler().callEvent(GMSystemEvent.Action.RELOADED);
                  }

                  return true;
               case listgroups:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  auxString = "";

                  for(Group g : this.dataHolder.getGroupList()) {
                     auxString = auxString + g.getName() + ", ";
                  }

                  for(Group g : getGlobalGroups().getGroupList()) {
                     auxString = auxString + g.getName() + ", ";
                  }

                  if (auxString.lastIndexOf(",") > 0) {
                     auxString = auxString.substring(0, auxString.lastIndexOf(","));
                  }

                  sender.sendMessage(ChatColor.YELLOW + " Groups Available: " + ChatColor.WHITE + auxString);
                  return true;
               case manpromote:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manpromote <player> <group>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = this.dataHolder.getGroup(args[1]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "Players may not be members of GlobalGroups directly.");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && this.permissionHandler.hasGroupInInheritance(auxGroup, senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "The destination group can't be the same as yours, or higher.");
                     return true;
                  }

                  if (isConsole || isOpOverride || this.permissionHandler.inGroup(senderUser.getName(), auxUser.getGroupName()) && this.permissionHandler.inGroup(senderUser.getName(), auxGroup.getName())) {
                     if (!isConsole && !isOpOverride && !this.permissionHandler.hasGroupInInheritance(auxUser.getGroup(), auxGroup.getName()) && !this.permissionHandler.hasGroupInInheritance(auxGroup, auxUser.getGroupName())) {
                        sender.sendMessage(ChatColor.RED + "You can't modify a player using groups with different heritage line.");
                        return true;
                     }

                     if (!isConsole && !isOpOverride && !this.permissionHandler.hasGroupInInheritance(auxGroup, auxUser.getGroupName())) {
                        sender.sendMessage(ChatColor.RED + "The new group must be a higher rank.");
                        return true;
                     }

                     auxUser.setGroup(auxGroup);
                     if (!sender.hasPermission("groupmanager.notify.other") || isConsole) {
                        sender.sendMessage(ChatColor.YELLOW + "You changed " + auxUser.getName() + " group to " + auxGroup.getName() + ".");
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't modify a player involving a group that you don't inherit.");
                  return true;
               case mandemote:
                  if ((this.dataHolder == null || this.permissionHandler == null) && !this.setDefaultWorldHandler(sender)) {
                     return true;
                  }

                  if (args.length != 2) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mandemote <player> <group>)");
                     return true;
                  }

                  if (this.validateOnlinePlayer && (match = this.validatePlayer(args[0], sender)) == null) {
                     return false;
                  }

                  if (match != null) {
                     auxUser = this.dataHolder.getUser((String)match.get(0));
                  } else {
                     auxUser = this.dataHolder.getUser(args[0]);
                  }

                  auxGroup = this.dataHolder.getGroup(args[1]);
                  if (auxGroup == null) {
                     sender.sendMessage(ChatColor.RED + "" + args[0] + " Group doesnt exist!");
                     return true;
                  }

                  if (auxGroup.isGlobal()) {
                     sender.sendMessage(ChatColor.RED + "Players may not be members of GlobalGroups directly.");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && senderGroup != null && this.permissionHandler.inGroup(auxUser.getName(), senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "You can't modify a player with same permissions as you, or higher.");
                     return true;
                  }

                  if (!isConsole && !isOpOverride && this.permissionHandler.hasGroupInInheritance(auxGroup, senderGroup.getName())) {
                     sender.sendMessage(ChatColor.RED + "The destination group can't be the same as yours, or higher.");
                     return true;
                  }

                  if (isConsole || isOpOverride || this.permissionHandler.inGroup(senderUser.getName(), auxUser.getGroupName()) && this.permissionHandler.inGroup(senderUser.getName(), auxGroup.getName())) {
                     if (!isConsole && !isOpOverride && !this.permissionHandler.hasGroupInInheritance(auxUser.getGroup(), auxGroup.getName()) && !this.permissionHandler.hasGroupInInheritance(auxGroup, auxUser.getGroupName())) {
                        sender.sendMessage(ChatColor.RED + "You can't modify a player using groups with different inheritage line.");
                        return true;
                     }

                     if (!isConsole && !isOpOverride && this.permissionHandler.hasGroupInInheritance(auxGroup, auxUser.getGroupName())) {
                        sender.sendMessage(ChatColor.RED + "The new group must be a lower rank.");
                        return true;
                     }

                     auxUser.setGroup(auxGroup);
                     if (!sender.hasPermission("groupmanager.notify.other") || isConsole) {
                        sender.sendMessage(ChatColor.YELLOW + "You changed " + auxUser.getName() + " group to " + auxGroup.getName() + ".");
                     }

                     return true;
                  }

                  sender.sendMessage(ChatColor.RED + "You can't modify a player involving a group that you don't inherit.");
                  return true;
               case mantogglevalidate:
                  this.validateOnlinePlayer = !this.validateOnlinePlayer;
                  sender.sendMessage(ChatColor.YELLOW + "Validate if player is online, now set to: " + Boolean.toString(this.validateOnlinePlayer));
                  if (!this.validateOnlinePlayer) {
                     sender.sendMessage(ChatColor.GOLD + "From now on you can edit players that are not connected... BUT:");
                     sender.sendMessage(ChatColor.LIGHT_PURPLE + "From now on you should type the whole name of the player, correctly.");
                  }

                  return true;
               case mantogglesave:
                  if (this.scheduler == null) {
                     this.enableScheduler();
                     sender.sendMessage(ChatColor.YELLOW + "The auto-saving is enabled!");
                  } else {
                     this.disableScheduler();
                     sender.sendMessage(ChatColor.YELLOW + "The auto-saving is disabled!");
                  }

                  return true;
               case manworld:
                  auxString = (String)this.selectedWorlds.get(sender.getName());
                  if (auxString != null) {
                     sender.sendMessage(ChatColor.YELLOW + "You have the world '" + this.dataHolder.getName() + "' in your selection.");
                  } else if (this.dataHolder == null) {
                     sender.sendMessage(ChatColor.YELLOW + "There is no world selected. And no world is available now.");
                  } else {
                     sender.sendMessage(ChatColor.YELLOW + "You don't have a world in your selection..");
                     sender.sendMessage(ChatColor.YELLOW + "Working with the direct world where your player is.");
                     sender.sendMessage(ChatColor.YELLOW + "Your world now uses permissions of world name: '" + this.dataHolder.getName() + "' ");
                  }

                  return true;
               case manselect:
                  if (args.length < 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/manselect <world>)");
                     sender.sendMessage(ChatColor.YELLOW + "Worlds available: ");
                     ArrayList<OverloadedWorldHolder> worlds = this.worldsHolder.allWorldsDataList();
                     auxString = "";

                     for(int i = 0; i < worlds.size(); ++i) {
                        auxString = auxString + ((OverloadedWorldHolder)worlds.get(i)).getName();
                        if (i + 1 < worlds.size()) {
                           auxString = auxString + ", ";
                        }
                     }

                     sender.sendMessage(ChatColor.YELLOW + auxString);
                     return false;
                  }

                  auxString = "";

                  for(int i = 0; i < args.length; ++i) {
                     if (args[i] == null) {
                        logger.warning("Bukkit gave invalid arguments array! Cmd: " + cmd.getName() + " args.length: " + args.length);
                        return false;
                     }

                     auxString = auxString + args[i];
                     if (i < args.length - 1) {
                        auxString = auxString + " ";
                     }
                  }

                  this.dataHolder = this.worldsHolder.getWorldData(auxString);
                  this.permissionHandler = this.dataHolder.getPermissionsHandler();
                  this.selectedWorlds.put(sender.getName(), this.dataHolder.getName());
                  sender.sendMessage(ChatColor.YELLOW + "You have selected world '" + this.dataHolder.getName() + "'.");
                  return true;
               case manclear:
                  if (args.length != 0) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count!");
                     return false;
                  }

                  this.selectedWorlds.remove(sender.getName());
                  sender.sendMessage(ChatColor.YELLOW + "You have removed your world selection. Working with current world(if possible).");
                  return true;
               case mancheckw:
                  if (args.length < 1) {
                     sender.sendMessage(ChatColor.RED + "Review your arguments count! (/mancheckw <world>)");
                     sender.sendMessage(ChatColor.YELLOW + "Worlds available: ");
                     ArrayList<OverloadedWorldHolder> worlds = this.worldsHolder.allWorldsDataList();
                     auxString = "";

                     for(int i = 0; i < worlds.size(); ++i) {
                        auxString = auxString + ((OverloadedWorldHolder)worlds.get(i)).getName();
                        if (i + 1 < worlds.size()) {
                           auxString = auxString + ", ";
                        }
                     }

                     sender.sendMessage(ChatColor.YELLOW + auxString);
                     return false;
                  }

                  auxString = "";

                  for(int i = 0; i < args.length; ++i) {
                     if (args[i] == null) {
                        logger.warning("Bukkit gave invalid arguments array! Cmd: " + cmd.getName() + " args.length: " + args.length);
                        return false;
                     }

                     auxString = auxString + args[i];
                     if (i < args.length - 1) {
                        auxString = auxString + " ";
                     }
                  }

                  this.dataHolder = this.worldsHolder.getWorldData(auxString);
                  sender.sendMessage(ChatColor.YELLOW + "You have selected world '" + this.dataHolder.getName() + "'.");
                  sender.sendMessage(ChatColor.YELLOW + "This world is using the following data files..");
                  sender.sendMessage(ChatColor.YELLOW + "Groups:" + ChatColor.GREEN + " " + this.dataHolder.getGroupsFile().getAbsolutePath());
                  sender.sendMessage(ChatColor.YELLOW + "Users:" + ChatColor.GREEN + " " + this.dataHolder.getUsersFile().getAbsolutePath());
                  return true;
            }
         }

         sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
         return true;
      }
   }

   private boolean setDefaultWorldHandler(CommandSender sender) {
      this.dataHolder = this.worldsHolder.getWorldData(this.worldsHolder.getDefaultWorld().getName());
      this.permissionHandler = this.dataHolder.getPermissionsHandler();
      if (this.dataHolder != null && this.permissionHandler != null) {
         this.selectedWorlds.put(sender.getName(), this.dataHolder.getName());
         sender.sendMessage(ChatColor.RED + "Couldn't retrieve your world. Default world '" + this.worldsHolder.getDefaultWorld().getName() + "' selected.");
         return true;
      } else {
         sender.sendMessage(ChatColor.RED + "Couldn't retrieve your world. World selection is needed.");
         sender.sendMessage(ChatColor.RED + "Use /manselect <world>");
         return false;
      }
   }

   public static void notify(String name, String msg) {
      Player player = Bukkit.getServer().getPlayerExact(name);

      for(Player test : Bukkit.getServer().getOnlinePlayers()) {
         if (!test.equals(player)) {
            if (test.hasPermission("groupmanager.notify.other")) {
               test.sendMessage(ChatColor.YELLOW + name + " was" + msg);
            }
         } else if (player != null && (player.hasPermission("groupmanager.notify.self") || player.hasPermission("groupmanager.notify.other"))) {
            player.sendMessage(ChatColor.YELLOW + "You were" + msg);
         }
      }

   }

   private List validatePlayer(String playerName, CommandSender sender) {
      new ArrayList();
      List<String> match = new ArrayList();
      List players = this.getServer().matchPlayer(playerName);
      if (players.isEmpty()) {
         if (Arrays.asList(this.getServer().getOfflinePlayers()).contains(Bukkit.getOfflinePlayer(playerName))) {
            match.add(playerName);
         } else {
            for(OfflinePlayer offline : this.getServer().getOfflinePlayers()) {
               if (offline.getName().toLowerCase().startsWith(playerName.toLowerCase())) {
                  match.add(offline.getName());
               }
            }
         }
      } else {
         for(Player player : players) {
            match.add(player.getName());
         }
      }

      if (!match.isEmpty() && match != null) {
         if (match.size() > 1) {
            sender.sendMessage(ChatColor.RED + "Too many matches found! (" + match.toString() + ")");
            return null;
         } else {
            return match;
         }
      } else {
         sender.sendMessage(ChatColor.RED + "Player not found!");
         return null;
      }
   }

   public GMConfiguration getGMConfig() {
      return this.config;
   }

   public File getBackupFolder() {
      return this.backupFolder;
   }

   public static GlobalGroups getGlobalGroups() {
      return globalGroups;
   }

   public static GroupManagerEventHandler getGMEventHandler() {
      return GMEventHandler;
   }

   public static void setGMEventHandler(GroupManagerEventHandler gMEventHandler) {
      GMEventHandler = gMEventHandler;
   }
}
