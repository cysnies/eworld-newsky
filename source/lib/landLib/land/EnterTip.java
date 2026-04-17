package land;

public class EnterTip {
   private long id;
   private long landId;
   private String tip;

   public EnterTip() {
   }

   public EnterTip(long landId, String tip) {
      this.landId = landId;
      this.tip = tip;
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

   public String getTip() {
      return this.tip;
   }

   public void setTip(String tip) {
      this.tip = tip;
   }
}
