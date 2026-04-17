package org.hibernate.type;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

public class TypeResolver implements Serializable {
   private final BasicTypeRegistry basicTypeRegistry;
   private final TypeFactory typeFactory;

   public TypeResolver() {
      this(new BasicTypeRegistry(), new TypeFactory());
   }

   public TypeResolver(BasicTypeRegistry basicTypeRegistry, TypeFactory typeFactory) {
      this.basicTypeRegistry = basicTypeRegistry;
      this.typeFactory = typeFactory;
   }

   public TypeResolver scope(SessionFactoryImplementor factory) {
      this.typeFactory.injectSessionFactory(factory);
      return new TypeResolver(this.basicTypeRegistry.shallowCopy(), this.typeFactory);
   }

   public void registerTypeOverride(BasicType type) {
      this.basicTypeRegistry.register(type);
   }

   public void registerTypeOverride(UserType type, String[] keys) {
      this.basicTypeRegistry.register(type, keys);
   }

   public void registerTypeOverride(CompositeUserType type, String[] keys) {
      this.basicTypeRegistry.register(type, keys);
   }

   public TypeFactory getTypeFactory() {
      return this.typeFactory;
   }

   public BasicType basic(String name) {
      return this.basicTypeRegistry.getRegisteredType(name);
   }

   public Type heuristicType(String typeName) throws MappingException {
      return this.heuristicType(typeName, (Properties)null);
   }

   public Type heuristicType(String typeName, Properties parameters) throws MappingException {
      Type type = this.basic(typeName);
      if (type != null) {
         return type;
      } else {
         try {
            Class typeClass = ReflectHelper.classForName(typeName);
            if (typeClass != null) {
               return this.typeFactory.byClass(typeClass, parameters);
            }
         } catch (ClassNotFoundException var5) {
         }

         return null;
      }
   }
}
