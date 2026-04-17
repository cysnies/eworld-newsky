package org.hibernate.service.jdbc.connections.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.HibernateException;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.InjectService;
import org.hibernate.service.spi.Stoppable;

public class DatasourceConnectionProviderImpl implements ConnectionProvider, Configurable, Stoppable {
   private DataSource dataSource;
   private String user;
   private String pass;
   private boolean useCredentials;
   private JndiService jndiService;
   private boolean available;

   public DataSource getDataSource() {
      return this.dataSource;
   }

   public void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
   }

   @InjectService(
      required = false
   )
   public void setJndiService(JndiService jndiService) {
      this.jndiService = jndiService;
   }

   public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType) || DatasourceConnectionProviderImpl.class.isAssignableFrom(unwrapType) || DataSource.class.isAssignableFrom(unwrapType);
   }

   public Object unwrap(Class unwrapType) {
      if (!ConnectionProvider.class.equals(unwrapType) && !DatasourceConnectionProviderImpl.class.isAssignableFrom(unwrapType)) {
         if (DataSource.class.isAssignableFrom(unwrapType)) {
            return this.getDataSource();
         } else {
            throw new UnknownUnwrapTypeException(unwrapType);
         }
      } else {
         return this;
      }
   }

   public void configure(Map configValues) {
      if (this.dataSource == null) {
         Object dataSource = configValues.get("hibernate.connection.datasource");
         if (DataSource.class.isInstance(dataSource)) {
            this.dataSource = (DataSource)dataSource;
         } else {
            String dataSourceJndiName = (String)dataSource;
            if (dataSourceJndiName == null) {
               throw new HibernateException("DataSource to use was not injected nor specified by [hibernate.connection.datasource] configuration property");
            }

            if (this.jndiService == null) {
               throw new HibernateException("Unable to locate JndiService to lookup Datasource");
            }

            this.dataSource = (DataSource)this.jndiService.locate(dataSourceJndiName);
         }
      }

      if (this.dataSource == null) {
         throw new HibernateException("Unable to determine appropriate DataSource to use");
      } else {
         this.user = (String)configValues.get("hibernate.connection.username");
         this.pass = (String)configValues.get("hibernate.connection.password");
         this.useCredentials = this.user != null || this.pass != null;
         this.available = true;
      }
   }

   public void stop() {
      this.available = false;
      this.dataSource = null;
   }

   public Connection getConnection() throws SQLException {
      if (!this.available) {
         throw new HibernateException("Provider is closed!");
      } else {
         return this.useCredentials ? this.dataSource.getConnection(this.user, this.pass) : this.dataSource.getConnection();
      }
   }

   public void closeConnection(Connection connection) throws SQLException {
      connection.close();
   }

   public boolean supportsAggressiveRelease() {
      return true;
   }
}
