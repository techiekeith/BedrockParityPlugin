package me.techiekeith;


import org.bukkit.plugin.java.JavaPlugin;

public class BedrockParityPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BedrockParityListener(), this);
        getLogger().info("Bedrock Parity plugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Bedrock Party plugin disabled.");
    }
}