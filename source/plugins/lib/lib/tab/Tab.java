package lib.tab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lib.Lib;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Tab implements Listener {
   private static Pattern pattern = Pattern.compile("<size:\\w+?>");
   private static Lib lib;
   private String pn;
   private static Roll roll = new Roll((Roll)null);
   private static boolean tabEnable;
   private static int speed = 20;
   private static HashList hasParamList;
   private static HashMap fixHash;
   private static HashMap infoHash;
   private static HashMap modeHash = new HashMap();
   private static int totalTicks;
   private static int intervalTick = 1;
   private static int tickAmount = 1;
   private static Update update = new Update((Update)null);
   private static List updatelist = new ArrayList();

   public Tab(Lib lib) {
      Tab.lib = lib;
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, lib);
      next();
      Bukkit.getScheduler().scheduleSyncRepeatingTask(lib, update, 1L, 1L);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public static void register(String mode) {
      modeHash.put(mode, new HashMap());
   }

   public static HashMap getMode(String mode) {
      return (HashMap)modeHash.get(mode);
   }

   public static boolean isTabEnable() {
      return tabEnable;
   }

   private static Mode getMode(String name, Player p) {
      HashMap<Player, Mode> hash = (HashMap)modeHash.get(name);
      return hash == null ? null : (Mode)hash.get(p);
   }

   private static int getModeSize(Player p, String name) {
      HashMap<Player, Mode> hash = (HashMap)modeHash.get(name);
      if (hash == null) {
         return 0;
      } else {
         Mode mode = (Mode)hash.get(p);
         return mode == null ? 0 : mode.size();
      }
   }

   private static boolean hasParam(String msg) {
      if (!msg.contains("<name>") && !msg.contains("<display>") && !msg.contains("<list>") && !msg.contains("<health>") && !msg.contains("<maxHealth>") && !msg.contains("<food>") && !msg.contains("<level>") && !msg.contains("<exp>")) {
         Matcher m = pattern.matcher(msg);
         return m.find();
      } else {
         return true;
      }
   }

   private static String getMsg(Player p, Pos pos, String msg) {
      if (hasParamList.has(pos)) {
         msg = msg.replace("<name>", p.getName());
         msg = msg.replace("<display>", p.getDisplayName());
         msg = msg.replace("<list>", p.getPlayerListName());
         msg = msg.replace("<health>", String.valueOf((int)p.getHealth()));
         msg = msg.replace("<maxHealth>", String.valueOf((int)p.getMaxHealth()));
         msg = msg.replace("<food>", String.valueOf(p.getFoodLevel()));
         msg = msg.replace("<level>", String.valueOf(p.getLevel()));
         msg = msg.replace("<exp>", String.valueOf(Util.getTotalExperience(p)));

         String s;
         int size;
         for(Matcher m = pattern.matcher(msg); m.find(); msg = msg.replace(s, String.valueOf(size))) {
            s = m.group();
            String name = s.substring(6, s.length() - 1);
            size = getModeSize(p, name);
         }
      }

      return msg;
   }

   private static void next() {
      Bukkit.getScheduler().scheduleSyncDelayedTask(lib, roll, (long)speed);
   }

   private void loadConfig(YamlConfiguration config) {
      tabEnable = config.getBoolean("tabEnable");
      speed = config.getInt("tab.speed");
      hasParamList = new HashListImpl();
      fixHash = new HashMap();

      for(String s : config.getStringList("tab.show.fix")) {
         String[] ss = s.split(" ");
         int x = Integer.parseInt(ss[0].split("-")[1]);
         int y = Integer.parseInt(ss[0].split("-")[0]);
         Pos pos = new Pos(x, y);
         String msg = Util.convert(Util.combine(ss, " ", 1, ss.length));
         fixHash.put(pos, msg);
         if (hasParam(msg)) {
            hasParamList.add(pos);
         }
      }

      infoHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("tab.show.mode");

      for(String key : ms.getValues(false).keySet()) {
         int roll = ms.getInt(key + ".roll");
         int space = ms.getInt(key + ".space");
         HashMap<Integer, Pos> posHash = new HashMap();
         int index = 0;

         for(String s : ms.getStringList(key + ".pos")) {
            int x = Integer.parseInt(s.split("-")[1]);
            int y = Integer.parseInt(s.split("-")[0]);
            Pos pos = new Pos(x, y);
            posHash.put(index++, pos);
         }

         Info info = new Info(roll, space, posHash);
         infoHash.put(key, info);
      }

   }

   public static class Pos {
      private int x;
      private int y;

      public Pos(int x, int y) {
         this.x = x;
         this.y = y;
      }

      public int getX() {
         return this.x;
      }

      public void setX(int x) {
         this.x = x;
      }

      public int getY() {
         return this.y;
      }

      public void setY(int y) {
         this.y = y;
      }

      public int hashCode() {
         return this.x + this.y;
      }

      public boolean equals(Object obj) {
         Pos pos = (Pos)obj;
         return pos.x == this.x && pos.y == this.y;
      }
   }

   public static class Info {
      private int roll;
      private int space;
      private HashMap posHash;

      public Info(int roll, int space, HashMap posHash) {
         this.roll = roll;
         this.space = space;
         this.posHash = posHash;
      }

      public int getRoll() {
         return this.roll;
      }

      public void setRoll(int roll) {
         this.roll = roll;
      }

      public int getSpace() {
         return this.space;
      }

      public void setSpace(int space) {
         this.space = space;
      }

      public HashMap getPosHash() {
         return this.posHash;
      }

      public void setPosHash(HashMap posHash) {
         this.posHash = posHash;
      }

      public int size() {
         return this.posHash.size();
      }
   }

   public static class Mode {
      private HashMap showHash = new HashMap();
      private HashMap posHash = new HashMap();
      private List showList = new ArrayList();
      private int current;
      private int dir = 1;

      public void add(String name, String show) {
         this.add(name, show, this.showList.size());
      }

      public void add(String name, String show, int pos) {
         if (pos < 0) {
            pos = 0;
         } else if (pos > this.showList.size()) {
            pos = this.showList.size();
         }

         this.showHash.put(name, show);
         this.posHash.put(name, pos);
         this.showList.add(pos, name);

         for(int index = pos + 1; index < this.showList.size(); ++index) {
            String s = (String)this.showList.get(index);
            this.posHash.put(s, index);
         }

      }

      public int size() {
         return this.showList.size();
      }

      public String getShow(String name) {
         return (String)this.showHash.get(name);
      }

      public String getShow(int index) {
         if (index >= 0 && index < this.showList.size()) {
            String name = (String)this.showList.get(index);
            return name == null ? null : this.getShow(name);
         } else {
            return null;
         }
      }

      public int getPos(String name) {
         return this.posHash.containsKey(name) ? (Integer)this.posHash.get(name) : -1;
      }

      public boolean remove(int index) {
         if (index >= 0 && index < this.showList.size()) {
            String name = (String)this.showList.remove(index);
            this.showHash.remove(name);
            this.posHash.remove(name);

            for(int i = index; i < this.showList.size(); ++i) {
               String s = (String)this.showList.get(i);
               this.posHash.put(s, i);
            }

            return true;
         } else {
            return false;
         }
      }

      public boolean set(String name, String show) {
         if (!this.showHash.containsKey(name)) {
            return false;
         } else {
            this.showHash.put(name, show);
            return true;
         }
      }

      public int getCurrent() {
         return this.current;
      }

      public void setCurrent(int current) {
         this.current = current;
      }

      public int getDir() {
         return this.dir;
      }

      public void setDir(int dir) {
         this.dir = dir;
      }
   }

   private static class Roll implements Runnable {
      private Roll() {
      }

      public void run() {
         this.roll();
         Tab.next();
      }

      private void roll() {
         int online = Bukkit.getOnlinePlayers().length;
         if (online <= 1) {
            online = 1;
         }

         Tab.intervalTick = Tab.speed / online;
         if (Tab.intervalTick < 1) {
            Tab.intervalTick = 1;
         }

         Tab.tickAmount = online / Tab.speed;
         if (Tab.tickAmount < 1) {
            Tab.tickAmount = 1;
         }

         Tab.update.flush();

         Player[] var5;
         for(Player p : var5 = Bukkit.getOnlinePlayers()) {
            this.roll(p);
            Tab.updatelist.add(p);
         }

      }

      private void roll(Player p) {
         TabAPI.clearTabInfo(p);

         for(Pos pos : Tab.fixHash.keySet()) {
            String msg = (String)Tab.fixHash.get(pos);
            msg = Tab.getMsg(p, pos, msg);
            TabAPI.setTabString(Tab.lib, p, pos.x, pos.y, msg);
         }

         for(String name : Tab.infoHash.keySet()) {
            Info info = (Info)Tab.infoHash.get(name);
            Mode mode = Tab.getMode(name, p);
            if (mode != null) {
               if (info.getRoll() == 0) {
                  if (mode.getDir() == 1) {
                     int dif = mode.getCurrent() + info.size() - mode.size();
                     if (dif > 0) {
                        mode.setCurrent(Math.max(0, mode.size() - info.size()));
                        mode.setDir(-1);
                     } else if (dif == 0) {
                        mode.setDir(-1);
                     }
                  } else if (mode.getCurrent() < 0) {
                     mode.setCurrent(0);
                     mode.setDir(1);
                  } else if (mode.getCurrent() == 0) {
                     mode.setDir(1);
                  }

                  int tar = mode.getCurrent() + mode.getDir();
                  if (tar >= 0 && tar <= mode.size() - info.size()) {
                     mode.setCurrent(tar);
                  }

                  int size = info.size();

                  for(int index = 0; index < size; ++index) {
                     Pos pos = (Pos)info.getPosHash().get(index);
                     String show = mode.getShow(index + mode.getCurrent());
                     if (show != null && !show.isEmpty()) {
                        TabAPI.setTabString(Tab.lib, p, pos.x, pos.y, show);
                     }
                  }
               } else if (info.getRoll() == 1) {
                  if (mode.size() < info.size()) {
                     mode.setCurrent(0);
                  } else if (mode.getCurrent() >= mode.size()) {
                     mode.setCurrent(mode.size() - info.size());
                  } else {
                     mode.setCurrent(mode.getCurrent() + 1);
                  }

                  int size = info.size();

                  for(int index = 0; index < size; ++index) {
                     int tar = index + mode.getCurrent();
                     Pos pos = (Pos)info.getPosHash().get(index);
                     String show;
                     if (tar < mode.size()) {
                        show = mode.getShow(tar);
                     } else if (tar < mode.size() + info.getSpace()) {
                        show = null;
                     } else if (mode.size() + info.getSpace() >= info.size()) {
                        show = mode.getShow(tar - mode.size() - info.getSpace());
                     } else {
                        show = null;
                     }

                     if (show != null && !show.isEmpty()) {
                        TabAPI.setTabString(Tab.lib, p, pos.x, pos.y, show);
                     }
                  }
               }
            }
         }

      }

      // $FF: synthetic method
      Roll(Roll var1) {
         this();
      }
   }

   private static class Update implements Runnable {
      private Update() {
      }

      public void run() {
         try {
            Tab.totalTicks = Tab.totalTicks + 1;
            if (Tab.totalTicks % Tab.intervalTick == 0) {
               for(int i = 0; i < Tab.tickAmount; ++i) {
                  if (!Tab.updatelist.isEmpty()) {
                     Player p = (Player)Tab.updatelist.remove(0);
                     if (p != null && p.isOnline()) {
                        TabAPI.updatePlayer(p);
                     }
                  }
               }
            }
         } catch (Exception var3) {
         }

      }

      public void flush() {
         for(Player p : Tab.updatelist) {
            if (p != null && p.isOnline()) {
               TabAPI.updatePlayer(p);
            }
         }

         Tab.updatelist.clear();
      }

      // $FF: synthetic method
      Update(Update var1) {
         this();
      }
   }
}
