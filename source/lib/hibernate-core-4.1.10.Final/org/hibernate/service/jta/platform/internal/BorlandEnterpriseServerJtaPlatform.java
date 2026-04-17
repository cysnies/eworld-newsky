package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class BorlandEnterpriseServerJtaPlatform extends AbstractJtaPlatform {
   protected static final String TM_NAME = "java:pm/TransactionManager";
   protected static final String UT_NAME = "java:comp/UserTransaction";

   protected TransactionManager locateTransactionManager() {
      return (TransactionManager)this.jndiService().locate("java:pm/TransactionManager");
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
