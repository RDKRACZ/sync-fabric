package dev.kir.sync;

import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import dev.kir.sync.networking.SyncPackets;
import dev.kir.sync.block.SyncBlocks;
import dev.kir.sync.block.entity.SyncBlockEntities;
import dev.kir.sync.client.render.SyncRenderers;
import dev.kir.sync.command.SyncCommands;
import dev.kir.sync.config.SyncConfig;
import dev.kir.sync.item.SyncItems;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Sync implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "sync";
    public static final String PROJECT_ID = MOD_ID + "-fabric";

    public static Identifier locate(String location) {
        return new Identifier(MOD_ID, location);
    }

    public static SyncConfig getConfig() {
        return AutoConfig.getConfigHolder(SyncConfig.class).getConfig();
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(SyncConfig.class, GsonConfigSerializer::new);
        SyncBlocks.init();
        SyncBlockEntities.init();
        SyncItems.init();
        SyncPackets.init();
        SyncCommands.init();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        SyncRenderers.initClient();
        SyncPackets.initClient();

        if (getConfig().updateTranslationsAutomatically) {
            CrowdinTranslate.downloadTranslations(PROJECT_ID, MOD_ID);
        }
    }
}