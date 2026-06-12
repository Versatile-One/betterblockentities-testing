package betterblockentities.mixin.render;

/* local */
import betterblockentities.client.BBE;

/* minecraft */
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* java/misc */
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void captureFrustum(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, CameraRenderState cameraRenderState, Matrix4fc frustumMatrix, GpuBufferSlice fog, Vector4f clearColor, boolean drawSky, CallbackInfo ci) {
        BBE.GlobalScope.frustum = cameraRenderState.cullFrustum;
        BBE.GlobalScope.altRenderDispatcher.prepare(cameraRenderState.pos);
    }

    @Inject(at = @At("HEAD"), method = "submitBlockEntities")
    private void updateSignRenderState(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        BBE.GlobalScope.limitVanillaSignRendering = true;
    }

    /*
     *  give ourselves a lower priority so we can make sure this executes before any other mixins here
    */
    @Inject(method = "submitBlockEntities", at = @At("RETURN"), order = 900)
    private void submitAltRenderers(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        BBE.GlobalScope.limitVanillaSignRendering = false;

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        for (BlockEntityRenderState renderState : BBE.GlobalScope.altBlockEntityRenderStates) {
            BlockPos blockPos = renderState.blockPos;
            poseStack.pushPose();
            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
            BBE.GlobalScope.altRenderDispatcher.submit(
                    renderState, poseStack, submitNodeCollector, levelRenderState.cameraRenderState
            );
            poseStack.popPose();
        }
    }


    @Inject(at = @At("TAIL"), method = "render")
    private void clearRenderStates(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, CameraRenderState cameraRenderState, Matrix4fc frustumMatrix, GpuBufferSlice fog, Vector4f clearColor, boolean drawSky, CallbackInfo ci) {
        BBE.GlobalScope.altBlockEntityRenderStates.clear();
        BBE.GlobalScope.altRenderDispatcher.clearStateRendererPairs();
    }
}
