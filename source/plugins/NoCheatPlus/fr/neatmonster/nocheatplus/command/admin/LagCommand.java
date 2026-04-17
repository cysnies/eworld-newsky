package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class LagCommand extends BaseCommand {
   public LagCommand(JavaPlugin plugin) {
      super(plugin, "lag", "nocheatplus.admin.lag");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      StringBuilder builder = new StringBuilder(300);
      builder.append("---- Lag tracking ----\n");
      long[] spikeDurations = TickTask.getLagSpikeDurations();
      int[] spikes = TickTask.getLagSpikes();
      builder.append("#### Lag spikes ####\n");
      if (spikes[0] == 0) {
         builder.append("No spikes > " + spikeDurations[0] + " ms within the last 40 to 60 minutes.");
      } else if (spikes[0] > 0) {
         builder.append("Total: " + spikes[0] + " > " + spikeDurations[0] + " ms within the last 40 to 60 minutes.");
         builder.append("\n| ");

         for(int i = 0; i < spikeDurations.length; ++i) {
            if ((i >= spikeDurations.length - 1 || spikes[i] != spikes[i + 1]) && spikes[i] != 0) {
               if (i < spikeDurations.length - 1) {
                  builder.append(spikes[i] - spikes[i + 1] + "x" + spikeDurations[i] + "..." + spikeDurations[i + 1] + " | ");
               } else {
                  builder.append(spikes[i] + "x" + spikeDurations[i] + "... | ");
               }
            }
         }
      }

      builder.append("\n");
      long max = 324000L;
      long medium = 4000L;
      long second = 1200L;
      builder.append("#### TPS lag ####\nPerc.[time]:");

      for(long ms : new long[]{second, medium, max}) {
         double lag = (double)TickTask.getLag(ms);
         int p = Math.max(0, (int)((lag - (double)1.0F) * (double)100.0F));
         builder.append(" " + p + "%[" + StringUtil.fdec1.format((double)ms / (double)1200.0F) + "s]");
      }

      sender.sendMessage(builder.toString());
      return true;
   }
}
