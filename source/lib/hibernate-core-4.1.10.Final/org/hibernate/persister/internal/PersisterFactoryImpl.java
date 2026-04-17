package org.hibernate.persister.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.AbstractPluralAttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public final class PersisterFactoryImpl implements PersisterFactory, ServiceRegistryAwareService {
   public static final Class[] ENTITY_PERSISTER_CONSTRUCTOR_ARGS = new Class[]{PersistentClass.class, EntityRegionAccessStrategy.class, NaturalIdRegionAccessStrategy.class, SessionFactoryImplementor.class, Mapping.class};
   public static final Class[] ENTITY_PERSISTER_CONSTRUCTOR_ARGS_NEW = new Class[]{EntityBinding.class, EntityRegionAccessStrategy.class, NaturalIdRegionAccessStrategy.class, SessionFactoryImplementor.class, Mapping.class};
   private static final Class[] COLLECTION_PERSISTER_CONSTRUCTOR_ARGS = new Class[]{Collection.class, CollectionRegionAccessStrategy.class, Configuration.class, SessionFactoryImplementor.class};
   private static final Class[] COLLECTION_PERSISTER_CONSTRUCTOR_ARGS_NEW = new Class[]{AbstractPluralAttributeBinding.class, CollectionRegionAccessStrategy.class, MetadataImplementor.class, SessionFactoryImplementor.class};
   private ServiceRegistryImplementor serviceRegistry;

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
   }

   public EntityPersister createEntityPersister(PersistentClass metadata, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping cfg) {
      Class<? extends EntityPersister> persisterClass = metadata.getEntityPersisterClass();
      if (persisterClass == null) {
         persisterClass = ((PersisterClassResolver)this.serviceRegistry.getService(PersisterClassResolver.class)).getEntityPersisterClass(metadata);
      }

      return create(persisterClass, ENTITY_PERSISTER_CONSTRUCTOR_ARGS, metadata, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory, cfg);
   }

   public EntityPersister createEntityPersister(EntityBinding metadata, EntityRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory, Mapping cfg) {
      Class<? extends EntityPersister> persisterClass = metadata.getCustomEntityPersisterClass();
      if (persisterClass == null) {
         persisterClass = ((PersisterClassResolver)this.serviceRegistry.getService(PersisterClassResolver.class)).getEntityPersisterClass(metadata);
      }

      return create(persisterClass, ENTITY_PERSISTER_CONSTRUCTOR_ARGS_NEW, metadata, cacheAccessStrategy, (NaturalIdRegionAccessStrategy)null, factory, cfg);
   }

   private static EntityPersister create(Class persisterClass, Class[] persisterConstructorArgs, Object metadata, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping cfg) throws HibernateException {
      try {
         Constructor<? extends EntityPersister> constructor = persisterClass.getConstructor(persisterConstructorArgs);

         try {
            return (EntityPersister)constructor.newInstance(metadata, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory, cfg);
         } catch (MappingException e) {
            throw e;
         } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof HibernateException) {
               throw (HibernateException)target;
            } else {
               throw new MappingException("Could not instantiate persister " + persisterClass.getName(), target);
            }
         } catch (Exception e) {
            throw new MappingException("Could not instantiate persister " + persisterClass.getName(), e);
         }
      } catch (MappingException e) {
         throw e;
      } catch (Exception e) {
         throw new MappingException("Could not get constructor for " + persisterClass.getName(), e);
      }
   }

   public CollectionPersister createCollectionPersister(Configuration cfg, Collection collectionMetadata, CollectionRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
      Class<? extends CollectionPersister> persisterClass = collectionMetadata.getCollectionPersisterClass();
      if (persisterClass == null) {
         persisterClass = ((PersisterClassResolver)this.serviceRegistry.getService(PersisterClassResolver.class)).getCollectionPersisterClass(collectionMetadata);
      }

      return create(persisterClass, COLLECTION_PERSISTER_CONSTRUCTOR_ARGS, cfg, collectionMetadata, cacheAccessStrategy, factory);
   }

   public CollectionPersister createCollectionPersister(MetadataImplementor metadata, PluralAttributeBinding collectionMetadata, CollectionRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
      Class<? extends CollectionPersister> persisterClass = collectionMetadata.getCollectionPersisterClass();
      if (persisterClass == null) {
         persisterClass = ((PersisterClassResolver)this.serviceRegistry.getService(PersisterClassResolver.class)).getCollectionPersisterClass(collectionMetadata);
      }

      return create(persisterClass, COLLECTION_PERSISTER_CONSTRUCTOR_ARGS_NEW, metadata, collectionMetadata, cacheAccessStrategy, factory);
   }

   private static CollectionPersister create(Class persisterClass, Class[] persisterConstructorArgs, Object cfg, Object collectionMetadata, CollectionRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
      try {
         Constructor<? extends CollectionPersister> constructor = persisterClass.getConstructor(persisterConstructorArgs);

         try {
            return (CollectionPersister)constructor.newInstance(collectionMetadata, cacheAccessStrategy, cfg, factory);
         } catch (MappingException e) {
            throw e;
         } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof HibernateException) {
               throw (HibernateException)target;
            } else {
               throw new MappingException("Could not instantiate collection persister " + persisterClass.getName(), target);
            }
         } catch (Exception e) {
            throw new MappingException("Could not instantiate collection persister " + persisterClass.getName(), e);
         }
      } catch (MappingException e) {
         throw e;
      } catch (Exception e) {
         throw new MappingException("Could not get constructor for " + persisterClass.getName(), e);
      }
   }
}
