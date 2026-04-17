package org.yi.acru.bukkit.Lockette;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.yi.acru.bukkit.PluginCore;

public class LocketteBlockListener implements Listener {
   private static Lockette plugin;
   static byte[] faceList = new byte[]{5, 3, 4, 2};
   final int[] materialList;
   final int[] materialListFurnaces;
   final int[] materialListDoors;
   final int[] materialListBad;

   static {
      if (BlockFace.NORTH.getModX() != -1) {
         faceList[0] = 3;
         faceList[1] = 4;
         faceList[2] = 2;
         faceList[3] = 5;
      }

   }

   public LocketteBlockListener(Lockette instance) {
      this.materialList = new int[]{Material.CHEST.getId(), Material.TRAPPED_CHEST.getId(), Material.DISPENSER.getId(), Material.DROPPER.getId(), Material.FURNACE.getId(), Material.BURNING_FURNACE.getId(), Material.BREWING_STAND.getId(), Material.TRAP_DOOR.getId(), Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId()};
      this.materialListFurnaces = new int[]{Material.FURNACE.getId(), Material.BURNING_FURNACE.getId()};
      this.materialListDoors = new int[]{Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId()};
      this.materialListBad = new int[]{50, 63, 64, 65, 68, 71, 75, 76, 96};
      plugin = instance;
   }

