package org.hibernate.cfg;

import java.io.InputStream;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.xml.DTDEntityResolver;
import org.jboss.logging.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class EJB3DTDEntityResolver extends DTDEntityResolver {
   public static final EntityResolver INSTANCE = new EJB3DTDEntityResolver();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EJB3DTDEntityResolver.class.getName());
   boolean resolved = false;

   public boolean isResolved() {
      return this.resolved;
   }

   public InputSource resolveEntity(String publicId, String systemId) {
      LOG.tracev("Resolving XML entity {0} : {1}", publicId, systemId);
      InputSource is = super.resolveEntity(publicId, systemId);
      if (is == null) {
         if (systemId != null) {
            if (systemId.endsWith("orm_1_0.xsd")) {
               InputStream dtdStream = this.getStreamFromClasspath("orm_1_0.xsd");
               InputSource source = this.buildInputSource(publicId, systemId, dtdStream, false);
               if (source != null) {
                  return source;
               }
            } else if (systemId.endsWith("orm_2_0.xsd")) {
               InputStream dtdStream = this.getStreamFromClasspath("orm_2_0.xsd");
               InputSource source = this.buildInputSource(publicId, systemId, dtdStream, false);
               if (source != null) {
                  return source;
               }
            } else if (systemId.endsWith("persistence_1_0.xsd")) {
               InputStream dtdStream = this.getStreamFromClasspath("persistence_1_0.xsd");
               InputSource source = this.buildInputSource(publicId, systemId, dtdStream, true);
               if (source != null) {
                  return source;
               }
            } else if (systemId.endsWith("persistence_2_0.xsd")) {
               InputStream dtdStream = this.getStreamFromClasspath("persistence_2_0.xsd");
               InputSource source = this.buildInputSource(publicId, systemId, dtdStream, true);
               if (source != null) {
                  return source;
               }
            }
         }

         return null;
      } else {
         this.resolved = true;
         return is;
      }
   }

   private InputSource buildInputSource(String publicId, String systemId, InputStream dtdStream, boolean resolved) {
      if (dtdStream == null) {
         LOG.tracev("Unable to locate [{0}] on classpath", systemId);
         return null;
      } else {
         LOG.tracev("Located [{0}] in classpath", systemId);
         InputSource source = new InputSource(dtdStream);
         source.setPublicId(publicId);
         source.setSystemId(systemId);
         this.resolved = resolved;
         return source;
      }
   }

   private InputStream getStreamFromClasspath(String fileName) {
      LOG.trace("Recognized JPA ORM namespace; attempting to resolve on classpath under org/hibernate/ejb");
      String path = "org/hibernate/ejb/" + fileName;
      InputStream dtdStream = this.resolveInHibernateNamespace(path);
      return dtdStream;
   }
}
