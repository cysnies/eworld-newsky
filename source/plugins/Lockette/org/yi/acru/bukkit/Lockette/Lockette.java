package org.yi.acru.bukkit.Lockette;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.PluginCore;

public class Lockette extends PluginCore {
   private static Lockette plugin;
   private static boolean enabled = false;
   private static boolean registered = false;
   private final LocketteBlockListener blockListener = new LocketteBlockListener(this);
   private final LocketteEntityListener entityListener = new LocketteEntityListener(this);
   private final LockettePlayerListener playerListener = new LockettePlayerListener(this);
   private final LockettePrefixListener prefixListener = new LockettePrefixListener(this);
   private final LocketteWorldListener worldListener = new LocketteWorldListener(this);
   protected final LocketteDoorCloser doorCloser = new LocketteDoorCloser(this);
   protected static boolean explosionProtectionAll;
   protected static boolean rotateChests;
   protected static boolean adminSnoop;
   protected static boolean adminBypass;
   protected static boolean adminBreak;
   protected static boolean protectDoors;
   protected static boolean protectTrapDoors;
   protected static boolean usePermissions;
   protected static boolean directPlacement;
   protected static boolean colorTags;
   protected static boolean debugMode;
   protected static int defaultDoorTimer;
   protected static String broadcastSnoopTarget;
   protected static String broadcastBreakTarget;
   protected static String broadcastReloadTarget;
   protected static boolean msgUser;
   protected static boolean msgOwner;
   protected static boolean msgAdmin;
   protected static boolean msgError;
   protected static boolean msgHelp;
   protected static String altPrivate;
   protected static String altMoreUsers;
   protected static String altEveryone;
   protected static String altOperators;
   protected static String altTimer;
   protected static String altFee;
   protected static List customBlockList = null;
   protected static List disabledPluginList = null;
   protected static FileConfiguration strings = null;
   protected final HashMap playerList = new HashMap();
   static final int materialTrapDoor = 96;
   static final int materialFenceGate = 107;

   public Lockette() {
      plugin = this;
   }

   public void onLoad() {
   }

