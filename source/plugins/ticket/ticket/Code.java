package ticket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Code implements Listener {
   private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH-mm-ss");
   private static final String SPEED_USE = "use";
   private Ticket main;
   private Dao dao;
   private Random r;
   private String pn;
   private static String cmd;
   private int interval;
   private String chars;
   private int lengthGenerateFix;
   private int maxTimes;
   private static String defaultGroup;
   private static HashMap vips;
   private static HashMap vipNames;
   private HashMap codeHash;

   public Code(Ticket main) {
      this.pn = main.getPn();
      this.main = main;
      this.dao = main.getDao();
      this.r = new Random();
      (new File(main.getPluginPath() + File.separator + this.pn + File.separator + "code")).mkdirs();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
      this.loadData();
      UtilSpeed.register(this.pn, "use");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void use(Player p, String code) {
      if (UtilSpeed.check(p, this.pn, "use", this.interval) || p.isOp()) {
         TicketCode ticketCode = (TicketCode)this.codeHash.get(code);
         if (ticketCode == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(195)}));
         } else if (ticketCode.getStatus() == 1) {
            p.sendMessage(UtilFormat.format(this.pn, "used", new Object[]{ticketCode.getUser()}));
         } else {
            int result = upgrade(p);
            if (result == -1) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(250)}));
            } else {
               ticketCode.setStatus(1);
               ticketCode.setUser(p.getName());
               ticketCode.setUseTime(System.currentTimeMillis());
               this.dao.addOrUpdateCode(ticketCode);
               String tar = (String)vipNames.get(result);
               p.sendMessage(UtilFormat.format(this.pn, "upgrade", new Object[]{tar}));
               Bukkit.broadcastMessage(UtilFormat.format(this.pn, "upgrade1", new Object[]{p.getName(), tar}));
            }
         }
      }
   }

   public void generate(CommandSender sender, int times) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || p.isOp()) {
         if (times > 0 && times <= this.maxTimes) {
            String path = this.main.getPluginPath() + File.separator + this.pn + File.separator + "code";
            (new File(path)).mkdirs();
            File logFile = new File(path + File.separator + sdf.format(new Date()) + ".txt");

            try {
               logFile.createNewFile();
               FileOutputStream fos = new FileOutputStream(logFile);
               DataOutputStream dos = new DataOutputStream(fos);
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(240)}));
               int sum = 0;
               HashList<TicketCode> successList = new HashListImpl();

               for(int i = 0; i < times; ++i) {
                  String code = this.getRandomCode();
                  if (!this.codeHash.containsKey(code)) {
                     TicketCode tc = new TicketCode(code, System.currentTimeMillis());
                     successList.add(tc);
                     this.codeHash.put(code, tc);
                     sender.sendMessage(UtilFormat.format(this.pn, "generate1", new Object[]{code}));
                     dos.write(("a " + code + "\n").getBytes());
                     ++sum;
                  }
               }

               if (successList.size() != 0) {
                  this.dao.addOrUpdateTicketCodes(successList);
               }

               dos.close();
               fos.close();
               sender.sendMessage(UtilFormat.format(this.pn, "generate2", new Object[]{times, sum}));
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }

         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "timesErr", new Object[]{this.maxTimes}));
         }
      }
   }

   public static int upgrade(Player p) {
      try {
         int max = vips.size();
         if (UtilPer.inGroup(p, (String)vips.get(max))) {
            return -1;
         }

         for(int level = max - 1; level >= 0; --level) {
            String checkGroup;
            if (level == 0) {
               checkGroup = defaultGroup;
            } else {
               checkGroup = (String)vips.get(level);
            }

            if (UtilPer.inGroup(p, checkGroup)) {
               ++level;
               String tarGroup = (String)vips.get(level);
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{0}", p.getName()).replace("{1}", tarGroup));
               GroupChangeEvent groupChangeEvent = new GroupChangeEvent(p);
               Bukkit.getPluginManager().callEvent(groupChangeEvent);
               return level;
            }
         }
      } catch (Exception var6) {
      }

      return -1;
   }

   private String getRandomCode() {
      String result = "";
      int length = this.chars.length();

      for(int i = 0; i < this.lengthGenerateFix; ++i) {
         result = result + this.chars.charAt(this.r.nextInt(length));
      }

      return result;
   }

   private void loadConfig(FileConfiguration config) {
      cmd = config.getString("cmd");
      this.interval = config.getInt("interval");
      this.chars = config.getString("chars");
      this.lengthGenerateFix = config.getInt("length");
      this.maxTimes = config.getInt("times.max");
      defaultGroup = config.getString("defaultGroup");
      vips = new HashMap();
      vipNames = new HashMap();

      for(String s : config.getStringList("vips")) {
         int level = Integer.parseInt(s.split(" ")[0]);
         String group = s.split(" ")[1];
         String name = Util.convert(s.split(" ")[2]);
         vips.put(level, group);
         vipNames.put(level, name);
      }

   }

   private void loadData() {
      this.codeHash = new HashMap();

      for(TicketCode tc : this.dao.getAllTicketCodes()) {
         this.codeHash.put(tc.getCode(), tc);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
