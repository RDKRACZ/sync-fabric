package dev.kir.sync.config;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellPriority;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

@Config(name = Sync.MOD_ID)
public class SyncConfig implements ConfigData {
    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean enableInstantShellConstruction = false;

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean warnPlayerInsteadOfKilling = false;

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public float fingerstickDamage = 20;

    @ConfigEntry.Category(value = "shell_construction")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public float hardcoreFingerstickDamage = 40;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellConstructorCapacity = 256000;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellStorageCapacity = 320;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public long shellStorageConsumption = 16;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean shellStorageAcceptsRedstone = true;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public int shellStorageMaxUnpoweredLifespan = 20;

    @ConfigEntry.Category(value = "energy")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public List<EnergyMapEntry> energyMap = new ArrayList<>(List.of(
        new EnergyMapEntry(EntityType.CHICKEN, 2),
        new EnergyMapEntry(EntityType.PIG, 16),
        new EnergyMapEntry(EntityType.PLAYER, 20),
        new EnergyMapEntry(EntityType.WOLF, 24),
        new EnergyMapEntry(EntityType.CREEPER, 80),
        new EnergyMapEntry(EntityType.ENDERMAN, 160)
    ));

    @ConfigEntry.Category(value = "sync")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public List<ShellPriorityWrapper> syncPriority = new ArrayList<>(List.of(new ShellPriorityWrapper()));

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public String wrench = "minecraft:stick";

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean updateTranslationsAutomatically = false;

    public static class ShellPriorityWrapper {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ShellPriority priority;

        public ShellPriorityWrapper() {
            this(ShellPriority.NATURAL);
        }

        public ShellPriorityWrapper(ShellPriority priority) {
            this.priority = priority;
        }
    }

    public static class EnergyMapEntry {
        @ConfigEntry.Gui.RequiresRestart
        public String entityId;

        @ConfigEntry.Gui.RequiresRestart
        public long outputEnergyQuantity;

        public EnergyMapEntry() {
            this(EntityType.PIG, 16);
        }

        public EnergyMapEntry(EntityType<?> entityType, long outputEnergyQuantity) {
            this(Registry.ENTITY_TYPE.getId(entityType).toString(), outputEnergyQuantity);
        }

        public EnergyMapEntry(String entityId, long outputEnergyQuantity) {
            this.entityId = entityId;
            this.outputEnergyQuantity = outputEnergyQuantity;
        }

        public EntityType<?> getEntityType() {
            Identifier id = Identifier.tryParse(this.entityId);
            return id == null ? EntityType.PIG : Registry.ENTITY_TYPE.get(id);
        }
    }
}
