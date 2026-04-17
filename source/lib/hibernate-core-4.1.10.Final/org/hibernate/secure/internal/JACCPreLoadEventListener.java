package org.hibernate.secure.internal;

import javax.security.jacc.EJBMethodPermission;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;

public class JACCPreLoadEventListener implements PreLoadEventListener, JACCSecurityListener {
   private final String contextId;

   public JACCPreLoadEventListener(String contextId) {
      this.contextId = contextId;
   }

   public void onPreLoad(PreLoadEvent event) {
      EJBMethodPermission loadPermission = new EJBMethodPermission(event.getPersister().getEntityName(), "read", (String)null, (String[])null);
      JACCPermissions.checkPermission(event.getEntity().getClass(), this.contextId, loadPermission);
   }
}
