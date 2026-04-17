package lock;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Lock extends JavaPlugin implements Listener {
   static final String NUM = "0123456789";
   static final int MAX_HEIGHT = 254;
   static final int WALL_SIGN_ID = 68;
   static final BlockFace[] FOUR_FACE;
   private static final String LOCK = "lock";
   private static final ItemMeta IM;
   private static final UUID uid;
   private String pn;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private String per_lock_create;
   private String per_lock_create_key;
   private String per_lock_use;
   private String per_lock_admin;
   private String per_lock_vip;
   private boolean banOtherInteract;
   private boolean banOtherBreakDoor;
   private boolean banRedstoneOpenDoor;
   private int interval;
   private int doorId;
   private HashList worlds;
   private boolean sneak;
   private double range;
   private boolean leaveCost;
   private int max;
   private int maxVip;
   private int rate;
   private boolean tipOwner;
   private String lock;
   private String line1;
   private String line2;
   private String line3;
   private int line2Start;
   private int line2Left;
   private char allowColor;
   private char allowLine;
   private char denyColor;
   private char denyLine;
   private String flagEnter;
   private String flagLeave;
   private String flagEmpty;
   private String flagMoney;
   private String flagEffect;
   private String line1Empty;
   private String line1Money;
   private String line1Effect;
   private int keyItem;
   private String keyShowName;
   private List keyShowLore;
   private int keyShowLength;
   private boolean keyOut;
   private String keyFlag;
   private String keyFormat;
   private int keyMax;
   private int keyLength;
   private int keyCost;
   private int keyCostVip;
   private boolean rateCancelVip;
   private int check1;

   static {
      FOUR_FACE = new BlockFace[]{BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH};
      IM = (new ItemStack(1)).getItemMeta();
      uid = UUID.fromString("139292bd-88b8-4c8f-8380-dbb9df24cc38");
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, this);
      UtilSpeed.register(this.pn, "lock");
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         int length = args.length;
         if (cmd.getName().equalsIgnoreCase("locks")) {
            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
               this.reloadConfig(sender);
               return true;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_lock_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            if (p != null) {
               this.tip(p);
            }
         } else if (cmd.getName().equalsIgnoreCase("key")) {
            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 2) {
               if (args[0].equalsIgnoreCase("create")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
                  } else {
                     this.createKey(p, args[1]);
                  }

                  return true;
               }

               if (args[0].equalsIgnoreCase("copy")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
                  } else {
                     this.copyKey(p, Integer.parseInt(args[1]));
                  }

                  return true;
               }

               if (args[0].equalsIgnoreCase("tip")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
                  } else {
                     this.tipKey(p, args[1]);
                  }

                  return true;
               }

               if (args[0].equalsIgnoreCase("time")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
                  } else {
                     this.timeKey(p, Integer.parseInt(args[1]));
                  }

                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(235)}));
            if (p == null || UtilPer.hasPer(p, this.per_lock_create_key)) {
               String vip = "";
               int costPre = this.keyCostVip;
               if (p != null && !UtilPer.hasPer(p, this.per_lock_vip)) {
                  vip = "§m";
                  costPre = this.keyCost;
               }

               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(140), UtilFormat.format(this.pn, "createKey", new Object[]{costPre, vip})}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(185), UtilFormat.format(this.pn, "copyKey", new Object[]{costPre, vip})}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(165), this.get(170)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(200), this.get(205)}));
            }
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityInteract(EntityInteractEvent e) {
      if (this.banOtherInteract && !(e.getEntity() instanceof Player) && this.isLockDoor(e.getBlock())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityBreakDoor(EntityBreakDoorEvent e) {
      if (this.banOtherBreakDoor && !(e.getEntity() instanceof Player) && this.isLockDoor(e.getBlock())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockRedstone(BlockRedstoneEvent e) {
      if (this.banRedstoneOpenDoor && e.getBlock().getTypeId() == this.doorId) {
         Block b;
         if (e.getBlock().getRelative(BlockFace.UP).getTypeId() == this.doorId) {
            b = e.getBlock().getRelative(BlockFace.UP, 2);
         } else {
            b = e.getBlock().getRelative(BlockFace.UP);
         }

         BlockFace[] var6;
         for(BlockFace bf : var6 = FOUR_FACE) {
            if (b.getRelative(bf).getTypeId() == 68) {
               Sign sign = (Sign)b.getRelative(bf).getState();
               if (sign.getLine(0).length() >= this.check1 && sign.getLine(0).subSequence(0, this.check1).equals(this.line1)) {
                  e.setNewCurrent(e.getOldCurrent());
                  return;
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      Player p = e.getPlayer();
      if (e.getLines()[0].length() >= this.check1 && e.getLines()[0].substring(0, this.check1).equalsIgnoreCase(this.line1)) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
         e.setCancelled(true);
      } else if (e.getLines()[0].equalsIgnoreCase(this.lock)) {
         if (!UtilPer.checkPer(p, this.per_lock_create)) {
            e.setCancelled(true);
         } else if (!UtilPer.hasPer(p, this.per_lock_admin) && !this.worlds.has(e.getBlock().getWorld().getName())) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(125)}));
            e.setCancelled(true);
         } else if (e.getBlock().getLocation().getBlockY() >= 254) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
            e.setCancelled(true);
         } else {
            try {
               int cost = Integer.parseInt(e.getLines()[1]);
               if (cost < 0) {
                  p.sendMessage(this.get(262));
                  e.setCancelled(true);
                  return;
               }

               if (!UtilPer.hasPer(p, this.per_lock_admin)) {
                  int max;
                  if (UtilPer.hasPer(p, this.per_lock_vip)) {
                     max = this.maxVip;
                  } else {
                     max = this.max;
                  }

                  if (cost > max) {
                     p.sendMessage(UtilFormat.format(this.pn, "max", new Object[]{max}));
                     e.setCancelled(true);
                     return;
                  }
               }

               boolean enter = true;
               boolean leave = true;
               String key = null;
               String[] args;
               if ((args = e.getLines()[2].split(":")).length == 2) {
                  if (args[0].equalsIgnoreCase(this.keyFlag)) {
                     key = args[1];
                     if (key.isEmpty() || key.length() > this.keyLength) {
                        p.sendMessage(UtilFormat.format(this.pn, "lengthErr", new Object[]{this.keyLength}));
                        e.setCancelled(true);
                        return;
                     }

                     if (!UtilPer.hasPer(p, this.per_lock_create_key)) {
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(245)}));
                        e.setCancelled(true);
                        return;
                     }
                  }
               } else {
                  enter = e.getLines()[2].contains(this.flagEnter);
                  leave = e.getLines()[2].contains(this.flagLeave);
               }

               boolean empty = e.getLines()[3].contains(this.flagEmpty);
               boolean money = e.getLines()[3].contains(this.flagMoney);
               boolean effect = e.getLines()[3].contains(this.flagEffect);
               org.bukkit.material.Sign sign = (org.bukkit.material.Sign)e.getBlock().getType().getNewData(e.getBlock().getData());
               Block check = e.getBlock().getRelative(sign.getFacing().getOppositeFace(), 2);
               if (check.getTypeId() == 68 && ((Sign)check.getState()).getLines()[0].equals(this.line1)) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(100)}));
                  e.setCancelled(true);
                  return;
               }

               String line1Result = this.line1;
               if (empty) {
                  line1Result = line1Result + this.line1Empty;
               }

               if (money) {
                  line1Result = line1Result + this.line1Money;
               }

               if (effect) {
                  line1Result = line1Result + this.line1Effect;
               }

               e.setLine(0, line1Result);
               e.setLine(1, this.line2.replace("*", String.valueOf(cost)));
               String s;
               if (key == null) {
                  s = this.line3;
                  if (enter) {
                     s = s.replace("{0}", String.valueOf(this.allowColor));
                     s = s.replace("{1}", String.valueOf(this.allowLine));
                  } else {
                     s = s.replace("{0}", String.valueOf(this.denyColor));
                     s = s.replace("{1}", String.valueOf(this.denyLine));
                  }

                  if (leave) {
                     s = s.replace("{2}", String.valueOf(this.allowColor));
                     s = s.replace("{3}", String.valueOf(this.allowLine));
                  } else {
                     s = s.replace("{2}", String.valueOf(this.denyColor));
                     s = s.replace("{3}", String.valueOf(this.denyLine));
                  }
               } else {
                  s = this.keyFormat.replace("{0}", key);
               }

               e.setLine(2, s);
               e.setLine(3, p.getName());
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(45)}));
            } catch (NumberFormatException var15) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
               e.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasBlock() && e.getClickedBlock().getTypeId() == this.doorId) {
            Block b = e.getClickedBlock();
            if (b.getLocation().getBlockY() >= 254) {
               return;
            }

            if (b.getRelative(BlockFace.UP).getTypeId() == this.doorId) {
               b = b.getRelative(BlockFace.UP, 2);
            } else {
               b = b.getRelative(BlockFace.UP);
            }

            BlockFace bf = null;
            Sign sign = null;
            if (b.getRelative(e.getBlockFace()).getTypeId() == 68) {
               sign = (Sign)b.getRelative(e.getBlockFace()).getState();
               if (sign.getLine(0).length() >= this.check1 && sign.getLine(0).subSequence(0, this.check1).equals(this.line1)) {
                  bf = e.getBlockFace();
               }
            }

            if (bf == null && b.getRelative(e.getBlockFace().getOppositeFace()).getTypeId() == 68) {
               sign = (Sign)b.getRelative(e.getBlockFace().getOppositeFace()).getState();
               if (sign.getLine(0).length() >= this.check1 && sign.getLine(0).subSequence(0, this.check1).equals(this.line1)) {
                  bf = e.getBlockFace().getOppositeFace();
               }
            }

            if (bf == null) {
               return;
            }

            int cost = Integer.parseInt(sign.getLine(1).substring(this.line2Start, sign.getLine(1).length() - this.line2Left));
            if (cost < 0) {
               return;
            }

            String owner = sign.getLine(3);
            if (!Bukkit.getOfflinePlayer(owner).hasPlayedBefore()) {
               return;
            }

            if (b.getRelative(BlockFace.DOWN).getLocation().distance(e.getPlayer().getEyeLocation()) > this.range) {
               e.getPlayer().sendMessage(this.get(260));
               e.setCancelled(true);
               return;
            }

            Player p = e.getPlayer();
            owner = Bukkit.getOfflinePlayer(owner).getName();
            boolean self = owner.equals(p.getName());
            if (!UtilSpeed.check(p, this.pn, "lock", this.interval)) {
               e.setCancelled(true);
               return;
            }

            if (!UtilPer.checkPer(p, this.per_lock_use)) {
               e.setCancelled(true);
               return;
            }

            boolean enter;
            if (e.getBlockFace().equals(bf)) {
               enter = true;
            } else {
               if (!e.getBlockFace().getOppositeFace().equals(bf)) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
                  e.setCancelled(true);
                  return;
               }

               enter = false;
            }

            String keyName = this.getKeyName(sign.getLine(2));
            boolean useKey = false;
            if (!self) {
               if (keyName == null) {
                  boolean allowEnter = false;
                  boolean allowLeave = false;
                  if (sign.getLine(2).charAt(3) == this.allowLine) {
                     allowEnter = true;
                  }

                  if (sign.getLine(2).charAt(10) == this.allowLine) {
                     allowLeave = true;
                  }

                  if (enter && !allowEnter) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(90)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (!enter && !allowLeave) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(95)}));
                     e.setCancelled(true);
                     return;
                  }
               } else if (!enter) {
                  if (!this.keyOut) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(95)}));
                     e.setCancelled(true);
                     return;
                  }
               } else {
                  KeyInfo keyInfo = this.getKeyInfo(p.getItemInHand());
                  if (keyInfo == null) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(220)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (!keyInfo.owner.equals(owner)) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (!keyInfo.key.equals(keyName)) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(230)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (keyInfo.time != -1) {
                     long startMinute = keyInfo.start / 60000L;
                     long nowMinute = System.currentTimeMillis() / 60000L;
                     if (nowMinute >= startMinute + (long)keyInfo.time) {
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(240)}));
                        e.setCancelled(true);
                        p.setItemInHand((ItemStack)null);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Update(p));
                        return;
                     }
                  }

                  useKey = true;
               }
            }

            if (this.sneak && !p.isSneaking()) {
               int willCost = 0;
               if (!self) {
                  if (enter) {
                     willCost = cost;
                  } else if (this.leaveCost) {
                     willCost = cost;
                  }
               }

               p.sendMessage(UtilFormat.format(this.pn, "leaveCostTip", new Object[]{willCost}));
               e.setCancelled(true);
               return;
            }

            if (enter) {
               if (!self && UtilEco.get(p.getName()) < (double)cost) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
                  e.setCancelled(true);
                  return;
               }
            } else if (this.leaveCost && !self && UtilEco.get(p.getName()) < (double)cost) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
               e.setCancelled(true);
               return;
            }

            if (!this.leaveCost && !enter) {
               cost = 0;
            }

            if (!self && sign.getLine(0).indexOf(this.line1Empty) != -1 && !UtilItems.checkEmpty(p)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(105)}));
               e.setCancelled(true);
               return;
            }

            if (!self && sign.getLine(0).indexOf(this.line1Money) != -1 && UtilEco.get(p.getName()) > (double)0.0F) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(120)}));
               e.setCancelled(true);
               return;
            }

            if (!self && sign.getLine(0).indexOf(this.line1Effect) != -1 && !p.getActivePotionEffects().isEmpty()) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
               e.setCancelled(true);
               return;
            }

            if (!self) {
               if (useKey) {
                  int amount = p.getItemInHand().getAmount();
                  if (amount <= 1) {
                     p.setItemInHand((ItemStack)null);
                  } else {
                     p.getItemInHand().setAmount(amount - 1);
                  }

                  Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Update(p));
               }

               if (cost > 0) {
                  UtilEco.del(p.getName(), (double)cost);
                  p.sendMessage(UtilFormat.format(this.pn, "pay2", new Object[]{cost}));
               }

               String vip = "§m";
               int get = cost;
               if (this.rateCancelVip && UtilPer.hasPer(owner, this.per_lock_vip)) {
                  vip = "";
               } else {
                  get = cost * (100 - this.rate) / 100;
               }

               if (get > 0) {
                  UtilEco.add(owner, (double)get);
               }

               if (this.tipOwner) {
                  if (this.rateCancelVip) {
                     Util.sendMsg(owner, UtilFormat.format(this.pn, "pay", new Object[]{p.getName(), get, vip}));
                  } else {
                     Util.sendMsg(owner, UtilFormat.format(this.pn, "pay3", new Object[]{p.getName(), get}));
                  }
               }
            }

            Location tarLoc = p.getLocation();
            if (enter) {
               bf = bf.getOppositeFace();
               tarLoc.setX((double)e.getClickedBlock().getRelative(bf).getLocation().getBlockX() + (double)0.5F);
               tarLoc.setY(b.getRelative(BlockFace.DOWN, 2).getLocation().getY());
               tarLoc.setZ((double)e.getClickedBlock().getRelative(bf).getLocation().getBlockZ() + (double)0.5F);
            } else {
               tarLoc.setX((double)e.getClickedBlock().getRelative(bf).getLocation().getBlockX() + (double)0.5F);
               tarLoc.setY(b.getRelative(BlockFace.DOWN, 2).getLocation().getY());
               tarLoc.setZ((double)e.getClickedBlock().getRelative(bf).getLocation().getBlockZ() + (double)0.5F);
            }

            p.teleport(tarLoc, TeleportCause.PLUGIN);
            if (enter) {
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(70)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(75)}));
            }

            e.setUseInteractedBlock(Result.DENY);
            e.setUseItemInHand(Result.DENY);
            e.setCancelled(true);
         }
      } catch (Exception var17) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerInteractMonitor(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
         Sign sign = (Sign)e.getClickedBlock().getState();
         if (sign.getLine(0).length() >= this.check1 && sign.getLine(0).subSequence(0, this.check1).equals(this.line1)) {
            this.tip(e.getPlayer());
         }
      }

   }

   public boolean isLockDoor(Block b) {
      if (b.getTypeId() != this.doorId) {
         return false;
      } else {
         Block check;
         if (b.getRelative(BlockFace.UP).getTypeId() == this.doorId) {
            check = b.getRelative(BlockFace.UP, 2);
         } else {
            check = b.getRelative(BlockFace.UP);
         }

         BlockFace[] var6;
         for(BlockFace bf : var6 = FOUR_FACE) {
            if (check.getRelative(bf).getTypeId() == 68) {
               Sign sign = (Sign)check.getRelative(bf).getState();
               if (sign.getLine(0).length() >= this.check1 && sign.getLine(0).subSequence(0, this.check1).equals(this.line1)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void tip(Player p) {
      p.sendMessage(this.get(265));
   }

   private String getKeyName(String s) {
      String[] ss = s.split(" ");
      return ss.length == 2 ? ss[1] : null;
   }

   private void timeKey(Player p, int time) {
      if (UtilPer.checkPer(p, this.per_lock_create_key)) {
         ItemStack keyItem = p.getItemInHand();
         KeyInfo keyInfo = this.getKeyInfo(keyItem);
         if (keyInfo == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
         } else if (!keyInfo.owner.equals(p.getName()) && !UtilPer.hasPer(p, this.per_lock_admin)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(195)}));
         } else if (time != -1 && time < 1) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(215)}));
         } else {
            int amount = keyItem.getAmount();
            long start = System.currentTimeMillis();
            ItemStack result = this.getKey(keyInfo.key, keyInfo.owner, start, time, keyInfo.tip);
            result.setAmount(amount);
            p.setItemInHand(result);
            p.updateInventory();
            String deadTime = this.get(135);
            if (time != -1) {
               deadTime = Util.getDateTime(new Date(start), 0, 0, time);
            }

            p.sendMessage(UtilFormat.format(this.pn, "setTimeSuccess", new Object[]{deadTime}));
         }
      }
   }

   private void tipKey(Player p, String tip) {
      if (UtilPer.checkPer(p, this.per_lock_create_key)) {
         ItemStack keyItem = p.getItemInHand();
         KeyInfo keyInfo = this.getKeyInfo(keyItem);
         if (keyInfo == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
         } else if (!keyInfo.owner.equals(p.getName()) && !UtilPer.hasPer(p, this.per_lock_admin)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(195)}));
         } else {
            tip = Util.convert(tip);
            if ((tip.isEmpty() || tip.length() > this.keyShowLength) && !UtilPer.hasPer(p, this.per_lock_admin)) {
               p.sendMessage(UtilFormat.format(this.pn, "lengthErr", new Object[]{this.keyShowLength}));
            } else {
               int amount = keyItem.getAmount();
               long start = System.currentTimeMillis();
               ItemStack result = this.getKey(keyInfo.key, keyInfo.owner, start, keyInfo.time, tip);
               result.setAmount(amount);
               p.setItemInHand(result);
               p.updateInventory();
               p.sendMessage(UtilFormat.format(this.pn, "setTipSuccess", new Object[]{tip}));
            }
         }
      }
   }

   private void copyKey(Player p, int amount) {
      if (UtilPer.checkPer(p, this.per_lock_create_key)) {
         if (amount < 1) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(155)}));
         } else {
            boolean vip = UtilPer.hasPer(p, this.per_lock_vip);
            int has = (int)UtilEco.get(p.getName());
            int costPre;
            if (vip) {
               costPre = this.keyCostVip;
            } else {
               costPre = this.keyCost;
            }

            if (costPre > 0) {
               amount = Math.min(amount, has / costPre);
               if (amount < 1) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(160)}));
                  return;
               }
            }

            ItemStack keyItem = p.getItemInHand();
            KeyInfo keyInfo = this.getKeyInfo(keyItem);
            if (keyInfo == null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
            } else if (!keyInfo.owner.equals(p.getName()) && !UtilPer.hasPer(p, this.per_lock_admin)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(195)}));
            } else {
               if (amount > this.keyMax && !UtilPer.hasPer(p, this.per_lock_admin)) {
                  amount = this.keyMax;
               }

               int cost = amount * costPre;
               if (cost > 0) {
                  UtilEco.del(p.getName(), (double)cost);
                  p.sendMessage(UtilFormat.format(this.pn, "pay2", new Object[]{cost}));
               }

               HashMap<Integer, ItemStack> itemsHash = new HashMap();
               int maxStackSize = keyItem.getType().getMaxStackSize();
               int times = amount / maxStackSize;

               for(int i = 0; i < times; ++i) {
                  ItemStack is = keyItem.clone();
                  is.setAmount(maxStackSize);
                  itemsHash.put(i, is);
               }

               int left = amount % maxStackSize;
               if (left > 0) {
                  ItemStack is = keyItem.clone();
                  is.setAmount(left);
                  itemsHash.put(times, is);
               }

               UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, this.get(210), itemsHash, true);
               p.sendMessage(UtilFormat.format(this.pn, "copySuccess", new Object[]{amount}));
            }
         }
      }
   }

   private void createKey(Player p, String key) {
      if (UtilPer.checkPer(p, this.per_lock_create_key)) {
         if (!key.isEmpty() && key.length() <= this.keyLength) {
            boolean vip = UtilPer.hasPer(p, this.per_lock_vip);
            int has = (int)UtilEco.get(p.getName());
            int costPre;
            if (vip) {
               costPre = this.keyCostVip;
            } else {
               costPre = this.keyCost;
            }

            if (has < costPre) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(160)}));
            } else {
               if (costPre > 0) {
                  UtilEco.del(p.getName(), (double)costPre);
                  p.sendMessage(UtilFormat.format(this.pn, "pay2", new Object[]{costPre}));
               }

               HashMap<Integer, ItemStack> itemsHash = new HashMap();
               long start = System.currentTimeMillis();
               ItemStack keyItem = this.getKey(key, p.getName(), start, -1, (String)null);
               itemsHash.put(0, keyItem);
               UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, this.get(180), itemsHash, true);
               p.sendMessage(UtilFormat.format(this.pn, "makeSuccess", new Object[]{key}));
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "lengthErr", new Object[]{this.keyLength}));
         }
      }
   }

   private KeyInfo getKeyInfo(ItemStack is) {
      try {
         if (is != null && is.getTypeId() == this.keyItem) {
            Attributes a = new Attributes(is);
            if (a.size() != 1) {
               return null;
            } else {
               Attributes.Attribute at = a.get(0);
               String data = at.getName();
               YamlConfiguration config = new YamlConfiguration();
               config.loadFromString(data);
               String key = config.getString("key");
               String owner = config.getString("owner");
               long start = config.getLong("start");
               int time = config.getInt("time");
               String tip = config.getString("tip");
               KeyInfo result = new KeyInfo(key, owner, start, time, tip);
               return result;
            }
         } else {
            return null;
         }
      } catch (Exception var13) {
         return null;
      }
   }

   private ItemStack getKey(String key, String owner, long start, int time, String tip) {
      ItemStack result = new ItemStack(this.keyItem, 1);
      ItemMeta im = IM.clone();
      im.setDisplayName(this.keyShowName.replace("{0}", key));
      List<String> lore = new ArrayList();
      lore.add(((String)this.keyShowLore.get(0)).replace("{0}", owner));
      String deadTime = this.get(135);
      if (time != -1) {
         deadTime = Util.getDateTime(new Date(start), 0, 0, time);
      }

      lore.add(((String)this.keyShowLore.get(1)).replace("{0}", deadTime));
      if (tip == null) {
         tip = this.get(175);
      }

      lore.add(((String)this.keyShowLore.get(2)).replace("{0}", tip));
      lore.add((String)this.keyShowLore.get(3));
      im.setLore(lore);
      result.setItemMeta(im);
      Attributes a = new Attributes(result);
      String data = this.getData(key, owner, start, time, tip);
      Attributes.Attribute at = Attribute.newBuilder().amount((double)0.0F).name(data).operation(Operation.ADD_NUMBER).type(AttributeType.GENERIC_MAX_HEALTH).uuid(uid).build();
      a.add(at);
      result = a.getStack();
      return result;
   }

   private String getData(String key, String owner, long start, int time, String tip) {
      if (time == -1) {
         start = 0L;
      }

      YamlConfiguration config = new YamlConfiguration();
      config.set("key", key);
      config.set("owner", owner);
      config.set("start", start);
      config.set("time", time);
      config.set("tip", tip);
      return config.saveToString();
   }

   private void initBasic() {
      this.pn = this.getName();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      UtilConfig.register(new File(this.pluginPath + File.separator + "lock.jar"), this.dataFolder, (HashList)null, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_lock_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
         }
      }

   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = Bukkit.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            Bukkit.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_lock_create = config.getString("per_lock_create");
      this.per_lock_create_key = config.getString("per_lock_create_key");
      this.per_lock_use = config.getString("per_lock_use");
      this.per_lock_admin = config.getString("per_lock_admin");
      this.per_lock_vip = config.getString("per_lock_vip");
      this.banOtherInteract = config.getBoolean("banOtherInteract");
      this.banOtherBreakDoor = config.getBoolean("banOtherBreakDoor");
      this.banRedstoneOpenDoor = config.getBoolean("banRedstoneOpenDoor");
      this.interval = config.getInt("interval");
      this.doorId = config.getInt("doorId");
      this.worlds = new HashListImpl();

      for(String s : config.getStringList("worlds")) {
         this.worlds.add(s);
      }

      this.sneak = config.getBoolean("sneak");
      this.range = config.getDouble("range");
      this.leaveCost = config.getBoolean("leaveCost");
      this.max = config.getInt("max");
      this.rate = config.getInt("rate");
      this.tipOwner = config.getBoolean("tipOwner");
      this.lock = config.getString("lock");
      this.line1 = Util.convert(config.getString("line1"));
      this.check1 = this.line1.length();
      this.line2 = Util.convert(config.getString("line2"));
      this.line2Start = this.line2.split("\\*")[0].length();
      this.line2Left = this.line2.split("\\*")[1].length();
      this.line3 = Util.convert(config.getString("line3"));
      this.allowColor = config.getString("allowColor").charAt(0);
      this.allowLine = config.getString("allowLine").charAt(0);
      this.denyColor = config.getString("denyColor").charAt(0);
      this.denyLine = config.getString("denyLine").charAt(0);
      this.flagEnter = config.getString("flagEnter");
      this.flagLeave = config.getString("flagLeave");
      this.flagEmpty = config.getString("flagEmpty");
      this.flagMoney = config.getString("flagMoney");
      this.flagEffect = config.getString("flagEffect");
      this.line1Empty = config.getString("line1Empty");
      this.line1Money = config.getString("line1Money");
      this.line1Effect = config.getString("line1Effect");
      this.keyItem = config.getInt("key.item");
      this.keyShowName = Util.convert(config.getString("key.show.name"));
      this.keyShowLore = new ArrayList();

      for(String s : config.getStringList("key.show.lore")) {
         this.keyShowLore.add(Util.convert(s));
      }

      this.keyShowLength = config.getInt("key.show.length");
      this.keyOut = config.getBoolean("key.out");
      this.keyFlag = config.getString("key.flag");
      this.keyFormat = Util.convert(config.getString("key.format"));
      this.keyMax = config.getInt("key.max");
      this.keyLength = config.getInt("key.length");
      this.keyCost = config.getInt("key.cost");
      this.maxVip = config.getInt("maxVip");
      this.rateCancelVip = config.getBoolean("rateCancelVip");
      this.keyCostVip = config.getInt("keyCostVip");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Update implements Runnable {
      private Player p;

      public Update(Player p) {
         this.p = p;
      }

      public void run() {
         try {
            this.p.closeInventory();
            this.p.updateInventory();
         } catch (Exception var2) {
         }

      }
   }

   private class KeyInfo {
      String key;
      String owner;
      long start;
      int time;
      String tip;

      public KeyInfo(String key, String owner, long start, int time, String tip) {
         this.key = key;
         this.owner = owner;
         this.start = start;
         this.time = time;
         this.tip = tip;
      }
   }
}
