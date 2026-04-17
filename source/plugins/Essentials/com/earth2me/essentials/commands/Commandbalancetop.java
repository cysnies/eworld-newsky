package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.textreader.ArrayListInput;
import com.earth2me.essentials.textreader.TextPager;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbalancetop extends EssentialsCommand {
   private static final int CACHETIME = 120000;
   public static final int MINUSERS = 50;
   private static ArrayListInput cache = new ArrayListInput();
   private static long cacheage = 0L;
   private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   public Commandbalancetop() {
      super("balancetop");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      int page = 0;
      boolean force = false;
      if (args.length > 0) {
         try {
            page = Integer.parseInt(args[0]);
         } catch (NumberFormatException var12) {
            if (args[0].equalsIgnoreCase("force") && sender.isOp()) {
               force = true;
            }
         }
      }

      if (!force && lock.readLock().tryLock()) {
         label92: {
            try {
               if (cacheage <= System.currentTimeMillis() - 120000L) {
                  if (this.ess.getUserMap().getUniqueUsers() > 50) {
                     sender.sendMessage(I18n._("orderBalances", this.ess.getUserMap().getUniqueUsers()));
                  }
                  break label92;
               }

               outputCache(sender, page);
            } finally {
               lock.readLock().unlock();
            }

            return;
         }

         this.ess.runTaskAsynchronously(new Viewer(sender, page, force));
      } else {
         if (this.ess.getUserMap().getUniqueUsers() > 50) {
            sender.sendMessage(I18n._("orderBalances", this.ess.getUserMap().getUniqueUsers()));
         }

         this.ess.runTaskAsynchronously(new Viewer(sender, page, force));
      }

   }

   private static void outputCache(CommandSender sender, int page) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(cacheage);
      DateFormat format = DateFormat.getDateTimeInstance(3, 3);
      sender.sendMessage(I18n._("balanceTop", format.format(cal.getTime())));
      (new TextPager(cache)).showPage(Integer.toString(page), (String)null, "balancetop", sender);
   }

   private class Calculator implements Runnable {
      private final transient Viewer viewer;
      private final boolean force;

      public Calculator(Viewer viewer, boolean force) {
         this.viewer = viewer;
         this.force = force;
      }

      public void run() {
         Commandbalancetop.lock.writeLock().lock();

         try {
            if (this.force || Commandbalancetop.cacheage <= System.currentTimeMillis() - 120000L) {
               Commandbalancetop.cache.getLines().clear();
               Map<String, Double> balances = new HashMap();
               double totalMoney = (double)0.0F;

               for(String u : Commandbalancetop.this.ess.getUserMap().getAllUniqueUsers()) {
                  User user = Commandbalancetop.this.ess.getUserMap().getUser(u);
                  if (user != null) {
                     double userMoney = user.getMoney();
                     user.updateMoneyCache(userMoney);
                     totalMoney += userMoney;
                     balances.put(user.getDisplayName(), userMoney);
                  }
               }

               List<Map.Entry<String, Double>> sortedEntries = new ArrayList(balances.entrySet());
               Collections.sort(sortedEntries, new Comparator() {
                  public int compare(Map.Entry entry1, Map.Entry entry2) {
                     return -((Double)entry1.getValue()).compareTo((Double)entry2.getValue());
                  }
               });
               Commandbalancetop.cache.getLines().add(I18n._("serverTotal", Util.displayCurrency(totalMoney, Commandbalancetop.this.ess)));
               int pos = 1;

               for(Map.Entry entry : sortedEntries) {
                  Commandbalancetop.cache.getLines().add(pos + ". " + (String)entry.getKey() + ", " + Util.displayCurrency((Double)entry.getValue(), Commandbalancetop.this.ess));
                  ++pos;
               }

               Commandbalancetop.cacheage = System.currentTimeMillis();
            }
         } finally {
            Commandbalancetop.lock.writeLock().unlock();
         }

         Commandbalancetop.this.ess.runTaskAsynchronously(this.viewer);
      }
   }

   private class Viewer implements Runnable {
      private final transient CommandSender sender;
      private final transient int page;
      private final transient boolean force;

      public Viewer(CommandSender sender, int page, boolean force) {
         this.sender = sender;
         this.page = page;
         this.force = force;
      }

      public void run() {
         Commandbalancetop.lock.readLock().lock();

         label43: {
            try {
               if (this.force || Commandbalancetop.cacheage <= System.currentTimeMillis() - 120000L) {
                  break label43;
               }

               Commandbalancetop.outputCache(this.sender, this.page);
            } finally {
               Commandbalancetop.lock.readLock().unlock();
            }

            return;
         }

         Commandbalancetop.this.ess.runTaskAsynchronously(Commandbalancetop.this.new Calculator(Commandbalancetop.this.new Viewer(this.sender, this.page, this.force), this.force));
      }
   }
}
