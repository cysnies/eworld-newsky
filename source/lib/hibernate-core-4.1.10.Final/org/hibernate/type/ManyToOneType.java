package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.persister.entity.EntityPersister;

public class ManyToOneType extends EntityType {
   private final boolean ignoreNotFound;
   private boolean isLogicalOneToOne;

   public ManyToOneType(TypeFactory.TypeScope scope, String referencedEntityName) {
      this(scope, referencedEntityName, false);
   }

   public ManyToOneType(TypeFactory.TypeScope scope, String referencedEntityName, boolean lazy) {
      this(scope, referencedEntityName, (String)null, lazy, true, false, false, false);
   }

   /** @deprecated */
   @Deprecated
   public ManyToOneType(TypeFactory.TypeScope scope, String referencedEntityName, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean isEmbeddedInXML, boolean ignoreNotFound, boolean isLogicalOneToOne) {
      super(scope, referencedEntityName, uniqueKeyPropertyName, !lazy, isEmbeddedInXML, unwrapProxy);
      this.ignoreNotFound = ignoreNotFound;
      this.isLogicalOneToOne = isLogicalOneToOne;
   }

   public ManyToOneType(TypeFactory.TypeScope scope, String referencedEntityName, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean ignoreNotFound, boolean isLogicalOneToOne) {
      super(scope, referencedEntityName, uniqueKeyPropertyName, !lazy, unwrapProxy);
      this.ignoreNotFound = ignoreNotFound;
      this.isLogicalOneToOne = isLogicalOneToOne;
   }

   protected boolean isNullable() {
      return this.ignoreNotFound;
   }

   public boolean isAlwaysDirtyChecked() {
      return true;
   }

   public boolean isOneToOne() {
      return false;
   }

   public boolean isLogicalOneToOne() {
      return this.isLogicalOneToOne;
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      return this.getIdentifierOrUniqueKeyType(mapping).getColumnSpan(mapping);
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      return this.getIdentifierOrUniqueKeyType(mapping).sqlTypes(mapping);
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return this.getIdentifierOrUniqueKeyType(mapping).dictatedSizes(mapping);
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return this.getIdentifierOrUniqueKeyType(mapping).defaultSizes(mapping);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      this.getIdentifierOrUniqueKeyType(session.getFactory()).nullSafeSet(st, this.getIdentifier(value, session), index, settable, session);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.getIdentifierOrUniqueKeyType(session.getFactory()).nullSafeSet(st, this.getIdentifier(value, session), index, session);
   }

   public ForeignKeyDirection getForeignKeyDirection() {
      return ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT;
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      Serializable id = (Serializable)this.getIdentifierOrUniqueKeyType(session.getFactory()).nullSafeGet(rs, (String[])names, session, (Object)null);
      this.scheduleBatchLoadIfNeeded(id, session);
      return id;
   }

   private void scheduleBatchLoadIfNeeded(Serializable id, SessionImplementor session) throws MappingException {
      if (this.uniqueKeyPropertyName == null && id != null) {
         EntityPersister persister = session.getFactory().getEntityPersister(this.getAssociatedEntityName());
         EntityKey entityKey = session.generateEntityKey(id, persister);
         if (entityKey.isBatchLoadable() && !session.getPersistenceContext().containsEntity(entityKey)) {
            session.getPersistenceContext().getBatchFetchQueue().addBatchLoadableEntityKey(entityKey);
         }
      }

   }

   public boolean useLHSPrimaryKey() {
      return false;
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      if (current == null) {
         return old != null;
      } else {
         return old == null ? true : this.getIdentifierOrUniqueKeyType(session.getFactory()).isDirty(old, this.getIdentifier(current, session), session);
      }
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      if (this.isNotEmbedded(session)) {
         return this.getIdentifierType(session).disassemble(value, session, owner);
      } else if (value == null) {
         return null;
      } else {
         Object id = ForeignKeys.getEntityIdentifierIfNotUnsaved(this.getAssociatedEntityName(), value, session);
         if (id == null) {
            throw new AssertionFailure("cannot cache a reference to an object with a null id: " + this.getAssociatedEntityName());
         } else {
            return this.getIdentifierType(session).disassemble(id, session, owner);
         }
      }
   }

   public Object assemble(Serializable oid, SessionImplementor session, Object owner) throws HibernateException {
      Serializable id = this.assembleId(oid, session);
      if (this.isNotEmbedded(session)) {
         return id;
      } else {
         return id == null ? null : this.resolveIdentifier(id, session);
      }
   }

   private Serializable assembleId(Serializable oid, SessionImplementor session) {
      return (Serializable)this.getIdentifierType(session).assemble(oid, session, (Object)null);
   }

   public void beforeAssemble(Serializable oid, SessionImplementor session) {
      this.scheduleBatchLoadIfNeeded(this.assembleId(oid, session), session);
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      boolean[] result = new boolean[this.getColumnSpan(mapping)];
      if (value != null) {
         Arrays.fill(result, true);
      }

      return result;
   }

   public boolean isDirty(Object old, Object current, SessionImplementor session) throws HibernateException {
      if (this.isSame(old, current)) {
         return false;
      } else {
         Object oldid = this.getIdentifier(old, session);
         Object newid = this.getIdentifier(current, session);
         return this.getIdentifierType(session).isDirty(oldid, newid, session);
      }
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      if (this.isAlwaysDirtyChecked()) {
         return this.isDirty(old, current, session);
      } else if (this.isSame(old, current)) {
         return false;
      } else {
         Object oldid = this.getIdentifier(old, session);
         Object newid = this.getIdentifier(current, session);
         return this.getIdentifierType(session).isDirty(oldid, newid, checkable, session);
      }
   }
}
