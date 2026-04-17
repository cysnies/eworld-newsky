package org.hibernate.secure.internal;

import javax.security.jacc.EJBMethodPermission;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;

public class JACCPreDeleteEventListener implements PreDeleteEventListener, JACCSecurityListener {
   private final String contextId;

   public JACCPreDeleteEventListener(String contextId) {
      this.contextId = contextId;
   }

   public boolean onPreDelete(PreDeleteEvent event) {
      EJBMethodPermission deletePermission = new EJBMethodPermission(event.getPersister().getEntityName(), "delete", (String)null, (String[])null);
      JACCPermissions.checkPermission(event.getEntity().getClass(), this.contextId, deletePermission);
      return false;
   }
}
