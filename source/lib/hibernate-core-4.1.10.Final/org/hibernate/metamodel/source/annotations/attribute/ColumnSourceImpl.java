package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.internal.util.StringHelper;

public class ColumnSourceImpl extends ColumnValuesSourceImpl {
   private final MappedAttribute attribute;
   private final String name;

   ColumnSourceImpl(MappedAttribute attribute, AttributeOverride attributeOverride) {
      super(attribute.getColumnValues());
      if (attributeOverride != null) {
         this.setOverrideColumnValues(attributeOverride.getColumnValues());
      }

      this.attribute = attribute;
      this.name = this.resolveColumnName();
   }

   protected String resolveColumnName() {
      return StringHelper.isEmpty(super.getName()) ? this.attribute.getContext().getNamingStrategy().propertyToColumnName(this.attribute.getName()) : super.getName();
   }

   public String getName() {
      return this.name;
   }

   public String getReadFragment() {
      return this.attribute instanceof BasicAttribute ? ((BasicAttribute)this.attribute).getCustomReadFragment() : null;
   }

   public String getWriteFragment() {
      return this.attribute instanceof BasicAttribute ? ((BasicAttribute)this.attribute).getCustomWriteFragment() : null;
   }

   public String getCheckCondition() {
      return this.attribute instanceof BasicAttribute ? ((BasicAttribute)this.attribute).getCheckCondition() : null;
   }
}
