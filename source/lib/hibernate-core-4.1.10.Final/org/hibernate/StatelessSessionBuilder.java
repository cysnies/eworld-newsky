package org.hibernate;

import java.sql.Connection;

public interface StatelessSessionBuilder {
   StatelessSession openStatelessSession();

   StatelessSessionBuilder connection(Connection var1);

   StatelessSessionBuilder tenantIdentifier(String var1);
}
