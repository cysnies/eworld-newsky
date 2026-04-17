package org.hibernate.id;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface IdentifierGenerator {
   String ENTITY_NAME = "entity_name";
   String JPA_ENTITY_NAME = "jpa_entity_name";

   Serializable generate(SessionImplementor var1, Object var2) throws HibernateException;
}
