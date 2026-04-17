package javassist.bytecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

abstract class MemberrefInfo extends ConstInfo {
   int classIndex;
   int nameAndTypeIndex;

   public MemberrefInfo(int cindex, int ntindex) {
      this.classIndex = cindex;
      this.nameAndTypeIndex = ntindex;
   }

   public MemberrefInfo(DataInputStream in) throws IOException {
      this.classIndex = in.readUnsignedShort();
      this.nameAndTypeIndex = in.readUnsignedShort();
   }

   public int copy(ConstPool src, ConstPool dest, Map map) {
      int classIndex2 = src.getItem(this.classIndex).copy(src, dest, map);
      int ntIndex2 = src.getItem(this.nameAndTypeIndex).copy(src, dest, map);
      return this.copy2(dest, classIndex2, ntIndex2);
   }

   boolean hashCheck(int a, int b) {
      return a == this.classIndex && b == this.nameAndTypeIndex;
   }

   protected abstract int copy2(ConstPool var1, int var2, int var3);

   public void write(DataOutputStream out) throws IOException {
      out.writeByte(this.getTag());
      out.writeShort(this.classIndex);
      out.writeShort(this.nameAndTypeIndex);
   }

   public void print(PrintWriter out) {
      out.print(this.getTagName() + " #");
      out.print(this.classIndex);
      out.print(", name&type #");
      out.println(this.nameAndTypeIndex);
   }

   public abstract String getTagName();
}
