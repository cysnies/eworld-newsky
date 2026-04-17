package fr.neatmonster.nocheatplus.command;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BaseCommand extends AbstractCommand {
   public static final String TAG;

   public BaseCommand(JavaPlugin plugin, String label, String permission) {
      this(plugin, label, permission, (String[])null);
   }

   public BaseCommand(JavaPlugin access, String label, String permission, String[] aliases) {
      super(access, label, permission, aliases);
   }

   static {
      TAG = ChatColor.RED + "NCP: " + ChatColor.WHITE;
   }
}
