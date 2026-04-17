package org.hibernate.internal.util.jndi;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/** @deprecated */
@Deprecated
public final class JndiHelper {
   private JndiHelper() {
   }

   public static Properties extractJndiProperties(Map configurationValues) {
      Properties jndiProperties = new Properties();

      for(Map.Entry entry : configurationValues.entrySet()) {
         if (String.class.isInstance(entry.getKey())) {
            String propertyName = (String)entry.getKey();
            Object propertyValue = entry.getValue();
            if (propertyName.startsWith("hibernate.jndi")) {
               if ("hibernate.jndi.class".equals(propertyName)) {
                  if (propertyValue != null) {
                     jndiProperties.put("java.naming.factory.initial", propertyValue);
                  }
               } else if ("hibernate.jndi.url".equals(propertyName)) {
                  if (propertyValue != null) {
                     jndiProperties.put("java.naming.provider.url", propertyValue);
                  }
               } else {
                  String passThruPropertyname = propertyName.substring("hibernate.jndi".length() + 1);
                  jndiProperties.put(passThruPropertyname, propertyValue);
               }
            }
         }
      }

      return jndiProperties;
   }

   public static InitialContext getInitialContext(Properties props) throws NamingException {
      Hashtable hash = extractJndiProperties(props);
      return hash.size() == 0 ? new InitialContext() : new InitialContext(hash);
   }

   public static void bind(Context ctx, String name, Object val) throws NamingException {
      try {
         ctx.rebind(name, val);
      } catch (Exception var9) {
         Name n;
         for(n = ctx.getNameParser("").parse(name); n.size() > 1; n = n.getSuffix(1)) {
            String ctxName = n.get(0);
            Context subctx = null;

            try {
               subctx = (Context)ctx.lookup(ctxName);
            } catch (NameNotFoundException var8) {
            }

            if (subctx != null) {
               ctx = subctx;
            } else {
               ctx = ctx.createSubcontext(ctxName);
            }
         }

         ctx.rebind(n, val);
      }

   }
}
