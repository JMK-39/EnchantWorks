package dev.xyat.enchantworks.enchantment.mixin.client;

import dev.xyat.enchantworks.enchantment.sixth_sense.SixthSenseClientEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 2000)
public abstract class EntityGlowingMixin {

    /**
     * 劫持原版发光逻辑：
     * 如果生物存在于我们的雷达缓存中，强制客户端认为它正在发光。
     * 这会触发原版的 OutlineBuffer 渲染，实现贴合模型的完美透视描边。
     */
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true, require = 0)
    private void enchantworks_enchant$forceSixthSenseGlow(CallbackInfoReturnable<Boolean> cir) {
        // 确保强转安全，且仅在客户端生效
        if ((Object) this instanceof LivingEntity living && living.level().isClientSide()) {
            if (SixthSenseClientEvent.isCached(living)) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * 劫持队伍颜色（这决定了原版轮廓发光的颜色）：
     * 我们直接给不同类型的生物返回不同的十六进制颜色值。
     */
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void enchantworks_enchant$forceSixthSenseColor(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof LivingEntity living && living.level().isClientSide()) {
            if (SixthSenseClientEvent.isCached(living)) {
                cir.setReturnValue(SixthSenseClientEvent.getColorForEntity(living));
            }
        }
    }
}
