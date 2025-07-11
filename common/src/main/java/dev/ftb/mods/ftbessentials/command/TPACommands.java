package dev.ftb.mods.ftbessentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbessentials.config.FTBEConfig;
import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Random;

/**
 * @author LatvianModder
 */
public class TPACommands {
	public record TPARequest(String id, FTBEPlayerData source, FTBEPlayerData target, boolean here, long created) {
	}

	public static final HashMap<String, TPARequest> REQUESTS = new HashMap<>();

	public static TPARequest create(FTBEPlayerData source, FTBEPlayerData target, boolean here) {
		String key;

		do {
			key = String.format("%08X", new Random().nextInt());
		}
		while (REQUESTS.containsKey(key));

		TPARequest r = new TPARequest(key, source, target, here, System.currentTimeMillis());
		REQUESTS.put(key, r);
		return r;
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		if (FTBEConfig.TPA.isEnabled()) {
			dispatcher.register(Commands.literal("tpa")
					.requires(FTBEConfig.TPA)
					.then(Commands.argument("target", StringArgumentType.word()) // 使用字符串而非 EntityArgument 直接处理
							.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
									ctx.getSource().getServer().getPlayerNames(), builder))
							.executes(context -> {
								ServerPlayer source = context.getSource().getPlayerOrException();
								String targetName = StringArgumentType.getString(context, "target");

								ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
								if (target == null) {
									source.displayClientMessage(Component.literal("§c[TPA] 玩家不存在或未在线!"), false);
									return 0;
								}

								if (source.getUUID().equals(target.getUUID())) {
									source.displayClientMessage(Component.literal("§c[TPA] 你不能请求传送到自己!"), false);
									return 0;
								}

								return tpa(source, target, false);
							})
					)
			);

			dispatcher.register(Commands.literal("tpahere")
					.requires(FTBEConfig.TPA)
					.then(Commands.argument("target", StringArgumentType.word()) // 使用字符串而非 EntityArgument 直接处理
							.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
									ctx.getSource().getServer().getPlayerNames(), builder))
							.executes(context -> {
								ServerPlayer source = context.getSource().getPlayerOrException();
								String targetName = StringArgumentType.getString(context, "target");

								ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
								if (target == null) {
									source.displayClientMessage(Component.literal("§c[TPA] 玩家不存在或未在线!"), false);
									return 0;
								}

								if (source.getUUID().equals(target.getUUID())) {
									source.displayClientMessage(Component.literal("§c[TPA] 你不能请求传送到自己!"), false);
									return 0;
								}

								return tpa(source, target, true);
							})
					)
			);

			dispatcher.register(Commands.literal("tpaccept")
					.requires(FTBEConfig.TPA)
					.then(Commands.argument("id", StringArgumentType.string())
							.executes(context -> tpaccept(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "id")))
					)
			);

			dispatcher.register(Commands.literal("tpdeny")
					.requires(FTBEConfig.TPA)
					.then(Commands.argument("id", StringArgumentType.string())
							.executes(context -> tpdeny(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "id")))
					)
			);
		}
	}

	public static int tpa(ServerPlayer player, ServerPlayer target, boolean here) {
		FTBEPlayerData dataSource = FTBEPlayerData.getOrCreate(player).orElse(null);
		FTBEPlayerData dataTarget = FTBEPlayerData.getOrCreate(target).orElse(null);

		if (dataSource == null || dataTarget == null) {
			return 0;
		}

		if (REQUESTS.values().stream().anyMatch(r -> r.source == dataSource && r.target == dataTarget)) {
			player.displayClientMessage(Component.literal("§c[TPA] 请求已发送, 别急!"), false);
			return 0;
		}

		if (player.equals(target)) {
			player.displayClientMessage(Component.literal("§c[TPA] 你不能传送到自己!"), false);
			return 0;
		}


		TeleportPos.TeleportResult result = here ?
				dataTarget.tpaTeleporter.checkCooldown() :
				dataSource.tpaTeleporter.checkCooldown();

		if (!result.isSuccess()) {
			return result.runCommand(player);
		}

		TPARequest request = create(dataSource, dataTarget, here);

		// ====== 组件1：主信息（换行 + 颜色）
		MutableComponent component = Component.literal("§b[TPA] §f您收到了一个新的传送请求\n§7传送请求 [ ");
		component.append((here ? target : player).getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
		component.append(Component.literal(" §f➡ ").withStyle(ChatFormatting.WHITE));
		component.append((here ? player : target).getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
		component.append(Component.literal("§7 ]"));

		// ====== 组件2：按钮部分（点击命令）
		MutableComponent component2 = Component.literal("【");
		component2.append(Component.literal("接受 ✔").setStyle(Style.EMPTY
				.applyFormat(ChatFormatting.GREEN)
				.withBold(true)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + request.id))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击以接受")))));

		component2.append(Component.literal(" §7| ").withStyle(ChatFormatting.GRAY));

		component2.append(Component.literal("拒绝 ❌").setStyle(Style.EMPTY
				.applyFormat(ChatFormatting.RED)
				.withBold(true)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + request.id))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击以拒绝")))));

		component2.append(Component.literal("】"));

		// ====== 发给目标玩家
		target.displayClientMessage(component, false);  // false = 显示在聊天栏
		target.displayClientMessage(component2, false);

		// ====== 热栏提示 + 声音（短提示）
		target.displayClientMessage(Component.literal("§e你收到了一个传送请求!"), true);
		target.level().playSound(
				null,  // null 表示在客户端播放声音
				target.getX(), target.getY(), target.getZ(),
				SoundEvents.NOTE_BLOCK_PLING.value(),
				SoundSource.PLAYERS,
				1.0F,
				1.2F
		);



		// ====== 发给请求发送者
		player.displayClientMessage(Component.literal("§a[TPA] 请求已发送!"), false);

		return 1;
	}


	public static int tpaccept(ServerPlayer player, String id) {
		TPARequest request = REQUESTS.get(id);

		if (request == null) {
			player.displayClientMessage(Component.literal("§e[TPA] 无效请求!"), false);
			return 0;
		}

		ServerPlayer sourcePlayer = player.server.getPlayerList().getPlayer(request.source.getUuid());

		if (sourcePlayer == null) {
			player.displayClientMessage(Component.literal("§e[TPA] 目标玩家已离线!"), false);
			return 0;
		}

		TeleportPos.TeleportResult result = request.here ?
				request.target.tpaTeleporter.teleport(player, p -> new TeleportPos(sourcePlayer)) :
				request.source.tpaTeleporter.teleport(sourcePlayer, p -> new TeleportPos(player));

		if (result.isSuccess()) {
			REQUESTS.remove(request.id);
		}

		return result.runCommand(player);
	}

	public static int tpdeny(ServerPlayer player, String id) {
		TPARequest request = REQUESTS.get(id);

		if (request == null) {
			player.displayClientMessage(Component.literal("§e[TPA] 无效请求! "), false);
			return 0;
		}

		REQUESTS.remove(request.id);

		player.displayClientMessage(Component.literal("§e[TPA] 请求被拒绝!"), false);

		ServerPlayer player2 = player.server.getPlayerList().getPlayer(request.target.getUuid());

		if (player2 != null) {
			player2.displayClientMessage(Component.literal("§e[TPA] 请求被拒绝!"), false);
		}

		return 1;
	}
}
