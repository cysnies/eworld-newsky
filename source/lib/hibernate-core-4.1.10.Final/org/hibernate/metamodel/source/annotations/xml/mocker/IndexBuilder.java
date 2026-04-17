package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.annotations.xml.filter.IndexedAnnotationFilter;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.logging.Logger;

public class IndexBuilder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IndexBuilder.class.getName());
   private Map annotations;
   private Map subclasses;
   private Map implementors;
   private Map classes;
   private Index index;
   private Map classInfoAnnotationsMap;
   private Map indexedClassInfoAnnotationsMap;
   private ServiceRegistry serviceRegistry;

   IndexBuilder(Index index, ServiceRegistry serviceRegistry) {
      this.index = index;
      this.serviceRegistry = serviceRegistry;
      this.annotations = new HashMap();
      this.subclasses = new HashMap();
      this.implementors = new HashMap();
      this.classes = new HashMap();
      this.classInfoAnnotationsMap = new HashMap();
      this.indexedClassInfoAnnotationsMap = new HashMap();
   }

   Index build(EntityMappingsMocker.Default globalDefaults) {
      for(ClassInfo ci : this.index.getKnownClasses()) {
         DotName name = ci.name();
         if (!this.indexedClassInfoAnnotationsMap.containsKey(name) && ci.annotations() != null && !ci.annotations().isEmpty()) {
            Map<DotName, List<AnnotationInstance>> tmp = new HashMap(ci.annotations());
            DefaultConfigurationHelper.INSTANCE.applyDefaults(tmp, globalDefaults);
            this.mergeAnnotationMap(tmp, this.annotations);
            this.classes.put(name, ci);
            if (ci.superName() != null) {
               this.addSubClasses(ci.superName(), ci);
            }

            if (ci.interfaces() != null && ci.interfaces().length > 0) {
               this.addImplementors(ci.interfaces(), ci);
            }
         }
      }

      return Index.create(this.annotations, this.subclasses, this.implementors, this.classes);
   }

   Map getAnnotations() {
      return Collections.unmodifiableMap(this.annotations);
   }

   void mappingMetadataComplete() {
      LOG.debug("xml-mapping-metadata-complete is specified in persistence-unit-metadata, ignore JPA annotations.");
      this.index = Index.create(new HashMap(), new HashMap(), new HashMap(), new HashMap());
   }

   void metadataComplete(DotName name) {
      LOG.debug("metadata-complete is specified in " + name + ", ignore JPA annotations.");
      this.getIndexedAnnotations(name).clear();
   }

   public Map getIndexedAnnotations(DotName name) {
      Map<DotName, List<AnnotationInstance>> map = (Map)this.indexedClassInfoAnnotationsMap.get(name);
      if (map == null) {
         ClassInfo ci = this.index.getClassByName(name);
         if (ci != null && ci.annotations() != null) {
            map = new HashMap(ci.annotations());

            for(DotName globalAnnotationName : DefaultConfigurationHelper.GLOBAL_ANNOTATIONS) {
               if (map.containsKey(globalAnnotationName)) {
                  map.put(globalAnnotationName, Collections.emptyList());
               }
            }
         } else {
            map = Collections.emptyMap();
         }

         this.indexedClassInfoAnnotationsMap.put(name, map);
      }

      return map;
   }

   public Map getClassInfoAnnotationsMap(DotName name) {
      return (Map)this.classInfoAnnotationsMap.get(name);
   }

   public ClassInfo getClassInfo(DotName name) {
      return (ClassInfo)this.classes.get(name);
   }

   public ClassInfo getIndexedClassInfo(DotName name) {
      return this.index.getClassByName(name);
   }

   void collectGlobalConfigurationFromIndex(GlobalAnnotations globalAnnotations) {
      for(DotName annName : DefaultConfigurationHelper.GLOBAL_ANNOTATIONS) {
         List<AnnotationInstance> annotationInstanceList = this.index.getAnnotations(annName);
         if (MockHelper.isNotEmpty(annotationInstanceList)) {
            globalAnnotations.addIndexedAnnotationInstance(annotationInstanceList);
         }
      }

      globalAnnotations.filterIndexedAnnotations();
   }

   void finishGlobalConfigurationMocking(GlobalAnnotations globalAnnotations) {
      this.annotations.putAll(globalAnnotations.getAnnotationInstanceMap());
   }

   void finishEntityObject(DotName name, EntityMappingsMocker.Default defaults) {
      Map<DotName, List<AnnotationInstance>> map = (Map)this.classInfoAnnotationsMap.get(name);
      if (map == null) {
         throw new AssertionFailure("Calling finish entity object " + name + " before create it.");
      } else {
         if (this.indexedClassInfoAnnotationsMap.containsKey(name)) {
            Map<DotName, List<AnnotationInstance>> tmp = this.getIndexedAnnotations(name);
            this.mergeAnnotationMap(tmp, map);
         }

         DefaultConfigurationHelper.INSTANCE.applyDefaults(map, defaults);
         this.mergeAnnotationMap(map, this.annotations);
      }
   }

   void addAnnotationInstance(DotName targetClassName, AnnotationInstance annotationInstance) {
      if (annotationInstance != null) {
         for(IndexedAnnotationFilter indexedAnnotationFilter : IndexedAnnotationFilter.ALL_FILTERS) {
            indexedAnnotationFilter.beforePush(this, targetClassName, annotationInstance);
         }

         Map<DotName, List<AnnotationInstance>> map = (Map)this.classInfoAnnotationsMap.get(targetClassName);
         if (map == null) {
            throw new AssertionFailure("Can't find " + targetClassName + " in internal cache, should call createClassInfo first");
         } else {
            List<AnnotationInstance> annotationInstanceList = (List)map.get(annotationInstance.name());
            if (annotationInstanceList == null) {
               annotationInstanceList = new ArrayList();
               map.put(annotationInstance.name(), annotationInstanceList);
            }

            annotationInstanceList.add(annotationInstance);
         }
      }
   }

   ServiceRegistry getServiceRegistry() {
      return this.serviceRegistry;
   }

   ClassInfo createClassInfo(String className) {
      if (StringHelper.isEmpty(className)) {
         throw new AssertionFailure("Class Name used to create ClassInfo is empty.");
      } else {
         DotName classDotName = DotName.createSimple(className);
         if (this.classes.containsKey(classDotName)) {
            return (ClassInfo)this.classes.get(classDotName);
         } else {
            Class clazz = ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(className);
            DotName superName = null;
            DotName[] interfaces = null;
            ClassInfo annClassInfo = this.index.getClassByName(classDotName);
            short access_flag;
            if (annClassInfo != null) {
               superName = annClassInfo.superName();
               interfaces = annClassInfo.interfaces();
               access_flag = annClassInfo.flags();
            } else {
               Class superClass = clazz.getSuperclass();
               if (superClass != null) {
                  superName = DotName.createSimple(superClass.getName());
               }

               Class[] classInterfaces = clazz.getInterfaces();
               if (classInterfaces != null && classInterfaces.length > 0) {
                  interfaces = new DotName[classInterfaces.length];

                  for(int i = 0; i < classInterfaces.length; ++i) {
                     interfaces[i] = DotName.createSimple(classInterfaces[i].getName());
                  }
               }

               access_flag = (short)(clazz.getModifiers() | 32);
            }

            Map<DotName, List<AnnotationInstance>> map = new HashMap();
            this.classInfoAnnotationsMap.put(classDotName, map);
            ClassInfo classInfo = ClassInfo.create(classDotName, superName, access_flag, interfaces, map);
            this.classes.put(classDotName, classInfo);
            this.addSubClasses(superName, classInfo);
            this.addImplementors(interfaces, classInfo);
            return classInfo;
         }
      }
   }

   private void addSubClasses(DotName superClassDotName, ClassInfo classInfo) {
      if (superClassDotName != null) {
         List<ClassInfo> classInfoList = (List)this.subclasses.get(superClassDotName);
         if (classInfoList == null) {
            classInfoList = new ArrayList();
            this.subclasses.put(superClassDotName, classInfoList);
         }

         classInfoList.add(classInfo);
      }

   }

   private void addImplementors(DotName[] dotNames, ClassInfo classInfo) {
      if (dotNames != null && dotNames.length > 0) {
         for(DotName dotName : dotNames) {
            List<ClassInfo> classInfoList = (List)this.implementors.get(dotName);
            if (classInfoList == null) {
               classInfoList = new ArrayList();
               this.implementors.put(dotName, classInfoList);
            }

            classInfoList.add(classInfo);
         }
      }

   }

   private void mergeAnnotationMap(Map source, Map target) {
      if (source != null) {
         for(Map.Entry el : source.entrySet()) {
            if (!((List)el.getValue()).isEmpty()) {
               DotName annotationName = (DotName)el.getKey();
               List<AnnotationInstance> value = (List)el.getValue();
               List<AnnotationInstance> annotationInstanceList = (List)target.get(annotationName);
               if (annotationInstanceList == null) {
                  annotationInstanceList = new ArrayList();
                  target.put(annotationName, annotationInstanceList);
               }

               annotationInstanceList.addAll(value);
            }
         }
      }

   }
}
