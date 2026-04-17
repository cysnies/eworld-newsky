package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Comparator;

class UserNameComparator implements Comparator {
   public int compare(User a, User b) {
      return a.getName().compareTo(b.getName());
   }
}
