package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class PostgresPlusDialect extends PostgreSQLDialect {
   public PostgresPlusDialect() {
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("soundex", new StandardSQLFunction("soundex"));
      this.registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.DATE, false));
      this.registerFunction("rowid", new NoArgSQLFunction("rowid", StandardBasicTypes.LONG, false));
      this.registerFunction("rownum", new NoArgSQLFunction("rownum", StandardBasicTypes.LONG, false));
      this.registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
      this.registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
      this.registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
      this.registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
      this.registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING));
      this.registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("coalesce", new NvlFunction());
      this.registerFunction("atan2", new StandardSQLFunction("atan2", StandardBasicTypes.FLOAT));
      this.registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER));
      this.registerFunction("nvl", new StandardSQLFunction("nvl"));
      this.registerFunction("nvl2", new StandardSQLFunction("nvl2"));
      this.registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.FLOAT));
      this.registerFunction("add_months", new StandardSQLFunction("add_months", StandardBasicTypes.DATE));
      this.registerFunction("months_between", new StandardSQLFunction("months_between", StandardBasicTypes.FLOAT));
      this.registerFunction("next_day", new StandardSQLFunction("next_day", StandardBasicTypes.DATE));
   }

   public String getCurrentTimestampSelectString() {
      return "select sysdate";
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "sysdate";
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      statement.registerOutParameter(col, 2006);
      ++col;
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      ps.execute();
      return (ResultSet)ps.getObject(1);
   }

   public String getSelectGUIDString() {
      return "select uuid_generate_v1";
   }
}
