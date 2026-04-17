package com.sk89q.worldedit.blocks;

import com.sk89q.util.StringUtil;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public enum ItemType {
   AIR(0, "Air", "air"),
   STONE(1, "Stone", new String[]{"stone", "rock"}),
   GRASS(2, "Grass", "grass"),
   DIRT(3, "Dirt", "dirt"),
   COBBLESTONE(4, "Cobblestone", new String[]{"cobblestone", "cobble"}),
   WOOD(5, "Wood", new String[]{"wood", "woodplank", "plank", "woodplanks", "planks"}),
   SAPLING(6, "Sapling", new String[]{"sapling", "seedling"}),
   BEDROCK(7, "Bedrock", new String[]{"adminium", "bedrock"}),
   WATER(8, "Water", new String[]{"watermoving", "movingwater", "flowingwater", "waterflowing"}),
   STATIONARY_WATER(9, "Water (stationary)", new String[]{"water", "waterstationary", "stationarywater", "stillwater"}),
   LAVA(10, "Lava", new String[]{"lavamoving", "movinglava", "flowinglava", "lavaflowing"}),
   STATIONARY_LAVA(11, "Lava (stationary)", new String[]{"lava", "lavastationary", "stationarylava", "stilllava"}),
   SAND(12, "Sand", "sand"),
   GRAVEL(13, "Gravel", "gravel"),
   GOLD_ORE(14, "Gold ore", "goldore"),
   IRON_ORE(15, "Iron ore", "ironore"),
   COAL_ORE(16, "Coal ore", "coalore"),
   LOG(17, "Log", new String[]{"log", "tree", "pine", "oak", "birch", "redwood"}),
   LEAVES(18, "Leaves", new String[]{"leaves", "leaf"}),
   SPONGE(19, "Sponge", "sponge"),
   GLASS(20, "Glass", "glass"),
   LAPIS_LAZULI_ORE(21, "Lapis lazuli ore", new String[]{"lapislazuliore", "blueore", "lapisore"}),
   LAPIS_LAZULI(22, "Lapis lazuli", new String[]{"lapislazuli", "lapislazuliblock", "bluerock"}),
   DISPENSER(23, "Dispenser", "dispenser"),
   SANDSTONE(24, "Sandstone", "sandstone"),
   NOTE_BLOCK(25, "Note block", new String[]{"musicblock", "noteblock", "note", "music", "instrument"}),
   BED(26, "Bed", "bed"),
   POWERED_RAIL(27, "Powered Rail", new String[]{"poweredrail", "boosterrail", "poweredtrack", "boostertrack", "booster"}),
   DETECTOR_RAIL(28, "Detector Rail", new String[]{"detectorrail", "detector"}),
   PISTON_STICKY_BASE(29, "Sticky Piston", "stickypiston"),
   WEB(30, "Web", new String[]{"web", "spiderweb"}),
   LONG_GRASS(31, "Long grass", new String[]{"longgrass", "tallgrass"}),
   DEAD_BUSH(32, "Shrub", new String[]{"deadbush", "shrub", "deadshrub", "tumbleweed"}),
   PISTON_BASE(33, "Piston", "piston"),
   PISTON_EXTENSION(34, "Piston extension", new String[]{"pistonextendsion", "pistonhead"}),
   CLOTH(35, "Wool", new String[]{"cloth", "wool"}),
   PISTON_MOVING_PIECE(36, "Piston moving piece", "movingpiston"),
   YELLOW_FLOWER(37, "Yellow flower", new String[]{"yellowflower", "flower"}),
   RED_FLOWER(38, "Red rose", new String[]{"redflower", "redrose", "rose"}),
   BROWN_MUSHROOM(39, "Brown mushroom", new String[]{"brownmushroom", "mushroom"}),
   RED_MUSHROOM(40, "Red mushroom", "redmushroom"),
   GOLD_BLOCK(41, "Gold block", new String[]{"gold", "goldblock"}),
   IRON_BLOCK(42, "Iron block", new String[]{"iron", "ironblock"}),
   DOUBLE_STEP(43, "Double step", new String[]{"doubleslab", "doublestoneslab", "doublestep"}),
   STEP(44, "Step", new String[]{"slab", "stoneslab", "step", "halfstep"}),
   BRICK(45, "Brick", new String[]{"brick", "brickblock"}),
   TNT(46, "TNT", new String[]{"tnt", "c4", "explosive"}),
   BOOKCASE(47, "Bookcase", new String[]{"bookshelf", "bookshelves", "bookcase", "bookcases"}),
   MOSSY_COBBLESTONE(48, "Cobblestone (mossy)", new String[]{"mossycobblestone", "mossstone", "mossystone", "mosscobble", "mossycobble", "moss", "mossy", "sossymobblecone"}),
   OBSIDIAN(49, "Obsidian", "obsidian"),
   TORCH(50, "Torch", new String[]{"torch", "light", "candle"}),
   FIRE(51, "Fire", new String[]{"fire", "flame", "flames"}),
   MOB_SPAWNER(52, "Mob spawner", new String[]{"mobspawner", "spawner"}),
   WOODEN_STAIRS(53, "Wooden stairs", new String[]{"woodstair", "woodstairs", "woodenstair", "woodenstairs"}),
   CHEST(54, "Chest", new String[]{"chest", "storage", "storagechest"}),
   REDSTONE_WIRE(55, "Redstone wire", new String[]{"redstone", "redstoneblock"}),
   DIAMOND_ORE(56, "Diamond ore", "diamondore"),
   DIAMOND_BLOCK(57, "Diamond block", new String[]{"diamond", "diamondblock"}),
   WORKBENCH(58, "Workbench", new String[]{"workbench", "table", "craftingtable", "crafting"}),
   CROPS(59, "Crops", new String[]{"crops", "crop", "plant", "plants"}),
   SOIL(60, "Soil", new String[]{"soil", "farmland"}),
   FURNACE(61, "Furnace", "furnace"),
   BURNING_FURNACE(62, "Furnace (burning)", new String[]{"burningfurnace", "litfurnace"}),
   SIGN_POST(63, "Sign post", new String[]{"sign", "signpost"}),
   WOODEN_DOOR(64, "Wooden door", new String[]{"wooddoor", "woodendoor", "door"}),
   LADDER(65, "Ladder", "ladder"),
   MINECART_TRACKS(66, "Minecart tracks", new String[]{"track", "tracks", "minecrattrack", "minecarttracks", "rails", "rail"}),
   COBBLESTONE_STAIRS(67, "Cobblestone stairs", new String[]{"cobblestonestair", "cobblestonestairs", "cobblestair", "cobblestairs"}),
   WALL_SIGN(68, "Wall sign", "wallsign"),
   LEVER(69, "Lever", new String[]{"lever", "switch", "stonelever", "stoneswitch"}),
   STONE_PRESSURE_PLATE(70, "Stone pressure plate", new String[]{"stonepressureplate", "stoneplate"}),
   IRON_DOOR(71, "Iron Door", "irondoor"),
   WOODEN_PRESSURE_PLATE(72, "Wooden pressure plate", new String[]{"woodpressureplate", "woodplate", "woodenpressureplate", "woodenplate", "plate", "pressureplate"}),
   REDSTONE_ORE(73, "Redstone ore", "redstoneore"),
   GLOWING_REDSTONE_ORE(74, "Glowing redstone ore", "glowingredstoneore"),
   REDSTONE_TORCH_OFF(75, "Redstone torch (off)", new String[]{"redstonetorchoff", "rstorchoff"}),
   REDSTONE_TORCH_ON(76, "Redstone torch (on)", new String[]{"redstonetorch", "redstonetorchon", "rstorchon", "redtorch"}),
   STONE_BUTTON(77, "Stone Button", new String[]{"stonebutton", "button"}),
   SNOW(78, "Snow", "snow"),
   ICE(79, "Ice", "ice"),
   SNOW_BLOCK(80, "Snow block", "snowblock"),
   CACTUS(81, "Cactus", new String[]{"cactus", "cacti"}),
   CLAY(82, "Clay", "clay"),
   SUGAR_CANE(83, "Reed", new String[]{"reed", "cane", "sugarcane", "sugarcanes", "vine", "vines"}),
   JUKEBOX(84, "Jukebox", new String[]{"jukebox", "stereo", "recordplayer"}),
   FENCE(85, "Fence", "fence"),
   PUMPKIN(86, "Pumpkin", "pumpkin"),
   NETHERRACK(87, "Netherrack", new String[]{"redmossycobblestone", "redcobblestone", "redmosstone", "redcobble", "netherstone", "netherrack", "nether", "hellstone"}),
   SOUL_SAND(88, "Soul sand", new String[]{"slowmud", "mud", "soulsand", "hellmud"}),
   GLOWSTONE(89, "Glowstone", new String[]{"brittlegold", "glowstone", "lightstone", "brimstone", "australium"}),
   PORTAL(90, "Portal", "portal"),
   JACK_O_LANTERN(91, "Pumpkin (on)", new String[]{"pumpkinlighted", "pumpkinon", "litpumpkin", "jackolantern"}),
   CAKE(92, "Cake", new String[]{"cake", "cakeblock"}),
   REDSTONE_REPEATER_OFF(93, "Redstone repeater (off)", new String[]{"diodeoff", "redstonerepeater", "repeateroff", "delayeroff"}),
   REDSTONE_REPEATER_ON(94, "Redstone repeater (on)", new String[]{"diodeon", "redstonerepeateron", "repeateron", "delayeron"}),
   LOCKED_CHEST(95, "Locked chest", new String[]{"lockedchest", "steveco", "supplycrate", "valveneedstoworkonep3nottf2kthx"}),
   TRAP_DOOR(96, "Trap door", new String[]{"trapdoor", "hatch", "floordoor"}),
   SILVERFISH_BLOCK(97, "Silverfish block", new String[]{"silverfish", "silver"}),
   STONE_BRICK(98, "Stone brick", new String[]{"stonebrick", "sbrick", "smoothstonebrick"}),
   RED_MUSHROOM_CAP(100, "Red mushroom cap", new String[]{"giantmushroomred", "redgiantmushroom", "redmushroomcap"}),
   BROWN_MUSHROOM_CAP(99, "Brown mushroom cap", new String[]{"giantmushroombrown", "browngiantmushoom", "brownmushroomcap"}),
   IRON_BARS(101, "Iron bars", new String[]{"ironbars", "ironfence"}),
   GLASS_PANE(102, "Glass pane", new String[]{"window", "glasspane", "glasswindow"}),
   MELON_BLOCK(103, "Melon (block)", "melonblock"),
   PUMPKIN_STEM(104, "Pumpkin stem", "pumpkinstem"),
   MELON_STEM(105, "Melon stem", "melonstem"),
   VINE(106, "Vine", new String[]{"vine", "vines", "creepers"}),
   FENCE_GATE(107, "Fence gate", new String[]{"fencegate", "gate"}),
   BRICK_STAIRS(108, "Brick stairs", new String[]{"brickstairs", "bricksteps"}),
   STONE_BRICK_STAIRS(109, "Stone brick stairs", new String[]{"stonebrickstairs", "smoothstonebrickstairs"}),
   MYCELIUM(110, "Mycelium", new String[]{"fungus", "mycel"}),
   LILY_PAD(111, "Lily pad", new String[]{"lilypad", "waterlily"}),
   NETHER_BRICK(112, "Nether brick", "netherbrick"),
   NETHER_BRICK_FENCE(113, "Nether brick fence", new String[]{"netherbrickfence", "netherfence"}),
   NETHER_BRICK_STAIRS(114, "Nether brick stairs", new String[]{"netherbrickstairs", "netherbricksteps", "netherstairs", "nethersteps"}),
   NETHER_WART(115, "Nether wart", new String[]{"netherwart", "netherstalk"}),
   ENCHANTMENT_TABLE(116, "Enchantment table", new String[]{"enchantmenttable", "enchanttable"}),
   BREWING_STAND(117, "Brewing Stand", "brewingstand"),
   CAULDRON(118, "Cauldron", new String[0]),
   END_PORTAL(119, "End Portal", new String[]{"endportal", "blackstuff", "airportal", "weirdblackstuff"}),
   END_PORTAL_FRAME(120, "End Portal Frame", new String[]{"endportalframe", "airportalframe", "crystalblock"}),
   END_STONE(121, "End Stone", new String[]{"endstone", "enderstone", "endersand"}),
   DRAGON_EGG(122, "Dragon Egg", new String[]{"dragonegg", "dragons"}),
   REDSTONE_LAMP_OFF(123, "Redstone lamp (off)", new String[]{"redstonelamp", "redstonelampoff", "rslamp", "rslampoff", "rsglow", "rsglowoff"}),
   REDSTONE_LAMP_ON(124, "Redstone lamp (on)", new String[]{"redstonelampon", "rslampon", "rsglowon"}),
   DOUBLE_WOODEN_STEP(125, "Double wood step", new String[]{"doublewoodslab", "doublewoodstep"}),
   WOODEN_STEP(126, "Wood step", new String[]{"woodenslab", "woodslab", "woodstep", "woodhalfstep"}),
   COCOA_PLANT(127, "Cocoa plant", new String[]{"cocoplant", "cocoaplant"}),
   SANDSTONE_STAIRS(128, "Sandstone stairs", new String[]{"sandstairs", "sandstonestairs"}),
   EMERALD_ORE(129, "Emerald ore", "emeraldore"),
   ENDER_CHEST(130, "Ender chest", "enderchest"),
   TRIPWIRE_HOOK(131, "Tripwire hook", "tripwirehook"),
   TRIPWIRE(132, "Tripwire", new String[]{"tripwire", "string"}),
   EMERALD_BLOCK(133, "Emerald block", new String[]{"emeraldblock", "emerald"}),
   SPRUCE_WOOD_STAIRS(134, "Spruce wood stairs", new String[]{"sprucestairs", "sprucewoodstairs"}),
   BIRCH_WOOD_STAIRS(135, "Birch wood stairs", new String[]{"birchstairs", "birchwoodstairs"}),
   JUNGLE_WOOD_STAIRS(136, "Jungle wood stairs", new String[]{"junglestairs", "junglewoodstairs"}),
   COMMAND_BLOCK(137, "Command block", new String[]{"commandblock", "cmdblock", "command", "cmd"}),
   BEACON(138, "Beacon", new String[]{"beacon", "beaconblock"}),
   COBBLESTONE_WALL(139, "Cobblestone wall", new String[]{"cobblestonewall", "cobblewall"}),
   FLOWER_POT_BLOCK(140, "Flower pot", new String[]{"flowerpot", "plantpot", "pot", "flowerpotblock"}),
   CARROTS_BLOCK(141, "Carrots", new String[]{"carrots", "carrotsplant", "carrotsblock"}),
   POTATOES_BLOCK(142, "Potatoes", new String[]{"patatoes", "potatoesblock"}),
   WOODEN_BUTTON(143, "Wooden button", new String[]{"woodbutton", "woodenbutton"}),
   HEAD_BLOCK(144, "Head", new String[]{"head", "headmount", "mount", "headblock", "mountblock"}),
   ANVIL(145, "Anvil", new String[]{"anvil", "blacksmith"}),
   TRAPPED_CHEST(146, "Trapped Chest", new String[]{"trappedchest", "redstonechest"}),
   PRESSURE_PLATE_LIGHT(147, "Weighted Pressure Plate (Light)", "lightpressureplate"),
   PRESSURE_PLATE_HEAVY(148, "Weighted Pressure Plate (Heavy)", "heavypressureplate"),
   COMPARATOR_OFF(149, "Redstone Comparator (inactive)", new String[]{"redstonecomparator", "comparator"}),
   COMPARATOR_ON(150, "Redstone Comparator (active)", new String[]{"redstonecomparatoron", "comparatoron"}),
   DAYLIGHT_SENSOR(151, "Daylight Sesnor", new String[]{"daylightsensor", "lightsensor"}),
   REDSTONE_BLOCK(152, "Block of Redstone", new String[]{"redstoneblock", "blockofredstone"}),
   QUARTZ_ORE(153, "Nether Quartz Ore", new String[]{"quartzore", "netherquartzore"}),
   HOPPER(154, "Hopper", "hopper"),
   QUARTZ_BLOCK(155, "Block of Quartz", "quartzblock"),
   QUARTZ_STAIRS(156, "Quartz Stairs", "quartzstairs"),
   ACTIVATOR_RAIL(157, "Activator Rail", new String[]{"activatorrail", "tntrail", "activatortrack"}),
   DROPPER(158, "Dropper", "dropper"),
   STAINED_CLAY(159, "Stained clay", "stainedclay"),
   HAY_BLOCK(170, "Hay Block", new String[]{"hayblock", "haybale", "wheatbale"}),
   CARPET(171, "Carpet", "carpet"),
   HARDENED_CLAY(172, "Hardened Clay", new String[]{"hardenedclay", "hardclay"}),
   COAL_BLOCK(173, "Block of Coal", new String[]{"coalblock", "blockofcoal"}),
   IRON_SHOVEL(256, "Iron shovel", "ironshovel"),
   IRON_PICK(257, "Iron pick", new String[]{"ironpick", "ironpickaxe"}),
   IRON_AXE(258, "Iron axe", "ironaxe"),
   FLINT_AND_TINDER(259, "Flint and tinder", new String[]{"flintandtinder", "lighter", "flintandsteel", "flintsteel", "flintandiron", "flintnsteel", "flintniron", "flintntinder"}),
   RED_APPLE(260, "Red apple", new String[]{"redapple", "apple"}),
   BOW(261, "Bow", "bow"),
   ARROW(262, "Arrow", "arrow"),
   COAL(263, "Coal", "coal"),
   DIAMOND(264, "Diamond", "diamond"),
   IRON_BAR(265, "Iron bar", new String[]{"ironbar", "iron"}),
   GOLD_BAR(266, "Gold bar", new String[]{"goldbar", "gold"}),
   IRON_SWORD(267, "Iron sword", "ironsword"),
   WOOD_SWORD(268, "Wooden sword", "woodsword"),
   WOOD_SHOVEL(269, "Wooden shovel", "woodshovel"),
   WOOD_PICKAXE(270, "Wooden pickaxe", new String[]{"woodpick", "woodpickaxe"}),
   WOOD_AXE(271, "Wooden axe", "woodaxe"),
   STONE_SWORD(272, "Stone sword", "stonesword"),
   STONE_SHOVEL(273, "Stone shovel", "stoneshovel"),
   STONE_PICKAXE(274, "Stone pickaxe", new String[]{"stonepick", "stonepickaxe"}),
   STONE_AXE(275, "Stone pickaxe", "stoneaxe"),
   DIAMOND_SWORD(276, "Diamond sword", "diamondsword"),
   DIAMOND_SHOVEL(277, "Diamond shovel", "diamondshovel"),
   DIAMOND_PICKAXE(278, "Diamond pickaxe", new String[]{"diamondpick", "diamondpickaxe"}),
   DIAMOND_AXE(279, "Diamond axe", "diamondaxe"),
   STICK(280, "Stick", "stick"),
   BOWL(281, "Bowl", "bowl"),
   MUSHROOM_SOUP(282, "Mushroom soup", new String[]{"mushroomsoup", "soup", "brbsoup"}),
   GOLD_SWORD(283, "Golden sword", "goldsword"),
   GOLD_SHOVEL(284, "Golden shovel", "goldshovel"),
   GOLD_PICKAXE(285, "Golden pickaxe", new String[]{"goldpick", "goldpickaxe"}),
   GOLD_AXE(286, "Golden axe", "goldaxe"),
   STRING(287, "String", "string"),
   FEATHER(288, "Feather", "feather"),
   SULPHUR(289, "Sulphur", new String[]{"sulphur", "sulfur", "gunpowder"}),
   WOOD_HOE(290, "Wooden hoe", "woodhoe"),
   STONE_HOE(291, "Stone hoe", "stonehoe"),
   IRON_HOE(292, "Iron hoe", "ironhoe"),
   DIAMOND_HOE(293, "Diamond hoe", "diamondhoe"),
   GOLD_HOE(294, "Golden hoe", "goldhoe"),
   SEEDS(295, "Seeds", new String[]{"seeds", "seed"}),
   WHEAT(296, "Wheat", "wheat"),
   BREAD(297, "Bread", "bread"),
   LEATHER_HELMET(298, "Leather helmet", new String[]{"leatherhelmet", "leatherhat"}),
   LEATHER_CHEST(299, "Leather chestplate", new String[]{"leatherchest", "leatherchestplate", "leathervest", "leatherbreastplate", "leatherplate", "leathercplate", "leatherbody"}),
   LEATHER_PANTS(300, "Leather pants", new String[]{"leatherpants", "leathergreaves", "leatherlegs", "leatherleggings", "leatherstockings", "leatherbreeches"}),
   LEATHER_BOOTS(301, "Leather boots", new String[]{"leatherboots", "leathershoes", "leatherfoot", "leatherfeet"}),
   CHAINMAIL_HELMET(302, "Chainmail helmet", new String[]{"chainmailhelmet", "chainmailhat"}),
   CHAINMAIL_CHEST(303, "Chainmail chestplate", new String[]{"chainmailchest", "chainmailchestplate", "chainmailvest", "chainmailbreastplate", "chainmailplate", "chainmailcplate", "chainmailbody"}),
   CHAINMAIL_PANTS(304, "Chainmail pants", new String[]{"chainmailpants", "chainmailgreaves", "chainmaillegs", "chainmailleggings", "chainmailstockings", "chainmailbreeches"}),
   CHAINMAIL_BOOTS(305, "Chainmail boots", new String[]{"chainmailboots", "chainmailshoes", "chainmailfoot", "chainmailfeet"}),
   IRON_HELMET(306, "Iron helmet", new String[]{"ironhelmet", "ironhat"}),
   IRON_CHEST(307, "Iron chestplate", new String[]{"ironchest", "ironchestplate", "ironvest", "ironbreastplate", "ironplate", "ironcplate", "ironbody"}),
   IRON_PANTS(308, "Iron pants", new String[]{"ironpants", "irongreaves", "ironlegs", "ironleggings", "ironstockings", "ironbreeches"}),
   IRON_BOOTS(309, "Iron boots", new String[]{"ironboots", "ironshoes", "ironfoot", "ironfeet"}),
   DIAMOND_HELMET(310, "Diamond helmet", new String[]{"diamondhelmet", "diamondhat"}),
   DIAMOND_CHEST(311, "Diamond chestplate", new String[]{"diamondchest", "diamondchestplate", "diamondvest", "diamondbreastplate", "diamondplate", "diamondcplate", "diamondbody"}),
   DIAMOND_PANTS(312, "Diamond pants", new String[]{"diamondpants", "diamondgreaves", "diamondlegs", "diamondleggings", "diamondstockings", "diamondbreeches"}),
   DIAMOND_BOOTS(313, "Diamond boots", new String[]{"diamondboots", "diamondshoes", "diamondfoot", "diamondfeet"}),
   GOLD_HELMET(314, "Gold helmet", new String[]{"goldhelmet", "goldhat"}),
   GOLD_CHEST(315, "Gold chestplate", new String[]{"goldchest", "goldchestplate", "goldvest", "goldbreastplate", "goldplate", "goldcplate", "goldbody"}),
   GOLD_PANTS(316, "Gold pants", new String[]{"goldpants", "goldgreaves", "goldlegs", "goldleggings", "goldstockings", "goldbreeches"}),
   GOLD_BOOTS(317, "Gold boots", new String[]{"goldboots", "goldshoes", "goldfoot", "goldfeet"}),
   FLINT(318, "Flint", "flint"),
   RAW_PORKCHOP(319, "Raw porkchop", new String[]{"rawpork", "rawporkchop", "rawbacon", "baconstrips", "rawmeat"}),
   COOKED_PORKCHOP(320, "Cooked porkchop", new String[]{"pork", "cookedpork", "cookedporkchop", "cookedbacon", "bacon", "meat"}),
   PAINTING(321, "Painting", "painting"),
   GOLD_APPLE(322, "Golden apple", new String[]{"goldapple", "goldenapple"}),
   SIGN(323, "Wooden sign", "sign"),
   WOODEN_DOOR_ITEM(324, "Wooden door", new String[]{"wooddoor", "door"}),
   BUCKET(325, "Bucket", new String[]{"bucket", "bukkit"}),
   WATER_BUCKET(326, "Water bucket", new String[]{"waterbucket", "waterbukkit"}),
   LAVA_BUCKET(327, "Lava bucket", new String[]{"lavabucket", "lavabukkit"}),
   MINECART(328, "Minecart", new String[]{"minecart", "cart"}),
   SADDLE(329, "Saddle", "saddle"),
   IRON_DOOR_ITEM(330, "Iron door", "irondoor"),
   REDSTONE_DUST(331, "Redstone dust", new String[]{"redstonedust", "reddust", "redstone", "dust", "wire"}),
   SNOWBALL(332, "Snowball", "snowball"),
   WOOD_BOAT(333, "Wooden boat", new String[]{"woodboat", "woodenboat", "boat"}),
   LEATHER(334, "Leather", new String[]{"leather", "cowhide"}),
   MILK_BUCKET(335, "Milk bucket", new String[]{"milkbucket", "milk", "milkbukkit"}),
   BRICK_BAR(336, "Brick", "brickbar"),
   CLAY_BALL(337, "Clay", "clay"),
   SUGAR_CANE_ITEM(338, "Sugar cane", new String[]{"sugarcane", "reed", "reeds"}),
   PAPER(339, "Paper", "paper"),
   BOOK(340, "Book", "book"),
   SLIME_BALL(341, "Slime ball", new String[]{"slimeball", "slime"}),
   STORAGE_MINECART(342, "Minecart with Chest", new String[]{"storageminecart", "storagecart", "minecartwithchest", "minecartchest", "chestminecart"}),
   POWERED_MINECART(343, "Minecart with Furnace", new String[]{"poweredminecart", "poweredcart", "minecartwithfurnace", "minecartfurnace", "furnaceminecart"}),
   EGG(344, "Egg", "egg"),
   COMPASS(345, "Compass", "compass"),
   FISHING_ROD(346, "Fishing rod", new String[]{"fishingrod", "fishingpole"}),
   WATCH(347, "Watch", new String[]{"watch", "clock", "timer"}),
   LIGHTSTONE_DUST(348, "Glowstone dust", new String[]{"lightstonedust", "glowstonedone", "brightstonedust", "brittlegolddust", "brimstonedust"}),
   RAW_FISH(349, "Raw fish", new String[]{"rawfish", "fish"}),
   COOKED_FISH(350, "Cooked fish", "cookedfish"),
   INK_SACK(351, "Ink sac", new String[]{"inksac", "ink", "dye", "inksack"}),
   BONE(352, "Bone", "bone"),
   SUGAR(353, "Sugar", "sugar"),
   CAKE_ITEM(354, "Cake", "cake"),
   BED_ITEM(355, "Bed", "bed"),
   REDSTONE_REPEATER(356, "Redstone repeater", new String[]{"redstonerepeater", "diode", "delayer", "repeater"}),
   COOKIE(357, "Cookie", "cookie"),
   MAP(358, "Map", "map"),
   SHEARS(359, "Shears", new String[]{"shears", "scissors"}),
   MELON(360, "Melon Slice", new String[]{"melon", "melonslice"}),
   PUMPKIN_SEEDS(361, "Pumpkin seeds", new String[]{"pumpkinseed", "pumpkinseeds"}),
   MELON_SEEDS(362, "Melon seeds", new String[]{"melonseed", "melonseeds"}),
   RAW_BEEF(363, "Raw beef", new String[]{"rawbeef", "rawcow", "beef"}),
   COOKED_BEEF(364, "Steak", new String[]{"steak", "cookedbeef", "cookedcow"}),
   RAW_CHICKEN(365, "Raw chicken", "rawchicken"),
   COOKED_CHICKEN(366, "Cooked chicken", new String[]{"cookedchicken", "chicken", "grilledchicken"}),
   ROTTEN_FLESH(367, "Rotten flesh", new String[]{"rottenflesh", "zombiemeat", "flesh"}),
   ENDER_PEARL(368, "Ender pearl", new String[]{"pearl", "enderpearl"}),
   BLAZE_ROD(369, "Blaze rod", "blazerod"),
   GHAST_TEAR(370, "Ghast tear", "ghasttear"),
   GOLD_NUGGET(371, "Gold nuggest", "goldnugget"),
   NETHER_WART_ITEM(372, "Nether wart", new String[]{"netherwart", "netherwartseed"}),
   POTION(373, "Potion", "potion"),
   GLASS_BOTTLE(374, "Glass bottle", "glassbottle"),
   SPIDER_EYE(375, "Spider eye", "spidereye"),
   FERMENTED_SPIDER_EYE(376, "Fermented spider eye", new String[]{"fermentedspidereye", "fermentedeye"}),
   BLAZE_POWDER(377, "Blaze powder", "blazepowder"),
   MAGMA_CREAM(378, "Magma cream", "magmacream"),
   BREWING_STAND_ITEM(379, "Brewing stand", "brewingstand"),
   CAULDRON_ITEM(380, "Cauldron", "cauldron"),
   EYE_OF_ENDER(381, "Eye of Ender", new String[]{"eyeofender", "endereye"}),
   GLISTERING_MELON(382, "Glistering Melon", new String[]{"glisteringmelon", "goldmelon"}),
   SPAWN_EGG(383, "Spawn Egg", new String[]{"spawnegg", "spawn", "mobspawnegg"}),
   BOTTLE_O_ENCHANTING(384, "Bottle o' Enchanting", new String[]{"expbottle", "bottleoenchanting", "experiencebottle", "exppotion", "experiencepotion"}),
   FIRE_CHARGE(385, "Fire Charge", new String[]{"firecharge", "firestarter", "firerock"}),
   BOOK_AND_QUILL(386, "Book and Quill", new String[]{"bookandquill", "quill", "writingbook"}),
   WRITTEN_BOOK(387, "Written Book", "writtenbook"),
   EMERALD(388, "Emerald", new String[]{"emeraldingot", "emerald"}),
   ITEM_FRAME(389, "Item frame", new String[]{"itemframe", "frame", "itempainting"}),
   FLOWER_POT(390, "Flower pot", new String[]{"flowerpot", "plantpot", "pot"}),
   CARROT(391, "Carrot", "carrot"),
   POTATO(392, "Potato", "potato"),
   BAKED_POTATO(393, "Baked potato", new String[]{"bakedpotato", "potatobaked"}),
   POISONOUS_POTATO(394, "Poisonous potato", new String[]{"poisonpotato", "poisonouspotato"}),
   BLANK_MAP(395, "Blank map", new String[]{"blankmap", "emptymap"}),
   GOLDEN_CARROT(396, "Golden carrot", new String[]{"goldencarrot", "goldcarrot"}),
   HEAD(397, "Head", new String[]{"skull", "head", "headmount", "mount"}),
   CARROT_ON_A_STICK(398, "Carrot on a stick", new String[]{"carrotonastick", "carrotonstick", "stickcarrot", "carrotstick"}),
   NETHER_STAR(399, "Nether star", new String[]{"netherstar", "starnether"}),
   PUMPKIN_PIE(400, "Pumpkin pie", "pumpkinpie"),
   FIREWORK_ROCKET(401, "Firework rocket", new String[]{"firework", "rocket"}),
   FIREWORK_STAR(402, "Firework star", new String[]{"fireworkstar", "fireworkcharge"}),
   ENCHANTED_BOOK(403, "Enchanted book", new String[]{"enchantedbook", "spellbook", "enchantedtome", "tome"}),
   COMPARATOR(404, "Comparator", new String[]{"comparator", "capacitor"}),
   NETHER_BRICK_ITEM(405, "Nether Brick (item)", "netherbrickitem"),
   NETHER_QUARTZ(406, "Nether Quartz", new String[]{"netherquartz", "quartz"}),
   TNT_MINECART(407, "Minecart with TNT", new String[]{"minecraftwithtnt", "tntminecart", "minecarttnt"}),
   HOPPER_MINECART(408, "Minecart with Hopper", new String[]{"minecraftwithhopper", "hopperminecart", "minecarthopper"}),
   HORSE_ARMOR_IRON(417, "Iron Horse Armor", new String[]{"ironhorsearmor", "ironbarding"}),
   HORSE_ARMOR_GOLD(418, "Gold Horse Armor", new String[]{"goldhorsearmor", "goldbarding"}),
   HORSE_ARMOR_DIAMOND(419, "Diamond Horse Armor", new String[]{"diamondhorsearmor", "diamondbarding"}),
   LEAD(420, "Lead", new String[]{"lead", "leash"}),
   NAME_TAG(421, "Name Tag", "nametag"),
   DISC_13(2256, "Music Disc - 13", "disc_13"),
   DISC_CAT(2257, "Music Disc - Cat", "disc_cat"),
   DISC_BLOCKS(2258, "Music Disc - blocks", "disc_blocks"),
   DISC_CHIRP(2259, "Music Disc - chirp", "disc_chirp"),
   DISC_FAR(2260, "Music Disc - far", "disc_far"),
   DISC_MALL(2261, "Music Disc - mall", "disc_mall"),
   DISC_MELLOHI(2262, "Music Disc - mellohi", "disc_mellohi"),
   DISC_STAL(2263, "Music Disc - stal", "disc_stal"),
   DISC_STRAD(2264, "Music Disc - strad", "disc_strad"),
   DISC_WARD(2265, "Music Disc - ward", "disc_ward"),
   DISC_11(2266, "Music Disc - 11", "disc_11"),
   DISC_WAIT(2267, "Music Disc - wait", "disc_wait"),
   /** @deprecated */
   @Deprecated
   GOLD_RECORD(2256, "Gold Record", new String[]{"goldrecord", "golddisc"}),
   /** @deprecated */
   @Deprecated
   GREEN_RECORD(2257, "Green Record", new String[]{"greenrecord", "greenddisc"});

   private static final Map ids = new HashMap();
   private static final Map lookup = new LinkedHashMap();
   private final int id;
   private final String name;
   private final String[] lookupKeys;
   private static final Set shouldNotStack;
   private static final Set usesDamageValue;

   private ItemType(int id, String name, String lookupKey) {
      this.id = id;
      this.name = name;
      this.lookupKeys = new String[]{lookupKey};
   }

   private ItemType(int id, String name, String... lookupKeys) {
      this.id = id;
      this.name = name;
      this.lookupKeys = lookupKeys;
   }

   public static ItemType fromID(int id) {
      return (ItemType)ids.get(id);
   }

   public static String toName(int id) {
      ItemType type = (ItemType)ids.get(id);
      return type != null ? type.getName() : "#" + id;
   }

   public static String toHeldName(int id) {
      if (id == 0) {
         return "Hand";
      } else {
         ItemType type = (ItemType)ids.get(id);
         return type != null ? type.getName() : "#" + id;
      }
   }

   public static ItemType lookup(String name) {
      return lookup(name, true);
   }

   public static ItemType lookup(String name, boolean fuzzy) {
      try {
         return fromID(Integer.parseInt(name));
      } catch (NumberFormatException var3) {
         return (ItemType)StringUtil.lookup(lookup, name, fuzzy);
      }
   }

   public int getID() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String[] getAliases() {
      return this.lookupKeys;
   }

   public static boolean shouldNotStack(int id) {
      return shouldNotStack.contains(id);
   }

   public static boolean usesDamageValue(int id) {
      return usesDamageValue.contains(id);
   }

   static {
      for(ItemType type : EnumSet.allOf(ItemType.class)) {
         ids.put(type.id, type);

         for(String key : type.lookupKeys) {
            lookup.put(key, type);
         }
      }

      shouldNotStack = new HashSet();
      shouldNotStack.add(256);
      shouldNotStack.add(257);
      shouldNotStack.add(258);
      shouldNotStack.add(259);
      shouldNotStack.add(261);
      shouldNotStack.add(267);
      shouldNotStack.add(268);
      shouldNotStack.add(269);
      shouldNotStack.add(270);
      shouldNotStack.add(271);
      shouldNotStack.add(272);
      shouldNotStack.add(273);
      shouldNotStack.add(274);
      shouldNotStack.add(275);
      shouldNotStack.add(276);
      shouldNotStack.add(277);
      shouldNotStack.add(278);
      shouldNotStack.add(279);
      shouldNotStack.add(281);
      shouldNotStack.add(283);
      shouldNotStack.add(284);
      shouldNotStack.add(285);
      shouldNotStack.add(286);
      shouldNotStack.add(290);
      shouldNotStack.add(291);
      shouldNotStack.add(292);
      shouldNotStack.add(293);
      shouldNotStack.add(294);
      shouldNotStack.add(298);
      shouldNotStack.add(299);
      shouldNotStack.add(300);
      shouldNotStack.add(301);
      shouldNotStack.add(303);
      shouldNotStack.add(302);
      shouldNotStack.add(305);
      shouldNotStack.add(304);
      shouldNotStack.add(306);
      shouldNotStack.add(307);
      shouldNotStack.add(308);
      shouldNotStack.add(309);
      shouldNotStack.add(310);
      shouldNotStack.add(312);
      shouldNotStack.add(311);
      shouldNotStack.add(313);
      shouldNotStack.add(314);
      shouldNotStack.add(315);
      shouldNotStack.add(316);
      shouldNotStack.add(317);
      shouldNotStack.add(324);
      shouldNotStack.add(326);
      shouldNotStack.add(327);
      shouldNotStack.add(328);
      shouldNotStack.add(329);
      shouldNotStack.add(330);
      shouldNotStack.add(333);
      shouldNotStack.add(335);
      shouldNotStack.add(342);
      shouldNotStack.add(343);
      shouldNotStack.add(347);
      shouldNotStack.add(354);
      shouldNotStack.add(355);
      shouldNotStack.add(358);
      shouldNotStack.add(359);
      shouldNotStack.add(397);
      shouldNotStack.add(401);
      shouldNotStack.add(402);
      shouldNotStack.add(403);
      shouldNotStack.add(407);
      shouldNotStack.add(408);
      shouldNotStack.add(417);
      shouldNotStack.add(418);
      shouldNotStack.add(419);
      shouldNotStack.add(2256);
      shouldNotStack.add(2257);
      shouldNotStack.add(2258);
      shouldNotStack.add(2259);
      shouldNotStack.add(2260);
      shouldNotStack.add(2261);
      shouldNotStack.add(2262);
      shouldNotStack.add(2263);
      shouldNotStack.add(2264);
      shouldNotStack.add(2265);
      shouldNotStack.add(2266);
      shouldNotStack.add(2267);
      usesDamageValue = new HashSet();
      usesDamageValue.add(5);
      usesDamageValue.add(6);
      usesDamageValue.add(17);
      usesDamageValue.add(18);
      usesDamageValue.add(24);
      usesDamageValue.add(31);
      usesDamageValue.add(35);
      usesDamageValue.add(43);
      usesDamageValue.add(44);
      usesDamageValue.add(97);
      usesDamageValue.add(98);
      usesDamageValue.add(99);
      usesDamageValue.add(100);
      usesDamageValue.add(125);
      usesDamageValue.add(126);
      usesDamageValue.add(139);
      usesDamageValue.add(145);
      usesDamageValue.add(155);
      usesDamageValue.add(159);
      usesDamageValue.add(171);
      usesDamageValue.add(263);
      usesDamageValue.add(351);
      usesDamageValue.add(373);
      usesDamageValue.add(383);
      usesDamageValue.add(358);
      usesDamageValue.add(397);
      usesDamageValue.add(322);
   }
}
