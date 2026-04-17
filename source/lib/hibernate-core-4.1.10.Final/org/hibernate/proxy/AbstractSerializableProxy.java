package org.hibernate.proxy;

import java.io.Serializable;

public abstract class AbstractSerializableProxy implements Serializable {
   private String entityName;
   private Serializable id;
   private Boolean readOnly;

   protected AbstractSerializableProxy() {
   }

   protected AbstractSerializableProxy(String entityName, Serializable id, Boolean readOnly) {
      this.entityName = entityName;
      this.id = id;
      this.readOnly = readOnly;
   }

   protected String getEntityName() {
      return this.entityName;
   }

   protected Serializable getId() {
      return this.id;
   }

   protected void setReadOnlyBeforeAttachedToSession(AbstractLazyInitializer li) {
      li.setReadOnlyBeforeAttachedToSession(this.readOnly);
   }
}
