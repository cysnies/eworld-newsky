package org.hibernate.type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;

public class SpecialOneToOneType extends OneToOneType {
   public SpecialOneToOneType(TypeFactory.TypeScope scope, String referencedEntityName, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, String entityName, String propertyName) {
      super(scope, referencedEntityName, foreignKeyType, uniqueKeyPropertyName, lazy, unwrapProxy, true, entityName, propertyName);
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      return super.getIdentifierOrUniqueKeyType(mapping).getColumnSpan(mapping);
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      return super.getIdentifierOrUniqueKeyType(mapping).sqlTypes(mapping);
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return super.getIdentifierOrUniqueKeyType(mapping).dictatedSizes(mapping);
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return super.getIdentifierOrUniqueKeyType(mapping).defaultSizes(mapping);
   }

   public boolean useLHSPrimaryKey() {
      return false;
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return super.getIdentifierOrUniqueKeyType(session.getFactory()).nullSafeGet(rs, names, session, owner);
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
      Serializable id = (Serializable)this.getIdentifierType(session).assemble(oid, session, (Object)null);
      if (this.isNotEmbedded(session)) {
         return id;
      } else {
         return id == null ? null : this.resolveIdentifier(id, session);
      }
   }
}
