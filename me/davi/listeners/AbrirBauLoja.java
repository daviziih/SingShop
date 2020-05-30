package me.davi.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.davi.api.Utilidades;

import java.util.EnumSet;
import java.util.Set;

public final class AbrirBauLoja implements Listener {

	@Deprecated
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void chestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (e.getClickedBlock().getType() != Material.CHEST
				&& e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
			return;
		}

		Set<BlockFace> directions = EnumSet.of(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH);

		Block block = e.getClickedBlock();

		directions.stream().map(block::getRelative).forEach(relative -> {
			if (relative.getType() != Material.WALL_SIGN) {
				return;
			}

			Sign sign = (Sign) relative.getState();
			if (!Utilidades.isLojaValid(sign.getLines())) {
				return;
			}

			if (e.getPlayer().getName().equals(Utilidades.replaceShopName(sign.getLine(0)))) {
				return;
			}

			if (e.getPlayer().hasPermission("loja.cmd.abrirbau")) {
				e.setCancelled(false);
				return;
			}

			e.getPlayer().sendMessage("§c§lERRO§f Você não tem permissão para abrir a loja do jogador §b"
					+ Utilidades.replaceShopName(sign.getLine(0)));

			e.setCancelled(true);
		});
	}
}