   public void onEnable() {
      if (!enabled) {
         log.info("[" + this.getDescription().getName() + "] Version " + this.getDescription().getVersion() + " is being enabled!  Yay!  (Core version " + getCoreVersion() + ")");
         int recBuild = 2771;
         int minBuild = 2735;
         float build = getBuildVersion();
         int printBuild;
         if (build > 399.0F && build < 400.0F) {
            printBuild = (int)((build - 399.0F) * 100.0F);
         } else {
            printBuild = (int)build;
         }

         if (build == 0.0F) {
            log.warning("[" + this.getDescription().getName() + "] Craftbukkit build unrecognized, please be sure you have build [" + 2771 + "] or greater.");
         } else {
            if (build < 2735.0F) {
               log.severe("[" + this.getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but requires requires build [" + 2735 + "] or greater!");
               log.severe("[" + this.getDescription().getName() + "] Aborting enable!");
               return;
            }

            if (build < 2771.0F) {
               log.warning("[" + this.getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but the recommended build is [" + 2771 + "] or greater.");
            } else if (build >= 605.0F && build <= 612.0F) {
               log.warning("[" + this.getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but this build is buggy!  Please upgrade to build 617 or greater.");
            } else if (build >= 685.0F && build <= 703.0F) {
               log.warning("[" + this.getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but this build is buggy!  Please upgrade to build 704 or greater.");
            } else {
               log.info("[" + this.getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "] ok.");
            }
         }

         this.loadProperties(false);
         super.onEnable();
         if (!registered) {
            this.blockListener.registerEvents();
            this.entityListener.registerEvents();
            this.playerListener.registerEvents();
            this.prefixListener.registerEvents();
            this.worldListener.registerEvents();
            registered = true;
         }

         log.info("[" + this.getDescription().getName() + "] Ready to protect your containers.");
         enabled = true;
      }
   }

   public void onDisable() {
      if (enabled) {
         log.info(this.getDescription().getName() + " is being disabled...  ;.;");
         if (protectDoors || protectTrapDoors) {
            log.info("[" + this.getDescription().getName() + "] Closing all automatic doors.");
            this.doorCloser.cleanup();
         }

         super.onDisable();
         enabled = false;
      }
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if (!cmd.getName().equalsIgnoreCase("lockette")) {
         return false;
      } else if (sender instanceof Player) {
         return true;
      } else {
         if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
               this.loadProperties(true);
               this.localizedMessage((Player)null, broadcastReloadTarget, "msg-admin-reload");
            } else if (args[0].equalsIgnoreCase("coredump")) {
               this.dumpCoreInfo();
            }
         }

         return true;
      }
   }

   protected void loadProperties(boolean reload) {
      if (reload) {
         log.info("[" + this.getDescription().getName() + "] Reloading plugin configuration files.");
         this.reloadConfig();
      }

      FileConfiguration properties = this.getConfig();
      boolean propChanged = true;
      msgUser = properties.getBoolean("enable-messages-user", true);
      properties.set("enable-messages-user", msgUser);
      msgOwner = properties.getBoolean("enable-messages-owner", false);
      properties.set("enable-messages-owner", msgOwner);
      msgAdmin = properties.getBoolean("enable-messages-admin", true);
      properties.set("enable-messages-admin", msgAdmin);
      msgError = properties.getBoolean("enable-messages-error", true);
      properties.set("enable-messages-error", msgError);
      msgHelp = properties.getBoolean("enable-messages-help", true);
      properties.set("enable-messages-help", msgHelp);
      explosionProtectionAll = properties.getBoolean("explosion-protection-all", true);
      properties.set("explosion-protection-all", explosionProtectionAll);
      rotateChests = properties.getBoolean("enable-chest-rotation", false);
      properties.set("enable-chest-rotation", rotateChests);
      usePermissions = properties.getBoolean("enable-permissions", false);
      properties.set("enable-permissions", usePermissions);
      protectDoors = properties.getBoolean("enable-protection-doors", true);
      properties.set("enable-protection-doors", protectDoors);
      protectTrapDoors = properties.getBoolean("enable-protection-trapdoors", true);
      properties.set("enable-protection-trapdoors", protectTrapDoors);
      adminSnoop = properties.getBoolean("allow-admin-snoop", false);
      properties.set("allow-admin-snoop", adminSnoop);
      adminBypass = properties.getBoolean("allow-admin-bypass", true);
      properties.set("allow-admin-bypass", adminBypass);
      adminBreak = properties.getBoolean("allow-admin-break", true);
      properties.set("allow-admin-break", adminBreak);
      if (!protectDoors && !protectTrapDoors) {
         this.doorCloser.stop();
      } else if (this.doorCloser.start()) {
         log.severe("[" + this.getDescription().getName() + "] Failed to register door closing task!");
      }

      directPlacement = properties.getBoolean("enable-quick-protect", true);
      properties.set("enable-quick-protect", directPlacement);
      colorTags = properties.getBoolean("enable-color-tags", true);
      properties.set("enable-color-tags", colorTags);
      debugMode = properties.getBoolean("enable-debug", false);
      if (debugMode) {
         log.warning("[" + this.getDescription().getName() + "] Debug mode is enabled, so Lockette chests are NOT secure.");
      }

      defaultDoorTimer = properties.getInt("default-door-timer", -1);
      if (defaultDoorTimer == -1) {
         defaultDoorTimer = 0;
         properties.set("default-door-timer", defaultDoorTimer);
         propChanged = true;
      }

      customBlockList = properties.getList("custom-lockable-block-list");
      if (customBlockList == null) {
         customBlockList = new ArrayList(3);
         customBlockList.add(Material.ENCHANTMENT_TABLE.getId());
         customBlockList.add(Material.JUKEBOX.getId());
         customBlockList.add(Material.DIAMOND_BLOCK.getId());
         customBlockList.add(Material.ANVIL.getId());
         customBlockList.add(Material.HOPPER.getId());
         properties.set("custom-lockable-block-list", customBlockList);
         propChanged = true;
      }

      if (!customBlockList.isEmpty()) {
         log.info("[" + this.getDescription().getName() + "] Custom lockable block list: " + customBlockList.toString());
      }

      disabledPluginList = properties.getList("linked-plugin-ignore-list");
      if (disabledPluginList == null) {
         disabledPluginList = new ArrayList(1);
         disabledPluginList.add("mcMMO");
         properties.set("linked-plugin-ignore-list", disabledPluginList);
         propChanged = true;
      }

      if (!disabledPluginList.isEmpty()) {
         log.info("[" + this.getDescription().getName() + "] Ignoring linked plugins: " + disabledPluginList.toString());
      }

      broadcastSnoopTarget = properties.getString("broadcast-snoop-target");
      if (broadcastSnoopTarget == null) {
         broadcastSnoopTarget = "[Everyone]";
         properties.set("broadcast-snoop-target", broadcastSnoopTarget);
         propChanged = true;
      }

      broadcastBreakTarget = properties.getString("broadcast-break-target");
      if (broadcastBreakTarget == null) {
         broadcastBreakTarget = "[Everyone]";
         properties.set("broadcast-break-target", broadcastBreakTarget);
         propChanged = true;
      }

      broadcastReloadTarget = properties.getString("broadcast-reload-target");
      if (broadcastReloadTarget == null) {
         broadcastReloadTarget = "[Operators]";
         properties.set("broadcast-reload-target", broadcastReloadTarget);
         propChanged = true;
      }

      String stringsFileName = properties.getString("strings-file-name");
      if (stringsFileName == null || stringsFileName.isEmpty()) {
         stringsFileName = "strings-en.yml";
         properties.set("strings-file-name", stringsFileName);
         propChanged = true;
      }

      if (propChanged) {
         this.saveConfig();
      }

      this.loadStrings(reload, stringsFileName);
   }

   protected void loadStrings(boolean reload, String fileName) {
      boolean stringChanged = false;
      File stringsFile = new File(this.getDataFolder(), fileName);
      if (strings != null) {
         strings = null;
      }

      strings = new YamlConfiguration();

      try {
         strings.load(stringsFile);
      } catch (InvalidConfigurationException ex) {
         log.warning("[" + this.getDescription().getName() + "] Error loading " + fileName + ": " + ex.getMessage());
         if (!fileName.equals("strings-en.yml")) {
            this.loadStrings(reload, "strings-en.yml");
            return;
         }

         log.warning("[" + this.getDescription().getName() + "] Returning to default strings.");
      } catch (Exception var11) {
      }

      boolean original = false;
      if (fileName.equals("strings-en.yml")) {
         original = true;
         strings.set("language", "English");
         if (original) {
            try {
               strings.save(stringsFile);
               strings.load(stringsFile);
            } catch (Exception var9) {
            }
         }

         strings.set("author", "Acru");
         strings.set("editors", "");
         strings.set("version", 0);
      }

      String tempString = strings.getString("language");
      if (tempString != null && !tempString.isEmpty()) {
         log.info("[" + this.getDescription().getName() + "] Loading strings file for " + tempString + " by " + strings.getString("author"));
      } else {
         log.info("[" + this.getDescription().getName() + "] Loading strings file " + fileName);
      }

      altPrivate = strings.getString("alternate-private-tag");
      if (altPrivate == null || altPrivate.isEmpty() || original && altPrivate.equals("Privé")) {
         altPrivate = "Private";
         strings.set("alternate-private-tag", altPrivate);
      }

      altPrivate = "[" + altPrivate + "]";
      altMoreUsers = strings.getString("alternate-moreusers-tag");
      if (altMoreUsers == null || altMoreUsers.isEmpty() || original && altMoreUsers.equals("Autre Noms")) {
         altMoreUsers = "More Users";
         strings.set("alternate-moreusers-tag", altMoreUsers);
         stringChanged = true;
      }

      altMoreUsers = "[" + altMoreUsers + "]";
      altEveryone = strings.getString("alternate-everyone-tag");
      if (altEveryone == null || altEveryone.isEmpty() || original && altEveryone.equals("Tout le Monde")) {
         altEveryone = "Everyone";
         strings.set("alternate-everyone-tag", altEveryone);
         stringChanged = true;
      }

      altEveryone = "[" + altEveryone + "]";
      altOperators = strings.getString("alternate-operators-tag");
      if (altOperators == null || altOperators.isEmpty() || original && altOperators.equals("Opérateurs")) {
         altOperators = "Operators";
         strings.set("alternate-operators-tag", altOperators);
         stringChanged = true;
      }

      altOperators = "[" + altOperators + "]";
      altTimer = strings.getString("alternate-timer-tag");
      if (altTimer == null || altTimer.isEmpty() || original && altTimer.equals("Minuterie")) {
         altTimer = "Timer";
         strings.set("alternate-timer-tag", altTimer);
         stringChanged = true;
      }

      altFee = strings.getString("alternate-fee-tag");
      if (altFee == null || altFee.isEmpty()) {
         altFee = "Fee";
         strings.set("alternate-fee-tag", altFee);
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-conflict-door");
      if (tempString == null) {
         strings.set("msg-user-conflict-door", "Conflicting door removed!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-illegal");
      if (tempString == null) {
         strings.set("msg-user-illegal", "Illegal chest removed!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-resize-owned");
      if (tempString == null) {
         strings.set("msg-user-resize-owned", "You cannot resize a chest claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-help-chest");
      if (tempString == null) {
         strings.set("msg-help-chest", "Place a sign headed [Private] next to a chest to lock it.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-owner-release");
      if (tempString == null) {
         strings.set("msg-owner-release", "You have released a container!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-release");
      if (tempString == null) {
         strings.set("msg-admin-release", "(Admin) @@@ has broken open a container owned by ***!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-release-owned");
      if (tempString == null) {
         strings.set("msg-user-release-owned", "You cannot release a container claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-owner-remove");
      if (tempString == null) {
         strings.set("msg-owner-remove", "You have removed users from a container!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-remove-owned");
      if (tempString == null) {
         strings.set("msg-user-remove-owned", "You cannot remove users from a container claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-break-owned");
      if (tempString == null) {
         strings.set("msg-user-break-owned", "You cannot break a container claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-denied-door");
      if (tempString == null) {
         strings.set("msg-user-denied-door", "You don't have permission to use this door.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-touch-fee");
      if (tempString == null) {
         strings.set("msg-user-touch-fee", "A fee of ### will be paid to ***, to open.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-touch-owned");
      if (tempString == null) {
         strings.set("msg-user-touch-owned", "This container has been claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-help-select");
      if (tempString == null) {
         strings.set("msg-help-select", "Sign selected, use /lockette <line number> <text> to edit.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-bypass");
      if (tempString == null) {
         strings.set("msg-admin-bypass", "Bypassed a door owned by ***, be sure to close it behind you.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-snoop");
      if (tempString == null) {
         strings.set("msg-admin-snoop", "(Admin) @@@ has snooped around in a container owned by ***!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-user-denied");
      if (tempString == null) {
         strings.set("msg-user-denied", "You don't have permission to open this container.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-zone");
      if (tempString == null) {
         strings.set("msg-error-zone", "This zone is protected by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-permission");
      if (tempString == null) {
         strings.set("msg-error-permission", "Permission to lock container denied.");
         stringChanged = true;
      } else if (tempString.equals("Permission to lock containers denied.")) {
         strings.set("msg-error-permission", "Permission to lock container denied.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-claim");
      if (tempString == null) {
         strings.set("msg-error-claim", "No unclaimed container nearby to make Private!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-claim-conflict");
      if (tempString == null) {
         strings.set("msg-error-claim-conflict", "Conflict with an existing protected door.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-claim-error");
      if (tempString == null) {
         strings.set("msg-admin-claim-error", "Player *** is not online, be sure you have the correct name.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-claim");
      if (tempString == null) {
         strings.set("msg-admin-claim", "You have claimed a container for ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-owner-claim");
      if (tempString == null) {
         strings.set("msg-owner-claim", "You have claimed a container!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-adduser-owned");
      if (tempString == null) {
         strings.set("msg-error-adduser-owned", "You cannot add users to a container claimed by ***.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-adduser");
      if (tempString == null) {
         strings.set("msg-error-adduser", "No claimed container nearby to add users to!");
         stringChanged = true;
      }

      tempString = strings.getString("msg-owner-adduser");
      if (tempString == null) {
         strings.set("msg-owner-adduser", "You have added users to a container!");
         stringChanged = true;
      }

      if (original) {
         strings.set("msg-help-command1", "&C/lockette <line number> <text> - Edits signs on locked containers. Right click on the sign to edit.");
         strings.set("msg-help-command2", "&C/lockette fix - Fixes an automatic door that is in the wrong position. Look at the door to edit.");
         strings.set("msg-help-command3", "&C/lockette reload - Reloads the configuration files. Operators only.");
         strings.set("msg-help-command4", "&C/lockette version - Reports Lockette version.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-admin-reload");
      if (tempString == null) {
         strings.set("msg-admin-reload", "Reloading plugin configuration files.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-fix");
      if (tempString == null) {
         strings.set("msg-error-fix", "No owned door found.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-error-edit");
      if (tempString == null) {
         strings.set("msg-error-edit", "First select a sign by right clicking it.");
         stringChanged = true;
      }

      tempString = strings.getString("msg-owner-edit");
      if (tempString == null) {
         strings.set("msg-owner-edit", "Sign edited successfully.");
         stringChanged = true;
      }

      if (original && stringChanged) {
         try {
            strings.save(stringsFile);
         } catch (Exception var8) {
         }
      }

   }

   public static boolean isProtected(Block block) {
      if (!enabled) {
         return false;
      } else {
         int type = block.getTypeId();
         if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign)block.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
               return true;
            }

            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               Block checkBlock = getSignAttachedBlock(block);
               if (checkBlock != null && findBlockOwner(checkBlock) != null) {
                  return true;
               }
            }
         } else if (findBlockOwner(block) != null) {
            return true;
         }

         return false;
      }
   }

   public static String getProtectedOwner(Block block) {
      if (!enabled) {
         return null;
      } else {
         int type = block.getTypeId();
         if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign)block.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
               return sign.getLine(1).replaceAll("(?i)§[0-F]", "");
            }

            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               Block checkBlock = getSignAttachedBlock(block);
               if (checkBlock != null) {
                  Block signBlock = findBlockOwner(checkBlock);
                  if (signBlock != null) {
                     sign = (Sign)signBlock.getState();
                     return sign.getLine(1).replaceAll("(?i)§[0-F]", "");
                  }
               }
            }
         } else {
            Block signBlock = findBlockOwner(block);
            if (signBlock != null) {
               Sign sign = (Sign)signBlock.getState();
               return sign.getLine(1).replaceAll("(?i)§[0-F]", "");
            }
         }

         return null;
      }
   }

   public static boolean isOwner(Block block, String name) {
      if (!enabled) {
         return true;
      } else {
         Block checkBlock = findBlockOwner(block);
         if (checkBlock == null) {
            return true;
         } else {
            Sign sign = (Sign)checkBlock.getState();
            int length = name.length();
            if (length > 15) {
               length = 15;
            }

            return sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(name.substring(0, length));
         }
      }
   }

   public static boolean isUser(Block block, String name, boolean withGroups) {
      if (!enabled) {
         return true;
      } else {
         Block signBlock = findBlockOwner(block);
         if (signBlock == null) {
            return true;
         } else {
            Sign sign = (Sign)signBlock.getState();
            int length = name.length();
            if (length > 15) {
               length = 15;
            }

            for(int y = 1; y <= 3; ++y) {
               if (!sign.getLine(y).isEmpty()) {
                  String line = sign.getLine(y).replaceAll("(?i)§[0-F]", "");
                  if (line.equalsIgnoreCase(name.substring(0, length))) {
                     return true;
                  }

                  if (withGroups && plugin.inGroup(block.getWorld(), name, line)) {
                     return true;
                  }
               }
            }

            List<Block> list = findBlockUsers(block, signBlock);
            int count = list.size();

            for(int x = 0; x < count; ++x) {
               sign = (Sign)((Block)list.get(x)).getState();

               for(int var13 = 1; var13 <= 3; ++var13) {
                  if (!sign.getLine(var13).isEmpty()) {
                     String line = sign.getLine(var13).replaceAll("(?i)§[0-F]", "");
                     if (line.equalsIgnoreCase(name.substring(0, length))) {
                        return true;
                     }

                     if (withGroups && plugin.inGroup(block.getWorld(), name, line)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public static boolean isEveryone(Block block) {
      if (!enabled) {
         return true;
      } else {
         Block signBlock = findBlockOwner(block);
         if (signBlock == null) {
            return true;
         } else {
            Sign sign = (Sign)signBlock.getState();

            for(int y = 1; y <= 3; ++y) {
               if (!sign.getLine(y).isEmpty()) {
                  String line = sign.getLine(y).replaceAll("(?i)§[0-F]", "");
                  if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(altEveryone)) {
                     return true;
                  }
               }
            }

            List<Block> list = findBlockUsers(block, signBlock);
            int count = list.size();

            for(int x = 0; x < count; ++x) {
               sign = (Sign)((Block)list.get(x)).getState();

               for(int var10 = 1; var10 <= 3; ++var10) {
                  if (!sign.getLine(var10).isEmpty()) {
                     String line = sign.getLine(var10).replaceAll("(?i)§[0-F]", "");
                     if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(altEveryone)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   protected boolean pluginEnableOverride(String pluginName) {
      return isInList(pluginName, disabledPluginList);
   }

   protected boolean usingExternalPermissions() {
      return !usePermissions ? false : super.usingExternalPermissions();
   }

   protected boolean usingExternalZones() {
      return super.usingExternalZones();
   }

   protected String getLocalizedEveryone() {
      return altEveryone;
   }

   protected String getLocalizedOperators() {
      return altOperators;
   }

   protected void localizedMessage(Player player, String broadcast, String key) {
      this.localizedMessage(player, broadcast, key, (String)null, (String)null);
   }

   protected void localizedMessage(Player player, String broadcast, String key, String sub) {
      this.localizedMessage(player, broadcast, key, sub, (String)null);
   }

   protected void localizedMessage(Player player, String broadcast, String key, String sub, String num) {
      String color = "";
      if (key.startsWith("msg-user-")) {
         if (broadcast == null && !msgUser) {
            return;
         }

         color = ChatColor.YELLOW.toString();
      } else if (key.startsWith("msg-owner-")) {
         if (broadcast == null && !msgOwner) {
            return;
         }

         color = ChatColor.GOLD.toString();
      } else if (key.startsWith("msg-admin-")) {
         if (broadcast == null && !msgAdmin) {
            return;
         }

         color = ChatColor.RED.toString();
      } else if (key.startsWith("msg-error-")) {
         if (broadcast == null && !msgError) {
            return;
         }

         color = ChatColor.RED.toString();
      } else if (key.startsWith("msg-help-")) {
         if (broadcast == null && !msgHelp) {
            return;
         }

         color = ChatColor.GOLD.toString();
      }

      String message = strings.getString(key);
      if (message != null && !message.isEmpty()) {
         message = message.replaceAll("&([0-9A-Fa-f])", "§$1");
         if (sub != null) {
            message = message.replaceAll("\\*\\*\\*", sub + color);
         }

         if (num != null) {
            message = message.replaceAll("###", num);
         }

         if (player != null) {
            message = message.replaceAll("@@@", player.getName());
         }

         if (broadcast != null) {
            this.selectiveBroadcast(broadcast, color + "[Lockette] " + message);
         } else if (player != null) {
            player.sendMessage(color + "[Lockette] " + message);
         }

      }
   }

   protected static Block findBlockOwner(Block block) {
      return findBlockOwner(block, (Block)null, false);
   }

   protected static Block findBlockOwnerBreak(Block block) {
      int type = block.getTypeId();
      if (type != Material.CHEST.getId() && type != Material.TRAPPED_CHEST.getId()) {
         if (type != Material.DISPENSER.getId() && type != Material.DROPPER.getId() && type != Material.FURNACE.getId() && type != Material.BURNING_FURNACE.getId() && type != Material.BREWING_STAND.getId() && !isInList(type, customBlockList)) {
            if (protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
               return findBlockOwnerBase(block, (Location)null, false, false, false, false, false);
            } else if (!protectDoors || type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
               Block checkBlock = findBlockOwnerBase(block, (Location)null, false, false, false, false, false);
               if (checkBlock != null) {
                  return checkBlock;
               } else {
                  if (protectTrapDoors) {
                     checkBlock = block.getRelative(BlockFace.NORTH);
                     if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 2) {
                        checkBlock = findBlockOwnerBase(checkBlock, (Location)null, false, false, false, false, false);
                        if (checkBlock != null) {
                           return checkBlock;
                        }
                     }

                     checkBlock = block.getRelative(BlockFace.EAST);
                     if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 0) {
                        checkBlock = findBlockOwnerBase(checkBlock, (Location)null, false, false, false, false, false);
                        if (checkBlock != null) {
                           return checkBlock;
                        }
                     }

                     checkBlock = block.getRelative(BlockFace.SOUTH);
                     if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 3) {
                        checkBlock = findBlockOwnerBase(checkBlock, (Location)null, false, false, false, false, false);
                        if (checkBlock != null) {
                           return checkBlock;
                        }
                     }

                     checkBlock = block.getRelative(BlockFace.WEST);
                     if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 1) {
                        checkBlock = findBlockOwnerBase(checkBlock, (Location)null, false, false, false, false, false);
                        if (checkBlock != null) {
                           return checkBlock;
                        }
                     }
                  }

                  if (protectDoors) {
                     checkBlock = block.getRelative(BlockFace.UP);
                     type = checkBlock.getTypeId();
                     if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
                        return findBlockOwnerBase(checkBlock, (Location)null, false, true, true, false, false);
                     }
                  }

                  return null;
               }
            } else {
               return findBlockOwnerBase(block, (Location)null, false, true, true, false, false);
            }
         } else {
            return findBlockOwnerBase(block, (Location)null, false, false, false, false, false);
         }
      } else {
         return findBlockOwnerBase(block, (Location)null, false, false, false, false, false);
      }
   }

   protected static Block findBlockOwner(Block block, Block ignoreBlock, boolean iterateFurther) {
      int type = block.getTypeId();
      Location ignore;
      if (ignoreBlock != null) {
         ignore = ignoreBlock.getLocation();
      } else {
         ignore = null;
      }

      if (type != Material.CHEST.getId() && type != Material.TRAPPED_CHEST.getId()) {
         if (type != Material.DISPENSER.getId() && type != Material.DROPPER.getId() && type != Material.FURNACE.getId() && type != Material.BURNING_FURNACE.getId() && type != Material.BREWING_STAND.getId() && !isInList(type, customBlockList)) {
            if (protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
               return findBlockOwner(getTrapDoorAttachedBlock(block), ignoreBlock, false);
            } else if (!protectDoors || type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
               if (protectTrapDoors) {
                  Block checkBlock = findBlockOwnerBase(block, ignore, false, false, false, false, false);
                  if (checkBlock != null) {
                     return checkBlock;
                  }

                  checkBlock = block.getRelative(BlockFace.NORTH);
                  if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 2) {
                     checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                     if (checkBlock != null) {
                        return checkBlock;
                     }
                  }

                  checkBlock = block.getRelative(BlockFace.EAST);
                  if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 0) {
                     checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                     if (checkBlock != null) {
                        return checkBlock;
                     }
                  }

                  checkBlock = block.getRelative(BlockFace.SOUTH);
                  if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 3) {
                     checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                     if (checkBlock != null) {
                        return checkBlock;
                     }
                  }

                  checkBlock = block.getRelative(BlockFace.WEST);
                  if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId() && (checkBlock.getData() & 3) == 1) {
                     checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                     if (checkBlock != null) {
                        return checkBlock;
                     }
                  }
               }

               if (protectDoors) {
                  Block checkBlock = block.getRelative(BlockFace.UP);
                  type = checkBlock.getTypeId();
                  if (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 107) {
                     Block result = findBlockOwnerBase(checkBlock, ignore, true, true, true, true, iterateFurther);
                     if (result != null) {
                        return result;
                     }
                  }

                  checkBlock = block.getRelative(BlockFace.DOWN);
                  type = checkBlock.getTypeId();
                  if (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 107) {
                     Block checkBlock2 = checkBlock.getRelative(BlockFace.DOWN);
                     type = checkBlock2.getTypeId();
                     if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
                        return findBlockOwnerBase(checkBlock, ignore, true, true, false, true, iterateFurther);
                     }

                     return findBlockOwnerBase(checkBlock2, ignore, true, true, false, true, iterateFurther);
                  }
               }

               return null;
            } else {
               return findBlockOwnerBase(block, ignore, true, true, true, true, iterateFurther);
            }
         } else {
            return findBlockOwnerBase(block, ignore, false, false, false, false, false);
         }
      } else {
         return findBlockOwnerBase(block, ignore, true, false, false, false, false);
      }
   }

   private static Block findBlockOwnerBase(Block block, Location ignore, boolean iterate, boolean iterateUp, boolean iterateDown, boolean includeEnds, boolean iterateFurther) {
      if (iterateUp) {
         Block checkBlock = block.getRelative(BlockFace.UP);
         int type = checkBlock.getTypeId();
         if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
            if (includeEnds) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
            } else {
               checkBlock = null;
            }
         } else {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, iterateUp, false, includeEnds, false);
         }

         if (checkBlock != null) {
            return checkBlock;
         }
      }

      if (iterateDown) {
         Block checkBlock = block.getRelative(BlockFace.DOWN);
         int type = checkBlock.getTypeId();
         if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
            if (includeEnds) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
            } else {
               checkBlock = null;
            }
         } else {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, iterateDown, includeEnds, false);
         }

         if (checkBlock != null) {
            return checkBlock;
         }
      }

      Block checkBlock = block.getRelative(BlockFace.NORTH);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 2) {
            boolean doCheck;
            if (ignore == null) {
               doCheck = true;
            } else if (checkBlock.getLocation().equals(ignore)) {
               doCheck = false;
            } else {
               doCheck = true;
            }

            if (doCheck) {
               Sign sign = (Sign)checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
               if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                  return checkBlock;
               }
            }
         }
      } else if (iterate && checkBlock.getTypeId() == block.getTypeId()) {
         checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
         if (checkBlock != null) {
            return checkBlock;
         }
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 5) {
            boolean doCheck;
            if (ignore == null) {
               doCheck = true;
            } else if (checkBlock.getLocation().equals(ignore)) {
               doCheck = false;
            } else {
               doCheck = true;
            }

            if (doCheck) {
               Sign sign = (Sign)checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
               if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                  return checkBlock;
               }
            }
         }
      } else if (iterate && checkBlock.getTypeId() == block.getTypeId()) {
         checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
         if (checkBlock != null) {
            return checkBlock;
         }
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 3) {
            boolean doCheck;
            if (ignore == null) {
               doCheck = true;
            } else if (checkBlock.getLocation().equals(ignore)) {
               doCheck = false;
            } else {
               doCheck = true;
            }

            if (doCheck) {
               Sign sign = (Sign)checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
               if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                  return checkBlock;
               }
            }
         }
      } else if (iterate && checkBlock.getTypeId() == block.getTypeId()) {
         checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
         if (checkBlock != null) {
            return checkBlock;
         }
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 4) {
            boolean doCheck;
            if (ignore == null) {
               doCheck = true;
            } else if (checkBlock.getLocation().equals(ignore)) {
               doCheck = false;
            } else {
               doCheck = true;
            }

            if (doCheck) {
               Sign sign = (Sign)checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
               if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                  return checkBlock;
               }
            }
         }
      } else if (iterate && checkBlock.getTypeId() == block.getTypeId()) {
         checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
         if (checkBlock != null) {
            return checkBlock;
         }
      }

      return null;
   }

   protected static List findBlockUsers(Block block, Block signBlock) {
      int type = block.getTypeId();
      if (type != Material.CHEST.getId() && type != Material.TRAPPED_CHEST.getId()) {
         if (protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
            return findBlockUsersBase(getTrapDoorAttachedBlock(block), false, false, false, true, 0);
         } else {
            return !protectDoors || type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107 ? findBlockUsersBase(block, false, false, false, false, 0) : findBlockUsersBase(block, true, true, true, false, signBlock.getY());
         }
      } else {
         return findBlockUsersBase(block, true, false, false, false, 0);
      }
   }

   private static List findBlockUsersBase(Block block, boolean iterate, boolean iterateUp, boolean iterateDown, boolean traps, int includeYPos) {
      List<Block> list = new ArrayList();
      if (iterateUp) {
         Block checkBlock = block.getRelative(BlockFace.UP);
         int type = checkBlock.getTypeId();
         if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
            if (checkBlock.getY() == includeYPos) {
               list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
         } else {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, false, false, includeYPos));
         }
      }

      if (iterateDown) {
         Block checkBlock = block.getRelative(BlockFace.DOWN);
         int type = checkBlock.getTypeId();
         if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
         } else {
            list.addAll(findBlockUsersBase(checkBlock, false, false, iterateDown, false, includeYPos));
         }
      }

      Block checkBlock = block.getRelative(BlockFace.NORTH);
      int type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 2) {
            Sign sign = (Sign)checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               list.add(checkBlock);
            }
         }
      } else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      } else if (traps && type == Material.TRAP_DOOR.getId()) {
         byte face = checkBlock.getData();
         if ((face & 3) == 2) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
         }
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 5) {
            Sign sign = (Sign)checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               list.add(checkBlock);
            }
         }
      } else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      } else if (traps && type == Material.TRAP_DOOR.getId()) {
         byte face = checkBlock.getData();
         if ((face & 3) == 0) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
         }
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 3) {
            Sign sign = (Sign)checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               list.add(checkBlock);
            }
         }
      } else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      } else if (traps && type == Material.TRAP_DOOR.getId()) {
         byte face = checkBlock.getData();
         if ((face & 3) == 3) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
         }
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         byte face = checkBlock.getData();
         if (face == 4) {
            Sign sign = (Sign)checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
               list.add(checkBlock);
            }
         }
      } else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      } else if (traps && type == Material.TRAP_DOOR.getId()) {
         byte face = checkBlock.getData();
         if ((face & 3) == 1) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
         }
      }

      return list;
   }

   protected static int findChestCountNear(Block block) {
      return findChestCountNearBase(block, (byte)0);
   }

   private static int findChestCountNearBase(Block block, byte face) {
      int count = 0;
      if (face != 2) {
         Block checkBlock = block.getRelative(BlockFace.NORTH);
         if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
            ++count;
            if (face == 0) {
               count += findChestCountNearBase(checkBlock, (byte)3);
            }
         }
      }

      if (face != 5) {
         Block checkBlock = block.getRelative(BlockFace.EAST);
         if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
            ++count;
            if (face == 0) {
               count += findChestCountNearBase(checkBlock, (byte)4);
            }
         }
      }

      if (face != 3) {
         Block checkBlock = block.getRelative(BlockFace.SOUTH);
         if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
            ++count;
            if (face == 0) {
               count += findChestCountNearBase(checkBlock, (byte)2);
            }
         }
      }

      if (face != 4) {
         Block checkBlock = block.getRelative(BlockFace.WEST);
         if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
            ++count;
            if (face == 0) {
               count += findChestCountNearBase(checkBlock, (byte)5);
            }
         }
      }

      return count;
   }

   protected static void rotateChestOrientation(Block block, BlockFace blockFace) {
      if (block.getTypeId() == Material.CHEST.getId() || block.getTypeId() == Material.TRAPPED_CHEST.getId()) {
         if (rotateChests || block.getData() == 0) {
            byte face;
            if (blockFace == BlockFace.NORTH) {
               face = 2;
            } else if (blockFace == BlockFace.EAST) {
               face = 5;
            } else if (blockFace == BlockFace.SOUTH) {
               face = 3;
            } else {
               if (blockFace != BlockFace.WEST) {
                  return;
               }

               face = 4;
            }

            Block checkBlock = block.getRelative(BlockFace.NORTH);
            if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
               if (face == 4 || face == 5) {
                  block.setData(face);
                  checkBlock.setData(face);
               }

            } else {
               checkBlock = block.getRelative(BlockFace.EAST);
               if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
                  if (face == 2 || face == 3) {
                     block.setData(face);
                     checkBlock.setData(face);
                  }

               } else {
                  checkBlock = block.getRelative(BlockFace.SOUTH);
                  if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
                     if (face == 4 || face == 5) {
                        block.setData(face);
                        checkBlock.setData(face);
                     }

                  } else {
                     checkBlock = block.getRelative(BlockFace.WEST);
                     if ((checkBlock.getTypeId() == Material.CHEST.getId() || checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()) && checkBlock.getTypeId() == block.getTypeId()) {
                        if (face == 2 || face == 3) {
                           block.setData(face);
                           checkBlock.setData(face);
                        }

                     } else {
                        block.setData(face);
                     }
                  }
               }
            }
         }
      }
   }

   protected static List toggleDoors(Block block, Block keyBlock, boolean wooden, boolean trap) {
      List<Block> list = new ArrayList();
      toggleDoorBase(block, keyBlock, !trap, wooden, list);

      try {
         if (!wooden) {
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
         }
      } catch (NoSuchFieldError var6) {
      } catch (NoSuchMethodError var7) {
      } catch (NoClassDefFoundError var8) {
      }

      return list;
   }

   protected static void toggleSingleDoor(Block block) {
      int type = block.getTypeId();
      if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId()) {
         if (type == 96 || type == 107) {
            toggleDoorBase(block, (Block)null, false, false, (List)null);
         }
      } else {
         toggleDoorBase(block, (Block)null, true, false, (List)null);
      }

   }

   protected static void toggleHalfDoor(Block block, boolean effect) {
      int type = block.getTypeId();
      if (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 96 || type == 107) {
         block.setData((byte)(block.getData() ^ 4));

         try {
            if (effect) {
               block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
            }
         } catch (NoSuchFieldError var4) {
         } catch (NoSuchMethodError var5) {
         } catch (NoClassDefFoundError var6) {
         }
      }

   }

   private static void toggleDoorBase(Block block, Block keyBlock, boolean iterateUpDown, boolean skipDoor, List list) {
      if (list != null) {
         list.add(block);
      }

      if (!skipDoor) {
         block.setData((byte)(block.getData() ^ 4));
      }

      if (iterateUpDown) {
         Block checkBlock = block.getRelative(BlockFace.UP);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            toggleDoorBase(checkBlock, (Block)null, false, skipDoor, list);
         }

         checkBlock = block.getRelative(BlockFace.DOWN);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            toggleDoorBase(checkBlock, (Block)null, false, skipDoor, list);
         }
      }

      if (keyBlock != null) {
         Block checkBlock = block.getRelative(BlockFace.NORTH);
         if (checkBlock.getTypeId() == block.getTypeId() && (checkBlock.getX() == keyBlock.getX() && checkBlock.getZ() == keyBlock.getZ() || block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())) {
            toggleDoorBase(checkBlock, (Block)null, true, false, list);
         }

         checkBlock = block.getRelative(BlockFace.EAST);
         if (checkBlock.getTypeId() == block.getTypeId() && (checkBlock.getX() == keyBlock.getX() && checkBlock.getZ() == keyBlock.getZ() || block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())) {
            toggleDoorBase(checkBlock, (Block)null, true, false, list);
         }

         checkBlock = block.getRelative(BlockFace.SOUTH);
         if (checkBlock.getTypeId() == block.getTypeId() && (checkBlock.getX() == keyBlock.getX() && checkBlock.getZ() == keyBlock.getZ() || block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())) {
            toggleDoorBase(checkBlock, (Block)null, true, false, list);
         }

         checkBlock = block.getRelative(BlockFace.WEST);
         if (checkBlock.getTypeId() == block.getTypeId() && (checkBlock.getX() == keyBlock.getX() && checkBlock.getZ() == keyBlock.getZ() || block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())) {
            toggleDoorBase(checkBlock, (Block)null, true, false, list);
         }
      }

   }

   protected static int getSignOption(Block signBlock, String tag, String altTag, int defaultValue) {
      Sign sign = (Sign)signBlock.getState();

      for(int y = 2; y <= 3; ++y) {
         if (!sign.getLine(y).isEmpty()) {
            String line = sign.getLine(y).replaceAll("(?i)§[0-F]", "");
            int end = line.length() - 1;
            if (end >= 2 && line.charAt(0) == '[' && line.charAt(end) == ']') {
               int index = line.indexOf(":");
               if (index == -1) {
                  if (line.substring(1, end).equalsIgnoreCase(tag) || line.substring(1, end).equalsIgnoreCase(altTag)) {
                     return defaultValue;
                  }
               } else if (line.substring(1, index).equalsIgnoreCase(tag) || line.substring(1, index).equalsIgnoreCase(altTag)) {
                  for(int x = index; x < end; ++x) {
                     if (Character.isDigit(line.charAt(x))) {
                        index = x;
                        break;
                     }
                  }

                  for(int var12 = index + 1; var12 < end; ++var12) {
                     if (!Character.isDigit(line.charAt(var12))) {
                        end = var12;
                        break;
                     }
                  }

                  try {
                     int value = Integer.parseInt(line.substring(index, end));
                     return value;
                  } catch (NumberFormatException var11) {
                     return defaultValue;
                  }
               }
            }
         }
      }

      return defaultValue;
   }

   protected static boolean isInList(Object target, List list) {
      if (list == null) {
         return false;
      } else {
         for(int x = 0; x < list.size(); ++x) {
            if (list.get(x).equals(target)) {
               return true;
            }
         }

         return false;
      }
   }
}
