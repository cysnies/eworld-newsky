package org.hibernate.secure.internal;

import javax.security.jacc.EJBMethodPermission;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;

public class JACCPreInsertEventListener implements PreInsertEventListener, JACCSecurityListener {
   private final String contextId;

   public JACCPreInsertEventListener(String contextId) {
      this.contextId = contextId;
   }

   public boolean onPreInsert(PreInsertEvent event) {
      EJBMethodPermission insertPermission = new EJBMethodPermission(event.getPersister().getEntityName(), "insert", (String)null, (String[])null);
      JACCPermissions.checkPermission(event.getEntity().getClass(), this.contextId, insertPermission);
      return false;
   }
}
