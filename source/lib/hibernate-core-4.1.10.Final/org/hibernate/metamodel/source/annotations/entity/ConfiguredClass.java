package org.hibernate.metamodel.source.annotations.entity;

import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.members.HierarchicType;
import com.fasterxml.classmate.members.ResolvedMember;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.AccessType;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.ReflectionHelper;
import org.hibernate.metamodel.source.annotations.attribute.AssociationAttribute;
import org.hibernate.metamodel.source.annotations.attribute.AttributeNature;
import org.hibernate.metamodel.source.annotations.attribute.AttributeOverride;
import org.hibernate.metamodel.source.annotations.attribute.BasicAttribute;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

public class ConfiguredClass {
   public static final Logger LOG = Logger.getLogger(ConfiguredClass.class.getName());
   private final ConfiguredClass parent;
   private final ClassInfo classInfo;
   private final Class clazz;
   private final AccessType classAccessType;
   private final ConfiguredClassType configuredClassType;
   private final Map idAttributeMap;
   private final Map associationAttributeMap;
   private final Map simpleAttributeMap;
   private BasicAttribute versionAttribute;
   private final Map embeddedClasses = new HashMap();
   private final Map attributeOverrideMap;
   private final Set transientFieldNames = new HashSet();
   private final Set transientMethodNames = new HashSet();
   private final String customTuplizer;
   private final EntityBindingContext localBindingContext;

   public ConfiguredClass(ClassInfo classInfo, AccessType defaultAccessType, ConfiguredClass parent, AnnotationBindingContext context) {
      this.parent = parent;
      this.classInfo = classInfo;
      this.clazz = context.locateClassByName(classInfo.toString());
      this.configuredClassType = this.determineType();
      this.classAccessType = this.determineClassAccessType(defaultAccessType);
      this.customTuplizer = this.determineCustomTuplizer();
      this.simpleAttributeMap = new TreeMap();
      this.idAttributeMap = new TreeMap();
      this.associationAttributeMap = new TreeMap();
      this.localBindingContext = new EntityBindingContext(context, this);
      this.collectAttributes();
      this.attributeOverrideMap = Collections.unmodifiableMap(this.findAttributeOverrides());
   }

   public String getName() {
      return this.clazz.getName();
   }

   public Class getConfiguredClass() {
      return this.clazz;
   }

   public ClassInfo getClassInfo() {
      return this.classInfo;
   }

   public ConfiguredClass getParent() {
      return this.parent;
   }

   public EntityBindingContext getLocalBindingContext() {
      return this.localBindingContext;
   }

   public Iterable getSimpleAttributes() {
      return this.simpleAttributeMap.values();
   }

   public Iterable getIdAttributes() {
      return this.idAttributeMap.values();
   }

   public BasicAttribute getVersionAttribute() {
      return this.versionAttribute;
   }

   public Iterable getAssociationAttributes() {
      return this.associationAttributeMap.values();
   }

   public Map getEmbeddedClasses() {
      return this.embeddedClasses;
   }

   public Map getAttributeOverrideMap() {
      return this.attributeOverrideMap;
   }

   public AccessType getClassAccessType() {
      return this.classAccessType;
   }

   public String getCustomTuplizer() {
      return this.customTuplizer;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("ConfiguredClass");
      sb.append("{clazz=").append(this.clazz.getSimpleName());
      sb.append('}');
      return sb.toString();
   }

   private ConfiguredClassType determineType() {
      if (this.classInfo.annotations().containsKey(JPADotNames.ENTITY)) {
         return ConfiguredClassType.ENTITY;
      } else if (this.classInfo.annotations().containsKey(JPADotNames.MAPPED_SUPERCLASS)) {
         return ConfiguredClassType.MAPPED_SUPERCLASS;
      } else {
         return this.classInfo.annotations().containsKey(JPADotNames.EMBEDDABLE) ? ConfiguredClassType.EMBEDDABLE : ConfiguredClassType.NON_ENTITY;
      }
   }

