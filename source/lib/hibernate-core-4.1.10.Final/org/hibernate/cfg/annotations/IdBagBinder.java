package org.hibernate.cfg.annotations;

import java.util.Collections;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.PropertyData;
import org.hibernate.cfg.PropertyInferredData;
import org.hibernate.cfg.WrappedInferredData;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IdentifierBag;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

public class IdBagBinder extends BagBinder {
   protected Collection createCollection(PersistentClass persistentClass) {
      return new IdentifierBag(this.getMappings(), persistentClass);
   }

   protected boolean bindStarToManySecondPass(Map persistentClasses, XClass collType, Ejb3JoinColumn[] fkJoinColumns, Ejb3JoinColumn[] keyColumns, Ejb3JoinColumn[] inverseColumns, Ejb3Column[] elementColumns, boolean isEmbedded, XProperty property, boolean unique, TableBinder associationTableBinder, boolean ignoreNotFound, Mappings mappings) {
      boolean result = super.bindStarToManySecondPass(persistentClasses, collType, fkJoinColumns, keyColumns, inverseColumns, elementColumns, isEmbedded, property, unique, associationTableBinder, ignoreNotFound, mappings);
      CollectionId collectionIdAnn = (CollectionId)property.getAnnotation(CollectionId.class);
      if (collectionIdAnn != null) {
         SimpleValueBinder simpleValue = new SimpleValueBinder();
         PropertyData propertyData = new WrappedInferredData(new PropertyInferredData((XClass)null, property, (String)null, mappings.getReflectionManager()), "id");
         Ejb3Column[] idColumns = Ejb3Column.buildColumnFromAnnotation(collectionIdAnn.columns(), (Formula)null, Nullability.FORCED_NOT_NULL, this.propertyHolder, propertyData, Collections.EMPTY_MAP, mappings);

         for(Ejb3Column idColumn : idColumns) {
            idColumn.setNullable(false);
         }

         Table table = this.collection.getCollectionTable();
         simpleValue.setTable(table);
         simpleValue.setColumns(idColumns);
         Type typeAnn = collectionIdAnn.type();
         if (typeAnn == null || BinderHelper.isEmptyAnnotationValue(typeAnn.type())) {
            throw new AnnotationException("@CollectionId is missing type: " + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
         }

         simpleValue.setExplicitType(typeAnn);
         simpleValue.setMappings(mappings);
         SimpleValue var25 = simpleValue.make();
         ((IdentifierCollection)this.collection).setIdentifier(var25);
         String generator = collectionIdAnn.generator();
         String generatorType;
         if (!"identity".equals(generator) && !"assigned".equals(generator) && !"sequence".equals(generator) && !"native".equals(generator)) {
            generatorType = null;
         } else {
            generatorType = generator;
            generator = "";
         }

         BinderHelper.makeIdGenerator(var25, generatorType, generator, mappings, this.localGenerators);
      }

      return result;
   }
}
