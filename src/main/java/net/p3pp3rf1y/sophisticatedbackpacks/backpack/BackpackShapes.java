package net.p3pp3rf1y.sophisticatedbackpacks.backpack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.p3pp3rf1y.sophisticatedcore.util.RotatedShapes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BackpackShapes {
	private BackpackShapes() {}

	public static void setShapeProvider(IShapeProvider provider) {
		shapeProvider = provider;
	}

	public static final IShapeProvider DEFAULT_SHAPE_PROVIDER = new IShapeProvider() {
		private static final Map<Integer, VoxelShape> SHAPES = new HashMap<>();
		@Override
		public VoxelShape getShape(Block backpackBlock, Direction dir, boolean leftTank, boolean rightTank, boolean battery) {
			int key = getKey(dir, leftTank, rightTank, battery);
			return SHAPES.computeIfAbsent(key, k -> composeShape(dir, leftTank, rightTank, battery));
		}

		private static VoxelShape composeShape(Direction dir, boolean leftTank, boolean rightTank, boolean battery) {
			List<RotatedShapes> shapes = new ArrayList<>();
			shapes.add(BODY);
			shapes.add(leftTank ? LEFT_TANK : LEFT_POUCHES);
			shapes.add(rightTank ? RIGHT_TANK : RIGHT_POUCHES);
			shapes.add(battery ? BATTERY : FRONT_POUCH);
			return or(shapes.stream().map(r -> r.getRotatedShape(dir)));
		}

		private static VoxelShape or(Stream<VoxelShape> shapes) {
			return shapes.reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR))
					.map(VoxelShape::optimize)
					.orElse(Shapes.empty());
		}

		private static int getKey(Direction dir, boolean leftTank, boolean rightTank, boolean battery) {
			return dir.get2DDataValue() << 3 | b2i(leftTank) << 2 | b2i(rightTank) << 1 | b2i(battery);
		}

		private static int b2i(boolean value) {
			return value ? 1 : 0;
		}

		private static final RotatedShapes BODY = new RotatedShapes(
				Block.box(5.25, 0, 4, 5.75, 12.5, 5),
				Block.box(4.25, 0, 4, 5.25, 12.5, 5),
				Block.box(3.75, 0, 4, 4.25, 12.5, 5),
				Block.box(10.25, 0, 4, 10.75, 12.5, 5),
				Block.box(10.75, 0, 4, 11.75, 12.5, 5),
				Block.box(11.75, 0, 4, 12.25, 12.5, 5),
				Block.box(4.25, 8.25, 11, 11.75, 9.25, 11.25),
				Block.box(11.75, 8.25, 5.25, 12.75, 13.25, 11.25),
				Block.box(4.25, 9.25, 5.25, 11.75, 13.25, 11.25),
				Block.box(3.25, 8.25, 5.25, 4.25, 13.25, 11.25),
				Block.box(5.75, 13.25, 7.25, 6.5, 14, 8.25),
				Block.box(5.75, 14, 7.25, 10.25, 14.5, 8.25),
				Block.box(9.5, 13.25, 7.25, 10.25, 14, 8.25),
				Block.box(4.5, 9, 4.75, 5.5, 13.5, 11.5),
				Block.box(4.5, 7, 10.75, 5.5, 9, 11.5),
				Block.box(10.5, 9, 4.75, 11.5, 13.5, 11.5),
				Block.box(10.5, 7, 10.75, 11.5, 9, 11.5),
				Block.box(3, 0, 5, 13, 13, 11),
				Block.box(4, 1, 4.5, 12, 12, 5)
		);

		private static final RotatedShapes BATTERY = new RotatedShapes(
				Block.box(4, 0, 11, 12, 6, 14),
				Block.box(6, 5.25, 11.5, 7, 6.25, 12.5),
				Block.box(7.25, 5.25, 11.5, 8.25, 6.25, 12.5),
				Block.box(6, 7, 10.25, 7, 8, 11.25),
				Block.box(7.25, 7, 10.25, 8.25, 8, 11.25),
				Block.box(6.199999999999999, 5.6, 11.7, 6.800000000000001, 7.2, 12.3),
				Block.box(6.199999999999999, 7.2, 10.7, 6.800000000000001, 7.8, 12.3),
				Block.box(7.449999999999999, 5.6, 11.7, 8.05, 7.2, 12.3),
				Block.box(7.449999999999999, 7.2, 10.7, 8.05, 7.8, 12.3),
				Block.box(8.8, 4.05, 12.95, 10.2, 5.45, 14.35),
				Block.box(8.8, 0.05, 12.95, 10.2, 1.45, 14.35),
				Block.box(11.25, 4.25, 10.25, 12.25, 5.25, 14.25),
				Block.box(4.5, 4.25, 13.25, 11.5, 5.25, 14.25),
				Block.box(3.75, 4.25, 10.25, 4.75, 5.25, 14.25),
				Block.box(11.25, 0.25, 10.25, 12.25, 1.25, 14.25),
				Block.box(4.5, 0.25, 13.25, 11.5, 1.25, 14.25),
				Block.box(3.75, 0.25, 10.25, 4.75, 1.25, 14.25)
		);

		private static final RotatedShapes LEFT_TANK = new RotatedShapes(
				Block.box(2.5, 1.5, 6, 3.5, 7.5, 10),
				Block.box(0.5, 10.5, 6.5, 3.5, 11.5, 9.5),
				Block.box(0, 9.5, 6, 3, 10.5, 10),
				Block.box(0, 8.5, 6, 3, 9.5, 10),
				Block.box(0, 7.5, 6, 3, 8.5, 10),
				Block.box(0, 0.5, 6, 3, 1.5, 10),
				Block.box(0.5, 1.5, 6.5, 2.5, 7.5, 9.5)
		);

		private static final RotatedShapes RIGHT_TANK = new RotatedShapes(
				Block.box(12.5, 1.5, 6, 13.5, 7.5, 10),
				Block.box(12.5, 10.5, 6.5, 15.5, 11.5, 9.5),
				Block.box(13, 9.5, 6, 16, 10.5, 10),
				Block.box(13, 8.5, 6, 16, 9.5, 10),
				Block.box(13, 7.5, 6, 16, 8.5, 10),
				Block.box(13, 0.5, 6, 16, 1.5, 10),
				Block.box(13.5, 1.5, 6.5, 15.5, 7.5, 9.5)
		);

		private static final RotatedShapes LEFT_POUCHES = new RotatedShapes(
				Block.box(1, 2, 5.5, 3, 6, 10.5),
				Block.box(1, 1, 5.5, 3, 2, 10.5),
				Block.box(1, 0, 5.5, 3, 1, 10.5),
				Block.box(0.75, 3, 7.5, 1, 5, 8.5),
				Block.box(1, 4, 5.5, 3, 5, 10.5),
				Block.box(2, 7, 5.5, 3, 11, 10.5),
				Block.box(1.75, 8, 7.5, 2, 10, 8.5),
				Block.box(2, 9, 5.5, 4, 10, 10.5)
		);

		private static final RotatedShapes RIGHT_POUCHES = new RotatedShapes(
				Block.box(13, 2, 5.5, 15, 6, 10.5),
				Block.box(13, 1, 5.5, 15, 2, 10.5),
				Block.box(13, 0, 5.5, 15, 1, 10.5),
				Block.box(15, 3, 7.5, 15.25, 5, 8.5),
				Block.box(13, 4, 5.5, 15, 5, 10.5),
				Block.box(13, 7, 5.5, 14, 11, 10.5),
				Block.box(14, 8, 7.5, 14.25, 10, 8.5),
				Block.box(12, 9, 5.5, 14, 10, 10.5)
		);

		private static final RotatedShapes FRONT_POUCH = new RotatedShapes(
				Block.box(4, 2, 11, 12, 6, 13),
				Block.box(4, 1, 11, 12, 2, 13),
				Block.box(4, 0, 11, 12, 1, 13),
				Block.box(5, 3, 13, 6, 5, 13.25),
				Block.box(10, 3, 13, 11, 5, 13.25),
				Block.box(4, 4, 11, 12, 5, 13)
		);
	};

	private static IShapeProvider shapeProvider = DEFAULT_SHAPE_PROVIDER;

	public static VoxelShape getShape(Block backpackBlock, Direction dir, boolean leftTank, boolean rightTank, boolean battery) {
		return shapeProvider.getShape(backpackBlock, dir, leftTank, rightTank, battery);
	}

	public interface IShapeProvider {
		VoxelShape getShape(Block backpackBlock, Direction dir, boolean leftTank, boolean rightTank, boolean battery);
	}
}
