package org.hibernate.cache.internal;

import java.util.Comparator;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.VersionType;

public class CacheDataDescriptionImpl implements CacheDataDescription {
   private final boolean mutable;
   private final boolean versioned;
   private final Comparator versionComparator;

   public CacheDataDescriptionImpl(boolean mutable, boolean versioned, Comparator versionComparator) {
      this.mutable = mutable;
      this.versioned = versioned;
      this.versionComparator = versionComparator;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public boolean isVersioned() {
      return this.versioned;
   }

   public Comparator getVersionComparator() {
      return this.versionComparator;
   }

   public static CacheDataDescriptionImpl decode(PersistentClass model) {
      return new CacheDataDescriptionImpl(model.isMutable(), model.isVersioned(), model.isVersioned() ? ((VersionType)model.getVersion().getType()).getComparator() : null);
   }

   public static CacheDataDescriptionImpl decode(EntityBinding model) {
      return new CacheDataDescriptionImpl(model.isMutable(), model.isVersioned(), getVersionComparator(model));
   }

   public static CacheDataDescriptionImpl decode(Collection model) {
      return new CacheDataDescriptionImpl(model.isMutable(), model.getOwner().isVersioned(), model.getOwner().isVersioned() ? ((VersionType)model.getOwner().getVersion().getType()).getComparator() : null);
   }

   public static CacheDataDescriptionImpl decode(PluralAttributeBinding model) {
      return new CacheDataDescriptionImpl(model.isMutable(), model.getContainer().seekEntityBinding().isVersioned(), getVersionComparator(model.getContainer().seekEntityBinding()));
   }

   public static CacheDataDescriptionImpl decode(EntityPersister persister) {
      return new CacheDataDescriptionImpl(!persister.getEntityMetamodel().hasImmutableNaturalId(), false, (Comparator)null);
   }

   private static Comparator getVersionComparator(EntityBinding model) {
      Comparator versionComparator = null;
      if (model.isVersioned()) {
         versionComparator = ((VersionType)model.getHierarchyDetails().getVersioningAttributeBinding().getHibernateTypeDescriptor().getResolvedTypeMapping()).getComparator();
      }

      return versionComparator;
   }
}
