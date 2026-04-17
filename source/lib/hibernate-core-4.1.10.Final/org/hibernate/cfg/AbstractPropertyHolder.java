package org.hibernate.cfg;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.internal.util.StringHelper;

public abstract class AbstractPropertyHolder implements PropertyHolder {
   protected AbstractPropertyHolder parent;
   private Map holderColumnOverride;
   private Map currentPropertyColumnOverride;
   private Map holderJoinColumnOverride;
   private Map currentPropertyJoinColumnOverride;
   private Map holderJoinTableOverride;
   private Map currentPropertyJoinTableOverride;
   private String path;
   private Mappings mappings;
   private Boolean isInIdClass;

   public AbstractPropertyHolder(String path, PropertyHolder parent, XClass clazzToProcess, Mappings mappings) {
      this.path = path;
      this.parent = (AbstractPropertyHolder)parent;
      this.mappings = mappings;
      this.buildHierarchyColumnOverride(clazzToProcess);
   }

   public boolean isInIdClass() {
      return this.isInIdClass != null ? this.isInIdClass : (this.parent != null ? this.parent.isInIdClass() : false);
   }

   public void setInIdClass(Boolean isInIdClass) {
      this.isInIdClass = isInIdClass;
   }

   public String getPath() {
      return this.path;
   }

   protected Mappings getMappings() {
      return this.mappings;
   }

   protected void setCurrentProperty(XProperty property) {
      if (property == null) {
         this.currentPropertyColumnOverride = null;
         this.currentPropertyJoinColumnOverride = null;
         this.currentPropertyJoinTableOverride = null;
      } else {
         this.currentPropertyColumnOverride = buildColumnOverride(property, this.getPath());
         if (this.currentPropertyColumnOverride.size() == 0) {
            this.currentPropertyColumnOverride = null;
         }

         this.currentPropertyJoinColumnOverride = buildJoinColumnOverride(property, this.getPath());
         if (this.currentPropertyJoinColumnOverride.size() == 0) {
            this.currentPropertyJoinColumnOverride = null;
         }

         this.currentPropertyJoinTableOverride = buildJoinTableOverride(property, this.getPath());
         if (this.currentPropertyJoinTableOverride.size() == 0) {
            this.currentPropertyJoinTableOverride = null;
         }
      }

   }

   public Column[] getOverriddenColumn(String propertyName) {
      Column[] result = this.getExactOverriddenColumn(propertyName);
      if (result == null && result == null && propertyName.contains(".collection&&element.")) {
         result = this.getExactOverriddenColumn(propertyName.replace(".collection&&element.", "."));
      }

      return result;
   }

   private Column[] getExactOverriddenColumn(String propertyName) {
      Column[] override = null;
      if (this.parent != null) {
         override = this.parent.getExactOverriddenColumn(propertyName);
      }

      if (override == null && this.currentPropertyColumnOverride != null) {
         override = (Column[])this.currentPropertyColumnOverride.get(propertyName);
      }

      if (override == null && this.holderColumnOverride != null) {
         override = (Column[])this.holderColumnOverride.get(propertyName);
      }

      return override;
   }

   public JoinColumn[] getOverriddenJoinColumn(String propertyName) {
      JoinColumn[] result = this.getExactOverriddenJoinColumn(propertyName);
      if (result == null && propertyName.contains(".collection&&element.")) {
         result = this.getExactOverriddenJoinColumn(propertyName.replace(".collection&&element.", "."));
      }

      return result;
   }

   private JoinColumn[] getExactOverriddenJoinColumn(String propertyName) {
      JoinColumn[] override = null;
      if (this.parent != null) {
         override = this.parent.getExactOverriddenJoinColumn(propertyName);
      }

      if (override == null && this.currentPropertyJoinColumnOverride != null) {
         override = (JoinColumn[])this.currentPropertyJoinColumnOverride.get(propertyName);
      }

      if (override == null && this.holderJoinColumnOverride != null) {
         override = (JoinColumn[])this.holderJoinColumnOverride.get(propertyName);
      }

      return override;
   }

   public JoinTable getJoinTable(XProperty property) {
      String propertyName = StringHelper.qualify(this.getPath(), property.getName());
      JoinTable result = this.getOverriddenJoinTable(propertyName);
      if (result == null) {
         result = (JoinTable)property.getAnnotation(JoinTable.class);
      }

      return result;
   }

   public JoinTable getOverriddenJoinTable(String propertyName) {
      JoinTable result = this.getExactOverriddenJoinTable(propertyName);
      if (result == null && propertyName.contains(".collection&&element.")) {
         result = this.getExactOverriddenJoinTable(propertyName.replace(".collection&&element.", "."));
      }

      return result;
   }

