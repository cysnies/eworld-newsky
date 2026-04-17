package lib.tab;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import lib.Lib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class TabAPI implements Listener {
   private static HashMap playerTab = new HashMap();
   private static HashMap playerTabLast = new HashMap();
   private static HashMap cachedPackets = new HashMap();
   private static HashMap updateSchedules = new HashMap();
   private static int horzTabSize = 3;
   private static int vertTabSize = 20;
   private static String[] colors = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
   private static int e = 0;
   private static int r = 0;
   private static long flickerPrevention = 5L;
   private static ProtocolManager protocolManager;
   private static boolean shuttingdown = false;
   private static Lib lib;

   public void onEnable(Lib lib) {
      TabAPI.lib = lib;
      protocolManager = ProtocolLibrary.getProtocolManager();
      Bukkit.getServer().getPluginManager().registerEvents(this, lib);

      Player[] var5;
      for(Player p : var5 = Bukkit.getOnlinePlayers()) {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("TabAPI");
         setPriority(plugin, p, 2);
         resetTabList(p);
         setPriority(plugin, p, -2);
      }

      protocolManager.addPacketListener(new PacketAdapter(lib, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, new Integer[]{201}) {
         public void onPacketSending(PacketEvent event) {
            if (Tab.isTabEnable()) {
               switch (event.getPacketID()) {
                  case 201:
                     PacketContainer p = event.getPacket();
                     String s = (String)p.getStrings().read(0);
                     if (s.startsWith("$")) {
                        p.getStrings().write(0, s.substring(1));
                        event.setPacket(p);
                     } else {
                        event.setCancelled(true);
                     }
                  default:
               }
            }
         }
      });
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLeave(PlayerQuitEvent e) {
      playerTab.remove(e.getPlayer().getName());
      playerTabLast.remove(e.getPlayer().getName());
   }

   public static void setPriority(Plugin plugin, Player player, int pri) {
      getTab(player).setPriority(plugin, pri);
   }

   public static void disableTabForPlayer(Player p) {
      playerTab.put(p.getName(), (Object)null);
      resetTabList(p);
   }

   public static void resetTabList(Player p) {
      int a = 0;
      int b = 0;

      Player[] var6;
      for(Player pl : var6 = Bukkit.getOnlinePlayers()) {
         setTabString(Bukkit.getPluginManager().getPlugin("TabAPI"), p, a, b, pl.getPlayerListName());
         ++b;
         if (b > horzTabSize) {
            b = 0;
            ++a;
         }
      }

   }

   public static void setTabString(Plugin plugin, Player p, int x, int y, String msg) {
      setTabString(plugin, p, x, y, msg, 0);
   }

   public static void setTabString(Plugin plugin, Player p, int x, int y, String msg, int ping) {
      try {
         TabObject tabo = getTab(p);
         tabo.setTab(plugin, x, y, msg, ping);
      } catch (Exception var7) {
      }

   }

   public static void updatePlayer(Player p) {
      if (p.isOnline()) {
         r = 0;
         e = 0;
         TabObject tabo = (TabObject)playerTab.get(p.getName());
         TabHolder tab = tabo.getTab();
         if (tab != null) {
            clearTab(p);

            for(int b = 0; b < tab.maxv; ++b) {
               for(int a = 0; a < tab.maxh; ++a) {
                  if (tab.tabs[a][b] == null) {
                     tab.tabs[a][b] = nextNull();
                  }

                  String msg = tab.tabs[a][b];
                  int ping = tab.tabPings[a][b];
                  addPacket(p, msg == null ? " " : msg.substring(0, Math.min(msg.length(), 16)), true, ping);
               }
            }

            flushPackets(p, tab.getCopy());
         }
      }
   }

   public static void clearTab(Player p) {
      if (p.isOnline()) {
         TabHolder tabold = (TabHolder)playerTabLast.get(p.getName());
         if (tabold != null) {
            String[][] var5;
            for(String[] s : var5 = tabold.tabs) {
               for(String msg : s) {
                  if (msg != null) {
                     addPacket(p, msg.substring(0, Math.min(msg.length(), 16)), false, 0);
                  }
               }
            }
         }

      }
   }

   public static void clearTabInfo(Player p) {
      if (p.isOnline()) {
         TabObject tabo = new TabObject();
         playerTab.put(p.getName(), tabo);
      }
   }

   public static String nextNull() {
      String s = "";

      for(int a = 0; a < r; ++a) {
         s = " " + s;
      }

      s = s + "§" + colors[e];
      ++e;
      if (e > 14) {
         e = 0;
         ++r;
      }

      return s;
   }

   public static int getVertSize() {
      return vertTabSize;
   }

   public static int getHorizSize() {
      return horzTabSize;
   }

   private static void addPacket(Player p, String msg, boolean b, int ping) {
      PacketContainer message = protocolManager.createPacket(201);
      message.getStrings().write(0, (!shuttingdown ? "$" : "") + msg);
      message.getBooleans().write(0, b);
      message.getIntegers().write(0, ping);
      ArrayList<PacketContainer> packetList = (ArrayList)cachedPackets.get(p);
      if (packetList == null) {
         packetList = new ArrayList();
         cachedPackets.put(p, packetList);
      }

      packetList.add(message);
   }

   private static void flushPackets(final Player p, final TabHolder tabCopy) {
      final PacketContainer[] packets = (PacketContainer[])((ArrayList)cachedPackets.get(p)).toArray(new PacketContainer[0]);
      Integer taskID = (Integer)updateSchedules.get(p);
      if (taskID != null) {
         Bukkit.getScheduler().cancelTask(taskID);
      }

      taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(lib, new Runnable() {
         public void run() {
            if (p.isOnline()) {
               PacketContainer[] var4;
               for(PacketContainer packet : var4 = packets) {
                  try {
                     if (Tab.isTabEnable()) {
                        TabAPI.protocolManager.sendServerPacket(p, packet);
                     }
                  } catch (InvocationTargetException e) {
                     e.printStackTrace();
                     System.out.println("[TabAPI] Error sending packet to client");
                  }
               }
            }

            if (tabCopy != null) {
               TabAPI.playerTabLast.put(p.getName(), tabCopy);
            }

            TabAPI.updateSchedules.remove(p);
         }
      }, flickerPrevention);
      updateSchedules.put(p, taskID);
      cachedPackets.remove(p);
   }

   private static TabObject getTab(Player p) {
      TabObject tabo = (TabObject)playerTab.get(p.getName());
      if (tabo == null) {
         tabo = new TabObject();
         playerTab.put(p.getName(), tabo);
      }

      return tabo;
   }
}
