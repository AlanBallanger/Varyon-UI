package com.varyon.varyonui.integration;

import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HytlSkinPreview {

    private static final Logger LOG = Logger.getLogger("VaryonUI");

    private static final String HYTALE_PHOTO_SKIN_FRONT =
            "https://hytale.photo/skin/front.png";
    private static final int PHOTO_QUERY_SIZE = 512;
    private static final int PREVIEW_CANVAS_WIDTH = 192;
    private static final int PREVIEW_CANVAS_HEIGHT = 256;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private HytlSkinPreview() {}

    public static void preparePortraitPackAtStartup(JavaPlugin plugin) {
        SkinPortraitRuntime.prepareAtStartup(plugin);
    }

    public static void applyPlaceholder(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.setObject(
                "#PlayerSkinPreview.Background",
                new PatchStyle().setColor(Value.of("#1e2838"))
        );
    }

    @Nullable
    public static byte[] fetchHeadshotPng(@Nonnull UUID uuid) {
        String qs = "user=" + uuid + "&trim=true&size=" + PHOTO_QUERY_SIZE;
        URI uri = URI.create(HYTALE_PHOTO_SKIN_FRONT + "?" + qs);
        try {
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(Duration.ofSeconds(45))
                    .header("Accept", "image/png,image/webp,*/*")
                    .build();
            HttpResponse<byte[]> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                LOG.log(Level.FINE, "hytale.photo HTTP " + resp.statusCode() + " for " + uuid);
                return null;
            }
            byte[] body = resp.body();
            if (body == null || body.length < 24 || !isPng(body)) {
                LOG.log(Level.FINE, "hytale.photo: not PNG or too small");
                return null;
            }
            return body;
        } catch (Exception e) {
            LOG.log(Level.FINE, "hytale.photo fetch failed", e);
            return null;
        }
    }

    public static void applyPngToPreview(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UUID uuid,
            @Nullable byte[] pngBytes,
            @Nullable JavaPlugin plugin) {
        if (pngBytes == null || pngBytes.length < 24 || !isPng(pngBytes)) {
            applyPlaceholder(commandBuilder);
            return;
        }
        byte[] scaled = resizeContainCenteredPng(pngBytes, PREVIEW_CANVAS_WIDTH, PREVIEW_CANVAS_HEIGHT);
        String rel = SkinPortraitRuntime.publishPng(plugin, uuid, scaled);
        if (rel == null || rel.isBlank()) {
            applyPlaceholder(commandBuilder);
            return;
        }
        PatchStyle ps = new PatchStyle()
                .setTexturePath(Value.of(rel.startsWith("/") ? rel.substring(1) : rel))
                .setBorder(Value.of(0));
        commandBuilder.setObject("#PlayerSkinPreview.Background", ps);
    }

    private static byte[] resizeContainCenteredPng(@Nonnull byte[] pngBytes, int canvasW, int canvasH) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(pngBytes));
            if (src == null) {
                return pngBytes;
            }
            int sw = Math.max(1, src.getWidth());
            int sh = Math.max(1, src.getHeight());
            double scale = Math.min((double) canvasW / sw, (double) canvasH / sh);
            int dw = Math.max(1, (int) Math.round(sw * scale));
            int dh = Math.max(1, (int) Math.round(sh * scale));
            BufferedImage dst = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                int ox = (canvasW - dw) / 2;
                int oy = (canvasH - dh) / 2;
                g.drawImage(src, ox, oy, dw, dh, null);
            } finally {
                g.dispose();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(dst, "PNG", baos);
            byte[] out = baos.toByteArray();
            return out.length >= 24 ? out : pngBytes;
        } catch (Exception e) {
            LOG.log(Level.FINE, "skin preview resize skipped", e);
            return pngBytes;
        }
    }

    private static boolean isPng(byte[] data) {
        return data.length >= 8
                && data[0] == (byte) 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47;
    }
}
