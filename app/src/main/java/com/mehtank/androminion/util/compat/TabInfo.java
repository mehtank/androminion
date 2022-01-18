package com.mehtank.androminion.util.compat;

public class TabInfo {
    private final Class<?> fragment;
    private final int iconId;
    private final int textId;

    public TabInfo(Class<?> fragment, int iconId, int textId) {
        this.fragment = fragment;
        this.iconId = iconId;
        this.textId = textId;
    }

    public Class<?> getFragmentClass() {
        return fragment;
    }

    public int getIconId() {
        return iconId;
    }

    public int getTextId() {
        return textId;
    }
}
