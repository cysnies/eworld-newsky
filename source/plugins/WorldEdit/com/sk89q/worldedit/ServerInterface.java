package com.sk89q.worldedit;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandsManager;
import java.util.Collections;
import java.util.List;

public abstract class ServerInterface {
   public abstract int resolveItem(String var1);

   public abstract boolean isValidMobType(String var1);

   public abstract void reload();

   public abstract BiomeTypes getBiomes();

   public int schedule(long delay, long period, Runnable task) {
      return -1;
   }

   public List getWorlds() {
      return Collections.emptyList();
   }

   /** @deprecated */
   @Deprecated
   public void onCommandRegistration(List commands) {
   }

   public void onCommandRegistration(List commands, CommandsManager manager) {
      this.onCommandRegistration(commands);
   }
}
