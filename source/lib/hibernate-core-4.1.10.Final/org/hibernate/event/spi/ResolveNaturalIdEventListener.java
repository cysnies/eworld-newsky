package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface ResolveNaturalIdEventListener extends Serializable {
   void onResolveNaturalId(ResolveNaturalIdEvent var1) throws HibernateException;
}
