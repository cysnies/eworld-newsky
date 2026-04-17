package com.mysql.jdbc.log;

import com.mysql.jdbc.SQLError;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class LogFactory {
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$log$Log;
   // $FF: synthetic field
   static Class class$java$lang$String;

   public static Log getLogger(String className, String instanceName) throws SQLException {
      if (className == null) {
         throw SQLError.createSQLException("Logger class can not be NULL", "S1009");
      } else if (instanceName == null) {
         throw SQLError.createSQLException("Logger instance name can not be NULL", "S1009");
      } else {
         try {
            Class loggerClass = null;

            try {
               loggerClass = Class.forName(className);
            } catch (ClassNotFoundException var4) {
               loggerClass = Class.forName((class$com$mysql$jdbc$log$Log == null ? (class$com$mysql$jdbc$log$Log = class$("com.mysql.jdbc.log.Log")) : class$com$mysql$jdbc$log$Log).getPackage().getName() + "." + className);
            }

            Constructor constructor = loggerClass.getConstructor(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String);
            return (Log)constructor.newInstance(instanceName);
         } catch (ClassNotFoundException cnfe) {
            SQLException sqlEx = SQLError.createSQLException("Unable to load class for logger '" + className + "'", "S1009");
            sqlEx.initCause(cnfe);
            throw sqlEx;
         } catch (NoSuchMethodException nsme) {
            SQLException sqlEx = SQLError.createSQLException("Logger class does not have a single-arg constructor that takes an instance name", "S1009");
            sqlEx.initCause(nsme);
            throw sqlEx;
         } catch (InstantiationException inse) {
            SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', exception in constructor?", "S1009");
            sqlEx.initCause(inse);
            throw sqlEx;
         } catch (InvocationTargetException ite) {
            SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', exception in constructor?", "S1009");
            sqlEx.initCause(ite);
            throw sqlEx;
         } catch (IllegalAccessException iae) {
            SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', constructor not public", "S1009");
            sqlEx.initCause(iae);
            throw sqlEx;
         } catch (ClassCastException cce) {
            SQLException sqlEx = SQLError.createSQLException("Logger class '" + className + "' does not implement the '" + (class$com$mysql$jdbc$log$Log == null ? (class$com$mysql$jdbc$log$Log = class$("com.mysql.jdbc.log.Log")) : class$com$mysql$jdbc$log$Log).getName() + "' interface", "S1009");
            sqlEx.initCause(cce);
            throw sqlEx;
         }
      }
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
