package fr.neatmonster.nocheatplus.permissions;

import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class PermissionUtil {
   public static List protectCommands(Collection commands, String permissionBase, boolean ops) {
      return protectCommands(permissionBase, commands, true, ops);
   }

   public static List protectCommands(String permissionBase, Collection ignoredCommands, boolean invertIgnored, boolean ops) {
      return protectCommands(permissionBase, ignoredCommands, invertIgnored, ops, ConfigManager.getConfigFile().getString("protection.plugins.hide.messages.unknowncommand"));
   }

   public static List protectCommands(String permissionBase, Collection ignoredCommands, boolean invertIgnored, boolean ops, String permissionMessage) {
      Set<String> checked = new HashSet();

      for(String label : ignoredCommands) {
         checked.add(CommandUtil.getCommandLabel(label, false));
      }

      PluginManager pm = Bukkit.getPluginManager();
      Permission rootPerm = pm.getPermission(permissionBase);
      if (rootPerm == null) {
         rootPerm = new Permission(permissionBase);
         pm.addPermission(rootPerm);
      }

      List<CommandProtectionEntry> changed = new LinkedList();

      for(Command command : CommandUtil.getCommands()) {
         String lcLabel = command.getLabel().trim().toLowerCase();
         if (checked != null) {
            if (checked.contains(lcLabel)) {
               if (!invertIgnored) {
                  continue;
               }
            } else if (invertIgnored) {
               continue;
            }
         }

         String cmdPermName = command.getPermission();
         boolean cmdHadPerm;
         if (cmdPermName == null) {
            cmdPermName = permissionBase + "." + lcLabel;
            command.setPermission(cmdPermName);
            cmdHadPerm = false;
         } else {
            cmdHadPerm = true;
         }

         Permission cmdPerm = pm.getPermission(cmdPermName);
         if (cmdPerm == null && !cmdHadPerm) {
            cmdPerm = new Permission(cmdPermName);
            cmdPerm.addParent(rootPerm, true);
            pm.addPermission(cmdPerm);
         }

         if (cmdHadPerm) {
            changed.add(new CommandProtectionEntry(command, lcLabel, cmdPermName, cmdPerm.getDefault(), command.getPermissionMessage()));
         } else {
            changed.add(new CommandProtectionEntry(command, lcLabel, (String)null, (PermissionDefault)null, command.getPermissionMessage()));
         }

         cmdPerm.setDefault(ops ? PermissionDefault.OP : PermissionDefault.FALSE);
         command.setPermissionMessage(permissionMessage);
      }

      return changed;
   }

   public static void addChildPermission(Collection permissions, String childPermissionName, PermissionDefault permissionDefault) {
      PluginManager pm = Bukkit.getPluginManager();
      Permission childPermission = pm.getPermission(childPermissionName);
      if (childPermission == null) {
         childPermission = new Permission(childPermissionName, "auto-generated child permission (NoCheatPlus)", permissionDefault);
         pm.addPermission(childPermission);
      }

      for(String permissionName : permissions) {
         Permission permission = pm.getPermission(permissionName);
         if (permission == null) {
            permission = new Permission(permissionName, "auto-generated permission (NoCheatPlus)", permissionDefault);
            pm.addPermission(permission);
         }

         if (!permission.getChildren().containsKey(childPermissionName)) {
            childPermission.addParent(permission, true);
         }
      }

   }

   public static class CommandProtectionEntry {
      public final Command command;
      public final String label;
      public final String permission;
      public final PermissionDefault permissionDefault;
      public final String permissionMessage;

      public CommandProtectionEntry(Command command, String label, String permission, PermissionDefault permissionDefault, String permissionMessage) {
         this.command = command;
         this.label = label;
         this.permission = permission;
         this.permissionDefault = permissionDefault;
         this.permissionMessage = permissionMessage;
      }

      public void restore() {
         Command registered = CommandUtil.getCommand(this.label);
         if (registered != null && registered == this.command) {
            if (!this.label.equalsIgnoreCase(this.command.getLabel().trim().toLowerCase())) {
               this.command.setLabel(this.label);
            }

            this.command.setPermission(this.permission);
            if (this.permission != null && this.permissionDefault != null) {
               Permission perm = Bukkit.getPluginManager().getPermission(this.permission);
               if (perm != null) {
                  perm.setDefault(this.permissionDefault);
               }
            }

            this.command.setPermissionMessage(this.permissionMessage);
         }
      }
   }
}
