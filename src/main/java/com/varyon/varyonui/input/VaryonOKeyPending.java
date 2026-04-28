package com.varyon.varyonui.input;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VaryonOKeyPending {

    private static final long WINDOW_MS = 500L;
    private static final ConcurrentHashMap<UUID, Long> MARKS = new ConcurrentHashMap<>();

    private VaryonOKeyPending() {
    }

    public static void mark(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        UUID uuid = playerRef.getUuid();
        if (uuid == null) {
            return;
        }
        MARKS.put(uuid, System.currentTimeMillis());
    }

    public static boolean tryConsumeForPlayer(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        Long t = MARKS.get(uuid);
        if (t == null || now - t >= WINDOW_MS) {
            if (t != null) {
                MARKS.remove(uuid, t);
            }
            return false;
        }
        MARKS.remove(uuid);
        return true;
    }

    public static void clear(UUID uuid) {
        if (uuid != null) {
            MARKS.remove(uuid);
        }
    }
}
