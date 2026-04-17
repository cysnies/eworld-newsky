package javassist.bytecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class NameAndTypeInfo extends ConstInfo {
   static final int tag = 12;
   int memberName;
   int typeDescriptor;

   public NameAndTypeInfo(int name, int type) {
      this.memberName = name;
      this.typeDescriptor = type;
   }

   public NameAndTypeInfo(DataInputStream in) throws IOException {
      this.memberName = in.readUnsignedShort();
      this.typeDescriptor = in.readUnsignedShort();
   }

   boolean hashCheck(int a, int b) {
      return a == this.memberName && b == this.typeDescriptor;
   }

   public int getTag() {
      return 12;
   }

   public void renameClass(ConstPool cp, String oldName, String newName) {
      String type = cp.getUtf8Info(this.typeDescriptor);
      String type2 = Descriptor.rename(type, oldName, newName);
      if (type != type2) {
         this.typeDescriptor = cp.addUtf8Info(type2);
      }

   }

   public void renameClass(ConstPool cp, Map map) {
      String type = cp.getUtf8Info(this.typeDescriptor);
      String type2 = Descriptor.rename(type, map);
      if (type != type2) {
         this.typeDescriptor = cp.addUtf8Info(type2);
      }

   }

   public int copy(ConstPool src, ConstPool dest, Map map) {
      String mname = src.getUtf8Info(this.memberName);
      String tdesc = src.getUtf8Info(this.typeDescriptor);
      tdesc = Descriptor.rename(tdesc, map);
      return dest.addNameAndTypeInfo(dest.addUtf8Info(mname), dest.addUtf8Info(tdesc));
   }

   public void write(DataOutputStream out) throws IOException {
      out.writeByte(12);
      out.writeShort(this.memberName);
      out.writeShort(this.typeDescriptor);
   }

   public void print(PrintWriter out) {
      out.print("NameAndType #");
      out.print(this.memberName);
      out.print(", type #");
      out.println(this.typeDescriptor);
   }
}
