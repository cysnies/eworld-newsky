package org.yi.acru.bukkit.Lockette;

import java.util.HashSet;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class LockettePlayerListener implements Listener {
   private static Lockette plugin;
   static final int materialFenceGate = 107;

   public LockettePlayerListener(Lockette instance) {
      plugin = instance;
   }

   protected void registerEvents() {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      String[] command = event.getMessage().split(" ", 3);
      if (command.length >= 1) {
         if (command[0].equalsIgnoreCase("/lockette") || command[0].equalsIgnoreCase("/lock")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (command.length == 2) {
               if (command[1].equalsIgnoreCase("reload")) {
                  if (!plugin.hasPermission(player.getWorld(), player, "lockette.admin.reload")) {
                     return;
                  }

                  plugin.loadProperties(true);
                  plugin.localizedMessage(player, Lockette.broadcastReloadTarget, "msg-admin-reload");
                  return;
               }

               if (command[1].equalsIgnoreCase("version")) {
                  player.sendMessage(ChatColor.RED + "Lockette version " + plugin.getDescription().getVersion() + " loaded.  (Core: " + Lockette.getCoreVersion() + ")");
                  return;
               }

               if (command[1].equalsIgnoreCase("fix")) {
                  if (fixDoor(player)) {
                     plugin.localizedMessage(player, (String)null, "msg-error-fix");
                  }

                  return;
               }
            }

            if ((command.length == 2 || command.length == 3) && (command[1].equals("1") || command[1].equals("2") || command[1].equals("3") || command[1].equals("4"))) {
               Block block = (Block)plugin.playerList.get(player.getName());
               if (block == null) {
                  plugin.localizedMessage(player, (String)null, "msg-error-edit");
               } else if (block.getTypeId() != Material.WALL_SIGN.getId()) {
                  plugin.localizedMessage(player, (String)null, "msg-error-edit");
               } else {
                  Sign sign = (Sign)block.getState();
                  Sign owner = sign;
                  String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
                  boolean privateSign;
                  if (!text.equals("[private]") && !text.equalsIgnoreCase(Lockette.altPrivate)) {
                     if (!text.equals("[more users]") && !text.equalsIgnoreCase(Lockette.altMoreUsers)) {
                        plugin.localizedMessage(player, (String)null, "msg-error-edit");
                        return;
                     }

                     privateSign = false;
                     Block checkBlock = Lockette.getSignAttachedBlock(block);
                     if (checkBlock == null) {
                        plugin.localizedMessage(player, (String)null, "msg-error-edit");
                        return;
                     }

                     Block signBlock = Lockette.findBlockOwner(checkBlock);
                     if (signBlock == null) {
                        plugin.localizedMessage(player, (String)null, "msg-error-edit");
                        return;
                     }

                     owner = (Sign)signBlock.getState();
                  } else {
                     privateSign = true;
                  }

                  int length = player.getName().length();
                  if (length > 15) {
                     length = 15;
                  }

                  if (!owner.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length)) && !Lockette.debugMode) {
                     plugin.localizedMessage(player, (String)null, "msg-error-edit");
                  } else {
                     int line = Integer.parseInt(command[1]) - 1;
                     if (!Lockette.debugMode) {
                        if (line <= 0) {
                           return;
                        }

                        if (line <= 1 && privateSign) {
                           return;
                        }
                     }

                     if (command.length == 3) {
                        length = command[2].length();
                        if (length > 15) {
                           length = 15;
                        }

                        if (Lockette.colorTags) {
                           sign.setLine(line, command[2].substring(0, length).replaceAll("&([0-9A-Fa-f])", "§$1"));
                        } else {
                           sign.setLine(line, command[2].substring(0, length));
                        }
                     } else {
                        sign.setLine(line, "");
                     }

                     sign.update();
                     plugin.localizedMessage(player, (String)null, "msg-owner-edit");
                  }
               }
            } else {
               plugin.localizedMessage(player, (String)null, "msg-help-command1");
               plugin.localizedMessage(player, (String)null, "msg-help-command2");
               plugin.localizedMessage(player, (String)null, "msg-help-command3");
               plugin.localizedMessage(player, (String)null, "msg-help-command4");
               plugin.localizedMessage(player, (String)null, "msg-help-command5");
               plugin.localizedMessage(player, (String)null, "msg-help-command6");
               plugin.localizedMessage(player, (String)null, "msg-help-command7");
               plugin.localizedMessage(player, (String)null, "msg-help-command8");
               plugin.localizedMessage(player, (String)null, "msg-help-command9");
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.hasBlock()) {
         Action action = event.getAction();
         Player player = event.getPlayer();
         Block block = event.getClickedBlock();
         int type = block.getTypeId();
         BlockFace face = event.getBlockFace();
         if (action == Action.RIGHT_CLICK_BLOCK) {
            if (Lockette.protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
               if (interactDoor(block, player)) {
                  return;
               }

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }

            if (!Lockette.protectDoors || type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != 107) {
               if (type == Material.WALL_SIGN.getId()) {
                  interactSign(block, player);
                  return;
               }

               if (type == Material.CHEST.getId() || type == Material.TRAPPED_CHEST.getId()) {
                  Lockette.rotateChestOrientation(block, face);
               }

               if (type != Material.CHEST.getId() && type != Material.TRAPPED_CHEST.getId() && type != Material.DISPENSER.getId() && type != Material.DROPPER.getId() && type != Material.FURNACE.getId() && type != Material.BURNING_FURNACE.getId() && type != Material.BREWING_STAND.getId() && !Lockette.isInList(type, Lockette.customBlockList)) {
                  if (type != Material.DIRT.getId() || !event.hasItem()) {
                     return;
                  }

                  ItemStack item = event.getItem();
                  type = item.getTypeId();
                  if (type != Material.DIAMOND_HOE.getId() && type != Material.GOLD_HOE.getId() && type != Material.IRON_HOE.getId() && type != Material.STONE_HOE.getId() && type != Material.WOOD_HOE.getId()) {
                     return;
                  }

                  Block checkBlock = block.getRelative(BlockFace.UP);
                  type = checkBlock.getTypeId();
                  if (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 107) {
                     event.setUseInteractedBlock(Result.DENY);
                     return;
                  }

                  if (hasAttachedTrapDoor(block)) {
                     event.setUseInteractedBlock(Result.DENY);
                     return;
                  }

                  return;
               }

               if (Lockette.directPlacement && event.hasItem() && face != BlockFace.UP && face != BlockFace.DOWN) {
                  ItemStack item = event.getItem();
                  if (item.getTypeId() == Material.SIGN.getId()) {
                     Block checkBlock = block.getRelative(face);
                     type = checkBlock.getTypeId();
                     if (type == Material.AIR.getId()) {
                        boolean place = false;
                        if (Lockette.isProtected(block)) {
                           if (Lockette.isOwner(block, player.getName())) {
                              place = true;
                           }
                        } else {
                           place = true;
                        }

                        if (place) {
                           event.setUseItemInHand(Result.ALLOW);
                           event.setUseInteractedBlock(Result.DENY);
                           return;
                        }
                     }
                  }
               }

               if (interactContainer(block, player)) {
                  return;
               }

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }

            if (interactDoor(block, player)) {
               return;
            }

            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
         } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (Lockette.protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
               if (interactDoor(block, player)) {
                  return;
               }

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }

            if (Lockette.protectDoors && (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 107)) {
               if (interactDoor(block, player)) {
                  return;
               }

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      plugin.playerList.remove(player.getName());
   }

   private static boolean interactDoor(Block block, Player player) {
      Block signBlock = Lockette.findBlockOwner(block);
      if (signBlock == null) {
         return true;
      } else {
         boolean wooden = block.getTypeId() == Material.WOODEN_DOOR.getId() || block.getTypeId() == Material.FENCE_GATE.getId();
         boolean trap = false;
         if (Lockette.protectTrapDoors && block.getTypeId() == Material.TRAP_DOOR.getId()) {
            wooden = true;
            trap = true;
         }

         boolean allow = false;
         if (canInteract(block, signBlock, player, true)) {
            allow = true;
         }

         if (allow) {
            List<Block> list = Lockette.toggleDoors(block, Lockette.getSignAttachedBlock(signBlock), wooden, trap);
            int delta = Lockette.getSignOption(signBlock, "timer", Lockette.altTimer, Lockette.defaultDoorTimer);
            plugin.doorCloser.add(list, delta != 0, delta);
            return true;
         } else if (block.equals(plugin.playerList.get(player.getName()))) {
            return false;
         } else {
            plugin.playerList.put(player.getName(), block);
            plugin.localizedMessage(player, (String)null, "msg-user-denied-door");
            return false;
         }
      }
   }

   private static void interactSign(Block block, Player player) {
      Sign sign = (Sign)block.getState();
      String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
      if (!text.equals("[private]") && !text.equalsIgnoreCase(Lockette.altPrivate)) {
         if (!text.equals("[more users]") && !text.equalsIgnoreCase(Lockette.altMoreUsers)) {
            return;
         }

         Block checkBlock = Lockette.getSignAttachedBlock(block);
         if (checkBlock == null) {
            return;
         }

         Block signBlock = Lockette.findBlockOwner(checkBlock);
         if (signBlock == null) {
            return;
         }

         sign = (Sign)signBlock.getState();
      }

      int length = player.getName().length();
      if (length > 15) {
         length = 15;
      }

      if (!sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length)) && !Lockette.debugMode) {
         if (!block.equals(plugin.playerList.get(player.getName()))) {
            plugin.playerList.put(player.getName(), block);
            plugin.localizedMessage(player, (String)null, "msg-user-touch-owned", sign.getLine(1));
         }
      } else if (!block.equals(plugin.playerList.get(player.getName()))) {
         plugin.playerList.put(player.getName(), block);
         plugin.localizedMessage(player, (String)null, "msg-help-select");
      }

   }

   private static boolean interactContainer(Block block, Player player) {
      Block signBlock = Lockette.findBlockOwner(block);
      if (signBlock == null) {
         return true;
      } else if (canInteract(block, signBlock, player, false)) {
         return true;
      } else if (block.equals(plugin.playerList.get(player.getName()))) {
         return false;
      } else {
         plugin.playerList.put(player.getName(), block);
         plugin.localizedMessage(player, (String)null, "msg-user-denied");
         return false;
      }
   }

   private static boolean canInteract(Block block, Block signBlock, Player player, boolean isDoor) {
      Sign sign = (Sign)signBlock.getState();
      int length = player.getName().length();
      if (length > 15) {
         length = 15;
      }

      String line = sign.getLine(1).replaceAll("(?i)§[0-F]", "");
      if (line.equals(player.getName().substring(0, length))) {
         return true;
      } else if (plugin.inGroup(block.getWorld(), player, line)) {
         return true;
      } else {
         for(int y = 2; y <= 3; ++y) {
            if (!sign.getLine(y).isEmpty()) {
               line = sign.getLine(y).replaceAll("(?i)§[0-F]", "");
               if (plugin.inGroup(block.getWorld(), player, line)) {
                  return true;
               }

               if (line.equalsIgnoreCase(player.getName().substring(0, length))) {
                  return true;
               }
            }
         }

         List<Block> list = Lockette.findBlockUsers(block, signBlock);
         int count = list.size();

         for(int x = 0; x < count; ++x) {
            Sign sign2 = (Sign)((Block)list.get(x)).getState();

            for(int var15 = 1; var15 <= 3; ++var15) {
               if (!sign2.getLine(var15).isEmpty()) {
                  line = sign2.getLine(var15).replaceAll("(?i)§[0-F]", "");
                  if (plugin.inGroup(block.getWorld(), player, line)) {
                     return true;
                  }

                  if (line.equalsIgnoreCase(player.getName().substring(0, length))) {
                     return true;
                  }
               }
            }
         }

         boolean snoop = false;
         if (isDoor) {
            if (Lockette.adminBypass) {
               if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.bypass")) {
                  snoop = true;
               }

               if (snoop) {
                  Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has bypassed a door owned by " + sign.getLine(1));
                  plugin.localizedMessage(player, (String)null, "msg-admin-bypass", sign.getLine(1));
                  return true;
               }
            }
         } else if (Lockette.adminSnoop) {
            if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.snoop")) {
               snoop = true;
            }

            if (snoop) {
               Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has snooped around in a container owned by " + sign.getLine(1) + "!");
               plugin.localizedMessage(player, Lockette.broadcastSnoopTarget, "msg-admin-snoop", sign.getLine(1));
               return true;
            }
         }

         return false;
      }
   }

   private static boolean fixDoor(Player player) {
      Block block = player.getTargetBlock((HashSet)null, 10);
      int type = block.getTypeId();
      boolean doCheck = false;
      if (Lockette.protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
         doCheck = true;
      }

      if (Lockette.protectDoors && (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == 107)) {
         doCheck = true;
      }

      if (!doCheck) {
         return true;
      } else {
         Block signBlock = Lockette.findBlockOwner(block);
         if (signBlock == null) {
            return true;
         } else {
            Sign sign = (Sign)signBlock.getState();
            int length = player.getName().length();
            if (length > 15) {
               length = 15;
            }

            if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
               Lockette.toggleSingleDoor(block);
               return false;
            } else {
               return true;
            }
         }
      }
   }

   public static boolean hasAttachedTrapDoor(Block block) {
      Block checkBlock = block.getRelative(BlockFace.NORTH);
      int type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         int face = checkBlock.getData() & 3;
         if (face == 2) {
            return true;
         }
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         int face = checkBlock.getData() & 3;
         if (face == 0) {
            return true;
         }
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         int face = checkBlock.getData() & 3;
         if (face == 3) {
            return true;
         }
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         int face = checkBlock.getData() & 3;
         if (face == 1) {
            return true;
         }
      }

      return false;
   }
}
