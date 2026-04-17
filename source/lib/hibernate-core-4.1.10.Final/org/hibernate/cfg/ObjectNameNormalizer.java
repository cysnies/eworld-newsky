package org.hibernate.cfg;

import org.hibernate.internal.util.StringHelper;

public abstract class ObjectNameNormalizer {
   public String normalizeDatabaseIdentifier(String explicitName, NamingStrategyHelper helper) {
      String objectName = null;
      if (StringHelper.isEmpty(explicitName)) {
         objectName = helper.determineImplicitName(this.getNamingStrategy());
         return this.normalizeIdentifierQuoting(objectName);
      } else {
         objectName = this.normalizeIdentifierQuoting(explicitName);
         objectName = helper.handleExplicitName(this.getNamingStrategy(), objectName);
         return this.normalizeIdentifierQuoting(objectName);
      }
   }

   public String normalizeIdentifierQuoting(String identifier) {
      if (StringHelper.isEmpty(identifier)) {
         return null;
      } else if (identifier.startsWith("\"") && identifier.endsWith("\"")) {
         return '`' + identifier.substring(1, identifier.length() - 1) + '`';
      } else {
         return !this.isUseQuotedIdentifiersGlobally() || identifier.startsWith("`") && identifier.endsWith("`") ? identifier : '`' + identifier + '`';
      }
   }

   protected abstract boolean isUseQuotedIdentifiersGlobally();

   protected abstract NamingStrategy getNamingStrategy();

   public interface NamingStrategyHelper {
      String determineImplicitName(NamingStrategy var1);

      String handleExplicitName(NamingStrategy var1, String var2);
   }
}
