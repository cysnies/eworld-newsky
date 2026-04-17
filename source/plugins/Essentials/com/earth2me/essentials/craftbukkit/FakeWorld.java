package com.earth2me.essentials.craftbukkit;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class FakeWorld implements World {
   private final String name;
   private final World.Environment env;

   public FakeWorld(String string, World.Environment environment) {
      this.name = string;
      this.env = environment;
   }

   public Block getBlockAt(int i, int i1, int i2) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Block getBlockAt(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getBlockTypeIdAt(int i, int i1, int i2) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getBlockTypeIdAt(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getHighestBlockYAt(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getHighestBlockYAt(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Chunk getChunkAt(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Chunk getChunkAt(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Chunk getChunkAt(Block block) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isChunkLoaded(Chunk chunk) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Chunk[] getLoadedChunks() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void loadChunk(Chunk chunk) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isChunkLoaded(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void loadChunk(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean loadChunk(int i, int i1, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunk(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunk(int i, int i1, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunk(int i, int i1, boolean bln, boolean bln1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunkRequest(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunkRequest(int i, int i1, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean regenerateChunk(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean refreshChunk(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Item dropItem(Location lctn, ItemStack is) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Item dropItemNaturally(Location lctn, ItemStack is) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Arrow spawnArrow(Location lctn, Vector vector, float f, float f1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean generateTree(Location lctn, TreeType tt) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean generateTree(Location lctn, TreeType tt, BlockChangeDelegate bcd) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public LivingEntity spawnCreature(Location lctn, CreatureType ct) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public LightningStrike strikeLightning(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public LightningStrike strikeLightningEffect(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getEntities() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getLivingEntities() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getPlayers() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String getName() {
      return this.name;
   }

   public Location getSpawnLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean setSpawnLocation(int i, int i1, int i2) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getTime() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTime(long l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getFullTime() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setFullTime(long l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasStorm() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setStorm(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getWeatherDuration() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setWeatherDuration(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isThundering() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setThundering(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getThunderDuration() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setThunderDuration(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public World.Environment getEnvironment() {
      return this.env;
   }

   public long getSeed() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getPVP() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setPVP(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void save() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean createExplosion(double d, double d1, double d2, float f) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean createExplosion(Location lctn, float f) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public ChunkGenerator getGenerator() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getPopulators() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playEffect(Location lctn, Effect effect, int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playEffect(Location lctn, Effect effect, int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean createExplosion(double d, double d1, double d2, float f, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean createExplosion(Location lctn, float f, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Entity spawn(Location lctn, Class type) throws IllegalArgumentException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public ChunkSnapshot getEmptyChunkSnapshot(int i, int i1, boolean bln, boolean bln1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setSpawnFlags(boolean bln, boolean bln1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getAllowAnimals() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getAllowMonsters() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public UUID getUID() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Block getHighestBlockAt(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Block getHighestBlockAt(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Biome getBiome(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public double getTemperature(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public double getHumidity(int i, int i1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean unloadChunk(Chunk chunk) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getMaxHeight() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getKeepSpawnInMemory() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setKeepSpawnInMemory(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isAutoSave() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setAutoSave(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Difficulty getDifficulty() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setDifficulty(Difficulty difficulty) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getSeaLevel() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public File getWorldFolder() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Collection getEntitiesByClass(Class... types) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public WorldType getWorldType() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void sendPluginMessage(Plugin plugin, String string, byte[] bytes) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Set getListeningPluginChannels() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean canGenerateStructures() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getTicksPerAnimalSpawns() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTicksPerAnimalSpawns(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getTicksPerMonsterSpawns() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTicksPerMonsterSpawns(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Collection getEntitiesByClass(Class type) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Collection getEntitiesByClasses(Class... types) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public LivingEntity spawnCreature(Location arg0, EntityType arg1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playEffect(Location lctn, Effect effect, Object t) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playEffect(Location lctn, Effect effect, Object t, int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setMetadata(String string, MetadataValue mv) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getMetadata(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasMetadata(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void removeMetadata(String string, Plugin plugin) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setBiome(int arg0, int arg1, Biome arg2) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getMonsterSpawnLimit() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setMonsterSpawnLimit(int arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getAnimalSpawnLimit() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setAnimalSpawnLimit(int arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getWaterAnimalSpawnLimit() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setWaterAnimalSpawnLimit(int arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Entity spawnEntity(Location lctn, EntityType et) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isChunkInUse(int x, int z) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public FallingBlock spawnFallingBlock(Location location, Material material, byte data) throws IllegalArgumentException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public FallingBlock spawnFallingBlock(Location location, int blockId, byte blockData) throws IllegalArgumentException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getAmbientSpawnLimit() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setAmbientSpawnLimit(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String[] getGameRules() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String getGameRuleValue(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean setGameRuleValue(String string, String string1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isGameRule(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean createExplosion(double d, double d1, double d2, float f, boolean bln, boolean bln1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
