package lib.types;

import lib.util.Util;
import org.bukkit.entity.EntityType;

public class EntityElement extends TypeElement {
   private EntityType entityType;

   public EntityElement(String s) {
      super(s);
      this.entityType = Util.getEntityType(s);
   }

   public int hashCode() {
      return this.entityType.hashCode();
   }

   public boolean equals(Object obj) {
      EntityElement entityElement = (EntityElement)obj;
      return this.entityType.equals(entityElement.entityType);
   }

   public EntityType getEntityType() {
      return this.entityType;
   }
}
