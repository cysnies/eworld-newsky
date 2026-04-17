package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.internal.jaxb.mapping.hbm.CustomSqlElement;
import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbColumnElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbJoinedSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbMetaElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbParamElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbUnionSubclassElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.binding.MetaAttribute;
import org.hibernate.metamodel.relational.Identifier;
import org.hibernate.metamodel.relational.Schema;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.MetaAttributeContext;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.MetaAttributeSource;
import org.hibernate.metamodel.source.binder.RelationalValueSource;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;

public class Helper {
   public static final ExplicitHibernateTypeSource TO_ONE_ATTRIBUTE_TYPE_SOURCE = new ExplicitHibernateTypeSource() {
      public String getName() {
         return null;
      }

      public Map getParameters() {
         return null;
      }
   };

   public static InheritanceType interpretInheritanceType(EntityElement entityElement) {
      if (JaxbSubclassElement.class.isInstance(entityElement)) {
         return InheritanceType.SINGLE_TABLE;
      } else if (JaxbJoinedSubclassElement.class.isInstance(entityElement)) {
         return InheritanceType.JOINED;
      } else {
         return JaxbUnionSubclassElement.class.isInstance(entityElement) ? InheritanceType.TABLE_PER_CLASS : InheritanceType.NO_INHERITANCE;
      }
   }

   public static CustomSQL buildCustomSql(CustomSqlElement customSqlElement) {
      if (customSqlElement == null) {
         return null;
      } else {
         ExecuteUpdateResultCheckStyle checkStyle = customSqlElement.getCheck() == null ? (customSqlElement.isCallable() ? ExecuteUpdateResultCheckStyle.NONE : ExecuteUpdateResultCheckStyle.COUNT) : ExecuteUpdateResultCheckStyle.fromExternalName(customSqlElement.getCheck().value());
         return new CustomSQL(customSqlElement.getValue(), customSqlElement.isCallable(), checkStyle);
      }
   }

   public static String determineEntityName(EntityElement entityElement, String unqualifiedClassPackage) {
      return entityElement.getEntityName() != null ? entityElement.getEntityName() : qualifyIfNeeded(entityElement.getName(), unqualifiedClassPackage);
   }

   public static String qualifyIfNeeded(String name, String unqualifiedClassPackage) {
      if (name == null) {
         return null;
      } else {
         return name.indexOf(46) < 0 && unqualifiedClassPackage != null ? unqualifiedClassPackage + '.' + name : name;
      }
   }

   public static String getPropertyAccessorName(String access, boolean isEmbedded, String defaultAccess) {
      return getStringValue(access, isEmbedded ? "embedded" : defaultAccess);
   }

   public static MetaAttributeContext extractMetaAttributeContext(List metaElementList, boolean onlyInheritable, MetaAttributeContext parentContext) {
      MetaAttributeContext subContext = new MetaAttributeContext(parentContext);

      for(JaxbMetaElement metaElement : metaElementList) {
         if (!(onlyInheritable & !metaElement.isInherit())) {
            String name = metaElement.getAttribute();
            MetaAttribute inheritedMetaAttribute = parentContext.getMetaAttribute(name);
            MetaAttribute metaAttribute = subContext.getLocalMetaAttribute(name);
            if (metaAttribute == null || metaAttribute == inheritedMetaAttribute) {
               metaAttribute = new MetaAttribute(name);
               subContext.add(metaAttribute);
            }

            metaAttribute.addValue(metaElement.getValue());
         }
      }

      return subContext;
   }

   public static String getStringValue(String value, String defaultValue) {
      return value == null ? defaultValue : value;
   }

   public static int getIntValue(String value, int defaultValue) {
      return value == null ? defaultValue : Integer.parseInt(value);
   }

   public static long getLongValue(String value, long defaultValue) {
      return value == null ? defaultValue : Long.parseLong(value);
   }

   public static boolean getBooleanValue(Boolean value, boolean defaultValue) {
      return value == null ? defaultValue : value;
   }

   public static Iterable interpretCascadeStyles(String cascades, LocalBindingContext bindingContext) {
      Set<CascadeStyle> cascadeStyles = new HashSet();
      if (StringHelper.isEmpty(cascades)) {
         cascades = bindingContext.getMappingDefaults().getCascadeStyle();
      }

      for(String cascade : StringHelper.split(",", cascades)) {
         cascadeStyles.add(CascadeStyle.getCascadeStyle(cascade));
      }

      return cascadeStyles;
   }

   public static Map extractParameters(List xmlParamElements) {
      if (xmlParamElements != null && !xmlParamElements.isEmpty()) {
         HashMap<String, String> params = new HashMap();

         for(JaxbParamElement paramElement : xmlParamElements) {
            params.put(paramElement.getName(), paramElement.getValue());
         }

         return params;
      } else {
         return null;
      }
   }

