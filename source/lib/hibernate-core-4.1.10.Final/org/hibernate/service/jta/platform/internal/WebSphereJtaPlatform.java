package org.hibernate.service.jta.platform.internal;

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;
import org.jboss.logging.Logger;

public class WebSphereJtaPlatform extends AbstractJtaPlatform {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, WebSphereJtaPlatform.class.getName());
   public static final String VERSION_5_UT_NAME = "java:comp/UserTransaction";
   public static final String VERSION_4_UT_NAME = "jta/usertransaction";
   private final Class transactionManagerAccessClass;
   private final int webSphereVersion;

   public WebSphereJtaPlatform() {
      try {
         Class clazz;
         int version;
         try {
            clazz = Class.forName("com.ibm.ws.Transaction.TransactionManagerFactory");
            version = 5;
            LOG.debug("WebSphere 5.1");
         } catch (Exception var6) {
            try {
               clazz = Class.forName("com.ibm.ejs.jts.jta.TransactionManagerFactory");
               version = 5;
               LOG.debug("WebSphere 5.0");
            } catch (Exception var5) {
               clazz = Class.forName("com.ibm.ejs.jts.jta.JTSXA");
               version = 4;
               LOG.debug("WebSphere 4");
            }
         }

         this.transactionManagerAccessClass = clazz;
         this.webSphereVersion = version;
      } catch (Exception e) {
         throw new JtaPlatformException("Could not locate WebSphere TransactionManager access class", e);
      }
   }

   protected TransactionManager locateTransactionManager() {
      try {
         Method method = this.transactionManagerAccessClass.getMethod("getTransactionManager");
         return (TransactionManager)method.invoke((Object)null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not obtain WebSphere TransactionManager", e);
      }
   }

   protected UserTransaction locateUserTransaction() {
      String utName = this.webSphereVersion == 5 ? "java:comp/UserTransaction" : "jta/usertransaction";
      return (UserTransaction)this.jndiService().locate(utName);
   }
}
