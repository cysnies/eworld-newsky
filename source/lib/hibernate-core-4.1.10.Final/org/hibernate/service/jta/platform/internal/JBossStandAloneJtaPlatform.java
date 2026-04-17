package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;

public class JBossStandAloneJtaPlatform extends AbstractJtaPlatform {
   private static final String JBOSS_TM_CLASS_NAME = "com.arjuna.ats.jta.TransactionManager";
   private static final String JBOSS_UT_CLASS_NAME = "com.arjuna.ats.jta.UserTransaction";

   protected TransactionManager locateTransactionManager() {
      try {
         Class jbossTmClass = ((ClassLoaderService)this.serviceRegistry().getService(ClassLoaderService.class)).classForName("com.arjuna.ats.jta.TransactionManager");
         return (TransactionManager)jbossTmClass.getMethod("transactionManager").invoke((Object)null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not obtain JBoss Transactions transaction manager instance", e);
      }
   }

   protected UserTransaction locateUserTransaction() {
      try {
         Class jbossUtClass = ((ClassLoaderService)this.serviceRegistry().getService(ClassLoaderService.class)).classForName("com.arjuna.ats.jta.UserTransaction");
         return (UserTransaction)jbossUtClass.getMethod("userTransaction").invoke((Object)null);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not obtain JBoss Transactions user transaction instance", e);
      }
   }
}
