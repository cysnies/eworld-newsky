package basic;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import land.Land;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilScoreboard;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopManager;
import org.maxgamer.QuickShop.Shop.ShopPreCreateEvent;
import ticket.GroupChangeEvent;

public class Fix implements Listener {
   private static final UUID HP_UID = UUID.fromString("7ced486b-b87a-4952-9841-e14bdd18435d");
   private Random r = new Random();
   private Basic main;
   private String pn;
   private ShopManager sm;
   private int str1Interval;
   private int str2Interval;
   private String str1Per;
   private String str2Per;
   private int joinTime;
   private int levelTime;
   private HashMap levelItems;
   private HashMap moreHpHash;
   private List moreHpList;
   private HashList locks;
   private List prefix;
   private HashMap joinHash = new HashMap();
   private HashMap timeHash = new HashMap();
   private HashList canList = new HashListImpl();
   ProtocolManager pm;

   public Fix(Basic basic) {
      this.main = basic;
      this.pn = Basic.getPn();
      this.sm = ((QuickShop)Bukkit.getPluginManager().getPlugin("QuickShop")).getShopManager();
      this.pm = ProtocolLibrary.getProtocolManager();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, basic);
      this.pm.addPacketListener(new PacketAdapter(basic, ConnectionSide.SERVER_SIDE, new Integer[]{3}) {
         public void onPacketSending(PacketEvent event) {
            if (Fix.this.joinHash.containsKey(event.getPlayer())) {
               long now = System.currentTimeMillis();
               if (now - (Long)Fix.this.joinHash.get(event.getPlayer()) > (long)(Fix.this.joinTime * 1000)) {
                  Fix.this.joinHash.remove(event.getPlayer());
               } else {
                  event.setCancelled(true);
               }
            }

         }
      });
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      try {
         String line0 = e.getLines()[0];
         if (line0 != null) {
            line0 = line0.toLowerCase();
            if (!line0.isEmpty() && this.locks.has(line0)) {
               HashList<Block> checkList = new HashListImpl();
               if (e.getBlock().getType().equals(Material.SIGN_POST)) {
                  checkList.add(e.getBlock().getRelative(BlockFace.EAST));
                  checkList.add(e.getBlock().getRelative(BlockFace.SOUTH));
                  checkList.add(e.getBlock().getRelative(BlockFace.WEST));
                  checkList.add(e.getBlock().getRelative(BlockFace.NORTH));
               } else if (e.getBlock().getType().equals(Material.WALL_SIGN)) {
                  byte data = e.getBlock().getData();
                  Sign sign = new Sign(Material.WALL_SIGN, data);
                  checkList.add(e.getBlock().getRelative(sign.getFacing().getOppositeFace()));
               }

               for(Block b : checkList) {
                  Shop shop = this.sm.getShop(b.getLocation());
                  if (shop != null && !shop.getOwner().equals(e.getPlayer().getName())) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
                     e.setCancelled(true);
                     return;
                  }
               }
            }
         }
      } catch (Exception var7) {
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSignChangeMonitor(SignChangeEvent e) {
      for(int i = 0; i < 4; ++i) {
         String result = Util.convert(e.getLine(i));
         e.setLine(i, result.substring(0, Math.min(15, result.length())));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      Player p = e.getPlayer();
      if (UtilPer.inGroup(p, "vip")) {
         e.setMessage("§6" + e.getMessage());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onShopPreCreate(ShopPreCreateEvent e) {
      Player p = e.getPlayer();
      Location l = e.getLocation();
      Land land = LandMain.getLandManager().getHighestPriorityLand(l);
      if (land == null || !land.getOwner().equals(p.getName())) {
         e.setCancelled(true);
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      this.checkPrefix(p, this.canList.has(p.getName()));
      this.joinHash.put(p, System.currentTimeMillis());
      this.sendMsg(p, this.get(550));

      for(String per : this.moreHpList) {
         if (UtilPer.hasPer(p, per)) {
            int add = (Integer)this.moreHpHash.get(per);
            this.setMoreHp(p, add);
            return;
         }
      }

      this.setMoreHp(p, 0);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      this.joinHash.remove(p);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerDeath(PlayerDeathEvent e) {
      Player p = e.getEntity();
      Player killer = p.getKiller();
      if (killer != null && killer.isOnline() && this.canList.has(p.getName())) {
         this.canList.remove(p.getName());
         this.timeHash.put(p.getName(), 0);
         this.checkPrefix(p, false);
         Bukkit.getScheduler().scheduleSyncDelayedTask(this.main, new LevelAdd(killer, p.getEyeLocation()));
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onGroupChange(GroupChangeEvent e) {
      this.checkPrefix(e.getP(), this.canList.has(e.getP().getName()));
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock().getType().equals(Material.SOIL)) {
            e.setCancelled(true);
         }
      } catch (Exception var3) {
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.str1Interval == 0L) {
         Player[] var5;
         for(Player p : var5 = Bukkit.getOnlinePlayers()) {
            if (UtilPer.hasPer(p, this.str1Per) && !p.isDead() && p.getFoodLevel() < 20) {
               p.setFoodLevel(p.getFoodLevel() + 1);
            }
         }
      }

      if (TimeEvent.getTime() % (long)this.str2Interval == 0L) {
         Player[] var14;
         for(Player p : var14 = Bukkit.getOnlinePlayers()) {
            if (UtilPer.hasPer(p, this.str2Per) && !p.isDead() && p.getHealth() < p.getMaxHealth()) {
               try {
                  p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + (double)1.0F));
               } catch (Exception var7) {
               }
            }
         }
      }

      if (TimeEvent.getTime() % 60L == 0L) {
         Player[] var15;
         for(Player p : var15 = Bukkit.getOnlinePlayers()) {
            if (!this.timeHash.containsKey(p.getName())) {
               this.timeHash.put(p.getName(), 0);
            }

            int result = (Integer)this.timeHash.get(p.getName()) + 1;
            if (result >= this.levelTime) {
               result = 0;
               this.setLevelAll(p, true);
            }

            this.timeHash.put(p.getName(), result);
         }
      }

   }

   private void setMoreHp(Player p, int add) {
      AttributeInstance ai = ((CraftPlayer)p).getHandle().getAttributeInstance(GenericAttributes.a);
      AttributeModifier am = new AttributeModifier(HP_UID, "moreHp", (double)add, 0);
      ai.b(am);
      ai.a(am);
   }

   private void sendMsg(Player p, String msg) {
      try {
         PacketContainer pc = new PacketContainer(3);
         pc.getStrings().write(0, this.getJsonMsg(msg));
         this.pm.sendServerPacket(p, pc, false);
      } catch (InvocationTargetException var4) {
      }

   }

   private String getJsonMsg(String s) {
      return "{\"text\":\"" + s + "\"}";
   }

   private void setLevelAll(Player p, boolean can) {
      if (can) {
         if (!this.canList.has(p.getName())) {
            this.canList.add(p.getName());
            this.checkPrefix(p, true);
            p.sendMessage(this.get(570));
            p.sendMessage(this.get(570));
            p.sendMessage(this.get(570));
         }
      } else if (this.canList.has(p.getName())) {
         this.canList.remove(p.getName());
         this.checkPrefix(p, false);
      }

   }

   private void checkPrefix(Player p, boolean can) {
      String result;
      if (can) {
         result = this.get(560);
      } else {
         result = "";
      }

      for(String s : this.prefix) {
         String group = s.split(" ")[0];
         if (UtilPer.inGroup(p, group)) {
            result = s.split(" ")[1] + result;
            UtilScoreboard.setPrefix(p, result);
            return;
         }
      }

      UtilScoreboard.setPrefix(p, result);
   }

   private void loadConfig(YamlConfiguration config) {
      this.locks = new HashListImpl();

      for(String s : config.getStringList("fix.locks")) {
         this.locks.add(s);
      }

      this.prefix = new ArrayList();

      for(String s : config.getStringList("prefix")) {
         this.prefix.add(Util.convert(s));
      }

      this.str1Interval = config.getInt("str1.interval");
      this.str1Per = config.getString("str1.per");
      this.str2Interval = config.getInt("str2.interval");
      this.str2Per = config.getString("str2.per");
      this.joinTime = config.getInt("joinTime");
      this.levelTime = config.getInt("levelTime");
      this.levelItems = new HashMap();

      for(String s : config.getStringList("levelItems")) {
         int id = Integer.parseInt(s.split(" ")[1]);
         int chance = Integer.parseInt(s.split(" ")[0]);
         this.levelItems.put(id, chance);
      }

      this.moreHpHash = new HashMap();
      this.moreHpList = new ArrayList();

      for(String s : config.getStringList("moreHp")) {
         String per = s.split(" ")[0];
         int hp = Integer.parseInt(s.split(" ")[1]);
         this.moreHpList.add(per);
         this.moreHpHash.put(per, hp);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class LevelAdd implements Runnable {
      private Player p;
      private Location l;

      public LevelAdd(Player p, Location l) {
         this.p = p;
         this.l = l;
      }

      public void run() {
         if (this.p != null && this.p.isOnline() && !this.p.isDead()) {
            World w = this.l.getWorld();

            for(int id : Fix.this.levelItems.keySet()) {
               int chance = (Integer)Fix.this.levelItems.get(id);
               if (Fix.this.r.nextInt(1000) < chance) {
                  ItemStack is = new ItemStack(id, 1);
                  w.dropItemNaturally(this.l, is);
               }
            }

            this.p.setLevel(this.p.getLevel() + 1);
            this.p.sendMessage(Fix.this.get(565));

            try {
               this.p.getWorld().playSound(this.p.getLocation(), Sound.valueOf("LEVEL_UP"), 2.0F, 1.0F);
            } catch (Exception var6) {
            }
         }

      }
   }
}
