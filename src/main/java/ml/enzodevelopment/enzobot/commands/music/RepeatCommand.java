package ml.enzodevelopment.enzobot.commands.music;

import ml.enzodevelopment.enzobot.objects.command.Command;
import ml.enzodevelopment.enzobot.objects.command.CommandCategory;
import ml.enzodevelopment.enzobot.audio.GuildMusicManager;
import ml.enzodevelopment.enzobot.audio.MusicUtils;
import ml.enzodevelopment.enzobot.config.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepeatCommand implements Command {
    private MusicUtils musicUtils = Config.musicUtils;

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent event) {
        GuildMusicManager mng = musicUtils.getMusicManager(event.getGuild());
        mng.scheduler.setRepeating(!mng.scheduler.isRepeating());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Info");
        builder.setColor(Color.white);
        builder.setDescription("Player is: " + (mng.scheduler.isRepeating() ? "repeating" : "not repeating"));
        event.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public String getUsage() {
        return "loop";
    }

    @Override
    public String getDesc() {
        return "Loops the currently playing track.";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(Arrays.asList("repeat", "loop"));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }
}
