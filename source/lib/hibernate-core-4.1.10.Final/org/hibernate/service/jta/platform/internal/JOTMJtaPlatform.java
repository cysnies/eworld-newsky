package org.hibernate.service.jta.platform.internal;

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;

public class JOTMJtaPlatform extends AbstractJtaPlatform {
   public static final String TM_CLASS_NAME = "org.objectweb.jotm.Current";
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected TransactionManager locateTransactionManager() {
      try {
         Class tmClass = ((ClassLoaderService)this.serviceRegistry().getService(ClassLoaderService.class)).classForName("org.objectweb.jotm.Current");
         Method getTransactionManagerMethod = tmClass.getMethod("getTransactionManager");
         return (TransactionManager)getTransactionManagerMethod.invoke((Object)null, (Object[])null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not obtain JOTM transaction manager instance", e);
      }
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
