package com.nisovin.shopkeepers.commands.lib;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Base class for commands that can only be executed by players.
 */
public abstract class PlayerCommand extends Command {

	public PlayerCommand(String name) {
		this(name, null);
	}

	public PlayerCommand(String name, List<String> aliases) {
		super(name, aliases);
	}

	@Override
	public boolean isAccepted(CommandSender sender) {
		return (sender instanceof Player);
	}

	@Override
	public void checkCommandSource(CommandSender sender) throws CommandSourceRejectedException {
		Validate.notNull(sender);
		if (!this.isAccepted(sender)) {
			throw createCommandSourceRejectedException(sender);
		}
	}

	public static CommandSourceRejectedException createCommandSourceRejectedException(CommandSender sender) {
		return new CommandSourceRejectedException("You must be a player in order to execute this command!");
	}
}
