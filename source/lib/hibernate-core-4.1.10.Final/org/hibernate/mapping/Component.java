package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.CompositeNestedGeneratedValueGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.property.Setter;
import org.hibernate.tuple.component.ComponentMetamodel;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;

public class Component extends SimpleValue implements MetaAttributable {
   private ArrayList properties = new ArrayList();
   private String componentClassName;
   private boolean embedded;
   private String parentProperty;
   private PersistentClass owner;
   private boolean dynamic;
   private java.util.Map metaAttributes;
   private String nodeName;
   private boolean isKey;
   private String roleName;
   private java.util.Map tuplizerImpls;
   private IdentifierGenerator builtIdentifierGenerator;

   public Component(Mappings mappings, PersistentClass owner) throws MappingException {
      super(mappings, owner.getTable());
      this.owner = owner;
   }

   public Component(Mappings mappings, Component component) throws MappingException {
      super(mappings, component.getTable());
      this.owner = component.getOwner();
   }

   public Component(Mappings mappings, Join join) throws MappingException {
      super(mappings, join.getTable());
      this.owner = join.getPersistentClass();
   }

   public Component(Mappings mappings, Collection collection) throws MappingException {
      super(mappings, collection.getCollectionTable());
      this.owner = collection.getOwner();
   }

   public int getPropertySpan() {
      return this.properties.size();
   }

   public Iterator getPropertyIterator() {
      return this.properties.iterator();
   }

   public void addProperty(Property p) {
      this.properties.add(p);
   }

   public void addColumn(Column column) {
      throw new UnsupportedOperationException("Cant add a column to a component");
   }

   public int getColumnSpan() {
      int n = 0;

      Property p;
      for(Iterator iter = this.getPropertyIterator(); iter.hasNext(); n += p.getColumnSpan()) {
         p = (Property)iter.next();
      }

      return n;
   }

   public Iterator getColumnIterator() {
      Iterator[] iters = new Iterator[this.getPropertySpan()];
      Iterator iter = this.getPropertyIterator();

      for(int i = 0; iter.hasNext(); iters[i++] = ((Property)iter.next()).getColumnIterator()) {
      }

      return new JoinedIterator(iters);
   }

   public void setTypeByReflection(String propertyClass, String propertyName) {
   }

   public boolean isEmbedded() {
      return this.embedded;
   }

   public String getComponentClassName() {
      return this.componentClassName;
   }

   public Class getComponentClass() throws MappingException {
      try {
         return ReflectHelper.classForName(this.componentClassName);
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("component class not found: " + this.componentClassName, cnfe);
      }
   }

   public PersistentClass getOwner() {
      return this.owner;
   }

   public String getParentProperty() {
      return this.parentProperty;
   }

   public void setComponentClassName(String componentClass) {
      this.componentClassName = componentClass;
   }

   public void setEmbedded(boolean embedded) {
      this.embedded = embedded;
   }

   public void setOwner(PersistentClass owner) {
      this.owner = owner;
   }

   public void setParentProperty(String parentProperty) {
      this.parentProperty = parentProperty;
   }

   public boolean isDynamic() {
      return this.dynamic;
   }

   public void setDynamic(boolean dynamic) {
      this.dynamic = dynamic;
   }

   public Type getType() throws MappingException {
      ComponentMetamodel metamodel = new ComponentMetamodel(this);
      TypeFactory factory = this.getMappings().getTypeResolver().getTypeFactory();
      return (Type)(this.isEmbedded() ? factory.embeddedComponent(metamodel) : factory.component(metamodel));
   }

   public void setTypeUsingReflection(String className, String propertyName) throws MappingException {
   }

   public java.util.Map getMetaAttributes() {
      return this.metaAttributes;
   }

   public MetaAttribute getMetaAttribute(String attributeName) {
      return this.metaAttributes == null ? null : (MetaAttribute)this.metaAttributes.get(attributeName);
   }

   public void setMetaAttributes(java.util.Map metas) {
      this.metaAttributes = metas;
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public boolean[] getColumnInsertability() {
      boolean[] result = new boolean[this.getColumnSpan()];
      Iterator iter = this.getPropertyIterator();

      boolean[] chunk;
      for(int i = 0; iter.hasNext(); i += chunk.length) {
         Property prop = (Property)iter.next();
         chunk = prop.getValue().getColumnInsertability();
         if (prop.isInsertable()) {
            System.arraycopy(chunk, 0, result, i, chunk.length);
         }
      }

      return result;
   }

   public boolean[] getColumnUpdateability() {
      boolean[] result = new boolean[this.getColumnSpan()];
      Iterator iter = this.getPropertyIterator();

      boolean[] chunk;
      for(int i = 0; iter.hasNext(); i += chunk.length) {
         Property prop = (Property)iter.next();
         chunk = prop.getValue().getColumnUpdateability();
         if (prop.isUpdateable()) {
            System.arraycopy(chunk, 0, result, i, chunk.length);
         }
      }

      return result;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
   }

   public boolean isKey() {
      return this.isKey;
   }

   public void setKey(boolean isKey) {
      this.isKey = isKey;
   }

   public boolean hasPojoRepresentation() {
      return this.componentClassName != null;
   }

   public void addTuplizer(EntityMode entityMode, String implClassName) {
      if (this.tuplizerImpls == null) {
         this.tuplizerImpls = new HashMap();
      }

      this.tuplizerImpls.put(entityMode, implClassName);
   }

   public String getTuplizerImplClassName(EntityMode mode) {
      return this.tuplizerImpls == null ? null : (String)this.tuplizerImpls.get(mode);
   }

   public java.util.Map getTuplizerMap() {
      return this.tuplizerImpls == null ? null : Collections.unmodifiableMap(this.tuplizerImpls);
   }

   public Property getProperty(String propertyName) throws MappingException {
      Iterator iter = this.getPropertyIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         if (prop.getName().equals(propertyName)) {
            return prop;
         }
      }

      throw new MappingException("component property not found: " + propertyName);
   }

