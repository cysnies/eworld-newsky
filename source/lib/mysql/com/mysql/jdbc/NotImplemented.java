package com.mysql.jdbc;

import java.sql.SQLException;

public class NotImplemented extends SQLException {
   public NotImplemented() {
      super(Messages.getString("NotImplemented.0"), "S1C00");
   }
}
