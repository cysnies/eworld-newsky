package org.hibernate.metamodel.source.annotations.xml.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.metamodel.source.annotations.xml.mocker.MockHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

class ExclusiveAnnotationFilter extends AbstractAnnotationFilter {
   public static ExclusiveAnnotationFilter INSTANCE = new ExclusiveAnnotationFilter();
   private DotName[] targetNames;
   private List exclusiveGroupList = this.getExclusiveGroupList();

   private ExclusiveAnnotationFilter() {
      Set<DotName> names = new HashSet();

      for(ExclusiveGroup group : this.exclusiveGroupList) {
         names.addAll(group.getNames());
      }

      this.targetNames = (DotName[])names.toArray(new DotName[names.size()]);
   }

   private List getExclusiveGroupList() {
      if (this.exclusiveGroupList == null) {
         this.exclusiveGroupList = new ArrayList();
         ExclusiveGroup group = new ExclusiveGroup();
         group.add(ENTITY);
         group.add(MAPPED_SUPERCLASS);
         group.add(EMBEDDABLE);
         group.scope = ExclusiveAnnotationFilter.Scope.TYPE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(SECONDARY_TABLES);
         group.add(SECONDARY_TABLE);
         group.scope = ExclusiveAnnotationFilter.Scope.TYPE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(PRIMARY_KEY_JOIN_COLUMNS);
         group.add(PRIMARY_KEY_JOIN_COLUMN);
         group.scope = ExclusiveAnnotationFilter.Scope.ATTRIBUTE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(SQL_RESULT_SET_MAPPING);
         group.add(SQL_RESULT_SET_MAPPINGS);
         group.scope = ExclusiveAnnotationFilter.Scope.TYPE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(NAMED_NATIVE_QUERY);
         group.add(NAMED_NATIVE_QUERIES);
         group.scope = ExclusiveAnnotationFilter.Scope.TYPE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(NAMED_QUERY);
         group.add(NAMED_QUERIES);
         group.scope = ExclusiveAnnotationFilter.Scope.TYPE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(ATTRIBUTE_OVERRIDES);
         group.add(ATTRIBUTE_OVERRIDE);
         group.scope = ExclusiveAnnotationFilter.Scope.ATTRIBUTE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(ASSOCIATION_OVERRIDE);
         group.add(ASSOCIATION_OVERRIDES);
         group.scope = ExclusiveAnnotationFilter.Scope.ATTRIBUTE;
         this.exclusiveGroupList.add(group);
         group = new ExclusiveGroup();
         group.add(MAP_KEY_JOIN_COLUMN);
         group.add(MAP_KEY_JOIN_COLUMNS);
         group.scope = ExclusiveAnnotationFilter.Scope.ATTRIBUTE;
         this.exclusiveGroupList.add(group);
      }

      return this.exclusiveGroupList;
   }

   protected void overrideIndexedAnnotationMap(DotName annName, AnnotationInstance annotationInstance, Map map) {
      ExclusiveGroup group = this.getExclusiveGroup(annName);
      if (group != null) {
         AnnotationTarget target = annotationInstance.target();

         for(DotName entityAnnName : group) {
            if (map.containsKey(entityAnnName)) {
               switch (group.scope) {
                  case TYPE:
                     map.put(entityAnnName, Collections.emptyList());
                     break;
                  case ATTRIBUTE:
                     List<AnnotationInstance> indexedAnnotationInstanceList = (List)map.get(entityAnnName);
                     Iterator<AnnotationInstance> iter = indexedAnnotationInstanceList.iterator();

                     while(iter.hasNext()) {
                        AnnotationInstance ann = (AnnotationInstance)iter.next();
                        if (MockHelper.targetEquals(target, ann.target())) {
                           iter.remove();
                        }
                     }
               }
            }
         }

      }
   }

   protected DotName[] targetAnnotation() {
      return this.targetNames;
   }

   private ExclusiveGroup getExclusiveGroup(DotName annName) {
      for(ExclusiveGroup group : this.exclusiveGroupList) {
         if (group.contains(annName)) {
            return group;
         }
      }

      return null;
   }

   static enum Scope {
      TYPE,
      ATTRIBUTE;
   }

   private class ExclusiveGroup implements Iterable {
      private Set names;
      Scope scope;

      private ExclusiveGroup() {
         this.names = new HashSet();
         this.scope = ExclusiveAnnotationFilter.Scope.ATTRIBUTE;
      }

      public Set getNames() {
         return this.names;
      }

      public Iterator iterator() {
         return this.names.iterator();
      }

      boolean contains(DotName name) {
         return this.names.contains(name);
      }

      void add(DotName name) {
         this.names.add(name);
      }
   }
}
