package com.lenis0012.pluginutils.modules.menu;

public enum Size {
    ONE_ROW(9),
    TWO_ROW(18),
    THREE_ROW(27),
    FOUR_ROW(36),
    FIVE_ROW(45),
    SIX_ROW(54);

    private final int slots;

    Size(int slots) {
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }
}
