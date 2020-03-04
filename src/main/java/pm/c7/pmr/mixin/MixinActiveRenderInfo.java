package pm.c7.pmr.mixin;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.c7.pmr.PerspectiveMod;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {
    @Shadow
    private boolean valid;
    @Shadow
    private IBlockReader world;
    @Shadow
    private Entity renderViewEntity;
    @Shadow
    private float pitch;
    @Shadow
    private float yaw;
    @Shadow
    private boolean thirdPerson;
    @Shadow
    private boolean thirdPersonReverse;
    @Shadow
    private float height;
    @Shadow
    private float previousHeight;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void updateCamera(IBlockReader worldIn, Entity renderViewEntity, boolean thirdPersonIn, boolean thirdPersonReverseIn, float partialTicks, CallbackInfo info) {
        if (PerspectiveMod.INSTANCE.perspectiveEnabled) {
            this.valid = true;
            this.world = worldIn;
            this.renderViewEntity = renderViewEntity;
            this.thirdPerson = thirdPersonIn;
            this.thirdPersonReverse = thirdPersonReverseIn;

            this.setDirection(PerspectiveMod.INSTANCE.cameraYaw, PerspectiveMod.INSTANCE.cameraPitch);
            this.setPosition(MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosX, renderViewEntity.getPosX()), MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosY, renderViewEntity.getPosY()) + (double)MathHelper.lerp(partialTicks, this.previousHeight, this.height), MathHelper.lerp((double)partialTicks, renderViewEntity.prevPosZ, renderViewEntity.getPosZ()));
            this.pitch = PerspectiveMod.INSTANCE.cameraPitch;
            this.yaw = PerspectiveMod.INSTANCE.cameraYaw;
            this.movePosition(-this.calcCameraDistance(4.0D), 0.0D, 0.0D);

            info.cancel();
        }
    }

    @Shadow
    protected void setDirection(float pitchIn, float yawIn) {}
    @Shadow
    protected void setPosition(double x, double y, double z) {}
    @Shadow
    protected void movePosition(double distanceOffset, double verticalOffset, double horizontalOffset) {}
    @Shadow
    private double calcCameraDistance(double startingDistance) {
        return startingDistance;
    }
}