   private JoinTable getExactOverriddenJoinTable(String propertyName) {
      JoinTable override = null;
      if (this.parent != null) {
         override = this.parent.getExactOverriddenJoinTable(propertyName);
      }

      if (override == null && this.currentPropertyJoinTableOverride != null) {
         override = (JoinTable)this.currentPropertyJoinTableOverride.get(propertyName);
      }

      if (override == null && this.holderJoinTableOverride != null) {
         override = (JoinTable)this.holderJoinTableOverride.get(propertyName);
      }

      return override;
   }

   private void buildHierarchyColumnOverride(XClass element) {
      XClass current = element;
      Map<String, Column[]> columnOverride = new HashMap();
      Map<String, JoinColumn[]> joinColumnOverride = new HashMap();

      Map<String, JoinTable> joinTableOverride;
      for(joinTableOverride = new HashMap(); current != null && !this.mappings.getReflectionManager().toXClass(Object.class).equals(current); current = current.getSuperclass()) {
         if (current.isAnnotationPresent(Entity.class) || current.isAnnotationPresent(MappedSuperclass.class) || current.isAnnotationPresent(Embeddable.class)) {
            Map<String, Column[]> currentOverride = buildColumnOverride(current, this.getPath());
            Map<String, JoinColumn[]> currentJoinOverride = buildJoinColumnOverride(current, this.getPath());
            Map<String, JoinTable> currentJoinTableOverride = buildJoinTableOverride(current, this.getPath());
            currentOverride.putAll(columnOverride);
            currentJoinOverride.putAll(joinColumnOverride);
            currentJoinTableOverride.putAll(joinTableOverride);
            columnOverride = currentOverride;
            joinColumnOverride = currentJoinOverride;
            joinTableOverride = currentJoinTableOverride;
         }
      }

      this.holderColumnOverride = columnOverride.size() > 0 ? columnOverride : null;
      this.holderJoinColumnOverride = joinColumnOverride.size() > 0 ? joinColumnOverride : null;
      this.holderJoinTableOverride = joinTableOverride.size() > 0 ? joinTableOverride : null;
   }

   private static Map buildColumnOverride(XAnnotatedElement element, String path) {
      Map<String, Column[]> columnOverride = new HashMap();
      if (element == null) {
         return columnOverride;
      } else {
         AttributeOverride singleOverride = (AttributeOverride)element.getAnnotation(AttributeOverride.class);
         AttributeOverrides multipleOverrides = (AttributeOverrides)element.getAnnotation(AttributeOverrides.class);
         AttributeOverride[] overrides;
         if (singleOverride != null) {
            overrides = new AttributeOverride[]{singleOverride};
         } else if (multipleOverrides != null) {
            overrides = multipleOverrides.value();
         } else {
            overrides = null;
         }

         if (overrides != null) {
            for(AttributeOverride depAttr : overrides) {
               columnOverride.put(StringHelper.qualify(path, depAttr.name()), new Column[]{depAttr.column()});
            }
         }

         return columnOverride;
      }
   }

   private static Map buildJoinColumnOverride(XAnnotatedElement element, String path) {
      Map<String, JoinColumn[]> columnOverride = new HashMap();
      if (element == null) {
         return columnOverride;
      } else {
         AssociationOverride singleOverride = (AssociationOverride)element.getAnnotation(AssociationOverride.class);
         AssociationOverrides multipleOverrides = (AssociationOverrides)element.getAnnotation(AssociationOverrides.class);
         AssociationOverride[] overrides;
         if (singleOverride != null) {
            overrides = new AssociationOverride[]{singleOverride};
         } else if (multipleOverrides != null) {
            overrides = multipleOverrides.value();
         } else {
            overrides = null;
         }

         if (overrides != null) {
            for(AssociationOverride depAttr : overrides) {
               columnOverride.put(StringHelper.qualify(path, depAttr.name()), depAttr.joinColumns());
            }
         }

         return columnOverride;
      }
   }

   private static Map buildJoinTableOverride(XAnnotatedElement element, String path) {
      Map<String, JoinTable> tableOverride = new HashMap();
      if (element == null) {
         return tableOverride;
      } else {
         AssociationOverride singleOverride = (AssociationOverride)element.getAnnotation(AssociationOverride.class);
         AssociationOverrides multipleOverrides = (AssociationOverrides)element.getAnnotation(AssociationOverrides.class);
         AssociationOverride[] overrides;
         if (singleOverride != null) {
            overrides = new AssociationOverride[]{singleOverride};
         } else if (multipleOverrides != null) {
            overrides = multipleOverrides.value();
         } else {
            overrides = null;
         }

         if (overrides != null) {
            for(AssociationOverride depAttr : overrides) {
               if (depAttr.joinColumns().length == 0) {
                  tableOverride.put(StringHelper.qualify(path, depAttr.name()), depAttr.joinTable());
               }
            }
         }

         return tableOverride;
      }
   }

   public void setParentProperty(String parentProperty) {
      throw new AssertionFailure("Setting the parent property to a non component");
   }
}
