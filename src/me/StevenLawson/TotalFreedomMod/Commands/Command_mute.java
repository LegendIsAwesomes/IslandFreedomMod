package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_AdminList;
import me.StevenLawson.TotalFreedomMod.TFM_PlayerData;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Toggles the mute status for a player", usage = "/<command> [<player> [-s] | list | purge | all]", aliases = "togglemute")
public class Command_mute extends TFM_Command
{

    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0 || args.length > 2)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("list"))
        {
            playerMsg("Muted players:");
            TFM_PlayerData info;
            int count = 0;
            for (Player mp : server.getOnlinePlayers())
            {
                info = TFM_PlayerData.getPlayerData(mp);
                if (info.isMuted())
                {
                    playerMsg("- " + mp.getName());
                    count++;
                }
            }
            if (count == 0)
            {
                playerMsg("- none");
            }
        }
        else
        {
            if (args[0].equalsIgnoreCase("purge"))
            {
                TFM_Util.adminAction(sender.getName(), "Toggling mute status all players - Muted: False.", true);
                TFM_PlayerData info;
                int count = 0;
                for (Player mp : server.getOnlinePlayers())
                {
                    info = TFM_PlayerData.getPlayerData(mp);
                    if (info.isMuted())
                    {
                        info.setMuted(false);
                        count++;
                    }
                }
                if (TotalFreedomMod.mutePurgeTask != null)
                {
                    TotalFreedomMod.mutePurgeTask.cancel();
                }
                playerMsg("Toggled mute status true for " + count + " players.");
            }
            else
            {
                if (args[0].equalsIgnoreCase("all"))
                {
                    TFM_Util.adminAction(sender.getName(), "Toggling mute status for all players - Muted: True.", true);

                    TFM_PlayerData playerdata;
                    int counter = 0;
                    for (Player player : server.getOnlinePlayers())
                    {
                        if (!TFM_AdminList.isSuperAdmin(player))
                        {
                            playerdata = TFM_PlayerData.getPlayerData(player);
                            playerdata.setMuted(true);
                            counter++;
                        }
                    }

                    if (TotalFreedomMod.mutePurgeTask != null)
                    {
                        TotalFreedomMod.mutePurgeTask.cancel();
                    }

                    TotalFreedomMod.mutePurgeTask = new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            TFM_Util.adminAction("MuteTimer", "Toggling mute status for all players - Muted: False.", false);
                            for (Player player : server.getOnlinePlayers())
                            {
                                TFM_PlayerData.getPlayerData(player).setMuted(false);
                            }
                        }
                    }.runTaskLater(plugin, 20L * 60L * 5L);

                    playerMsg("Toggled mute false for " + counter + " players.");
                }
                else
                {
                    final Player player = getPlayer(args[0]);

                    if (player == null)
                    {
                        sender.sendMessage(TFM_Command.PLAYER_NOT_FOUND);
                        return true;
                    }

                    TFM_PlayerData playerdata = TFM_PlayerData.getPlayerData(player);
                    if (playerdata.isMuted())
                    {
                        TFM_Util.adminAction(sender.getName(), "Toggling mute status for " + player.getName() + " - Muted: False", true);
                        playerdata.setMuted(false);
                        playerMsg("Unmuted " + player.getName());
                    }
                    else
                    {
                        if (!TFM_AdminList.isSuperAdmin(player))
                        {
                            TFM_Util.adminAction(sender.getName(), "Toggling mute status for " + player.getName() + " - Muted: True.", true);
                            playerdata.setMuted(true);

                            if (args.length == 2 && args[1].equalsIgnoreCase("-s"))
                            {
                                Command_smite.smite(player);
                            }

                            playerMsg("Muted " + player.getName());
                        }
                        else
                        {
                            playerMsg(player.getName() + " is a superadmin, and you can't toggle their mute status..");
                        }
                    }
                }
            }
        }

        return true;
    }
}
