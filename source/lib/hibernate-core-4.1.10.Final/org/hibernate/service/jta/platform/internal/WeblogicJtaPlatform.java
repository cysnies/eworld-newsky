package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class WeblogicJtaPlatform extends AbstractJtaPlatform {
   public static final String TM_NAME = "javax.transaction.TransactionManager";
   public static final String UT_NAME = "javax.transaction.UserTransaction";

   protected TransactionManager locateTransactionManager() {
      return (TransactionManager)this.jndiService().locate("javax.transaction.TransactionManager");
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("javax.transaction.UserTransaction");
   }
}
