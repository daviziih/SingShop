package me.davi;

import me.davi.commands.ItemGenerate;
import me.davi.config.LojaConfig;
import me.davi.listeners.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Loja extends JavaPlugin {
	private Economy economy = null;

	final LojaConfig loja = new LojaConfig(this, "itens.yml");

	@Override
	public void onEnable() {

		setupEconomy();

		Bukkit.getPluginManager().registerEvents(new ComprarChest(economy, loja, this), this);
		Bukkit.getPluginManager().registerEvents(new PlacaComprar(economy, loja), this);
		Bukkit.getPluginManager().registerEvents(new CriarLoja(loja), this);
		Bukkit.getPluginManager().registerEvents(new VenderPlaca(economy, loja), this);
		Bukkit.getPluginManager().registerEvents(new VenderChest(economy, loja), this);

		Bukkit.getPluginManager().registerEvents(new QuerarLoja(this), this);
		Bukkit.getPluginManager().registerEvents(new AbrirBauLoja(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoinEvent(), this);

		getCommand("geraritem").setExecutor(new ItemGenerate(loja));
		
		Bukkit.getConsoleSender().sendMessage("Inicializando tarefas para inicialização...");

	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	@Override
	public void onDisable() {
		loja.saveConfig();
	}
}