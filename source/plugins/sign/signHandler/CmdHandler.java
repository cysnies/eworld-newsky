package signHandler;

import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import sign.SignHandler;
import sign.SignManager;

public class CmdHandler implements SignHandler, Listener {
   private String pn;
   private String createFlag;
   private String checkFlag;
   private String tip;
   private HashList playerCanUseList;

   public CmdHandler(SignManager signManager) {
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
      if (p.isOp()) {
         return true;
      } else {
         String cmdName = this.getCmdName(lines[1]);
         if (!this.playerCanUseList.has(cmdName)) {
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(80)}));
            String result = "";
            boolean first = true;

            for(String s : this.playerCanUseList) {
               if (first) {
                  first = false;
               } else {
                  result = result + ",";
               }

               result = result + s;
            }

            p.sendMessage(result);
            return false;
         } else {
            return true;
         }
      }
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
      p.chat(msg);
   }

   public boolean onBreak(Player p, String[] lines) {
      return false;
   }

   private void loadConfig(FileConfiguration config) {
      this.createFlag = config.getString("cmd.createFlag");
      this.checkFlag = config.getString("cmd.checkFlag");
      this.tip = config.getString("cmd.tip");
      this.playerCanUseList = new HashListImpl();

      for(String s : config.getStringList("cmd.playerCanUse")) {
         this.playerCanUseList.add(s.toLowerCase());
      }

   }

   private String getCmdName(String s) {
      if (s.trim().isEmpty()) {
         return null;
      } else {
         return !s.substring(0, 1).equals("/") ? null : s.split(" ")[0].substring(1, s.split(" ")[0].length()).toLowerCase();
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
