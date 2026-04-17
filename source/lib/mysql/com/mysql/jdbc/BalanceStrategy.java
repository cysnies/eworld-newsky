package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BalanceStrategy extends Extension {
   Connection pickConnection(LoadBalancingConnectionProxy var1, List var2, Map var3, long[] var4, int var5) throws SQLException;
}
