package com.leclowndu93150.spawnercontrol.util;

public class TogglerState {
    public final boolean hasToggler;
    public final boolean togglerPowered;
    public final long checkTime;

    public TogglerState(boolean hasToggler, boolean togglerPowered) {
        this.hasToggler = hasToggler;
        this.togglerPowered = togglerPowered;
        this.checkTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - checkTime > 1000;  
    }
}
