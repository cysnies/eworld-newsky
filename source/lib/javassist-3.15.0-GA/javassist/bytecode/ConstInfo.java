package javassist.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

abstract class ConstInfo {
   public abstract int getTag();

   public String getClassName(ConstPool cp) {
      return null;
   }

   public void renameClass(ConstPool cp, String oldName, String newName) {
   }

   public void renameClass(ConstPool cp, Map classnames) {
   }

   public abstract int copy(ConstPool var1, ConstPool var2, Map var3);

   public abstract void write(DataOutputStream var1) throws IOException;

   public abstract void print(PrintWriter var1);

   void makeHashtable(ConstPool cp) {
   }

   boolean hashCheck(int a, int b) {
      return false;
   }

   public String toString() {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(bout);
      this.print(out);
      return bout.toString();
   }
}
