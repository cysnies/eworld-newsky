package org.hibernate.metamodel.source.annotations;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationOverrides;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.domain.Type;
import org.hibernate.metamodel.source.MappingDefaults;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

public class AnnotationBindingContextImpl implements AnnotationBindingContext {
   private final MetadataImplementor metadata;
   private final ValueHolder classLoaderService;
   private final Index index;
   private final TypeResolver typeResolver = new TypeResolver();
   private final Map resolvedTypeCache = new HashMap();
   private Map nameToJavaTypeMap = new HashMap();

   public AnnotationBindingContextImpl(MetadataImplementor metadata, Index index) {
      this.metadata = metadata;
      this.classLoaderService = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public ClassLoaderService initialize() {
            return (ClassLoaderService)AnnotationBindingContextImpl.this.metadata.getServiceRegistry().getService(ClassLoaderService.class);
         }
      });
      this.index = index;
   }

   public Index getIndex() {
      return this.index;
   }

   public ClassInfo getClassInfo(String name) {
      DotName dotName = DotName.createSimple(name);
      return this.index.getClassByName(dotName);
   }

   public void resolveAllTypes(String className) {
      Class<?> clazz = ((ClassLoaderService)this.classLoaderService.getValue()).classForName(className);
      ResolvedType resolvedType = this.typeResolver.resolve(clazz);

      while(resolvedType != null) {
         this.resolvedTypeCache.put(clazz, resolvedType);
         resolvedType = resolvedType.getParentClass();
         if (resolvedType != null) {
            clazz = resolvedType.getErasedType();
         }
      }

   }

   public ResolvedType getResolvedType(Class clazz) {
      return (ResolvedType)this.resolvedTypeCache.get(clazz);
   }

   public ResolvedTypeWithMembers resolveMemberTypes(ResolvedType type) {
      MemberResolver memberResolver = new MemberResolver(this.typeResolver);
      return memberResolver.resolve(type, (AnnotationConfiguration)null, (AnnotationOverrides)null);
   }

   public ServiceRegistry getServiceRegistry() {
      return this.getMetadataImplementor().getServiceRegistry();
   }

   public NamingStrategy getNamingStrategy() {
      return this.metadata.getNamingStrategy();
   }

   public MappingDefaults getMappingDefaults() {
      return this.metadata.getMappingDefaults();
   }

   public MetadataImplementor getMetadataImplementor() {
      return this.metadata;
   }

   public Class locateClassByName(String name) {
      return ((ClassLoaderService)this.classLoaderService.getValue()).classForName(name);
   }

   public Type makeJavaType(String className) {
      Type javaType = (Type)this.nameToJavaTypeMap.get(className);
      if (javaType == null) {
         javaType = this.metadata.makeJavaType(className);
         this.nameToJavaTypeMap.put(className, javaType);
      }

      return javaType;
   }

   public ValueHolder makeClassReference(String className) {
      return new ValueHolder(this.locateClassByName(className));
   }

   public String qualifyClassName(String name) {
      return name;
   }

   public boolean isGloballyQuotedIdentifiers() {
      return this.metadata.isGloballyQuotedIdentifiers();
   }
}
