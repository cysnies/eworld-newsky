package signHandler;

import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import sign.SignHandler;
import sign.SignManager;

public class ConsoleHandler implements SignHandler, Listener {
   private Server server;
   private String pn;
   private String createFlag;
   private String checkFlag;
   private String tip;

   public ConsoleHandler(SignManager signManager) {
      this.server = signManager.getServer();
      this.pn = signManager.getSign().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      signManager.getServer().getPluginManager().registerEvents(this, signManager.getSign());
      signManager.register(this);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.createFlag = config.getString("console.createFlag");
      this.checkFlag = config.getString("console.checkFlag");
      this.tip = config.getString("console.tip");
   }

   public String getCreateFlag() {
      return this.createFlag;
   }

   public String getCheckFlag() {
      return this.checkFlag;
   }

   public String getTip() {
      return this.tip;
   }

   public boolean checkPer(Player p, Block b, String[] lines) {
      return p.isOp();
   }

   public String[] getShow(Player p, Block b, String[] lines) {
      return new String[]{"", "", ""};
   }

   public void onClick(Player p, Block b, String[] lines) {
      String msg = lines[1];
      if (msg.charAt(msg.length() - 1) == '-') {
         msg = msg.substring(0, msg.length() - 1) + lines[2];
         if (msg.charAt(msg.length() - 1) == '-') {
            msg = msg.substring(0, msg.length() - 1) + lines[3];
         }
      }

      msg = msg.replace("<p>", p.getName());
      this.server.dispatchCommand(this.server.getConsoleSender(), msg);
   }

   public boolean onBreak(Player p, String[] lines) {
      return false;
   }
}
