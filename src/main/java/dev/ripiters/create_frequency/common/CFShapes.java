package dev.ripiters.create_frequency.common;

import com.simibubi.create.AllShapes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;

public class CFShapes {

    private static final VoxelShape VERTICAL_TABLET_SHAPE = cuboid(3, 1, -1, 13, 15, 3), SQUARE_TABLET_SHAPE = cuboid(2, 2, -1, 14, 14, 3);

    public static final VoxelShaper LOGISTICAL_CONTROLLER = shape(SQUARE_TABLET_SHAPE).forDirectional(SOUTH), FREQUENCY_BRIDGE = shape(VERTICAL_TABLET_SHAPE).forDirectional(SOUTH).withVerticalShapes(LOGISTICAL_CONTROLLER.get(UP));

    private static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }

    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
