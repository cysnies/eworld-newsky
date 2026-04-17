package org.hibernate.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.HibernateException;

public interface Expectation {
   void verifyOutcome(int var1, PreparedStatement var2, int var3) throws SQLException, HibernateException;

   int prepare(PreparedStatement var1) throws SQLException, HibernateException;

   boolean canBeBatched();
}
