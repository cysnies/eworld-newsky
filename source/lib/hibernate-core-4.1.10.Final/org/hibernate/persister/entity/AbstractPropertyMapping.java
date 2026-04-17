package org.hibernate.persister.entity;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractPropertyMapping implements PropertyMapping {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractPropertyMapping.class.getName());
   private final Map typesByPropertyPath = new HashMap();
   private final Map columnsByPropertyPath = new HashMap();
   private final Map columnReadersByPropertyPath = new HashMap();
   private final Map columnReaderTemplatesByPropertyPath = new HashMap();
   private final Map formulaTemplatesByPropertyPath = new HashMap();

   public String[] getIdentifierColumnNames() {
      throw new UnsupportedOperationException("one-to-one is not supported here");
   }

   public String[] getIdentifierColumnReaderTemplates() {
      throw new UnsupportedOperationException("one-to-one is not supported here");
   }

   public String[] getIdentifierColumnReaders() {
      throw new UnsupportedOperationException("one-to-one is not supported here");
   }

   protected abstract String getEntityName();

   public Type toType(String propertyName) throws QueryException {
      Type type = (Type)this.typesByPropertyPath.get(propertyName);
      if (type == null) {
         throw this.propertyException(propertyName);
      } else {
         return type;
      }
   }

   protected final QueryException propertyException(String propertyName) throws QueryException {
      return new QueryException("could not resolve property: " + propertyName + " of: " + this.getEntityName());
   }

   public String[] getColumnNames(String propertyName) {
      String[] cols = (String[])this.columnsByPropertyPath.get(propertyName);
      if (cols == null) {
         throw new MappingException("unknown property: " + propertyName);
      } else {
         return cols;
      }
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      String[] columns = (String[])this.columnsByPropertyPath.get(propertyName);
      if (columns == null) {
         throw this.propertyException(propertyName);
      } else {
         String[] formulaTemplates = (String[])this.formulaTemplatesByPropertyPath.get(propertyName);
         String[] columnReaderTemplates = (String[])this.columnReaderTemplatesByPropertyPath.get(propertyName);
         String[] result = new String[columns.length];

         for(int i = 0; i < columns.length; ++i) {
            if (columnReaderTemplates[i] == null) {
               result[i] = StringHelper.replace(formulaTemplates[i], "$PlaceHolder$", alias);
            } else {
               result[i] = StringHelper.replace(columnReaderTemplates[i], "$PlaceHolder$", alias);
            }
         }

         return result;
      }
   }

   public String[] toColumns(String propertyName) throws QueryException {
      String[] columns = (String[])this.columnsByPropertyPath.get(propertyName);
      if (columns == null) {
         throw this.propertyException(propertyName);
      } else {
         String[] formulaTemplates = (String[])this.formulaTemplatesByPropertyPath.get(propertyName);
         String[] columnReaders = (String[])this.columnReadersByPropertyPath.get(propertyName);
         String[] result = new String[columns.length];

         for(int i = 0; i < columns.length; ++i) {
            if (columnReaders[i] == null) {
               result[i] = StringHelper.replace(formulaTemplates[i], "$PlaceHolder$", "");
            } else {
               result[i] = columnReaders[i];
            }
         }

         return result;
      }
   }

   protected void addPropertyPath(String path, Type type, String[] columns, String[] columnReaders, String[] columnReaderTemplates, String[] formulaTemplates) {
      if (this.typesByPropertyPath.containsKey(path)) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Skipping duplicate registration of path [{0}], existing type = [{1}], incoming type = [{2}]", path, this.typesByPropertyPath.get(path), type);
         }

      } else {
         this.typesByPropertyPath.put(path, type);
         this.columnsByPropertyPath.put(path, columns);
         this.columnReadersByPropertyPath.put(path, columnReaders);
         this.columnReaderTemplatesByPropertyPath.put(path, columnReaderTemplates);
         if (formulaTemplates != null) {
            this.formulaTemplatesByPropertyPath.put(path, formulaTemplates);
         }

      }
   }

   protected void initPropertyPaths(String path, Type type, String[] columns, String[] columnReaders, String[] columnReaderTemplates, String[] formulaTemplates, Mapping factory) throws MappingException {
      if (columns.length != type.getColumnSpan(factory)) {
         throw new MappingException("broken column mapping for: " + path + " of: " + this.getEntityName());
      } else {
         if (type.isAssociationType()) {
            AssociationType actype = (AssociationType)type;
            if (actype.useLHSPrimaryKey()) {
               columns = this.getIdentifierColumnNames();
               columnReaders = this.getIdentifierColumnReaders();
               columnReaderTemplates = this.getIdentifierColumnReaderTemplates();
            } else {
               String foreignKeyProperty = actype.getLHSPropertyName();
               if (foreignKeyProperty != null && !path.equals(foreignKeyProperty)) {
                  columns = (String[])this.columnsByPropertyPath.get(foreignKeyProperty);
                  if (columns == null) {
                     return;
                  }

                  columnReaders = (String[])this.columnReadersByPropertyPath.get(foreignKeyProperty);
                  columnReaderTemplates = (String[])this.columnReaderTemplatesByPropertyPath.get(foreignKeyProperty);
               }
            }
         }

         if (path != null) {
            this.addPropertyPath(path, type, columns, columnReaders, columnReaderTemplates, formulaTemplates);
         }

         if (type.isComponentType()) {
            CompositeType actype = (CompositeType)type;
            this.initComponentPropertyPaths(path, actype, columns, columnReaders, columnReaderTemplates, formulaTemplates, factory);
            if (actype.isEmbedded()) {
               this.initComponentPropertyPaths(path == null ? null : StringHelper.qualifier(path), actype, columns, columnReaders, columnReaderTemplates, formulaTemplates, factory);
            }
         } else if (type.isEntityType()) {
            this.initIdentifierPropertyPaths(path, (EntityType)type, columns, columnReaders, columnReaderTemplates, factory);
         }

      }
   }

   protected void initIdentifierPropertyPaths(String path, EntityType etype, String[] columns, String[] columnReaders, String[] columnReaderTemplates, Mapping factory) throws MappingException {
      Type idtype = etype.getIdentifierOrUniqueKeyType(factory);
      String idPropName = etype.getIdentifierOrUniqueKeyPropertyName(factory);
      boolean hasNonIdentifierPropertyNamedId = this.hasNonIdentifierPropertyNamedId(etype, factory);
      if (etype.isReferenceToPrimaryKey() && !hasNonIdentifierPropertyNamedId) {
         String idpath1 = extendPath(path, "id");
         this.addPropertyPath(idpath1, idtype, columns, columnReaders, columnReaderTemplates, (String[])null);
         this.initPropertyPaths(idpath1, idtype, columns, columnReaders, columnReaderTemplates, (String[])null, factory);
      }

      if (idPropName != null) {
         String idpath2 = extendPath(path, idPropName);
         this.addPropertyPath(idpath2, idtype, columns, columnReaders, columnReaderTemplates, (String[])null);
         this.initPropertyPaths(idpath2, idtype, columns, columnReaders, columnReaderTemplates, (String[])null, factory);
      }

   }

   private boolean hasNonIdentifierPropertyNamedId(EntityType entityType, Mapping factory) {
      try {
         return factory.getReferencedPropertyType(entityType.getAssociatedEntityName(), "id") != null;
      } catch (MappingException var4) {
         return false;
      }
   }

   protected void initComponentPropertyPaths(String path, CompositeType type, String[] columns, String[] columnReaders, String[] columnReaderTemplates, String[] formulaTemplates, Mapping factory) throws MappingException {
      Type[] types = type.getSubtypes();
      String[] properties = type.getPropertyNames();
      int begin = 0;

      for(int i = 0; i < properties.length; ++i) {
         String subpath = extendPath(path, properties[i]);

         try {
            int length = types[i].getColumnSpan(factory);
            String[] columnSlice = ArrayHelper.slice(columns, begin, length);
            String[] columnReaderSlice = ArrayHelper.slice(columnReaders, begin, length);
            String[] columnReaderTemplateSlice = ArrayHelper.slice(columnReaderTemplates, begin, length);
            String[] formulaSlice = formulaTemplates == null ? null : ArrayHelper.slice(formulaTemplates, begin, length);
            this.initPropertyPaths(subpath, types[i], columnSlice, columnReaderSlice, columnReaderTemplateSlice, formulaSlice, factory);
            begin += length;
         } catch (Exception e) {
            throw new MappingException("bug in initComponentPropertyPaths", e);
         }
      }

   }

   private static String extendPath(String path, String property) {
      return StringHelper.isEmpty(path) ? property : StringHelper.qualify(path, property);
   }
}
