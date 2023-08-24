package io.github.addoncommunity.galactifun.api.universe;

import javax.annotation.Nonnull;

import lombok.Getter;

import org.bukkit.inventory.ItemStack;

import io.github.addoncommunity.galactifun.api.universe.attributes.DayCycle;
import io.github.addoncommunity.galactifun.api.universe.attributes.Gravity;
import io.github.addoncommunity.galactifun.api.universe.attributes.Orbit;
import io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere.Atmosphere;
import io.github.addoncommunity.galactifun.api.universe.types.PlanetaryType;

/**
 * A celestial object
 *
 * @author Mooy1
 */
@Getter
public class PlanetaryObject extends UniversalObject {

    private final DayCycle dayCycle;
    private final Atmosphere atmosphere;
    private final Gravity gravity;
    private final String CN_name;

    public PlanetaryObject(String name, PlanetaryType type, Orbit orbit, StarSystem orbiting, ItemStack baseItem,
                           @Nonnull DayCycle dayCycle, @Nonnull Atmosphere atmosphere, @Nonnull Gravity gravity, String CN_name) {
        super(name, type, orbit, orbiting, baseItem, CN_name);
        this.dayCycle = dayCycle;
        this.atmosphere = atmosphere;
        this.gravity = gravity;
        this.CN_name = CN_name;
    }

    public PlanetaryObject(String name, PlanetaryType type, Orbit orbit, PlanetaryObject orbiting, ItemStack baseItem,
                           @Nonnull DayCycle dayCycle, @Nonnull Atmosphere atmosphere, @Nonnull Gravity gravity, String CN_name) {
        super(name, type, orbit, orbiting, baseItem, CN_name);
        this.dayCycle = dayCycle;
        this.atmosphere = atmosphere;
        this.gravity = gravity;
        this.CN_name = CN_name;
    }

}
