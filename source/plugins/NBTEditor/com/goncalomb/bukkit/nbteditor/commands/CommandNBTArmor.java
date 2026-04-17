package com.goncalomb.bukkit.nbteditor.commands;

import com.goncalomb.bukkit.betterplugin.BetterCommand;
import com.goncalomb.bukkit.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.betterplugin.BetterCommandType;
import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.betterplugin.SubCommand;
import java.awt.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class CommandNBTArmor extends BetterCommand {
   public CommandNBTArmor() {
      super("nbtarmor", "nbteditor.armor");
      this.setAlises(new String[]{"nbta"});
      this.setDescription(Lang._("nbt.cmds.nbta.description"));
   }

   @SubCommand.Command(
      args = "",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 1,
      usage = "<color>"
   )
   public boolean potionCommand(CommandSender sender, String[] args) throws BetterCommandException {
      if (args.length > 0) {
         HandItemWrapper.LeatherArmor item = new HandItemWrapper.LeatherArmor((Player)sender);
         if (!args[0].startsWith("#")) {
            args[0] = "#" + args[0];
         }

         if (args[0].length() == 7) {
            try {
               Color color = Color.decode(args[0]);
               ((LeatherArmorMeta)item.meta).setColor(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()));
               item.save();
               sender.sendMessage(Lang._("nbt.cmds.nbta.ok"));
               return true;
            } catch (NumberFormatException var5) {
            }
         }

         sender.sendMessage(Lang._("nbt.cmds.nbta.nop"));
      }

      sender.sendMessage(Lang._("nbt.cmds.nbta.info"));
      return false;
   }
}
