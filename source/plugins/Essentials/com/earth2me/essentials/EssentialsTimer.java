package com.earth2me.essentials;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class EssentialsTimer implements Runnable {
   private final transient IEssentials ess;
   private final transient Set onlineUsers = new HashSet();
   private transient long lastPoll = System.currentTimeMillis();
   private final transient LinkedList history = new LinkedList();

   EssentialsTimer(IEssentials ess) {
      this.ess = ess;
   }

   public void run() {
      long currentTime = System.currentTimeMillis();
      long timeSpent = (currentTime - this.lastPoll) / 1000L;
      if (timeSpent == 0L) {
         timeSpent = 1L;
      }

      if (this.history.size() > 10) {
         this.history.remove();
      }

      float tps = 100.0F / (float)timeSpent;
      if (tps <= 20.0F) {
         this.history.add(tps);
      }

      this.lastPoll = currentTime;

      for(Player player : this.ess.getServer().getOnlinePlayers()) {
         try {
            User user = this.ess.getUser(player);
            this.onlineUsers.add(user);
            user.setLastOnlineActivity(currentTime);
            user.checkActivity();
         } catch (Exception e) {
            this.ess.getLogger().log(Level.WARNING, "EssentialsTimer Error:", e);
         }
      }

      Iterator<User> iterator = this.onlineUsers.iterator();

      while(iterator.hasNext()) {
         User user = (User)iterator.next();
         if (user.getLastOnlineActivity() < currentTime && user.getLastOnlineActivity() > user.getLastLogout()) {
            user.setLastLogout(user.getLastOnlineActivity());
            iterator.remove();
         } else {
            user.checkMuteTimeout(currentTime);
            user.checkJailTimeout(currentTime);
            user.resetInvulnerabilityAfterTeleport();
         }
      }

   }

   public float getAverageTPS() {
      float avg = 0.0F;

      for(Float f : this.history) {
         if (f != null) {
            avg += f;
         }
      }

      return avg / (float)this.history.size();
   }
}
