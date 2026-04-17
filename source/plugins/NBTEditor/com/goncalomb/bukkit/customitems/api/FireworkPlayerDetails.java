package com.goncalomb.bukkit.customitems.api;

import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;

public final class FireworkPlayerDetails extends DelayedPlayerDetails {
   private Firework _firework;

   static FireworkPlayerDetails fromConsumableDetails(IConsumableDetails details, Firework firework, Object userObject) {
      return details instanceof PlayerDetails ? new FireworkPlayerDetails((PlayerDetails)details, firework, userObject) : new FireworkPlayerDetails(details, firework, userObject);
   }

   private FireworkPlayerDetails(IConsumableDetails details, Firework firework, Object userObject) {
      super(details.getItem(), (Player)null);
      this._firework = firework;
      this._userObject = userObject;
   }

   private FireworkPlayerDetails(PlayerDetails details, Firework firework, Object userObject) {
      super(details._item, details._player);
      this._firework = firework;
      this._userObject = userObject;
   }

   public Firework getFirework() {
      return this._firework;
   }
}
