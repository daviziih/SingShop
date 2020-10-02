package me.davi.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerJoinEvent implements Listener {

	@EventHandler(ignoreCancelled = true)
	private void onPreLogin(PlayerLoginEvent e) {
		if (e.getPlayer().getName().equalsIgnoreCase("Loja")) {
			e.setKickMessage("&cVocê não pode entrar no servidor com o mesmo nome da loja");
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, e.getKickMessage());
		}
	}
}
