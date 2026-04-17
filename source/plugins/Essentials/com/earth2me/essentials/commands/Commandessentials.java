package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.metrics.Metrics;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Commandessentials extends EssentialsCommand {
   private transient int taskid;
   private final transient Map noteBlocks = new HashMap();

   public Commandessentials() {
      super("essentials");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length == 0) {
         this.run_disabled(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("debug")) {
         this.run_debug(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("nya")) {
         this.run_nya(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("moo")) {
         this.run_moo(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("reset")) {
         this.run_reset(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("opt-out")) {
         this.run_optout(server, sender, commandLabel, args);
      } else if (args[0].equalsIgnoreCase("cleanup")) {
         this.run_cleanup(server, sender, commandLabel, args);
      } else {
         this.run_reload(server, sender, commandLabel, args);
      }

   }

   private void run_disabled(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      sender.sendMessage("/<command> <reload/debug>");
      StringBuilder disabledCommands = new StringBuilder();

      for(Map.Entry entry : this.ess.getAlternativeCommandsHandler().disabledCommands().entrySet()) {
         if (disabledCommands.length() > 0) {
            disabledCommands.append(", ");
         }

         disabledCommands.append((String)entry.getKey()).append(" => ").append((String)entry.getValue());
      }

      if (disabledCommands.length() > 0) {
         sender.sendMessage(I18n._("blockList"));
         sender.sendMessage(disabledCommands.toString());
      }

   }

   private void run_reset(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new Exception("/<command> reset <player>");
      } else {
         User user = this.getPlayer(server, args, 1, true, true);
         user.reset();
         sender.sendMessage("Reset Essentials userdata for player: " + user.getDisplayName());
      }
   }

   private void run_debug(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.ess.getSettings().setDebug(!this.ess.getSettings().isDebug());
      sender.sendMessage("Essentials " + this.ess.getDescription().getVersion() + " debug mode " + (this.ess.getSettings().isDebug() ? "enabled" : "disabled"));
   }

   private void run_reload(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.ess.reload();
      sender.sendMessage(I18n._("essentialsReload", this.ess.getDescription().getVersion()));
   }

   private void run_nya(final Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      final Map<String, Float> noteMap = new HashMap();
      noteMap.put("1F#", 0.5F);
      noteMap.put("1G", 0.53F);
      noteMap.put("1G#", 0.56F);
      noteMap.put("1A", 0.6F);
      noteMap.put("1A#", 0.63F);
      noteMap.put("1B", 0.67F);
      noteMap.put("1C", 0.7F);
      noteMap.put("1C#", 0.76F);
      noteMap.put("1D", 0.8F);
      noteMap.put("1D#", 0.84F);
      noteMap.put("1E", 0.9F);
      noteMap.put("1F", 0.94F);
      noteMap.put("2F#", 1.0F);
      noteMap.put("2G", 1.06F);
      noteMap.put("2G#", 1.12F);
      noteMap.put("2A", 1.18F);
      noteMap.put("2A#", 1.26F);
      noteMap.put("2B", 1.34F);
      noteMap.put("2C", 1.42F);
      noteMap.put("2C#", 1.5F);
      noteMap.put("2D", 1.6F);
      noteMap.put("2D#", 1.68F);
      noteMap.put("2E", 1.78F);
      noteMap.put("2F", 1.88F);
      String tuneStr = "1D#,1E,2F#,,2A#,1E,1D#,1E,2F#,2B,2D#,2E,2D#,2A#,2B,,2F#,,1D#,1E,2F#,2B,2C#,2A#,2B,2C#,2E,2D#,2E,2C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1B,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1B,,";
      final String[] tune = "1D#,1E,2F#,,2A#,1E,1D#,1E,2F#,2B,2D#,2E,2D#,2A#,2B,,2F#,,1D#,1E,2F#,2B,2C#,2A#,2B,2C#,2E,2D#,2E,2C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1C#,,2F#,,2G#,,1D,1D#,,1C#,1D,1C#,1B,,1B,,1C#,,1D,,1D,1C#,1B,1C#,1D#,2F#,2G#,1D#,2F#,1C#,1D#,1B,1C#,1B,1D#,,2F#,,2G#,1D#,2F#,1C#,1D#,1B,1D,1D#,1D,1C#,1B,1C#,1D,,1B,1C#,1D#,2F#,1C#,1D,1C#,1B,1C#,,1B,,1B,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1A#,,1B,,1F#,1G#,1B,,1F#,1G#,1B,1C#,1D#,1B,1E,1D#,1E,2F#,1B,,1B,,1F#,1G#,1B,1F#,1E,1D#,1C#,1B,,,,1F#,1B,,1F#,1G#,1B,,1F#,1G#,1B,1B,1C#,1D#,1B,1F#,1G#,1F#,1B,,1B,1A#,1B,1F#,1G#,1B,1E,1D#,1E,2F#,1B,,1B,,".split(",");
      this.taskid = this.ess.scheduleSyncRepeatingTask(new Runnable() {
         int i = 0;

         public void run() {
            String note = tune[this.i];
            ++this.i;
            if (this.i >= tune.length) {
               Commandessentials.this.stopTune();
            }

            if (!note.isEmpty() && note != null) {
               for(Player onlinePlayer : server.getOnlinePlayers()) {
                  onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.NOTE_PIANO, 1.0F, (Float)noteMap.get(note));
               }

            }
         }
      }, 20L, 2L);
   }

   private void stopTune() {
      this.ess.getScheduler().cancelTask(this.taskid);

      for(Block block : this.noteBlocks.values()) {
         if (block.getType() == Material.NOTE_BLOCK) {
            block.setType(Material.AIR);
         }
      }

      this.noteBlocks.clear();
   }

   private void run_moo(Server server, CommandSender sender, String command, String[] args) {
      if (sender instanceof ConsoleCommandSender) {
         sender.sendMessage(new String[]{"         (__)", "         (oo)", "   /------\\/", "  / |    ||", " *  /\\---/\\", "    ~~   ~~", "....\"Have you mooed today?\"..."});
      } else {
         sender.sendMessage(new String[]{"            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..."});
         Player player = (Player)sender;
         player.playSound(player.getLocation(), Sound.COW_IDLE, 1.0F, 1.0F);
      }

   }

   private void run_optout(Server server, CommandSender sender, String command, String[] args) {
      Metrics metrics = this.ess.getMetrics();

      try {
         sender.sendMessage("Essentials collects simple metrics to highlight which features to concentrate work on in the future.");
         if (metrics.isOptOut()) {
            metrics.enable();
         } else {
            metrics.disable();
         }

         sender.sendMessage("Anonymous Metrics are now: " + (metrics.isOptOut() ? "disabled" : "enabled"));
      } catch (IOException ex) {
         sender.sendMessage("Unable to modify 'plugins/PluginMetrics/config.yml': " + ex.getMessage());
      }

   }

   private void run_cleanup(Server server, final CommandSender sender, String command, String[] args) throws Exception {
      if (args.length >= 2 && Util.isInt(args[1])) {
         sender.sendMessage(I18n._("cleaning"));
         final long daysArg = Long.parseLong(args[1]);
         final double moneyArg = args.length >= 3 ? Double.parseDouble(args[2].replaceAll("[^0-9\\.]", "")) : (double)0.0F;
         final int homesArg = args.length >= 4 && Util.isInt(args[3]) ? Integer.parseInt(args[3]) : 0;
         final int bansArg = args.length >= 5 && Util.isInt(args[4]) ? Integer.parseInt(args[4]) : 0;
         final UserMap userMap = this.ess.getUserMap();
         this.ess.runTaskAsynchronously(new Runnable() {
            public void run() {
               for(String u : userMap.getAllUniqueUsers()) {
                  User user = Commandessentials.this.ess.getUserMap().getUser(u);
                  if (user != null) {
                     int ban = user.getBanReason().equals("") ? 0 : 1;
                     long lastLog = user.getLastLogout();
                     long timeDiff = System.currentTimeMillis() - lastLog;
                     long milliDays = daysArg * 24L * 60L * 60L * 1000L;
                     int homeCount = user.getHomes().size();
                     double moneyCount = user.getMoney();
                     if (lastLog != 0L && ban <= bansArg && timeDiff >= milliDays && homeCount <= homesArg && !(moneyCount > moneyArg)) {
                        if (Commandessentials.this.ess.getSettings().isDebug()) {
                           Commandessentials.this.ess.getLogger().info("Deleting user: " + user.getName() + " Money: " + moneyCount + " Homes: " + homeCount + " Last seen: " + Util.formatDateDiff(lastLog));
                        }

                        user.reset();
                     }
                  }
               }

               sender.sendMessage(I18n._("cleaned"));
            }
         });
      } else {
         sender.sendMessage("This sub-command will delete users who havent logged in in the last <days> days.");
         sender.sendMessage("Optional parameters define the minium amount required to prevent deletion.");
         throw new Exception("/<command> cleanup <days> [money] [homes] [ban count]");
      }
   }
}
