package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

public class CommandAction extends ActionWithParameters {
   public CommandAction(String name, int delay, int repeat, String command) {
      super(name, delay, repeat, command);
   }

   public boolean execute(ParameterHolder violationData) {
      String command = super.getMessage(violationData);

      try {
         Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
      } catch (CommandException e) {
         LogUtil.logWarning("[NoCheatPlus] Failed to execute the command '" + command + "': " + e.getMessage() + ", please check if everything is setup correct.");
      } catch (Exception var5) {
      }

      return false;
   }

   public String toString() {
      return "cmd:" + this.name + ":" + this.delay + ":" + this.repeat;
   }
}
