package org.hibernate.proxy;

import java.io.Serializable;

public interface HibernateProxy extends Serializable {
   Object writeReplace();

   LazyInitializer getHibernateLazyInitializer();
}
