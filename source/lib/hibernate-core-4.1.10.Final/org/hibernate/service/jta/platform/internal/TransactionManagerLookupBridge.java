package org.hibernate.service.jta.platform.internal;

import java.util.Properties;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.transaction.TransactionManagerLookup;

public class TransactionManagerLookupBridge extends AbstractJtaPlatform {
   private final TransactionManagerLookup lookup;
   private final Properties jndiProperties;

   public TransactionManagerLookupBridge(TransactionManagerLookup lookup, Properties jndiProperties) {
      this.lookup = lookup;
      this.jndiProperties = jndiProperties;
   }

   protected TransactionManager locateTransactionManager() {
      return this.lookup.getTransactionManager(this.jndiProperties);
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)((JndiService)this.serviceRegistry().getService(JndiService.class)).locate(this.lookup.getUserTransactionName());
   }

   public Object getTransactionIdentifier(Transaction transaction) {
      return this.lookup.getTransactionIdentifier(transaction);
   }
}
