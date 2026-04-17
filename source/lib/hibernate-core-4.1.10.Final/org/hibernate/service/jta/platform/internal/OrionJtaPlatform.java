package org.hibernate.service.jta.platform.internal;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class OrionJtaPlatform extends AbstractJtaPlatform {
   public static final String TM_NAME = "java:comp/UserTransaction";
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected TransactionManager locateTransactionManager() {
      return (TransactionManager)this.jndiService().locate("java:comp/UserTransaction");
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }
}
