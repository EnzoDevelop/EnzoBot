package gravity.gbot.commands.basic;

import gravity.gbot.Command;
import gravity.gbot.Main;
import gravity.gbot.music.MusicMaps;
import gravity.gbot.utils.Config;
import gravity.gbot.utils.GuildConfig;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public class HelpCommand implements Command {

    private GuildConfig config = new GuildConfig();

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent event) {
        String admincheck = config.isAdmin(event.getAuthor().getId(), event.getGuild().getId(), event.getJDA());
        String bot_prefix = config.getPrefix(event.getGuild().getId(), this.getClass().getName());
        EmbedBuilder builder0 = new EmbedBuilder();

        builder0.setTitle("Help");
        builder0.setAuthor(event.getAuthor().getName(), "https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591", event.getAuthor().getAvatarUrl());
        builder0.setDescription("Here are all the commands currently available.");
        builder0.setColor(Config.GBot_Blue);

        EmbedBuilder builder1 = new EmbedBuilder();
        builder1.setTitle("Basic Commands");
        builder1.setColor(Config.GBot_Blue);

        EmbedBuilder builder2 = new EmbedBuilder();
        builder2.setTitle("Music Commands");
        builder2.setColor(Config.GBot_Blue);

        EmbedBuilder builder3 = new EmbedBuilder();
        builder3.setTitle("Admin Commands");
        builder3.setColor(Config.GBot_Blue);

        EmbedBuilder builder4 = new EmbedBuilder();
        builder4.setTitle("Bot Owner Commands");
        builder4.setColor(Config.GBot_Blue);

        for (Command command : Main.cmdlist) {
            if (command.cmdType() != null) {
                if (command.cmdType().equals("public")) {
                    builder1.addField(bot_prefix + command.getAlias(), "**Usage:** *" + bot_prefix + command.cmdUsage() + "*\n**Description:** *" + command.cmdDesc() + "*", false);
                }
                if (command.cmdType().equals("admin")) {
                    builder3.addField(bot_prefix + command.getAlias(), "**Usage:** *" + bot_prefix + command.cmdUsage() + "*\n**Description:** *" + command.cmdDesc() + "*", false);
                }
                if (command.cmdType().equals("owner")) {
                    builder4.addField(bot_prefix + command.getAlias(), "**Usage:** *" + bot_prefix + command.cmdUsage() + "*\n**Description:** *" + command.cmdDesc() + "*", false);
                }
            }
        }

        for (String cmd : MusicMaps.musicCmds) {
            String[] command = cmd.split("\\|");
            String Alias = command[0];
            String Description = command[1];
            String Usage = command[2];
            builder2.addField(bot_prefix + "m " + Alias, "**Usage:** *" + bot_prefix + "m " + Usage + "*\n**Description:** *" + Description + "*", false);
        }


        if (event.getChannel().getType() == ChannelType.PRIVATE) {
            event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder0.build()).queue(null, failure -> {}));
            event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder1.build()).queue(null, failure -> {}));
            event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder2.build()).queue(null, failure -> {}));
            if (admincheck != null) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder3.build()).queue(null, failure -> {}));
            }
            if (event.getAuthor().getId().equals("205056315351891969")) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder4.build()).queue(null, failure -> {}));
            }
        } else if (event.getChannel().getType() == ChannelType.TEXT) {
            try {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder0.build()).queue(success -> event.getChannel().sendMessage(event.getAuthor().getAsMention() + " I sent you a DM containing help. :mailbox_with_mail:").queue(), failure -> event.getChannel().sendMessage(event.getMember().getAsMention() + " Oh no i couldn't DM you please check your privacy settings and ensure you haven't blocked me.").queue()));
            } catch (InsufficientPermissionException e) {

            }
            event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder1.build()).queue(null, failure -> {}));
            event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder2.build()).queue(null, failure -> {}));
            if (admincheck != null) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder3.build()).queue(null, failure -> {}));
            }
            if (event.getAuthor().getId().equals("205056315351891969")) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(builder4.build()).queue(null, failure -> {}));
            }
        }
    }

    public void HelpSpecific(String[] args, GuildMessageReceivedEvent event, String desc, String help, String alias) {
        String bot_prefix = config.getPrefix(event.getGuild().getId(), this.getClass().getName());
        if (args.length < 2) {
            return;
        } else if (args.length > 2) {
            return;
        }
        if (alias.equals(help)) {
            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle(bot_prefix + alias)
                    .setDescription(desc)
                    .setAuthor(event.getAuthor().getName(), "https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591", event.getAuthor().getAvatarUrl())
                    .setColor(Config.GBot_Blue)
                    .addField("Usage:", bot_prefix + "Help or " + bot_prefix + "help (command)", true)
                    .addField("Want me in your server?", "Hey!, want to add me to your server? [Click Here](https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591) to invite me to your server.", false);
            if (event.getChannel().getType() == ChannelType.PRIVATE) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage("This command is guild only").queue());
            } else if (event.getChannel().getType() == ChannelType.TEXT) {
                event.getChannel().sendMessage(builder.build()).queue();
            }

        } else {
            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle(bot_prefix + alias)
                    .setDescription(desc)
                    .setAuthor(event.getAuthor().getName(), "https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591", event.getAuthor().getAvatarUrl())
                    .setColor(Config.GBot_Blue)
                    .addField("Usage:", bot_prefix + help, true)
                    .addField("Want me in your server?", "Hey!, want to add me to your server? [Click Here](https://discordapp.com/oauth2/authorize?client_id=391558265265192961&scope=bot&permissions=2146958591) to invite me to your server.", false);
            if (event.getChannel().getType() == ChannelType.PRIVATE) {
                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage("This command is guild only").queue());
            } else if (event.getChannel().getType() == ChannelType.TEXT) {
                event.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }


    @Override
    public String cmdUsage() {
        return "help or help (command)";
    }

    @Override
    public String cmdDesc() {
        return "Sends you a private message containing help.";
    }

    @Override
    public String getAlias() {
        return "help";
    }

    @Override
    public String cmdType() {
        return "public";
    }
}