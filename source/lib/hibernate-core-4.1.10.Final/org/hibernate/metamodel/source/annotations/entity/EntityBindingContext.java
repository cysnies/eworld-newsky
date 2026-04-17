package org.hibernate.metamodel.source.annotations.entity;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.SourceType;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.domain.Type;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingDefaults;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.service.ServiceRegistry;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;

public class EntityBindingContext implements LocalBindingContext, AnnotationBindingContext {
   private final AnnotationBindingContext contextDelegate;
   private final Origin origin;

   public EntityBindingContext(AnnotationBindingContext contextDelegate, ConfiguredClass source) {
      this.contextDelegate = contextDelegate;
      this.origin = new Origin(SourceType.ANNOTATION, source.getName());
   }

   public Origin getOrigin() {
      return this.origin;
   }

   public ServiceRegistry getServiceRegistry() {
      return this.contextDelegate.getServiceRegistry();
   }

   public NamingStrategy getNamingStrategy() {
      return this.contextDelegate.getNamingStrategy();
   }

   public MappingDefaults getMappingDefaults() {
      return this.contextDelegate.getMappingDefaults();
   }

   public MetadataImplementor getMetadataImplementor() {
      return this.contextDelegate.getMetadataImplementor();
   }

   public Class locateClassByName(String name) {
      return this.contextDelegate.locateClassByName(name);
   }

   public Type makeJavaType(String className) {
      return this.contextDelegate.makeJavaType(className);
   }

   public boolean isGloballyQuotedIdentifiers() {
      return this.contextDelegate.isGloballyQuotedIdentifiers();
   }

   public ValueHolder makeClassReference(String className) {
      return this.contextDelegate.makeClassReference(className);
   }

   public String qualifyClassName(String name) {
      return this.contextDelegate.qualifyClassName(name);
   }

   public Index getIndex() {
      return this.contextDelegate.getIndex();
   }

   public ClassInfo getClassInfo(String name) {
      return this.contextDelegate.getClassInfo(name);
   }

   public void resolveAllTypes(String className) {
      this.contextDelegate.resolveAllTypes(className);
   }

   public ResolvedType getResolvedType(Class clazz) {
      return this.contextDelegate.getResolvedType(clazz);
   }

   public ResolvedTypeWithMembers resolveMemberTypes(ResolvedType type) {
      return this.contextDelegate.resolveMemberTypes(type);
   }
}
