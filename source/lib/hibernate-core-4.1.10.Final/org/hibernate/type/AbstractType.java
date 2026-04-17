package org.hibernate.type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.metamodel.relational.Size;

public abstract class AbstractType implements Type {
   protected static final Size LEGACY_DICTATED_SIZE = new Size();
   protected static final Size LEGACY_DEFAULT_SIZE;

   public boolean isAssociationType() {
      return false;
   }

   public boolean isCollectionType() {
      return false;
   }

   public boolean isComponentType() {
      return false;
   }

   public boolean isEntityType() {
      return false;
   }

   public boolean isXMLElement() {
      return false;
   }

   public int compare(Object x, Object y) {
      return ((Comparable)x).compareTo(y);
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value == null ? null : (Serializable)this.deepCopy(value, session.getFactory());
   }

   public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      return cached == null ? null : this.deepCopy(cached, session.getFactory());
   }

   public boolean isDirty(Object old, Object current, SessionImplementor session) throws HibernateException {
      return !this.isSame(old, current);
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, names, session, owner);
   }

   public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value;
   }

   public Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value;
   }

   public boolean isAnyType() {
      return false;
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return this.isDirty(old, current, session);
   }

   public boolean isSame(Object x, Object y) throws HibernateException {
      return this.isEqual(x, y);
   }

   public boolean isEqual(Object x, Object y) {
      return EqualsHelper.equals(x, y);
   }

   public int getHashCode(Object x) {
      return x.hashCode();
   }

   public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) {
      return this.isEqual(x, y);
   }

   public int getHashCode(Object x, SessionFactoryImplementor factory) {
      return this.getHashCode(x);
   }

   protected static void replaceNode(Node container, Element value) {
      if (container != value) {
         Element parent = container.getParent();
         container.detach();
         value.setName(container.getName());
         value.detach();
         parent.add(value);
      }

   }

   public Type getSemiResolvedType(SessionFactoryImplementor factory) {
      return this;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) throws HibernateException {
      boolean include;
      if (this.isAssociationType()) {
         AssociationType atype = (AssociationType)this;
         include = atype.getForeignKeyDirection() == foreignKeyDirection;
      } else {
         include = ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT == foreignKeyDirection;
      }

      return include ? this.replace(original, target, session, owner, copyCache) : target;
   }

   public void beforeAssemble(Serializable cached, SessionImplementor session) {
   }

   static {
      LEGACY_DEFAULT_SIZE = new Size(19, 2, 255L, Size.LobMultiplier.NONE);
   }
}
