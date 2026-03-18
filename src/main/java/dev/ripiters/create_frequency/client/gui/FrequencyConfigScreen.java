package dev.ripiters.create_frequency.client.gui;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import dev.ripiters.create_frequency.common.CFLang;
import dev.ripiters.create_frequency.common.CFGuiTextures;
import dev.ripiters.create_frequency.common.link.FrequencyBlockEntity;
import dev.ripiters.create_frequency.common.network.ConfigureFrequencyPacket;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class FrequencyConfigScreen extends AbstractSimiScreen {

    private final ItemStack renderedItem;
    private final CFGuiTextures background = CFGuiTextures.TRANSMITTER_RECEIVER;
    private final BlockPos pos;
    private final FrequencyBlockEntity be;

    private EditBox hzInput;
    private EditBox nameInput;
    private ScrollInput scrollInput;
    private IconButton confirmButton;

    private float frequency;
    private String networkName;

    private static final String KEY_TITLE = "gui.frequency.title";
    private static final String KEY_LABEL_NAME = "gui.frequency.name_label";
    private static final String KEY_LABEL_HZ = "gui.frequency.hz_label";
    private static final String KEY_HINT = "gui.frequency.name_hint";

    public FrequencyConfigScreen(BlockPos pos, FrequencyBlockEntity be) {
        super(CFLang.translateDirect(KEY_TITLE));
        this.pos = pos;
        this.be = be;
        this.frequency = be.getFrequency();
        this.networkName = be.getNetworkName();
        this.renderedItem = new ItemStack(be.getBlockState().getBlock());
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        nameInput = new EditBox(font, x + 38, y + 46, 95, 10, Component.empty());
        nameInput.setValue(networkName);
        nameInput.setBordered(false);
        nameInput.setMaxLength(25);
        nameInput.setTextColor(0xFFFFFF);
        nameInput.setHint(CFLang.translateDirect(KEY_HINT));
        addRenderableWidget(nameInput);

        hzInput = new EditBox(font, x + 38, y + 88, 75, 10, Component.empty());
        hzInput.setValue(formatFreq(frequency));
        hzInput.setBordered(false);
        hzInput.setTextColor(0xFBDC7D);
        hzInput.setResponder(s -> {
            try {
                if (!s.isEmpty() && !s.equals(".")) {
                    float val = Float.parseFloat(s.replace(",", "."));
                    if (val > 1000) hzInput.setValue("1000");
                }
            } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(hzInput);

        scrollInput = new ScrollInput(x + 20, y + 83, 110, 18)
                .withRange(0, 10001)
                .setState((int) (frequency * 10))
                .titled(CFLang.translateDirect(KEY_LABEL_HZ))
                .withStepFunction(ctx -> {
                    if (ctx.shift) return 100;
                    if (ctx.control) return 1;
                    return 10;
                })
                .calling(state -> {
                    this.frequency = state / 10f;
                    if (!hzInput.isFocused()) {
                        hzInput.setValue(formatFreq(this.frequency));
                    }
                });
        addRenderableWidget(scrollInput);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        background.render(graphics, x, y);

        Component titleComp = CFLang.translateDirect(KEY_TITLE);
        graphics.drawString(font, titleComp, x + background.width / 2 - 4 - font.width(titleComp) / 2, y + 4, 0x592424, false);

        renderLabel(graphics, x + 20, y + 31, CFLang.translateDirect(KEY_LABEL_NAME));
        renderLabel(graphics, x + 20, y + 73, CFLang.translateDirect(KEY_LABEL_HZ));

        if (!nameInput.isFocused()) {
            AllGuiTextures.STATION_EDIT_NAME.render(graphics, x + 22, y + 44);
        }
        if (!hzInput.isFocused()) {
            AllGuiTextures.STATION_EDIT_NAME.render(graphics, x + 22, y + 86);
        }

        String hzValue = hzInput.getValue();
        if (!hzValue.isEmpty()) {
            int textWidth = font.width(hzValue);
            graphics.drawString(font, "Hz", x + 38 + textWidth + 2, y + 88, 0xFBDC7D, true);
        }

        renderAdditional(graphics, x, y);
    }

    private void renderLabel(GuiGraphics graphics, int x, int y, Component text) {
        int width = font.width(text);
        graphics.fill(x - 2, y - 1, x + width + 2, y + 9, 0x44000000);
        graphics.drawString(font, text, x, y, 0xFFFFEE);
    }

    private void renderAdditional(GuiGraphics graphics, int guiLeft, int guiTop) {
        GuiGameElement.of(renderedItem)
                .<GuiGameElement.GuiRenderBuilder>at(guiLeft + background.width + 10, guiTop + background.height - 60, 100)
                .scale(5)
                .render(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollInput.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= guiTop + 42 && mouseY <= guiTop + 54 && mouseX >= guiLeft + 20 && mouseX <= guiLeft + 130) {
            if (!nameInput.isFocused()) {
                setFocused(nameInput);
                nameInput.setFocused(true);
                nameInput.setCursorPosition(nameInput.getValue().length());
                nameInput.setHighlightPos(0);
            } else {
                nameInput.mouseClicked(mouseX, mouseY, button);
            }
            hzInput.setFocused(false);
            return true;
        }

        if (mouseY >= guiTop + 82 && mouseY <= guiTop + 94 && mouseX >= guiLeft + 20 && mouseX <= guiLeft + 130) {
            if (!hzInput.isFocused()) {
                setFocused(hzInput);
                hzInput.setFocused(true);
                hzInput.setCursorPosition(hzInput.getValue().length());
                hzInput.setHighlightPos(0);
            } else {
                hzInput.mouseClicked(mouseX, mouseY, button);
            }
            nameInput.setFocused(false);
            return true;
        }

        nameInput.setFocused(false);
        hzInput.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameInput.isFocused() && nameInput.charTyped(codePoint, modifiers)) return true;
        if (hzInput.isFocused() && hzInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        if (nameInput.isFocused() && nameInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (hzInput.isFocused() && hzInput.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        String val = hzInput.getValue().replace(",", ".");

        if (val.isEmpty() || val.equals(".")) {
            this.frequency = 0f;
        } else {
            try {
                this.frequency = Math.min(1000f, Float.parseFloat(val));
            } catch (NumberFormatException e) {
                this.frequency = 0f;
            }
        }

        String nameToSave = nameInput.getValue().trim();
        PacketDistributor.sendToServer(new ConfigureFrequencyPacket(pos, frequency, nameToSave));
    }

    private String formatFreq(float f) {
        if (f == (long) f) return String.format("%d", (long) f);
        return String.format("%.1f", f).replace(",", ".");
    }
}