package org.hibernate.exception.spi;

import java.sql.SQLException;

public interface ViolatedConstraintNameExtracter {
   String extractConstraintName(SQLException var1);
}
