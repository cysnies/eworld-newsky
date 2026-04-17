package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

public class NonRegisteringReplicationDriver extends NonRegisteringDriver {
   public NonRegisteringReplicationDriver() throws SQLException {
   }

   public java.sql.Connection connect(String url, Properties info) throws SQLException {
      Properties parsedProps = this.parseURL(url, info);
      if (parsedProps == null) {
         return null;
      } else {
         Properties masterProps = (Properties)parsedProps.clone();
         Properties slavesProps = (Properties)parsedProps.clone();
         slavesProps.setProperty("com.mysql.jdbc.ReplicationConnection.isSlave", "true");
         String hostValues = parsedProps.getProperty("HOST");
         if (hostValues != null) {
            StringTokenizer st = new StringTokenizer(hostValues, ",");
            StringBuffer masterHost = new StringBuffer();
            StringBuffer slaveHosts = new StringBuffer();
            if (st.hasMoreTokens()) {
               String[] hostPortPair = parseHostPortPair(st.nextToken());
               if (hostPortPair[0] != null) {
                  masterHost.append(hostPortPair[0]);
               }

               if (hostPortPair[1] != null) {
                  masterHost.append(":");
                  masterHost.append(hostPortPair[1]);
               }
            }

            boolean firstSlaveHost = true;

            while(st.hasMoreTokens()) {
               String[] hostPortPair = parseHostPortPair(st.nextToken());
               if (!firstSlaveHost) {
                  slaveHosts.append(",");
               } else {
                  firstSlaveHost = false;
               }

               if (hostPortPair[0] != null) {
                  slaveHosts.append(hostPortPair[0]);
               }

               if (hostPortPair[1] != null) {
                  slaveHosts.append(":");
                  slaveHosts.append(hostPortPair[1]);
               }
            }

            if (slaveHosts.length() == 0) {
               throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", "01S00");
            }

            masterProps.setProperty("HOST", masterHost.toString());
            slavesProps.setProperty("HOST", slaveHosts.toString());
         }

         return new ReplicationConnection(masterProps, slavesProps);
      }
   }
}
