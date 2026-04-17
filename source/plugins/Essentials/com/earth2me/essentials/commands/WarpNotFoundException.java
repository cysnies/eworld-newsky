package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;

public class WarpNotFoundException extends Exception {
   public WarpNotFoundException() {
      super(I18n._("warpNotExist"));
   }

   public WarpNotFoundException(String message) {
      super(message);
   }
}
