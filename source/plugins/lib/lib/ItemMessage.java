package lib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMessage implements Listener {
   private ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
   private ItemFactory itemFactory;
   private String pn;
   private ItemStack is;
   private boolean ignoreTipEmpty;
   private int messageItem;
   private HashList ignoreTipItems;
   private HashMap infoHash;

   public ItemMessage(Lib lib) {
      this.itemFactory = lib.getServer().getItemFactory();
      this.pn = lib.getPn();
      this.infoHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      lib.getServer().getPluginManager().registerEvents(this, lib);
      this.protocolManager.addPacketListener(new PacketAdapter(lib, ConnectionSide.SERVER_SIDE, ListenerPriority.LOWEST, new Integer[]{103}) {
         public void onPacketSending(PacketEvent e) {
            try {
               if (ItemMessage.this.infoHash.containsKey(e.getPlayer()) && e.getPlayer().getInventory().getHeldItemSlot() + 36 == (Integer)e.getPacket().getIntegers().read(1)) {
                  ItemStack is = (ItemStack)e.getPacket().getItemModifier().read(0);
                  if (is.hasItemMeta() && is.getItemMeta().getDisplayName() != null) {
                     Info info = (Info)ItemMessage.this.infoHash.get(e.getPlayer());
                     if (is.getItemMeta().getDisplayName().equals(info.getMsg() + info.getColor())) {
                        return;
                     }
                  }

                  e.setCancelled(true);
               }
            } catch (Exception var4) {
            }

         }
      });
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.infoHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerItemHeld(PlayerItemHeldEvent e) {
      Info info = (Info)this.infoHash.get(e.getPlayer());
      if (info != null) {
         int slot = e.getPreviousSlot();
         ItemStack is = e.getPlayer().getInventory().getItem(slot);
         this.send(e.getPlayer(), this.getSendItemSlotChangePacket(slot, is));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      Iterator<Player> it = this.infoHash.keySet().iterator();

      while(it.hasNext()) {
         Player p = (Player)it.next();
         if (p != null && p.isOnline()) {
            Info info = (Info)this.infoHash.get(p);
            if (info.getLeft() <= 1) {
               it.remove();
               if (p.getItemInHand() == null || !this.ignoreTipItems.has(p.getItemInHand().getTypeId())) {
                  int slot = p.getInventory().getHeldItemSlot();
                  this.send(p, this.getSendItemSlotChangePacket(slot, p.getInventory().getItem(slot)));
               }
            } else {
               info.setLeft(info.getLeft() - 1);
            }
         } else {
            it.remove();
         }
      }

      for(Player p : this.infoHash.keySet()) {
         if (p.getItemInHand() == null || !this.ignoreTipItems.has(p.getItemInHand().getTypeId())) {
            Info info = (Info)this.infoHash.get(p);
            int slot = p.getInventory().getHeldItemSlot();
            if (this.ignoreTipEmpty) {
               ItemStack is0 = p.getInventory().getItem(slot);
               if (is0 == null || is0.getTypeId() == 0) {
                  continue;
               }
            }

            ItemStack is = this.makeStack(p, slot, info.getMsg() + info.getNextColor());
            this.send(p, this.getSendItemSlotChangePacket(slot, is));
         }
      }

   }

   public void sendItemMessage(Player p, String msg, int dur) {
      if (p != null && p.isOnline() && msg != null && !msg.trim().isEmpty()) {
         Info info = new Info(dur, msg);
         this.infoHash.put(p, info);
         if (p.getItemInHand() == null || !this.ignoreTipItems.has(p.getItemInHand().getTypeId())) {
            int slot = p.getInventory().getHeldItemSlot();
            if (this.ignoreTipEmpty) {
               ItemStack is0 = p.getInventory().getItem(slot);
               if (is0 == null || is0.getTypeId() == 0) {
                  return;
               }
            }

            ItemStack is = this.makeStack(p, slot, msg + info.getNextColor());
            this.send(p, this.getSendItemSlotChangePacket(slot, is));
         }
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.ignoreTipEmpty = config.getBoolean("itemMessage.ignoreTipEmpty");
      this.messageItem = config.getInt("itemMessage.messageItem");
      this.ignoreTipItems = new HashListImpl();

      for(int id : config.getIntegerList("itemMessage.ignoreTipItems")) {
         this.ignoreTipItems.add(id);
      }

      this.is = new ItemStack(this.messageItem, 1);
   }

   private ItemStack makeStack(Player p, int slot, String msg) {
      ItemStack stack0 = p.getInventory().getItem(slot);
      ItemStack stack;
      if (stack0 != null && stack0.getType() != Material.AIR) {
         stack = stack0.clone();
      } else {
         stack = this.is.clone();
      }

      ItemMeta meta = stack.getItemMeta();
      if (meta == null) {
         meta = this.itemFactory.getItemMeta(Material.STONE);
      }

      meta.setDisplayName(msg);
      stack.setItemMeta(meta);
      return stack;
   }

   private PacketContainer getSendItemSlotChangePacket(int slot, ItemStack stack) {
      PacketContainer setSlot = new PacketContainer(103);
      setSlot.getIntegers().write(0, 0).write(1, slot + 36);
      setSlot.getItemModifier().write(0, stack);
      return setSlot;
   }

   private void send(Player p, PacketContainer pc) {
      try {
         this.protocolManager.sendServerPacket(p, pc, false);
      } catch (InvocationTargetException var4) {
      }

   }

   private class Info {
      private int left;
      private String msg;
      private ChatColor c;

      public Info(int left, String msg) {
         this.left = left;
         this.msg = msg;
         this.c = ChatColor.WHITE;
      }

      public int getLeft() {
         return this.left;
      }

      public void setLeft(int left) {
         this.left = left;
      }

      public String getMsg() {
         return this.msg;
      }

      public ChatColor getColor() {
         return this.c;
      }

      public ChatColor getNextColor() {
         if (this.c.equals(ChatColor.WHITE)) {
            this.c = ChatColor.BLACK;
         } else {
            this.c = ChatColor.WHITE;
         }

         return this.c;
      }
   }
}
