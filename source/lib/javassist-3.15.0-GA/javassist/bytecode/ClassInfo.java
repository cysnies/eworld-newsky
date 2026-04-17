package javassist.bytecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class ClassInfo extends ConstInfo {
   static final int tag = 7;
   int name;
   int index;

   public ClassInfo(int className, int i) {
      this.name = className;
      this.index = i;
   }

   public ClassInfo(DataInputStream in, int i) throws IOException {
      this.name = in.readUnsignedShort();
      this.index = i;
   }

   public int getTag() {
      return 7;
   }

   public String getClassName(ConstPool cp) {
      return cp.getUtf8Info(this.name);
   }

   public void renameClass(ConstPool cp, String oldName, String newName) {
      String nameStr = cp.getUtf8Info(this.name);
      if (nameStr.equals(oldName)) {
         this.name = cp.addUtf8Info(newName);
      } else if (nameStr.charAt(0) == '[') {
         String nameStr2 = Descriptor.rename(nameStr, oldName, newName);
         if (nameStr != nameStr2) {
            this.name = cp.addUtf8Info(nameStr2);
         }
      }

   }

   public void renameClass(ConstPool cp, Map map) {
      String oldName = cp.getUtf8Info(this.name);
      if (oldName.charAt(0) == '[') {
         String newName = Descriptor.rename(oldName, map);
         if (oldName != newName) {
            this.name = cp.addUtf8Info(newName);
         }
      } else {
         String newName = (String)map.get(oldName);
         if (newName != null && !newName.equals(oldName)) {
            this.name = cp.addUtf8Info(newName);
         }
      }

   }

   public int copy(ConstPool src, ConstPool dest, Map map) {
      String classname = src.getUtf8Info(this.name);
      if (map != null) {
         String newname = (String)map.get(classname);
         if (newname != null) {
            classname = newname;
         }
      }

      return dest.addClassInfo(classname);
   }

   public void write(DataOutputStream out) throws IOException {
      out.writeByte(7);
      out.writeShort(this.name);
   }

   public void print(PrintWriter out) {
      out.print("Class #");
      out.println(this.name);
   }

   void makeHashtable(ConstPool cp) {
      String name = Descriptor.toJavaName(this.getClassName(cp));
      cp.classes.put(name, this);
   }
}
