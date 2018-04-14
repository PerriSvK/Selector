package sk.perri.selector;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class Selector extends Plugin
{
    private Vector<Command> cmds = new Vector<>();

    @Override
    public void onEnable()
    {
        final Configuration[] config = {loadConfig()};

        if(config[0] != null)
            registerCmds(config[0]);

        getProxy().getPluginManager().registerCommand(this, new Command("selector")
        {
            @Override
            public void execute(CommandSender sender, String[] args)
            {
                if(!sender.hasPermission("selector.cmd"))
                {
                    sender.sendMessage("§cNemáš oprávnení na tento příkaz!");
                    return;
                }

                if(args.length == 0)
                {
                    sender.sendMessage("§3§lSelector §7by §2§lPerri");
                    return;
                }

                if(args[0].equalsIgnoreCase("reload"))
                {
                    cmds.forEach(c -> getProxy().getPluginManager().unregisterCommand(c));
                    cmds.clear();
                    config[0] = loadConfig();
                    if(config[0] != null)
                        registerCmds(config[0]);

                    sender.sendMessage("§ePlugin reloadovany!");
                }
            }
        });
    }

    private Configuration loadConfig()
    {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        if(!new File(getDataFolder(), "config.yml").exists())
        {
            try
            {
                File f = new File(getDataFolder(), "config.yml");
                f.createNewFile();
            }
            catch (IOException ignored){}
        }

        try
        {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        }
        catch (IOException e)
        {
            getLogger().warning("Neviem nacitat config.yml");
            return null;
        }
    }

    private void registerCmds(Configuration config)
    {
        config.getKeys().forEach(k ->
        {
            cmds.add(new Command(k)
            {
                @Override
                public void execute(CommandSender sender, String[] strings)
                {
                    if(!(sender instanceof ProxiedPlayer))
                    {
                        sender.sendMessage(ChatColor.RED+"Nemůžu tě přepojit!");
                        return;
                    }

                    if(((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase(config.getString(k)))
                    {
                        sender.sendMessage("§cJiž jsi připojen na server "+k+"!");
                        return;
                    }

                    ServerInfo target = ProxyServer.getInstance().getServerInfo(config.getString(k));
                    ((ProxiedPlayer) sender).connect(target);
                }
            });
            getProxy().getPluginManager().registerCommand(this, cmds.lastElement());
        });
    }

    @Override
    public void onDisable()
    {

    }
}
