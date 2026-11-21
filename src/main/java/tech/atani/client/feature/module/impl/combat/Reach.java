package tech.atani.client.feature.module.impl.combat;

import tech.atani.client.feature.module.Module;
import tech.atani.client.feature.module.data.ModuleData;
import tech.atani.client.feature.module.data.enums.Category;
import tech.atani.client.feature.value.impl.SliderValue;
import tech.atani.client.listener.event.minecraft.player.rotation.RayTraceRangeEvent;
import tech.atani.client.listener.radbus.Listen;

@ModuleData(name = "Reach", description = "Allows you to hit further", category = Category.COMBAT)
public class Reach extends Module {

    public SliderValue<Float> attackRange = new SliderValue<>("Attack Range", "What'll be the range for Attacking?", this, 3f, 3f, 6f, 1);

    @Listen
    public void onRayTrace(RayTraceRangeEvent rayTraceRangeEvent) {
        rayTraceRangeEvent.setRange(this.attackRange.getValue());
        rayTraceRangeEvent.setBlockReachDistance(Math.max(mc.playerController.getBlockReachDistance(), this.attackRange.getValue()));
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
