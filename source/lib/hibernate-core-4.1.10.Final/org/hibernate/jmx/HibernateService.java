package org.hibernate.jmx;

import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.ExternalSessionFactoryConfig;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.jndi.JndiHelper;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class HibernateService extends ExternalSessionFactoryConfig implements HibernateServiceMBean {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HibernateService.class.getName());
   private String boundName;
   private Properties properties = new Properties();

   public void start() throws HibernateException {
      this.boundName = this.getJndiName();

      try {
         this.buildSessionFactory();
      } catch (HibernateException he) {
         LOG.unableToBuildSessionFactoryUsingMBeanClasspath(he.getMessage());
         LOG.debug("Error was", he);
         new SessionFactoryStub(this);
      }

   }

   public void stop() {
      LOG.stoppingService();

      try {
         InitialContext context = JndiHelper.getInitialContext(this.buildProperties());
         ((SessionFactory)context.lookup(this.boundName)).close();
      } catch (Exception e) {
         LOG.unableToStopHibernateService(e);
      }

   }

   SessionFactory buildSessionFactory() throws HibernateException {
      LOG.startingServiceAtJndiName(this.boundName);
      LOG.serviceProperties(this.properties);
      return this.buildConfiguration().buildSessionFactory((new ServiceRegistryBuilder()).applySettings(this.properties).buildServiceRegistry());
   }

   protected Map getExtraProperties() {
      return this.properties;
   }

   public String getTransactionStrategy() {
      return this.getProperty("hibernate.transaction.factory_class");
   }

   public void setTransactionStrategy(String txnStrategy) {
      this.setProperty("hibernate.transaction.factory_class", txnStrategy);
   }

   public String getUserTransactionName() {
      return this.getProperty("jta.UserTransaction");
   }

   public void setUserTransactionName(String utName) {
      this.setProperty("jta.UserTransaction", utName);
   }

   public String getJtaPlatformName() {
      return this.getProperty("hibernate.transaction.jta.platform");
   }

   public void setJtaPlatformName(String name) {
      this.setProperty("hibernate.transaction.jta.platform", name);
   }

   public String getPropertyList() {
      return this.buildProperties().toString();
   }

   public String getProperty(String property) {
      return this.properties.getProperty(property);
   }

   public void setProperty(String property, String value) {
      this.properties.setProperty(property, value);
   }

   public void dropSchema() {
      (new SchemaExport(this.buildConfiguration())).drop(false, true);
   }

   public void createSchema() {
      (new SchemaExport(this.buildConfiguration())).create(false, true);
   }

   public String getName() {
      return this.getProperty("hibernate.session_factory_name");
   }

   public String getDatasource() {
      return this.getProperty("hibernate.connection.datasource");
   }

   public void setDatasource(String datasource) {
      this.setProperty("hibernate.connection.datasource", datasource);
   }

   public String getJndiName() {
      return this.getProperty("hibernate.session_factory_name");
   }

   public void setJndiName(String jndiName) {
      this.setProperty("hibernate.session_factory_name", jndiName);
   }

   public String getUserName() {
      return this.getProperty("hibernate.connection.username");
   }

   public void setUserName(String userName) {
      this.setProperty("hibernate.connection.username", userName);
   }

   public String getPassword() {
      return this.getProperty("hibernate.connection.password");
   }

   public void setPassword(String password) {
      this.setProperty("hibernate.connection.password", password);
   }

   public void setFlushBeforeCompletionEnabled(String enabled) {
      this.setProperty("hibernate.transaction.flush_before_completion", enabled);
   }

   public String getFlushBeforeCompletionEnabled() {
      return this.getProperty("hibernate.transaction.flush_before_completion");
   }

   public void setAutoCloseSessionEnabled(String enabled) {
      this.setProperty("hibernate.transaction.auto_close_session", enabled);
   }

   public String getAutoCloseSessionEnabled() {
      return this.getProperty("hibernate.transaction.auto_close_session");
   }

   public Properties getProperties() {
      return this.buildProperties();
   }
}
