package org.hibernate.service.jdbc.connections.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.sql.DataSource;
import org.hibernate.HibernateException;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;

public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl implements ServiceRegistryAwareService, Stoppable {
   public static final String TENANT_IDENTIFIER_TO_USE_FOR_ANY_KEY = "hibernate.multi_tenant.datasource.identifier_for_any";
   private Map dataSourceMap;
   private JndiService jndiService;
   private String tenantIdentifierForAny;
   private String baseJndiNamespace;

   protected DataSource selectAnyDataSource() {
      return this.selectDataSource(this.tenantIdentifierForAny);
   }

   protected DataSource selectDataSource(String tenantIdentifier) {
      DataSource dataSource = (DataSource)this.dataSourceMap().get(tenantIdentifier);
      if (dataSource == null) {
         dataSource = (DataSource)this.jndiService.locate(this.baseJndiNamespace + '/' + tenantIdentifier);
         this.dataSourceMap().put(tenantIdentifier, dataSource);
      }

      return dataSource;
   }

   private Map dataSourceMap() {
      if (this.dataSourceMap == null) {
         this.dataSourceMap = new ConcurrentHashMap();
      }

      return this.dataSourceMap;
   }

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      Object dataSourceConfigValue = ((ConfigurationService)serviceRegistry.getService(ConfigurationService.class)).getSettings().get("hibernate.connection.datasource");
      if (dataSourceConfigValue != null && String.class.isInstance(dataSourceConfigValue)) {
         String jndiName = (String)dataSourceConfigValue;
         this.jndiService = (JndiService)serviceRegistry.getService(JndiService.class);
         if (this.jndiService == null) {
            throw new HibernateException("Could not locate JndiService from DataSourceBasedMultiTenantConnectionProviderImpl");
         } else {
            Object namedObject = this.jndiService.locate(jndiName);
            if (namedObject == null) {
               throw new HibernateException("JNDI name [" + jndiName + "] could not be resolved");
            } else {
               if (DataSource.class.isInstance(namedObject)) {
                  int loc = jndiName.lastIndexOf("/");
                  this.baseJndiNamespace = jndiName.substring(0, loc);
                  this.tenantIdentifierForAny = jndiName.substring(loc + 1);
                  this.dataSourceMap().put(this.tenantIdentifierForAny, (DataSource)namedObject);
               } else {
                  if (!Context.class.isInstance(namedObject)) {
                     throw new HibernateException("Unknown object type [" + namedObject.getClass().getName() + "] found in JNDI location [" + jndiName + "]");
                  }

                  this.baseJndiNamespace = jndiName;
                  this.tenantIdentifierForAny = (String)((ConfigurationService)serviceRegistry.getService(ConfigurationService.class)).getSettings().get("hibernate.multi_tenant.datasource.identifier_for_any");
                  if (this.tenantIdentifierForAny == null) {
                     throw new HibernateException("JNDI name named a Context, but tenant identifier to use for ANY was not specified");
                  }
               }

            }
         }
      } else {
         throw new HibernateException("Improper set up of DataSourceBasedMultiTenantConnectionProviderImpl");
      }
   }

   public void stop() {
      if (this.dataSourceMap != null) {
         this.dataSourceMap.clear();
         this.dataSourceMap = null;
      }

   }
}
