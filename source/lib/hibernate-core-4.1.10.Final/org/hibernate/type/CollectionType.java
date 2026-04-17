package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.jboss.logging.Logger;

public abstract class CollectionType extends AbstractType implements AssociationType {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CollectionType.class.getName());
   private static final Object NOT_NULL_COLLECTION = new MarkerObject("NOT NULL COLLECTION");
   public static final Object UNFETCHED_COLLECTION = new MarkerObject("UNFETCHED COLLECTION");
   private final TypeFactory.TypeScope typeScope;
   private final String role;
   private final String foreignKeyPropertyName;
   private final boolean isEmbeddedInXML;

   /** @deprecated */
   @Deprecated
   public CollectionType(TypeFactory.TypeScope typeScope, String role, String foreignKeyPropertyName, boolean isEmbeddedInXML) {
      this.typeScope = typeScope;
      this.role = role;
      this.foreignKeyPropertyName = foreignKeyPropertyName;
      this.isEmbeddedInXML = isEmbeddedInXML;
   }

   public CollectionType(TypeFactory.TypeScope typeScope, String role, String foreignKeyPropertyName) {
      this.typeScope = typeScope;
      this.role = role;
      this.foreignKeyPropertyName = foreignKeyPropertyName;
      this.isEmbeddedInXML = true;
   }

   public boolean isEmbeddedInXML() {
      return this.isEmbeddedInXML;
   }

   public String getRole() {
      return this.role;
   }

   public Object indexOf(Object collection, Object element) {
      throw new UnsupportedOperationException("generic collections don't have indexes");
   }

   public boolean contains(Object collection, Object childObject, SessionImplementor session) {
      Iterator elems = this.getElementsIterator(collection, session);

      while(elems.hasNext()) {
         Object element = elems.next();
         if (element instanceof HibernateProxy) {
            LazyInitializer li = ((HibernateProxy)element).getHibernateLazyInitializer();
            if (!li.isUninitialized()) {
               element = li.getImplementation();
            }
         }

         if (element == childObject) {
            return true;
         }
      }

      return false;
   }

   public boolean isCollectionType() {
      return true;
   }

   public final boolean isEqual(Object x, Object y) {
      return x == y || x instanceof PersistentCollection && ((PersistentCollection)x).isWrapper(y) || y instanceof PersistentCollection && ((PersistentCollection)y).isWrapper(x);
   }

   public int compare(Object x, Object y) {
      return 0;
   }

   public int getHashCode(Object x) {
      throw new UnsupportedOperationException("cannot doAfterTransactionCompletion lookups on collections");
   }

   public abstract PersistentCollection instantiate(SessionImplementor var1, CollectionPersister var2, Serializable var3);

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws SQLException {
      return this.nullSafeGet(rs, new String[]{name}, session, owner);
   }

   public Object nullSafeGet(ResultSet rs, String[] name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.resolve((Object)null, session, owner);
   }

   public final void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
   }

   public int[] sqlTypes(Mapping session) throws MappingException {
      return ArrayHelper.EMPTY_INT_ARRAY;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return new Size[]{LEGACY_DICTATED_SIZE};
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return new Size[]{LEGACY_DEFAULT_SIZE};
   }

   public int getColumnSpan(Mapping session) throws MappingException {
      return 0;
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return "null";
      } else {
         return !Hibernate.isInitialized(value) ? "<uninitialized>" : this.renderLoggableString(value, factory);
      }
   }

   protected String renderLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      List<String> list = new ArrayList();
      Type elemType = this.getElementType(factory);
      Iterator itr = this.getElementsIterator(value);

      while(itr.hasNext()) {
         list.add(elemType.toLoggableString(itr.next(), factory));
      }

      return list.toString();
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value;
   }

   public String getName() {
      return this.getReturnedClass().getName() + '(' + this.getRole() + ')';
   }

   public Iterator getElementsIterator(Object collection, SessionImplementor session) {
      return this.getElementsIterator(collection);
   }

   protected Iterator getElementsIterator(Object collection) {
      return ((Collection)collection).iterator();
   }

   public boolean isMutable() {
      return false;
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      Serializable key = this.getKeyOfOwner(owner, session);
      return key == null ? null : this.getPersister(session).getKeyType().disassemble(key, session, owner);
   }

   public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      if (cached == null) {
         return null;
      } else {
         Serializable key = (Serializable)this.getPersister(session).getKeyType().assemble(cached, session, owner);
         return this.resolveKey(key, session, owner);
      }
   }

   private boolean isOwnerVersioned(SessionImplementor session) throws MappingException {
      return this.getPersister(session).getOwnerEntityPersister().isVersioned();
   }

   private CollectionPersister getPersister(SessionImplementor session) {
      return session.getFactory().getCollectionPersister(this.role);
   }

   public boolean isDirty(Object old, Object current, SessionImplementor session) throws HibernateException {
      return this.isOwnerVersioned(session) && super.isDirty(old, current, session);
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return this.isDirty(old, current, session);
   }

   public abstract PersistentCollection wrap(SessionImplementor var1, Object var2);

   public boolean isAssociationType() {
      return true;
   }

   public ForeignKeyDirection getForeignKeyDirection() {
      return ForeignKeyDirection.FOREIGN_KEY_TO_PARENT;
   }

   public Serializable getKeyOfOwner(Object owner, SessionImplementor session) {
      EntityEntry entityEntry = session.getPersistenceContext().getEntry(owner);
      if (entityEntry == null) {
         return null;
      } else if (this.foreignKeyPropertyName == null) {
         return entityEntry.getId();
      } else {
         Object id;
         if (entityEntry.getLoadedState() != null) {
            id = entityEntry.getLoadedValue(this.foreignKeyPropertyName);
         } else {
            id = entityEntry.getPersister().getPropertyValue(owner, this.foreignKeyPropertyName);
         }

         Type keyType = this.getPersister(session).getKeyType();
         if (!keyType.getReturnedClass().isInstance(id)) {
            id = (Serializable)keyType.semiResolve(entityEntry.getLoadedValue(this.foreignKeyPropertyName), session, owner);
         }

         return (Serializable)id;
      }
   }

   public Serializable getIdOfOwnerOrNull(Serializable key, SessionImplementor session) {
      Serializable ownerId = null;
      if (this.foreignKeyPropertyName == null) {
         ownerId = key;
      } else {
         Type keyType = this.getPersister(session).getKeyType();
         EntityPersister ownerPersister = this.getPersister(session).getOwnerEntityPersister();
         Class ownerMappedClass = ownerPersister.getMappedClass();
         if (ownerMappedClass.isAssignableFrom(keyType.getReturnedClass()) && keyType.getReturnedClass().isInstance(key)) {
            ownerId = ownerPersister.getIdentifier(key, session);
         }
      }

      return ownerId;
   }

   public Object hydrate(ResultSet rs, String[] name, SessionImplementor session, Object owner) {
      return NOT_NULL_COLLECTION;
   }

   public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return this.resolveKey(this.getKeyOfOwner(owner, session), session, owner);
   }

   private Object resolveKey(Serializable key, SessionImplementor session, Object owner) {
      return key == null ? null : this.getCollection(key, session, owner);
   }

   public Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      throw new UnsupportedOperationException("collection mappings may not form part of a property-ref");
   }

   public boolean isArrayType() {
      return false;
   }

   public boolean useLHSPrimaryKey() {
      return this.foreignKeyPropertyName == null;
   }

   public String getRHSUniqueKeyPropertyName() {
      return null;
   }

   public Joinable getAssociatedJoinable(SessionFactoryImplementor factory) throws MappingException {
      return (Joinable)factory.getCollectionPersister(this.role);
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return false;
   }

   public String getAssociatedEntityName(SessionFactoryImplementor factory) throws MappingException {
      try {
         QueryableCollection collectionPersister = (QueryableCollection)factory.getCollectionPersister(this.role);
         if (!collectionPersister.getElementType().isEntityType()) {
            throw new MappingException("collection was not an association: " + collectionPersister.getRole());
         } else {
            return collectionPersister.getElementPersister().getEntityName();
         }
      } catch (ClassCastException var3) {
         throw new MappingException("collection role is not queryable " + this.role);
      }
   }

   public Object replaceElements(Object original, Object target, Object owner, Map copyCache, SessionImplementor session) {
      Collection result = (Collection)target;
      result.clear();
      Type elemType = this.getElementType(session.getFactory());
      Iterator iter = ((Collection)original).iterator();

      while(iter.hasNext()) {
         result.add(elemType.replace(iter.next(), (Object)null, session, owner, copyCache));
      }

      if (original instanceof PersistentCollection && result instanceof PersistentCollection) {
         PersistentCollection originalPersistentCollection = (PersistentCollection)original;
         PersistentCollection resultPersistentCollection = (PersistentCollection)result;
         this.preserveSnapshot(originalPersistentCollection, resultPersistentCollection, elemType, owner, copyCache, session);
         if (!originalPersistentCollection.isDirty()) {
            resultPersistentCollection.clearDirty();
         }
      }

      return result;
   }

   private void preserveSnapshot(PersistentCollection original, PersistentCollection result, Type elemType, Object owner, Map copyCache, SessionImplementor session) {
      Serializable originalSnapshot = original.getStoredSnapshot();
      Serializable resultSnapshot = result.getStoredSnapshot();
      Serializable targetSnapshot;
      if (originalSnapshot instanceof List) {
         targetSnapshot = new ArrayList(((List)originalSnapshot).size());

         for(Object obj : (List)originalSnapshot) {
            ((List)targetSnapshot).add(elemType.replace(obj, (Object)null, session, owner, copyCache));
         }
      } else if (originalSnapshot instanceof Map) {
         if (originalSnapshot instanceof SortedMap) {
            targetSnapshot = new TreeMap(((SortedMap)originalSnapshot).comparator());
         } else {
            targetSnapshot = new HashMap(CollectionHelper.determineProperSizing(((Map)originalSnapshot).size()), 0.75F);
         }

         for(Map.Entry entry : ((Map)originalSnapshot).entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object resultSnapshotValue = resultSnapshot == null ? null : ((Map)resultSnapshot).get(key);
            Object newValue = elemType.replace(value, resultSnapshotValue, session, owner, copyCache);
            if (key == value) {
               ((Map)targetSnapshot).put(newValue, newValue);
            } else {
               ((Map)targetSnapshot).put(key, newValue);
            }
         }
      } else if (originalSnapshot instanceof Object[]) {
         Object[] arr = (Object[])originalSnapshot;

         for(int i = 0; i < arr.length; ++i) {
            arr[i] = elemType.replace(arr[i], (Object)null, session, owner, copyCache);
         }

         targetSnapshot = originalSnapshot;
      } else {
         targetSnapshot = resultSnapshot;
      }

      CollectionEntry ce = session.getPersistenceContext().getCollectionEntry(result);
      if (ce != null) {
         ce.resetStoredSnapshot(result, targetSnapshot);
      }

   }

   protected Object instantiateResult(Object original) {
      return this.instantiate(-1);
   }

   public abstract Object instantiate(int var1);

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      if (original == null) {
         return null;
      } else if (!Hibernate.isInitialized(original)) {
         return target;
      } else {
         Object result = target != null && target != original ? target : this.instantiateResult(original);
         result = this.replaceElements(original, result, owner, copyCache, session);
         if (original == target) {
            boolean wasClean = PersistentCollection.class.isInstance(target) && !((PersistentCollection)target).isDirty();
            this.replaceElements(result, target, owner, copyCache, session);
            if (wasClean) {
               ((PersistentCollection)target).clearDirty();
            }

            result = target;
         }

         return result;
      }
   }

   public final Type getElementType(SessionFactoryImplementor factory) throws MappingException {
      return factory.getCollectionPersister(this.getRole()).getElementType();
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getRole() + ')';
   }

   public String getOnCondition(String alias, SessionFactoryImplementor factory, Map enabledFilters) throws MappingException {
      return this.getAssociatedJoinable(factory).filterFragment(alias, enabledFilters);
   }

   public Object getCollection(Serializable key, SessionImplementor session, Object owner) {
      CollectionPersister persister = this.getPersister(session);
      PersistenceContext persistenceContext = session.getPersistenceContext();
      EntityMode entityMode = persister.getOwnerEntityPersister().getEntityMode();
      PersistentCollection collection = persistenceContext.getLoadContexts().locateLoadingCollection(persister, key);
      if (collection == null) {
         collection = persistenceContext.useUnownedCollection(new CollectionKey(persister, key, entityMode));
         if (collection == null) {
            collection = this.instantiate(session, persister, key);
            collection.setOwner(owner);
            persistenceContext.addUninitializedCollection(persister, collection, key);
            if (this.initializeImmediately()) {
               session.initializeCollection(collection, false);
            } else if (!persister.isLazy()) {
               persistenceContext.addNonLazyCollection(collection);
            }

            if (this.hasHolder()) {
               session.getPersistenceContext().addCollectionHolder(collection);
            }
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracef("Created collection wrapper: %s", MessageHelper.collectionInfoString(persister, collection, key, session));
         }
      }

      collection.setOwner(owner);
      return collection.getValue();
   }

   public boolean hasHolder() {
      return false;
   }

   protected boolean initializeImmediately() {
      return false;
   }

   public String getLHSPropertyName() {
      return this.foreignKeyPropertyName;
   }

   public boolean isXMLElement() {
      return true;
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return xml;
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (!this.isEmbeddedInXML) {
         node.detach();
      } else {
         replaceNode(node, (Element)value);
      }

   }

   public boolean isAlwaysDirtyChecked() {
      return true;
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return ArrayHelper.EMPTY_BOOLEAN_ARRAY;
   }
}
