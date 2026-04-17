package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Element;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryCollectionReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryJoinReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.type.Type;

public abstract class ResultSetMappingBinder {
   protected static ResultSetMappingDefinition buildResultSetMappingDefinition(Element resultSetElem, String path, Mappings mappings) {
      String resultSetName = resultSetElem.attribute("name").getValue();
      if (path != null) {
         resultSetName = path + '.' + resultSetName;
      }

      ResultSetMappingDefinition definition = new ResultSetMappingDefinition(resultSetName);
      int cnt = 0;
      Iterator returns = resultSetElem.elementIterator();

      while(returns.hasNext()) {
         ++cnt;
         Element returnElem = (Element)returns.next();
         String name = returnElem.getName();
         if ("return-scalar".equals(name)) {
            String column = returnElem.attributeValue("column");
            String typeFromXML = HbmBinder.getTypeFromXML(returnElem);
            Type type = null;
            if (typeFromXML != null) {
               type = mappings.getTypeResolver().heuristicType(typeFromXML);
               if (type == null) {
                  throw new MappingException("could not determine type " + type);
               }
            }

            definition.addQueryReturn(new NativeSQLQueryScalarReturn(column, type));
         } else if ("return".equals(name)) {
            definition.addQueryReturn(bindReturn(returnElem, mappings, cnt));
         } else if ("return-join".equals(name)) {
            definition.addQueryReturn(bindReturnJoin(returnElem, mappings));
         } else if ("load-collection".equals(name)) {
            definition.addQueryReturn(bindLoadCollection(returnElem, mappings));
         }
      }

      return definition;
   }

   private static NativeSQLQueryRootReturn bindReturn(Element returnElem, Mappings mappings, int elementCount) {
      String alias = returnElem.attributeValue("alias");
      if (StringHelper.isEmpty(alias)) {
         alias = "alias_" + elementCount;
      }

      String entityName = HbmBinder.getEntityName(returnElem, mappings);
      if (entityName == null) {
         throw new MappingException("<return alias='" + alias + "'> must specify either a class or entity-name");
      } else {
         LockMode lockMode = getLockMode(returnElem.attributeValue("lock-mode"));
         PersistentClass pc = mappings.getClass(entityName);
         Map propertyResults = bindPropertyResults(alias, returnElem, pc, mappings);
         return new NativeSQLQueryRootReturn(alias, entityName, propertyResults, lockMode);
      }
   }

   private static NativeSQLQueryJoinReturn bindReturnJoin(Element returnElem, Mappings mappings) {
      String alias = returnElem.attributeValue("alias");
      String roleAttribute = returnElem.attributeValue("property");
      LockMode lockMode = getLockMode(returnElem.attributeValue("lock-mode"));
      int dot = roleAttribute.lastIndexOf(46);
      if (dot == -1) {
         throw new MappingException("Role attribute for sql query return [alias=" + alias + "] not formatted correctly {owningAlias.propertyName}");
      } else {
         String roleOwnerAlias = roleAttribute.substring(0, dot);
         String roleProperty = roleAttribute.substring(dot + 1);
         Map propertyResults = bindPropertyResults(alias, returnElem, (PersistentClass)null, mappings);
         return new NativeSQLQueryJoinReturn(alias, roleOwnerAlias, roleProperty, propertyResults, lockMode);
      }
   }

   private static NativeSQLQueryCollectionReturn bindLoadCollection(Element returnElem, Mappings mappings) {
      String alias = returnElem.attributeValue("alias");
      String collectionAttribute = returnElem.attributeValue("role");
      LockMode lockMode = getLockMode(returnElem.attributeValue("lock-mode"));
      int dot = collectionAttribute.lastIndexOf(46);
      if (dot == -1) {
         throw new MappingException("Collection attribute for sql query return [alias=" + alias + "] not formatted correctly {OwnerClassName.propertyName}");
      } else {
         String ownerClassName = HbmBinder.getClassName(collectionAttribute.substring(0, dot), mappings);
         String ownerPropertyName = collectionAttribute.substring(dot + 1);
         Map propertyResults = bindPropertyResults(alias, returnElem, (PersistentClass)null, mappings);
         return new NativeSQLQueryCollectionReturn(alias, ownerClassName, ownerPropertyName, propertyResults, lockMode);
      }
   }

