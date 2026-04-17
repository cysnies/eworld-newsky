package land;

public class LandSpawn {
   private long id;
   private long landId;
   private Pos spawn;
   private float yaw;
   private float pitch;

   public LandSpawn() {
   }

   public LandSpawn(long landId, Pos spawn, float yaw, float pitch) {
      this.landId = landId;
      this.spawn = spawn;
      this.yaw = yaw;
      this.pitch = pitch;
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

   public Pos getSpawn() {
      return this.spawn;
   }

   public void setSpawn(Pos spawn) {
      this.spawn = spawn;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      LandSpawn landSpawn = (LandSpawn)obj;
      return landSpawn.id == this.id;
   }
}
