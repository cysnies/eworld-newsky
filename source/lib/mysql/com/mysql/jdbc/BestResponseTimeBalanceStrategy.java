package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BestResponseTimeBalanceStrategy implements BalanceStrategy {
   public void destroy() {
   }

   public void init(Connection conn, Properties props) throws SQLException {
   }

   public Connection pickConnection(LoadBalancingConnectionProxy proxy, List configuredHosts, Map liveConnections, long[] responseTimes, int numRetries) throws SQLException {
      long minResponseTime = Long.MAX_VALUE;
      int bestHostIndex = 0;
      Map blackList = new HashMap(configuredHosts.size());
      SQLException ex = null;
      int attempts = 0;

      Connection conn;
      while(true) {
         if (attempts >= numRetries) {
            if (ex != null) {
               throw ex;
            }

            return null;
         }

         if (blackList.size() == configuredHosts.size()) {
            blackList.clear();
         }

         for(int i = 0; i < responseTimes.length; ++i) {
            long candidateResponseTime = responseTimes[i];
            if (candidateResponseTime < minResponseTime && !blackList.containsKey(configuredHosts.get(i))) {
               if (candidateResponseTime == 0L) {
                  bestHostIndex = i;
                  break;
               }

               bestHostIndex = i;
               minResponseTime = candidateResponseTime;
            }
         }

         String bestHost = (String)configuredHosts.get(bestHostIndex);
         conn = (Connection)liveConnections.get(bestHost);
         if (conn != null) {
            break;
         }

         try {
            conn = proxy.createConnectionForHost(bestHost);
            break;
         } catch (SQLException sqlEx) {
            ex = sqlEx;
            if (!(sqlEx instanceof CommunicationsException) && !"08S01".equals(sqlEx.getSQLState())) {
               throw sqlEx;
            }

            blackList.put(bestHost, (Object)null);
            if (blackList.size() == configuredHosts.size()) {
               blackList.clear();

               try {
                  Thread.sleep(250L);
               } catch (InterruptedException var16) {
               }
            }

            ++attempts;
         }
      }

      return conn;
   }
}
