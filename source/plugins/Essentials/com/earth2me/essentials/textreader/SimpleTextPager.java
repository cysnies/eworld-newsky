package com.earth2me.essentials.textreader;

import java.util.List;
import org.bukkit.command.CommandSender;

public class SimpleTextPager {
   private final transient IText text;

   public SimpleTextPager(IText text) {
      this.text = text;
   }

   public void showPage(CommandSender sender) {
      for(String line : this.text.getLines()) {
         sender.sendMessage(line);
      }

   }

   public List getLines() {
      return this.text.getLines();
   }

   public String getLine(int line) {
      return this.text.getLines().size() < line ? null : (String)this.text.getLines().get(line);
   }
}
