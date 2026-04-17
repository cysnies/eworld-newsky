package org.hibernate.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.cache.internal.RegionFactoryInitiator;
import org.hibernate.engine.jdbc.batch.internal.BatchBuilderInitiator;
import org.hibernate.engine.jdbc.internal.JdbcServicesInitiator;
import org.hibernate.engine.transaction.internal.TransactionFactoryInitiator;
import org.hibernate.id.factory.internal.MutableIdentifierGeneratorFactoryInitiator;
import org.hibernate.persister.internal.PersisterClassResolverInitiator;
import org.hibernate.persister.internal.PersisterFactoryInitiator;
import org.hibernate.service.config.internal.ConfigurationServiceInitiator;
import org.hibernate.service.internal.SessionFactoryServiceRegistryFactoryInitiator;
import org.hibernate.service.jdbc.connections.internal.ConnectionProviderInitiator;
import org.hibernate.service.jdbc.connections.internal.MultiTenantConnectionProviderInitiator;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryInitiator;
import org.hibernate.service.jdbc.dialect.internal.DialectResolverInitiator;
import org.hibernate.service.jmx.internal.JmxServiceInitiator;
import org.hibernate.service.jndi.internal.JndiServiceInitiator;
import org.hibernate.service.jta.platform.internal.JtaPlatformInitiator;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.tool.hbm2ddl.ImportSqlCommandExtractorInitiator;

public class StandardServiceInitiators {
   public static List LIST = buildStandardServiceInitiatorList();

   private static List buildStandardServiceInitiatorList() {
      List<BasicServiceInitiator> serviceInitiators = new ArrayList();
      serviceInitiators.add(ConfigurationServiceInitiator.INSTANCE);
      serviceInitiators.add(ImportSqlCommandExtractorInitiator.INSTANCE);
      serviceInitiators.add(JndiServiceInitiator.INSTANCE);
      serviceInitiators.add(JmxServiceInitiator.INSTANCE);
      serviceInitiators.add(PersisterClassResolverInitiator.INSTANCE);
      serviceInitiators.add(PersisterFactoryInitiator.INSTANCE);
      serviceInitiators.add(ConnectionProviderInitiator.INSTANCE);
      serviceInitiators.add(MultiTenantConnectionProviderInitiator.INSTANCE);
      serviceInitiators.add(DialectResolverInitiator.INSTANCE);
      serviceInitiators.add(DialectFactoryInitiator.INSTANCE);
      serviceInitiators.add(BatchBuilderInitiator.INSTANCE);
      serviceInitiators.add(JdbcServicesInitiator.INSTANCE);
      serviceInitiators.add(MutableIdentifierGeneratorFactoryInitiator.INSTANCE);
      serviceInitiators.add(JtaPlatformInitiator.INSTANCE);
      serviceInitiators.add(TransactionFactoryInitiator.INSTANCE);
      serviceInitiators.add(SessionFactoryServiceRegistryFactoryInitiator.INSTANCE);
      serviceInitiators.add(RegionFactoryInitiator.INSTANCE);
      return Collections.unmodifiableList(serviceInitiators);
   }
}
