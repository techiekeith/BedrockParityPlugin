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
        // TODO find out how many tries Bedrock actually makes
        for (int i = 0; i < 20; i++) {
            success |= tryPlaceDuplicateFlowerRandomly(world, flowerType, x, y, z);
        }
        return success;
    }

    private boolean tryPlaceDuplicateFlowerRandomly(World world, Material flowerType, int x, int y, int z) {
        // TODO find out what the real random flower distribution is
        int dx = x + random.nextInt(-3, 4);
        int dy = y + random.nextInt(-1, 3);
        int dz = z + random.nextInt(-3, 4);
        Material duplicateFlowerType = getDuplicateFlowerType(flowerType);
        Block maybeGrassBlock = world.getBlockAt(dx, dy, dz);
        Block maybeAirBlock = world.getBlockAt(dx, dy + 1, dz);
        boolean success = maybeGrassBlock.getType() == Material.GRASS_BLOCK && maybeAirBlock.getType() == Material.AIR;
        if (success) {
            maybeAirBlock.setType(duplicateFlowerType);
            log("Bone Meal: Added " + duplicateFlowerType.getTranslationKey() + "  at " + dx + ", " + dy + ", " + dz);
        }
        return success;
    }

    private Material getDuplicateFlowerType(Material flowerType) {
        if (flowerType == Material.DANDELION && random.nextInt(6) == 1) {
            // 1/6 chance of Poppy instead (ratio is a guess and may not be altogether accurate)
            return Material.POPPY;
        } else if (flowerType == Material.POPPY && random.nextInt(6) == 1) {
            // 1/6 chance of Dandelion instead (ratio is a guess and may not be altogether accurate)
            return Material.DANDELION;
        }
        return flowerType;
    }

    private void log(String message) {
        Bukkit.getLogger().info("[" + getClass().getName() + "]: " + message);
    }
}
