package net.foxsgr.minecraft.memories;

import net.minecraft.SharedConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod that takes a screenshot every INTERVAL ticks.
 */
@Mod(Memories.MOD_ID)
@EventBusSubscriber(modid = Memories.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Memories {
    /**
     * The mod's ID.
     */
    public static final String MOD_ID = "memories";

    /**
     * The mod's logger.
     */
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /**
     * The number of ticks between screenshots.
     */
    private static final int INTERVAL = 120 * SharedConstants.TICKS_PER_SECOND;

    /**
     * The screenshotter.
     */
    private final Screenshotter screenshotter = new Screenshotter(LOGGER);

    /**
     * Whether the player is in a world.
     */
    private boolean inWorld = false;

    /**
     * Whether a screenshot is being taken.
     */
    private boolean requesting = false;

    /**
     * The number of ticks elapsed since the last screenshot.
     */
    private int ticksElapsed = 0;

    /**
     * Creates a new instance of this mod.
     */
    public Memories() {
        MinecraftForge.EVENT_BUS.register(this);
        ConfigManager.init();
    }

    /**
     * Takes a screenshot every INTERVAL ticks.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!inWorld || requesting || event.phase != TickEvent.Phase.END) {
            return;
        }

        ticksElapsed++;
        if (ticksElapsed >= INTERVAL) {
            takeScreenshot();
            ticksElapsed = 0;
        }
    }

    /**
     * Starts the timer when the player enters a server.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (inWorld || !(event.getEntity() instanceof Player)) {
            return;
        }

        LOGGER.debug("Starting timer");
        inWorld = true;
        ticksElapsed = 0;
    }

    /**
     * Stops the timer when the player leaves a server.
     *
     * @param event the event
     */
    @SubscribeEvent
    public void onWorldUnload(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        LOGGER.debug("Stopping timer");
        inWorld = false;
        ticksElapsed = 0;
    }

    /**
     * Takes a screenshot in a separate thread.
     */
    private void takeScreenshot() {
        requesting = true;
        screenshotter.takeScreenshot(() -> requesting = false);
    }
}
