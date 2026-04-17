package org.hibernate.cfg;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

public class ComponentPropertyHolder extends AbstractPropertyHolder {
   private Component component;
   private boolean isOrWithinEmbeddedId;

   public String getEntityName() {
      return this.component.getComponentClassName();
   }

   public void addProperty(Property prop, Ejb3Column[] columns, XClass declaringClass) {
      if (columns != null) {
         Table table = columns[0].getTable();
         if (!table.equals(this.component.getTable())) {
            if (this.component.getPropertySpan() != 0) {
               throw new AnnotationException("A component cannot hold properties split into 2 different tables: " + this.getPath());
            }

            this.component.setTable(table);
         }
      }

      this.addProperty(prop, declaringClass);
   }

   public Join addJoin(JoinTable joinTableAnn, boolean noDelayInPkColumnCreation) {
      return this.parent.addJoin(joinTableAnn, noDelayInPkColumnCreation);
   }

   public ComponentPropertyHolder(Component component, String path, PropertyData inferredData, PropertyHolder parent, Mappings mappings) {
      super(path, parent, inferredData.getPropertyClass(), mappings);
      XProperty property = inferredData.getProperty();
      this.setCurrentProperty(property);
      this.component = component;
      this.isOrWithinEmbeddedId = parent.isOrWithinEmbeddedId() || property != null && (property.isAnnotationPresent(Id.class) || property.isAnnotationPresent(EmbeddedId.class));
   }

   public String getClassName() {
      return this.component.getComponentClassName();
   }

   public String getEntityOwnerClassName() {
      return this.component.getOwner().getClassName();
   }

   public Table getTable() {
      return this.component.getTable();
   }

   public void addProperty(Property prop, XClass declaringClass) {
      this.component.addProperty(prop);
   }

   public KeyValue getIdentifier() {
      return this.component.getOwner().getIdentifier();
   }

   public boolean isOrWithinEmbeddedId() {
      return this.isOrWithinEmbeddedId;
   }

   public PersistentClass getPersistentClass() {
      return this.component.getOwner();
   }

   public boolean isComponent() {
      return true;
   }

   public boolean isEntity() {
      return false;
   }

   public void setParentProperty(String parentProperty) {
      this.component.setParentProperty(parentProperty);
   }

   public Column[] getOverriddenColumn(String propertyName) {
      Column[] result = super.getOverriddenColumn(propertyName);
      if (result == null) {
         String userPropertyName = this.extractUserPropertyName("id", propertyName);
         if (userPropertyName != null) {
            result = super.getOverriddenColumn(userPropertyName);
         }
      }

      if (result == null) {
         String userPropertyName = this.extractUserPropertyName("_identifierMapper", propertyName);
         if (userPropertyName != null) {
            result = super.getOverriddenColumn(userPropertyName);
         }
      }

      return result;
   }

   private String extractUserPropertyName(String redundantString, String propertyName) {
      String result = null;
      String className = this.component.getOwner().getClassName();
      if (propertyName.startsWith(className) && propertyName.length() > className.length() + 2 + redundantString.length() && propertyName.substring(className.length() + 1, className.length() + 1 + redundantString.length()).equals(redundantString)) {
         result = className + propertyName.substring(className.length() + 1 + redundantString.length());
      }

      return result;
   }

   public JoinColumn[] getOverriddenJoinColumn(String propertyName) {
      return super.getOverriddenJoinColumn(propertyName);
   }
}
