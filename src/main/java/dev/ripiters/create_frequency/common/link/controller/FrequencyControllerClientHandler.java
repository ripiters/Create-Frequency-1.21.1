package dev.ripiters.create_frequency.common.link.controller;

import java.util.*;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ripiters.create_frequency.common.CFBlocks;
import dev.ripiters.create_frequency.common.CFItems;
import dev.ripiters.create_frequency.common.link.FrequencyLinkBehaviour;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerBindPacket;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerInputPacket;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerStopLecternPacket;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

public class FrequencyControllerClientHandler {

    public static final LayeredDraw.Layer OVERLAY = FrequencyControllerClientHandler::renderOverlay;

    public static Mode MODE = Mode.IDLE;
    public static int PACKET_RATE = 5;
    public static Collection<Integer> currentlyPressed = new HashSet<>();
    private static BlockPos lecternPos;
    private static BlockPos selectedLocation = BlockPos.ZERO;
    private static int packetCooldown;

    public static void toggleBindMode(BlockPos location) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.BIND;
            selectedLocation = location;
        } else {
            MODE = Mode.IDLE;
            onReset();
        }
    }

    public static void toggle() {
        if (MODE == Mode.IDLE) {
            MODE = Mode.ACTIVE;
            lecternPos = null;
        } else {
            MODE = Mode.IDLE;
            onReset();
        }
    }

    public static void activateInLectern(BlockPos lecternAt) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.ACTIVE;
            lecternPos = lecternAt;
        }
    }

    public static void deactivateInLectern() {
        if (MODE == Mode.ACTIVE && inLectern()) {
            MODE = Mode.IDLE;
            onReset();
        }
    }

    public static boolean inLectern() {
        return lecternPos != null;
    }

    protected static void onReset() {
        ControlsUtil.getControls()
                .forEach(kb -> kb.setDown(ControlsUtil.isActuallyPressed(kb)));
        packetCooldown = 0;
        selectedLocation = BlockPos.ZERO;

        if (inLectern())
            CatnipServices.NETWORK.sendToServer(new FrequencyControllerStopLecternPacket(lecternPos));
        lecternPos = null;

        if (!currentlyPressed.isEmpty())
            CatnipServices.NETWORK.sendToServer(new FrequencyControllerInputPacket(new ArrayList<>(currentlyPressed), false, Optional.ofNullable(lecternPos)));
        currentlyPressed.clear();

        FrequencyControllerItemRenderer.resetButtons();
    }

    public static void tick() {
        FrequencyControllerItemRenderer.tick();

        if (MODE == Mode.IDLE)
            return;

        if (packetCooldown > 0)
            packetCooldown--;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack heldItem = player.getMainHandItem();

        if (player.isSpectator()) {
            MODE = Mode.IDLE;
            onReset();
            return;
        }

        if (!inLectern() && !CFItems.FREQUENCY_CONTROLLER.isIn(heldItem)) {
            heldItem = player.getOffhandItem();
            if (!CFItems.FREQUENCY_CONTROLLER.isIn(heldItem)) {
                MODE = Mode.IDLE;
                onReset();
                return;
            }
        }

        if (inLectern() && CFBlocks.LECTERN_FREQUENCY_CONTROLLER.get()
                .getBlockEntityOptional(mc.level, lecternPos)
                .map(be -> !be.isUsedBy(mc.player))
                .orElse(true)) {
            deactivateInLectern();
            return;
        }

        if (mc.screen != null) {
            MODE = Mode.IDLE;
            onReset();
            return;
        }

        if (InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
            MODE = Mode.IDLE;
            onReset();
            return;
        }

        List<KeyMapping> controls = ControlsUtil.getControls();
        Collection<Integer> pressedKeys = new HashSet<>();
        for (int i = 0; i < controls.size(); i++) {
            if (ControlsUtil.isActuallyPressed(controls.get(i)))
                pressedKeys.add(i);
        }

        Collection<Integer> newKeys = new HashSet<>(pressedKeys);
        Collection<Integer> releasedKeys = new HashSet<>(currentlyPressed);
        newKeys.removeAll(currentlyPressed);
        releasedKeys.removeAll(pressedKeys);

        if (MODE == Mode.ACTIVE) {
            if (!releasedKeys.isEmpty()) {
                CatnipServices.NETWORK.sendToServer(new FrequencyControllerInputPacket(new ArrayList<>(releasedKeys), false, Optional.ofNullable(lecternPos)));
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.level(), player.blockPosition(), 1f, .5f, true);
            }

            if (!newKeys.isEmpty()) {
                CatnipServices.NETWORK.sendToServer(new FrequencyControllerInputPacket(new ArrayList<>(newKeys), true, Optional.ofNullable(lecternPos)));
                packetCooldown = PACKET_RATE;
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.level(), player.blockPosition(), 1f, .75f, true);
            }

            if (packetCooldown == 0 && !pressedKeys.isEmpty()) {
                CatnipServices.NETWORK.sendToServer(new FrequencyControllerInputPacket(new ArrayList<>(pressedKeys), true, Optional.ofNullable(lecternPos)));
                packetCooldown = PACKET_RATE;
            }
        }

        if (MODE == Mode.BIND) {
            VoxelShape shape = mc.level.getBlockState(selectedLocation).getShape(mc.level, selectedLocation);
            if (!shape.isEmpty())
                Outliner.getInstance().showAABB("controller", shape.bounds().move(selectedLocation))
                        .colored(0xB73C2D).lineWidth(1 / 16f);

            for (Integer integer : newKeys) {
                FrequencyLinkBehaviour linkBehaviour = BlockEntityBehaviour.get(mc.level, selectedLocation, FrequencyLinkBehaviour.TYPE);

                if (linkBehaviour != null) {
                    float freq = linkBehaviour.getNetworkKey();

                    String networkName = linkBehaviour.getNetworkName();

                    CatnipServices.NETWORK.sendToServer(new FrequencyControllerBindPacket(integer, freq, Optional.ofNullable(lecternPos)));

                    Component msg = CreateLang.translateDirect("linked_controller.key_bound",
                            controls.get(integer).getTranslatedKeyMessage().getString(),
                            freq);
                    player.displayClientMessage(msg, true);
                } else {
                    CatnipServices.NETWORK.sendToServer(new FrequencyControllerBindPacket(integer, -1.0f, Optional.ofNullable(lecternPos)));
                    player.displayClientMessage(Component.literal("Frequency Reset to Default").withStyle(ChatFormatting.RED), true);
                }

                MODE = Mode.IDLE;
                onReset();
                break;
            }
        }

        currentlyPressed = pressedKeys;
        controls.forEach(kb -> kb.setDown(false));

    }

    public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int width1 = guiGraphics.guiWidth();
        int height1 = guiGraphics.guiHeight();
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || MODE != Mode.BIND) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        Object[] keys = new Object[6];
        List<KeyMapping> controls = ControlsUtil.getControls();
        for (int i = 0; i < Math.min(controls.size(), 6); i++) {
            keys[i] = controls.get(i).getTranslatedKeyMessage().getString();
        }

        List<Component> list = new ArrayList<>();
        list.add(CreateLang.translateDirect("linked_controller.bind_mode").withStyle(ChatFormatting.GOLD));
        list.addAll(TooltipHelper.cutTextComponent(CreateLang.translateDirect("linked_controller.press_keybind", keys), FontHelper.Palette.ALL_GRAY));

        int width = 0;
        for (Component c : list) width = Math.max(width, mc.font.width(c));

        int height = list.size() * mc.font.lineHeight;
        int x = (width1 / 3) - width / 2;
        int y = height1 - height - 24;

        guiGraphics.renderComponentTooltip(mc.font, list, x, y);
        poseStack.popPose();
    }

    public enum Mode { IDLE, ACTIVE, BIND }
}