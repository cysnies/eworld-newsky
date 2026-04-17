package event;

import land.Land;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLandChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Player p;
   private HashList preLandList;
   private HashList nowLandList;

   public PlayerLandChangeEvent(Player p, HashList preLandList, HashList nowLandList) {
      this.p = p;
      this.preLandList = preLandList;
      this.nowLandList = nowLandList;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Player getPlayer() {
      return this.p;
   }

   public HashList getPreLandList() {
      return this.preLandList;
   }

   public HashList getNowLandList() {
      return this.nowLandList;
   }

   public HashList getEnterList() {
      HashList<Land> enterHash = new HashListImpl();

      for(Land land : this.nowLandList) {
         if (!this.preLandList.has(land)) {
            enterHash.add(land);
         }
      }

      return enterHash;
   }

   public HashList getLeaveList() {
      HashList<Land> leaveHash = new HashListImpl();

      for(Land land : this.preLandList) {
         if (!this.nowLandList.has(land)) {
            leaveHash.add(land);
         }
      }

      return leaveHash;
   }
}
