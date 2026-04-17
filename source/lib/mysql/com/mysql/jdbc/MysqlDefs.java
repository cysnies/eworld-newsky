package com.mysql.jdbc;

import java.util.HashMap;
import java.util.Map;

public final class MysqlDefs {
   static final int COM_BINLOG_DUMP = 18;
   static final int COM_CHANGE_USER = 17;
   static final int COM_CLOSE_STATEMENT = 25;
   static final int COM_CONNECT_OUT = 20;
   static final int COM_END = 29;
   static final int COM_EXECUTE = 23;
   static final int COM_FETCH = 28;
   static final int COM_LONG_DATA = 24;
   static final int COM_PREPARE = 22;
   static final int COM_REGISTER_SLAVE = 21;
   static final int COM_RESET_STMT = 26;
   static final int COM_SET_OPTION = 27;
   static final int COM_TABLE_DUMP = 19;
   static final int CONNECT = 11;
   static final int CREATE_DB = 5;
   static final int DEBUG = 13;
   static final int DELAYED_INSERT = 16;
   static final int DROP_DB = 6;
   static final int FIELD_LIST = 4;
   static final int FIELD_TYPE_BIT = 16;
   public static final int FIELD_TYPE_BLOB = 252;
   static final int FIELD_TYPE_DATE = 10;
   static final int FIELD_TYPE_DATETIME = 12;
   static final int FIELD_TYPE_DECIMAL = 0;
   static final int FIELD_TYPE_DOUBLE = 5;
   static final int FIELD_TYPE_ENUM = 247;
   static final int FIELD_TYPE_FLOAT = 4;
   static final int FIELD_TYPE_GEOMETRY = 255;
   static final int FIELD_TYPE_INT24 = 9;
   static final int FIELD_TYPE_LONG = 3;
   static final int FIELD_TYPE_LONG_BLOB = 251;
   static final int FIELD_TYPE_LONGLONG = 8;
   static final int FIELD_TYPE_MEDIUM_BLOB = 250;
   static final int FIELD_TYPE_NEW_DECIMAL = 246;
   static final int FIELD_TYPE_NEWDATE = 14;
   static final int FIELD_TYPE_NULL = 6;
   static final int FIELD_TYPE_SET = 248;
   static final int FIELD_TYPE_SHORT = 2;
   static final int FIELD_TYPE_STRING = 254;
   static final int FIELD_TYPE_TIME = 11;
   static final int FIELD_TYPE_TIMESTAMP = 7;
   static final int FIELD_TYPE_TINY = 1;
   static final int FIELD_TYPE_TINY_BLOB = 249;
   static final int FIELD_TYPE_VAR_STRING = 253;
   static final int FIELD_TYPE_VARCHAR = 15;
   static final int FIELD_TYPE_YEAR = 13;
   static final int INIT_DB = 2;
   static final long LENGTH_BLOB = 65535L;
   static final long LENGTH_LONGBLOB = 4294967295L;
   static final long LENGTH_MEDIUMBLOB = 16777215L;
   static final long LENGTH_TINYBLOB = 255L;
   static final int MAX_ROWS = 50000000;
   public static final int NO_CHARSET_INFO = -1;
   static final byte OPEN_CURSOR_FLAG = 1;
   static final int PING = 14;
   static final int PROCESS_INFO = 10;
   static final int PROCESS_KILL = 12;
   static final int QUERY = 3;
   static final int QUIT = 1;
   static final int RELOAD = 7;
   static final int SHUTDOWN = 8;
   static final int SLEEP = 0;
   static final int STATISTICS = 9;
   static final int TIME = 15;
   private static Map mysqlToJdbcTypesMap = new HashMap();

   static int mysqlToJavaType(int mysqlType) {
      int jdbcType;
      switch (mysqlType) {
         case 0:
         case 246:
            jdbcType = 3;
            break;
         case 1:
            jdbcType = -6;
            break;
         case 2:
            jdbcType = 5;
            break;
         case 3:
            jdbcType = 4;
            break;
         case 4:
            jdbcType = 7;
            break;
         case 5:
            jdbcType = 8;
            break;
         case 6:
            jdbcType = 0;
            break;
         case 7:
            jdbcType = 93;
            break;
         case 8:
            jdbcType = -5;
            break;
         case 9:
            jdbcType = 4;
            break;
         case 10:
            jdbcType = 91;
            break;
         case 11:
            jdbcType = 92;
            break;
         case 12:
            jdbcType = 93;
            break;
         case 13:
            jdbcType = 91;
            break;
         case 14:
            jdbcType = 91;
            break;
         case 15:
         case 253:
            jdbcType = 12;
            break;
         case 16:
            jdbcType = -7;
            break;
         case 247:
            jdbcType = 1;
            break;
         case 248:
            jdbcType = 1;
            break;
         case 249:
            jdbcType = -3;
            break;
         case 250:
            jdbcType = -4;
            break;
         case 251:
            jdbcType = -4;
            break;
         case 252:
            jdbcType = -4;
            break;
         case 254:
            jdbcType = 1;
            break;
         case 255:
            jdbcType = -2;
            break;
         default:
            jdbcType = 12;
      }

      return jdbcType;
   }

