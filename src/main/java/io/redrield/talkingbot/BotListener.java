package io.redrield.talkingbot;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Random;

public class BotListener implements Listener {

    TalkingBot plugin;

    public BotListener(TalkingBot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bot-prefix"));
        String msg = e.getMessage();
        Player p = e.getPlayer();
        ConfigurationSection conf = plugin.getConfig().getConfigurationSection("special-match");
        String[] split = msg.split(" ");
        for(String s : split) {
            if(conf.contains(s)) {
                if(conf.getBoolean(s + ".block")) {
                    e.setCancelled(true);
                }
                Bukkit.broadcastMessage(prefix + ChatColor.translateAlternateColorCodes('&', conf.getString(s + ".response")));
            }
        }
        StringBuilder lookNew = new StringBuilder();
        if(plugin.getToggleState().get(p) || !p.hasPermission("chatbot.interact")) {
            return;
        }
        for(int i = 0; i < split.length; i++) {
            if(i==0) {
                lookNew.append(split[i]);
                continue;
            }
            lookNew.append("-").append(split[i]);
            if(plugin.getConfig().contains("miscellaneous." + lookNew.toString().toLowerCase())) {
                StringBuilder sb = new StringBuilder();
                for(int j = i+1; j<split.length; j++) {
                    sb.append(j!=i+1?" ":"").append(split[j]);
                }
                boolean match = false;
                for(String s : plugin.getConfig().getStringList("ignore-words")) {
                    if(sb.toString().replace("!", "").replace("?", "").replace(".", "").equalsIgnoreCase(s)) {
                        match = true;
                        break;
                    }
                }
                if(match) break;
                sb = new StringBuilder();
            }
        }
        if(plugin.getConfig().contains("miscellaneous." + lookNew.toString().toLowerCase())) {
            List<String> selection = plugin.getConfig().getStringList("miscellaneous." + lookNew.toString());
            String say = ChatColor.translateAlternateColorCodes('&', selection.get(new Random().nextInt(selection.size())).replace("%player%", p.getName()));
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> Bukkit.broadcastMessage(prefix + " " + say));
            return;
        }
        if(split.length>0 && split[0].equalsIgnoreCase(plugin.getConfig().getString("bot-name"))) {
            String[] lookup = msg.substring(plugin.getConfig().getString("bot-name").length()).split(" ");
            if(lookup.length == 1) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    List<String> selection = plugin.getConfig().getStringList("no-match.bot-name-only");
                    String say = selection.get(new Random().nextInt(selection.size()));
                    say = say.replace("%player%", p.getName());
                    Bukkit.broadcastMessage(prefix + " " + say);
                }, plugin.getConfig().getLong("response-speed"));
                return;
            }

            String look = "sayings";
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i < lookup.length; i++) {
                look = look + "." + lookup[i];
                if(plugin.getConfig().contains(look)) {
                    for(int j = i+1; j < lookup.length; j++) {
                        sb.append(j!=i+1?" ":"").append(lookup[j]);
                    }
                    if(plugin.getConfig().getStringList("ignore-words").contains(sb.toString())) {
                        break;
                    }
                    sb = new StringBuilder();
                }
            }

            if(plugin.getConfig().contains(look)) {
                List<String> selection = plugin.getConfig().getStringList(look);
                final String say = selection.get(new Random().nextInt(selection.size())).replace("%player%", p.getName());
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getServer().broadcastMessage(prefix + " " + say), plugin.getConfig().getLong("response-speed"));
            }else {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    List<String> selection = plugin.getConfig().getStringList("no-match.question-not-found");
                    String say = selection.get(new Random().nextInt(selection.size()));
                    say = say.replace("%player%", p.getName());
                    Bukkit.broadcastMessage(prefix + " " +  say);
                }, plugin.getConfig().getLong("response-speed"));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.getToggleState().put(e.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getToggleState().remove(e.getPlayer());
    }
}