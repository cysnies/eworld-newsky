package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class OC4JJtaPlatform extends AbstractJtaPlatform {
   public static final String TM_NAME = "java:comp/pm/TransactionManager";
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected TransactionManager locateTransactionManager() {
      return (TransactionManager)this.jndiService().locate("java:comp/pm/TransactionManager");
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