   protected void registerEvents() {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      int type = block.getTypeId();
      if (!event.isCancelled() || type == Material.WOODEN_DOOR.getId()) {
         if (type == Material.WALL_SIGN.getId()) {
            if (block.getData() == 0) {
               block.setData((byte)5);
            }

            Sign sign = (Sign)block.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (!text.equals("[private]") && !text.equalsIgnoreCase(Lockette.altPrivate)) {
               if (text.equals("[more users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
                  Block checkBlock = Lockette.getSignAttachedBlock(block);
                  if (checkBlock == null) {
                     return;
                  }

                  Block signBlock = Lockette.findBlockOwner(checkBlock);
                  if (signBlock == null) {
                     return;
                  }

                  Sign sign2 = (Sign)signBlock.getState();
                  int length = player.getName().length();
                  if (length > 15) {
                     length = 15;
                  }

                  if (sign2.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
                     plugin.localizedMessage(player, (String)null, "msg-owner-remove");
                     return;
                  }

                  event.setCancelled(true);
                  sign.update();
                  plugin.localizedMessage(player, (String)null, "msg-user-remove-owned", sign2.getLine(1));
               }
            } else {
               int length = player.getName().length();
               if (length > 15) {
                  length = 15;
               }

               if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
                  Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a container.");
                  plugin.localizedMessage(player, (String)null, "msg-owner-release");
                  return;
               }

               if (Lockette.adminBreak) {
                  boolean snoop = false;
                  if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.break")) {
                     snoop = true;
                  }

                  if (snoop) {
                     Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has broken open a container owned by " + sign.getLine(1) + "!");
                     plugin.localizedMessage(player, Lockette.broadcastBreakTarget, "msg-admin-release", sign.getLine(1));
                     return;
                  }
               }

               event.setCancelled(true);
               sign.update();
               plugin.localizedMessage(player, (String)null, "msg-user-release-owned", sign.getLine(1));
            }
         } else {
            Block signBlock = Lockette.findBlockOwner(block);
            if (signBlock == null) {
               return;
            }

            Sign sign = (Sign)signBlock.getState();
            int length = player.getName().length();
            if (length > 15) {
               length = 15;
            }

            if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
               if (Lockette.findBlockOwnerBreak(block) != null) {
                  Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a container.");
               } else if (type != Material.WOODEN_DOOR.getId()) {
                  Material.IRON_DOOR_BLOCK.getId();
               }

               return;
            }

            event.setCancelled(true);
            plugin.localizedMessage(player, (String)null, "msg-user-break-owned", sign.getLine(1));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      Block block = event.getBlock();
      List<Block> blockList = event.getBlocks();
      int count = blockList.size();

      for(int x = 0; x < count; ++x) {
         Block checkBlock = (Block)blockList.get(x);
         if (Lockette.isProtected(checkBlock)) {
            event.setCancelled(true);
            return;
         }
      }

      Block checkBlock = block.getRelative(Lockette.getPistonFacing(block), event.getLength() + 1);
      if (Lockette.isProtected(checkBlock)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      if (event.isSticky()) {
         Block block = event.getBlock();
         Block checkBlock = block.getRelative(Lockette.getPistonFacing(block), 2);
         int type = checkBlock.getTypeId();
         if (type != Material.CHEST.getId()) {
            if (type != Material.TRAPPED_CHEST.getId()) {
               if (type != Material.DISPENSER.getId()) {
                  if (type != Material.DROPPER.getId()) {
                     if (type != Material.FURNACE.getId()) {
                        if (type != Material.BURNING_FURNACE.getId()) {
                           if (type != Material.WOODEN_DOOR.getId()) {
                              if (type != Material.IRON_DOOR_BLOCK.getId()) {
                                 if (Lockette.isProtected(checkBlock)) {
                                    event.setCancelled(true);
                                 }

                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
         Player player = event.getPlayer();
         Block block = event.getBlockPlaced();
         int type = block.getTypeId();
         Block against = event.getBlockAgainst();
         if (against.getTypeId() == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign)against.getState();
            String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "").toLowerCase();
            if (text.equals("[private]") || text.equalsIgnoreCase(Lockette.altPrivate) || text.equals("[more users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
               event.setCancelled(true);
               return;
            }
         }

         if (type != Material.WOODEN_DOOR.getId() && type != Material.IRON_DOOR_BLOCK.getId() && type != Material.TRAP_DOOR.getId() && type != Material.FENCE_GATE.getId()) {
            if (Lockette.directPlacement && type == Material.WALL_SIGN.getId()) {
               Block checkBlock = Lockette.getSignAttachedBlock(block);
               if (checkBlock != null) {
                  type = checkBlock.getTypeId();
                  if (type == Material.CHEST.getId() || type == Material.TRAPPED_CHEST.getId() || type == Material.DISPENSER.getId() || type == Material.DROPPER.getId() || type == Material.FURNACE.getId() || type == Material.BURNING_FURNACE.getId() || type == Material.BREWING_STAND.getId() || Lockette.isInList(type, Lockette.customBlockList)) {
                     Sign sign = (Sign)block.getState();
                     int length = player.getName().length();
                     if (length > 15) {
                        length = 15;
                     }

                     if (Lockette.isProtected(checkBlock)) {
                        if (Lockette.isOwner(checkBlock, player.getName())) {
                           sign.setLine(0, Lockette.altMoreUsers);
                           sign.setLine(1, Lockette.altEveryone);
                           sign.setLine(2, "");
                           sign.setLine(3, "");
                           sign.update(true);
                           plugin.localizedMessage(player, (String)null, "msg-owner-adduser");
                        } else {
                           event.setCancelled(true);
                        }

                        return;
                     }

                     if (!this.checkPermissions(player, block, checkBlock)) {
                        event.setCancelled(true);
                        plugin.localizedMessage(player, (String)null, "msg-error-permission");
                        return;
                     }

                     sign.setLine(0, Lockette.altPrivate);
                     sign.setLine(1, player.getName().substring(0, length));
                     sign.setLine(2, "");
                     sign.setLine(3, "");
                     sign.update(true);
                     Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has protected a block or door.");
                     plugin.localizedMessage(player, (String)null, "msg-owner-claim");
                  }

               }
            } else {
               if (type == Material.CHEST.getId() || type == Material.TRAPPED_CHEST.getId()) {
                  int chests = Lockette.findChestCountNear(block);
                  if (chests > 1) {
                     event.setCancelled(true);
                     plugin.localizedMessage(player, (String)null, "msg-user-illegal");
                     return;
                  }

                  Block signBlock = Lockette.findBlockOwner(block);
                  if (signBlock != null) {
                     Sign sign = (Sign)signBlock.getState();
                     int length = player.getName().length();
                     if (length > 15) {
                        length = 15;
                     }

                     if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
                        return;
                     }

                     event.setCancelled(true);
                     plugin.localizedMessage(player, (String)null, "msg-user-resize-owned", sign.getLine(1));
                  } else if (plugin.playerList.get(player.getName()) == null) {
                     plugin.playerList.put(player.getName(), block);
                     plugin.localizedMessage(player, (String)null, "msg-help-chest");
                  }
               }

               if (type == Material.HOPPER.getId()) {
                  Block checkBlock = block.getRelative(BlockFace.UP);
                  type = checkBlock.getTypeId();
                  if ((type == Material.CHEST.getId() || type == Material.DISPENSER.getId() || type == Material.DROPPER.getId() || type == Material.FURNACE.getId() || type == Material.BURNING_FURNACE.getId() || type == Material.BREWING_STAND.getId() || Lockette.isInList(type, Lockette.customBlockList)) && !this.validateOwner(checkBlock, player)) {
                     event.setCancelled(true);
                     plugin.localizedMessage(player, (String)null, "msg-user-denied");
                     return;
                  }

                  checkBlock = block.getRelative(BlockFace.DOWN);
                  type = checkBlock.getTypeId();
                  if ((type == Material.CHEST.getId() || type == Material.DISPENSER.getId() || type == Material.DROPPER.getId() || type == Material.FURNACE.getId() || type == Material.BURNING_FURNACE.getId() || type == Material.BREWING_STAND.getId() || Lockette.isInList(type, Lockette.customBlockList)) && !this.validateOwner(checkBlock, player)) {
                     event.setCancelled(true);
                     plugin.localizedMessage(player, (String)null, "msg-user-denied");
                     return;
                  }
               }

            }
         } else if (!canBuildDoor(block, against, player)) {
            event.setCancelled(true);
            plugin.localizedMessage(player, (String)null, "msg-user-conflict-door");
         }
      }
   }

   private boolean checkPermissions(Player player, Block block, Block checkBlock) {
      int type = checkBlock.getTypeId();
      if (plugin.usingExternalZones()) {
         if (!plugin.canBuild(player, block)) {
            plugin.localizedMessage(player, (String)null, "msg-error-zone", PluginCore.lastZoneDeny());
            return false;
         }

         if (!plugin.canBuild(player, checkBlock)) {
            plugin.localizedMessage(player, (String)null, "msg-error-zone", PluginCore.lastZoneDeny());
            return false;
         }
      }

      if (!plugin.usingExternalPermissions()) {
         return true;
      } else {
         boolean create = false;
         if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all")) {
            create = true;
         } else if (type == Material.CHEST.getId()) {
            if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest")) {
               create = true;
            }
         } else if (type != Material.FURNACE.getId() && type != Material.BURNING_FURNACE.getId()) {
            if (type == Material.DISPENSER.getId()) {
               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser")) {
                  create = true;
               }
            } else if (type == Material.DROPPER.getId()) {
               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dropper")) {
                  create = true;
               }
            } else if (type == Material.BREWING_STAND.getId()) {
               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand")) {
                  create = true;
               }
            } else if (Lockette.isInList(type, Lockette.customBlockList) && plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom")) {
               create = true;
            }
         } else if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace")) {
            create = true;
         }

         return create;
      }
   }

   private boolean validateOwner(Block block, Player player) {
      Block signBlock = Lockette.findBlockOwner(block);
      if (signBlock == null) {
         return true;
      } else {
         Sign sign = (Sign)signBlock.getState();
         int length = player.getName().length();
         if (length > 15) {
            length = 15;
         }

         return sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length));
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockRedstoneChange(BlockRedstoneEvent event) {
      Block block = event.getBlock();
      int type = block.getTypeId();
      boolean doCheck = false;
      if (Lockette.protectTrapDoors && type == Material.TRAP_DOOR.getId()) {
         doCheck = true;
      }

      if (Lockette.protectDoors && (type == Material.WOODEN_DOOR.getId() || type == Material.IRON_DOOR_BLOCK.getId() || type == Material.FENCE_GATE.getId())) {
         doCheck = true;
      }

      if (doCheck) {
         Block signBlock = Lockette.findBlockOwner(block);
         if (signBlock == null) {
            return;
         }

         Sign sign = (Sign)signBlock.getState();

         for(int y = 1; y <= 3; ++y) {
            if (!sign.getLine(y).isEmpty()) {
               String line = sign.getLine(y).replaceAll("(?i)§[0-F]", "");
               if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone)) {
                  return;
               }
            }
         }

         List<Block> list = Lockette.findBlockUsers(block, signBlock);
         int count = list.size();

         for(int x = 0; x < count; ++x) {
            sign = (Sign)((Block)list.get(x)).getState();

            for(int var14 = 1; var14 <= 3; ++var14) {
               if (!sign.getLine(var14).isEmpty()) {
                  String line = sign.getLine(var14).replaceAll("(?i)§[0-F]", "");
                  if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone)) {
                     return;
                  }
               }
            }
         }

         event.setNewCurrent(event.getOldCurrent());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      boolean typeWallSign = block.getTypeId() == Material.WALL_SIGN.getId();
      boolean typeSignPost = block.getTypeId() == Material.SIGN_POST.getId();
      if (typeWallSign) {
         Sign sign = (Sign)block.getState();
         String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "");
         if ((text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate) || text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) && event.isCancelled()) {
            return;
         }
      } else if (!typeSignPost) {
         event.setCancelled(true);
         return;
      }

      String text = event.getLine(0).replaceAll("(?i)§[0-F]", "");
      if (!text.equalsIgnoreCase("[Private]") && !text.equalsIgnoreCase(Lockette.altPrivate)) {
         if (text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
            Block[] checkBlock = new Block[4];
            Block signBlock = null;
            Sign sign = null;
            byte face = 0;
            int length = player.getName().length();
            if (length > 15) {
               length = 15;
            }

            if ((Lockette.protectDoors || Lockette.protectTrapDoors) && typeWallSign) {
               checkBlock[0] = Lockette.getSignAttachedBlock(block);
               if (checkBlock[0] != null && !this.isInList(checkBlock[0].getTypeId(), this.materialListBad)) {
                  signBlock = Lockette.findBlockOwner(checkBlock[0]);
                  if (signBlock != null) {
                     sign = (Sign)signBlock.getState();
                     if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
                        face = block.getData();
                     }
                  }
               }
            }

            if (face == 0) {
               checkBlock[0] = block.getRelative(BlockFace.NORTH);
               checkBlock[1] = block.getRelative(BlockFace.EAST);
               checkBlock[2] = block.getRelative(BlockFace.SOUTH);
               checkBlock[3] = block.getRelative(BlockFace.WEST);

               for(int x = 0; x < 4; ++x) {
                  if (this.isInList(checkBlock[x].getTypeId(), this.materialList) && (Lockette.protectTrapDoors || checkBlock[x].getTypeId() != Material.TRAP_DOOR.getId()) && (Lockette.protectDoors || !this.isInList(checkBlock[x].getTypeId(), this.materialListDoors))) {
                     signBlock = Lockette.findBlockOwner(checkBlock[x]);
                     if (signBlock != null) {
                        sign = (Sign)signBlock.getState();
                        if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
                           face = faceList[x];
                           break;
                        }
                     }
                  }
               }
            }

            if (face == 0) {
               event.setLine(0, "[?]");
               if (sign != null) {
                  plugin.localizedMessage(player, (String)null, "msg-error-adduser-owned", sign.getLine(1));
               } else {
                  plugin.localizedMessage(player, (String)null, "msg-error-adduser");
               }

               return;
            }

            event.setCancelled(false);
            if (!typeWallSign) {
               block.setType(Material.WALL_SIGN);
               block.setData(face);
               sign = (Sign)block.getState();
               sign.setLine(0, event.getLine(0));
               sign.setLine(1, event.getLine(1));
               sign.setLine(2, event.getLine(2));
               sign.setLine(3, event.getLine(3));
               sign.update(true);
            } else {
               block.setData(face);
            }

            plugin.localizedMessage(player, (String)null, "msg-owner-adduser");
         }
      } else {
         boolean doChests = true;
         boolean doFurnaces = true;
         boolean doDispensers = true;
         boolean doDroppers = true;
         boolean doBrewingStands = true;
         boolean doCustoms = true;
         boolean doTrapDoors = true;
         boolean doDoors = true;
         if (plugin.usingExternalZones() && !plugin.canBuild(player, block)) {
            event.setLine(0, "[?]");
            plugin.localizedMessage(player, (String)null, "msg-error-zone", PluginCore.lastZoneDeny());
            return;
         }

         if (plugin.usingExternalPermissions()) {
            boolean create = false;
            doChests = false;
            doFurnaces = false;
            doDispensers = false;
            doDroppers = false;
            doBrewingStands = false;
            doCustoms = false;
            doTrapDoors = false;
            doDoors = false;
            if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all")) {
               create = true;
               doChests = true;
               doFurnaces = true;
               doDispensers = true;
               doDroppers = true;
               doBrewingStands = true;
               doCustoms = true;
               doTrapDoors = true;
               doDoors = true;
            } else {
               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest")) {
                  create = true;
                  doChests = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace")) {
                  create = true;
                  doFurnaces = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser")) {
                  create = true;
                  doDispensers = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dropper")) {
                  create = true;
                  doDroppers = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand")) {
                  create = true;
                  doBrewingStands = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom")) {
                  create = true;
                  doCustoms = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.trapdoor")) {
                  create = true;
                  doTrapDoors = true;
               }

               if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.door")) {
                  create = true;
                  doDoors = true;
               }
            }

            if (!create) {
               event.setLine(0, "[?]");
               plugin.localizedMessage(player, (String)null, "msg-error-permission");
               return;
            }
         }

         Block[] checkBlock = new Block[4];
         byte face = 0;
         int type = 0;
         boolean conflict = false;
         boolean deny = false;
         boolean zonedeny = false;
         if (Lockette.protectTrapDoors && typeWallSign) {
            checkBlock[3] = Lockette.getSignAttachedBlock(block);
            if (checkBlock[3] != null && !this.isInList(checkBlock[3].getTypeId(), this.materialListBad)) {
               checkBlock[0] = checkBlock[3].getRelative(BlockFace.NORTH);
               checkBlock[1] = checkBlock[3].getRelative(BlockFace.EAST);
               checkBlock[2] = checkBlock[3].getRelative(BlockFace.SOUTH);
               checkBlock[3] = checkBlock[3].getRelative(BlockFace.WEST);

               for(int x = 0; x < 4; ++x) {
                  if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId() && Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
                     if (doTrapDoors) {
                        face = block.getData();
                        type = 4;
                        break;
                     }

                     deny = true;
                  }
               }
            }
         }

         if (Lockette.protectDoors && typeWallSign) {
            checkBlock[0] = Lockette.getSignAttachedBlock(block);
            if (checkBlock[0] != null && !this.isInList(checkBlock[0].getTypeId(), this.materialListBad)) {
               checkBlock[1] = checkBlock[0].getRelative(BlockFace.UP);
               checkBlock[2] = checkBlock[0].getRelative(BlockFace.DOWN);
               if (this.isInList(checkBlock[1].getTypeId(), this.materialListDoors)) {
                  if (Lockette.findBlockOwner(checkBlock[1], block, true) == null) {
                     if (this.isInList(checkBlock[2].getTypeId(), this.materialListDoors)) {
                        if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
                           if (!doDoors) {
                              deny = true;
                           } else {
                              face = block.getData();
                              type = 5;
                           }
                        } else {
                           conflict = true;
                        }
                     } else if (!doDoors) {
                        deny = true;
                     } else {
                        face = block.getData();
                        type = 5;
                     }
                  } else {
                     conflict = true;
                  }
               } else if (this.isInList(checkBlock[2].getTypeId(), this.materialListDoors)) {
                  if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
                     if (!doDoors) {
                        deny = true;
                     } else {
                        face = block.getData();
                        type = 5;
                     }
                  } else {
                     conflict = true;
                  }
               }
            }
         }

         if (conflict) {
            face = 0;
            type = 0;
         }

         if (face == 0) {
            checkBlock[0] = block.getRelative(BlockFace.NORTH);
            checkBlock[1] = block.getRelative(BlockFace.EAST);
            checkBlock[2] = block.getRelative(BlockFace.SOUTH);
            checkBlock[3] = block.getRelative(BlockFace.WEST);

            for(int x = 0; x < 4; ++x) {
               if (plugin.usingExternalZones() && !plugin.canBuild(player, checkBlock[x])) {
                  zonedeny = true;
               } else {
                  int lastType;
                  if (checkBlock[x].getTypeId() != Material.CHEST.getId() && checkBlock[x].getTypeId() != Material.TRAPPED_CHEST.getId()) {
                     if (this.isInList(checkBlock[x].getTypeId(), this.materialListFurnaces)) {
                        if (!doFurnaces) {
                           deny = true;
                           continue;
                        }

                        lastType = 2;
                     } else if (checkBlock[x].getTypeId() == Material.DISPENSER.getId()) {
                        if (!doDispensers) {
                           deny = true;
                           continue;
                        }

                        lastType = 3;
                     } else if (checkBlock[x].getTypeId() == Material.DROPPER.getId()) {
                        if (!doDroppers) {
                           deny = true;
                           continue;
                        }

                        lastType = 8;
                     } else if (checkBlock[x].getTypeId() == Material.BREWING_STAND.getId()) {
                        if (!doBrewingStands) {
                           deny = true;
                           continue;
                        }

                        lastType = 6;
                     } else if (Lockette.isInList(checkBlock[x].getTypeId(), Lockette.customBlockList)) {
                        if (!doCustoms) {
                           deny = true;
                           continue;
                        }

                        lastType = 7;
                     } else if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId()) {
                        if (!Lockette.protectTrapDoors) {
                           continue;
                        }

                        if (!doTrapDoors) {
                           deny = true;
                           continue;
                        }

                        lastType = 4;
                     } else {
                        if (!this.isInList(checkBlock[x].getTypeId(), this.materialListDoors) || !Lockette.protectDoors) {
                           continue;
                        }

                        if (!doDoors) {
                           deny = true;
                           continue;
                        }

                        lastType = 5;
                     }
                  } else {
                     if (!doChests) {
                        deny = true;
                        continue;
                     }

                     lastType = 1;
                  }

                  if (Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
                     face = faceList[x];
                     type = lastType;
                     break;
                  }

                  if (Lockette.protectTrapDoors && doTrapDoors && checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId()) {
                     conflict = true;
                  }

                  if (Lockette.protectDoors && doDoors && this.isInList(checkBlock[x].getTypeId(), this.materialListDoors)) {
                     conflict = true;
                  }
               }
            }
         }

         if (face == 0) {
            event.setLine(0, "[?]");
            if (conflict) {
               plugin.localizedMessage(player, (String)null, "msg-error-claim-conflict");
            } else if (zonedeny) {
               plugin.localizedMessage(player, (String)null, "msg-error-zone", PluginCore.lastZoneDeny());
            } else if (deny) {
               plugin.localizedMessage(player, (String)null, "msg-error-permission");
            } else {
               plugin.localizedMessage(player, (String)null, "msg-error-claim");
            }

            return;
         }

         boolean anyone = true;
         int length = player.getName().length();
         if (event.getLine(1).isEmpty()) {
            anyone = false;
         }

         if (length > 15) {
            length = 15;
         }

         event.setCancelled(false);
         if (anyone) {
            if (type == 1) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.chest")) {
                  anyone = false;
               }
            } else if (type == 2) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.furnace")) {
                  anyone = false;
               }
            } else if (type == 3) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.dispenser")) {
                  anyone = false;
               }
            } else if (type == 8) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.dropper")) {
                  anyone = false;
               }
            } else if (type == 6) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.brewingstand")) {
                  anyone = false;
               }
            } else if (type == 7) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.custom")) {
                  anyone = false;
               }
            } else if (type == 4) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.trapdoor")) {
                  anyone = false;
               }
            } else if (type == 5) {
               if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.door")) {
                  anyone = false;
               }
            } else {
               anyone = false;
            }
         }

         if (!anyone) {
            event.setLine(1, player.getName().substring(0, length));
         }

         if (!typeWallSign) {
            block.setType(Material.WALL_SIGN);
            block.setData(face);
            Sign sign = (Sign)block.getState();
            sign.setLine(0, event.getLine(0));
            sign.setLine(1, event.getLine(1));
            sign.setLine(2, event.getLine(2));
            sign.setLine(3, event.getLine(3));
            sign.update(true);
         } else {
            block.setData(face);
         }

         if (anyone) {
            Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has claimed a container for " + event.getLine(1) + ".");
            if (!plugin.playerOnline(event.getLine(1))) {
               plugin.localizedMessage(player, (String)null, "msg-admin-claim-error", event.getLine(1));
            } else {
               plugin.localizedMessage(player, (String)null, "msg-admin-claim", event.getLine(1));
            }
         } else {
            Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has claimed a container.");
            plugin.localizedMessage(player, (String)null, "msg-owner-claim");
         }
      }

   }

   private static boolean canBuildDoor(Block block, Block against, Player player) {
      if (!Lockette.isOwner(against, player.getName())) {
         return false;
      } else if (Lockette.protectTrapDoors && block.getTypeId() == Material.TRAP_DOOR.getId()) {
         return true;
      } else if (!Lockette.isOwner(against.getRelative(BlockFace.UP, 3), player.getName())) {
         return false;
      } else {
         Block checkBlock = block.getRelative(BlockFace.NORTH);
         if (checkBlock.getTypeId() == block.getTypeId() && !Lockette.isOwner(checkBlock, player.getName())) {
            return false;
         } else {
            checkBlock = block.getRelative(BlockFace.EAST);
            if (checkBlock.getTypeId() == block.getTypeId() && !Lockette.isOwner(checkBlock, player.getName())) {
               return false;
            } else {
               checkBlock = block.getRelative(BlockFace.SOUTH);
               if (checkBlock.getTypeId() == block.getTypeId() && !Lockette.isOwner(checkBlock, player.getName())) {
                  return false;
               } else {
                  checkBlock = block.getRelative(BlockFace.WEST);
                  return checkBlock.getTypeId() != block.getTypeId() || Lockette.isOwner(checkBlock, player.getName());
               }
            }
         }
      }
   }

   private boolean isInList(int target, int[] list) {
      if (list == null) {
         return false;
      } else {
         for(int x = 0; x < list.length; ++x) {
            if (target == list[x]) {
               return true;
            }
         }

         return false;
      }
   }
}
