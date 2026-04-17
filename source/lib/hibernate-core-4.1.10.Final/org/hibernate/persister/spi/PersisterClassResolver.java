package org.hibernate.persister.spi;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.service.Service;

public interface PersisterClassResolver extends Service {
   Class getEntityPersisterClass(PersistentClass var1);

   Class getEntityPersisterClass(EntityBinding var1);

   Class getCollectionPersisterClass(Collection var1);

   Class getCollectionPersisterClass(PluralAttributeBinding var1);
}
