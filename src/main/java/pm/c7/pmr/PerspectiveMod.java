package pm.c7.pmr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod(PerspectiveMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PerspectiveMod
{
    public static final String MODID = "pmr";

    public static KeyBinding KEY_TOGGLE;

    public static PerspectiveMod INSTANCE;

    private static Minecraft mc = Minecraft.getInstance();

    public boolean perspectiveEnabled = false;
    public float cameraPitch;
    public float cameraYaw;

    public PerspectiveMod() {
        if (!isMixinInClasspath())
            throw new IllegalStateException("You did not install MixinBootstrap.");
        PerspectiveMod.INSTANCE = this;
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        KEY_TOGGLE = new KeyBinding("key.pmr.toggle", GLFW.GLFW_KEY_F4, "key.categories.pmr");
        ClientRegistry.registerKeyBinding(KEY_TOGGLE);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void keyEvent(InputEvent.KeyInputEvent event) {
            if (mc.world == null) return; // why does stuff fire on menu what
            if (mc.player != null && KEY_TOGGLE != null && KEY_TOGGLE.isPressed()) {
                PerspectiveMod.INSTANCE.perspectiveEnabled = !PerspectiveMod.INSTANCE.perspectiveEnabled;

                PerspectiveMod.INSTANCE.cameraPitch = mc.player.rotationPitch;
                PerspectiveMod.INSTANCE.cameraYaw = mc.player.rotationYaw;

                mc.gameSettings.thirdPersonView = PerspectiveMod.INSTANCE.perspectiveEnabled ? 1 : 0;
            }
        }

        @SubscribeEvent
        public static void tickEvent(TickEvent.ClientTickEvent event) {
            if (mc.world == null) return; // why does stuff fire on menu what
            if (PerspectiveMod.INSTANCE.perspectiveEnabled && mc.gameSettings.thirdPersonView != 1)
                PerspectiveMod.INSTANCE.perspectiveEnabled = false;
        }
    }

    private static boolean isMixinInClasspath() {
        try {
            Class.forName("org.spongepowered.asm.launch.Phases");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }
}
