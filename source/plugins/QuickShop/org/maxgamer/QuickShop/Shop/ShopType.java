package org.maxgamer.QuickShop.Shop;

public enum ShopType {
   SELLING,
   BUYING;

   public static ShopType fromID(int id) {
      if (id == 0) {
         return SELLING;
      } else {
         return id == 1 ? BUYING : null;
      }
   }

   public static int toID(ShopType shopType) {
      if (shopType == SELLING) {
         return 0;
      } else {
         return shopType == BUYING ? 1 : -1;
      }
   }

   public int toID() {
      return toID(this);
   }
}
