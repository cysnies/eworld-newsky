package com.sk89q.worldedit;

public class Location {
   private final LocalWorld world;
   private final Vector position;
   private final float yaw;
   private final float pitch;

   public Location(LocalWorld world, Vector position) {
      this(world, position, 0.0F, 0.0F);
   }

   public Location(LocalWorld world, Vector position, float yaw, float pitch) {
      this.world = world;
      this.position = position;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public LocalWorld getWorld() {
      return this.world;
   }

   public Vector getPosition() {
      return this.position;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public Location setAngles(float yaw, float pitch) {
      return new Location(this.world, this.position, yaw, pitch);
   }

   public Location setPosition(Vector position) {
      return new Location(this.world, position, this.yaw, this.pitch);
   }

   public Location add(Vector other) {
      return this.setPosition(this.position.add(other));
   }

   public Location add(double x, double y, double z) {
      return this.setPosition(this.position.add(x, y, z));
   }

   public Vector getDirection() {
      double yawRadians = Math.toRadians((double)this.yaw);
      double pitchRadians = Math.toRadians((double)this.pitch);
      double y = -Math.sin(pitchRadians);
      double h = Math.cos(pitchRadians);
      double x = -h * Math.sin(yawRadians);
      double z = h * Math.cos(yawRadians);
      return new Vector(x, y, z);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Location)) {
         return false;
      } else {
         Location location = (Location)obj;
         if (!this.world.equals(location.world)) {
            return false;
         } else {
            return this.position.equals(location.position);
         }
      }
   }

   public int hashCode() {
      return this.position.hashCode() + 19 * this.world.hashCode();
   }

   public String toString() {
      return "World: " + this.world.getName() + ", Coordinates: " + this.position.toString() + ", Yaw: " + this.yaw + ", Pitch: " + this.pitch;
   }

   public static Location fromLookAt(LocalWorld world, Vector start, Vector lookAt) {
      Vector diff = lookAt.subtract(start);
      return fromEye(world, start, diff);
   }

   public static Location fromEye(LocalWorld world, Vector start, Vector eye) {
      double eyeX = eye.getX();
      double eyeZ = eye.getZ();
      float yaw = (float)Math.toDegrees(Math.atan2(-eyeX, eyeZ));
      double length = Math.sqrt(eyeX * eyeX + eyeZ * eyeZ);
      float pitch = (float)Math.toDegrees(Math.atan2(-eye.getY(), length));
      return new Location(world, start, yaw, pitch);
   }
}
