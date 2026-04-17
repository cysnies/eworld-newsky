package org.hibernate.service.jta.platform.internal;

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;

public class BitronixJtaPlatform extends AbstractJtaPlatform {
   private static final String TM_CLASS_NAME = "bitronix.tm.TransactionManagerServices";

   protected TransactionManager locateTransactionManager() {
      try {
         Class transactionManagerServicesClass = ((ClassLoaderService)this.serviceRegistry().getService(ClassLoaderService.class)).classForName("bitronix.tm.TransactionManagerServices");
         Method getTransactionManagerMethod = transactionManagerServicesClass.getMethod("getTransactionManager");
         return (TransactionManager)getTransactionManagerMethod.invoke((Object)null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not locate Bitronix TransactionManager", e);
      }
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
