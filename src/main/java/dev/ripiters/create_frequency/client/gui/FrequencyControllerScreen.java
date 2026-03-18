package dev.ripiters.create_frequency.client.gui;

import java.util.*;

import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.ripiters.create_frequency.common.CFGuiTextures;
import dev.ripiters.create_frequency.common.CFDataComponents;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerBindPacket;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class FrequencyControllerScreen extends AbstractSimiContainerScreen<FrequencyControllerMenu> {

    protected CFGuiTextures background;
    private List<Rect2i> extraAreas = Collections.emptyList();
    private final EditBox[] hzInputs = new EditBox[6];

    private IconButton resetButton;
    private IconButton confirmButton;

    public FrequencyControllerScreen(FrequencyControllerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.background = CFGuiTextures.FREQUENCY_CONTROLLER;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + 4 + PLAYER_INVENTORY.getHeight() + 22);
        setWindowOffset(1, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        List<Float> currentBinds = menu.contentHolder.getOrDefault(CFDataComponents.CONTROLLER_BINDS.get(), List.of());

        resetButton = new IconButton(x + background.getWidth() - 63, y + background.getHeight() - 23 + 22, AllIcons.I_TRASH);
        resetButton.withCallback(() -> {
            for (EditBox input : hzInputs) {
                input.setValue("0");
            }
        });

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 23 + 22, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);

        addRenderableWidget(resetButton);
        addRenderableWidget(confirmButton);

        for (int i = 0; i < 6; i++) {
            EditBox input = getEditBox(i, x, y);
            final int index = i;

            input.setTooltip(Tooltip.create(
                    CreateLang.translateDirect("linked_controller.frequency_slot_1",
                                    ControlsUtil.getControls().get(index).getTranslatedKeyMessage().getString())
                            .withStyle(ChatFormatting.GOLD)
            ));

            input.setBordered(false);
            input.setMaxLength(5);
            input.setTextColor(0xFBDC7D);

            float initialVal = i < currentBinds.size() ? currentBinds.get(i) : 0f;
            input.setValue(formatFreq(initialVal));

            input.setResponder(s -> {
                if (!s.contains(".") && s.length() > 4) {
                    input.setValue(s.substring(0, 4));
                    return;
                }
                try {
                    if (!s.isEmpty() && !s.equals(".")) {
                        float val = Float.parseFloat(s.replace(",", "."));
                        if (val > 1000) input.setValue("1000");
                    }
                } catch (NumberFormatException ignored) {}
            });

            hzInputs[i] = input;
            addRenderableWidget(input);
        }

        extraAreas = ImmutableList.of(
                new Rect2i(x + background.getWidth() + 8, y + background.getHeight() - 20, 55, 50)
        );
    }

    private @NotNull EditBox getEditBox(int i, int x, int y) {
        int xOffset = (i < 4) ? (8 + (i * 34)) : (30 + (3 * 30) + 35 + ((i - 4) * 34));

        return new EditBox(font, x + xOffset - 2, y + 60, 28, 10, Component.empty()) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                String valStr = getValue();
                float offsetX = 2;

                if (valStr.contains(".")) {
                    try {
                        float val = Float.parseFloat(valStr.replace(",", "."));
                        if (val >= 100) {
                            offsetX = 1;
                        }
                    } catch (NumberFormatException ignored) {}
                }

                graphics.pose().pushPose();
                graphics.pose().translate(offsetX, 0, 0);
                super.renderWidget(graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (EditBox input : hzInputs) {
            if (input.isMouseOver(mouseX, mouseY)) {
                if (!input.isFocused()) {
                    this.setFocused(null);
                    for (EditBox other : hzInputs) {
                        other.setFocused(false);
                        other.setHighlightPos(other.getCursorPosition());
                    }

                    input.setFocused(true);
                    this.setFocused(input);
                    input.setCursorPosition(input.getValue().length());
                    input.setHighlightPos(0);
                } else {
                    input.mouseClicked(mouseX, mouseY, button);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (EditBox input : hzInputs) {
            if (input.isMouseOver(mouseX, mouseY)) {
                try {
                    float currentVal = Float.parseFloat(input.getValue().replace(",", "."));
                    float step = hasControlDown() ? 0.1f : (hasShiftDown() ? 10.0f : 1.0f);
                    float newVal = Math.max(0, Math.min(1000, currentVal + (scrollY > 0 ? step : -step)));
                    input.setValue(formatFreq(newVal));
                    return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 26;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y + 22);
        graphics.drawString(font, title, x + background.width / 2 - 4 - font.width(title) / 2, y + 4 + 22, 0x592424, false);

        GuiGameElement.of(menu.contentHolder).<GuiGameElement.GuiRenderBuilder>at(
                x + background.getWidth() - 4,
                y + background.getHeight() - 55 + 22,
                -200
        ).scale(5).render(graphics);
    }

    @Override
    public void removed() {
        List<Float> values = new ArrayList<>();
        for (EditBox input : hzInputs) {
            try {
                String s = input.getValue().replace(",", ".");
                float f = (s.isEmpty() || s.equals(".")) ? 0f : Float.parseFloat(s);
                values.add(Math.min(1000f, Math.max(0f, f)));
            } catch (NumberFormatException e) { values.add(0f); }
        }
        PacketDistributor.sendToServer(new FrequencyControllerBindPacket(values, Optional.empty()));
        super.removed();
    }

    private String formatFreq(float f) {
        if (f == (long) f) return String.format("%d", (long) f);
        return String.format("%.1f", f).replace(",", ".");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.hoveredSlot == null) {
            super.renderTooltip(graphics, x, y);
            return;
        }

        if (hoveredSlot.container == menu.playerInventory) {
            if (hoveredSlot.hasItem()) {
                graphics.renderTooltip(font, getTooltipFromContainerItem(hoveredSlot.getItem()), Optional.empty(), x, y);
            }
            return;
        }

        if (menu.getCarried().isEmpty()) {
            List<Component> list = new ArrayList<>();
            if (hoveredSlot.hasItem()) {
                list = getTooltipFromContainerItem(hoveredSlot.getItem());
            }
            graphics.renderComponentTooltip(font, addToTooltip(list, hoveredSlot.getSlotIndex()), x, y);
        }
    }

    private List<Component> addToTooltip(List<Component> list, int slot) {
        if (slot < 0 || slot >= 12 || slot % 2 != 0)
            return list;

        list.add(CreateLang.translateDirect("linked_controller.frequency_slot_1",
                        ControlsUtil.getControls()
                                .get(slot / 2)
                                .getTranslatedKeyMessage()
                                .getString())
                .withStyle(ChatFormatting.GOLD));
        return list;
    }

    @Override
    public List<Rect2i> getExtraAreas() { return extraAreas; }
}