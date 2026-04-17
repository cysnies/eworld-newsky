package com.wimbli.WorldBorder;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Config {
   private static WorldBorder plugin;
   private static FileConfiguration cfg = null;
   private static final Logger mcLog = Logger.getLogger("Minecraft");
   public static DecimalFormat coord = new DecimalFormat("0.0");
   private static int borderTask = -1;
   public static WorldFillTask fillTask;
   public static WorldTrimTask trimTask;
   private static Set bypassPlayers = Collections.synchronizedSet(new LinkedHashSet());
   private static Runtime rt = Runtime.getRuntime();
   private static boolean shapeRound = true;
   private static Map borders = Collections.synchronizedMap(new LinkedHashMap());
   private static String message;
   private static boolean DEBUG = false;
   private static double knockBack = (double)3.0F;
   private static int timerTicks = 4;
   private static boolean whooshEffect = false;
   private static boolean portalRedirection = true;
   private static boolean dynmapEnable = true;
   private static String dynmapMessage;
   private static int remountDelayTicks = 0;
   private static final String logName = "WorldBorder";
   private static final int currentCfgVersion = 7;

   public static long Now() {
      return System.currentTimeMillis();
   }

   public static void setBorder(String world, BorderData border) {
      borders.put(world, border);
      Log("Border set. " + BorderDescription(world));
      save(true);
      DynMapFeatures.showBorder(world, border);
   }

   public static void setBorder(String world, int radiusX, int radiusZ, double x, double z, Boolean shapeRound) {
      BorderData old = Border(world);
      boolean oldWrap = old == null ? false : old.getWrapping();
      setBorder(world, new BorderData(x, z, radiusX, radiusZ, shapeRound, oldWrap));
   }

   public static void setBorder(String world, int radiusX, int radiusZ, double x, double z) {
      BorderData old = Border(world);
      Boolean oldShape = old == null ? null : old.getShape();
      boolean oldWrap = old == null ? false : old.getWrapping();
      setBorder(world, new BorderData(x, z, radiusX, radiusZ, oldShape, oldWrap));
   }

   public static void setBorder(String world, int radius, double x, double z, Boolean shapeRound) {
      setBorder(world, new BorderData(x, z, radius, radius, shapeRound));
   }

   public static void setBorder(String world, int radius, double x, double z) {
      setBorder(world, radius, radius, x, z);
   }

   public static void setBorderCorners(String world, double x1, double z1, double x2, double z2, Boolean shapeRound, boolean wrap) {
      double radiusX = Math.abs(x1 - x2) / (double)2.0F;
      double radiusZ = Math.abs(z1 - z2) / (double)2.0F;
      double x = (x1 < x2 ? x1 : x2) + radiusX;
      double z = (z1 < z2 ? z1 : z2) + radiusZ;
      setBorder(world, new BorderData(x, z, (int)Math.round(radiusX), (int)Math.round(radiusZ), shapeRound, wrap));
   }

   public static void setBorderCorners(String world, double x1, double z1, double x2, double z2, Boolean shapeRound) {
      setBorderCorners(world, x1, z1, x2, z2, shapeRound, false);
   }

   public static void setBorderCorners(String world, double x1, double z1, double x2, double z2) {
      BorderData old = Border(world);
      Boolean oldShape = old == null ? null : old.getShape();
      boolean oldWrap = old == null ? false : old.getWrapping();
      setBorderCorners(world, x1, z1, x2, z2, oldShape, oldWrap);
   }

   public static void removeBorder(String world) {
      borders.remove(world);
      Log("Removed border for world \"" + world + "\".");
      save(true);
      DynMapFeatures.removeBorder(world);
   }

   public static void removeAllBorders() {
      borders.clear();
      Log("Removed all borders for all worlds.");
      save(true);
      DynMapFeatures.removeAllBorders();
   }

   public static String BorderDescription(String world) {
      BorderData border = (BorderData)borders.get(world);
      return border == null ? "No border was found for the world \"" + world + "\"." : "World \"" + world + "\" has border " + border.toString();
   }

   public static Set BorderDescriptions() {
      Set<String> output = new HashSet();
      Iterator world = borders.keySet().iterator();

      while(world.hasNext()) {
         output.add(BorderDescription((String)world.next()));
      }

      return output;
   }

   public static BorderData Border(String world) {
      return (BorderData)borders.get(world);
   }

   public static Map getBorders() {
      return new LinkedHashMap(borders);
   }

   public static void setMessage(String msg) {
      message = msg;
      Log("Border message is now set to: " + msg);
      save(true);
   }

   public static String Message() {
      return message;
   }

   public static void setShape(boolean round) {
      shapeRound = round;
      Log("Set default border shape to " + ShapeName() + ".");
      save(true);
      DynMapFeatures.showAllBorders();
   }

   public static boolean ShapeRound() {
      return shapeRound;
   }

   public static String ShapeName() {
      return ShapeName(shapeRound);
   }

   public static String ShapeName(boolean round) {
      return round ? "elliptic/round" : "rectangular/square";
   }

   public static void setDebug(boolean debugMode) {
      DEBUG = debugMode;
      Log("Debug mode " + (DEBUG ? "enabled" : "disabled") + ".");
      save(true);
   }

   public static boolean Debug() {
      return DEBUG;
   }

   public static void setWhooshEffect(boolean enable) {
      whooshEffect = enable;
      Log("\"Whoosh\" knockback effect " + (enable ? "enabled" : "disabled") + ".");
      save(true);
   }

   public static boolean whooshEffect() {
      return whooshEffect;
   }

   public static void showWhooshEffect(Location loc) {
      if (whooshEffect()) {
         World world = loc.getWorld();
         world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
         world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
         world.playEffect(loc, Effect.SMOKE, 4);
         world.playEffect(loc, Effect.SMOKE, 4);
         world.playEffect(loc, Effect.SMOKE, 4);
         world.playEffect(loc, Effect.GHAST_SHOOT, 0);
      }
   }

   public static void setPortalRedirection(boolean enable) {
      portalRedirection = enable;
      Log("Portal redirection " + (enable ? "enabled" : "disabled") + ".");
      save(true);
   }

   public static boolean portalRedirection() {
      return portalRedirection;
   }

   public static void setKnockBack(double numBlocks) {
      knockBack = numBlocks;
      Log("Knockback set to " + knockBack + " blocks inside the border.");
      save(true);
   }

   public static double KnockBack() {
      return knockBack;
   }

   public static void setTimerTicks(int ticks) {
      timerTicks = ticks;
      Log("Timer delay set to " + timerTicks + " tick(s). That is roughly " + timerTicks * 50 + "ms / " + (double)timerTicks * (double)50.0F / (double)1000.0F + " seconds.");
      StartBorderTimer();
      save(true);
   }

   public static int TimerTicks() {
      return timerTicks;
   }

   public static void setRemountTicks(int ticks) {
      remountDelayTicks = ticks;
      if (remountDelayTicks == 0) {
         Log("Remount delay set to 0. Players will be left dismounted when knocked back from the border while on a vehicle.");
      } else {
         Log("Remount delay set to " + remountDelayTicks + " tick(s). That is roughly " + remountDelayTicks * 50 + "ms / " + (double)remountDelayTicks * (double)50.0F / (double)1000.0F + " seconds.");
      }

      if (ticks < 10) {
         LogWarn("setting the remount delay to less than 10 (and greater than 0) is not recommended. This can lead to nasty client glitches.");
      }

      save(true);
   }

   public static int RemountTicks() {
      return remountDelayTicks;
   }

   public static void setDynmapBorderEnabled(boolean enable) {
      dynmapEnable = enable;
      Log("DynMap border display is now " + (enable ? "enabled" : "disabled") + ".");
      save(true);
      DynMapFeatures.showAllBorders();
   }

   public static boolean DynmapBorderEnabled() {
      return dynmapEnable;
   }

   public static void setDynmapMessage(String msg) {
      dynmapMessage = msg;
      Log("DynMap border label is now set to: " + msg);
      save(true);
      DynMapFeatures.showAllBorders();
   }

   public static String DynmapMessage() {
      return dynmapMessage;
   }

   public static void setPlayerBypass(String player, boolean bypass) {
      if (bypass) {
         bypassPlayers.add(player.toLowerCase());
      } else {
         bypassPlayers.remove(player.toLowerCase());
      }

   }

   public static boolean isPlayerBypassing(String player) {
      return bypassPlayers.contains(player.toLowerCase());
   }

   public static void togglePlayerBypass(String player) {
      setPlayerBypass(player, !isPlayerBypassing(player));
   }

   public static boolean isBorderTimerRunning() {
      if (borderTask == -1) {
         return false;
      } else {
         return plugin.getServer().getScheduler().isQueued(borderTask) || plugin.getServer().getScheduler().isCurrentlyRunning(borderTask);
      }
   }

   public static void StartBorderTimer() {
      StopBorderTimer();
      borderTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new BorderCheckTask(), (long)timerTicks, (long)timerTicks);
      if (borderTask == -1) {
         LogWarn("Failed to start timed border-checking task! This will prevent the plugin from working. Try restarting Bukkit.");
      }

      LogConfig("Border-checking timed task started.");
   }

   public static void StopBorderTimer() {
      if (borderTask != -1) {
         plugin.getServer().getScheduler().cancelTask(borderTask);
         borderTask = -1;
         LogConfig("Border-checking timed task stopped.");
      }
   }

   public static void StopFillTask() {
      if (fillTask != null && fillTask.valid()) {
         fillTask.cancel();
      }

   }

   public static void StoreFillTask() {
      save(false, true);
   }

   public static void UnStoreFillTask() {
      save(false);
   }

   public static void RestoreFillTask(String world, int fillDistance, int chunksPerRun, int tickFrequency, int x, int z, int length, int total) {
      fillTask = new WorldFillTask(plugin.getServer(), (Player)null, world, fillDistance, chunksPerRun, tickFrequency);
      if (fillTask.valid()) {
         fillTask.continueProgress(x, z, length, total);
         int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, fillTask, 20L, (long)tickFrequency);
         fillTask.setTaskID(task);
      }

   }

   public static void StopTrimTask() {
      if (trimTask != null && trimTask.valid()) {
         trimTask.cancel();
      }

   }

   public static int AvailableMemory() {
      return (int)((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1048576L);
   }

   public static boolean HasPermission(Player player, String request) {
      return HasPermission(player, request, true);
   }

   public static boolean HasPermission(Player player, String request, boolean notify) {
      if (player == null) {
         return true;
      } else if (player.hasPermission("worldborder." + request)) {
         return true;
      } else {
         if (notify) {
            player.sendMessage("You do not have sufficient permissions.");
         }

         return false;
      }
   }

   public static void Log(Level lvl, String text) {
      mcLog.log(lvl, String.format("[%s] %s", "WorldBorder", text));
   }

   public static void Log(String text) {
      Log(Level.INFO, text);
   }

   public static void LogWarn(String text) {
      Log(Level.WARNING, text);
   }

   public static void LogConfig(String text) {
      Log(Level.INFO, "[CONFIG] " + text);
   }

   public static void load(WorldBorder master, boolean logIt) {
      plugin = master;
      plugin.reloadConfig();
      cfg = plugin.getConfig();
      int cfgVersion = cfg.getInt("cfg-version", 7);
      message = cfg.getString("message");
      shapeRound = cfg.getBoolean("round-border", true);
      DEBUG = cfg.getBoolean("debug-mode", false);
      whooshEffect = cfg.getBoolean("whoosh-effect", false);
      portalRedirection = cfg.getBoolean("portal-redirection", true);
      knockBack = cfg.getDouble("knock-back-dist", (double)3.0F);
      timerTicks = cfg.getInt("timer-delay-ticks", 5);
      remountDelayTicks = cfg.getInt("remount-delay-ticks", 0);
      dynmapEnable = cfg.getBoolean("dynmap-border-enabled", true);
      dynmapMessage = cfg.getString("dynmap-border-message", "The border of the world.");
      LogConfig("Using " + ShapeName() + " border, knockback of " + knockBack + " blocks, and timer delay of " + timerTicks + ".");
      StartBorderTimer();
      borders.clear();
      if (message != null && !message.isEmpty()) {
         ConfigurationSection worlds = cfg.getConfigurationSection("worlds");
         if (worlds != null) {
            for(String worldName : worlds.getKeys(false)) {
               ConfigurationSection bord = worlds.getConfigurationSection(worldName);
               if (cfgVersion > 3) {
                  worldName = worldName.replace("<", ".");
               }

               if (bord.isSet("radius") && !bord.isSet("radiusX")) {
                  int radius = bord.getInt("radius");
                  bord.set("radiusX", radius);
                  bord.set("radiusZ", radius);
               }

               Boolean overrideShape = (Boolean)bord.get("shape-round");
               boolean wrap = bord.getBoolean("wrapping", false);
               BorderData border = new BorderData(bord.getDouble("x", (double)0.0F), bord.getDouble("z", (double)0.0F), bord.getInt("radiusX", 0), bord.getInt("radiusZ", 0), overrideShape, wrap);
               borders.put(worldName, border);
               LogConfig(BorderDescription(worldName));
            }
         }

         ConfigurationSection storedFillTask = cfg.getConfigurationSection("fillTask");
         if (storedFillTask != null) {
            String worldName = storedFillTask.getString("world");
            int fillDistance = storedFillTask.getInt("fillDistance", 176);
            int chunksPerRun = storedFillTask.getInt("chunksPerRun", 5);
            int tickFrequency = storedFillTask.getInt("tickFrequency", 20);
            int fillX = storedFillTask.getInt("x", 0);
            int fillZ = storedFillTask.getInt("z", 0);
            int fillLength = storedFillTask.getInt("length", 0);
            int fillTotal = storedFillTask.getInt("total", 0);
            RestoreFillTask(worldName, fillDistance, chunksPerRun, tickFrequency, fillX, fillZ, fillLength, fillTotal);
            save(false);
         }

         if (logIt) {
            LogConfig("Configuration loaded.");
         }

         if (cfgVersion < 7) {
            save(false);
         }

      } else {
         LogConfig("Configuration not present, creating new file.");
         message = "You have reached the edge of this world.";
         save(false);
      }
   }

   public static void save(boolean logIt) {
      save(logIt, false);
   }

   public static void save(boolean logIt, boolean storeFillTask) {
      if (cfg != null) {
         cfg.set("cfg-version", 7);
         cfg.set("message", message);
         cfg.set("round-border", shapeRound);
         cfg.set("debug-mode", DEBUG);
         cfg.set("whoosh-effect", whooshEffect);
         cfg.set("portal-redirection", portalRedirection);
         cfg.set("knock-back-dist", knockBack);
         cfg.set("timer-delay-ticks", timerTicks);
         cfg.set("remount-delay-ticks", remountDelayTicks);
         cfg.set("dynmap-border-enabled", dynmapEnable);
         cfg.set("dynmap-border-message", dynmapMessage);
         cfg.set("worlds", (Object)null);

         for(Map.Entry wdata : borders.entrySet()) {
            String name = ((String)wdata.getKey()).replace(".", "<");
            BorderData bord = (BorderData)wdata.getValue();
            cfg.set("worlds." + name + ".x", bord.getX());
            cfg.set("worlds." + name + ".z", bord.getZ());
            cfg.set("worlds." + name + ".radiusX", bord.getRadiusX());
            cfg.set("worlds." + name + ".radiusZ", bord.getRadiusZ());
            cfg.set("worlds." + name + ".wrapping", bord.getWrapping());
            if (bord.getShape() != null) {
               cfg.set("worlds." + name + ".shape-round", bord.getShape());
            }
         }

         if (storeFillTask && fillTask != null && fillTask.valid()) {
            cfg.set("fillTask.world", fillTask.refWorld());
            cfg.set("fillTask.fillDistance", fillTask.refFillDistance());
            cfg.set("fillTask.chunksPerRun", fillTask.refChunksPerRun());
            cfg.set("fillTask.tickFrequency", fillTask.refTickFrequency());
            cfg.set("fillTask.x", fillTask.refX());
            cfg.set("fillTask.z", fillTask.refZ());
            cfg.set("fillTask.length", fillTask.refLength());
            cfg.set("fillTask.total", fillTask.refTotal());
         } else {
            cfg.set("fillTask", (Object)null);
         }

         plugin.saveConfig();
         if (logIt) {
            LogConfig("Configuration saved.");
         }

      }
   }
}
