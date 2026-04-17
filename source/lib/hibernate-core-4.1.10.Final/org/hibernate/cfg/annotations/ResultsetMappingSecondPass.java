package org.hibernate.cfg.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.ColumnResult;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.SqlResultSetMapping;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.QuerySecondPass;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class ResultsetMappingSecondPass implements QuerySecondPass {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ResultsetMappingSecondPass.class.getName());
   private SqlResultSetMapping ann;
   private Mappings mappings;
   private boolean isDefault;

   public ResultsetMappingSecondPass(SqlResultSetMapping ann, Mappings mappings, boolean isDefault) {
      this.ann = ann;
      this.mappings = mappings;
      this.isDefault = isDefault;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      if (this.ann != null) {
         ResultSetMappingDefinition definition = new ResultSetMappingDefinition(this.ann.name());
         LOG.debugf("Binding result set mapping: %s", definition.getName());
         int entityAliasIndex = 0;

         for(EntityResult entity : this.ann.entities()) {
            List<FieldResult> properties = new ArrayList();
            List<String> propertyNames = new ArrayList();

            for(FieldResult field : entity.fields()) {
               String name = field.name();
               if (name.indexOf(46) == -1) {
                  properties.add(field);
                  propertyNames.add(name);
               } else {
                  PersistentClass pc = this.mappings.getClass(entity.entityClass().getName());
                  if (pc == null) {
                     throw new MappingException("Entity not found " + entity.entityClass().getName() + " in SqlResultsetMapping " + this.ann.name());
                  }

                  int dotIndex = name.lastIndexOf(46);
                  String reducedName = name.substring(0, dotIndex);
                  Iterator parentPropIter = this.getSubPropertyIterator(pc, reducedName);
                  List followers = this.getFollowers(parentPropIter, reducedName, name);
                  int index = propertyNames.size();
                  int followersSize = followers.size();

                  for(int loop = 0; loop < followersSize; ++loop) {
                     String follower = (String)followers.get(loop);
                     int currentIndex = getIndexOfFirstMatchingProperty(propertyNames, follower);
                     index = currentIndex != -1 && currentIndex < index ? currentIndex : index;
                  }

                  propertyNames.add(index, name);
                  properties.add(index, field);
               }
            }

            Set<String> uniqueReturnProperty = new HashSet();
            Map<String, ArrayList<String>> propertyResultsTmp = new HashMap();

            for(Object property : properties) {
               FieldResult propertyresult = (FieldResult)property;
               String name = propertyresult.name();
               if ("class".equals(name)) {
                  throw new MappingException("class is not a valid property name to use in a @FieldResult, use @Entity(discriminatorColumn) instead");
               }

               if (uniqueReturnProperty.contains(name)) {
                  throw new MappingException("duplicate @FieldResult for property " + name + " on @Entity " + entity.entityClass().getName() + " in " + this.ann.name());
               }

               uniqueReturnProperty.add(name);
               String quotingNormalizedColumnName = this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(propertyresult.column());
               String key = StringHelper.root(name);
               ArrayList<String> intermediateResults = (ArrayList)propertyResultsTmp.get(key);
               if (intermediateResults == null) {
                  intermediateResults = new ArrayList();
                  propertyResultsTmp.put(key, intermediateResults);
               }

               intermediateResults.add(quotingNormalizedColumnName);
            }

            Map<String, String[]> propertyResults = new HashMap();

            for(Map.Entry entry : propertyResultsTmp.entrySet()) {
               propertyResults.put(entry.getKey(), ((ArrayList)entry.getValue()).toArray(new String[((ArrayList)entry.getValue()).size()]));
            }

            if (!BinderHelper.isEmptyAnnotationValue(entity.discriminatorColumn())) {
               String quotingNormalizedName = this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(entity.discriminatorColumn());
               propertyResults.put("class", new String[]{quotingNormalizedName});
            }

            if (propertyResults.isEmpty()) {
               propertyResults = Collections.emptyMap();
            }

            NativeSQLQueryRootReturn result = new NativeSQLQueryRootReturn("alias" + entityAliasIndex++, entity.entityClass().getName(), propertyResults, LockMode.READ);
            definition.addQueryReturn(result);
         }

         for(ColumnResult column : this.ann.columns()) {
            definition.addQueryReturn(new NativeSQLQueryScalarReturn(this.mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(column.name()), (Type)null));
         }

         if (this.isDefault) {
            this.mappings.addDefaultResultSetMapping(definition);
         } else {
            this.mappings.addResultSetMapping(definition);
         }

      }
   }

   private List getFollowers(Iterator parentPropIter, String reducedName, String name) {
      boolean hasFollowers = false;
      List followers = new ArrayList();

      while(parentPropIter.hasNext()) {
         String currentPropertyName = ((Property)parentPropIter.next()).getName();
         String currentName = reducedName + '.' + currentPropertyName;
         if (hasFollowers) {
            followers.add(currentName);
         }

         if (name.equals(currentName)) {
            hasFollowers = true;
         }
      }

      return followers;
   }

   private Iterator getSubPropertyIterator(PersistentClass pc, String reducedName) {
      Value value = pc.getRecursiveProperty(reducedName).getValue();
      Iterator parentPropIter;
      if (value instanceof Component) {
         Component comp = (Component)value;
         parentPropIter = comp.getPropertyIterator();
      } else {
         if (!(value instanceof ToOne)) {
            throw new MappingException("dotted notation reference neither a component nor a many/one to one");
         }

         ToOne toOne = (ToOne)value;
         PersistentClass referencedPc = this.mappings.getClass(toOne.getReferencedEntityName());
         if (toOne.getReferencedPropertyName() != null) {
            try {
               parentPropIter = ((Component)referencedPc.getRecursiveProperty(toOne.getReferencedPropertyName()).getValue()).getPropertyIterator();
            } catch (ClassCastException e) {
               throw new MappingException("dotted notation reference neither a component nor a many/one to one", e);
            }
         } else {
            try {
               if (referencedPc.getIdentifierMapper() == null) {
                  parentPropIter = ((Component)referencedPc.getIdentifierProperty().getValue()).getPropertyIterator();
               } else {
                  parentPropIter = referencedPc.getIdentifierMapper().getPropertyIterator();
               }
            } catch (ClassCastException e) {
               throw new MappingException("dotted notation reference neither a component nor a many/one to one", e);
            }
         }
      }

      return parentPropIter;
   }

   private static int getIndexOfFirstMatchingProperty(List propertyNames, String follower) {
      int propertySize = propertyNames.size();

      for(int propIndex = 0; propIndex < propertySize; ++propIndex) {
         if (((String)propertyNames.get(propIndex)).startsWith(follower)) {
            return propIndex;
         }
      }

      return -1;
   }
}
