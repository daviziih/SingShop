package me.davi.listeners;

import me.davi.api.Utilidades;
import me.davi.config.LojaConfig;
import me.davi.enums.LojaEnum;
import me.davi.exceptions.InventoryFullException;
import me.davi.exceptions.PlayerEqualsTargetException;
import me.davi.exceptions.PlayerMoneyException;
import me.davi.exceptions.SignUnknowBuy;
import me.davi.handlers.LojaBuyServer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class PlacaComprar implements Listener {

	private final Economy economy;
	private final LojaConfig lojaConfig;

	public PlacaComprar(Economy economy, LojaConfig lojaConfig) {
		this.economy = economy;
		this.lojaConfig = lojaConfig;
	}

	@EventHandler(ignoreCancelled = true)
	private void onComprar(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.SIGN
				&& e.getClickedBlock().getType() != Material.WALL_SIGN) {
			return;
		}

		Sign sign = (Sign) e.getClickedBlock().getState();
		if (!Utilidades.isLojaValid(sign.getLines())) {
			return;
		}
		String placaLoja = "Loja";

		if (!Utilidades.replaceShopName(sign.getLine(0)).equals(placaLoja)) {
			return;
		}

		Block block = e.getClickedBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
		if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
			return;
		}

		Player player = e.getPlayer();
		try {
			ItemStack item = Utilidades.getItemLoja(sign.getLines(), lojaConfig);
			comprarPelaPlaca(player, sign, item);
		} catch (PlayerEqualsTargetException error1) {
			player.sendMessage("§a§lLOJA§f Você não pode comprar de si mesmo");
		} catch (SignUnknowBuy error2) {
			player.sendMessage("§a§lLOJA§f Você só pode §a§lVENDER§f nessa loja");
		} catch (InventoryFullException error3) {
			player.sendMessage("§a§lLOJA§f Você não tem espaço no suficiente inventario");
		} catch (PlayerMoneyException erro4) {
			player.sendMessage("§a§lLOJA§f Você não tem dinheiro suficiente para comprar esse item");
		}
	}

	private void comprarPelaPlaca(Player player, org.bukkit.block.Sign placa, ItemStack item)
			throws PlayerMoneyException, SignUnknowBuy, InventoryFullException, PlayerEqualsTargetException {
		Double priceBuy = Utilidades.getPrices(LojaEnum.COMPRAR, placa);
		if (priceBuy == 0.0D) {
			throw new SignUnknowBuy("A placa {x=" + placa.getLocation().getX() + ",y=" + placa.getLocation().getY()
					+ ",z=" + placa.getLocation().getZ() + "} não tem opção para comprar.");
		}
		if (Utilidades.replaceShopName(placa.getLine(0)).equals(player.getName())) {
			throw new PlayerEqualsTargetException(
					"O jogador '" + player.getName() + "' está tentando comprar dele mesmo.");
		}
		int amountItemSign = Short.parseShort(Utilidades.replace(placa.getLine(1)));
		if (!Utilidades.haveSlotClearInv(player.getInventory(), item, amountItemSign)) {
			throw new InventoryFullException("Inventário do jogador está lotado e não tem como receber os itens.");
		}
		for (int i = 0; i <= 100; i++) {
			if ((player.hasPermission("*")) || (player.isOp())) {
				break;
			}
			if (player.hasPermission("loja.comprar." + i)) {
				priceBuy = priceBuy - priceBuy * i / 100.0D;
				break;
			}
		}
		if (economy.getBalance(player) < priceBuy) {
			throw new PlayerMoneyException(
					"O jogador '" + player.getName() + "' não tem dinheiro suficiente para fazer a compra.");
		}
		String moneyFormatted = String.format("%.2f", priceBuy);
		player.sendMessage(
				"§a§lLOJA§f Você comprou §b" + amountItemSign + "§f itens por §e" + moneyFormatted + " coins");

		economy.withdrawPlayer(player, priceBuy);

		item.setAmount(amountItemSign);
		player.getInventory().addItem(item);
		player.updateInventory();

		LojaBuyServer eventBuy = new LojaBuyServer(player, priceBuy, item, amountItemSign);
		Bukkit.getServer().getPluginManager().callEvent(eventBuy);
	}
}
