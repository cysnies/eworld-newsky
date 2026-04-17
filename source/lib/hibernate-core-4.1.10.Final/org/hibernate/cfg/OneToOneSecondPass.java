package org.hibernate.cfg;

import java.util.Iterator;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.annotations.PropertyBinder;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.ForeignKeyDirection;

public class OneToOneSecondPass implements SecondPass {
   private String mappedBy;
   private Mappings mappings;
   private String ownerEntity;
   private String ownerProperty;
   private PropertyHolder propertyHolder;
   private boolean ignoreNotFound;
   private PropertyData inferredData;
   private XClass targetEntity;
   private boolean cascadeOnDelete;
   private boolean optional;
   private String cascadeStrategy;
   private Ejb3JoinColumn[] joinColumns;

   public OneToOneSecondPass(String mappedBy, String ownerEntity, String ownerProperty, PropertyHolder propertyHolder, PropertyData inferredData, XClass targetEntity, boolean ignoreNotFound, boolean cascadeOnDelete, boolean optional, String cascadeStrategy, Ejb3JoinColumn[] columns, Mappings mappings) {
      this.ownerEntity = ownerEntity;
      this.ownerProperty = ownerProperty;
      this.mappedBy = mappedBy;
      this.propertyHolder = propertyHolder;
      this.mappings = mappings;
      this.ignoreNotFound = ignoreNotFound;
      this.inferredData = inferredData;
      this.targetEntity = targetEntity;
      this.cascadeOnDelete = cascadeOnDelete;
      this.optional = optional;
      this.cascadeStrategy = cascadeStrategy;
      this.joinColumns = columns;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      OneToOne value = new OneToOne(this.mappings, this.propertyHolder.getTable(), this.propertyHolder.getPersistentClass());
      String propertyName = this.inferredData.getPropertyName();
      value.setPropertyName(propertyName);
      String referencedEntityName = ToOneBinder.getReferenceEntityName(this.inferredData, this.targetEntity, this.mappings);
      value.setReferencedEntityName(referencedEntityName);
      AnnotationBinder.defineFetchingStrategy(value, this.inferredData.getProperty());
      value.setCascadeDeleteEnabled(this.cascadeOnDelete);
      if (!this.optional) {
         value.setConstrained(true);
      }

      value.setForeignKeyType(value.isConstrained() ? ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT : ForeignKeyDirection.FOREIGN_KEY_TO_PARENT);
      PropertyBinder binder = new PropertyBinder();
      binder.setName(propertyName);
      binder.setValue(value);
      binder.setCascade(this.cascadeStrategy);
      binder.setAccessType(this.inferredData.getDefaultAccess());
      Property prop = binder.makeProperty();
      if (BinderHelper.isEmptyAnnotationValue(this.mappedBy)) {
         boolean rightOrder = true;
         if (rightOrder) {
            String path = StringHelper.qualify(this.propertyHolder.getPath(), propertyName);
            (new ToOneFkSecondPass(value, this.joinColumns, !this.optional, this.propertyHolder.getEntityOwnerClassName(), path, this.mappings)).doSecondPass(persistentClasses);
            this.propertyHolder.addProperty(prop, this.inferredData.getDeclaringClass());
         }
      } else {
         PersistentClass otherSide = (PersistentClass)persistentClasses.get(value.getReferencedEntityName());

         Property otherSideProperty;
         try {
            if (otherSide == null) {
               throw new MappingException("Unable to find entity: " + value.getReferencedEntityName());
            }

            otherSideProperty = BinderHelper.findPropertyByName(otherSide, this.mappedBy);
         } catch (MappingException var16) {
            throw new AnnotationException("Unknown mappedBy in: " + StringHelper.qualify(this.ownerEntity, this.ownerProperty) + ", referenced property unknown: " + StringHelper.qualify(value.getReferencedEntityName(), this.mappedBy));
         }

         if (otherSideProperty == null) {
            throw new AnnotationException("Unknown mappedBy in: " + StringHelper.qualify(this.ownerEntity, this.ownerProperty) + ", referenced property unknown: " + StringHelper.qualify(value.getReferencedEntityName(), this.mappedBy));
         }

         if (otherSideProperty.getValue() instanceof OneToOne) {
            this.propertyHolder.addProperty(prop, this.inferredData.getDeclaringClass());
         } else {
            if (!(otherSideProperty.getValue() instanceof ManyToOne)) {
               throw new AnnotationException("Referenced property not a (One|Many)ToOne: " + StringHelper.qualify(otherSide.getEntityName(), this.mappedBy) + " in mappedBy of " + StringHelper.qualify(this.ownerEntity, this.ownerProperty));
            }

            Iterator it = otherSide.getJoinIterator();
            Join otherSideJoin = null;

            while(it.hasNext()) {
               Join otherSideJoinValue = (Join)it.next();
               if (otherSideJoinValue.containsProperty(otherSideProperty)) {
                  otherSideJoin = otherSideJoinValue;
                  break;
               }
            }

            if (otherSideJoin == null) {
               this.propertyHolder.addProperty(prop, this.inferredData.getDeclaringClass());
            } else {
               Join mappedByJoin = this.buildJoinFromMappedBySide((PersistentClass)persistentClasses.get(this.ownerEntity), otherSideProperty, otherSideJoin);
               ManyToOne manyToOne = new ManyToOne(this.mappings, mappedByJoin.getTable());
               manyToOne.setIgnoreNotFound(this.ignoreNotFound);
               manyToOne.setCascadeDeleteEnabled(value.isCascadeDeleteEnabled());
               manyToOne.setEmbedded(value.isEmbedded());
               manyToOne.setFetchMode(value.getFetchMode());
               manyToOne.setLazy(value.isLazy());
               manyToOne.setReferencedEntityName(value.getReferencedEntityName());
               manyToOne.setUnwrapProxy(value.isUnwrapProxy());
               prop.setValue(manyToOne);
               Iterator otherSideJoinKeyColumns = otherSideJoin.getKey().getColumnIterator();

               while(otherSideJoinKeyColumns.hasNext()) {
                  Column column = (Column)otherSideJoinKeyColumns.next();
                  Column copy = new Column();
                  copy.setLength(column.getLength());
                  copy.setScale(column.getScale());
                  copy.setValue(manyToOne);
                  copy.setName(column.getQuotedName());
                  copy.setNullable(column.isNullable());
                  copy.setPrecision(column.getPrecision());
                  copy.setUnique(column.isUnique());
                  copy.setSqlType(column.getSqlType());
                  copy.setCheckConstraint(column.getCheckConstraint());
                  copy.setComment(column.getComment());
                  copy.setDefaultValue(column.getDefaultValue());
                  manyToOne.addColumn(copy);
               }

               mappedByJoin.addProperty(prop);
            }

            value.setReferencedPropertyName(this.mappedBy);
            String propertyRef = value.getReferencedPropertyName();
            if (propertyRef != null) {
               this.mappings.addUniquePropertyReference(value.getReferencedEntityName(), propertyRef);
            }
         }
      }

      ForeignKey fk = (ForeignKey)this.inferredData.getProperty().getAnnotation(ForeignKey.class);
      String fkName = fk != null ? fk.name() : "";
      if (!BinderHelper.isEmptyAnnotationValue(fkName)) {
         value.setForeignKeyName(fkName);
      }

   }

