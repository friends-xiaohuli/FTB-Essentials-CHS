package dev.ftb.mods.ftbessentials.config;

import dev.ftb.mods.ftbessentials.FTBEssentials;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringListValue;

import java.util.List;

/**
 * @author LatvianModder
 */
public interface FTBEConfig {
	SNBTConfig CONFIG = SNBTConfig.create(FTBEssentials.MOD_ID).comment("FTB Essentials配置文件 ZH", "如果您想要编辑此模组的配置文件，请编辑defaultconfigs/ftbessentials-server.snbt \n 模组汉化修改补充文件来自 WhiteFox_rua（https://www.mcmod.cn/author/28434.html[B站主页]https://space.bilibili.com/515094027） ");

	SNBTConfig TELEPORTATION = CONFIG.addGroup("teleportation").comment("Teleportation-related settings");
	// back
	TimedCommandConfig BACK = new TimedCommandConfig(TELEPORTATION, "back", 30, 0)
			.comment("允许用户在传送（或死亡）后返回到之前的位置");
	PermissionBasedIntValue MAX_BACK = new PermissionBasedIntValue(
			BACK.config.addInt("max", 10)
					.range(0, Integer.MAX_VALUE),
			"ftbessentials.back.max",
			"传送历史的最大大小。这限制了您可以使用/返回的次数"
	);
	BooleanValue BACK_ON_DEATH_ONLY = BACK.config.addBoolean("only_on_death", false)
			.comment("是否仅在死亡后使用 /back 命令返回到上次死亡点？");
	// spawn
	TimedCommandConfig SPAWN = new TimedCommandConfig(TELEPORTATION, "spawn", 10, 0);
	// warp
	TimedCommandConfig WARP = new TimedCommandConfig(TELEPORTATION, "warp", 10, 0)
			.comment("允许管理员创建 '传送门'，这些是固定的世界点，用户可以使用 /warp 命令传送到这些点");
	// home
	TimedCommandConfig HOME = new TimedCommandConfig(TELEPORTATION, "home", 10, 0)
			.comment("允许用户设置 'HOME'，用户可以随时使用 /home 命令传送到这个地点");
	PermissionBasedIntValue MAX_HOMES = new PermissionBasedIntValue(
			HOME.config.addInt("max", 1)
					.range(0, Integer.MAX_VALUE),
			"ftbessentials.home.max",
			"用户可以拥有的最大 HOME 数量."
	);
	// tpa
	TimedCommandConfig TPA = new TimedCommandConfig(TELEPORTATION, "tpa", 10, 0)
			.comment("允许玩家发送请求以传送到服务器中的其他玩家,",
					"也可以请求其他玩家传送到自己这里");
	// rtp
	TimedCommandConfig RTP = new TimedCommandConfig(TELEPORTATION, "rtp", 600, 0)
			.comment("允许玩家随机传送到荒野中的某个点",
					"注意：目前尚未考虑已被声明的区块！");
	IntValue RTP_MAX_TRIES = RTP.config.addInt("max_tries", 100).range(1, 1000).comment("执行 /rtp 命令前尝试寻找可传送位置的最大次数");
	IntValue RTP_MIN_DISTANCE = RTP.config.addInt("min_distance", 500).range(0, 30000000).comment("/rtp 距离出生点的最小距离");
	IntValue RTP_MAX_DISTANCE = RTP.config.addInt("max_distance", 25000).range(0, 30000000).comment("/rtp 距离出生点的最大距离");
	StringListValue RTP_DIMENSION_WHITELIST = RTP.config.addStringList("dimension_whitelist", List.of())
			.comment("允许执行 /rtp 的维度 ID 白名单（如果不为空，玩家 *必须* 处于这些维度之一）",
					"支持通配符维度名（例如 'somemod:*'）");
	StringListValue RTP_DIMENSION_BLACKLIST = RTP.config.addStringList("dimension_blacklist", List.of("minecraft:the_end"))
			.comment("禁止执行 /rtp 的维度 ID 黑名单（玩家 *不能* 处于这些维度之一）",
					"支持通配符维度名（例如 'somemod:*'）");
	// tpl
	ToggleableConfig TPL = new ToggleableConfig(TELEPORTATION, "tpl")
			.comment("允许管理员传送到用户最后一次出现的位置");

