package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.SimpleValue;

public class JoinedSubclassFkSecondPass extends FkSecondPass {
   private JoinedSubclass entity;
   private Mappings mappings;

   public JoinedSubclassFkSecondPass(JoinedSubclass entity, Ejb3JoinColumn[] inheritanceJoinedColumns, SimpleValue key, Mappings mappings) {
      super(key, inheritanceJoinedColumns);
      this.entity = entity;
      this.mappings = mappings;
   }

   public String getReferencedEntityName() {
      return this.entity.getSuperclass().getEntityName();
   }

   public boolean isInPrimaryKey() {
      return true;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      TableBinder.bindFk(this.entity.getSuperclass(), this.entity, this.columns, this.value, false, this.mappings);
   }
}
