package org.hibernate.id.factory.internal;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.id.Assigned;
import org.hibernate.id.Configurable;
import org.hibernate.id.ForeignGenerator;
import org.hibernate.id.GUIDGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.IncrementGenerator;
import org.hibernate.id.SelectGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.id.SequenceHiLoGenerator;
import org.hibernate.id.SequenceIdentityGenerator;
import org.hibernate.id.TableHiLoGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class DefaultIdentifierGeneratorFactory implements MutableIdentifierGeneratorFactory, Serializable, ServiceRegistryAwareService {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultIdentifierGeneratorFactory.class.getName());
   private transient Dialect dialect;
   private ConcurrentHashMap generatorStrategyToClassNameMap = new ConcurrentHashMap();

   public DefaultIdentifierGeneratorFactory() {
      this.register("uuid2", UUIDGenerator.class);
      this.register("guid", GUIDGenerator.class);
      this.register("uuid", UUIDHexGenerator.class);
      this.register("uuid.hex", UUIDHexGenerator.class);
      this.register("hilo", TableHiLoGenerator.class);
      this.register("assigned", Assigned.class);
      this.register("identity", IdentityGenerator.class);
      this.register("select", SelectGenerator.class);
      this.register("sequence", SequenceGenerator.class);
      this.register("seqhilo", SequenceHiLoGenerator.class);
      this.register("increment", IncrementGenerator.class);
      this.register("foreign", ForeignGenerator.class);
      this.register("sequence-identity", SequenceIdentityGenerator.class);
      this.register("enhanced-sequence", SequenceStyleGenerator.class);
      this.register("enhanced-table", TableGenerator.class);
   }

   public void register(String strategy, Class generatorClass) {
      LOG.debugf("Registering IdentifierGenerator strategy [%s] -> [%s]", strategy, generatorClass.getName());
      Class previous = (Class)this.generatorStrategyToClassNameMap.put(strategy, generatorClass);
      if (previous != null) {
         LOG.debugf("    - overriding [%s]", previous.getName());
      }

   }

   public Dialect getDialect() {
      return this.dialect;
   }

   public void setDialect(Dialect dialect) {
      LOG.debugf("Setting dialect [%s]", dialect);
      this.dialect = dialect;
   }

   public IdentifierGenerator createIdentifierGenerator(String strategy, Type type, Properties config) {
      try {
         Class clazz = this.getIdentifierGeneratorClass(strategy);
         IdentifierGenerator identifierGenerator = (IdentifierGenerator)clazz.newInstance();
         if (identifierGenerator instanceof Configurable) {
            ((Configurable)identifierGenerator).configure(type, config, this.dialect);
         }

         return identifierGenerator;
      } catch (Exception e) {
         String entityName = config.getProperty("entity_name");
         throw new MappingException(String.format("Could not instantiate id generator [entity-name=%s]", entityName), e);
      }
   }

   public Class getIdentifierGeneratorClass(String strategy) {
      if ("native".equals(strategy)) {
         return this.dialect.getNativeIdentifierGeneratorClass();
      } else {
         Class generatorClass = (Class)this.generatorStrategyToClassNameMap.get(strategy);

         try {
            if (generatorClass == null) {
               generatorClass = ReflectHelper.classForName(strategy);
            }

            return generatorClass;
         } catch (ClassNotFoundException var4) {
            throw new MappingException(String.format("Could not interpret id generator strategy [%s]", strategy));
         }
      }
   }

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.dialect = ((JdbcServices)serviceRegistry.getService(JdbcServices.class)).getDialect();
   }
}
