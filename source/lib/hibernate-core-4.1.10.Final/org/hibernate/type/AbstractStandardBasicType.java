package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public abstract class AbstractStandardBasicType implements BasicType, StringRepresentableType, XmlRepresentableType {
   private static final Size DEFAULT_SIZE;
   private final Size dictatedSize = new Size();
   private SqlTypeDescriptor sqlTypeDescriptor;
   private JavaTypeDescriptor javaTypeDescriptor;

   public AbstractStandardBasicType(SqlTypeDescriptor sqlTypeDescriptor, JavaTypeDescriptor javaTypeDescriptor) {
      this.sqlTypeDescriptor = sqlTypeDescriptor;
      this.javaTypeDescriptor = javaTypeDescriptor;
   }

   public Object fromString(String string) {
      return this.javaTypeDescriptor.fromString(string);
   }

   public String toString(Object value) {
      return this.javaTypeDescriptor.toString(value);
   }

   public Object fromStringValue(String xml) throws HibernateException {
      return this.fromString(xml);
   }

   public String toXMLString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return this.toString(value);
   }

   public Object fromXMLString(String xml, Mapping factory) throws HibernateException {
      return StringHelper.isEmpty(xml) ? null : this.fromStringValue(xml);
   }

   protected MutabilityPlan getMutabilityPlan() {
      return this.javaTypeDescriptor.getMutabilityPlan();
   }

   protected Object getReplacement(Object original, Object target, SessionImplementor session) {
      if (!this.isMutable()) {
         return original;
      } else {
         return this.isEqual(original, target) ? original : this.deepCopy(original);
      }
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
   }

   public String[] getRegistrationKeys() {
      return this.registerUnderJavaType() ? new String[]{this.getName(), this.javaTypeDescriptor.getJavaTypeClass().getName()} : new String[]{this.getName()};
   }

   protected boolean registerUnderJavaType() {
      return false;
   }

   protected static Size getDefaultSize() {
      return DEFAULT_SIZE;
   }

   protected Size getDictatedSize() {
      return this.dictatedSize;
   }

   public final JavaTypeDescriptor getJavaTypeDescriptor() {
      return this.javaTypeDescriptor;
   }

   public final void setJavaTypeDescriptor(JavaTypeDescriptor javaTypeDescriptor) {
      this.javaTypeDescriptor = javaTypeDescriptor;
   }

   public final SqlTypeDescriptor getSqlTypeDescriptor() {
      return this.sqlTypeDescriptor;
   }

   public final void setSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
      this.sqlTypeDescriptor = sqlTypeDescriptor;
   }

   public final Class getReturnedClass() {
      return this.javaTypeDescriptor.getJavaTypeClass();
   }

   public final int getColumnSpan(Mapping mapping) throws MappingException {
      return this.sqlTypes(mapping).length;
   }

   public final int[] sqlTypes(Mapping mapping) throws MappingException {
      return new int[]{this.sqlTypeDescriptor.getSqlType()};
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return new Size[]{this.getDictatedSize()};
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return new Size[]{getDefaultSize()};
   }

   public final boolean isAssociationType() {
      return false;
   }

   public final boolean isCollectionType() {
      return false;
   }

   public final boolean isComponentType() {
      return false;
   }

   public final boolean isEntityType() {
      return false;
   }

   public final boolean isAnyType() {
      return false;
   }

   public final boolean isXMLElement() {
      return false;
   }

   public final boolean isSame(Object x, Object y) {
      return this.isEqual(x, y);
   }

   public final boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) {
      return this.isEqual(x, y);
   }

   public final boolean isEqual(Object one, Object another) {
      return this.javaTypeDescriptor.areEqual(one, another);
   }

   public final int getHashCode(Object x) {
      return this.javaTypeDescriptor.extractHashCode(x);
   }

   public final int getHashCode(Object x, SessionFactoryImplementor factory) {
      return this.getHashCode(x);
   }

   public final int compare(Object x, Object y) {
      return this.javaTypeDescriptor.getComparator().compare(x, y);
   }

   public final boolean isDirty(Object old, Object current, SessionImplementor session) {
      return this.isDirty(old, current);
   }

   public final boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) {
      return checkable[0] && this.isDirty(old, current);
   }

   protected final boolean isDirty(Object old, Object current) {
      return !this.isSame(old, current);
   }

   public final boolean isModified(Object oldHydratedState, Object currentState, boolean[] checkable, SessionImplementor session) {
      return this.isDirty(oldHydratedState, currentState);
   }

   public final Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
      return this.nullSafeGet(rs, names[0], session);
   }

   public final Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws SQLException {
      return this.nullSafeGet(rs, name, session);
   }

   public final Object nullSafeGet(ResultSet rs, String name, SessionImplementor session) throws SQLException {
      WrapperOptions options = this.getOptions(session);
      return this.nullSafeGet(rs, name, options);
   }

   protected final Object nullSafeGet(ResultSet rs, String name, WrapperOptions options) throws SQLException {
      return this.remapSqlTypeDescriptor(options).getExtractor(this.javaTypeDescriptor).extract(rs, name, options);
   }

   public Object get(ResultSet rs, String name, SessionImplementor session) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, name, session);
   }

   public final void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
      WrapperOptions options = this.getOptions(session);
      this.nullSafeSet(st, value, index, options);
   }

   protected final void nullSafeSet(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
      this.remapSqlTypeDescriptor(options).getBinder(this.javaTypeDescriptor).bind(st, value, index, options);
   }

   protected SqlTypeDescriptor remapSqlTypeDescriptor(WrapperOptions options) {
      return options.remapSqlTypeDescriptor(this.sqlTypeDescriptor);
   }

   public void set(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.nullSafeSet(st, value, index, session);
   }

   public final String toLoggableString(Object value, SessionFactoryImplementor factory) {
      return this.javaTypeDescriptor.extractLoggableRepresentation(value);
   }

   public final void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) {
      node.setText(this.toString(value));
   }

   public final Object fromXMLNode(Node xml, Mapping factory) {
      return this.fromString(xml.getText());
   }

   public final boolean isMutable() {
      return this.getMutabilityPlan().isMutable();
   }

   public final Object deepCopy(Object value, SessionFactoryImplementor factory) {
      return this.deepCopy(value);
   }

   protected final Object deepCopy(Object value) {
      return this.getMutabilityPlan().deepCopy(value);
   }

   public final Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return this.getMutabilityPlan().disassemble(value);
   }

   public final Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      return this.getMutabilityPlan().assemble(cached);
   }

   public final void beforeAssemble(Serializable cached, SessionImplementor session) {
   }

   public final Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, names, session, owner);
   }

   public final Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value;
   }

   public final Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value;
   }

   public final Type getSemiResolvedType(SessionFactoryImplementor factory) {
      return this;
   }

   public final Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) {
      return this.getReplacement(original, target, session);
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) {
      return ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT == foreignKeyDirection ? this.getReplacement(original, target, session) : target;
   }

   private WrapperOptions getOptions(SessionImplementor session) {
      // $FF: Couldn't be decompiled
   }

   static {
      DEFAULT_SIZE = new Size(19, 2, 255L, Size.LobMultiplier.NONE);
   }
}
