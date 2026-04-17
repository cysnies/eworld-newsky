package org.hibernate.cfg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

public class CopyIdentifierComponentSecondPass implements SecondPass {
   private final String referencedEntityName;
   private final Component component;
   private final Mappings mappings;
   private final Ejb3JoinColumn[] joinColumns;

   public CopyIdentifierComponentSecondPass(Component comp, String referencedEntityName, Ejb3JoinColumn[] joinColumns, Mappings mappings) {
      this.component = comp;
      this.referencedEntityName = referencedEntityName;
      this.mappings = mappings;
      this.joinColumns = joinColumns;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      PersistentClass referencedPersistentClass = (PersistentClass)persistentClasses.get(this.referencedEntityName);
      if (referencedPersistentClass == null) {
         throw new AnnotationException("Unknown entity name: " + this.referencedEntityName);
      } else if (!(referencedPersistentClass.getIdentifier() instanceof Component)) {
         throw new AssertionFailure("Unexpected identifier type on the referenced entity when mapping a @MapsId: " + this.referencedEntityName);
      } else {
         Component referencedComponent = (Component)referencedPersistentClass.getIdentifier();
         Iterator<Property> properties = referencedComponent.getPropertyIterator();
         boolean isExplicitReference = true;
         Map<String, Ejb3JoinColumn> columnByReferencedName = new HashMap(this.joinColumns.length);

         for(Ejb3JoinColumn joinColumn : this.joinColumns) {
            String referencedColumnName = joinColumn.getReferencedColumn();
            if (referencedColumnName == null || BinderHelper.isEmptyAnnotationValue(referencedColumnName)) {
               break;
            }

            columnByReferencedName.put(referencedColumnName.toLowerCase(), joinColumn);
         }

         int index = 0;
         if (columnByReferencedName.isEmpty()) {
            isExplicitReference = false;

            for(Ejb3JoinColumn joinColumn : this.joinColumns) {
               columnByReferencedName.put("" + index, joinColumn);
               ++index;
            }

            index = 0;
         }

         Property property;
         for(; properties.hasNext(); this.component.addProperty(property)) {
            Property referencedProperty = (Property)properties.next();
            if (referencedProperty.isComposite()) {
               throw new AssertionFailure("Unexpected nested component on the referenced entity when mapping a @MapsId: " + this.referencedEntityName);
            }

            property = new Property();
            property.setName(referencedProperty.getName());
            property.setNodeName(referencedProperty.getNodeName());
            property.setPersistentClass(this.component.getOwner());
            property.setPropertyAccessorName(referencedProperty.getPropertyAccessorName());
            SimpleValue value = new SimpleValue(this.mappings, this.component.getTable());
            property.setValue(value);
            SimpleValue referencedValue = (SimpleValue)referencedProperty.getValue();
            value.setTypeName(referencedValue.getTypeName());
            value.setTypeParameters(referencedValue.getTypeParameters());
            Iterator<Column> columns = referencedValue.getColumnIterator();
            if (this.joinColumns[0].isNameDeferred()) {
               this.joinColumns[0].copyReferencedStructureAndCreateDefaultJoinColumns(referencedPersistentClass, columns, value);
            } else {
               while(columns.hasNext()) {
                  Column column = (Column)columns.next();
                  String logicalColumnName = null;
                  Ejb3JoinColumn joinColumn;
                  if (isExplicitReference) {
                     String columnName = column.getName();
                     logicalColumnName = this.mappings.getLogicalColumnName(columnName, referencedPersistentClass.getTable());
                     joinColumn = (Ejb3JoinColumn)columnByReferencedName.get(logicalColumnName.toLowerCase());
                  } else {
                     joinColumn = (Ejb3JoinColumn)columnByReferencedName.get("" + index);
                     ++index;
                  }

                  if (joinColumn == null && !this.joinColumns[0].isNameDeferred()) {
                     throw new AnnotationException(isExplicitReference ? "Unable to find column reference in the @MapsId mapping: " + logicalColumnName : "Implicit column reference in the @MapsId mapping fails, try to use explicit referenceColumnNames: " + this.referencedEntityName);
                  }

                  String columnName = joinColumn != null && !joinColumn.isNameDeferred() ? joinColumn.getName() : "tata_" + column.getName();
                  value.addColumn(new Column(columnName));
                  column.setValue(value);
               }
            }
         }

      }
   }
}
