package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class SunOneJtaPlatform extends AbstractJtaPlatform {
   public static final String TM_NAME = "java:appserver/TransactionManager";
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected TransactionManager locateTransactionManager() {
      return (TransactionManager)this.jndiService().locate("java:appserver/TransactionManager");
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
