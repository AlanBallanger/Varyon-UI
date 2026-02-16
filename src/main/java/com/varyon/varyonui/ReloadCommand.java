package com.varyon.varyonui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ReloadCommand extends AbstractAsyncCommand {

    public ReloadCommand() {
        super("varyonuireload", "Reload VaryonUI configuration");
        this.requirePermission("varyonui.reload");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        CommandsConfig.getInstance().reload();
        NewsConfig.getInstance().reload();
        HomeConfig.getInstance().reload();
        ctx.sendMessage(Message.raw("VaryonUI configuration reloaded!"));
        
        return CompletableFuture.completedFuture(null);
    }
}
