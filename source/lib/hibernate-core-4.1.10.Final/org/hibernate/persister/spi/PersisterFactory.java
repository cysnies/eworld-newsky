package org.hibernate.persister.spi;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.Service;

public interface PersisterFactory extends Service {
   EntityPersister createEntityPersister(PersistentClass var1, EntityRegionAccessStrategy var2, NaturalIdRegionAccessStrategy var3, SessionFactoryImplementor var4, Mapping var5) throws HibernateException;

   EntityPersister createEntityPersister(EntityBinding var1, EntityRegionAccessStrategy var2, SessionFactoryImplementor var3, Mapping var4) throws HibernateException;

   CollectionPersister createCollectionPersister(Configuration var1, Collection var2, CollectionRegionAccessStrategy var3, SessionFactoryImplementor var4) throws HibernateException;

   CollectionPersister createCollectionPersister(MetadataImplementor var1, PluralAttributeBinding var2, CollectionRegionAccessStrategy var3, SessionFactoryImplementor var4) throws HibernateException;
}
