package com.vdom.api;

import java.util.ArrayList;

public class FrameworkEventHelper {
    private static ArrayList<FrameworkEventListener> listeners = new ArrayList<FrameworkEventListener>();

    public static void addFrameworkListener(FrameworkEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void broadcastEvent(FrameworkEvent frameworkEvent) {
        for (FrameworkEventListener frameworkEventListener : listeners) {
            frameworkEventListener.frameworkEvent(frameworkEvent);
        }
    }
}
