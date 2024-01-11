package me.techiekeith;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Salmon;
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

    // TODO find out how many placement attempts Bedrock actually makes
    private static final int maxFlowerPlacementAttempts = 24;

    private static final int yOffsetChance = 10;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Salmon) {
            substituteBoneForBoneMeal(event);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        if (item != null
                && block != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && item.getType() == Material.BONE_MEAL
                && isSmallFlower(block)) {
            applyBoneMealToSmallFlower(item, block, event.getPlayer().getGameMode());
        }
    }

    private void substituteBoneForBoneMeal(EntityDeathEvent event) {
        List<ItemStack> drops = event.getDrops();
        if (drops.removeIf(item -> item.getType() == Material.BONE_MEAL)) {
            log("Salmon drop: Substituted Bone for Bone Meal");
            drops.add(new ItemStack(Material.BONE));
        }
    }

    private boolean isSmallFlower(Block block) {
        Material blockType = block.getType();
        return Arrays.stream(smallFlowers).anyMatch(smallFlower -> smallFlower == blockType);
    }

    private void applyBoneMealToSmallFlower(ItemStack item, Block block, GameMode gameMode) {
        World world = block.getWorld();
        if (placeDuplicateFlowersInWorld(world, block.getType(), block.getX(), block.getY(), block.getZ())) {
            Location location = block.getLocation();
            world.playEffect(location, Effect.VILLAGER_PLANT_GROW, 10);
            world.playSound(location, Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
            if (gameMode != GameMode.CREATIVE) {
                int remainingBoneMeal = item.getAmount() - 1;
                log("Bone Meal: Reduced player-held Bone Meal amount to " + remainingBoneMeal);
                item.setAmount(remainingBoneMeal);
            }
        }
    }

    private boolean placeDuplicateFlowersInWorld(World world, Material flowerType, int x, int y, int z) {
        log("Bone Meal: Applied to " + flowerType.getTranslationKey() + " at " + x + ", " + y + ", " + z);
        boolean success = false;
        for (int i = 0; i < maxFlowerPlacementAttempts; i++) {
            success |= tryPlaceDuplicateFlowerRandomly(world, flowerType, x, y, z);
        }
        return success;
    }

    private boolean tryPlaceDuplicateFlowerRandomly(World world, Material flowerType, int x, int y, int z) {
        // TODO find out what the real random flower distribution is
        int nx = x;
        int ny = y;
        int nz = z;
        for (int i = 0; i < 3; i++) {
            nx += random.nextInt(3) - 1;
            nz += random.nextInt(3) - 1;
            if (i > 0) {
                int moveY = random.nextInt(yOffsetChance);
                if (moveY < 3) {
                    ny += moveY  - 1;
                }
            }
        }
        Material duplicateFlowerType = getDuplicateFlowerType(flowerType);
        Block maybeGrassBlock = world.getBlockAt(nx, ny, nz);
        Block maybeAirBlock = world.getBlockAt(nx, ny + 1, nz);
        boolean success = maybeGrassBlock.getType() == Material.GRASS_BLOCK && maybeAirBlock.getType() == Material.AIR;
        if (success) {
            maybeAirBlock.setType(duplicateFlowerType);
            log("Bone Meal: Added " + duplicateFlowerType.getTranslationKey() + "  at " + nx + ", " + (ny + 1) + ", " + nz);
        }
        return success;
    }

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

    private void log(String message) {
        Bukkit.getLogger().info("[" + getClass().getName() + "]: " + message);
    }
}
