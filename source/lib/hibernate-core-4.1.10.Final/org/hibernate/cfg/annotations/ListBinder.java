package org.hibernate.cfg.annotations;

import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.CollectionSecondPass;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.cfg.PropertyHolderBuilder;
import org.hibernate.cfg.SecondPass;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IndexBackref;
import org.hibernate.mapping.List;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.jboss.logging.Logger;

public class ListBinder extends CollectionBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ListBinder.class.getName());

   protected Collection createCollection(PersistentClass persistentClass) {
      return new List(this.getMappings(), persistentClass);
   }

   public void setSqlOrderBy(OrderBy orderByAnn) {
      if (orderByAnn != null) {
         LOG.orderByAnnotationIndexedCollection();
      }

   }

   public void setSort(Sort sortAnn) {
      if (sortAnn != null) {
         LOG.sortAnnotationIndexedCollection();
      }

   }

   public SecondPass getSecondPass(final Ejb3JoinColumn[] fkJoinColumns, final Ejb3JoinColumn[] keyColumns, final Ejb3JoinColumn[] inverseColumns, final Ejb3Column[] elementColumns, Ejb3Column[] mapKeyColumns, Ejb3JoinColumn[] mapKeyManyToManyColumns, final boolean isEmbedded, final XProperty property, final XClass collType, final boolean ignoreNotFound, final boolean unique, final TableBinder assocTableBinder, final Mappings mappings) {
      return new CollectionSecondPass(mappings, this.collection) {
         public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
            ListBinder.this.bindStarToManySecondPass(persistentClasses, collType, fkJoinColumns, keyColumns, inverseColumns, elementColumns, isEmbedded, property, unique, assocTableBinder, ignoreNotFound, mappings);
            ListBinder.this.bindIndex(mappings);
         }
      };
   }

   private void bindIndex(Mappings mappings) {
      if (!this.indexColumn.isImplicit()) {
         PropertyHolder valueHolder = PropertyHolderBuilder.buildPropertyHolder(this.collection, StringHelper.qualify(this.collection.getRole(), "key"), (XClass)null, (XProperty)null, this.propertyHolder, mappings);
         List list = (List)this.collection;
         if (!list.isOneToMany()) {
            this.indexColumn.forceNotNull();
         }

         this.indexColumn.setPropertyHolder(valueHolder);
         SimpleValueBinder value = new SimpleValueBinder();
         value.setColumns(new Ejb3Column[]{this.indexColumn});
         value.setExplicitType("integer");
         value.setMappings(mappings);
         SimpleValue indexValue = value.make();
         this.indexColumn.linkWithValue(indexValue);
         list.setIndex(indexValue);
         list.setBaseIndex(this.indexColumn.getBase());
         if (list.isOneToMany() && !list.getKey().isNullable() && !list.isInverse()) {
            String entityName = ((OneToMany)list.getElement()).getReferencedEntityName();
            PersistentClass referenced = mappings.getClass(entityName);
            IndexBackref ib = new IndexBackref();
            ib.setName('_' + this.propertyName + "IndexBackref");
            ib.setUpdateable(false);
            ib.setSelectable(false);
            ib.setCollectionRole(list.getRole());
            ib.setEntityName(list.getOwner().getEntityName());
            ib.setValue(list.getIndex());
            referenced.addProperty(ib);
         }

      } else {
         Collection coll = this.collection;
         throw new AnnotationException("List/array has to be annotated with an @OrderColumn (or @IndexColumn): " + coll.getRole());
      }
   }
}
