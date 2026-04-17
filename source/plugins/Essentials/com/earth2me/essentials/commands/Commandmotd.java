package com.earth2me.essentials.commands;

import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandmotd extends EssentialsCommand {
   public Commandmotd() {
      super("motd");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      IText input = new TextInput(sender, "motd", true, this.ess);
      IText output = new KeywordReplacer(input, sender, this.ess);
      TextPager pager = new TextPager(output);
      pager.showPage(args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null, commandLabel, sender);
   }
}
