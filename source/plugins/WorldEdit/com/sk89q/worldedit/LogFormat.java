package com.sk89q.worldedit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormat extends Formatter {
   public String format(LogRecord record) {
      StringBuilder text = new StringBuilder();
      Level level = record.getLevel();
      if (level == Level.FINEST) {
         text.append("[FINEST] ");
      } else if (level == Level.FINER) {
         text.append("[FINER] ");
      } else if (level == Level.FINE) {
         text.append("[FINE] ");
      } else if (level == Level.INFO) {
         text.append("[INFO] ");
      } else if (level == Level.WARNING) {
         text.append("[WARNING] ");
      } else if (level == Level.SEVERE) {
         text.append("[SEVERE] ");
      }

      text.append(record.getMessage());
      text.append("\r\n");
      Throwable t = record.getThrown();
      if (t != null) {
         StringWriter writer = new StringWriter();
         t.printStackTrace(new PrintWriter(writer));
         text.append(writer.toString());
      }

      return text.toString();
   }
}
