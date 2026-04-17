package com.earth2me.essentials.api;

import com.earth2me.essentials.I18n;

public class UserDoesNotExistException extends Exception {
   public UserDoesNotExistException(String name) {
      super(I18n._("userDoesNotExist", name));
   }
}
