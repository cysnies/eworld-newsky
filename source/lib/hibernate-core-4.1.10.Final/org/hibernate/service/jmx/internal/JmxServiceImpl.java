package org.hibernate.service.jmx.internal;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.hibernate.HibernateException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.Service;
import org.hibernate.service.jmx.spi.JmxService;
import org.hibernate.service.spi.Manageable;
import org.hibernate.service.spi.Stoppable;
import org.jboss.logging.Logger;

public class JmxServiceImpl implements JmxService, Stoppable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JmxServiceImpl.class.getName());
   public static final String OBJ_NAME_TEMPLATE = "%s:sessionFactory=%s,serviceRole=%s,serviceType=%s";
   private final boolean usePlatformServer;
   private final String agentId;
   private final String defaultDomain;
   private final String sessionFactoryName;
   private boolean startedServer;
   private ArrayList registeredMBeans;

   public JmxServiceImpl(Map configValues) {
      this.usePlatformServer = ConfigurationHelper.getBoolean("hibernate.jmx.usePlatformServer", configValues);
      this.agentId = (String)configValues.get("hibernate.jmx.agentId");
      this.defaultDomain = (String)configValues.get("hibernate.jmx.defaultDomain");
      this.sessionFactoryName = ConfigurationHelper.getString("hibernate.jmx.sessionFactoryName", configValues, ConfigurationHelper.getString("hibernate.session_factory_name", configValues));
   }

   public void stop() {
      try {
         if (!this.startedServer && this.registeredMBeans == null) {
            return;
         }

         MBeanServer mBeanServer = this.findServer();
         if (mBeanServer != null) {
            if (this.registeredMBeans != null) {
               for(ObjectName objectName : this.registeredMBeans) {
                  try {
                     LOG.tracev("Unregistering registered MBean [ON={0}]", objectName);
                     mBeanServer.unregisterMBean(objectName);
                  } catch (Exception e) {
                     LOG.debugf("Unable to unregsiter registered MBean [ON=%s] : %s", objectName, e.toString());
                  }
               }
            }

            if (this.startedServer) {
               LOG.trace("Attempting to release created MBeanServer");

               try {
                  MBeanServerFactory.releaseMBeanServer(mBeanServer);
               } catch (Exception e) {
                  LOG.unableToReleaseCreatedMBeanServer(e.toString());
               }

               return;
            }

            return;
         }

         LOG.unableToLocateMBeanServer();
      } finally {
         this.startedServer = false;
         if (this.registeredMBeans != null) {
            this.registeredMBeans.clear();
            this.registeredMBeans = null;
         }

      }

   }

   public void registerService(Manageable service, Class serviceRole) {
      String domain = service.getManagementDomain() == null ? "org.hibernate.core" : service.getManagementDomain();
      String serviceType = service.getManagementServiceType() == null ? service.getClass().getName() : service.getManagementServiceType();

      try {
         ObjectName objectName = new ObjectName(String.format("%s:sessionFactory=%s,serviceRole=%s,serviceType=%s", domain, this.sessionFactoryName, serviceRole.getName(), serviceType));
         this.registerMBean(objectName, service.getManagementBean());
      } catch (MalformedObjectNameException e) {
         throw new HibernateException("Unable to generate service IbjectName", e);
      }
   }

   public void registerMBean(ObjectName objectName, Object mBean) {
      MBeanServer mBeanServer = this.findServer();
      if (mBeanServer == null) {
         if (this.startedServer) {
            throw new HibernateException("Could not locate previously started MBeanServer");
         }

         mBeanServer = this.startMBeanServer();
         this.startedServer = true;
      }

      try {
         mBeanServer.registerMBean(mBean, objectName);
         if (this.registeredMBeans == null) {
            this.registeredMBeans = new ArrayList();
         }

         this.registeredMBeans.add(objectName);
      } catch (Exception e) {
         throw new HibernateException("Unable to register MBean [ON=" + objectName + "]", e);
      }
   }

   private MBeanServer findServer() {
      if (this.usePlatformServer) {
         return ManagementFactory.getPlatformMBeanServer();
      } else {
         ArrayList<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(this.agentId);
         if (this.defaultDomain == null) {
            return (MBeanServer)mbeanServers.get(0);
         } else {
            for(MBeanServer mbeanServer : mbeanServers) {
               if (this.defaultDomain.equals(mbeanServer.getDefaultDomain())) {
                  return mbeanServer;
               }
            }

            return null;
         }
      }
   }

   private MBeanServer startMBeanServer() {
      try {
         return MBeanServerFactory.createMBeanServer(this.defaultDomain);
      } catch (Exception e) {
         throw new HibernateException("Unable to start MBeanServer", e);
      }
   }
}
