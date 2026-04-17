package com.goncalomb.bukkit.betterplugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class BetterCommand extends SubCommand {
   InternalCommand _internalCommand;
   Plugin _plugin;

   public BetterCommand(String name) {
      this(name, (String)null);
   }

   public BetterCommand(String name, String permission) {
      this._base = this;
      this._internalCommand = new InternalCommand(this, name);
      this._internalCommand.setPermission(permission);
      this._internalCommand.setPermissionMessage(Lang._("common.commands.no-perm"));
      Method[] methods = this.getClass().getDeclaredMethods();

      for(Method method : methods) {
         SubCommand.Command config = (SubCommand.Command)method.getAnnotation(SubCommand.Command.class);
         if (config != null) {
            Class[] params = method.getParameterTypes();
            if (params.length == 2 && method.getReturnType() == Boolean.TYPE && params[0] == CommandSender.class && params[1] == String[].class) {
               this.addSubCommand(config, method);
            }
         }
      }

   }

   public void setDescription(String description) {
      this._internalCommand.setDescription(description);
   }

   public void setAlises(String... aliases) {
      this._internalCommand.setAliases(Arrays.asList(aliases));
   }

   public void setAlises(List aliases) {
      this._internalCommand.setAliases(aliases);
   }

   public Plugin getPlugin() {
      return this._plugin;
   }

   boolean invokeMethod(Method method, CommandSender sender, String[] args) {
      try {
         return (Boolean)method.invoke(this, sender, args);
      } catch (InvocationTargetException e) {
         if (e.getCause() instanceof BetterCommandException) {
            sender.sendMessage(e.getCause().getMessage());
            return true;
         } else {
            throw new RuntimeException(e.getCause());
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
