package me.davi.listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import me.davi.api.Utilidades;

import java.util.EnumSet;
import java.util.Set;

public final class QuerarLoja implements Listener {

	public QuerarLoja(Plugin plugin) {
	}

	@Deprecated
	@EventHandler
	public void chestBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() != Material.CHEST && e.getBlock().getType() != Material.TRAPPED_CHEST) {
			return;
		}

		Set<BlockFace> directions = EnumSet.of(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH);

		directions.stream().map(e.getBlock()::getRelative).forEach(relative -> {
			if (relative.getType() != Material.WALL_SIGN) {
				return;
			}

			Sign sign = (Sign) relative.getState();
			if (!Utilidades.isLojaValid(sign.getLines())) {
				return;
			}

			if (e.getPlayer().getName().equals(sign.getLine(0))) {
				return;
			}

			if (e.getPlayer().hasPermission("loja.cmd.quebrarloja")) {
				e.setCancelled(false);
				return;
			}

			e.getPlayer().sendMessage("§c§lERRO§f Você não tem permissão para quebrar a loja do jogador §b"
					+ Utilidades.replaceShopName(sign.getLine(0)));

			e.setCancelled(true);
		});
	}

	@Deprecated
	@EventHandler(priority = EventPriority.HIGHEST)
	public void signBreak(BlockBreakEvent e) {

		if (e.getBlock().getType() != Material.WALL_SIGN) {
			return;
		}

		Sign sign = (Sign) e.getBlock().getState();
		if (!Utilidades.isLojaValid(sign.getLines())) {
			return;
		}

		if (e.getPlayer().getName().equals(sign.getLine(0))) {
			return;
		}

		if (e.getPlayer().hasPermission("loja.cmd.quebrarloja")) {
			e.setCancelled(false);
			return;
		}

		e.getPlayer().sendMessage("§c§lERRO§f Você não tem permissão para quebrar a loja do jogador §b"
				+ Utilidades.replaceShopName(sign.getLine(0)));

		e.setCancelled(true);
	}
}
