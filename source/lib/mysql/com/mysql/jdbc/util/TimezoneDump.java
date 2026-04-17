package com.mysql.jdbc.util;

import com.mysql.jdbc.TimeUtil;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TimezoneDump {
   private static final String DEFAULT_URL = "jdbc:mysql:///test";

   public static void main(String[] args) throws Exception {
      String jdbcUrl = "jdbc:mysql:///test";
      if (args.length == 1 && args[0] != null) {
         jdbcUrl = args[0];
      }

      Class.forName("com.mysql.jdbc.Driver").newInstance();
      ResultSet rs = DriverManager.getConnection(jdbcUrl).createStatement().executeQuery("SHOW VARIABLES LIKE 'timezone'");

      while(rs.next()) {
         String timezoneFromServer = rs.getString(2);
         System.out.println("MySQL timezone name: " + timezoneFromServer);
         String canonicalTimezone = TimeUtil.getCanoncialTimezone(timezoneFromServer);
         System.out.println("Java timezone name: " + canonicalTimezone);
      }

   }
}
