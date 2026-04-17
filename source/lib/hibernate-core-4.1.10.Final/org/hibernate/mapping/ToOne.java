package org.hibernate.mapping;

import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.Type;

public abstract class ToOne extends SimpleValue implements Fetchable {
   private FetchMode fetchMode;
   protected String referencedPropertyName;
   private String referencedEntityName;
   private boolean embedded;
   private boolean lazy = true;
   protected boolean unwrapProxy;

   protected ToOne(Mappings mappings, Table table) {
      super(mappings, table);
   }

   public FetchMode getFetchMode() {
      return this.fetchMode;
   }

   public void setFetchMode(FetchMode fetchMode) {
      this.fetchMode = fetchMode;
   }

   public abstract void createForeignKey() throws MappingException;

   public abstract Type getType() throws MappingException;

   public String getReferencedPropertyName() {
      return this.referencedPropertyName;
   }

   public void setReferencedPropertyName(String name) {
      this.referencedPropertyName = name == null ? null : name.intern();
   }

   public String getReferencedEntityName() {
      return this.referencedEntityName;
   }

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName == null ? null : referencedEntityName.intern();
   }

   public void setTypeUsingReflection(String className, String propertyName) throws MappingException {
      if (this.referencedEntityName == null) {
         this.referencedEntityName = ReflectHelper.reflectedPropertyClass(className, propertyName).getName();
      }

   }

   public boolean isTypeSpecified() {
      return this.referencedEntityName != null;
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept((SimpleValue)this);
   }

   /** @deprecated */
   @Deprecated
   public boolean isEmbedded() {
      return this.embedded;
   }

   /** @deprecated */
   @Deprecated
   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   public boolean isValid(Mapping mapping) throws MappingException {
      if (this.referencedEntityName == null) {
         throw new MappingException("association must specify the referenced entity");
      } else {
         return super.isValid(mapping);
      }
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public boolean isUnwrapProxy() {
      return this.unwrapProxy;
   }

   public void setUnwrapProxy(boolean unwrapProxy) {
      this.unwrapProxy = unwrapProxy;
   }
}
