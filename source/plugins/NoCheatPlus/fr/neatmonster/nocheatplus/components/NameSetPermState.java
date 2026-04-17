package fr.neatmonster.nocheatplus.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NameSetPermState implements PermStateReceiver {
   protected final HashMap playerSets = new HashMap();
   protected String[] defaultPermissions;

   public NameSetPermState(String... permissions) {
      this.defaultPermissions = permissions;
   }

   public String[] getDefaultPermissions() {
      return this.defaultPermissions;
   }

   public boolean hasPermission(String player, String permission) {
      Set<String> names = (Set)this.playerSets.get(permission);
      return names == null ? false : names.contains(player);
   }

   public void setPermission(String player, String permission, boolean state) {
      Set<String> names = (Set)this.playerSets.get(permission);
      if (names == null) {
         if (!state) {
            return;
         }

         names = new LinkedHashSet(20);
         this.playerSets.put(permission, names);
      }

      if (state) {
         names.add(player);
      } else {
         names.remove(player);
      }

   }

   public void removePlayer(String player) {
      Iterator<Map.Entry<String, Set<String>>> it = this.playerSets.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<String, Set<String>> entry = (Map.Entry)it.next();
         Set<String> set = (Set)entry.getValue();
         set.remove(player);
         if (set.isEmpty()) {
            it.remove();
         }
      }

   }

   public Set getPlayers(String permission) {
      return (Set)this.playerSets.get(permission);
   }

   public void addDefaultPermissions(Collection permissions) {
      Collection<String> newDefaults = new HashSet();
      newDefaults.addAll(Arrays.asList(this.defaultPermissions));
      newDefaults.addAll(permissions);
   }

   public void setDefaultPermissions(Collection permissions) {
      this.defaultPermissions = new String[permissions.size()];
      permissions.toArray(this.defaultPermissions);
   }
}
