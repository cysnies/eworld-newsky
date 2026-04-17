package com.comphenix.protocol.events;

import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

class SerializedOfflinePlayer implements OfflinePlayer, Serializable {
   private static final long serialVersionUID = -2728976288470282810L;
   private transient Location bedSpawnLocation;
   private String name;
   private long firstPlayed;
   private long lastPlayed;
   private boolean operator;
   private boolean banned;
   private boolean playedBefore;
   private boolean online;
   private boolean whitelisted;
   private static Map lookup = new ConcurrentHashMap();

   public SerializedOfflinePlayer() {
   }

   public SerializedOfflinePlayer(OfflinePlayer offline) {
      this.name = offline.getName();
      this.firstPlayed = offline.getFirstPlayed();
      this.lastPlayed = offline.getLastPlayed();
      this.operator = offline.isOp();
      this.banned = offline.isBanned();
      this.playedBefore = offline.hasPlayedBefore();
      this.online = offline.isOnline();
      this.whitelisted = offline.isWhitelisted();
   }

   public boolean isOp() {
      return this.operator;
   }

   public void setOp(boolean operator) {
      this.operator = operator;
   }

   public Map serialize() {
      throw new UnsupportedOperationException();
   }

   public Location getBedSpawnLocation() {
      return this.bedSpawnLocation;
   }

   public long getFirstPlayed() {
      return this.firstPlayed;
   }

   public long getLastPlayed() {
      return this.lastPlayed;
   }

   public String getName() {
      return this.name;
   }

   public boolean hasPlayedBefore() {
      return this.playedBefore;
   }

   public boolean isBanned() {
      return this.banned;
   }

   public void setBanned(boolean banned) {
      this.banned = banned;
   }

   public boolean isOnline() {
      return this.online;
   }

   public boolean isWhitelisted() {
      return this.whitelisted;
   }

   public void setWhitelisted(boolean whitelisted) {
      this.whitelisted = whitelisted;
   }

   private void writeObject(ObjectOutputStream output) throws IOException {
      output.defaultWriteObject();
      output.writeUTF(this.bedSpawnLocation.getWorld().getName());
      output.writeDouble(this.bedSpawnLocation.getX());
      output.writeDouble(this.bedSpawnLocation.getY());
      output.writeDouble(this.bedSpawnLocation.getZ());
   }

   private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
      input.defaultReadObject();
      this.bedSpawnLocation = new Location(this.getWorld(input.readUTF()), input.readDouble(), input.readDouble(), input.readDouble());
   }

   private World getWorld(String name) {
      try {
         return Bukkit.getServer().getWorld(name);
      } catch (Exception var3) {
         return null;
      }
   }

   public Player getPlayer() {
      try {
         return Bukkit.getServer().getPlayerExact(this.name);
      } catch (Exception var2) {
         return this.getProxyPlayer();
      }
   }

   public Player getProxyPlayer() {
      if (lookup.size() == 0) {
         for(Method method : OfflinePlayer.class.getMethods()) {
            lookup.put(method.getName(), method);
         }
      }

      Enhancer ex = new Enhancer();
      ex.setSuperclass(Player.class);
      ex.setCallback(new MethodInterceptor() {
         public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Method offlineMethod = (Method)SerializedOfflinePlayer.lookup.get(method.getName());
            if (offlineMethod == null) {
               throw new UnsupportedOperationException("The method " + method.getName() + " is not supported for offline players.");
            } else {
               return offlineMethod.invoke(SerializedOfflinePlayer.this, args);
            }
         }
      });
      return (Player)ex.create();
   }
}
