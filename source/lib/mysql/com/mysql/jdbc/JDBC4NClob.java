package com.mysql.jdbc;

import java.sql.NClob;

public class JDBC4NClob extends Clob implements NClob {
   JDBC4NClob() {
   }

   JDBC4NClob(String charDataInit) {
      super(charDataInit);
   }
}
