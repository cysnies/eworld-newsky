package com.earth2me.essentials.chat;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class ChatStore {
   private final User user;
   private final String type;
   private final Trade charge;
   private long radius;

   ChatStore(IEssentials ess, User user, String type) {
      this.user = user;
      this.type = type;
      this.charge = new Trade(this.getLongType(), ess);
   }

   public User getUser() {
      return this.user;
   }

   public Trade getCharge() {
      return this.charge;
   }

   public String getType() {
      return this.type;
   }

   public String getLongType() {
      return this.type.length() == 0 ? "chat" : "chat-" + this.type;
   }

   public long getRadius() {
      return this.radius;
   }

   public void setRadius(long radius) {
      this.radius = radius;
   }
}
