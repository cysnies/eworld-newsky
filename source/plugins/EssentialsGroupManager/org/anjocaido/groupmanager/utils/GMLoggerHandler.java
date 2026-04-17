package org.anjocaido.groupmanager.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GMLoggerHandler extends ConsoleHandler {
   public void publish(LogRecord record) {
      String message = "GroupManager - " + record.getLevel() + " - " + record.getMessage();
      if (!record.getLevel().equals(Level.SEVERE) && !record.getLevel().equals(Level.WARNING)) {
         System.out.println(message);
      } else {
         System.err.println(message);
      }

   }
}
