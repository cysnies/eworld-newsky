package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IntegralDataTypeHolder extends Serializable {
   IntegralDataTypeHolder initialize(long var1);

   IntegralDataTypeHolder initialize(ResultSet var1, long var2) throws SQLException;

   void bind(PreparedStatement var1, int var2) throws SQLException;

   IntegralDataTypeHolder increment();

   IntegralDataTypeHolder add(long var1);

   IntegralDataTypeHolder decrement();

   IntegralDataTypeHolder subtract(long var1);

   IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder var1);

   IntegralDataTypeHolder multiplyBy(long var1);

   boolean eq(IntegralDataTypeHolder var1);

   boolean eq(long var1);

   boolean lt(IntegralDataTypeHolder var1);

   boolean lt(long var1);

   boolean gt(IntegralDataTypeHolder var1);

   boolean gt(long var1);

   IntegralDataTypeHolder copy();

   Number makeValue();

   Number makeValueThenIncrement();

   Number makeValueThenAdd(long var1);
}
