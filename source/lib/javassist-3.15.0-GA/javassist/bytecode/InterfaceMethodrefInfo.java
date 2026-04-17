package javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;

class InterfaceMethodrefInfo extends MemberrefInfo {
   static final int tag = 11;

   public InterfaceMethodrefInfo(int cindex, int ntindex) {
      super(cindex, ntindex);
   }

   public InterfaceMethodrefInfo(DataInputStream in) throws IOException {
      super(in);
   }

   public int getTag() {
      return 11;
   }

   public String getTagName() {
      return "Interface";
   }

   protected int copy2(ConstPool dest, int cindex, int ntindex) {
      return dest.addInterfaceMethodrefInfo(cindex, ntindex);
   }
}
