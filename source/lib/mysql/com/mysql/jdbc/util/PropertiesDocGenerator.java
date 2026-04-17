package com.mysql.jdbc.util;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import java.sql.SQLException;

public class PropertiesDocGenerator extends ConnectionPropertiesImpl {
   public static void main(String[] args) throws SQLException {
      System.out.println((new PropertiesDocGenerator()).exposeAsXml());
   }
}
