package org.hibernate.metamodel;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.EntityNotFoundDelegate;

public interface SessionFactoryBuilder {
   SessionFactoryBuilder with(Interceptor var1);

   SessionFactoryBuilder with(EntityNotFoundDelegate var1);

   SessionFactory buildSessionFactory();
}
