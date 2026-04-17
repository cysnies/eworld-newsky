package org.hibernate.tuple;

import org.hibernate.FetchMode;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.VersionValue;
import org.hibernate.type.Type;

public class VersionProperty extends StandardProperty {
   private final VersionValue unsavedValue;

   public VersionProperty(String name, String node, Type type, boolean lazy, boolean insertable, boolean updateable, boolean insertGenerated, boolean updateGenerated, boolean nullable, boolean checkable, boolean versionable, CascadeStyle cascadeStyle, VersionValue unsavedValue) {
      super(name, node, type, lazy, insertable, updateable, insertGenerated, updateGenerated, nullable, checkable, versionable, cascadeStyle, (FetchMode)null);
      this.unsavedValue = unsavedValue;
   }

   public VersionValue getUnsavedValue() {
      return this.unsavedValue;
   }
}
