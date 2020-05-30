package me.davi.listeners;

import me.davi.api.Mensagens;
import me.davi.api.Utilidades;
import me.davi.config.LojaConfig;
import me.davi.exceptions.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public final class CriarLoja implements Listener {

	private final LojaConfig lojaConfig;

	public CriarLoja(LojaConfig lojaConfig) {
		this.lojaConfig = lojaConfig;
	}

	@EventHandler(ignoreCancelled = true)
	private void onCriar(SignChangeEvent e) {
		Player player = e.getPlayer();

		Sign sign = (Sign) e.getBlock().getState();

		if (!Utilidades.isLojaValid(e.getLines())) {
			return;
		}

		try {
			createSignLoja(player, e.getLines(), sign);
			e.setLine(2, Utilidades.updatePriceSign(e.getLine(2)));
			player.sendMessage("§a§lLOJA§f Loja criada com §a§lSUCESSO§f!");
		} catch (CreateSignNickOtherPlayerException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			e.getBlock().setType(Material.AIR);
			player.sendMessage("§a§lLOJA§f Você so pode criar uma loja com seu nome");
			return;
		} catch (CreateSignPlayerWithoutPermissionException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			player.sendMessage("§a§lLOJA§f Você não pode criar uma loja");
			return;
		} catch (CreateSignServerWithoutPermissionException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			player.sendMessage("§a§lLOJA§f Você não tem permissão para criar loja com o nome do servidor");
			return;
		} catch (CreateSignItemInvalidException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			player.sendMessage("§a§lLOJA§f Item que você inseriu está inválido");
			return;
		} catch (CreateSignWithoutChestException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			player.sendMessage("§a§lLOJA§f Você só pode criar lojas colocando a placa no baú");
			return;
		} catch (CreateSignServerOnChestException error) {
			e.getBlock().breakNaturally(new ItemStack(Material.SIGN));
			player.sendMessage("§a§lLOJA§f Você não pode criar loja do servidor em baús");
			return;
		}
	}

	private void createSignLoja(Player player, String[] lines, org.bukkit.block.Sign sign)
			throws CreateSignPlayerWithoutPermissionException, CreateSignWithoutChestException,
			CreateSignItemInvalidException, CreateSignNickOtherPlayerException,
			CreateSignServerWithoutPermissionException, CreateSignServerOnChestException {

		if ((!player.hasPermission("loja.admin")) && (!player.hasPermission("loja.player")) && (!player.isOp())) {
			throw new CreateSignPlayerWithoutPermissionException(
					"O player " + player.getName() + " tentou criar loja sem permissão.");
		}
		if (Utilidades.getItemLoja(lines, lojaConfig) == null) {
			throw new CreateSignItemInvalidException(
					"O player " + player.getName() + " tentou criar uma loja com um item inválido: " + lines[3]);
		}
		Block block = sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
		String placaLoja = Mensagens.nomeLoja;
		if (!Utilidades.replaceShopName(lines[0]).equals(placaLoja)) {
			if ((!block.getType().equals(Material.CHEST)) && (!block.getType().equals(Material.TRAPPED_CHEST))) {
				throw new CreateSignWithoutChestException(
						"O player " + player.getName() + " tentou criar uma loja fora do baú.");
			}
			if (!Utilidades.replaceShopName(lines[0]).equals(player.getName())) {
				throw new CreateSignNickOtherPlayerException(
						"O player " + player.getName() + " tentou criar uma loja com o nick de outro player.");
			}
			return;
		}
		if ((block.getType().equals(Material.CHEST)) || (block.getType().equals(Material.TRAPPED_CHEST))) {
			throw new CreateSignServerOnChestException(
					"O player " + player.getName() + " tentou criar uma loja do servidor em um baú.");
		}
		if ((!player.hasPermission("loja.cmd.admin")) && (!player.isOp())) {
			throw new CreateSignServerWithoutPermissionException(
					"O player " + player.getName() + " tentou criar uma loja com o nome do servidor sem permissão.");
		}
	}
}
