package org.maxgamer.QuickShop.Metrics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.ShopPurchaseEvent;

public class ShopListener implements Listener {
   int sales = 0;
   int purchases = 0;

   public ShopListener() {
      Metrics metrics = QuickShop.instance.getMetrics();
      Metrics.Graph graph = metrics.createGraph("Sales vs Purchases");
      graph.addPlotter(new Metrics.Plotter("Sales") {
         public int getValue() {
            int oldsales = ShopListener.this.sales;
            ShopListener.this.sales = 0;
            return oldsales;
         }
      });
      graph.addPlotter(new Metrics.Plotter("Purchases") {
         public int getValue() {
            int oldpurchases = ShopListener.this.purchases;
            ShopListener.this.purchases = 0;
            return oldpurchases;
         }
      });
   }

   @EventHandler
   public void onPurchase(ShopPurchaseEvent e) {
      if (e.getShop().isSelling()) {
         this.sales += e.getAmount();
      } else {
         this.purchases += e.getAmount();
      }

   }
}
