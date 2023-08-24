package io.github.addoncommunity.galactifun.base.aliens;

import javax.annotation.Nonnull;

import org.bukkit.entity.Cow;

import io.github.addoncommunity.galactifun.api.aliens.Alien;

public final class Charger extends Alien<Cow> {

    public Charger() {
        super(Cow.class, "CHARGER", "充电器", 30, 5);
    }

    @Override
    public void onSpawn(@Nonnull Cow spawned) {
//        spawned.setScreaming(true);
    }

}
