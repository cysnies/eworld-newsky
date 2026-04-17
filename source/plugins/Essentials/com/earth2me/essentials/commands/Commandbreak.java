package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

public class Commandbreak extends EssentialsCommand {
   public Commandbreak() {
      super("break");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      Block block = user.getTargetBlock((HashSet)null, 20);
      if (block == null) {
         throw new NoChargeException();
      } else if (block.getType() == Material.AIR) {
         throw new NoChargeException();
      } else if (block.getType() == Material.BEDROCK && !user.isAuthorized("essentials.break.bedrock")) {
         throw new Exception(I18n._("noBreakBedrock"));
      } else {
         BlockBreakEvent event = new BlockBreakEvent(block, user.getBase());
         server.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            throw new NoChargeException();
         } else {
            block.setType(Material.AIR);
         }
      }
   }
}
