package ml.enzodevelopment.enzobot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import ml.enzodevelopment.enzobot.Command;
import ml.enzodevelopment.enzobot.music.GuildMusicManager;
import ml.enzodevelopment.enzobot.music.MusicUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PauseCommand implements Command {
    private MusicUtils musicUtils = new MusicUtils();

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent event) {
        GuildMusicManager mng = musicUtils.getMusicManager(event.getGuild());
        AudioPlayer player = mng.player;
        if (player.getPlayingTrack() == null) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Info");
            builder.setColor(Color.WHITE);
            builder.setDescription("Cannot pause or resume player because no track is loaded for playing.");
            event.getChannel().sendMessage(builder.build()).queue();
            return;
        }
        player.setPaused(!player.isPaused());
        if (player.isPaused()) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Info");
            builder.setColor(Color.WHITE);
            builder.setDescription(":play_pause: The player has been paused.");
            event.getChannel().sendMessage(builder.build()).queue();
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Info");
            builder.setColor(Color.WHITE);
            builder.setDescription(":play_pause: The player has resumed playing.");
            event.getChannel().sendMessage(builder.build()).queue();
        }
    }

    @Override
    public String getUsage() {
        return "pause";
    }

    @Override
    public String getDesc() {
        return "Pauses the player.";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(Arrays.asList("pause"));
    }

    @Override
    public String getType() {
        return "music";
    }
}
