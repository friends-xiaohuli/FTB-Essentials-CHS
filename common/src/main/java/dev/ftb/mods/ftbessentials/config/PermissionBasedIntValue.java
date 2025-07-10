package dev.ftb.mods.ftbessentials.config;

import dev.ftb.mods.ftbessentials.integration.PermissionsHelper;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import net.minecraft.server.level.ServerPlayer;

public class PermissionBasedIntValue {
	public final IntValue value;
	public final String permission;

	public PermissionBasedIntValue(IntValue value, String permission, String... comment) {
		this.value = value
				.comment(comment)
				.comment("您可以使用FTB排名覆盖此设置 " + permission);
		this.permission = permission;
	}

	public int get(ServerPlayer player) {
		return PermissionsHelper.getInstance().getInt(player, value.get(), permission);
	}

}
