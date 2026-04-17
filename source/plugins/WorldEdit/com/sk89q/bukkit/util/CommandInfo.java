package com.sk89q.bukkit.util;

public class CommandInfo {
   private final String[] aliases;
   private final Object registeredWith;
   private final String usage;
   private final String desc;
   private final String[] permissions;

   public CommandInfo(String usage, String desc, String[] aliases, Object registeredWith) {
      this(usage, desc, aliases, registeredWith, (String[])null);
   }

   public CommandInfo(String usage, String desc, String[] aliases, Object registeredWith, String[] permissions) {
      this.usage = usage;
      this.desc = desc;
      this.aliases = aliases;
      this.permissions = permissions;
      this.registeredWith = registeredWith;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public String getName() {
      return this.aliases[0];
   }

   public String getUsage() {
      return this.usage;
   }

   public String getDesc() {
      return this.desc;
   }

   public String[] getPermissions() {
      return this.permissions;
   }

   public Object getRegisteredWith() {
      return this.registeredWith;
   }
}
