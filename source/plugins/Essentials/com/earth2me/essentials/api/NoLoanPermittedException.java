package com.earth2me.essentials.api;

import com.earth2me.essentials.I18n;

public class NoLoanPermittedException extends Exception {
   public NoLoanPermittedException() {
      super(I18n._("negativeBalanceError"));
   }
}
