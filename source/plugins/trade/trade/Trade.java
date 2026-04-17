package trade;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Trade implements Listener {
   private static final int CONFIRM_TIME = 3;
   private static final int CHECK_CAN_CONFIRM_INTERVAL = 9;
   private static final long CANCEL_TIME = 15L;
   private static final String SPEED = "speed";
   private static final int INTERVAL = 1000;
   private static HashMap tipHash = new HashMap();
   private static HashMap msgHash;
   private Main main;
   private static Speed speed;
   private static Time time;
   private static Names names;
   private static ItemManager im;
   private static EcoManager em;
   private HashMap statusHash = new HashMap();

   static {
      tipHash.put(5L, true);
      tipHash.put(10L, true);
      msgHash = new HashMap();
      msgHash.put(1, Util.convert("&a>已确认<"));
      msgHash.put(2, Util.convert("&c>未确认<"));
      msgHash.put(3, Util.convert("&a[可以确认]"));
      msgHash.put(4, Util.convert("&c[不可确认]"));
   }

   public Trade(Main main) {
      this.main = main;
      speed = new Speed();
      speed.register("trade", "speed");
      time = new Time(main);
      names = new Names(main);
      im = new ItemManager(main);
      em = new EcoManager(main);
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ConnectionSide.SERVER_SIDE, new Integer[]{3}) {
         public void onPacketSending(PacketEvent e) {
            Status s = (Status)Trade.this.statusHash.get(e.getPlayer());
            if (s != null && s.start) {
               e.setCancelled(true);
            }

         }
      });
      Bukkit.getPluginManager().registerEvents(this, main);
      Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
         public void run() {
            Trade.this.checkCanConfirm();
         }
      }, 9L, 9L);
   }

   public void onCommand(CommandSender sender, Command command, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null) {
         sender.sendMessage(Util.convert("&c此命令只能由玩家发出."));
      } else {
         String msg = speed.check(p, "trade", "speed", 1000);
         if (msg == null) {
            if (command.getName().equalsIgnoreCase("qx")) {
               this.cancel(p, true);
            } else if (command.getName().equalsIgnoreCase("qh")) {
               this.get(p);
            } else if (command.getName().equalsIgnoreCase("jq")) {
               try {
                  this.setGold(p, Integer.parseInt(args[0]));
               } catch (Exception var8) {
                  p.sendMessage(Util.convert("&b[交易]&c数字格式异常."));
               }
            } else if (command.getName().equalsIgnoreCase("wp")) {
               this.setItem(p);
            } else if (command.getName().equalsIgnoreCase("qr")) {
               this.confirm(p);
            }

         } else {
            Status status = (Status)this.statusHash.get(p);
            if (status != null && status.start) {
               if (status.from.getName().equals(p.getName())) {
                  status.msgFrom = msg;
               } else {
                  status.msgTo = msg;
               }

               this.update(p);
            } else {
               p.sendMessage(msg);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerAnimation(PlayerAnimationEvent e) {
      this.cancel(e.getPlayer(), false);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      this.cancel(e.getPlayer(), false);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (e.getRightClicked() instanceof Player) {
         Player p = e.getPlayer();
         int id = 0;
         ItemStack is = p.getItemInHand();
         if (is != null) {
            id = is.getTypeId();
         }

         if (id == 0) {
            if (p.isSneaking()) {
               e.setCancelled(true);
               if (im.getItem(p.getName()) != null) {
                  p.sendMessage(Util.convert("&b[交易]&c操作失败,你当前有未取回的交易物品&7(输入/qh取回)"));
                  return;
               }

               Player tar = (Player)e.getRightClicked();
               Status status = (Status)this.statusHash.get(p);
               if (status != null) {
                  if (status.from.getName().equals(p.getName())) {
                     if (!status.start) {
                        p.sendMessage(Util.convert("&b[交易]&c你正在请求与&e*&c交易中,无法再发起交易&7(输入/qx可取消请求)".replace("*", status.to.getName())));
                     }
                  } else if (status.from.getName().equals(tar.getName())) {
                     status.start = true;
                     status.change = System.currentTimeMillis();
                     this.update(p);
                     this.update(tar);
                  } else {
                     p.sendMessage(Util.convert("&b[交易]&c你当前有&e*&c的交易请求&7(输入/qx可取消请求)".replace("*", status.from.getName())));
                  }
               } else if (this.checkGetBack(p)) {
                  status = (Status)this.statusHash.get(tar);
                  if (status != null) {
                     p.sendMessage(Util.convert("&b[交易]&c对方正处于交易状态中,无法发起交易."));
                  } else {
                     Status s = new Status(p, tar);
                     this.statusHash.put(p, s);
                     this.statusHash.put(tar, s);
                     p.sendMessage(Util.convert("&b[交易]&a你对&e*&a发出了交易请求.".replace("*", tar.getName())));
                     tar.sendMessage(Util.convert("&b[交易]&6玩家&e*&6请求与你交易\n&7 - 空手+潜行+右键对方接受".replace("*", p.getName()) + "\n&7 - " + 15L + "秒内未接受即拒绝\n&7 - 输入/qx可立即取消拒绝对方的请求"));
                  }
               }
            } else {
               p.sendMessage(Util.convert("&5===== [&b发起交易&5] ====="));
               p.sendMessage(Util.convert("&a空手+潜行+右键玩家可请求向对方发起交易."));
               p.sendMessage(Util.convert("&5===== [&b接受交易&5] ====="));
               p.sendMessage(Util.convert("&a对方向你发起交易时,你同样空手+潜行+右键对方接受交易."));
               p.sendMessage(Util.convert("&5===== [&b拒绝交易&5] ====="));
               p.sendMessage(Util.convert("&a15秒内未接受即拒绝交易."));
               p.sendMessage(Util.convert("&5===== [&b取消交易&5] ====="));
               p.sendMessage(Util.convert("&a输入&d/qx&a可取消你当前的任何交易."));
               p.sendMessage(Util.convert("&5===== [&b取回物品&5] ====="));
               p.sendMessage(Util.convert("&a输入&d/qh&a可取回保存的交易物品."));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.cancel(e.getPlayer(), false);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      final Status status = (Status)this.statusHash.get(e.getPlayer());
      if (status != null && status.start) {
         String msg = "&3<*> &e{0}".replace("*", e.getPlayer().getName()).replace("{0}", e.getMessage());
         msg = Util.convert(msg);
         status.msgFrom = msg;
         status.msgTo = msg;
         Bukkit.getScheduler().scheduleSyncDelayedTask(this.main, new Runnable() {
            public void run() {
               Trade.this.update(status.from);
               Trade.this.update(status.to);
            }
         });
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      List<Player> removeList = new ArrayList();
      HashMap<Status, Boolean> hasHash = new HashMap();

      for(Status s : this.statusHash.values()) {
         if (!s.start && !hasHash.containsKey(s)) {
            hasHash.put(s, true);
            long left = 15L - TimeEvent.getTime() + s.init;
            if (left <= 0L) {
               removeList.add(s.from);
            } else if (tipHash.containsKey(left)) {
               s.from.sendMessage(Util.convert("&b[交易]&7玩家&e{0}&7未确认你的请求,交易将在&e{1}&7秒后自动取消.".replace("{0}", s.to.getName()).replace("{1}", "" + left)));
               s.to.sendMessage(Util.convert("&b[交易]&7你没有确认玩家&e{0}&7的请求,交易将在&e{1}&7秒后自动取消.".replace("{0}", s.from.getName()).replace("{1}", "" + left)));
            }
         }
      }

      for(Player p : removeList) {
         this.cancel(p, true);
      }

   }

   public static Time getTime() {
      return time;
   }

   private void confirm(Player p) {
      Status status = (Status)this.statusHash.get(p);
      if (status == null) {
         p.sendMessage(Util.convert("&b[交易]&7你当前没有正在进行中的交易."));
      } else if (!status.start) {
         p.sendMessage(Util.convert("&b[交易]&7你的当前交易未开始."));
      } else {
         boolean from = status.from.getName().equals(p.getName());
         if (!status.canConfirm) {
            String msg = Util.convert("&c为了保障交易安全,修改交易内容后过3秒才能确认.");
            if (from) {
               status.msgFrom = msg;
            } else {
               status.msgTo = msg;
            }

         } else if ((!from || !status.confirmFrom) && (from || !status.confirmTo)) {
            if (from) {
               if (!status.confirmTo) {
                  status.confirmFrom = true;
                  String msg = Util.convert("&a你确认了交易.");
                  status.msgFrom = msg;
                  this.update(status.from);
                  this.update(status.to);
                  return;
               }
            } else if (!status.confirmFrom) {
               status.confirmTo = true;
               String msg = Util.convert("&a你确认了交易.");
               status.msgTo = msg;
               this.update(status.from);
               this.update(status.to);
               return;
            }

            this.statusHash.remove(status.from);
            this.statusHash.remove(status.to);
            if (status.goldTo > 0) {
               em.addGold(status.from.getName(), status.goldTo);
            }

            if (status.goldFrom > 0) {
               em.addGold(status.to.getName(), status.goldFrom);
            }

            ItemStack itemFrom = im.getItem(status.from.getName());
            ItemStack itemTo = im.getItem(status.to.getName());
            im.delete(status.from.getName());
            im.delete(status.to.getName());
            im.save(status.from.getName(), itemTo);
            im.save(status.to.getName(), itemFrom);
            status.from.sendMessage(Util.convert("&b[交易]&a与&e*&a的交易成功!&7(输入/qh获取交易物品)".replace("*", status.to.getName())));
            status.to.sendMessage(Util.convert("&b[交易]&a与&e*&a的交易成功!&7(输入/qh获取交易物品)".replace("*", status.from.getName())));
         } else {
            String msg = Util.convert("&c你已经确认过了.");
            if (from) {
               status.msgFrom = msg;
            } else {
               status.msgTo = msg;
            }

         }
      }
   }

   private void checkCanConfirm() {
      long now = System.currentTimeMillis();

      for(Status status : this.statusHash.values()) {
         if (status.start && !status.canConfirm && now - status.change >= 3000L) {
            status.canConfirm = true;
            this.update(status.from);
            this.update(status.to);
         }
      }

   }

   private boolean checkGetBack(Player p) {
      ItemStack is = im.getItem(p.getName());
      if (is != null) {
         p.sendMessage(Util.convert("&b[交易]&c你必须先取回旧的交易物品&7(输入/qh取回)"));
         return false;
      } else {
         return true;
      }
   }

   private void setGold(Player p, int amount) {
      Status status = (Status)this.statusHash.get(p);
      if (status == null) {
         p.sendMessage(Util.convert("&b[交易]&7你当前没有正在进行中的交易."));
      } else if (!status.start) {
         p.sendMessage(Util.convert("&b[交易]&7交易当前未开始."));
      } else {
         if (amount < 0) {
            amount = 0;
         }

         int has = em.getGold(p.getName());
         if (has >= 0) {
            boolean from = status.from.getName().equals(p.getName());
            int put = 0;
            if (from) {
               put = status.goldFrom;
            } else {
               put = status.goldTo;
            }

            if (amount > has + put) {
               amount = has + put;
            }

            if (amount >= 0) {
               if (em.setGold(p.getName(), (double)(has + put - amount))) {
                  if (from) {
                     status.goldFrom = amount;
                  } else {
                     status.goldTo = amount;
                  }

                  status.change = System.currentTimeMillis();
                  status.canConfirm = false;
                  status.confirmFrom = false;
                  status.confirmTo = false;
                  String msg = Util.convert("&6你将放入交易栏的金钱数量设置为 {0}".replace("{0}", "" + amount));
                  if (from) {
                     status.msgFrom = msg;
                  } else {
                     status.msgTo = msg;
                  }

                  this.update(status.from);
                  this.update(status.to);
               }
            }
         }
      }
   }

   private void setItem(Player p) {
      Status status = (Status)this.statusHash.get(p);
      if (status == null) {
         p.sendMessage(Util.convert("&b[交易]&7你当前没有正在进行中的交易."));
      } else if (!status.start) {
         p.sendMessage(Util.convert("&b[交易]&7交易当前未开始."));
      } else {
         ItemStack handItem = p.getItemInHand();
         if (handItem != null && handItem.getTypeId() == 0) {
            handItem = null;
         }

         ItemStack putItem = im.getItem(p.getName());
         if (handItem == null) {
            im.delete(p.getName());
         } else {
            im.save(p.getName(), handItem);
         }

         p.setItemInHand(putItem);
         p.updateInventory();
         boolean from = status.from.getName().equals(p.getName());
         String msg = Util.convert("&6你改变了交易的物品.");
         if (from) {
            status.msgFrom = msg;
         } else {
            status.msgTo = msg;
         }

         status.change = System.currentTimeMillis();
         status.canConfirm = false;
         status.confirmFrom = false;
         status.confirmTo = false;
         this.update(status.from);
         this.update(status.to);
      }
   }

   private void update(Player p) {
      Status status = (Status)this.statusHash.get(p);
      if (status != null && status.start) {
         boolean from = status.from.getName().equals(p.getName());
         List<String> list = new ArrayList();
         list.add(Util.convert("&7 -"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7-"));
         list.add(Util.convert("&7+=========[&b&l交易操作&7]=========+"));
         list.add(Util.convert("&b/jq 数量 &7设置放入交易栏的金钱"));
         list.add(Util.convert("&b/wp &7设置放入交易栏的物品为手中物品(全部数量)"));
         list.add(Util.convert("&b/qr &7确认交易"));
         list.add(Util.convert("&b/qx &7取消交易"));
         list.add(Util.convert("&b/qh &7取回交易物品"));
         list.add(Util.convert("&7+==========[&b&l交易栏&7]==========+"));
         int gold;
         if (from) {
            gold = status.goldFrom;
         } else {
            gold = status.goldTo;
         }

         list.add(Util.convert("&7-----&b你放入的金钱:&e *&7-----".replace("*", "" + gold)));
         list.add(Util.convert("&7-----&b你放入的物品:&7-----"));
         this.addMsg(list, im.getItem(p.getName()));
         String protect;
         if (status.canConfirm) {
            protect = (String)msgHash.get(3);
         } else {
            protect = (String)msgHash.get(4);
         }

         String statusP;
         String statusTar;
         String tarName;
         if (from) {
            if (status.confirmFrom) {
               statusP = (String)msgHash.get(1);
            } else {
               statusP = (String)msgHash.get(2);
            }

            if (status.confirmTo) {
               statusTar = (String)msgHash.get(1);
            } else {
               statusTar = (String)msgHash.get(2);
            }

            tarName = status.to.getName();
         } else {
            if (status.confirmTo) {
               statusP = (String)msgHash.get(1);
            } else {
               statusP = (String)msgHash.get(2);
            }

            if (status.confirmFrom) {
               statusTar = (String)msgHash.get(1);
            } else {
               statusTar = (String)msgHash.get(2);
            }

            tarName = status.from.getName();
         }

         list.add(Util.convert("&7+--&b你:{1}&7---{0}&7---&b{3}:{2}&7--+".replace("{0}", protect).replace("{1}", statusP).replace("{2}", statusTar).replace("{3}", tarName)));
         if (from) {
            gold = status.goldTo;
         } else {
            gold = status.goldFrom;
         }

         list.add(Util.convert("&7-----&b对方放入的金钱:&e *&7-----".replace("*", "" + gold)));
         list.add(Util.convert("&7-----&b对方放入的物品:&7-----"));
         if (from) {
            this.addMsg(list, im.getItem(status.to.getName()));
         } else {
            this.addMsg(list, im.getItem(status.from.getName()));
         }

         list.add(Util.convert("&7+=========[&b&l交易提示&7]=========+"));
         String msg;
         if (from) {
            msg = status.msgFrom;
         } else {
            msg = status.msgTo;
         }

         list.add(msg);
         String result = "";
         boolean first = true;

         for(String s : list) {
            if (first) {
               first = false;
            } else {
               result = result + "\n";
            }

            result = result + s;
         }

         Util.sendMsg(p, result);
      }
   }

   private void addMsg(List list, ItemStack itemStack) {
      if (itemStack == null) {
         list.add(Util.convert("&7无"));
      } else {
         String name = names.getItemName(itemStack.getTypeId(), itemStack.getDurability());
         list.add(Util.convert("&3{0} x {1}".replace("{0}", name).replace("{1}", "" + itemStack.getAmount())));
         ItemMeta im = itemStack.getItemMeta();
         Map<Enchantment, Integer> enchants = im.getEnchants();
         String enMsg = "";
         if (enchants != null && enchants.size() > 0) {
            boolean first = true;

            for(Enchantment en : enchants.keySet()) {
               if (first) {
                  first = false;
               } else {
                  enMsg = enMsg + " ";
               }

               enMsg = enMsg + names.getEnchantName(en.getId()) + enchants.get(en);
            }

            list.add(Util.convert("&a附魔: &e{0}".replace("{0}", enMsg)));
         }

         List<String> lore = im.getLore();
         if (lore != null && lore.size() > 0) {
            list.add(Util.convert("&alore信息:"));

            for(String s : lore) {
               list.add(s);
            }
         }
      }

   }

   private void cancel(Player p, boolean tip) {
      Status status = (Status)this.statusHash.get(p);
      if (status == null) {
         if (tip) {
            p.sendMessage(Util.convert("&b[交易]&7你当前没有处于交易状态."));
         }
      } else {
         Player from = status.from;
         Player to = status.to;
         this.statusHash.remove(from);
         this.statusHash.remove(to);
         if (status.start) {
            int goldFrom = status.goldFrom;
            if (goldFrom > 0) {
               em.addGold(from.getName(), goldFrom);
            }

            int goldTo = status.goldTo;
            if (goldTo > 0) {
               em.addGold(to.getName(), goldTo);
            }
         }

         from.sendMessage(Util.convert("&b[交易]&7你与&e*&7的交易已取消.".replace("*", to.getName())));
         to.sendMessage(Util.convert("&b[交易]&7你与&e*&7的交易已取消.".replace("*", from.getName())));
      }

   }

   private void get(Player p) {
      Status status = (Status)this.statusHash.get(p);
      if (status != null && status.start) {
         String msg = Util.convert("&b[交易]&c你当前处于交易中,无法取回物品&7(/qx取消交易)");
         if (status.from.getName().equals(p.getName())) {
            status.msgFrom = msg;
         } else {
            status.msgTo = msg;
         }

         this.update(p);
      } else {
         ItemStack is = im.getItem(p.getName());
         if (is == null) {
            p.sendMessage(Util.convert("&b[交易]&7你当前没有保存的交易物品需要取回."));
         } else {
            if (Util.getEmptySlots(p.getInventory()) <= 0) {
               p.sendMessage(Util.convert("&b[交易]&c请在背包中至少留出一个空格来."));
               return;
            }

            p.getInventory().addItem(new ItemStack[]{is});
            p.updateInventory();
            im.delete(p.getName());
            p.sendMessage(Util.convert("&b[交易]&a成功取回保存的交易物品."));
         }

      }
   }

   private static class Status {
      Player from;
      Player to;
      long init;
      boolean start;
      long change;
      boolean canConfirm;
      boolean confirmFrom;
      boolean confirmTo;
      int goldFrom;
      int goldTo;
      String msgFrom;
      String msgTo;

      public Status(Player from, Player to) {
         this.from = from;
         this.to = to;
         this.init = TimeEvent.getTime();
         this.msgFrom = "无";
         this.msgTo = "无";
      }
   }
}
