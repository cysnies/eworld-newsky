package org.hibernate.tuple;

import org.hibernate.FetchMode;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.type.Type;

public class StandardProperty extends Property {
   private final boolean lazy;
   private final boolean insertable;
   private final boolean updateable;
   private final boolean insertGenerated;
   private final boolean updateGenerated;
   private final boolean nullable;
   private final boolean dirtyCheckable;
   private final boolean versionable;
   private final CascadeStyle cascadeStyle;
   private final FetchMode fetchMode;

   public StandardProperty(String name, String node, Type type, boolean lazy, boolean insertable, boolean updateable, boolean insertGenerated, boolean updateGenerated, boolean nullable, boolean checkable, boolean versionable, CascadeStyle cascadeStyle, FetchMode fetchMode) {
      super(name, node, type);
      this.lazy = lazy;
      this.insertable = insertable;
      this.updateable = updateable;
      this.insertGenerated = insertGenerated;
      this.updateGenerated = updateGenerated;
      this.nullable = nullable;
      this.dirtyCheckable = checkable;
      this.versionable = versionable;
      this.cascadeStyle = cascadeStyle;
      this.fetchMode = fetchMode;
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public boolean isInsertable() {
      return this.insertable;
   }

   public boolean isUpdateable() {
      return this.updateable;
   }

   public boolean isInsertGenerated() {
      return this.insertGenerated;
   }

   public boolean isUpdateGenerated() {
      return this.updateGenerated;
   }

   public boolean isNullable() {
      return this.nullable;
   }

   public boolean isDirtyCheckable(boolean hasUninitializedProperties) {
      return this.isDirtyCheckable() && (!hasUninitializedProperties || !this.isLazy());
   }

   public boolean isDirtyCheckable() {
      return this.dirtyCheckable;
   }

   public boolean isVersionable() {
      return this.versionable;
   }

   public CascadeStyle getCascadeStyle() {
      return this.cascadeStyle;
   }

   public FetchMode getFetchMode() {
      return this.fetchMode;
   }
}
