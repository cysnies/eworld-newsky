package org.hibernate.cfg;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.cfg.annotations.Nullability;
import org.hibernate.internal.util.StringHelper;

class ColumnsBuilder {
   private PropertyHolder propertyHolder;
   private Nullability nullability;
   private XProperty property;
   private PropertyData inferredData;
   private EntityBinder entityBinder;
   private Mappings mappings;
   private Ejb3Column[] columns;
   private Ejb3JoinColumn[] joinColumns;

   public ColumnsBuilder(PropertyHolder propertyHolder, Nullability nullability, XProperty property, PropertyData inferredData, EntityBinder entityBinder, Mappings mappings) {
      this.propertyHolder = propertyHolder;
      this.nullability = nullability;
      this.property = property;
      this.inferredData = inferredData;
      this.entityBinder = entityBinder;
      this.mappings = mappings;
   }

   public Ejb3Column[] getColumns() {
      return this.columns;
   }

   public Ejb3JoinColumn[] getJoinColumns() {
      return this.joinColumns;
   }

   public ColumnsBuilder extractMetadata() {
      this.columns = null;
      this.joinColumns = this.buildExplicitJoinColumns(this.property, this.inferredData);
      if (!this.property.isAnnotationPresent(Column.class) && !this.property.isAnnotationPresent(Formula.class)) {
         if (this.property.isAnnotationPresent(Columns.class)) {
            Columns anns = (Columns)this.property.getAnnotation(Columns.class);
            this.columns = Ejb3Column.buildColumnFromAnnotation(anns.columns(), (Formula)null, this.nullability, this.propertyHolder, this.inferredData, this.entityBinder.getSecondaryTables(), this.mappings);
         }
      } else {
         Column ann = (Column)this.property.getAnnotation(Column.class);
         Formula formulaAnn = (Formula)this.property.getAnnotation(Formula.class);
         this.columns = Ejb3Column.buildColumnFromAnnotation(new Column[]{ann}, formulaAnn, this.nullability, this.propertyHolder, this.inferredData, this.entityBinder.getSecondaryTables(), this.mappings);
      }

      if (this.joinColumns != null || !this.property.isAnnotationPresent(ManyToOne.class) && !this.property.isAnnotationPresent(OneToOne.class)) {
         if (this.joinColumns == null && (this.property.isAnnotationPresent(OneToMany.class) || this.property.isAnnotationPresent(ElementCollection.class))) {
            OneToMany oneToMany = (OneToMany)this.property.getAnnotation(OneToMany.class);
            String mappedBy = oneToMany != null ? oneToMany.mappedBy() : "";
            this.joinColumns = Ejb3JoinColumn.buildJoinColumns((JoinColumn[])null, mappedBy, this.entityBinder.getSecondaryTables(), this.propertyHolder, this.inferredData.getPropertyName(), this.mappings);
         } else if (this.joinColumns == null && this.property.isAnnotationPresent(Any.class)) {
            throw new AnnotationException("@Any requires an explicit @JoinColumn(s): " + BinderHelper.getPath(this.propertyHolder, this.inferredData));
         }
      } else {
         this.joinColumns = this.buildDefaultJoinColumnsForXToOne(this.property, this.inferredData);
      }

      if (this.columns == null && !this.property.isAnnotationPresent(ManyToMany.class)) {
         this.columns = Ejb3Column.buildColumnFromAnnotation((Column[])null, (Formula)null, this.nullability, this.propertyHolder, this.inferredData, this.entityBinder.getSecondaryTables(), this.mappings);
      }

      if (this.nullability == Nullability.FORCED_NOT_NULL) {
         for(Ejb3Column col : this.columns) {
            col.forceNotNull();
         }
      }

      return this;
   }

   Ejb3JoinColumn[] buildDefaultJoinColumnsForXToOne(XProperty property, PropertyData inferredData) {
      JoinTable joinTableAnn = this.propertyHolder.getJoinTable(property);
      Ejb3JoinColumn[] joinColumns;
      if (joinTableAnn != null) {
         joinColumns = Ejb3JoinColumn.buildJoinColumns(joinTableAnn.inverseJoinColumns(), (String)null, this.entityBinder.getSecondaryTables(), this.propertyHolder, inferredData.getPropertyName(), this.mappings);
         if (StringHelper.isEmpty(joinTableAnn.name())) {
            throw new AnnotationException("JoinTable.name() on a @ToOne association has to be explicit: " + BinderHelper.getPath(this.propertyHolder, inferredData));
         }
      } else {
         OneToOne oneToOneAnn = (OneToOne)property.getAnnotation(OneToOne.class);
         String mappedBy = oneToOneAnn != null ? oneToOneAnn.mappedBy() : null;
         joinColumns = Ejb3JoinColumn.buildJoinColumns((JoinColumn[])null, mappedBy, this.entityBinder.getSecondaryTables(), this.propertyHolder, inferredData.getPropertyName(), this.mappings);
      }

      return joinColumns;
   }

   Ejb3JoinColumn[] buildExplicitJoinColumns(XProperty property, PropertyData inferredData) {
      Ejb3JoinColumn[] joinColumns = null;
      JoinColumn[] anns = null;
      if (property.isAnnotationPresent(JoinColumn.class)) {
         anns = new JoinColumn[]{(JoinColumn)property.getAnnotation(JoinColumn.class)};
      } else if (property.isAnnotationPresent(JoinColumns.class)) {
         JoinColumns ann = (JoinColumns)property.getAnnotation(JoinColumns.class);
         anns = ann.value();
         int length = anns.length;
         if (length == 0) {
            throw new AnnotationException("Cannot bind an empty @JoinColumns");
         }
      }

      if (anns != null) {
         joinColumns = Ejb3JoinColumn.buildJoinColumns(anns, (String)null, this.entityBinder.getSecondaryTables(), this.propertyHolder, inferredData.getPropertyName(), this.mappings);
      } else if (property.isAnnotationPresent(JoinColumnsOrFormulas.class)) {
         JoinColumnsOrFormulas ann = (JoinColumnsOrFormulas)property.getAnnotation(JoinColumnsOrFormulas.class);
         joinColumns = Ejb3JoinColumn.buildJoinColumnsOrFormulas(ann, (String)null, this.entityBinder.getSecondaryTables(), this.propertyHolder, inferredData.getPropertyName(), this.mappings);
      } else if (property.isAnnotationPresent(JoinFormula.class)) {
         JoinFormula ann = (JoinFormula)property.getAnnotation(JoinFormula.class);
         joinColumns = new Ejb3JoinColumn[]{Ejb3JoinColumn.buildJoinFormula(ann, (String)null, this.entityBinder.getSecondaryTables(), this.propertyHolder, inferredData.getPropertyName(), this.mappings)};
      }

      return joinColumns;
   }

   Ejb3Column[] overrideColumnFromMapperOrMapsIdProperty(boolean isId) {
      Ejb3Column[] result = this.columns;
      PropertyData overridingProperty = BinderHelper.getPropertyOverriddenByMapperOrMapsId(isId, this.propertyHolder, this.property.getName(), this.mappings);
      if (overridingProperty != null) {
         result = this.buildExcplicitOrDefaultJoinColumn(overridingProperty);
      }

      return result;
   }

   Ejb3Column[] buildExcplicitOrDefaultJoinColumn(PropertyData overridingProperty) {
      Ejb3Column[] result = this.buildExplicitJoinColumns(overridingProperty.getProperty(), overridingProperty);
      if (result == null) {
         result = this.buildDefaultJoinColumnsForXToOne(overridingProperty.getProperty(), overridingProperty);
      }

      return result;
   }
}
