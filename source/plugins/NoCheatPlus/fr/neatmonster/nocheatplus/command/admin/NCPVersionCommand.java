package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class NCPVersionCommand extends BaseCommand {
   public NCPVersionCommand(JavaPlugin plugin) {
      super(plugin, "version", "nocheatplus.admin.version", new String[]{"versions", "ver"});
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      MCAccess mc = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
      sender.sendMessage(new String[]{"---- Version information ----", "#### Server ####", Bukkit.getServer().getVersion(), "#### NoCheatPlus ####", "Plugin: " + ((JavaPlugin)this.access).getDescription().getVersion(), "MCAccess: " + mc.getMCVersion() + " / " + mc.getServerVersionTag()});
      Collection<NCPHook> hooks = NCPHookManager.getAllHooks();
      if (!hooks.isEmpty()) {
         List<String> fullNames = new LinkedList();

         for(NCPHook hook : hooks) {
            fullNames.add(hook.getHookName() + " " + hook.getHookVersion());
         }

         Collections.sort(fullNames, String.CASE_INSENSITIVE_ORDER);
         sender.sendMessage("Hooks: " + StringUtil.join(fullNames, " | "));
      }

      return true;
   }
}
