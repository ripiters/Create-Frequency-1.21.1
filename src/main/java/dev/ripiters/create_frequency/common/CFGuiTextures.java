package dev.ripiters.create_frequency.common;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripiters.create_frequency.CreateFrequency;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum CFGuiTextures implements ScreenElement {

    PLAYER_INVENTORY("player_inventory", 176, 108),
    TRANSMITTER_RECEIVER("transmitter_receiver", 173, 205),
    FREQUENCY_CONTROLLER("frequency_controller", 234, 86),
    ;

    public final ResourceLocation location;
    public final int width;
    public final int height;
    public final int startX;
    public final int startY;

    CFGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    CFGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    CFGuiTextures(String location, int startX, int startY, int width, int height) {
        this(CreateFrequency.MODID, location, startX, startY, width, height);
    }

    CFGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

    public int getStartX() {return startX;}

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
