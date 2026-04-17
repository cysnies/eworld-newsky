package net.milkbowl.vault.economy;

import java.util.List;

public interface Economy {
   boolean isEnabled();

   String getName();

   boolean hasBankSupport();

   int fractionalDigits();

   String format(double var1);

   String currencyNamePlural();

   String currencyNameSingular();

   boolean hasAccount(String var1);

   boolean hasAccount(String var1, String var2);

   double getBalance(String var1);

   double getBalance(String var1, String var2);

   boolean has(String var1, double var2);

   boolean has(String var1, String var2, double var3);

   EconomyResponse withdrawPlayer(String var1, double var2);

   EconomyResponse withdrawPlayer(String var1, String var2, double var3);

   EconomyResponse depositPlayer(String var1, double var2);

   EconomyResponse depositPlayer(String var1, String var2, double var3);

   EconomyResponse createBank(String var1, String var2);

   EconomyResponse deleteBank(String var1);

   EconomyResponse bankBalance(String var1);

   EconomyResponse bankHas(String var1, double var2);

   EconomyResponse bankWithdraw(String var1, double var2);

   EconomyResponse bankDeposit(String var1, double var2);

   EconomyResponse isBankOwner(String var1, String var2);

   EconomyResponse isBankMember(String var1, String var2);

   List getBanks();

   boolean createPlayerAccount(String var1);

   boolean createPlayerAccount(String var1, String var2);
}
