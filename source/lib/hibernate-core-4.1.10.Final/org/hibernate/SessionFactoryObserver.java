package org.hibernate;

import java.io.Serializable;

public interface SessionFactoryObserver extends Serializable {
   void sessionFactoryCreated(SessionFactory var1);

   void sessionFactoryClosed(SessionFactory var1);
}