   public String getRoleName() {
      return this.roleName;
   }

   public void setRoleName(String roleName) {
      this.roleName = roleName;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.properties.toString() + ')';
   }

   public IdentifierGenerator createIdentifierGenerator(IdentifierGeneratorFactory identifierGeneratorFactory, Dialect dialect, String defaultCatalog, String defaultSchema, RootClass rootClass) throws MappingException {
      if (this.builtIdentifierGenerator == null) {
         this.builtIdentifierGenerator = this.buildIdentifierGenerator(identifierGeneratorFactory, dialect, defaultCatalog, defaultSchema, rootClass);
      }

      return this.builtIdentifierGenerator;
   }

   private IdentifierGenerator buildIdentifierGenerator(IdentifierGeneratorFactory identifierGeneratorFactory, Dialect dialect, String defaultCatalog, String defaultSchema, RootClass rootClass) throws MappingException {
      boolean hasCustomGenerator = !"assigned".equals(this.getIdentifierGeneratorStrategy());
      if (hasCustomGenerator) {
         return super.createIdentifierGenerator(identifierGeneratorFactory, dialect, defaultCatalog, defaultSchema, rootClass);
      } else {
         Class entityClass = rootClass.getMappedClass();
         Class attributeDeclarer;
         if (rootClass.getIdentifierMapper() != null) {
            attributeDeclarer = this.resolveComponentClass();
         } else if (rootClass.getIdentifierProperty() != null) {
            attributeDeclarer = this.resolveComponentClass();
         } else {
            attributeDeclarer = entityClass;
         }

         CompositeNestedGeneratedValueGenerator.GenerationContextLocator locator = new StandardGenerationContextLocator(rootClass.getEntityName());
         CompositeNestedGeneratedValueGenerator generator = new CompositeNestedGeneratedValueGenerator(locator);
         Iterator itr = this.getPropertyIterator();

         while(itr.hasNext()) {
            Property property = (Property)itr.next();
            if (property.getValue().isSimpleValue()) {
               SimpleValue value = (SimpleValue)property.getValue();
               if (!"assigned".equals(value.getIdentifierGeneratorStrategy())) {
                  IdentifierGenerator valueGenerator = value.createIdentifierGenerator(identifierGeneratorFactory, dialect, defaultCatalog, defaultSchema, rootClass);
                  generator.addGeneratedValuePlan(new ValueGenerationPlan(property.getName(), valueGenerator, this.injector(property, attributeDeclarer)));
               }
            }
         }

         return generator;
      }
   }

   private Setter injector(Property property, Class attributeDeclarer) {
      return property.getPropertyAccessor(attributeDeclarer).getSetter(attributeDeclarer, property.getName());
   }

   private Class resolveComponentClass() {
      try {
         return this.getComponentClass();
      } catch (Exception var2) {
         return null;
      }
   }

   public static class StandardGenerationContextLocator implements CompositeNestedGeneratedValueGenerator.GenerationContextLocator {
      private final String entityName;

      public StandardGenerationContextLocator(String entityName) {
         this.entityName = entityName;
      }

      public Serializable locateGenerationContext(SessionImplementor session, Object incomingObject) {
         return session.getEntityPersister(this.entityName, incomingObject).getIdentifier(incomingObject, session);
      }
   }

   public static class ValueGenerationPlan implements CompositeNestedGeneratedValueGenerator.GenerationPlan {
      private final String propertyName;
      private final IdentifierGenerator subGenerator;
      private final Setter injector;

      public ValueGenerationPlan(String propertyName, IdentifierGenerator subGenerator, Setter injector) {
         this.propertyName = propertyName;
         this.subGenerator = subGenerator;
         this.injector = injector;
      }

      public void execute(SessionImplementor session, Object incomingObject, Object injectionContext) {
         Object generatedValue = this.subGenerator.generate(session, incomingObject);
         this.injector.set(injectionContext, generatedValue, session.getFactory());
      }

      public void registerPersistentGenerators(java.util.Map generatorMap) {
         if (PersistentIdentifierGenerator.class.isInstance(this.subGenerator)) {
            generatorMap.put(((PersistentIdentifierGenerator)this.subGenerator).generatorKey(), this.subGenerator);
         }

      }
   }
}
