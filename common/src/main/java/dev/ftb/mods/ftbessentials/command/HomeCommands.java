package dev.ftb.mods.ftbessentials.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbessentials.config.FTBEConfig;
import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;
import dev.ftb.mods.ftbessentials.util.SavedTeleportManager;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * @author LatvianModder
 */
public class HomeCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		if (FTBEConfig.HOME.isEnabled()) {
			dispatcher.register(Commands.literal("home")
					.requires(FTBEConfig.HOME)
					.executes(context -> home(context.getSource().getPlayerOrException(), "home"))
					.then(Commands.argument("name", StringArgumentType.greedyString())
							.suggests((context, builder) -> SharedSuggestionProvider.suggest(getHomeSuggestions(context), builder))
							.executes(context -> home(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name")))
					)
			);
			dispatcher.register(Commands.literal("sethome")
					.requires(FTBEConfig.HOME)
					.executes(context -> setHome(context.getSource().getPlayerOrException(), "home"))
					.then(Commands.argument("name", StringArgumentType.greedyString())
							.executes(context -> setHome(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name")))
					)
			);
			dispatcher.register(Commands.literal("delhome")
					.requires(FTBEConfig.HOME)
					.executes(context -> delHome(context.getSource().getPlayerOrException(), "home"))
					.then(Commands.argument("name", StringArgumentType.greedyString())
							.suggests((context, builder) -> SharedSuggestionProvider.suggest(getHomeSuggestions(context), builder))
							.executes(context -> delHome(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name")))
					)
			);
			dispatcher.register(Commands.literal("listhomes")
					.requires(FTBEConfig.HOME)
					.executes(context -> listHomes(context.getSource(), context.getSource().getPlayerOrException().getGameProfile()))
					.then(Commands.argument("player", GameProfileArgument.gameProfile())
							.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
							.executes(context -> listHomes(context.getSource(), GameProfileArgument.getGameProfiles(context, "player").iterator().next()))
					)
			);
		}
	}

	public static Set<String> getHomeSuggestions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return FTBEPlayerData.getOrCreate(context.getSource().getPlayerOrException())
				.map(data -> data.homeManager().getNames())
				.orElse(Set.of());
	}

	public static int home(ServerPlayer player, String name) {
		return FTBEPlayerData.getOrCreate(player).map(data -> {
				Set<String> homeNames = data.homeManager().getNames();
				if (!homeNames.contains(name)) {
					player.displayClientMessage(Component.literal("§c[HOME] 不存在: §e" + name), false);
					return 0;
				}

				return data.homeManager().teleportTo(name, player, data.homeTeleporter).runCommand(player);
				}).orElse(0);
	}

	public static int setHome(ServerPlayer player, String name) {
		return FTBEPlayerData.getOrCreate(player).map(data -> {
			try {
				data.homeManager().addDestination(name, new TeleportPos(player), player);
				player.displayClientMessage(Component.literal("§a[HOME] 设置成功! " + name), false);
				return 1;
			} catch (SavedTeleportManager.TooManyDestinationsException e) {
				player.displayClientMessage(Component.literal("§e[HOME] 已到达上限! 无法继续添加！"), false);
				return 0;
			}
		}).orElse(0);
	}

	public static int delHome(ServerPlayer player, String name) {
		String homeName = name.toLowerCase();
		return FTBEPlayerData.getOrCreate(player).map(data -> {
			if (data.homeManager().deleteDestination(homeName)) {
				player.displayClientMessage(Component.literal("§a[HOME] 已删除 Home 点: §e" + homeName), false);
				return 1;
			} else {
				player.displayClientMessage(Component.literal("§c[HOME] 未找到名为 §e" + homeName + " §c的 Home 点!"), false);
				return 0;
			}
		}).orElse(0);
	}


	public static int listHomes(CommandSourceStack source, GameProfile of) {
		return FTBEPlayerData.getOrCreate(of).map(data -> {
			if (data.homeManager().getNames().isEmpty()) {
				source.sendSuccess(() -> Component.literal("§c[HOME] None 未找到任何点位! "), false);
			} else {
				TeleportPos origin = new TeleportPos(source.getLevel().dimension(), BlockPos.containing(source.getPosition()));
				data.homeManager().destinations().forEach(entry ->
						source.sendSuccess(() -> Component.empty()
										.append(Component.literal(entry.name()).withStyle(ChatFormatting.YELLOW))
										.append(Component.literal(" |距离 ").withStyle(ChatFormatting.GRAY))
										.append(Component.literal(entry.destination().distanceString(origin)).withStyle(ChatFormatting.GREEN)),
								false));
			}
			return 1;
		}).orElse(0);
	}
}
