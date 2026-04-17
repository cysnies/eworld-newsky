package org.hibernate.secure.internal;

import javax.security.jacc.EJBMethodPermission;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;

public class JACCPreUpdateEventListener implements PreUpdateEventListener, JACCSecurityListener {
   private final String contextId;

   public JACCPreUpdateEventListener(String contextId) {
      this.contextId = contextId;
   }

   public boolean onPreUpdate(PreUpdateEvent event) {
      EJBMethodPermission updatePermission = new EJBMethodPermission(event.getPersister().getEntityName(), "update", (String)null, (String[])null);
      JACCPermissions.checkPermission(event.getEntity().getClass(), this.contextId, updatePermission);
      return false;
   }
}
