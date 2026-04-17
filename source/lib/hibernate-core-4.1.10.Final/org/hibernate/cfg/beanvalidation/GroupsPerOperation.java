package org.hibernate.cfg.beanvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.validation.groups.Default;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;

public class GroupsPerOperation {
   private static final String JPA_GROUP_PREFIX = "javax.persistence.validation.group.";
   private static final String HIBERNATE_GROUP_PREFIX = "org.hibernate.validator.group.";
   private static final Class[] DEFAULT_GROUPS = new Class[]{Default.class};
   private static final Class[] EMPTY_GROUPS = new Class[0];
   private Map groupsPerOperation = new HashMap(4);

   public GroupsPerOperation(Properties properties) {
      this.setGroupsForOperation(GroupsPerOperation.Operation.INSERT, properties);
      this.setGroupsForOperation(GroupsPerOperation.Operation.UPDATE, properties);
      this.setGroupsForOperation(GroupsPerOperation.Operation.DELETE, properties);
      this.setGroupsForOperation(GroupsPerOperation.Operation.DDL, properties);
   }

   private void setGroupsForOperation(Operation operation, Properties properties) {
      Object property = properties.get(operation.getGroupPropertyName());
      Class<?>[] groups;
      if (property == null) {
         groups = operation == GroupsPerOperation.Operation.DELETE ? EMPTY_GROUPS : DEFAULT_GROUPS;
      } else if (property instanceof String) {
         String stringProperty = (String)property;
         String[] groupNames = stringProperty.split(",");
         if (groupNames.length == 1 && groupNames[0].equals("")) {
            groups = EMPTY_GROUPS;
         } else {
            List<Class<?>> groupsList = new ArrayList(groupNames.length);

            for(String groupName : groupNames) {
               String cleanedGroupName = groupName.trim();
               if (cleanedGroupName.length() > 0) {
                  try {
                     groupsList.add(ReflectHelper.classForName(cleanedGroupName));
                  } catch (ClassNotFoundException e) {
                     throw new HibernateException("Unable to load class " + cleanedGroupName, e);
                  }
               }
            }

            groups = (Class[])groupsList.toArray(new Class[groupsList.size()]);
         }
      } else {
         if (!(property instanceof Class[])) {
            throw new HibernateException("javax.persistence.validation.group." + operation.getGroupPropertyName() + " is of unknown type: String or Class<?>[] only");
         }

         groups = (Class[])property;
      }

      this.groupsPerOperation.put(operation, groups);
   }

   public Class[] get(Operation operation) {
      return (Class[])this.groupsPerOperation.get(operation);
   }

   public static enum Operation {
      INSERT("persist", "javax.persistence.validation.group.pre-persist"),
      UPDATE("update", "javax.persistence.validation.group.pre-update"),
      DELETE("remove", "javax.persistence.validation.group.pre-remove"),
      DDL("ddl", "org.hibernate.validator.group.ddl");

      private String exposedName;
      private String groupPropertyName;

      private Operation(String exposedName, String groupProperty) {
         this.exposedName = exposedName;
         this.groupPropertyName = groupProperty;
      }

      public String getName() {
         return this.exposedName;
      }

      public String getGroupPropertyName() {
         return this.groupPropertyName;
      }
   }
}
