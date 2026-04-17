package org.anjocaido.groupmanager.data;

import java.util.Map;

public class GroupVariables extends Variables implements Cloneable {
   private Group owner;

   public GroupVariables(Group owner) {
      super(owner);
      this.owner = owner;
      this.addVar("prefix", "");
      this.addVar("suffix", "");
      this.addVar("build", false);
   }

   public GroupVariables(Group owner, Map varList) {
      super(owner);
      this.variables.clear();
      this.variables.putAll(varList);
      if (this.variables.get("prefix") == null) {
         this.variables.put("prefix", "");
         owner.flagAsChanged();
      }

      if (this.variables.get("suffix") == null) {
         this.variables.put("suffix", "");
         owner.flagAsChanged();
      }

      if (this.variables.get("build") == null) {
         this.variables.put("build", false);
         owner.flagAsChanged();
      }

      this.owner = owner;
   }

   protected GroupVariables clone(Group newOwner) {
      GroupVariables clone = new GroupVariables(newOwner);
      synchronized(this.variables) {
         for(String key : this.variables.keySet()) {
            clone.variables.put(key, this.variables.get(key));
         }
      }

      newOwner.flagAsChanged();
      return clone;
   }

   public void removeVar(String name) {
      try {
         this.variables.remove(name);
      } catch (Exception var3) {
      }

      if (name.equals("prefix")) {
         this.addVar("prefix", "");
      } else if (name.equals("suffix")) {
         this.addVar("suffix", "");
      } else if (name.equals("build")) {
         this.addVar("build", false);
      }

      this.owner.flagAsChanged();
   }

   public Group getOwner() {
      return this.owner;
   }
}
