package dev.ftb.mods.ftbessentials.config;

import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.server.level.ServerPlayer;

public class TimedCommandConfig extends ToggleableConfig {
	private final PermissionBasedIntValue cooldown;
	private final PermissionBasedIntValue warmup;

	public TimedCommandConfig(SNBTConfig parent, String name, int defaultCooldown, int defaultWarmup) {
		super(parent, name);

		cooldown = new PermissionBasedIntValue(
				config.addInt("cooldown", defaultCooldown).range(0, 604800),
				String.format("ftbessentials.%s.cooldown", name),
				String.format("使用 /%s 指令的冷却时间（单位：秒）", name)
		);

		warmup = new PermissionBasedIntValue(
				config.addInt("warmup", defaultWarmup).range(0, 604800),
				String.format("ftbessentials.%s.warmup", name),
				String.format("执行 /%s 指令前的预热时间（单位：秒）", name)
		);
	}

	public int getCooldown(ServerPlayer player) {
		return cooldown.get(player);
	}

	public int getWarmup(ServerPlayer player) {
		return warmup.get(player);
	}

	@Override
	public TimedCommandConfig comment(String... comment) {
		super.comment(comment);
		return this;
	}
}
