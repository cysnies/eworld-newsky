package trade;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   private static Trade trade;

   public void onEnable() {
      trade = new Trade(this);
      Util.sendConsoleMessage("&atrade交易插件已经启动!");
   }

   public void onDisable() {
      Util.sendConsoleMessage("&ctrade交易插件已经停止!");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      trade.onCommand(sender, command, label, args);
      return true;
   }

   public static Trade getTrade() {
      return trade;
   }
}
