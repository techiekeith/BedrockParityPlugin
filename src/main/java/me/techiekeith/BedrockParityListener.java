package me.techiekeith;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BedrockParityListener implements Listener {

    private final Random random = new Random();

    /**
     * The list of block types that qualify as "small flowers" for Bone Meal purposes.
     */
    private final Material[] smallFlowers = {
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
    };

    /**
     * The number of attempts to randomly place a nearby flower.
     */
    private static final int maxFlowerPlacementAttempts = 32;

    /**
     * Represents the upper bound on the random number used to determine whether to randomly move up or down when
     * selecting a nearby candidate location for placing flowers. The actual value generated is
     * {@code Random.nextInt(yOffsetDieRoll) - 1}, and a random move up or down happens only if the result is 1 or -1.
     */
    private static final int yOffsetDieRoll = 10;

    /**
     * Checks for Cod, PufferFish, Salmon and TropicalFish deaths and modifies drops where applicable.
     *
     * @param event the entity death event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Cod
                || entity instanceof PufferFish
                || entity instanceof Salmon
                || entity instanceof TropicalFish) {
            substituteBoneForBoneMeal(entity, event.getDrops());
        }
    }

    /**
     * Checks for Bone Meal usage and modifies behaviour where applicable.
     *
     * @param event the player interaction event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        GameMode gameMode = event.getPlayer().getGameMode();
        if (item != null
                && block != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && item.getType() == Material.BONE_MEAL) {
            Material target = block.getType();
            if (target == Material.SUGAR_CANE) {
                applyBoneMealToSugarCane(item, block, gameMode);
            } else if (isSmallFlower(target)) {
                applyBoneMealToSmallFlower(item, block, gameMode);
            }
        }
    }

    /**
     * Replaces all instances of Bone Meal with Bone in a list of item drops.
     *
     * @param drops the list of item drops
     */
    private void substituteBoneForBoneMeal(LivingEntity entity, List<ItemStack> drops) {
        if (drops.removeIf(item -> item.getType() == Material.BONE_MEAL)) {
            log("substituteBoneForBoneMeal", entity.getName() + ": substituted Bone for Bone Meal");
            drops.add(new ItemStack(Material.BONE));
        }
    }

    /**
     * Replaces 1-2 Air blocks with Sugar Cane blocks above the one to which the player has applied Bone Meal.
     *
     * @param item     the stack of Bone Meal
     * @param block    the block to which Bone Meal is being applied
     * @param gameMode the game mode
     */
    private void applyBoneMealToSugarCane(ItemStack item, Block block, GameMode gameMode) {
        World world = block.getWorld();
        Location location = block.getLocation().clone();
        location.add(0, 1, 0);
        Block blockAbove = world.getBlockAt(location);
        if (blockAbove.getType() == Material.AIR) {
            log("applyBoneMealToSugarCane",
                    "Applied to " + block.getType().getTranslationKey() + " at " + locationString(block.getLocation()));
            blockAbove.setType(Material.SUGAR_CANE);
            location.add(0, 1, 0);
            blockAbove = world.getBlockAt(location);
            if (blockAbove.getType() == Material.AIR) {
                blockAbove.setType(Material.SUGAR_CANE);
            }
            playBoneMealEffect(block);
            consumeItem(item, gameMode);
        }
    }

    /**
     * Determines whether the targeted block type is a small flower.
     *
     * @return true if the block type is a small flower
     * @see BedrockParityListener#smallFlowers
     */
    private boolean isSmallFlower(Material blockType) {
        return Arrays.stream(smallFlowers).anyMatch(smallFlower -> smallFlower == blockType);
    }

    /**
     * Adds flowers randomly on grass blocks surrounding the one to which the player has applied Bone Meal.
     *
     * @param item     the stack of Bone Meal
     * @param block    the block to which Bone Meal is being applied
     * @param gameMode the game mode
     */
    private void applyBoneMealToSmallFlower(ItemStack item, Block block, GameMode gameMode) {
        if (placeDuplicateFlowersInWorld(block)) {
            playBoneMealEffect(block);
            consumeItem(item, gameMode);
        }
    }

    /**
     * Adds flowers randomly on grass blocks surrounding a specified block.
     *
     * @param block the block around which to place flowers
     * @return true if one or more flowers were successfully placed
     */
    private boolean placeDuplicateFlowersInWorld(Block block) {
        log("placeDuplicateFlowersInWorld",
                "Applied to " + block.getType().getTranslationKey() + " at " + locationString(block.getLocation()));
        boolean success = false;
        for (int i = 0; i < maxFlowerPlacementAttempts; i++) {
            success |= tryPlaceDuplicateFlowerRandomlyNearby(block);
        }
        return success;
    }

    /**
     * Tries to place a flower randomly on top of a grass block near a specified block.
     *
     * @param block the block around which to place a flower
     * @return true if a flower was successfully placed
     */
    private boolean tryPlaceDuplicateFlowerRandomlyNearby(Block block) {
        Location nearbyLocation = pickRandomNearbyLocation(block);
        Material flowerType = getDuplicateFlowerType(block.getType());
        World world = block.getWorld();
        Block maybeGrassBlock = world.getBlockAt(nearbyLocation);
        nearbyLocation.add(0, 1, 0);
        Block maybeAirBlock = world.getBlockAt(nearbyLocation);
        boolean success = maybeGrassBlock.getType() == Material.GRASS_BLOCK && maybeAirBlock.getType() == Material.AIR;
        if (success) {
            maybeAirBlock.setType(flowerType);
            log("tryPlaceDuplicateFlowerRandomly",
                    "Added " + flowerType.getTranslationKey() + " at " + locationString(nearbyLocation));
        }
        return success;
    }

    /**
     * Randomly picks a candidate location for a new flower to place near a specified block.
     *
     * @param block the block around which to place a flower
     * @return the candidate location
     */
    private Location pickRandomNearbyLocation(Block block) {
        Location location = block.getLocation().clone();
        for (int i = 0; i < 3; i++) {
            int moveY = random.nextInt(yOffsetDieRoll) - 1;
            if (moveY > 1) {
                moveY = 0;
            }
            location.add(random.nextInt(3) - 1, moveY, random.nextInt(3) - 1);
        }
        return location;
    }

    /**
     * Determines the flower to place. For Dandelion and Poppy flowers, there is a 15% chance that the flower placed
     * will be different to that to which Bone Meal is being applied.
     *
     * @param flowerType the type of flower to which Bone Meal is being applied
     */
    private Material getDuplicateFlowerType(Material flowerType) {
        if (flowerType == Material.DANDELION && random.nextInt(20) < 3) {
            // 15% chance of Poppy instead (ratio is an estimate based on observations)
            return Material.POPPY;
        } else if (flowerType == Material.POPPY && random.nextInt(20) < 3) {
            // 15% chance of Dandelion instead (ratio is an estimate based on observations)
            return Material.DANDELION;
        }
        return flowerType;
    }

    /**
     * Plays the Bone Meal effect (particles and sound) on the block to which Bone Meal is being applied.
     *
     * @param block the block to which Bone Meal is being applied
     */
    private void playBoneMealEffect(Block block) {
        World world = block.getWorld();
        Location location = block.getLocation();
        world.playEffect(location, Effect.VILLAGER_PLANT_GROW, 10);
        world.playSound(location, Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
    }

    /**
     * Consumes an item in a stack (unless the game mode is "Creative").
     *
     * @param item     the item stack
     * @param gameMode the game mode
     */
    private void consumeItem(ItemStack item, GameMode gameMode) {
        if (gameMode != GameMode.CREATIVE) {
            int remainingAmount = item.getAmount() - 1;
            log("consumeItem", "Reduced " + item.getTranslationKey() + " amount to " + remainingAmount);
            item.setAmount(remainingAmount);
        }
    }

    /**
     * Logs a message to the Bukkit logger, with a prefix showing where the message came from.
     *
     * @param methodName the name of the method that logged the message
     * @param message    the log message
     */
    private void log(String methodName, String message) {
        Bukkit.getLogger().info("[" + getClass().getName() + "]: " + methodName + ":: " + message);
    }

    /**
     * Displays a location as a string in the form "X, Y, Z".
     *
     * @param location the location
     * @return the string representation of the location
     */
    private String locationString(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
}
