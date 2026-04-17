package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.MetaType;
import org.hibernate.type.Type;

public class Any extends SimpleValue {
   private String identifierTypeName;
   private String metaTypeName = "string";
   private java.util.Map metaValues;

   public Any(Mappings mappings, Table table) {
      super(mappings, table);
   }

   public String getIdentifierType() {
      return this.identifierTypeName;
   }

   public void setIdentifierType(String identifierType) {
      this.identifierTypeName = identifierType;
   }

   public Type getType() throws MappingException {
      Type metaType = this.getMappings().getTypeResolver().heuristicType(this.metaTypeName);
      return this.getMappings().getTypeResolver().getTypeFactory().any((Type)(this.metaValues == null ? metaType : new MetaType(this.metaValues, metaType)), this.getMappings().getTypeResolver().heuristicType(this.identifierTypeName));
   }

   public void setTypeByReflection(String propertyClass, String propertyName) {
   }

   public String getMetaType() {
      return this.metaTypeName;
   }

   public void setMetaType(String type) {
      this.metaTypeName = type;
   }

   public java.util.Map getMetaValues() {
      return this.metaValues;
   }

   public void setMetaValues(java.util.Map metaValues) {
      this.metaValues = metaValues;
   }

   public void setTypeUsingReflection(String className, String propertyName) throws MappingException {
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
