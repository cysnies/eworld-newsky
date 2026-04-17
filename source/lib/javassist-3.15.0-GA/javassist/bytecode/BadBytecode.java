package javassist.bytecode;

public class BadBytecode extends Exception {
   public BadBytecode(int opcode) {
      super("bytecode " + opcode);
   }

   public BadBytecode(String msg) {
      super(msg);
   }

   public BadBytecode(String msg, Throwable cause) {
      super(msg, cause);
   }
}