   public static Iterable buildMetaAttributeSources(List metaElements) {
      ArrayList<MetaAttributeSource> result = new ArrayList();
      if (metaElements != null && !metaElements.isEmpty()) {
         for(final JaxbMetaElement metaElement : metaElements) {
            result.add(new MetaAttributeSource() {
               public String getName() {
                  return metaElement.getAttribute();
               }

               public String getValue() {
                  return metaElement.getValue();
               }

               public boolean isInheritable() {
                  return metaElement.isInherit();
               }
            });
         }
      }

      return result;
   }

   public static Schema.Name determineDatabaseSchemaName(String explicitSchemaName, String explicitCatalogName, LocalBindingContext bindingContext) {
      return new Schema.Name(resolveIdentifier(explicitSchemaName, bindingContext.getMappingDefaults().getSchemaName(), bindingContext.isGloballyQuotedIdentifiers()), resolveIdentifier(explicitCatalogName, bindingContext.getMappingDefaults().getCatalogName(), bindingContext.isGloballyQuotedIdentifiers()));
   }

   public static Identifier resolveIdentifier(String explicitName, String defaultName, boolean globalQuoting) {
      String name = StringHelper.isNotEmpty(explicitName) ? explicitName : defaultName;
      if (globalQuoting) {
         name = StringHelper.quote(name);
      }

      return Identifier.toIdentifier(name);
   }

   public static List buildValueSources(ValueSourcesAdapter valueSourcesAdapter, LocalBindingContext bindingContext) {
      List<RelationalValueSource> result = new ArrayList();
      if (StringHelper.isNotEmpty(valueSourcesAdapter.getColumnAttribute())) {
         if (valueSourcesAdapter.getColumnOrFormulaElements() != null && !valueSourcesAdapter.getColumnOrFormulaElements().isEmpty()) {
            throw new MappingException("column/formula attribute may not be used together with <column>/<formula> subelement", bindingContext.getOrigin());
         }

         if (StringHelper.isNotEmpty(valueSourcesAdapter.getFormulaAttribute())) {
            throw new MappingException("column and formula attributes may not be used together", bindingContext.getOrigin());
         }

         result.add(new ColumnAttributeSourceImpl(valueSourcesAdapter.getContainingTableName(), valueSourcesAdapter.getColumnAttribute(), valueSourcesAdapter.isIncludedInInsertByDefault(), valueSourcesAdapter.isIncludedInUpdateByDefault(), valueSourcesAdapter.isForceNotNull()));
      } else if (StringHelper.isNotEmpty(valueSourcesAdapter.getFormulaAttribute())) {
         if (valueSourcesAdapter.getColumnOrFormulaElements() != null && !valueSourcesAdapter.getColumnOrFormulaElements().isEmpty()) {
            throw new MappingException("column/formula attribute may not be used together with <column>/<formula> subelement", bindingContext.getOrigin());
         }

         result.add(new FormulaImpl(valueSourcesAdapter.getContainingTableName(), valueSourcesAdapter.getFormulaAttribute()));
      } else if (valueSourcesAdapter.getColumnOrFormulaElements() != null && !valueSourcesAdapter.getColumnOrFormulaElements().isEmpty()) {
         for(Object columnOrFormulaElement : valueSourcesAdapter.getColumnOrFormulaElements()) {
            if (JaxbColumnElement.class.isInstance(columnOrFormulaElement)) {
               result.add(new ColumnSourceImpl(valueSourcesAdapter.getContainingTableName(), (JaxbColumnElement)columnOrFormulaElement, valueSourcesAdapter.isIncludedInInsertByDefault(), valueSourcesAdapter.isIncludedInUpdateByDefault(), valueSourcesAdapter.isForceNotNull()));
            } else {
               result.add(new FormulaImpl(valueSourcesAdapter.getContainingTableName(), (String)columnOrFormulaElement));
            }
         }
      }

      return result;
   }

   public static Class classForName(String className, ServiceRegistry serviceRegistry) {
      ClassLoaderService classLoaderService = (ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class);

      try {
         return classLoaderService.classForName(className);
      } catch (ClassLoadingException var4) {
         throw new org.hibernate.MappingException("Could not find class: " + className);
      }
   }

   public static class ValueSourcesAdapter {
      public String getContainingTableName() {
         return null;
      }

      public boolean isIncludedInInsertByDefault() {
         return false;
      }

      public boolean isIncludedInUpdateByDefault() {
         return false;
      }

      public String getColumnAttribute() {
         return null;
      }

      public String getFormulaAttribute() {
         return null;
      }

      public List getColumnOrFormulaElements() {
         return null;
      }

      public boolean isForceNotNull() {
         return false;
      }
   }
}
