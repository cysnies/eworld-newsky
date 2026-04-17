package org.anjocaido.groupmanager.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Variables implements Cloneable {
   private DataUnit owner;
   protected final Map variables = Collections.synchronizedMap(new HashMap());

   public Variables(DataUnit owner) {
      this.owner = owner;
   }

   public void addVar(String name, Object o) {
      if (o != null) {
         if (this.variables.containsKey(name)) {
            this.variables.remove(name);
         }

         this.variables.put(name, o);
         this.owner.flagAsChanged();
      }
   }

   public Object getVarObject(String name) {
      return this.variables.get(name);
   }

   public String getVarString(String name) {
      Object o = this.variables.get(name);

      try {
         return o == null ? "" : o.toString();
      } catch (Exception var4) {
         return "";
      }
   }

   public Boolean getVarBoolean(String name) {
      Object o = this.variables.get(name);

      try {
         return o == null ? false : Boolean.parseBoolean(o.toString());
      } catch (Exception var4) {
         return false;
      }
   }

   public Integer getVarInteger(String name) {
      Object o = this.variables.get(name);

      try {
         return o == null ? -1 : Integer.parseInt(o.toString());
      } catch (Exception var4) {
         return -1;
      }
   }

   public Double getVarDouble(String name) {
      Object o = this.variables.get(name);

      try {
         return o == null ? (double)-1.0F : Double.parseDouble(o.toString());
      } catch (Exception var4) {
         return (double)-1.0F;
      }
   }

   public String[] getVarKeyList() {
      synchronized(this.variables) {
         return (String[])this.variables.keySet().toArray(new String[0]);
      }
   }

   public boolean hasVar(String name) {
      return this.variables.containsKey(name);
   }

   public int getSize() {
      return this.variables.size();
   }

   public void removeVar(String name) {
      try {
         this.variables.remove(name);
      } catch (Exception var3) {
      }

      this.owner.flagAsChanged();
   }

   public static Object parseVariableValue(String value) {
      try {
         Integer i = Integer.parseInt(value);
         return i;
      } catch (NumberFormatException var3) {
         try {
            Double d = Double.parseDouble(value);
            return d;
         } catch (NumberFormatException var2) {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("yes") && !value.equalsIgnoreCase("on")) {
               return !value.equalsIgnoreCase("false") && !value.equalsIgnoreCase("no") && !value.equalsIgnoreCase("off") ? value : false;
            } else {
               return true;
            }
         }
      }
   }

   public void clearVars() {
      this.variables.clear();
      this.owner.flagAsChanged();
   }

   public DataUnit getOwner() {
      return this.owner;
   }

   public boolean isEmpty() {
      return this.variables.isEmpty();
   }
}
