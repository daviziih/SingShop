package me.davi.listeners;

import me.davi.api.Utilidades;
import me.davi.config.LojaConfig;
import me.davi.enums.LojaEnum;
import me.davi.exceptions.*;
import me.davi.handlers.LojaBuyOtherPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class ComprarChest implements Listener {

	private final Economy economy;
	private final LojaConfig lojaConfig;

	public ComprarChest(Economy economy, LojaConfig lojaConfig, Plugin plugin) {
		this.economy = economy;
		this.lojaConfig = lojaConfig;
	}

	@EventHandler(ignoreCancelled = true)
	@Deprecated
	private void onComprar(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (e.getClickedBlock().getType() != Material.WALL_SIGN) {
			return;
		}

		Sign sign = (Sign) e.getClickedBlock().getState();
		if (!Utilidades.isLojaValid(sign.getLines())) {
			return;
		}

		String placaLoja = "Loja";

		if (Utilidades.replaceShopName(sign.getLine(0)).equals(placaLoja)) {
			return;
		}

		Block block = e.getClickedBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
		if (!block.getType().equals(Material.CHEST) && !block.getType().equals(Material.TRAPPED_CHEST)) {
			return;
		}

		Player player = e.getPlayer();
		try {
			ItemStack item = Utilidades.getItemLoja(sign.getLines(), lojaConfig);
			Chest chest = (Chest) block.getState();
			comprarPeloBau(player, sign, chest, item);
		} catch (PlayerEqualsTargetException error1) {
			player.sendMessage("�a�lLOJA�f Voc� n�o pode comprar de si mesmo");
		} catch (PlayerMoneyException error2) {
			player.sendMessage("�a�lLOJA�f Voc� n�o tem dinheiro suficiente para comprar esse item");
		} catch (EmptyChestException error3) {
			player.sendMessage("�a�lLOJA�f N�o tem itens no ba� para compra");
		} catch (InventoryFullException erro4) {
			player.sendMessage("�a�lLOJA�f Voc� n�o tem espa�o no suficiente inventario");
		} catch (TargetUnknowException erro5) {
			player.sendMessage(
					"�a�lLOJA�f O jogador �b" + Utilidades.replaceShopName(sign.getLine(0) + "�f n�o foi encontrado"));
		} catch (SignUnknowBuy erro6) {
			player.sendMessage("�a�lLOJA�f Voc� s� pode �a�lVENDER�f nessa loja");
		}
	}

	@Deprecated
	public void comprarPeloBau(Player player, org.bukkit.block.Sign sign, Chest chest, ItemStack item)
			throws EmptyChestException, InventoryFullException, TargetUnknowException, PlayerMoneyException,
			PlayerEqualsTargetException, SignUnknowBuy {
		String line1 = Utilidades.replaceShopName(sign.getLine(0));
		if (line1.equals(player.getName())) {
			throw new PlayerEqualsTargetException(
					"O jogador '" + player.getName() + "' est� tentando comprar dele mesmo.");
		}
		Double priceBuy = Utilidades.getPrices(LojaEnum.COMPRAR, sign);
		if (priceBuy == 0.0D) {
			throw new SignUnknowBuy("A placa {x=" + sign.getLocation().getX() + ",y=" + sign.getLocation().getY()
					+ ",z=" + sign.getLocation().getZ() + "} n�o tem op��o para comprar.");
		}
		if (economy.getBalance(player) < priceBuy) {
			throw new PlayerMoneyException(
					"O jogador '" + player.getName() + "' n�o tem dinheiro suficiente para fazer a compra.");
		}
		int amountSign = Short.parseShort(Utilidades.replace(sign.getLine(1)));
		int amountChest = Utilidades.quantidadeItemInventory(chest.getInventory(), item);
		if (amountChest < amountSign) {
			throw new EmptyChestException("N�o tem item suficiente no ba� para fazer a compra.");
		}
		if (!Utilidades.haveSlotClearInv(player.getInventory(), item, amountSign)) {
			throw new InventoryFullException("Invent�rio do jogador est� lotado e n�o tem como receber os itens.");
		}
		OfflinePlayer target = Bukkit.getOfflinePlayer(line1);
		if (target == null) {
			throw new TargetUnknowException(
					"Jogador com o nick '" + Utilidades.replaceShopName(sign.getLine(0)) + "' n�o foi encontrado.");
		}
		economy.depositPlayer(target, priceBuy);
		economy.withdrawPlayer(player, priceBuy);

		item.setAmount(amountSign);

		player.getInventory().addItem(item);
		removeItemBau(chest, item, amountSign);

		String moneyFormatted = String.format("%.2f", priceBuy);
		player.sendMessage("�a�lLOJA�f Voc� comprou �b" + amountSign + "�f itens por �a" + moneyFormatted
				+ " coins �fdo jogador �e" + target.getName());
		player.updateInventory();
		LojaBuyOtherPlayer eventBuy = new LojaBuyOtherPlayer(target, player, priceBuy, item, amountSign);
		Bukkit.getServer().getPluginManager().callEvent(eventBuy);
	}

	private void removeItemBau(Chest chest, ItemStack itemStack, int amount) {
		int var = amount;
		for (int i = 0; i < chest.getInventory().getSize(); i++) {
			ItemStack item = chest.getInventory().getItem(i);
			if (item != null) {
				if (item.isSimilar(itemStack)) {
					if (var - item.getAmount() > 0) {
						var -= item.getAmount();
						chest.getInventory().setItem(i, new ItemStack(Material.AIR));
					} else {
						if (var - item.getAmount() == 0) {
							chest.getInventory().setItem(i, new ItemStack(Material.AIR));
							break;
						}
						if (var - item.getAmount() < 0) {
							item.setAmount(item.getAmount() - var);
							break;
						}
					}
				}
			}
		}
	}
}
