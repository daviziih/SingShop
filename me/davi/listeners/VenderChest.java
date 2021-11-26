package me.davi.listeners;

import me.davi.api.Utilidades;
import me.davi.config.LojaConfig;
import me.davi.enums.LojaEnum;
import me.davi.exceptions.*;
import me.davi.handlers.LojaSellOtherPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

public final class VenderChest implements Listener {

	private final Economy economy;
	private final LojaConfig lojaConfig;

	public VenderChest(Economy economy, LojaConfig lojaConfig) {
		this.economy = economy;
		this.lojaConfig = lojaConfig;
	}

	@EventHandler(ignoreCancelled = true)
	@Deprecated
	private void onComprar(PlayerInteractEvent e) {
		if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
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

		Player player = e.getPlayer();
		if (player.getGameMode().equals(GameMode.CREATIVE)) {
			if (player.isOp() || player.hasPermission("loja.cmd.admin")) {
				sign.getBlock().breakNaturally(null);
				e.setCancelled(true);
				return;
			}
		}

		Block block = e.getClickedBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
		if ((!block.getType().equals(Material.CHEST)) && (!block.getType().equals(Material.TRAPPED_CHEST))) {
			return;
		}

		try {
			Chest chest = (Chest) block.getState();
			ItemStack item = Utilidades.getItemLoja(sign.getLines(), lojaConfig);
			venderPelaPlaca(player, sign, chest, item);
		} catch (PlayerEqualsTargetException error) {
			player.sendMessage("ßaßlLOJAßf VocÍ n„o pode vender de si mesmo");
		} catch (PlayerUnknowItemException error) {
			player.sendMessage("ßaßlLOJAßf VocÍ n„o tem itens suficiente para vender");
		} catch (TargetUnknowException error) {
			player.sendMessage(
					"ßaßlLOJAßf O jogador ßb" + Utilidades.replaceShopName(sign.getLine(0) + "ßf n„o foi encontrado"));
		} catch (SignUnknowSell error) {
			player.sendMessage("ßaßlLOJAßf VocÍ sÛ pode ßcßlCOMPRARßf nessa loja");
		} catch (InventoryFullException error) {
			player.sendMessage("ßaßlLOJAßf Este ba˙ que vocÍ est· tentando vender est· lotado");
		} catch (TargetMoneyException error) {
			player.sendMessage("ßaßlLOJAßf O jogador ßb" + Utilidades.replaceShopName(sign.getLine(0))
					+ "ßf n„o tem dinheiro suficiente para comprar seu item");
		}
	}

	@Deprecated
	public void venderPelaPlaca(Player player, org.bukkit.block.Sign sign, Chest chest, ItemStack item)
			throws PlayerEqualsTargetException, PlayerUnknowItemException, TargetUnknowException, SignUnknowSell,
			InventoryFullException, TargetMoneyException {
		String line1 = Utilidades.replaceShopName(sign.getLine(0));
		if (line1.equals(player.getName())) {
			throw new PlayerEqualsTargetException(
					"O jogador '" + player.getName() + "' est√° tentando vender para ele mesmo.");
		}
		double priceSell = Utilidades.getPrices(LojaEnum.VENDER, sign);
		if (priceSell == 0.0D) {
			throw new SignUnknowSell("A placa {x=" + sign.getLocation().getX() + ",y=" + sign.getLocation().getY()
					+ ",z=" + sign.getLocation().getZ() + "} n√£o tem op√ß√£o para vender.");
		}
		int amoutItemPlayerHas = Utilidades.quantidadeItemInventory(player.getInventory(), item);
		if (amoutItemPlayerHas == 0) {
			throw new PlayerUnknowItemException("O jogador '" + player.getName()
					+ "' est√° tentando vender um item que ele n√£o tem no invent√°rio.");
		}
		OfflinePlayer target = Bukkit.getOfflinePlayer(line1);
		if (target == null) {
			throw new TargetUnknowException("Jogador com o nick '" + line1 + "' n√£o foi encontrado.");
		}
		if (!Utilidades.haveSlotClearInv(chest.getInventory(), item, amoutItemPlayerHas)) {
			throw new InventoryFullException("O ba√∫ {x=" + chest.getLocation().getX() + ",y= + "
					+ chest.getLocation().getY() + ",z=" + chest.getLocation().getZ()
					+ "} n√£o tem espa√ßo para receber itens de venda do jogador ." + player.getName());
		}
		double amoutItemSign = Integer.parseInt(Utilidades.replace(sign.getLine(1)));
		double finalValueSale = ((double) amoutItemPlayerHas / amoutItemSign) * priceSell;

		if (economy.getBalance(target) < finalValueSale) {
			throw new TargetMoneyException("O jogador " + target.getName() + " n√£o tem dinheiro para pagar o jogador "
					+ player.getName() + " pela venda.");
		}

		String moneyFormatted = String.format("%.2f", finalValueSale);
		player.sendMessage("ßaßlLOJAßf VocÍ vendeu ßb" + amoutItemPlayerHas + "ßf itens por ßa" + moneyFormatted
				+ " coins ßfpara o jogador ße" + target.getName());

		item.setAmount(amoutItemPlayerHas);
		player.getInventory().removeItem(item);

		player.updateInventory();

		if (item.getMaxStackSize() != 1) {
			chest.getInventory().addItem(item);
		} else {
			item.setAmount(1);
			for (int a = 0; a < amoutItemPlayerHas; a++) {
				for (int b = 0; b < chest.getInventory().getSize(); b++) {
					if (chest.getInventory().getItem(b) == null) {
						chest.getInventory().setItem(b, item);
						break;
					}
				}
			}

		}

		economy.withdrawPlayer(target, finalValueSale);
		economy.depositPlayer(player, finalValueSale);

		LojaSellOtherPlayer eventBuy = new LojaSellOtherPlayer(target, player, finalValueSale, item,
				amoutItemPlayerHas);
		Bukkit.getServer().getPluginManager().callEvent(eventBuy);
	}
}
