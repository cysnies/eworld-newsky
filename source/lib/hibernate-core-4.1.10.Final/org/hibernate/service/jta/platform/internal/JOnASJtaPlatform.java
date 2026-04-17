package org.hibernate.service.jta.platform.internal;

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;

public class JOnASJtaPlatform extends AbstractJtaPlatform {
   public static final String UT_NAME = "java:comp/UserTransaction";
   private static final String TM_CLASS_NAME = "org.objectweb.jonas_tm.Current";

   protected TransactionManager locateTransactionManager() {
      try {
         Class clazz = Class.forName("org.objectweb.jonas_tm.Current");
         Method getTransactionManagerMethod = clazz.getMethod("getTransactionManager");
         return (TransactionManager)getTransactionManagerMethod.invoke((Object)null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not obtain JOnAS transaction manager instance", e);
      }
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
