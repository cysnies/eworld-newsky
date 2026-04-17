package fr.neatmonster.nocheatplus.logging;

import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class LogUtil {
   public static String toString(Throwable t) {
      StringWriter w = new StringWriter();
      t.printStackTrace(new PrintWriter(w));
      return w.toString();
   }

   public static void logInfo(String msg) {
      log(Level.INFO, msg);
   }

   public static void logWarning(String msg) {
      log(Level.WARNING, msg);
   }

   public static void logSevere(String msg) {
      log(Level.SEVERE, msg);
   }

   public static void logInfo(Throwable t) {
      log(Level.INFO, toString(t));
   }

   public static void logWarning(Throwable t) {
      log(Level.WARNING, toString(t));
   }

   public static void logSevere(Throwable t) {
      log(Level.SEVERE, toString(t));
   }

   public static void log(Level level, String msg) {
      Bukkit.getLogger().log(level, msg);
   }

   public static boolean scheduleLogInfo(String message) {
      return scheduleLog(Level.INFO, message);
   }

   public static boolean scheduleLogWarning(String message) {
      return scheduleLog(Level.WARNING, message);
   }

   public static boolean scheduleLogSevere(String message) {
      return scheduleLog(Level.SEVERE, message);
   }

   public static boolean scheduleLogInfo(Throwable t) {
      return scheduleLog(Level.INFO, toString(t));
   }

   public static boolean scheduleLogWarning(Throwable t) {
      return scheduleLog(Level.WARNING, toString(t));
   }

   public static boolean scheduleLogSevere(Throwable t) {
      return scheduleLog(Level.SEVERE, toString(t));
   }

   public static boolean scheduleLog(final Level level, final String message) {
      try {
         return Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("NoCheatPlus"), new Runnable() {
            public final void run() {
               Bukkit.getLogger().log(level, message);
            }
         }) != -1;
      } catch (Exception var3) {
         return false;
      }
   }

   public static boolean scheduleLogInfo(List parts, String link) {
      return scheduleLog(Level.INFO, parts, link);
   }

   public static boolean scheduleLog(Level level, List parts, String link) {
      return scheduleLog(level, StringUtil.join(parts, link));
   }
}
