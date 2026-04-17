package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RandomBalanceStrategy implements BalanceStrategy {
   public void destroy() {
   }

   public void init(Connection conn, Properties props) throws SQLException {
   }

   public Connection pickConnection(LoadBalancingConnectionProxy proxy, List configuredHosts, Map liveConnections, long[] responseTimes, int numRetries) throws SQLException {
      int numHosts = configuredHosts.size();
      SQLException ex = null;
      Map whiteListMap = new HashMap(numHosts);
      List whiteList = new ArrayList(numHosts);
      whiteList.addAll(configuredHosts);

      for(int i = 0; i < numHosts; ++i) {
         whiteListMap.put(whiteList.get(i), new Integer(i));
      }

      int attempts = 0;

      Connection conn;
      while(true) {
         if (attempts >= numRetries) {
            if (ex != null) {
               throw ex;
            }

            return null;
         }

         int random = (int)(Math.random() * (double)whiteList.size());
         if (random == whiteList.size()) {
            --random;
         }

         String hostPortSpec = (String)whiteList.get(random);
         conn = (Connection)liveConnections.get(hostPortSpec);
         if (conn != null) {
            break;
         }

         try {
            conn = proxy.createConnectionForHost(hostPortSpec);
            break;
         } catch (SQLException sqlEx) {
            ex = sqlEx;
            if (!(sqlEx instanceof CommunicationsException) && !"08S01".equals(sqlEx.getSQLState())) {
               throw sqlEx;
            }

            Integer whiteListIndex = (Integer)whiteListMap.get(hostPortSpec);
            if (whiteListIndex != null) {
               whiteList.remove(whiteListIndex);
            }

            if (whiteList.size() == 0) {
               try {
                  Thread.sleep(250L);
               } catch (InterruptedException var17) {
               }

               whiteList.addAll(configuredHosts);
            }

            ++attempts;
         }
      }

      return conn;
   }
}