   static int mysqlToJavaType(String mysqlType) {
      if (mysqlType.equalsIgnoreCase("BIT")) {
         return mysqlToJavaType(16);
      } else if (mysqlType.equalsIgnoreCase("TINYINT")) {
         return mysqlToJavaType(1);
      } else if (mysqlType.equalsIgnoreCase("SMALLINT")) {
         return mysqlToJavaType(2);
      } else if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
         return mysqlToJavaType(9);
      } else if (!mysqlType.equalsIgnoreCase("INT") && !mysqlType.equalsIgnoreCase("INTEGER")) {
         if (mysqlType.equalsIgnoreCase("BIGINT")) {
            return mysqlToJavaType(8);
         } else if (mysqlType.equalsIgnoreCase("INT24")) {
            return mysqlToJavaType(9);
         } else if (mysqlType.equalsIgnoreCase("REAL")) {
            return mysqlToJavaType(5);
         } else if (mysqlType.equalsIgnoreCase("FLOAT")) {
            return mysqlToJavaType(4);
         } else if (mysqlType.equalsIgnoreCase("DECIMAL")) {
            return mysqlToJavaType(0);
         } else if (mysqlType.equalsIgnoreCase("NUMERIC")) {
            return mysqlToJavaType(0);
         } else if (mysqlType.equalsIgnoreCase("DOUBLE")) {
            return mysqlToJavaType(5);
         } else if (mysqlType.equalsIgnoreCase("CHAR")) {
            return mysqlToJavaType(254);
         } else if (mysqlType.equalsIgnoreCase("VARCHAR")) {
            return mysqlToJavaType(253);
         } else if (mysqlType.equalsIgnoreCase("DATE")) {
            return mysqlToJavaType(10);
         } else if (mysqlType.equalsIgnoreCase("TIME")) {
            return mysqlToJavaType(11);
         } else if (mysqlType.equalsIgnoreCase("YEAR")) {
            return mysqlToJavaType(13);
         } else if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
            return mysqlToJavaType(7);
         } else if (mysqlType.equalsIgnoreCase("DATETIME")) {
            return mysqlToJavaType(12);
         } else if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
            return -2;
         } else if (mysqlType.equalsIgnoreCase("BLOB")) {
            return -4;
         } else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
            return -4;
         } else if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
            return -4;
         } else if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
            return 12;
         } else if (mysqlType.equalsIgnoreCase("TEXT")) {
            return -1;
         } else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
            return -1;
         } else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
            return -1;
         } else if (mysqlType.equalsIgnoreCase("ENUM")) {
            return mysqlToJavaType(247);
         } else if (mysqlType.equalsIgnoreCase("SET")) {
            return mysqlToJavaType(248);
         } else if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
            return mysqlToJavaType(255);
         } else if (mysqlType.equalsIgnoreCase("BINARY")) {
            return -2;
         } else if (mysqlType.equalsIgnoreCase("VARBINARY")) {
            return -3;
         } else {
            return mysqlType.equalsIgnoreCase("BIT") ? mysqlToJavaType(16) : 1111;
         }
      } else {
         return mysqlToJavaType(3);
      }
   }

   public static String typeToName(int mysqlType) {
      switch (mysqlType) {
         case 0:
            return "FIELD_TYPE_DECIMAL";
         case 1:
            return "FIELD_TYPE_TINY";
         case 2:
            return "FIELD_TYPE_SHORT";
         case 3:
            return "FIELD_TYPE_LONG";
         case 4:
            return "FIELD_TYPE_FLOAT";
         case 5:
            return "FIELD_TYPE_DOUBLE";
         case 6:
            return "FIELD_TYPE_NULL";
         case 7:
            return "FIELD_TYPE_TIMESTAMP";
         case 8:
            return "FIELD_TYPE_LONGLONG";
         case 9:
            return "FIELD_TYPE_INT24";
         case 10:
            return "FIELD_TYPE_DATE";
         case 11:
            return "FIELD_TYPE_TIME";
         case 12:
            return "FIELD_TYPE_DATETIME";
         case 13:
            return "FIELD_TYPE_YEAR";
         case 14:
            return "FIELD_TYPE_NEWDATE";
         case 15:
            return "FIELD_TYPE_VARCHAR";
         case 247:
            return "FIELD_TYPE_ENUM";
         case 248:
            return "FIELD_TYPE_SET";
         case 249:
            return "FIELD_TYPE_TINY_BLOB";
         case 250:
            return "FIELD_TYPE_MEDIUM_BLOB";
         case 251:
            return "FIELD_TYPE_LONG_BLOB";
         case 252:
            return "FIELD_TYPE_BLOB";
         case 253:
            return "FIELD_TYPE_VAR_STRING";
         case 254:
            return "FIELD_TYPE_STRING";
         case 255:
            return "FIELD_TYPE_GEOMETRY";
         default:
            return " Unknown MySQL Type # " + mysqlType;
      }
   }

   static final void appendJdbcTypeMappingQuery(StringBuffer buf, String mysqlTypeColumnName) {
      buf.append("CASE ");
      Map typesMap = new HashMap();
      typesMap.putAll(mysqlToJdbcTypesMap);
      typesMap.put("BINARY", Constants.integerValueOf(-2));
      typesMap.put("VARBINARY", Constants.integerValueOf(-3));

      for(String mysqlTypeName : typesMap.keySet()) {
         buf.append(" WHEN ");
         buf.append(mysqlTypeColumnName);
         buf.append("='");
         buf.append(mysqlTypeName);
         buf.append("' THEN ");
         buf.append(typesMap.get(mysqlTypeName));
         if (mysqlTypeName.equalsIgnoreCase("DOUBLE") || mysqlTypeName.equalsIgnoreCase("FLOAT") || mysqlTypeName.equalsIgnoreCase("DECIMAL") || mysqlTypeName.equalsIgnoreCase("NUMERIC")) {
            buf.append(" WHEN ");
            buf.append(mysqlTypeColumnName);
            buf.append("='");
            buf.append(mysqlTypeName);
            buf.append(" unsigned' THEN ");
            buf.append(typesMap.get(mysqlTypeName));
         }
      }

      buf.append(" ELSE ");
      buf.append(1111);
      buf.append(" END ");
   }

   static {
      mysqlToJdbcTypesMap.put("BIT", Constants.integerValueOf(mysqlToJavaType(16)));
      mysqlToJdbcTypesMap.put("TINYINT", Constants.integerValueOf(mysqlToJavaType(1)));
      mysqlToJdbcTypesMap.put("SMALLINT", Constants.integerValueOf(mysqlToJavaType(2)));
      mysqlToJdbcTypesMap.put("MEDIUMINT", Constants.integerValueOf(mysqlToJavaType(9)));
      mysqlToJdbcTypesMap.put("INT", Constants.integerValueOf(mysqlToJavaType(3)));
      mysqlToJdbcTypesMap.put("INTEGER", Constants.integerValueOf(mysqlToJavaType(3)));
      mysqlToJdbcTypesMap.put("BIGINT", Constants.integerValueOf(mysqlToJavaType(8)));
      mysqlToJdbcTypesMap.put("INT24", Constants.integerValueOf(mysqlToJavaType(9)));
      mysqlToJdbcTypesMap.put("REAL", Constants.integerValueOf(mysqlToJavaType(5)));
      mysqlToJdbcTypesMap.put("FLOAT", Constants.integerValueOf(mysqlToJavaType(4)));
      mysqlToJdbcTypesMap.put("DECIMAL", Constants.integerValueOf(mysqlToJavaType(0)));
      mysqlToJdbcTypesMap.put("NUMERIC", Constants.integerValueOf(mysqlToJavaType(0)));
      mysqlToJdbcTypesMap.put("DOUBLE", Constants.integerValueOf(mysqlToJavaType(5)));
      mysqlToJdbcTypesMap.put("CHAR", Constants.integerValueOf(mysqlToJavaType(254)));
      mysqlToJdbcTypesMap.put("VARCHAR", Constants.integerValueOf(mysqlToJavaType(253)));
      mysqlToJdbcTypesMap.put("DATE", Constants.integerValueOf(mysqlToJavaType(10)));
      mysqlToJdbcTypesMap.put("TIME", Constants.integerValueOf(mysqlToJavaType(11)));
      mysqlToJdbcTypesMap.put("YEAR", Constants.integerValueOf(mysqlToJavaType(13)));
      mysqlToJdbcTypesMap.put("TIMESTAMP", Constants.integerValueOf(mysqlToJavaType(7)));
      mysqlToJdbcTypesMap.put("DATETIME", Constants.integerValueOf(mysqlToJavaType(12)));
      mysqlToJdbcTypesMap.put("TINYBLOB", Constants.integerValueOf(-2));
      mysqlToJdbcTypesMap.put("BLOB", Constants.integerValueOf(-4));
      mysqlToJdbcTypesMap.put("MEDIUMBLOB", Constants.integerValueOf(-4));
      mysqlToJdbcTypesMap.put("LONGBLOB", Constants.integerValueOf(-4));
      mysqlToJdbcTypesMap.put("TINYTEXT", Constants.integerValueOf(12));
      mysqlToJdbcTypesMap.put("TEXT", Constants.integerValueOf(-1));
      mysqlToJdbcTypesMap.put("MEDIUMTEXT", Constants.integerValueOf(-1));
      mysqlToJdbcTypesMap.put("LONGTEXT", Constants.integerValueOf(-1));
      mysqlToJdbcTypesMap.put("ENUM", Constants.integerValueOf(mysqlToJavaType(247)));
      mysqlToJdbcTypesMap.put("SET", Constants.integerValueOf(mysqlToJavaType(248)));
      mysqlToJdbcTypesMap.put("GEOMETRY", Constants.integerValueOf(mysqlToJavaType(255)));
   }
}
