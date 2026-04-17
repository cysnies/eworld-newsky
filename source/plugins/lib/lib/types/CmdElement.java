package lib.types;

public class CmdElement extends TypeElement {
   private String cmd;

   public CmdElement(String s) {
      super(s);
      this.cmd = s;
   }

   public String getCmd() {
      return this.cmd;
   }

   public int hashCode() {
      return this.cmd.hashCode();
   }

   public boolean equals(Object obj) {
      CmdElement cmdElement = (CmdElement)obj;
      return cmdElement.getCmd().equalsIgnoreCase(this.cmd);
   }
}
