package lib.types;

import lib.util.Util;

public class ItemElement extends TypeElement {
   private int id;
   private short smallId;

   public ItemElement(String s) {
      super(s);
      if (s.indexOf(":") != -1) {
         try {
            String type = s.split(":")[0];

            try {
               this.id = Integer.parseInt(type);
            } catch (NumberFormatException var5) {
               this.id = Util.getMaterial(type).getId();
            }

            this.smallId = (short)Integer.parseInt(s.split(":")[1]);
         } catch (NumberFormatException e) {
            e.printStackTrace();
         }
      } else {
         try {
            this.id = Integer.parseInt(s);
         } catch (NumberFormatException var4) {
            this.id = Util.getMaterial(s).getId();
         }
      }

   }

   public int getId() {
      return this.id;
   }

   public short getSmallId() {
      return this.smallId;
   }

   public int hashCode() {
      return this.id + this.smallId;
   }

   public boolean equals(Object obj) {
      ItemElement itemElement = (ItemElement)obj;
      return this.id == itemElement.getId() && this.smallId == itemElement.getSmallId();
   }
}
