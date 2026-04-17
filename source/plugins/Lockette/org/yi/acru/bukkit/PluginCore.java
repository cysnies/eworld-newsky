package org.yi.acru.bukkit;

import com.gmail.nossr50.api.PartyAPI;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.register.payment.Methods;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.platymuus.bukkit.permissions.Group;
import de.bananaco.bpermissions.api.WorldManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public abstract class PluginCore extends JavaPlugin {
   private static final String coreVersion = "1.3.7";
   public static final Logger log = Logger.getLogger("Minecraft");
   private static boolean registered = false;
   private final PluginCoreServerListener serverListener = new PluginCoreServerListener(this);
   private static int useExternalGroups = 0;
   private static int useExternalPermissions = 0;
   private static int useExternalZones = 0;
   private static int useExternalEconomy = 0;
   private static String lastZoneDeny = "lockette.unknown";
   private static List linkList = null;
   private static PluginCoreLink linkSuperPerms = null;
   private static PluginCoreLink linkGroupManager = null;
   private static PluginCoreLink linkPermsBukkit = null;
   private static PluginCoreLink linkPermissionsEx = null;
   private static PluginCoreLink linkBPermissions = null;
   private static PluginCoreLink linkTowny = null;
   private static PluginCoreLink linkSimpleClans = null;
   private static PluginCoreLink linkMcmmo = null;
   private static PluginCoreLink linkFactions = null;
   private static PluginCoreLink linkLWC = null;
   private static PluginCoreLink linkRegister = null;
   private static PluginCoreLink linkPermissions = null;
   private static boolean permissionsWorld = false;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$org$yi$acru$bukkit$PluginCoreLink$LinkType;

   public void onEnable() {
      if (!registered) {
         this.serverListener.registerEvents();
         registered = true;
      }

      if (linkList != null) {
         linkList.clear();
         linkList = null;
         useExternalGroups = 0;
         useExternalPermissions = 0;
         useExternalZones = 0;
         useExternalEconomy = 0;
      }

      linkList = new ArrayList(10);
      linkSuperPerms = this.linkInternalPerms();
      linkGroupManager = this.linkExternalPlugin("GroupManager", PluginCoreLink.LinkType.GroupManager);
      linkPermsBukkit = this.linkExternalPlugin("PermissionsBukkit", PluginCoreLink.LinkType.GROUPS_PERMISSIONS);
      linkPermissionsEx = this.linkExternalPlugin("PermissionsEx", PluginCoreLink.LinkType.GROUPS_PERMISSIONS);
      linkBPermissions = this.linkExternalPlugin("bPermissions", PluginCoreLink.LinkType.GROUPS_PERMISSIONS);
      linkTowny = this.linkExternalPlugin("Towny", PluginCoreLink.LinkType.GROUPS_ZONES);
      linkSimpleClans = this.linkExternalPlugin("SimpleClans", PluginCoreLink.LinkType.GROUPS);
      linkMcmmo = this.linkExternalPlugin("mcMMO", PluginCoreLink.LinkType.GROUPS);
      linkFactions = this.linkExternalPlugin("Factions", PluginCoreLink.LinkType.GROUPS);
      linkLWC = this.linkExternalPlugin("LWC", PluginCoreLink.LinkType.ZONES);
      linkRegister = this.linkExternalPlugin("Register", PluginCoreLink.LinkType.ECONOMY);
      linkPermissions = this.linkExternalPlugin("Permissions", PluginCoreLink.LinkType.Permissions);
      if (this.usingExternalPermissions()) {
         log.info("[" + this.getDescription().getName() + "] Using linked plugin for admin permissions.");
      } else {
         log.info("[" + this.getDescription().getName() + "] Using ops file for admin permissions.");
      }

   }

   public void onDisable() {
      if (linkList != null) {
         linkList.clear();
         linkList = null;
         useExternalGroups = 0;
         useExternalPermissions = 0;
         useExternalZones = 0;
         useExternalEconomy = 0;
      }

   }

   public static String getCoreVersion() {
      return "1.3.7";
   }

   public static String lastZoneDeny() {
      return lastZoneDeny;
   }

   public void dumpCoreInfo() {
      log.info("[" + this.getDescription().getName() + "] Dumping core information:");
      log.info("Number of linked group plugins: " + useExternalGroups);
      log.info("Number of linked permission plugins: " + useExternalPermissions);
      log.info("Number of linked zone plugins: " + useExternalZones);
      log.info("Number of linked economy plugins: " + useExternalEconomy);
      if (linkSuperPerms.isEnabled()) {
         Player[] players = this.getServer().getOnlinePlayers();
         log.info("Superperms is available, " + players.length + " players online.");

         for(Player player : players) {
            log.info("Player: " + player.getName() + " has the following permissions:");

            for(PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
               log.info("    " + perm.getPermission() + " = " + perm.getValue());
            }
         }
      } else {
         log.info("Superperms is unavailable.");
      }

   }

   private PluginCoreLink linkInternalPerms() {
      PluginCoreLink link = new PluginCoreLink((Plugin)null, PluginCoreLink.LinkType.PERMISSIONS);

      try {
         Class[] args = new Class[]{String.class};
         Player.class.getMethod("hasPermission", args);
      } catch (Throwable var3) {
         return link;
      }

      link.setEnabled(true);
      return link;
   }

   private PluginCoreLink linkExternalPlugin(String pluginName, PluginCoreLink.LinkType handler) {
      Plugin plugin = this.getServer().getPluginManager().getPlugin(pluginName);
      PluginCoreLink link = new PluginCoreLink(plugin, handler);
      if (plugin == null) {
         return link;
      } else if (linkList == null) {
         return link;
      } else {
         link.setLinked(true);
         linkList.add(0, link);
         if (this.getServer().getPluginManager().isPluginEnabled(plugin)) {
            this.setLink(link.getPluginName(), true, false);
         }

         return link;
      }
   }

   public void setLink(String pluginName, boolean enable, boolean listener) {
      if (linkList != null) {
         boolean external = this.usingExternalPermissions();
         int groups = useExternalGroups;
         int perms = useExternalPermissions;
         int zones = useExternalZones;
         int economy = useExternalEconomy;
         boolean result = true;
         int difference;
         if (enable) {
            difference = 1;
         } else {
            difference = -1;
         }

         for(PluginCoreLink link : linkList) {
            if (pluginName.equals(link.getPluginName())) {
               if (!link.isLinked()) {
                  return;
               }

               switch (link.getType()) {
                  case GROUPS:
                  case GROUPS_PERMISSIONS:
                  case GROUPS_ZONES:
                  case GROUPS_PERMISSIONS_ZONES:
                     useExternalGroups += difference;
                  case PERMISSIONS:
                  case ZONES:
                  case ECONOMY:
                  case PERMISSIONS_ZONES:
                  default:
                     switch (link.getType()) {
                        case PERMISSIONS:
                        case GROUPS_PERMISSIONS:
                        case PERMISSIONS_ZONES:
                        case GROUPS_PERMISSIONS_ZONES:
                           useExternalPermissions += difference;
                        case ZONES:
                        case ECONOMY:
                        case GROUPS_ZONES:
                        default:
                           switch (link.getType()) {
                              case ZONES:
                              case GROUPS_ZONES:
                              case PERMISSIONS_ZONES:
                              case GROUPS_PERMISSIONS_ZONES:
                                 useExternalZones += difference;
                              case ECONOMY:
                              case GROUPS_PERMISSIONS:
                              default:
                                 switch (link.getType()) {
                                    case ECONOMY:
                                       useExternalEconomy += difference;
                                    default:
                                       switch (link.getType()) {
                                          case GroupManager:
                                             result = this.enableLinkGroupManager(link, enable, difference);
                                             break;
                                          case Permissions:
                                             result = this.enableLinkPermissions(link, enable, difference);
                                             break;
                                          default:
                                             link.setEnabled(enable);
                                       }

                                       if (enable) {
                                          if (result) {
                                             String changed = "";
                                             if (groups != useExternalGroups) {
                                                changed = changed + "/Groups";
                                             }

                                             if (this.usingExternalPermissions() && perms != useExternalPermissions) {
                                                changed = changed + "/Permissions";
                                             }

                                             if (zones != useExternalZones) {
                                                changed = changed + "/Zones";
                                             }

                                             if (economy != useExternalEconomy) {
                                                changed = changed + "/Economy";
                                             }

                                             if (changed.startsWith("/")) {
                                                changed = changed.substring(1);
                                             } else if (!this.usingExternalPermissions()) {
                                                changed = "NO REASON (permissions disabled)";
                                             } else {
                                                changed = "NO REASON";
                                             }

                                             log.info("[" + this.getDescription().getName() + "] Enabled link to plugin " + link.getPluginName() + " for " + changed + ", version " + link.getPluginVersion());
                                             if (listener && !external && this.usingExternalPermissions()) {
                                                log.info("[" + this.getDescription().getName() + "] Ops file overridden, using linked plugin for admin permissions.");
                                             }
                                          } else if (!link.isLinked()) {
                                             log.info("[" + this.getDescription().getName() + "] Ignoring fake Permissions plugin version " + link.getPluginVersion());
                                          } else {
                                             log.warning("[" + this.getDescription().getName() + "] Failed to enable link to plugin" + link.getPluginName() + ", version " + link.getPluginVersion() + "!");
                                          }
                                       } else if (result) {
                                          log.info("[" + this.getDescription().getName() + "] Disabled link to plugin " + link.getPluginName() + ", version " + link.getPluginVersion());
                                       } else {
                                          log.warning("[" + this.getDescription().getName() + "] Failed to disable link to plugin " + link.getPluginName() + ", version " + link.getPluginVersion() + "!");
                                       }

                                       return;
                                 }
                           }
                     }
               }
            }
         }

      }
   }

   protected boolean enableLinkGroupManager(PluginCoreLink link, boolean enable, int difference) {
      if (enable) {
         try {
            Method getWho = GroupManager.class.getMethod("getWorldsHolder", (Class[])null);
            link.setData(getWho.invoke(link.getGroupManager(), (Object[])null));
         } catch (Throwable var5) {
            return false;
         }
      }

      useExternalGroups += difference;
      useExternalPermissions += difference;
      link.setEnabled(enable);
      return true;
   }

   protected boolean enableLinkPermissions(PluginCoreLink link, boolean enable, int difference) {
      boolean usePerms = true;
      if (linkBPermissions != null && linkBPermissions.isLinked()) {
         usePerms = false;
      }

      if (enable) {
         try {
            Class.forName("org.anjocaido.groupmanager.permissions.NijikoPermissionsProxy");
            link.setLinked(false);
            return false;
         } catch (Throwable var10) {
            try {
               Class.forName("com.platymuus.bukkit.permcompat.PermissionHandler");
               link.setLinked(false);
               return false;
            } catch (Throwable var9) {
               try {
                  Class.forName("ru.tehkode.permissions.compat.P2Group");
                  link.setLinked(false);
                  return false;
               } catch (Throwable var8) {
                  try {
                     link.setData(link.getPermissions().getHandler());
                  } catch (Throwable var7) {
                     return false;
                  }

                  permissionsWorld = false;

                  try {
                     Class[] args = new Class[]{String.class, String.class, String.class};
                     PermissionHandler.class.getMethod("inGroup", args);
                     permissionsWorld = true;
                  } catch (Throwable var6) {
                  }
               }
            }
         }
      }

      useExternalGroups += difference;
      if (usePerms) {
         useExternalPermissions += difference;
      }

      link.setEnabled(enable);
      return true;
   }

   protected boolean usingExternalGroups() {
      return useExternalGroups > 0;
   }

   protected boolean usingExternalPermissions() {
      return useExternalPermissions > 0;
   }

   protected boolean usingExternalZones() {
      return useExternalZones > 0;
   }

   protected boolean usingExternalEconomy() {
      return useExternalEconomy > 0;
   }

   protected String getLocalizedEveryone() {
      return null;
   }

   protected String getLocalizedOperators() {
      return null;
   }

   public boolean inGroup(World world, Player player, String groupName) {
      return this.inGroup(world, player, player.getName(), groupName);
   }

   public boolean inGroup(World world, String playerName, String groupName) {
      return this.inGroup(world, this.getServer().getPlayer(playerName), playerName, groupName);
   }

   private boolean inGroup(World world, Player player, String playerName, String groupName) {
      if (groupName.equalsIgnoreCase("[Everyone]")) {
         return true;
      } else {
         String local = this.getLocalizedEveryone();
         if (local != null && groupName.equalsIgnoreCase(local)) {
            return true;
         } else {
            if (player != null && player.isOp()) {
               if (groupName.equalsIgnoreCase("[Operators]")) {
                  return true;
               }

               local = this.getLocalizedOperators();
               if (local != null && groupName.equalsIgnoreCase(local)) {
                  return true;
               }
            }

            if (!this.usingExternalGroups()) {
               return false;
            } else {
               boolean result = false;
               int end = groupName.length() - 1;
               if (end >= 2 && groupName.charAt(0) == '[' && groupName.charAt(end) == ']') {
                  if (linkPermsBukkit.isEnabled()) {
                     Group group = linkPermsBukkit.getPermsBukkit().getGroup(groupName.substring(1, end));
                     if (group != null) {
                        List<Group> membership = linkPermsBukkit.getPermsBukkit().getGroups(playerName);
                        if (membership != null) {
                           int count = membership.size();

                           for(int x = 0; x < count; ++x) {
                              if (((Group)membership.get(x)).equals(group)) {
                                 return true;
                              }
                           }
                        }
                     }
                  }

                  if (linkPermissionsEx.isEnabled()) {
                     result = PermissionsEx.getUser(playerName).inGroup(groupName.substring(1, end), world.getName());
                     if (result) {
                        return true;
                     }
                  }

                  if (linkBPermissions.isEnabled()) {
                     result = WorldManager.getInstance().getWorld(world.getName()).getUser(playerName).hasGroupRecursive(groupName.substring(1, end).toLowerCase());
                     if (result) {
                        return true;
                     }
                  }

                  if (linkGroupManager.isEnabled()) {
                     result = linkGroupManager.getWorldsHolder().getWorldPermissions(world.getName()).inGroup(playerName, groupName.substring(1, end));
                     if (result) {
                        return true;
                     }
                  }

                  if (linkTowny.isEnabled()) {
                     try {
                        Resident resident = TownyUniverse.getDataSource().getResident(playerName);

                        try {
                           Town town = resident.getTown();
                           if (town.getName().equalsIgnoreCase(groupName.substring(1, end))) {
                              return true;
                           }

                           try {
                              Nation nation = town.getNation();
                              if (nation.getName().equalsIgnoreCase(groupName.substring(1, end))) {
                                 return true;
                              }
                           } catch (Throwable var12) {
                           }
                        } catch (Throwable var13) {
                        }
                     } catch (Throwable var14) {
                     }
                  }

                  if (linkSimpleClans.isEnabled()) {
                     Clan clan = linkSimpleClans.getSimpleClans().getClanManager().getClanByPlayerName(playerName);
                     if (clan != null) {
                        if (clan.getName().equalsIgnoreCase(groupName.substring(1, end))) {
                           return true;
                        }

                        if (clan.getTag().equalsIgnoreCase(groupName.substring(1, end))) {
                           return true;
                        }
                     }
                  }

                  if (linkMcmmo.isEnabled() && player != null && PartyAPI.inParty(player) && PartyAPI.getPartyName(player).equalsIgnoreCase(groupName.substring(1, end))) {
                     return true;
                  }

                  if (linkFactions.isEnabled() && player != null) {
                     String tag = linkFactions.getFactions().getPlayerFactionTag(player);
                     if (tag != null && tag.equalsIgnoreCase(groupName.substring(1, end))) {
                        return true;
                     }
                  }

                  if (linkPermissions.isEnabled()) {
                     if (permissionsWorld) {
                        result = linkPermissions.getPermissionHandler().inGroup(world.getName(), playerName, groupName.substring(1, end));
                     } else {
                        result = linkPermissions.getPermissionHandler().inGroup(player.getWorld().getName(), playerName, groupName.substring(1, end));
                     }

                     if (result) {
                        return true;
                     }
                  }
               }

               return false;
            }
         }
      }
   }

   public boolean hasPermission(World world, String playerName, String permissionNode) {
      return this.hasPermission(world, this.getServer().getPlayer(playerName), permissionNode);
   }

   public boolean hasPermission(World world, Player player, String permissionNode) {
      if (player == null) {
         return false;
      } else if (!this.usingExternalPermissions()) {
         return player.isOp();
      } else {
         boolean result = false;
         if (linkSuperPerms.isEnabled() && player != null) {
            result = player.hasPermission(permissionNode);
            if (result) {
               return true;
            }
         }

         if (linkGroupManager.isEnabled()) {
            result = linkGroupManager.getWorldsHolder().getWorldPermissions(world.getName()).has(player, permissionNode);
            if (result) {
               return true;
            }
         }

         if (linkPermissions.isEnabled()) {
            result = linkPermissions.getPermissionHandler().has(player, permissionNode);
            if (result) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean canBuild(String playerName, Block block) {
      return this.canBuild(this.getServer().getPlayer(playerName), block);
   }

   public boolean canBuild(Player player, Block block) {
      lastZoneDeny = "noone";
      if (!this.usingExternalZones()) {
         return true;
      } else {
         if (linkTowny.isEnabled()) {
            try {
               if (TownyUniverse.getDataSource().getWorld(block.getWorld().getName()).isUsingTowny() && TownyUniverse.isWilderness(block) && this.usingExternalPermissions() && !this.hasPermission(block.getWorld(), player, "lockette.towny.wilds")) {
                  lastZoneDeny = "towny.wilds";
                  return false;
               }
            } catch (Exception var5) {
            }
         }

         if (linkLWC.isEnabled()) {
            LWC lwc = linkLWC.getLWCPlugin().getLWC();
            Protection protection = lwc.findProtection(block);
            if (protection != null && !lwc.canAdminProtection(player, protection)) {
               lastZoneDeny = "lwc.protection";
               return false;
            }
         }

         return true;
      }
   }

   public String economyFormat(double amount) {
      if (!this.usingExternalEconomy()) {
         return Double.toString(amount);
      } else {
         return linkRegister.isEnabled() && Methods.hasMethod() ? Methods.getMethod().format(amount) : Double.toString(amount);
      }
   }

   public boolean economyTransfer(String source, String destination, double amount) {
      if (!this.usingExternalEconomy()) {
         return false;
      } else {
         if (linkRegister.isEnabled() && Methods.hasMethod() && Methods.getMethod().hasAccount(source)) {
            com.nijikokun.register.payment.Method.MethodAccount sourceAccount = Methods.getMethod().getAccount(source);
            if (sourceAccount.hasEnough(amount)) {
               if (!Methods.getMethod().hasAccount(destination)) {
                  Methods.getMethod().createAccount(destination);
               }

               if (Methods.getMethod().hasAccount(destination)) {
                  com.nijikokun.register.payment.Method.MethodAccount destinationAccount = Methods.getMethod().getAccount(destination);
                  if (sourceAccount.subtract(amount)) {
                     if (destinationAccount.add(amount)) {
                        return true;
                     }

                     sourceAccount.add(amount);
                  }
               }
            }
         }

         return false;
      }
   }

   protected void selectiveBroadcast(String target, String message) {
      if (target != null) {
         if (!target.isEmpty()) {
            if (message != null) {
               if (!message.isEmpty()) {
                  Player[] players = this.getServer().getOnlinePlayers();
                  if (target.charAt(0) == '[') {
                     for(int x = 0; x < players.length; ++x) {
                        if (this.inGroup(players[x].getWorld(), players[x], target)) {
                           players[x].sendMessage(message);
                        }
                     }
                  } else {
                     for(int x = 0; x < players.length; ++x) {
                        if (target.equalsIgnoreCase(players[x].getName())) {
                           players[x].sendMessage(message);
                        }
                     }
                  }

               }
            }
         }
      }
   }

   public boolean playerOnline(String truncName) {
      String text = truncName.replaceAll("(?i)§[0-F]", "");
      Player[] players = this.getServer().getOnlinePlayers();

      for(int x = 0; x < players.length; ++x) {
         int length = players[x].getName().length();
         if (length > 15) {
            length = 15;
         }

         if (text.equals(players[x].getName().substring(0, length))) {
            return true;
         }
      }

      return false;
   }

   protected static float getBuildVersion() {
      String version = Server.class.getPackage().getImplementationVersion() + ' ';
      int index = version.lastIndexOf("-b");
      if (index == -1) {
         return 0.0F;
      } else {
         index += 2;
         if (version.length() < index + 3) {
            return 0.0F;
         } else {
            if (version.charAt(index) == '{') {
               ++index;
            }

            int endIndex = index;

            for(int x = index; x < version.length() && Character.isDigit(version.charAt(x)); ++x) {
               ++endIndex;
            }

            int build;
            try {
               build = Integer.parseInt(version.substring(index, endIndex));
            } catch (NumberFormatException var7) {
               return 0.0F;
            }

            boolean jenkins = false;
            boolean bamboo = false;
            if (version.length() >= endIndex + 3) {
               if (version.charAt(endIndex) == '}') {
                  ++endIndex;
               }

               if (version.charAt(endIndex) != ' ') {
                  if (version.substring(endIndex).startsWith("jnks ")) {
                     jenkins = true;
                  } else {
                     bamboo = true;
                  }
               }
            }

            if (build >= 231 && build <= 326 && !jenkins && !bamboo) {
               return (float)build;
            } else if (build >= 35 && build <= 54 && jenkins) {
               return 399.0F + (float)build / 100.0F;
            } else {
               return build >= 400 && jenkins ? (float)build : 0.0F;
            }
         }
      }
   }

   public static Block getSignAttachedBlock(Block block) {
      if (block.getTypeId() != Material.WALL_SIGN.getId()) {
         return null;
      } else {
         int face = block.getData() & 7;
         if (face == 3) {
            return block.getRelative(BlockFace.NORTH);
         } else if (face == 4) {
            return block.getRelative(BlockFace.EAST);
         } else if (face == 2) {
            return block.getRelative(BlockFace.SOUTH);
         } else {
            return face == 5 ? block.getRelative(BlockFace.WEST) : null;
         }
      }
   }

   public static Block getTrapDoorAttachedBlock(Block block) {
      if (block.getTypeId() != 96) {
         return null;
      } else {
         int face = block.getData() & 3;
         if (face == 1) {
            return block.getRelative(BlockFace.NORTH);
         } else if (face == 2) {
            return block.getRelative(BlockFace.EAST);
         } else if (face == 0) {
            return block.getRelative(BlockFace.SOUTH);
         } else {
            return face == 3 ? block.getRelative(BlockFace.WEST) : null;
         }
      }
   }

   public static BlockFace getPistonFacing(Block block) {
      int type = block.getTypeId();
      if (type != Material.PISTON_BASE.getId() && type != Material.PISTON_STICKY_BASE.getId() && type != Material.PISTON_EXTENSION.getId()) {
         return BlockFace.SELF;
      } else {
         int face = block.getData() & 7;
         switch (face) {
            case 0:
               return BlockFace.DOWN;
            case 1:
               return BlockFace.UP;
            case 2:
               return BlockFace.NORTH;
            case 3:
               return BlockFace.SOUTH;
            case 4:
               return BlockFace.WEST;
            case 5:
               return BlockFace.EAST;
            default:
               return BlockFace.SELF;
         }
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$yi$acru$bukkit$PluginCoreLink$LinkType() {
      int[] var10000 = $SWITCH_TABLE$org$yi$acru$bukkit$PluginCoreLink$LinkType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[PluginCoreLink.LinkType.values().length];

         try {
            var0[PluginCoreLink.LinkType.ECONOMY.ordinal()] = 5;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[PluginCoreLink.LinkType.GROUPS.ordinal()] = 2;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[PluginCoreLink.LinkType.GROUPS_PERMISSIONS.ordinal()] = 6;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[PluginCoreLink.LinkType.GROUPS_PERMISSIONS_ZONES.ordinal()] = 9;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[PluginCoreLink.LinkType.GROUPS_ZONES.ordinal()] = 7;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[PluginCoreLink.LinkType.GroupManager.ordinal()] = 10;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[PluginCoreLink.LinkType.NONE.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[PluginCoreLink.LinkType.PERMISSIONS.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[PluginCoreLink.LinkType.PERMISSIONS_ZONES.ordinal()] = 8;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[PluginCoreLink.LinkType.Permissions.ordinal()] = 11;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[PluginCoreLink.LinkType.ZONES.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$org$yi$acru$bukkit$PluginCoreLink$LinkType = var0;
         return var0;
      }
   }
}