	ToggleableConfig TPX = new ToggleableConfig(TELEPORTATION, "tpx")
			.comment("允许管理员跨维度传送");
	ToggleableConfig JUMP = new ToggleableConfig(TELEPORTATION, "jump")
			.comment("允许管理员传送（跳跃）到当前指向的方块位置");

	SNBTConfig ADMIN = CONFIG.addGroup("admin").comment("管理员用于作弊和管理的指令配置");
	ToggleableConfig HEAL = new ToggleableConfig(ADMIN, "heal")
			.comment("允许管理员使用指令为自己或他人完全恢复（生命值、饥饿值、火焰、药水效果）");
	ToggleableConfig FEED = new ToggleableConfig(ADMIN, "feed")
			.comment("允许管理员使用指令为自己或他人恢复饥饿值");
	ToggleableConfig EXTINGUISH = new ToggleableConfig(ADMIN, "extinguish")
			.comment("允许管理员使用指令熄灭自己或他人身上的火焰");
	ToggleableConfig FLY = new ToggleableConfig(ADMIN, "fly")
			.comment("允许管理员使用指令切换飞行状态，无需使用创造模式");
	ToggleableConfig SPEED = new ToggleableConfig(ADMIN, "speed")
			.comment("允许管理员修改自己或他人的行走速度");
	ToggleableConfig GOD = new ToggleableConfig(ADMIN, "god")
			.comment("允许管理员使用指令开启无敌模式，无需使用创造模式");
	ToggleableConfig INVSEE = new ToggleableConfig(ADMIN, "invsee")
			.comment("允许管理员查看其他玩家的背包");
	ToggleableConfig MUTE = new ToggleableConfig(ADMIN, "mute")
			.comment("允许管理员使用指令禁言或取消禁言玩家");
	ToggleableConfig KIT = new ToggleableConfig(ADMIN, "kit")
			.comment("允许管理员配置物品礼包，并给予玩家");
	ToggleableConfig TP_OFFLINE = new ToggleableConfig(ADMIN, "tp_offline")
			.comment("允许管理员修改离线玩家的位置");

	SNBTConfig MISC = CONFIG.addGroup("misc").comment("杂项功能和实用工具配置");
	ToggleableConfig KICKME = new ToggleableConfig(MISC, "kickme")
			.comment("允许用户踢出自己，例如卡住或数据不同步时使用");
	ToggleableConfig TRASHCAN = new ToggleableConfig(MISC, "trashcan")
			.comment("启用垃圾桶功能，可以销毁不需要的物品");
	ToggleableConfig REC = new ToggleableConfig(MISC, "rec")
			.comment("允许用户使用指令向服务器宣布自己正在录制或直播");
	ToggleableConfig HAT = new ToggleableConfig(MISC, "hat")
			.comment("允许用户将物品作为帽子佩戴在头部");
	ToggleableConfig NICK = new ToggleableConfig(MISC, "nick")
			.comment("允许用户更改自己的显示昵称，管理员也可为他人更改昵称");
	ToggleableConfig ENDER_CHEST = new ToggleableConfig(MISC, "enderchest")
			.comment("允许用户访问自己的末影箱，管理员也可管理其他玩家的末影箱");
	ToggleableConfig CRAFTING_TABLE = new ToggleableConfig(MISC, "crafting")
			.comment("允许用户无需工作台即可打开合成界面");
	ToggleableConfig STONECUTTER = new ToggleableConfig(MISC, "stonecutter")
			.comment("允许用户无需切石机即可打开切石界面");
	ToggleableConfig ANVIL = new ToggleableConfig(MISC, "anvil")
			.comment("允许用户无需铁砧即可打开铁砧界面");
	ToggleableConfig SMITHING_TABLE = new ToggleableConfig(MISC, "smithing")
			.comment("允许用户无需锻造台即可打开锻造界面");
	ToggleableConfig LEADERBOARD = new ToggleableConfig(MISC, "leaderboard")
			.comment("允许用户查看服务器排行榜统计信息");
	ToggleableConfig NEAR = new ToggleableConfig(MISC, "near")
			.comment("允许用户列出附近的玩家，按距离排序");
}

