package pm.c7.pmr.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.c7.pmr.PerspectiveMod;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private double xVelocity;
    @Shadow
    private double yVelocity;
    @Shadow
    private final MouseSmoother xSmoother = new MouseSmoother();
    @Shadow
    private final MouseSmoother ySmoother = new MouseSmoother();
    @Shadow
    private double lastLookTime = Double.MIN_VALUE;
    @Shadow
    private boolean mouseGrabbed;

    @Inject(method = "updatePlayerLook", at = @At("HEAD"), cancellable = true)
    private void noPlayerRotation(CallbackInfo info) {
        double time = NativeUtil.getTime();
        double min = time - this.lastLookTime;
        this.lastLookTime = time;
        if (this.isMouseGrabbed() && this.minecraft.isGameFocused()) {
            double sens = this.minecraft.gameSettings.mouseSensitivity * (double)0.6F + (double)0.2F;
            double mult = sens * sens * sens * 8.0D;
            double deltaX;
            double deltaY;
            if (this.minecraft.gameSettings.smoothCamera) {
                double smoothX = this.xSmoother.smooth(this.xVelocity * mult, min * mult);
                double smoothY = this.ySmoother.smooth(this.yVelocity * mult, min * mult);
                deltaX = smoothX;
                deltaY = smoothY;
            } else {
                this.xSmoother.reset();
                this.ySmoother.reset();
                deltaX = this.xVelocity * mult;
                deltaY = this.yVelocity * mult;
            }

            if (PerspectiveMod.INSTANCE.perspectiveEnabled) {
                PerspectiveMod.INSTANCE.cameraYaw += deltaX / 8.0D;
                PerspectiveMod.INSTANCE.cameraPitch += deltaY / 8.0D;

                if (Math.abs(PerspectiveMod.INSTANCE.cameraPitch) > 90.0f) {
                    PerspectiveMod.INSTANCE.cameraPitch = (PerspectiveMod.INSTANCE.cameraPitch > 0.0F) ? 90.0F : -90.0F;
                }
            }

            this.xVelocity = 0.0D;
            this.yVelocity = 0.0D;
            int yDir = 1;
            if (this.minecraft.gameSettings.invertMouse) {
                yDir = -1;
            }

            this.minecraft.getTutorial().onMouseMove(deltaX, deltaY);
            if (this.minecraft.player != null && !PerspectiveMod.INSTANCE.perspectiveEnabled) {
                this.minecraft.player.rotateTowards(deltaX, deltaY * (double)yDir);
            }
        }else{
            this.xVelocity = 0.0D;
            this.yVelocity = 0.0D;
        }

        info.cancel();
    }

    @Shadow
    public boolean isMouseGrabbed() {
        return this.mouseGrabbed;
    }
}
