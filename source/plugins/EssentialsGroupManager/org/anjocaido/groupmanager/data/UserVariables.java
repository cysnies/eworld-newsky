package org.anjocaido.groupmanager.data;

import java.util.Map;

public class UserVariables extends Variables {
   private User owner;

   public UserVariables(User owner) {
      super(owner);
      this.owner = owner;
   }

   public UserVariables(User owner, Map varList) {
      super(owner);
      this.variables.clear();
      this.variables.putAll(varList);
      this.owner = owner;
   }

   protected UserVariables clone(User newOwner) {
      UserVariables clone = new UserVariables(newOwner);
      synchronized(this.variables) {
         for(String key : this.variables.keySet()) {
            clone.variables.put(key, this.variables.get(key));
         }
      }

      newOwner.flagAsChanged();
      return clone;
   }

   public User getOwner() {
      return this.owner;
   }
}
