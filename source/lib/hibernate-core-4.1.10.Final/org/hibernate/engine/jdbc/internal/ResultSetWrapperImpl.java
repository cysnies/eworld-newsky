package org.hibernate.engine.jdbc.internal;

import java.sql.ResultSet;
import org.hibernate.engine.jdbc.ColumnNameCache;
import org.hibernate.engine.jdbc.ResultSetWrapperProxy;
import org.hibernate.engine.jdbc.spi.ResultSetWrapper;

public class ResultSetWrapperImpl implements ResultSetWrapper {
   public static ResultSetWrapper INSTANCE = new ResultSetWrapperImpl();

   private ResultSetWrapperImpl() {
   }

   public ResultSet wrap(ResultSet resultSet, ColumnNameCache columnNameCache) {
      return ResultSetWrapperProxy.generateProxy(resultSet, columnNameCache);
   }
}
