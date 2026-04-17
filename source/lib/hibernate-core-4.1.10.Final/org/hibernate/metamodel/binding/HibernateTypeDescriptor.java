package org.hibernate.metamodel.binding;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.type.Type;

public class HibernateTypeDescriptor {
   private String explicitTypeName;
   private String javaTypeName;
   private boolean isToOne;
   private Map typeParameters = new HashMap();
   private Type resolvedTypeMapping;

   public String getExplicitTypeName() {
      return this.explicitTypeName;
   }

   public void setExplicitTypeName(String explicitTypeName) {
      this.explicitTypeName = explicitTypeName;
   }

   public String getJavaTypeName() {
      return this.javaTypeName;
   }

   public void setJavaTypeName(String javaTypeName) {
      this.javaTypeName = javaTypeName;
   }

   public boolean isToOne() {
      return this.isToOne;
   }

   public void setToOne(boolean toOne) {
      this.isToOne = toOne;
   }

   public Map getTypeParameters() {
      return this.typeParameters;
   }

   public void setTypeParameters(Map typeParameters) {
      this.typeParameters = typeParameters;
   }

   public Type getResolvedTypeMapping() {
      return this.resolvedTypeMapping;
   }

   public void setResolvedTypeMapping(Type resolvedTypeMapping) {
      this.resolvedTypeMapping = resolvedTypeMapping;
   }
}
