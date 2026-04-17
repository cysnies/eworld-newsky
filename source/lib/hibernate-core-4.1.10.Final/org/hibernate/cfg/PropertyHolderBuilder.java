package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;

public final class PropertyHolderBuilder {
   private PropertyHolderBuilder() {
   }

   public static PropertyHolder buildPropertyHolder(XClass clazzToProcess, PersistentClass persistentClass, EntityBinder entityBinder, Mappings mappings, Map inheritanceStatePerClass) {
      return new ClassPropertyHolder(persistentClass, clazzToProcess, entityBinder, mappings, inheritanceStatePerClass);
   }

   public static PropertyHolder buildPropertyHolder(Component component, String path, PropertyData inferredData, PropertyHolder parent, Mappings mappings) {
      return new ComponentPropertyHolder(component, path, inferredData, parent, mappings);
   }

   public static PropertyHolder buildPropertyHolder(Collection collection, String path, XClass clazzToProcess, XProperty property, PropertyHolder parentPropertyHolder, Mappings mappings) {
      return new CollectionPropertyHolder(collection, path, clazzToProcess, property, parentPropertyHolder, mappings);
   }

   public static PropertyHolder buildPropertyHolder(PersistentClass persistentClass, Map joins, Mappings mappings, Map inheritanceStatePerClass) {
      return new ClassPropertyHolder(persistentClass, (XClass)null, joins, mappings, inheritanceStatePerClass);
   }
}
