package lib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Msg implements Listener {
   private static String CHECK_PER = "per.lib.checkMsg";
   private static long Id = 0L;
   private ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
   private Server server;
   private String pn;
   private String adminPer;
   private int sideListSize;
   private HashMap msgInfoHash;
   private HashMap teamHash;
   private HashMap playersHash;
   private HashList showList;
   private ShowInfo currentShowInfo;
   private int counter;

   public Msg(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.msgInfoHash = new HashMap();
      this.teamHash = new HashMap();
      this.playersHash = new HashMap();
      this.showList = new HashListImpl();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      lib.getServer().getPluginManager().registerEvents(this, lib);
      this.server.getScheduler().scheduleSyncRepeatingTask(lib, new Runnable() {
         public void run() {
            this.checkShowSideMsg();
         }

         private void checkShowSideMsg() {
            if (Msg.this.currentShowInfo == null) {
               Msg.this.next();
            }

         }
      }, 2L, 2L);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         int length = args.length;
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("list")) {
                  this.showList(sender, 1);
                  return;
               }

               if (args[0].equalsIgnoreCase("remove")) {
                  this.remove(sender, 0);
                  return;
               }

               if (args[0].equalsIgnoreCase("clear")) {
                  this.clear(sender);
                  return;
               }
            } else if (length == 2) {
               if (args[0].equalsIgnoreCase("list")) {
                  this.showList(sender, Integer.parseInt(args[1]));
                  return;
               }

               if (args[0].equalsIgnoreCase("remove")) {
                  this.remove(sender, Integer.parseInt(args[1]));
                  return;
               }
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(1000)));
         if (p == null || UtilPer.hasPer(p, this.adminPer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1002), this.get(1005)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1010), this.get(1015)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1020), this.get(1025)));
         }
      } catch (NumberFormatException var7) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler
   public void onTime(TimeEvent e) {
      if (this.currentShowInfo != null) {
         --this.counter;
         if (this.counter < 0) {
            this.end();
            this.next();
         } else {
            String from = this.currentShowInfo.getFrom();
            PacketContainer pc = this.getUpdateSideTimePacket(from, this.counter);

            Player[] var7;
            for(Player p : var7 = this.server.getOnlinePlayers()) {
               if (this.isDisplaySideBar(p)) {
                  this.send(p, pc);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      String name = p.getName();
      if (!this.teamHash.containsKey(name)) {
         this.teamHash.put(name, this.getNextName());
      }

      if (!this.playersHash.containsKey(name)) {
         List<String> list = new LinkedList();
         list.add(name);
         this.playersHash.put(name, list);
      }

      MsgInfo msgInfo0 = this.getMsgInfo(p.getName());
      String team0 = (String)this.teamHash.get(name);
      List<String> list0 = (List)this.playersHash.get(name);
      PacketContainer createTeamPacketSelf = this.getCreateTeamPacket(team0, msgInfo0.getPrefix(), msgInfo0.getSuffix(), list0);

      Player[] var11;
      for(Player tar : var11 = this.server.getOnlinePlayers()) {
         if (!tar.equals(p)) {
            this.send(tar, createTeamPacketSelf);
         }

         MsgInfo msgInfo = this.getMsgInfo(tar.getName());
         if (msgInfo != null) {
            String team = (String)this.teamHash.get(tar.getName());
            List<String> list = (List)this.playersHash.get(tar.getName());
            PacketContainer createTeamPacketOther = this.getCreateTeamPacket(team, msgInfo.getPrefix(), msgInfo.getSuffix(), list);
            this.send(p, createTeamPacketOther);
         }
      }

      if (this.currentShowInfo != null && this.isDisplaySideBar(p)) {
         for(PacketContainer pc : this.getSendCreateSidePacket(this.currentShowInfo.getShow(), this.currentShowInfo.getFrom(), this.counter)) {
            this.send(p, pc);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      String team0 = (String)this.teamHash.get(p.getName());
      PacketContainer pc = this.getRemoveTeamPacket(team0);

      Player[] var8;
      for(Player tar : var8 = this.server.getOnlinePlayers()) {
         if (!tar.equals(p)) {
            this.send(tar, pc);
         }
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.adminPer = config.getString("scoreboard.adminPer");
      this.sideListSize = config.getInt("scoreboard.side.listSize");
   }

   private void showList(CommandSender sender, int page) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.adminPer)) {
         if (this.showList.isEmpty()) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(1030)));
         } else {
            int maxPage = this.showList.getMaxPage(this.sideListSize);
            if (page >= 1 && page <= maxPage) {
               List<ShowInfo> list = this.showList.getPage(page, this.sideListSize);
               sender.sendMessage(UtilFormat.format(this.pn, "listHeader", this.get(1035), page, maxPage));
               int past = (page - 1) * this.sideListSize;

               for(int index = 1; index <= list.size(); ++index) {
                  int id = past + index;
                  ShowInfo showInfo = (ShowInfo)list.get(index);
                  sender.sendMessage(UtilFormat.format(this.pn, "sidePageShow", id, showInfo.getFrom(), showInfo.getLast(), showInfo.getShow()));
               }

            } else {
               sender.sendMessage(UtilFormat.format(this.pn, "sidePageError", maxPage));
            }
         }
      }
   }

   private void remove(CommandSender sender, int index) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.adminPer)) {
         if (index == 0) {
            if (this.currentShowInfo == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(1040)));
               return;
            }
         } else if (this.showList.size() == 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(1045)));
            return;
         }

         if (index == 0) {
            this.counter = 0;
            sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(1050)));
         } else {
            if (index < 1) {
               index = 1;
            }

            if (index > this.showList.size()) {
               index = this.showList.size();
            }

            this.showList.remove(index - 1);
            sender.sendMessage(UtilFormat.format(this.pn, "sideDelSuccess", index));
         }

      }
   }

   private void clear(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.adminPer)) {
         if (this.currentShowInfo == null && this.showList.size() == 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(1055)));
         } else {
            this.counter = 0;
            this.showList.clear();
            sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(1060)));
         }
      }
   }

   private String getNextName() {
      ++Id;
      return "a" + Id;
   }

   private PacketContainer getCreateTeamPacket(String name, String prefix, String suffix, List players) {
      if (prefix == null) {
         prefix = "";
      }

      if (suffix == null) {
         suffix = "";
      }

      PacketContainer pc = new PacketContainer(209);
      pc.getStrings().write(0, name).write(2, prefix).write(3, suffix);
      pc.getSpecificModifier(Collection.class).write(0, players);
      pc.getIntegers().write(0, 0).write(1, 1);
      return pc;
   }

   private PacketContainer getUpdateTeamInfoPacket(String name, String prefix, String suffix) {
      if (prefix == null) {
         prefix = "";
      }

      if (suffix == null) {
         suffix = "";
      }

      PacketContainer pc = new PacketContainer(209);
      pc.getStrings().write(0, name).write(2, prefix).write(3, suffix);
      pc.getIntegers().write(0, 2).write(1, 1);
      return pc;
   }

   private PacketContainer getRemoveTeamPacket(String name) {
      PacketContainer pc = new PacketContainer(209);
      pc.getStrings().write(0, name);
      pc.getIntegers().write(0, 1);
      return pc;
   }

   private List getSendCreateSidePacket(String show, String from, int score) {
      List<PacketContainer> result = new LinkedList();
      show = show.substring(0, Math.min(32, show.length()));
      PacketContainer pc = new PacketContainer(206);
      pc.getIntegers().write(0, 0);
      pc.getStrings().write(0, "show").write(1, show);
      result.add(pc);
      pc = new PacketContainer(208);
      pc.getIntegers().write(0, 1);
      pc.getStrings().write(0, "show");
      result.add(pc);
      pc = new PacketContainer(207);
      pc.getStrings().write(0, from).write(1, "show");
      pc.getIntegers().write(0, 0).write(1, score);
      result.add(pc);
      return result;
   }

   private PacketContainer getUpdateSideTimePacket(String from, int score) {
      PacketContainer pc = new PacketContainer(207);
      pc.getStrings().write(0, from).write(1, "show");
      pc.getIntegers().write(0, score).write(1, 0);
      return pc;
   }

   private PacketContainer getRemoveSidePacket() {
      PacketContainer pc = new PacketContainer(206);
      pc.getIntegers().write(0, 1);
      pc.getStrings().write(0, "show").write(1, "");
      return pc;
   }

   private void send(Player p, PacketContainer pc) {
      try {
         this.protocolManager.sendServerPacket(p, pc);
      } catch (InvocationTargetException var4) {
      }

   }

   private void next() {
      if (!this.showList.isEmpty()) {
         this.currentShowInfo = (ShowInfo)this.showList.get(0);
         this.showList.remove(0);
         this.counter = this.currentShowInfo.getLast();
         List<PacketContainer> list = this.getSendCreateSidePacket(this.currentShowInfo.getShow(), this.currentShowInfo.getFrom(), this.currentShowInfo.getLast());

         Player[] var5;
         for(Player p : var5 = this.server.getOnlinePlayers()) {
            if (this.isDisplaySideBar(p)) {
               for(PacketContainer pc : list) {
                  this.send(p, pc);
               }
            }
         }
      }

   }

   private void end() {
      this.currentShowInfo = null;
      this.counter = 0;
      PacketContainer removeSidePacket = this.getRemoveSidePacket();

      Player[] var5;
      for(Player p : var5 = this.server.getOnlinePlayers()) {
         if (this.isDisplaySideBar(p)) {
            this.send(p, removeSidePacket);
         }
      }

   }

   private MsgInfo getMsgInfo(String name) {
      MsgInfo msgInfo = (MsgInfo)this.msgInfoHash.get(name);
      if (msgInfo == null) {
         msgInfo = new MsgInfo((String)null, (String)null);
         this.msgInfoHash.put(name, msgInfo);
      }

      return msgInfo;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public void setDisplaySideBar(Player p, boolean display) {
      if (display) {
         if (!this.isDisplaySideBar(p)) {
            UtilPer.remove(p, CHECK_PER);
            if (this.currentShowInfo != null) {
               for(PacketContainer pc : this.getSendCreateSidePacket(this.currentShowInfo.getShow(), this.currentShowInfo.getFrom(), this.currentShowInfo.getLast())) {
                  this.send(p, pc);
               }
            }
         }
      } else if (this.isDisplaySideBar(p)) {
         UtilPer.add(p, CHECK_PER);
         if (this.currentShowInfo != null) {
            PacketContainer removeSidePacket = this.getRemoveSidePacket();
            this.send(p, removeSidePacket);
         }
      }

   }

   public boolean isDisplaySideBar(Player p) {
      return !UtilPer.hasPer(p, CHECK_PER);
   }

   public void setPrefix(Player p, String prefix) {
      if (prefix == null) {
         prefix = "";
      }

      MsgInfo msgInfo = this.getMsgInfo(p.getName());
      msgInfo.setPrefix(prefix);
      String team = (String)this.teamHash.get(p.getName());
      PacketContainer pc = this.getUpdateTeamInfoPacket(team, msgInfo.getPrefix(), msgInfo.getSuffix());

      Player[] var9;
      for(Player tar : var9 = this.server.getOnlinePlayers()) {
         this.send(tar, pc);
      }

   }

   public String getPrefix(String name) {
      MsgInfo msgInfo = this.getMsgInfo(name);
      return msgInfo.getPrefix();
   }

   public void setSuffix(Player p, String suffix) {
      if (suffix == null) {
         suffix = "";
      }

      MsgInfo msgInfo = this.getMsgInfo(p.getName());
      msgInfo.setSuffix(suffix);
      String team = (String)this.teamHash.get(p.getName());
      PacketContainer pc = this.getUpdateTeamInfoPacket(team, msgInfo.getPrefix(), msgInfo.getSuffix());

      Player[] var9;
      for(Player tar : var9 = this.server.getOnlinePlayers()) {
         this.send(tar, pc);
      }

   }

   public String getSuffix(String name) {
      MsgInfo msgInfo = this.getMsgInfo(name);
      return msgInfo.getSuffix();
   }

   public void show(String show, String from, int last) {
      ShowInfo showInfo = new ShowInfo(show.substring(0, Math.min(32, show.length())), from.substring(0, Math.min(16, from.length())), last);
      this.showList.add(showInfo);
   }

   private class MsgInfo {
      private String prefix;
      private String suffix;

      public MsgInfo(String prefix, String suffix) {
         this.prefix = prefix;
         this.suffix = suffix;
      }

      public String getPrefix() {
         return this.prefix;
      }

      public void setPrefix(String prefix) {
         this.prefix = prefix;
      }

      public String getSuffix() {
         return this.suffix;
      }

      public void setSuffix(String suffix) {
         this.suffix = suffix;
      }
   }

   private class ShowInfo {
      private String show;
      private String from;
      private int last;

      public ShowInfo(String show, String from, int last) {
         this.show = show;
         this.from = from;
         this.last = last;
      }

      public String getShow() {
         return this.show;
      }

      public String getFrom() {
         return this.from;
      }

      public int getLast() {
         return this.last;
      }
   }
}
