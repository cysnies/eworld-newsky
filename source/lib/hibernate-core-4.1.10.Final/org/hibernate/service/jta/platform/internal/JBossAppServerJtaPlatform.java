package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.jndi.JndiException;

public class JBossAppServerJtaPlatform extends AbstractJtaPlatform {
   public static final String AS7_TM_NAME = "java:jboss/TransactionManager";
   public static final String AS4_TM_NAME = "java:/TransactionManager";
   public static final String JBOSS__UT_NAME = "java:jboss/UserTransaction";
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected boolean canCacheUserTransactionByDefault() {
      return true;
   }

   protected boolean canCacheTransactionManagerByDefault() {
      return true;
   }

   protected TransactionManager locateTransactionManager() {
      try {
         return (TransactionManager)this.jndiService().locate("java:jboss/TransactionManager");
      } catch (JndiException jndiException) {
         try {
            return (TransactionManager)this.jndiService().locate("java:/TransactionManager");
         } catch (JndiException var3) {
            throw new JndiException("unable to find transaction manager", jndiException);
         }
      }
   }

   protected UserTransaction locateUserTransaction() {
      try {
         return (UserTransaction)this.jndiService().locate("java:jboss/UserTransaction");
      } catch (JndiException jndiException) {
         try {
            return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
         } catch (JndiException var3) {
            throw new JndiException("unable to find UserTransaction", jndiException);
         }
      }
   }
}
