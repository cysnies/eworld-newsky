package org.hibernate.proxy;

import java.io.Serializable;

public interface EntityNotFoundDelegate {
   void handleEntityNotFound(String var1, Serializable var2);
}