   private AccessType determineClassAccessType(AccessType defaultAccessType) {
      AccessType accessType = defaultAccessType;
      AnnotationInstance accessAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, JPADotNames.ACCESS);
      if (accessAnnotation != null && accessAnnotation.target().getClass().equals(ClassInfo.class)) {
         accessType = (AccessType)JandexHelper.getEnumValue(accessAnnotation, "value", AccessType.class);
      }

      return accessType;
   }

   private void collectAttributes() {
      this.findTransientFieldAndMethodNames();
      ResolvedTypeWithMembers resolvedType = this.localBindingContext.resolveMemberTypes(this.localBindingContext.getResolvedType(this.clazz));

      for(HierarchicType hierarchicType : resolvedType.allTypesAndOverrides()) {
         if (hierarchicType.getType().getErasedType().equals(this.clazz)) {
            resolvedType = this.localBindingContext.resolveMemberTypes(hierarchicType.getType());
            break;
         }
      }

      if (resolvedType == null) {
         throw new AssertionFailure("Unable to resolve types for " + this.clazz.getName());
      } else {
         Set<String> explicitlyConfiguredMemberNames = this.createExplicitlyConfiguredAccessProperties(resolvedType);
         if (AccessType.FIELD.equals(this.classAccessType)) {
            Field[] fields = this.clazz.getDeclaredFields();
            Field.setAccessible(fields, true);

            for(Field field : fields) {
               if (this.isPersistentMember(this.transientFieldNames, explicitlyConfiguredMemberNames, field)) {
                  this.createMappedAttribute(field, resolvedType, AccessType.FIELD);
               }
            }
         } else {
            Method[] methods = this.clazz.getDeclaredMethods();
            Method.setAccessible(methods, true);

            for(Method method : methods) {
               if (this.isPersistentMember(this.transientMethodNames, explicitlyConfiguredMemberNames, method)) {
                  this.createMappedAttribute(method, resolvedType, AccessType.PROPERTY);
               }
            }
         }

      }
   }

   private boolean isPersistentMember(Set transientNames, Set explicitlyConfiguredMemberNames, Member member) {
      if (!ReflectionHelper.isProperty(member)) {
         return false;
      } else if (transientNames.contains(member.getName())) {
         return false;
      } else {
         return !explicitlyConfiguredMemberNames.contains(ReflectionHelper.getPropertyName(member));
      }
   }

   private Set createExplicitlyConfiguredAccessProperties(ResolvedTypeWithMembers resolvedMembers) {
      Set<String> explicitAccessPropertyNames = new HashSet();
      List<AnnotationInstance> accessAnnotations = (List)this.classInfo.annotations().get(JPADotNames.ACCESS);
      if (accessAnnotations == null) {
         return explicitAccessPropertyNames;
      } else {
         for(AnnotationInstance accessAnnotation : accessAnnotations) {
            AnnotationTarget annotationTarget = accessAnnotation.target();
            if (annotationTarget.getClass().equals(MethodInfo.class) || annotationTarget.getClass().equals(FieldInfo.class)) {
               AccessType accessType = (AccessType)JandexHelper.getEnumValue(accessAnnotation, "value", AccessType.class);
               if (this.isExplicitAttributeAccessAnnotationPlacedCorrectly(annotationTarget, accessType)) {
                  Member member;
                  if (annotationTarget instanceof MethodInfo) {
                     Method m;
                     try {
                        m = this.clazz.getMethod(((MethodInfo)annotationTarget).name());
                     } catch (NoSuchMethodException var12) {
                        throw new HibernateException("Unable to load method " + ((MethodInfo)annotationTarget).name() + " of class " + this.clazz.getName());
                     }

                     member = m;
                     accessType = AccessType.PROPERTY;
                  } else {
                     Field f;
                     try {
                        f = this.clazz.getField(((FieldInfo)annotationTarget).name());
                     } catch (NoSuchFieldException var11) {
                        throw new HibernateException("Unable to load field " + ((FieldInfo)annotationTarget).name() + " of class " + this.clazz.getName());
                     }

                     member = f;
                     accessType = AccessType.FIELD;
                  }

                  if (ReflectionHelper.isProperty(member)) {
                     this.createMappedAttribute(member, resolvedMembers, accessType);
                     explicitAccessPropertyNames.add(ReflectionHelper.getPropertyName(member));
                  }
               }
            }
         }

         return explicitAccessPropertyNames;
      }
   }

   private boolean isExplicitAttributeAccessAnnotationPlacedCorrectly(AnnotationTarget annotationTarget, AccessType accessType) {
      if (AccessType.FIELD.equals(this.classAccessType)) {
         if (!(annotationTarget instanceof MethodInfo)) {
            LOG.tracef("The access type of class %s is AccessType.FIELD. To override the access for an attribute @Access has to be placed on the property (getter)", this.classInfo.name().toString());
            return false;
         }

         if (!AccessType.PROPERTY.equals(accessType)) {
            LOG.tracef("The access type of class %s is AccessType.FIELD. To override the access for an attribute @Access has to be placed on the property (getter) with an access type of AccessType.PROPERTY. Using AccessType.FIELD on the property has no effect", this.classInfo.name().toString());
            return false;
         }
      }

      if (AccessType.PROPERTY.equals(this.classAccessType)) {
         if (!(annotationTarget instanceof FieldInfo)) {
            LOG.tracef("The access type of class %s is AccessType.PROPERTY. To override the access for a field @Access has to be placed on the field ", this.classInfo.name().toString());
            return false;
         }

         if (!AccessType.FIELD.equals(accessType)) {
            LOG.tracef("The access type of class %s is AccessType.PROPERTY. To override the access for a field @Access has to be placed on the field with an access type of AccessType.FIELD. Using AccessType.PROPERTY on the field has no effect", this.classInfo.name().toString());
            return false;
         }
      }

      return true;
   }

   private void createMappedAttribute(Member member, ResolvedTypeWithMembers resolvedType, AccessType accessType) {
      String attributeName = ReflectionHelper.getPropertyName(member);
      ResolvedMember[] resolvedMembers;
      if (member instanceof Field) {
         resolvedMembers = resolvedType.getMemberFields();
      } else {
         resolvedMembers = resolvedType.getMemberMethods();
      }

      Class<?> attributeType = (Class)this.findResolvedType(member.getName(), resolvedMembers);
      Map<DotName, List<AnnotationInstance>> annotations = JandexHelper.getMemberAnnotations(this.classInfo, member.getName());
      AttributeNature attributeNature = this.determineAttributeNature(annotations);
      String accessTypeString = accessType.toString().toLowerCase();
      switch (attributeNature) {
         case BASIC:
            BasicAttribute attribute = BasicAttribute.createSimpleAttribute(attributeName, attributeType, annotations, accessTypeString, this.getLocalBindingContext());
            if (attribute.isId()) {
               this.idAttributeMap.put(attributeName, attribute);
            } else if (attribute.isVersioned()) {
               if (this.versionAttribute != null) {
                  throw new MappingException("Multiple version attributes", this.localBindingContext.getOrigin());
               }

               this.versionAttribute = attribute;
            } else {
               this.simpleAttributeMap.put(attributeName, attribute);
            }
            break;
         case ELEMENT_COLLECTION:
            throw new NotYetImplementedException("Element collections must still be implemented.");
         case EMBEDDED_ID:
            throw new NotYetImplementedException("Embedded ids must still be implemented.");
         case EMBEDDED:
            AnnotationInstance targetAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.TARGET);
            if (targetAnnotation != null) {
               attributeType = this.localBindingContext.locateClassByName((String)JandexHelper.getValue(targetAnnotation, "value", String.class));
            }

            this.resolveEmbeddable(attributeName, attributeType);
            break;
         default:
            AssociationAttribute attribute = AssociationAttribute.createAssociationAttribute(attributeName, attributeType, attributeNature, accessTypeString, annotations, this.getLocalBindingContext());
            this.associationAttributeMap.put(attributeName, attribute);
      }

   }

   private void resolveEmbeddable(String attributeName, Class type) {
      ClassInfo embeddableClassInfo = this.localBindingContext.getClassInfo(type.getName());
      if (embeddableClassInfo == null) {
         String msg = String.format("Attribute '%s#%s' is annotated with @Embedded, but '%s' does not seem to be annotated with @Embeddable. Are all annotated classes added to the configuration?", this.getConfiguredClass().getSimpleName(), attributeName, type.getSimpleName());
         throw new AnnotationException(msg);
      } else {
         this.localBindingContext.resolveAllTypes(type.getName());
         EmbeddableHierarchy hierarchy = EmbeddableHierarchy.createEmbeddableHierarchy(this.localBindingContext.locateClassByName(embeddableClassInfo.toString()), attributeName, this.classAccessType, this.localBindingContext);
         this.embeddedClasses.put(attributeName, hierarchy.getLeaf());
      }
   }

   private AttributeNature determineAttributeNature(Map annotations) {
      EnumMap<AttributeNature, AnnotationInstance> discoveredAttributeTypes = new EnumMap(AttributeNature.class);
      AnnotationInstance oneToOne = JandexHelper.getSingleAnnotation(annotations, JPADotNames.ONE_TO_ONE);
      if (oneToOne != null) {
         discoveredAttributeTypes.put(AttributeNature.ONE_TO_ONE, oneToOne);
      }

      AnnotationInstance oneToMany = JandexHelper.getSingleAnnotation(annotations, JPADotNames.ONE_TO_MANY);
      if (oneToMany != null) {
         discoveredAttributeTypes.put(AttributeNature.ONE_TO_MANY, oneToMany);
      }

      AnnotationInstance manyToOne = JandexHelper.getSingleAnnotation(annotations, JPADotNames.MANY_TO_ONE);
      if (manyToOne != null) {
         discoveredAttributeTypes.put(AttributeNature.MANY_TO_ONE, manyToOne);
      }

      AnnotationInstance manyToMany = JandexHelper.getSingleAnnotation(annotations, JPADotNames.MANY_TO_MANY);
      if (manyToMany != null) {
         discoveredAttributeTypes.put(AttributeNature.MANY_TO_MANY, manyToMany);
      }

      AnnotationInstance embedded = JandexHelper.getSingleAnnotation(annotations, JPADotNames.EMBEDDED);
      if (embedded != null) {
         discoveredAttributeTypes.put(AttributeNature.EMBEDDED, embedded);
      }

      AnnotationInstance embeddedId = JandexHelper.getSingleAnnotation(annotations, JPADotNames.EMBEDDED_ID);
      if (embeddedId != null) {
         discoveredAttributeTypes.put(AttributeNature.EMBEDDED_ID, embeddedId);
      }

      AnnotationInstance elementCollection = JandexHelper.getSingleAnnotation(annotations, JPADotNames.ELEMENT_COLLECTION);
      if (elementCollection != null) {
         discoveredAttributeTypes.put(AttributeNature.ELEMENT_COLLECTION, elementCollection);
      }

      if (discoveredAttributeTypes.size() == 0) {
         return AttributeNature.BASIC;
      } else if (discoveredAttributeTypes.size() == 1) {
         return (AttributeNature)discoveredAttributeTypes.keySet().iterator().next();
      } else {
         throw new AnnotationException("More than one association type configured for property  " + this.getName() + " of class " + this.getName());
      }
   }

   private Type findResolvedType(String name, ResolvedMember[] resolvedMembers) {
      for(ResolvedMember resolvedMember : resolvedMembers) {
         if (resolvedMember.getName().equals(name)) {
            return resolvedMember.getType().getErasedType();
         }
      }

      throw new AssertionFailure(String.format("Unable to resolve type of attribute %s of class %s", name, this.classInfo.name().toString()));
   }

   private void findTransientFieldAndMethodNames() {
      List<AnnotationInstance> transientMembers = (List)this.classInfo.annotations().get(JPADotNames.TRANSIENT);
      if (transientMembers != null) {
         for(AnnotationInstance transientMember : transientMembers) {
            AnnotationTarget target = transientMember.target();
            if (target instanceof FieldInfo) {
               this.transientFieldNames.add(((FieldInfo)target).name());
            } else {
               this.transientMethodNames.add(((MethodInfo)target).name());
            }
         }

      }
   }

   private Map findAttributeOverrides() {
      Map<String, AttributeOverride> attributeOverrideList = new HashMap();
      AnnotationInstance attributeOverrideAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, JPADotNames.ATTRIBUTE_OVERRIDE);
      if (attributeOverrideAnnotation != null) {
         String prefix = this.createPathPrefix(attributeOverrideAnnotation.target());
         AttributeOverride override = new AttributeOverride(prefix, attributeOverrideAnnotation);
         attributeOverrideList.put(override.getAttributePath(), override);
      }

      AnnotationInstance attributeOverridesAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, JPADotNames.ATTRIBUTE_OVERRIDES);
      if (attributeOverridesAnnotation != null) {
         AnnotationInstance[] annotationInstances = attributeOverridesAnnotation.value().asNestedArray();

         for(AnnotationInstance annotationInstance : annotationInstances) {
            String prefix = this.createPathPrefix(attributeOverridesAnnotation.target());
            AttributeOverride override = new AttributeOverride(prefix, annotationInstance);
            attributeOverrideList.put(override.getAttributePath(), override);
         }
      }

      return attributeOverrideList;
   }

   private String createPathPrefix(AnnotationTarget target) {
      String prefix = null;
      if (target instanceof FieldInfo || target instanceof MethodInfo) {
         prefix = JandexHelper.getPropertyName(target);
      }

      return prefix;
   }

   private List findAssociationOverrides() {
      List<AnnotationInstance> associationOverrideList = new ArrayList();
      AnnotationInstance associationOverrideAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, JPADotNames.ASSOCIATION_OVERRIDE);
      if (associationOverrideAnnotation != null) {
         associationOverrideList.add(associationOverrideAnnotation);
      }

      AnnotationInstance associationOverridesAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, JPADotNames.ASSOCIATION_OVERRIDES);
      if (associationOverrideAnnotation != null) {
         AnnotationInstance[] attributeOverride = associationOverridesAnnotation.value().asNestedArray();
         Collections.addAll(associationOverrideList, attributeOverride);
      }

      return associationOverrideList;
   }

   private String determineCustomTuplizer() {
      AnnotationInstance tuplizersAnnotation = JandexHelper.getSingleAnnotation(this.classInfo, HibernateDotNames.TUPLIZERS);
      if (tuplizersAnnotation == null) {
         return null;
      } else {
         AnnotationInstance[] annotations = (AnnotationInstance[])JandexHelper.getValue(tuplizersAnnotation, "value", AnnotationInstance[].class);
         AnnotationInstance pojoTuplizerAnnotation = null;

         for(AnnotationInstance tuplizerAnnotation : annotations) {
            if (EntityMode.valueOf(tuplizerAnnotation.value("entityModeType").asEnum()) == EntityMode.POJO) {
               pojoTuplizerAnnotation = tuplizerAnnotation;
               break;
            }
         }

         String customTuplizer = null;
         if (pojoTuplizerAnnotation != null) {
            customTuplizer = pojoTuplizerAnnotation.value("impl").asString();
         }

         return customTuplizer;
      }
   }
}
