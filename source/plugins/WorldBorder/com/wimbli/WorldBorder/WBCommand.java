package com.wimbli.WorldBorder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WBCommand implements CommandExecutor {
   private WorldBorder plugin;
   private final String clrCmd;
   private final String clrReq;
   private final String clrOpt;
   private final String clrDesc;
   private final String clrHead;
   private final String clrErr;
   private String fillWorld;
   private int fillFrequency;
   private int fillPadding;
   private String trimWorld;
   private int trimFrequency;
   private int trimPadding;

   public WBCommand(WorldBorder plugin) {
      this.clrCmd = ChatColor.AQUA.toString();
      this.clrReq = ChatColor.GREEN.toString();
      this.clrOpt = ChatColor.DARK_GREEN.toString();
      this.clrDesc = ChatColor.WHITE.toString();
      this.clrHead = ChatColor.YELLOW.toString();
      this.clrErr = ChatColor.RED.toString();
      this.fillWorld = "";
      this.fillFrequency = 20;
      this.fillPadding = CoordXZ.chunkToBlock(13);
      this.trimWorld = "";
      this.trimFrequency = 5000;
      this.trimPadding = CoordXZ.chunkToBlock(13);
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
      Player player = sender instanceof Player ? (Player)sender : null;
      String cmd = this.clrCmd + (player == null ? "wb" : "/wb");
      String cmdW = this.clrCmd + (player == null ? "wb " + this.clrReq + "<world>" : "/wb " + this.clrOpt + "[world]") + this.clrCmd;
      if (split.length > 2 && split[0].startsWith("\"")) {
         if (split[0].endsWith("\"")) {
            split[0] = split[0].substring(1, split[0].length() - 1);
         } else {
            List<String> args = new ArrayList();
            String quote = split[0];

            int loop;
            for(loop = 1; loop < split.length; ++loop) {
               quote = quote + " " + split[loop];
               if (split[loop].endsWith("\"")) {
                  break;
               }
            }

            if (loop < split.length || !split[loop].endsWith("\"")) {
               args.add(quote.substring(1, quote.length() - 1));
               ++loop;

               while(loop < split.length) {
                  args.add(split[loop]);
                  ++loop;
               }

               split = (String[])args.toArray(new String[0]);
            }
         }
      }

      if ((split.length == 5 || split.length == 6) && split[1].equalsIgnoreCase("set")) {
         if (!Config.HasPermission(player, "set")) {
            return true;
         }

         World world = sender.getServer().getWorld(split[0]);
         if (world == null) {
            sender.sendMessage("The world you specified (\"" + split[0] + "\") could not be found on the server, but data for it will be stored anyway.");
         }

         if (this.cmdSet(sender, split[0], split, 2, split.length == 5) && player != null) {
            sender.sendMessage("Border has been set. " + Config.BorderDescription(split[0]));
         }
      } else if ((split.length == 4 || split.length == 5) && split[0].equalsIgnoreCase("set") && player != null) {
         if (!Config.HasPermission(player, "set")) {
            return true;
         }

         String world = player.getWorld().getName();
         if (this.cmdSet(sender, world, split, 1, split.length == 4)) {
            sender.sendMessage("Border has been set. " + Config.BorderDescription(world));
         }
      } else if ((split.length == 2 || split.length == 3) && split[0].equalsIgnoreCase("set") && player != null) {
         if (!Config.HasPermission(player, "set")) {
            return true;
         }

         String world = player.getWorld().getName();
         double x = player.getLocation().getX();
         double z = player.getLocation().getZ();

         int radiusX;
         int radiusZ;
         try {
            radiusX = Integer.parseInt(split[1]);
            if (split.length == 3) {
               radiusZ = Integer.parseInt(split[2]);
            } else {
               radiusZ = radiusX;
            }
         } catch (NumberFormatException var25) {
            sender.sendMessage(this.clrErr + "The radius value(s) must be integers.");
            return true;
         }

         Config.setBorder(world, radiusX, radiusZ, x, z);
         sender.sendMessage("Border has been set. " + Config.BorderDescription(world));
      } else if (split.length == 6 && split[1].equalsIgnoreCase("setcorners")) {
         if (!Config.HasPermission(player, "set")) {
            return true;
         }

         String world = split[0];
         World worldTest = sender.getServer().getWorld(world);
         if (worldTest == null) {
            sender.sendMessage("The world you specified (\"" + world + "\") could not be found on the server, but data for it will be stored anyway.");
         }

         try {
            double x1 = Double.parseDouble(split[2]);
            double z1 = Double.parseDouble(split[3]);
            double x2 = Double.parseDouble(split[4]);
            double z2 = Double.parseDouble(split[5]);
            Config.setBorderCorners(world, x1, z1, x2, z2);
         } catch (NumberFormatException var24) {
            sender.sendMessage(this.clrErr + "The x1, z1, x2, and z2 values must be numerical.");
            return true;
         }

         if (player != null) {
            sender.sendMessage("Border has been set. " + Config.BorderDescription(world));
         }
      } else if (split.length == 5 && split[0].equalsIgnoreCase("setcorners") && player != null) {
         if (!Config.HasPermission(player, "set")) {
            return true;
         }

         String world = player.getWorld().getName();

         try {
            double x1 = Double.parseDouble(split[1]);
            double z1 = Double.parseDouble(split[2]);
            double x2 = Double.parseDouble(split[3]);
            double z2 = Double.parseDouble(split[4]);
            Config.setBorderCorners(world, x1, z1, x2, z2);
         } catch (NumberFormatException var23) {
            sender.sendMessage(this.clrErr + "The x1, z1, x2, and z2 values must be numerical.");
            return true;
         }

         sender.sendMessage("Border has been set. " + Config.BorderDescription(world));
      } else if ((split.length == 3 || split.length == 4) && split[1].equalsIgnoreCase("radius")) {
         if (!Config.HasPermission(player, "radius")) {
            return true;
         }

         String world = split[0];
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage(this.clrErr + "That world (\"" + world + "\") must first have a border set normally.");
            return true;
         }

         double x = border.getX();
         double z = border.getZ();

         int radiusX;
         int radiusZ;
         try {
            radiusX = Integer.parseInt(split[2]);
            if (split.length == 4) {
               radiusZ = Integer.parseInt(split[3]);
            } else {
               radiusZ = radiusX;
            }
         } catch (NumberFormatException var22) {
            sender.sendMessage(this.clrErr + "The radius value(s) must be integers.");
            return true;
         }

         Config.setBorder(world, radiusX, radiusZ, x, z);
         if (player != null) {
            sender.sendMessage("Radius has been set. " + Config.BorderDescription(world));
         }
      } else if ((split.length == 2 || split.length == 3) && split[0].equalsIgnoreCase("radius") && player != null) {
         if (!Config.HasPermission(player, "radius")) {
            return true;
         }

         String world = player.getWorld().getName();
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage(this.clrErr + "This world (\"" + world + "\") must first have a border set normally.");
            return true;
         }

         double x = border.getX();
         double z = border.getZ();

         int radiusX;
         int radiusZ;
         try {
            radiusX = Integer.parseInt(split[1]);
            if (split.length == 3) {
               radiusZ = Integer.parseInt(split[2]);
            } else {
               radiusZ = radiusX;
            }
         } catch (NumberFormatException var21) {
            sender.sendMessage(this.clrErr + "The radius value(s) must be integers.");
            return true;
         }

         Config.setBorder(world, radiusX, radiusZ, x, z);
         sender.sendMessage("Radius has been set. " + Config.BorderDescription(world));
      } else if (split.length == 2 && split[1].equalsIgnoreCase("clear")) {
         if (!Config.HasPermission(player, "clear")) {
            return true;
         }

         String world = split[0];
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage("The world you specified (\"" + world + "\") does not have a border set.");
            return true;
         }

         Config.removeBorder(world);
         if (player != null) {
            sender.sendMessage("Border cleared for world \"" + world + "\".");
         }
      } else if (split.length == 1 && split[0].equalsIgnoreCase("clear") && player != null) {
         if (!Config.HasPermission(player, "clear")) {
            return true;
         }

         String world = player.getWorld().getName();
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage(this.clrErr + "Your current world (\"" + world + "\") does not have a border set.");
            return true;
         }

         Config.removeBorder(world);
         sender.sendMessage("Border cleared for world \"" + world + "\".");
      } else if (split.length == 2 && split[0].equalsIgnoreCase("clear") && split[1].equalsIgnoreCase("all")) {
         if (!Config.HasPermission(player, "clear")) {
            return true;
         }

         Config.removeAllBorders();
         if (player != null) {
            sender.sendMessage("All borders cleared for all worlds.");
         }
      } else if (split.length == 1 && split[0].equalsIgnoreCase("list")) {
         if (!Config.HasPermission(player, "list")) {
            return true;
         }

         sender.sendMessage("Default border shape for all worlds is \"" + Config.ShapeName() + "\".");
         Set<String> list = Config.BorderDescriptions();
         if (list.isEmpty()) {
            sender.sendMessage("There are no borders currently set.");
            return true;
         }

         Iterator listItem = list.iterator();

         while(listItem.hasNext()) {
            sender.sendMessage((String)listItem.next());
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("shape")) {
         if (!Config.HasPermission(player, "shape")) {
            return true;
         }

         if (!split[1].equalsIgnoreCase("rectangular") && !split[1].equalsIgnoreCase("square")) {
            if (!split[1].equalsIgnoreCase("elliptic") && !split[1].equalsIgnoreCase("round")) {
               sender.sendMessage("You must specify a shape of \"elliptic\"/\"round\" or \"rectangular\"/\"square\".");
               return true;
            }

            Config.setShape(true);
         } else {
            Config.setShape(false);
         }

         if (player != null) {
            sender.sendMessage("Default border shape for all worlds is now set to \"" + Config.ShapeName() + "\".");
         }
      } else if (split.length == 1 && split[0].equalsIgnoreCase("getmsg")) {
         if (!Config.HasPermission(player, "getmsg")) {
            return true;
         }

         sender.sendMessage("Border message is currently set to:");
         sender.sendMessage(this.clrErr + Config.Message());
      } else if (split.length >= 2 && split[0].equalsIgnoreCase("setmsg")) {
         if (!Config.HasPermission(player, "setmsg")) {
            return true;
         }

         String message = "";

         for(int i = 1; i < split.length; ++i) {
            if (i != 1) {
               message = message + ' ';
            }

            message = message + split[i];
         }

         Config.setMessage(message);
         if (player != null) {
            sender.sendMessage("Border message is now set to:");
            sender.sendMessage(this.clrErr + Config.Message());
         }
      } else if (split.length == 1 && split[0].equalsIgnoreCase("reload")) {
         if (!Config.HasPermission(player, "reload")) {
            return true;
         }

         if (player != null) {
            Config.Log("Reloading config file at the command of player \"" + player.getName() + "\".");
         }

         Config.load(this.plugin, true);
         if (player != null) {
            sender.sendMessage("WorldBorder configuration reloaded.");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("debug")) {
         if (!Config.HasPermission(player, "debug")) {
            return true;
         }

         Config.setDebug(this.strAsBool(split[1]));
         if (player != null) {
            Config.Log((Config.Debug() ? "Enabling" : "Disabling") + " debug output at the command of player \"" + player.getName() + "\".");
         }

         if (player != null) {
            sender.sendMessage("Debug mode " + this.enabledColored(Config.Debug()) + ".");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("whoosh")) {
         if (!Config.HasPermission(player, "whoosh")) {
            return true;
         }

         Config.setWhooshEffect(this.strAsBool(split[1]));
         if (player != null) {
            Config.Log((Config.whooshEffect() ? "Enabling" : "Disabling") + " \"whoosh\" knockback effect at the command of player \"" + player.getName() + "\".");
            sender.sendMessage("\"Whoosh\" knockback effect " + this.enabledColored(Config.whooshEffect()) + ".");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("knockback")) {
         if (!Config.HasPermission(player, "knockback")) {
            return true;
         }

         double numBlocks = (double)0.0F;

         try {
            numBlocks = Double.parseDouble(split[1]);
         } catch (NumberFormatException var20) {
            sender.sendMessage(this.clrErr + "The knockback must be a decimal value of at least 1.0, or it can be 0.");
            return true;
         }

         if (numBlocks < (double)0.0F || numBlocks > (double)0.0F && numBlocks < (double)1.0F) {
            sender.sendMessage(this.clrErr + "The knockback must be a decimal value of at least 1.0, or it can be 0.");
            return true;
         }

         Config.setKnockBack(numBlocks);
         if (player != null) {
            sender.sendMessage("Knockback set to " + numBlocks + " blocks inside the border.");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("delay")) {
         if (!Config.HasPermission(player, "delay")) {
            return true;
         }

         int delay = 0;

         try {
            delay = Integer.parseInt(split[1]);
         } catch (NumberFormatException var19) {
            sender.sendMessage(this.clrErr + "The timer delay must be an integer of 1 or higher.");
            return true;
         }

         if (delay < 1) {
            sender.sendMessage(this.clrErr + "The timer delay must be an integer of 1 or higher.");
            return true;
         }

         Config.setTimerTicks(delay);
         if (player != null) {
            sender.sendMessage("Timer delay set to " + delay + " tick(s). That is roughly " + delay * 50 + "ms.");
         }
      } else if (split.length == 3 && split[0].equalsIgnoreCase("wshape")) {
         if (!Config.HasPermission(player, "wshape")) {
            return true;
         }

         String world = split[1];
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage("The world you specified (\"" + world + "\") does not have a border set.");
            return true;
         }

         Boolean shape = null;
         if (!split[2].equalsIgnoreCase("rectangular") && !split[2].equalsIgnoreCase("square")) {
            if (split[2].equalsIgnoreCase("elliptic") || split[2].equalsIgnoreCase("round")) {
               shape = true;
            }
         } else {
            shape = false;
         }

         border.setShape(shape);
         Config.setBorder(world, border);
         if (player != null) {
            sender.sendMessage("Border shape for world \"" + world + "\" is now set to \"" + (shape == null ? "default" : Config.ShapeName(shape)) + "\".");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("wshape") && player != null) {
         if (!Config.HasPermission(player, "wshape")) {
            return true;
         }

         String world = player.getWorld().getName();
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage("This world (\"" + world + "\") does not have a border set.");
            return true;
         }

         Boolean shape = null;
         if (!split[1].equalsIgnoreCase("rectangular") && !split[1].equalsIgnoreCase("square")) {
            if (split[1].equalsIgnoreCase("elliptic") || split[1].equalsIgnoreCase("round")) {
               shape = true;
            }
         } else {
            shape = false;
         }

         border.setShape(shape);
         Config.setBorder(world, border);
         sender.sendMessage("Border shape for world \"" + world + "\" is now set to \"" + (shape == null ? "default" : Config.ShapeName(shape)) + "\".");
      } else if (split.length == 3 && split[0].equalsIgnoreCase("wrap")) {
         if (!Config.HasPermission(player, "wrap")) {
            return true;
         }

         String world = split[1];
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage("The world you specified (\"" + world + "\") does not have a border set.");
            return true;
         }

         boolean wrap = this.strAsBool(split[2]);
         border.setWrapping(wrap);
         Config.setBorder(world, border);
         if (player != null) {
            sender.sendMessage("Border for world \"" + world + "\" is now set to " + (wrap ? "" : "not ") + "wrap around.");
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("wrap") && player != null) {
         if (!Config.HasPermission(player, "wrap")) {
            return true;
         }

         String world = player.getWorld().getName();
         BorderData border = Config.Border(world);
         if (border == null) {
            sender.sendMessage("This world (\"" + world + "\") does not have a border set.");
            return true;
         }

         boolean wrap = this.strAsBool(split[1]);
         border.setWrapping(wrap);
         Config.setBorder(world, border);
         sender.sendMessage("Border for world \"" + world + "\" is now set to " + (wrap ? "" : "not ") + "wrap around.");
      } else if (split.length == 2 && split[0].equalsIgnoreCase("portal")) {
         if (!Config.HasPermission(player, "portal")) {
            return true;
         }

         Config.setPortalRedirection(this.strAsBool(split[1]));
         if (player != null) {
            Config.Log((Config.portalRedirection() ? "Enabling" : "Disabling") + " portal redirection at the command of player \"" + player.getName() + "\".");
            sender.sendMessage("Portal redirection " + this.enabledColored(Config.portalRedirection()) + ".");
         }
      } else if (split.length >= 2 && split[1].equalsIgnoreCase("fill")) {
         if (!Config.HasPermission(player, "fill")) {
            return true;
         }

         boolean cancel = false;
         boolean confirm = false;
         boolean pause = false;
         String pad = "";
         String frequency = "";
         if (split.length >= 3) {
            cancel = split[2].equalsIgnoreCase("cancel") || split[2].equalsIgnoreCase("stop");
            confirm = split[2].equalsIgnoreCase("confirm");
            pause = split[2].equalsIgnoreCase("pause");
            if (!cancel && !confirm && !pause) {
               frequency = split[2];
            }
         }

         if (split.length >= 4) {
            pad = split[3];
         }

         String world = split[0];
         this.cmdFill(sender, player, world, confirm, cancel, pause, pad, frequency);
      } else if (split.length >= 1 && split[0].equalsIgnoreCase("fill")) {
         if (!Config.HasPermission(player, "fill")) {
            return true;
         }

         boolean cancel = false;
         boolean confirm = false;
         boolean pause = false;
         String pad = "";
         String frequency = "";
         if (split.length >= 2) {
            cancel = split[1].equalsIgnoreCase("cancel") || split[1].equalsIgnoreCase("stop");
            confirm = split[1].equalsIgnoreCase("confirm");
            pause = split[1].equalsIgnoreCase("pause");
            if (!cancel && !confirm && !pause) {
               frequency = split[1];
            }
         }

         if (split.length >= 3) {
            pad = split[2];
         }

         String world = "";
         if (player != null && !cancel && !confirm && !pause) {
            world = player.getWorld().getName();
         }

         if (!cancel && !confirm && !pause && world.isEmpty()) {
            sender.sendMessage("You must specify a world! Example: " + cmdW + " fill " + this.clrOpt + "[freq] [pad]");
            return true;
         }

         this.cmdFill(sender, player, world, confirm, cancel, pause, pad, frequency);
      } else if (split.length >= 2 && split[1].equalsIgnoreCase("trim")) {
         if (!Config.HasPermission(player, "trim")) {
            return true;
         }

         boolean cancel = false;
         boolean confirm = false;
         boolean pause = false;
         String pad = "";
         String frequency = "";
         if (split.length >= 3) {
            cancel = split[2].equalsIgnoreCase("cancel") || split[2].equalsIgnoreCase("stop");
            confirm = split[2].equalsIgnoreCase("confirm");
            pause = split[2].equalsIgnoreCase("pause");
            if (!cancel && !confirm && !pause) {
               frequency = split[2];
            }
         }

         if (split.length >= 4) {
            pad = split[3];
         }

         String world = split[0];
         this.cmdTrim(sender, player, world, confirm, cancel, pause, pad, frequency);
      } else if (split.length >= 1 && split[0].equalsIgnoreCase("trim")) {
         if (!Config.HasPermission(player, "trim")) {
            return true;
         }

         boolean cancel = false;
         boolean confirm = false;
         boolean pause = false;
         String pad = "";
         String frequency = "";
         if (split.length >= 2) {
            cancel = split[1].equalsIgnoreCase("cancel") || split[1].equalsIgnoreCase("stop");
            confirm = split[1].equalsIgnoreCase("confirm");
            pause = split[1].equalsIgnoreCase("pause");
            if (!cancel && !confirm && !pause) {
               frequency = split[1];
            }
         }

         if (split.length >= 3) {
            pad = split[2];
         }

         String world = "";
         if (player != null && !cancel && !confirm && !pause) {
            world = player.getWorld().getName();
         }

         if (!cancel && !confirm && !pause && world.isEmpty()) {
            sender.sendMessage("You must specify a world! Example: " + cmdW + " trim " + this.clrOpt + "[freq] [pad]");
            return true;
         }

         this.cmdTrim(sender, player, world, confirm, cancel, pause, pad, frequency);
      } else if (split.length == 2 && split[0].equalsIgnoreCase("remount")) {
         if (!Config.HasPermission(player, "remount")) {
            return true;
         }

         int delay = 0;

         try {
            delay = Integer.parseInt(split[1]);
            if (delay < 0) {
               throw new NumberFormatException();
            }
         } catch (NumberFormatException var26) {
            sender.sendMessage(this.clrErr + "The remount delay must be an integer of 0 or higher. Setting to 0 will disable remounting.");
            return true;
         }

         Config.setRemountTicks(delay);
         if (player != null) {
            if (delay == 0) {
               sender.sendMessage("Remount delay set to 0. Players will be left dismounted when knocked back from the border while on a vehicle.");
            } else {
               sender.sendMessage("Remount delay set to " + delay + " tick(s). That is roughly " + delay * 50 + "ms / " + (double)delay * (double)50.0F / (double)1000.0F + " seconds. Setting to 0 would disable remounting.");
               if (delay < 10) {
                  sender.sendMessage(this.clrErr + "WARNING:" + this.clrDesc + " setting this to less than 10 (and greater than 0) is not recommended. This can lead to nasty client glitches.");
               }
            }
         }
      } else if (split.length == 2 && split[0].equalsIgnoreCase("dynmap")) {
         if (!Config.HasPermission(player, "dynmap")) {
            return true;
         }

         Config.setDynmapBorderEnabled(this.strAsBool(split[1]));
         sender.sendMessage("DynMap border display " + (Config.DynmapBorderEnabled() ? "enabled" : "disabled") + ".");
         if (player != null) {
            Config.Log((Config.DynmapBorderEnabled() ? "Enabled" : "Disabled") + " DynMap border display at the command of player \"" + player.getName() + "\".");
         }
      } else if (split.length >= 2 && split[0].equalsIgnoreCase("dynmapmsg")) {
         if (!Config.HasPermission(player, "dynmapmsg")) {
            return true;
         }

         String message = "";

         for(int i = 1; i < split.length; ++i) {
            if (i != 1) {
               message = message + ' ';
            }

            message = message + split[i];
         }

         Config.setDynmapMessage(message);
         if (player != null) {
            sender.sendMessage("DynMap border label is now set to:");
            sender.sendMessage(this.clrErr + Config.DynmapMessage());
         }
      } else if (split.length >= 2 && split[0].equalsIgnoreCase("bypass")) {
         if (!Config.HasPermission(player, "bypass")) {
            return true;
         }

         String sPlayer = split[1];
         boolean bypassing = !Config.isPlayerBypassing(sPlayer);
         if (split.length > 2) {
            bypassing = this.strAsBool(split[2]);
         }

         Config.setPlayerBypass(sPlayer, bypassing);
         Player target = Bukkit.getPlayer(sPlayer);
         if (target != null && target.isOnline()) {
            target.sendMessage("Border bypass is now " + this.enabledColored(bypassing) + ".");
         }

         Config.Log("Border bypass for player \"" + sPlayer + "\" is " + (bypassing ? "enabled" : "disabled") + (player != null ? " at the command of player \"" + player.getName() + "\"" : "") + ".");
         if (player != null && player != target) {
            sender.sendMessage("Border bypass for player \"" + sPlayer + "\" is " + this.enabledColored(bypassing) + ".");
         }
      } else if (split.length == 1 && split[0].equalsIgnoreCase("bypass") && player != null) {
         if (!Config.HasPermission(player, "bypass")) {
            return true;
         }

         String sPlayer = player.getName();
         boolean bypassing = !Config.isPlayerBypassing(sPlayer);
         Config.setPlayerBypass(sPlayer, bypassing);
         Config.Log("Border bypass is " + (bypassing ? "enabled" : "disabled") + " for player \"" + sPlayer + "\".");
         sender.sendMessage("Border bypass is now " + this.enabledColored(bypassing) + ".");
      } else {
         if (!Config.HasPermission(player, "help")) {
            return true;
         }

         int page = player == null ? 0 : 1;
         if (split.length == 1) {
            try {
               page = Integer.parseInt(split[0]);
            } catch (NumberFormatException var18) {
            }

            if (page > 4) {
               page = 1;
            }
         }

         sender.sendMessage(this.clrHead + this.plugin.getDescription().getFullName() + " - commands (" + this.clrReq + "<required> " + this.clrOpt + "[optional]" + this.clrHead + ")" + (page > 0 ? " " + page + "/4" : "") + ":");
         if (page == 0 || page == 1) {
            if (player != null) {
               sender.sendMessage(cmd + " set " + this.clrReq + "<radiusX> " + this.clrOpt + "[radiusZ]" + this.clrDesc + " - set border, centered on you.");
            }

            sender.sendMessage(cmdW + " set " + this.clrReq + "<radiusX> " + this.clrOpt + "[radiusZ] <x> <z>" + this.clrDesc + " - set border.");
            sender.sendMessage(cmdW + " setcorners " + this.clrReq + "<x1> <z1> <x2> <z2>" + this.clrDesc + " - set by corners.");
            sender.sendMessage(cmdW + " radius " + this.clrReq + "<radiusX> " + this.clrOpt + "[radiusZ]" + this.clrDesc + " - change radius.");
            sender.sendMessage(cmdW + " clear" + this.clrDesc + " - remove border for this world.");
            sender.sendMessage(cmd + " clear all" + this.clrDesc + " - remove border for all worlds.");
            sender.sendMessage(cmd + " shape " + this.clrReq + "<elliptic|rectangular>" + this.clrDesc + " - set the default shape.");
            sender.sendMessage(cmd + " shape " + this.clrReq + "<round|square>" + this.clrDesc + " - same as above.");
            if (page == 1) {
               sender.sendMessage(cmd + " 2" + this.clrDesc + " - view second page of commands.");
            }
         }

         if (page == 0 || page == 2) {
            sender.sendMessage(cmd + " list" + this.clrDesc + " - show border information for all worlds.");
            sender.sendMessage(cmdW + " fill " + this.clrOpt + "[freq] [pad]" + this.clrDesc + " - generate world out to border.");
            sender.sendMessage(cmdW + " trim " + this.clrOpt + "[freq] [pad]" + this.clrDesc + " - trim world outside of border.");
            sender.sendMessage(cmd + " bypass " + (player == null ? this.clrReq + "<player>" : this.clrOpt + "[player]") + this.clrOpt + " [on/off]" + this.clrDesc + " - let player go beyond border.");
            sender.sendMessage(cmd + " wshape " + (player == null ? this.clrReq + "<world>" : this.clrOpt + "[world]") + this.clrReq + " <elliptic|rectangular|default>" + this.clrDesc + " - shape override for this world.");
            sender.sendMessage(cmd + " wshape " + (player == null ? this.clrReq + "<world>" : this.clrOpt + "[world]") + this.clrReq + " <round|square|default>" + this.clrDesc + " - same as above.");
            sender.sendMessage(cmd + " wrap " + (player == null ? this.clrReq + "<world>" : this.clrOpt + "[world]") + this.clrReq + " <on/off>" + this.clrDesc + " - can make border crossings wrap.");
            if (page == 2) {
               sender.sendMessage(cmd + " 3" + this.clrDesc + " - view third page of commands.");
            }
         }

         if (page == 0 || page == 3) {
            sender.sendMessage(cmd + " whoosh " + this.clrReq + "<on|off>" + this.clrDesc + " - turn knockback effect on or off.");
            sender.sendMessage(cmd + " getmsg" + this.clrDesc + " - display border message.");
            sender.sendMessage(cmd + " setmsg " + this.clrReq + "<text>" + this.clrDesc + " - set border message.");
            sender.sendMessage(cmd + " knockback " + this.clrReq + "<distance>" + this.clrDesc + " - how far to move the player back.");
            sender.sendMessage(cmd + " delay " + this.clrReq + "<amount>" + this.clrDesc + " - time between border checks.");
            sender.sendMessage(cmd + " remount " + this.clrReq + "<amount>" + this.clrDesc + " - player remount delay after knockback.");
            sender.sendMessage(cmd + " dynmap " + this.clrReq + "<on|off>" + this.clrDesc + " - turn DynMap border display on or off.");
            sender.sendMessage(cmd + " dynmapmsg " + this.clrReq + "<text>" + this.clrDesc + " - DynMap border labels will show this.");
            if (page == 3) {
               sender.sendMessage(cmd + " 4" + this.clrDesc + " - view fourth page of commands.");
            }
         }

         if (page == 0 || page == 4) {
            sender.sendMessage(cmd + " portal " + this.clrReq + "<on|off>" + this.clrDesc + " - turn portal redirection on or off.");
            sender.sendMessage(cmd + " reload" + this.clrDesc + " - re-load data from config.yml.");
            sender.sendMessage(cmd + " debug " + this.clrReq + "<on|off>" + this.clrDesc + " - turn console debug output on or off.");
            if (page == 4) {
               sender.sendMessage(cmd + this.clrDesc + " - view first page of commands.");
            }
         }
      }

      return true;
   }

   private boolean strAsBool(String str) {
      str = str.toLowerCase();
      return str.startsWith("y") || str.startsWith("t") || str.startsWith("on") || str.startsWith("+") || str.startsWith("1");
   }

   private String enabledColored(boolean enabled) {
      return enabled ? this.clrReq + "enabled" : this.clrErr + "disabled";
   }

   private boolean cmdSet(CommandSender sender, String world, String[] data, int offset, boolean oneRadius) {
      int radiusX;
      int radiusZ;
      double x;
      double z;
      try {
         radiusX = Integer.parseInt(data[offset]);
         if (oneRadius) {
            radiusZ = radiusX;
            --offset;
         } else {
            radiusZ = Integer.parseInt(data[offset + 1]);
         }

         x = Double.parseDouble(data[offset + 2]);
         z = Double.parseDouble(data[offset + 3]);
      } catch (NumberFormatException var13) {
         sender.sendMessage(this.clrErr + "The radius value(s) must be integers and the x and z values must be numerical.");
         return false;
      }

      Config.setBorder(world, radiusX, radiusZ, x, z);
      return true;
   }

   private void fillDefaults() {
      this.fillWorld = "";
      this.fillFrequency = 20;
      this.fillPadding = CoordXZ.chunkToBlock(13);
   }

   private boolean cmdFill(CommandSender sender, Player player, String world, boolean confirm, boolean cancel, boolean pause, String pad, String frequency) {
      if (cancel) {
         sender.sendMessage(this.clrHead + "Cancelling the world map generation task.");
         this.fillDefaults();
         Config.StopFillTask();
         return true;
      } else if (pause) {
         if (Config.fillTask != null && Config.fillTask.valid()) {
            Config.fillTask.pause();
            sender.sendMessage(this.clrHead + "The world map generation task is now " + (Config.fillTask.isPaused() ? "" : "un") + "paused.");
            return true;
         } else {
            sender.sendMessage(this.clrHead + "The world map generation task is not currently running.");
            return true;
         }
      } else if (Config.fillTask != null && Config.fillTask.valid()) {
         sender.sendMessage(this.clrHead + "The world map generation task is already running.");
         return true;
      } else {
         try {
            if (!pad.isEmpty()) {
               this.fillPadding = Math.abs(Integer.parseInt(pad));
            }

            if (!frequency.isEmpty()) {
               this.fillFrequency = Math.abs(Integer.parseInt(frequency));
            }
         } catch (NumberFormatException var12) {
            sender.sendMessage(this.clrErr + "The frequency and padding values must be integers.");
            return false;
         }

         if (!world.isEmpty()) {
            this.fillWorld = world;
         }

         if (confirm) {
            if (this.fillWorld.isEmpty()) {
               sender.sendMessage(this.clrErr + "You must first use this command successfully without confirming.");
               return false;
            }

            if (player != null) {
               Config.Log("Filling out world to border at the command of player \"" + player.getName() + "\".");
            }

            int ticks = 1;
            int repeats = 1;
            if (this.fillFrequency > 20) {
               repeats = this.fillFrequency / 20;
            } else {
               ticks = 20 / this.fillFrequency;
            }

            Config.fillTask = new WorldFillTask(this.plugin.getServer(), player, this.fillWorld, this.fillPadding, repeats, ticks);
            if (Config.fillTask.valid()) {
               int task = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, Config.fillTask, (long)ticks, (long)ticks);
               Config.fillTask.setTaskID(task);
               sender.sendMessage("WorldBorder map generation for world \"" + this.fillWorld + "\" task started.");
            } else {
               sender.sendMessage(this.clrErr + "The world map generation task failed to start.");
            }

            this.fillDefaults();
         } else {
            if (this.fillWorld.isEmpty()) {
               sender.sendMessage(this.clrErr + "You must first specify a valid world.");
               return false;
            }

            String cmd = this.clrCmd + (player == null ? "wb" : "/wb");
            sender.sendMessage(this.clrHead + "World generation task is ready for world \"" + this.fillWorld + "\", padding the map out to " + this.fillPadding + " blocks beyond the border (default " + CoordXZ.chunkToBlock(13) + "), and the task will try to generate up to " + this.fillFrequency + " chunks per second (default 20).");
            sender.sendMessage(this.clrHead + "This process can take a very long time depending on the world's border size. Also, depending on the chunk processing rate, players will likely experience severe lag for the duration.");
            sender.sendMessage(this.clrDesc + "You should now use " + cmd + " fill confirm" + this.clrDesc + " to start the process.");
            sender.sendMessage(this.clrDesc + "You can cancel at any time with " + cmd + " fill cancel" + this.clrDesc + ", or pause/unpause with " + cmd + " fill pause" + this.clrDesc + ".");
         }

         return true;
      }
   }

   private void trimDefaults() {
      this.trimWorld = "";
      this.trimFrequency = 5000;
      this.trimPadding = CoordXZ.chunkToBlock(13);
   }

   private boolean cmdTrim(CommandSender sender, Player player, String world, boolean confirm, boolean cancel, boolean pause, String pad, String frequency) {
      if (cancel) {
         sender.sendMessage(this.clrHead + "Cancelling the world map trimming task.");
         this.trimDefaults();
         Config.StopTrimTask();
         return true;
      } else if (pause) {
         if (Config.trimTask != null && Config.trimTask.valid()) {
            Config.trimTask.pause();
            sender.sendMessage(this.clrHead + "The world map trimming task is now " + (Config.trimTask.isPaused() ? "" : "un") + "paused.");
            return true;
         } else {
            sender.sendMessage(this.clrHead + "The world map trimming task is not currently running.");
            return true;
         }
      } else if (Config.trimTask != null && Config.trimTask.valid()) {
         sender.sendMessage(this.clrHead + "The world map trimming task is already running.");
         return true;
      } else {
         try {
            if (!pad.isEmpty()) {
               this.trimPadding = Math.abs(Integer.parseInt(pad));
            }

            if (!frequency.isEmpty()) {
               this.trimFrequency = Math.abs(Integer.parseInt(frequency));
            }
         } catch (NumberFormatException var12) {
            sender.sendMessage(this.clrErr + "The frequency and padding values must be integers.");
            return false;
         }

         if (!world.isEmpty()) {
            this.trimWorld = world;
         }

         if (confirm) {
            if (this.trimWorld.isEmpty()) {
               sender.sendMessage(this.clrErr + "You must first use this command successfully without confirming.");
               return false;
            }

            if (player != null) {
               Config.Log("Trimming world beyond border at the command of player \"" + player.getName() + "\".");
            }

            int ticks = 1;
            int repeats = 1;
            if (this.trimFrequency > 20) {
               repeats = this.trimFrequency / 20;
            } else {
               ticks = 20 / this.trimFrequency;
            }

            Config.trimTask = new WorldTrimTask(this.plugin.getServer(), player, this.trimWorld, this.trimPadding, repeats);
            if (Config.trimTask.valid()) {
               int task = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, Config.trimTask, (long)ticks, (long)ticks);
               Config.trimTask.setTaskID(task);
               sender.sendMessage("WorldBorder map trimming task for world \"" + this.trimWorld + "\" started.");
            } else {
               sender.sendMessage(this.clrErr + "The world map trimming task failed to start.");
            }

            this.trimDefaults();
         } else {
            if (this.trimWorld.isEmpty()) {
               sender.sendMessage(this.clrErr + "You must first specify a valid world.");
               return false;
            }

            String cmd = this.clrCmd + (player == null ? "wb" : "/wb");
            sender.sendMessage(this.clrHead + "World trimming task is ready for world \"" + this.trimWorld + "\", trimming the map past " + this.trimPadding + " blocks beyond the border (default " + CoordXZ.chunkToBlock(13) + "), and the task will try to process up to " + this.trimFrequency + " chunks per second (default 5000).");
            sender.sendMessage(this.clrHead + "This process can take a while depending on the world's overall size. Also, depending on the chunk processing rate, players may experience lag for the duration.");
            sender.sendMessage(this.clrDesc + "You should now use " + cmd + " trim confirm" + this.clrDesc + " to start the process.");
            sender.sendMessage(this.clrDesc + "You can cancel at any time with " + cmd + " trim cancel" + this.clrDesc + ", or pause/unpause with " + cmd + " trim pause" + this.clrDesc + ".");
         }

         return true;
      }
   }
}
