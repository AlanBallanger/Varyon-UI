package com.varyon.varyonui.integration;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlaytimeBridge {

    private static final Logger LOG = Logger.getLogger("VaryonUI");
    private static final String API_CLASS = "com.varyon.playtime.api.PlaytimeAPI";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private static Boolean available = null;
    private static Object cachedApi = null;

    private PlaytimeBridge() {}

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName(API_CLASS);
                available = true;
            } catch (ClassNotFoundException e) {
                available = false;
            }
        }
        return Boolean.TRUE.equals(available);
    }

    @Nullable
    private static Object api() {
        if (!isAvailable()) return null;
        if (cachedApi != null) return cachedApi;
        try {
            Class<?> cls = Class.forName(API_CLASS);
            Method get = cls.getMethod("get");
            cachedApi = get.invoke(null);
            return cachedApi;
        } catch (Exception e) {
            LOG.log(Level.FINE, "PlaytimeBridge: cannot get API instance", e);
            return null;
        }
    }

    public static long getTotalPlaytime(UUID uuid) {
        try {
            Object a = api();
            if (a == null) return 0;
            Method m = a.getClass().getMethod("getTotalPlaytime", UUID.class);
            return (Long) m.invoke(a, uuid);
        } catch (Exception e) { return 0; }
    }

    public static long getDailyPlaytime(UUID uuid) {
        try {
            Object a = api();
            if (a == null) return 0;
            Method m = a.getClass().getMethod("getPlaytime", UUID.class, String.class);
            return (Long) m.invoke(a, uuid, "daily");
        } catch (Exception e) { return 0; }
    }

    public static long getFirstLogin(UUID uuid) {
        try {
            Object a = api();
            if (a == null) return 0;
            Method m = a.getClass().getMethod("getFirstLogin", UUID.class);
            return (Long) m.invoke(a, uuid);
        } catch (Exception e) { return 0; }
    }

    public static long getLastLogin(UUID uuid) {
        try {
            Object a = api();
            if (a == null) return 0;
            Method m = a.getClass().getMethod("getLastLogin", UUID.class);
            return (Long) m.invoke(a, uuid);
        } catch (Exception e) { return 0; }
    }

    /** Returns [threshold0, claimed0, threshold1, claimed1, ...] for daily rewards. */
    public static long[] getDailyRewardData(UUID uuid) {
        try {
            Object a = api();
            if (a == null) return new long[0];
            Method m = a.getClass().getMethod("getDailyRewardData", UUID.class);
            return (long[]) m.invoke(a, uuid);
        } catch (Exception e) { return new long[0]; }
    }

    public static String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        return hours + "h " + minutes + "m";
    }

    public static String formatRewardTime(long ms) {
        long totalMin = ms / 60_000L;
        if (totalMin < 60) return totalMin + " min";
        long h = totalMin / 60;
        long m = totalMin % 60;
        return m > 0 ? h + "h " + m + "m" : h + "h";
    }

    public static String formatDate(long epochMs) {
        if (epochMs <= 0) return "—";
        return DATE_FMT.format(Instant.ofEpochMilli(epochMs));
    }
}
