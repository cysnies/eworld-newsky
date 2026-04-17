package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.persister.entity.EntityPersister;

public class OneToOneType extends EntityType {
   private final ForeignKeyDirection foreignKeyType;
   private final String propertyName;
   private final String entityName;
   private static final Size[] SIZES = new Size[0];

   /** @deprecated */
   @Deprecated
   public OneToOneType(TypeFactory.TypeScope scope, String referencedEntityName, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean isEmbeddedInXML, String entityName, String propertyName) {
      super(scope, referencedEntityName, uniqueKeyPropertyName, !lazy, isEmbeddedInXML, unwrapProxy);
      this.foreignKeyType = foreignKeyType;
      this.propertyName = propertyName;
      this.entityName = entityName;
   }

   public OneToOneType(TypeFactory.TypeScope scope, String referencedEntityName, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, String entityName, String propertyName) {
      super(scope, referencedEntityName, uniqueKeyPropertyName, !lazy, unwrapProxy);
      this.foreignKeyType = foreignKeyType;
      this.propertyName = propertyName;
      this.entityName = entityName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public boolean isNull(Object owner, SessionImplementor session) {
      if (this.propertyName != null) {
         EntityPersister ownerPersister = session.getFactory().getEntityPersister(this.entityName);
         Serializable id = session.getContextEntityIdentifier(owner);
         EntityKey entityKey = session.generateEntityKey(id, ownerPersister);
         return session.getPersistenceContext().isPropertyNull(entityKey, this.getPropertyName());
      } else {
         return false;
      }
   }

   public int getColumnSpan(Mapping session) throws MappingException {
      return 0;
   }

   public int[] sqlTypes(Mapping session) throws MappingException {
      return ArrayHelper.EMPTY_INT_ARRAY;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return SIZES;
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return SIZES;
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return ArrayHelper.EMPTY_BOOLEAN_ARRAY;
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) {
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) {
   }

   public boolean isOneToOne() {
      return true;
   }

   public boolean isDirty(Object old, Object current, SessionImplementor session) {
      return false;
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) {
      return false;
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) {
      return false;
   }

   public ForeignKeyDirection getForeignKeyDirection() {
      return this.foreignKeyType;
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return session.getContextEntityIdentifier(owner);
   }

   protected boolean isNullable() {
      return this.foreignKeyType == ForeignKeyDirection.FOREIGN_KEY_TO_PARENT;
   }

   public boolean useLHSPrimaryKey() {
      return true;
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return null;
   }

   public Object assemble(Serializable oid, SessionImplementor session, Object owner) throws HibernateException {
      return this.resolve(session.getContextEntityIdentifier(owner), session, owner);
   }

   public boolean isAlwaysDirtyChecked() {
      return false;
   }
}
