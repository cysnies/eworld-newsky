package sign;

import java.util.HashMap;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import signHandler.CmdHandler;
import signHandler.ConsoleHandler;

public class SignManager implements Listener {
   private Server server;
   private Sign sign;
   private String pn;
   private HashMap createHash;
   private HashMap checkHash;
   private CmdHandler cmdHandler;
   private ConsoleHandler consoleHandler;

   public SignManager(Sign sign) {
      this.server = sign.getServer();
      this.sign = sign;
      this.pn = sign.getPn();
      this.createHash = new HashMap();
      this.checkHash = new HashMap();
      this.cmdHandler = new CmdHandler(this);
      this.consoleHandler = new ConsoleHandler(this);
      sign.getServer().getPluginManager().registerEvents(this, sign);
   }

   public void register(SignHandler signHandler) {
      this.createHash.put(signHandler.getCreateFlag(), signHandler);
      this.checkHash.put(signHandler.getCheckFlag(), signHandler);
   }

   public SignHandler getSignHandler(String createFlag) {
      return (SignHandler)this.createHash.get(createFlag);
   }

   public void onClick(Player p, Block b, String[] lines) {
      SignHandler signHandler = (SignHandler)this.checkHash.get(this.sign.getCheckFlag(lines[0]));
      if (signHandler == null) {
         this.show(p, "fail", this.get(45));
      } else {
         signHandler.onClick(p, b, lines);
      }
   }

   public boolean onBreak(Player p, String[] lines) {
      SignHandler signHandler = (SignHandler)this.checkHash.get(this.sign.getCheckFlag(lines[0]));
      if (signHandler == null) {
         this.show(p, "fail", this.get(45));
         return false;
      } else {
         return signHandler.onBreak(p, lines);
      }
   }

   public Server getServer() {
      return this.server;
   }

   public Sign getSign() {
      return this.sign;
   }

   public CmdHandler getCmdHandler() {
      return this.cmdHandler;
   }

   public ConsoleHandler getConsoleHandler() {
      return this.consoleHandler;
   }

   public void showList(CommandSender sender) {
      sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(75)}));

      for(String s : this.createHash.keySet()) {
         SignHandler signHandler = (SignHandler)this.createHash.get(s);
         sender.sendMessage(UtilFormat.format(this.pn, "flagList", new Object[]{signHandler.getCreateFlag(), signHandler.getCheckFlag(), signHandler.getTip()}));
      }

   }

   private void show(Player p, String type, Object args) {
      p.sendMessage(UtilFormat.format(this.pn, type, new Object[]{args}));
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