   private static Map bindPropertyResults(String alias, Element returnElement, PersistentClass pc, Mappings mappings) {
      HashMap propertyresults = new HashMap();
      Element discriminatorResult = returnElement.element("return-discriminator");
      if (discriminatorResult != null) {
         ArrayList resultColumns = getResultColumns(discriminatorResult);
         propertyresults.put("class", ArrayHelper.toStringArray((Collection)resultColumns));
      }

      Iterator iterator = returnElement.elementIterator("return-property");
      List properties = new ArrayList();
      List propertyNames = new ArrayList();

      while(iterator.hasNext()) {
         Element propertyresult = (Element)iterator.next();
         String name = propertyresult.attributeValue("name");
         if (pc != null && name.indexOf(46) != -1) {
            if (pc == null) {
               throw new MappingException("dotted notation in <return-join> or <load_collection> not yet supported");
            }

            int dotIndex = name.lastIndexOf(46);
            String reducedName = name.substring(0, dotIndex);
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
               PersistentClass referencedPc = mappings.getClass(toOne.getReferencedEntityName());
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

            int index = propertyNames.size();
            int followersSize = followers.size();

            for(int loop = 0; loop < followersSize; ++loop) {
               String follower = (String)followers.get(loop);
               int currentIndex = getIndexOfFirstMatchingProperty(propertyNames, follower);
               index = currentIndex != -1 && currentIndex < index ? currentIndex : index;
            }

            propertyNames.add(index, name);
            properties.add(index, propertyresult);
         } else {
            properties.add(propertyresult);
            propertyNames.add(name);
         }
      }

      Set uniqueReturnProperty = new HashSet();

      for(Element propertyresult : properties) {
         String name = propertyresult.attributeValue("name");
         if ("class".equals(name)) {
            throw new MappingException("class is not a valid property name to use in a <return-property>, use <return-discriminator> instead");
         }

         ArrayList allResultColumns = getResultColumns(propertyresult);
         if (allResultColumns.isEmpty()) {
            throw new MappingException("return-property for alias " + alias + " must specify at least one column or return-column name");
         }

         if (uniqueReturnProperty.contains(name)) {
            throw new MappingException("duplicate return-property for property " + name + " on alias " + alias);
         }

         uniqueReturnProperty.add(name);
         ArrayList intermediateResults = (ArrayList)propertyresults.get(name);
         if (intermediateResults == null) {
            propertyresults.put(name, allResultColumns);
         } else {
            intermediateResults.addAll(allResultColumns);
         }
      }

      for(Map.Entry entry : propertyresults.entrySet()) {
         if (entry.getValue() instanceof ArrayList) {
            ArrayList list = (ArrayList)entry.getValue();
            entry.setValue(list.toArray(new String[list.size()]));
         }
      }

      return (Map)(propertyresults.isEmpty() ? Collections.EMPTY_MAP : propertyresults);
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

   private static ArrayList getResultColumns(Element propertyresult) {
      String column = unquote(propertyresult.attributeValue("column"));
      ArrayList allResultColumns = new ArrayList();
      if (column != null) {
         allResultColumns.add(column);
      }

      Iterator resultColumns = propertyresult.elementIterator("return-column");

      while(resultColumns.hasNext()) {
         Element element = (Element)resultColumns.next();
         allResultColumns.add(unquote(element.attributeValue("name")));
      }

      return allResultColumns;
   }

   private static String unquote(String name) {
      if (name != null && name.charAt(0) == '`') {
         name = name.substring(1, name.length() - 1);
      }

      return name;
   }

   private static LockMode getLockMode(String lockMode) {
      if (lockMode != null && !"read".equals(lockMode)) {
         if ("none".equals(lockMode)) {
            return LockMode.NONE;
         } else if ("upgrade".equals(lockMode)) {
            return LockMode.UPGRADE;
         } else if ("upgrade-nowait".equals(lockMode)) {
            return LockMode.UPGRADE_NOWAIT;
         } else if ("write".equals(lockMode)) {
            return LockMode.WRITE;
         } else if ("force".equals(lockMode)) {
            return LockMode.FORCE;
         } else if ("optimistic".equals(lockMode)) {
            return LockMode.OPTIMISTIC;
         } else if ("optimistic_force_increment".equals(lockMode)) {
            return LockMode.OPTIMISTIC_FORCE_INCREMENT;
         } else if ("pessimistic_read".equals(lockMode)) {
            return LockMode.PESSIMISTIC_READ;
         } else if ("pessimistic_write".equals(lockMode)) {
            return LockMode.PESSIMISTIC_WRITE;
         } else if ("pessimistic_force_increment".equals(lockMode)) {
            return LockMode.PESSIMISTIC_FORCE_INCREMENT;
         } else {
            throw new MappingException("unknown lockmode");
         }
      } else {
         return LockMode.READ;
      }
   }
}
