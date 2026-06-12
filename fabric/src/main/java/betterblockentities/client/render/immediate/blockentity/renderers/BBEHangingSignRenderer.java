package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BBEHangingSignRenderer extends BBEAbstractSignRenderer<HangingSignRenderState> {
    private static final Vector3fc TEXT_OFFSET = new Vector3f(0.0F, -0.32F, 0.073F);
    public static final WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS = new WallAndGroundTransformations<>(
            BBEHangingSignRenderer::createWallTransformation, BBEHangingSignRenderer::createGroundTransformation, 16
    );

    public BBEHangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public HangingSignRenderState createRenderState() {
        return new HangingSignRenderState();
    }

    public void extractRenderState(SignBlockEntity blockEntity, HangingSignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.attachmentType = HangingSignBlock.getAttachmentPoint(blockState);
        if (blockState.getBlock() instanceof WallHangingSignBlock) {
            state.transformations = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallHangingSignBlock.FACING));
        } else {
            state.transformations = TRANSFORMATIONS.freeTransformations(blockState.getValue(CeilingHangingSignBlock.ROTATION));
        }
    }

    private static Matrix4f baseTransformation(float angle) {
        return new Matrix4f().translation(0.5F, 0.9375F, 0.5F).rotate(Axis.YP.rotationDegrees(-angle)).translate(0.0F, -0.3125F, 0.0F);
    }

    private static Transformation textTransformation(float angle, boolean isFrontText) {
        Matrix4f result = baseTransformation(angle);
        if (!isFrontText) {
            result.rotate(Axis.YP.rotationDegrees(180.0F));
        }

        float s = 0.0140625F;
        result.translate(TEXT_OFFSET);
        result.scale(0.0140625F, -0.0140625F, 0.0140625F);
        return new Transformation(result);
    }

    private static SignRenderState.SignTransformations createTransformations(float angle) {
        return new SignRenderState.SignTransformations(textTransformation(angle, true), textTransformation(angle, false));
    }

    private static SignRenderState.SignTransformations createGroundTransformation(int segment) {
        return createTransformations(RotationSegment.convertToDegrees(segment));
    }

    private static SignRenderState.SignTransformations createWallTransformation(Direction direction) {
        return createTransformations(direction.toYRot());
    }
}
