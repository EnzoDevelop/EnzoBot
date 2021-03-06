/*
 * Enzo Bot, a multipurpose discord bot
 *
 * Copyright (c) 2018 William "Enzo" Johnstone
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package ml.enzodevelopment.enzobot.commands.basic;

import ml.enzodevelopment.enzobot.BuildConfig;
import ml.enzodevelopment.enzobot.objects.command.Command;
import ml.enzodevelopment.enzobot.objects.command.CommandCategory;
import ml.enzodevelopment.enzobot.EnzoBot;
import ml.enzodevelopment.enzobot.config.Config;
import ml.enzodevelopment.enzobot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ml.enzodevelopment.enzobot.BotListener.getCommand;

public class HelpCommand implements Command {

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent event) {
        if (args.length == 1) {
            EmbedBuilder helpBuilder = new EmbedBuilder();

            helpBuilder.setTitle("Help");
            helpBuilder.setAuthor(event.getAuthor().getName(), "https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591", event.getAuthor().getAvatarUrl());
            helpBuilder.setDescription("Here are all the commands currently available.");
            helpBuilder.setColor(Config.ENZO_BLUE);
            StringBuilder mainCommands = new StringBuilder();
            StringBuilder adminCommands = new StringBuilder();
            StringBuilder musicCommands = new StringBuilder();
            StringBuilder ownerComamnds = new StringBuilder();
            for (Command cmd : EnzoBot.cmdList) {
                if (cmd.getCategory() == CommandCategory.MAIN) {
                    mainCommands.append("`").append(cmd.getAliases().get(0)).append("` ");
                } else if (cmd.getCategory() == CommandCategory.MOD) {
                    adminCommands.append("`").append(cmd.getAliases().get(0)).append("` ");
                } else if (cmd.getCategory() == CommandCategory.OWNER) {
                    ownerComamnds.append("`").append(cmd.getAliases().get(0)).append("` ");
                } else if (cmd.getCategory() == CommandCategory.MUSIC) {
                    musicCommands.append("`").append(cmd.getAliases().get(0)).append("` ");
                }
            }

            helpBuilder.addField("Basic Commands", mainCommands.toString(), false);
            helpBuilder.addField("Admin Commands", adminCommands.toString(), false);
            helpBuilder.addField("Music Commands", musicCommands.toString(), false);
            if (event.getAuthor().getId().equals(BuildConfig.OWNER_ID)) {
                helpBuilder.addField("Owner Commands", ownerComamnds.toString(), false);
            }
            if (event.getChannel().getType() == ChannelType.TEXT && event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE)) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(helpBuilder.build()).queue(success -> event.getChannel().sendMessage(event.getAuthor().getAsMention() + " I sent you a DM containing help. :mailbox_with_mail:").queue(), failure -> event.getChannel().sendMessage(event.getMember().getAsMention() + " Oh no i couldn't DM you please check your privacy settings and ensure you haven't blocked me.").queue()));
            }
        } else if (args.length == 2) {
            Command cmd = getCommand(args[1].toLowerCase());
            if (cmd == null) {
                return;
            }
            getSpecififcHelp(args, event, cmd);
        }
    }

    private void getSpecififcHelp(String[] args, GuildMessageReceivedEvent event, Command command) {
        String botPrefix = GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix();
        if (args.length < 2) {
            return;
        } else if (args.length > 2) {
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(botPrefix + command.getAliases().get(0))
                .setDescription(command.getDesc())
                .setAuthor(event.getAuthor().getName(), "https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591", event.getAuthor().getAvatarUrl())
                .setColor(Config.ENZO_BLUE)
                .addField("Usage:", botPrefix + command.getUsage(), true)
                .addField("Aliases:", getAliasesString(command), true);
        if (event.getChannel().getType() == ChannelType.TEXT) {
            event.getChannel().sendMessage(builder.build()).queue();
        }

    }

    private String getAliasesString(Command command) {
        StringBuilder sb = new StringBuilder();
        for (String alias : command.getAliases()) {
            sb.append("`").append(alias).append("` ");
        }
        return sb.toString();
    }

    @Override
    public String getUsage() {
        return "help or help (command)";
    }

    @Override
    public String getDesc() {
        return "Sends you a private message containing help.";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(Arrays.asList("help"));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MAIN;
    }
}
