package land;

public class LandCmd {
   private long id;
   private long landId;
   private String cmd;

   public LandCmd() {
   }

   public LandCmd(long landId, String cmd) {
      this.landId = landId;
      this.cmd = cmd;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getLandId() {
      return this.landId;
   }

   public void setLandId(long landId) {
      this.landId = landId;
   }

   public String getCmd() {
      return this.cmd;
   }

   public void setCmd(String cmd) {
      this.cmd = cmd;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      LandCmd landSpawn = (LandCmd)obj;
      return landSpawn.id == this.id;
   }
}
