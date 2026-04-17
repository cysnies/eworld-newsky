package lib;

import java.util.Random;
import lib.barapi.BarAPI;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Bar implements Listener {
   private static Random r = new Random();
   private static Lib lib;
   private String pn;
   private static Roll roll = new Roll((Roll)null);
   private static final String COLOR = "§";
   private static final String colors = "abcdef0123456789";
   private static final String formats = "lmnok";
   private static boolean enable;
   private static boolean move = true;
   private static int speed = 20;
   private static int length = 32;
   private static String space = "";
   private static HashList showList = new HashListImpl();
   private static String show = " ";
   private static int current;
   private static String color;

   public Bar(Lib lib) {
      Bar.lib = lib;
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, lib);
      next();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public static boolean isEnable() {
      return enable;
   }

   public static void addMsg(String msg) {
      if (msg != null) {
         showList.add(msg);
      }

   }

   public static void addMsgRandom(String msg) {
      if (msg != null) {
         showList.add(msg, r.nextInt(showList.size() + 1));
      }

   }

   public static void removeMsg(int index) {
      try {
         showList.remove(index);
      } catch (Exception var2) {
      }

   }

   public static void removeMsg(String msg) {
      try {
         showList.remove(msg);
      } catch (Exception var2) {
      }

   }

   public static void clearMsg() {
      showList.clear();
      show = " ";
      current = 0;

      Player[] var3;
      for(Player p : var3 = Bukkit.getOnlinePlayers()) {
         if (BarAPI.hasBar(p)) {
            BarAPI.removeBar(p);
         }
      }

   }

   private static void next() {
      Bukkit.getScheduler().scheduleSyncDelayedTask(lib, roll, (long)speed);
   }

   private static void showMsg(String msg, int percent) {
      if (msg != null && !msg.isEmpty()) {
         if (percent < 1) {
            percent = 1;
         }

         if (percent > 100) {
            percent = 100;
         }

         msg = color + msg;

         Player[] var10;
         for(Player p : var10 = Bukkit.getOnlinePlayers()) {
            if (move) {
               BarAPI.setMessage(p, msg, (float)percent);
            } else {
               BarAPI.setMessage(p, msg);
            }
         }

      } else {
         Player[] var5;
         for(Player p : var5 = Bukkit.getOnlinePlayers()) {
            if (BarAPI.hasBar(p)) {
               BarAPI.removeBar(p);
            }
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      enable = config.getBoolean("bar.enable");
      move = config.getBoolean("bar.move");
      speed = config.getInt("bar.speed");
      length = config.getInt("bar.length");
      if (length < 1) {
         length = 1;
      }

      if (length > 64) {
         length = 64;
      }

      for(int i = 0; i < config.getInt("bar.space"); ++i) {
         space = space + " ";
      }

   }

   private static class Roll implements Runnable {
      private Roll() {
      }

      public void run() {
         this.roll();
         Bar.next();
      }

      private void roll() {
         this.checkGetNext();
         String result = "";
         if (!Bar.show.isEmpty()) {
            if (Bar.show.length() <= Bar.length) {
               result = Bar.show;
            } else {
               if (this.checkColor()) {
                  Bar.show = Bar.show.substring(2);
               } else {
                  Bar.show = Bar.show.substring(1);
               }

               result = Bar.show.substring(0, Math.min(Bar.length, Bar.show.length()));
            }
         }

         if (result.isEmpty()) {
            Bar.clearMsg();
         } else {
            Bar.showMsg(result, this.getPercent());
         }

      }

      private boolean checkColor() {
         if (Bar.show.length() >= 2 && Bar.show.substring(0, 1).equals("§")) {
            char c = Bar.show.charAt(1);
            if ("abcdef0123456789".indexOf(c) != -1) {
               Bar.color = "§" + c;
               return true;
            }

            if ("lmnok".indexOf(c) != -1) {
               Bar.color = Bar.color + "§" + c;
               return true;
            }
         }

         return false;
      }

      private int getPercent() {
         try {
            return 100 * Bar.current / Bar.showList.size();
         } catch (Exception var2) {
            return 100;
         }
      }

      private void checkGetNext() {
         if (Bar.show == null) {
            Bar.show = " ";
         }

         if (Bar.show.length() <= Bar.length) {
            this.getNext();
         }

      }

      private void getNext() {
         if (!Bar.showList.isEmpty()) {
            Bar.current = Bar.current + 1;
            if (Bar.current < 0 || Bar.current >= Bar.showList.size()) {
               Bar.current = 0;
            }

            String add = (String)Bar.showList.get(Bar.current);
            Bar.show = Bar.show + Bar.space + add;
         }

      }

      // $FF: synthetic method
      Roll(Roll var1) {
         this();
      }
   }
}
