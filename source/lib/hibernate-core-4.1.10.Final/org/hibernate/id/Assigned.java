package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public class Assigned implements IdentifierGenerator, Configurable {
   private String entityName;

   public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
      Serializable id = session.getEntityPersister(this.entityName, obj).getIdentifier(obj, session);
      if (id == null) {
         throw new IdentifierGenerationException("ids for this class must be manually assigned before calling save(): " + this.entityName);
      } else {
         return id;
      }
   }

   public void configure(Type type, Properties params, Dialect d) throws MappingException {
      this.entityName = params.getProperty("entity_name");
      if (this.entityName == null) {
         throw new MappingException("no entity name");
      }
   }
}