   private Join buildJoinFromMappedBySide(PersistentClass persistentClass, Property otherSideProperty, Join originalJoin) {
      Join join = new Join();
      join.setPersistentClass(persistentClass);
      join.setTable(originalJoin.getTable());
      join.setInverse(true);
      SimpleValue key = new DependantValue(this.mappings, join.getTable(), persistentClass.getIdentifier());
      join.setKey(key);
      join.setSequentialSelect(false);
      join.setOptional(true);
      key.setCascadeDeleteEnabled(false);
      Iterator mappedByColumns = otherSideProperty.getValue().getColumnIterator();

      while(mappedByColumns.hasNext()) {
         Column column = (Column)mappedByColumns.next();
         Column copy = new Column();
         copy.setLength(column.getLength());
         copy.setScale(column.getScale());
         copy.setValue(key);
         copy.setName(column.getQuotedName());
         copy.setNullable(column.isNullable());
         copy.setPrecision(column.getPrecision());
         copy.setUnique(column.isUnique());
         copy.setSqlType(column.getSqlType());
         copy.setCheckConstraint(column.getCheckConstraint());
         copy.setComment(column.getComment());
         copy.setDefaultValue(column.getDefaultValue());
         key.addColumn(copy);
      }

      persistentClass.addJoin(join);
      return join;
   }
}
