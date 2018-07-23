package ml.enzodevelopment.enzobot.music.objects.spotify;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import ml.enzodevelopment.enzobot.utils.Config;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ml.duncte123.skybot.utils.YoutubeUtils.getVideoById;
import static ml.duncte123.skybot.utils.YoutubeUtils.searchYoutube;

public class SpotifyAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

    private static final String PROTOCOL_REGEX = "?:spotify:(track:)|(http://|https://)[a-z]+\\.";
    private static final String DOMAIN_REGEX = "spotify\\.com/";
    private static final String TRACK_REGEX = "track/([a-zA-z0-9]+)";
    private static final String ALBUM_REGEX = "album/([a-zA-z0-9]+)";
    private static final String PLAYLIST_REGEX = "user/(.*)/playlist/([a-zA-z0-9]+)";
    private static final String REST_REGEX = "(?:.*)";
    private static final String SPOTIFY_BASE_REGEX = PROTOCOL_REGEX + DOMAIN_REGEX;

    private static final Pattern SPOTIFY_TRACK_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + TRACK_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_ALBUM_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ALBUM_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ")" + PLAYLIST_REGEX + REST_REGEX + "$");
    private static final Pattern SPOTIFY_SECOND_PLAYLIST_REGEX = Pattern.compile("^(?:spotify:user:)(.*)(?::playlist:)(.*)$");
    private final SpotifyApi spotifyApi;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final ScheduledExecutorService service;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        String defaultValue = "To use Spotify search, please create an app over at https://developer.spotify.com/web-api/";
        String clientId = Config.spotifyClientID;
        String clientSecret = Config.spotifyClientSecret;
        String youtubeApiKey = Config.google_api;
        if (clientId == null || clientSecret == null || clientId.equals(defaultValue) || clientId.equals(defaultValue)
                || youtubeApiKey.isEmpty()) {
            logger.error("Could not load Spotify keys\n" + defaultValue);
            this.spotifyApi = null;
            this.service = null;
            this.youtubeAudioSourceManager = null;
        } else {
            this.youtubeAudioSourceManager = youtubeAudioSourceManager;
            this.spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .build();
            this.service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Spotify-Token-Update-Thread"));
            service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);

        }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        // not needed
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        // also not needed
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {

        if (isSpotifyAlbum(reference.identifier)) {
            Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final List<AudioTrack> playList = new ArrayList<>();

                    final Future<Album> albumFuture = spotifyApi.getAlbum(res.group(res.groupCount())).build().executeAsync();
                    final Album album = albumFuture.get();

                    for (TrackSimplified t : album.getTracks().getItems()) {
                        List<SearchResult> results = searchYoutube(album.getArtists()[0].getName() + " - " + t.getName());
                        playList.addAll(doThingWithPlaylist(results));
                    }

                    return new BasicAudioPlaylist(album.getName(), playList, playList.get(0), false);
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
                }
            }
        } else if (isSpotifyPlaylist(reference.identifier)) {
            Matcher res = getSpotifyPlaylistFromString(reference.identifier);
            if (res.matches()) {

                try {
                    final List<AudioTrack> finalPlaylist = new ArrayList<>();

                    final Future<Playlist> playlistFuture = spotifyApi.getPlaylist(res.group(res.groupCount() - 1),
                            res.group(res.groupCount())).build().executeAsync();
                    final Playlist spotifyPlaylist = playlistFuture.get();

                    for (PlaylistTrack playlistTrack : spotifyPlaylist.getTracks().getItems()) {
                        List<SearchResult> results = searchYoutube(playlistTrack.getTrack().getArtists()[0].getName()
                                + " - " + playlistTrack.getTrack().getName());
                        finalPlaylist.addAll(doThingWithPlaylist(results));
                    }

                    return new BasicAudioPlaylist(spotifyPlaylist.getName(), finalPlaylist, finalPlaylist.get(0), false);
                } catch (IllegalArgumentException ex) {
                    throw new FriendlyException("This playlist could not be loaded, make sure that it's public", FriendlyException.Severity.FAULT, ex);
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
                }
            }
        } else if (isSpotyfyTrack(reference.identifier)) {
            Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);
            if (res.matches()) {

                try {
                    final Future<Track> trackFuture = spotifyApi.getTrack(res.group(res.groupCount())).build().executeAsync();
                    final Track track = trackFuture.get();

                    List<SearchResult> results = searchYoutube(track.getArtists()[0].getName() + " - " + track.getName());

                    Video v = getVideoById(results.get(0).getId().getVideoId());
                    return audioTrackFromVideo(v);
                } catch (Exception e) {
                    //logger.error("Something went wrong!", e);
                    throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
                }
            }
        }

        return null;
    }


    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpotifyAudioTrack(trackInfo, youtubeAudioSourceManager);
    }

    @Override
    public void shutdown() {
        if (this.youtubeAudioSourceManager != null)
            this.youtubeAudioSourceManager.shutdown();
        if (this.service != null)
            this.service.shutdown();

    }

    private boolean isSpotyfyTrack(String input) {
        return SPOTIFY_TRACK_REGEX.matcher(input).matches();
    }

    private boolean isSpotifyAlbum(String input) {
        return SPOTIFY_ALBUM_REGEX.matcher(input).matches();
    }

    private boolean isSpotifyPlaylist(String input) {
        return SPOTIFY_PLAYLIST_REGEX.matcher(input).matches();
    }

    private void updateAccessToken() {
        try {
            final ClientCredentialsRequest request = spotifyApi.clientCredentials().build();
            final Future<ClientCredentials> clientCredentialsFuture = request.executeAsync();
            final ClientCredentials clientCredentials = clientCredentialsFuture.get();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            logger.debug("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            logger.debug("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            logger.error("Error while fetching Spotify token", e);
        }
    }

    private Matcher getSpotifyPlaylistFromString(String input) {
        Matcher match1 = SPOTIFY_PLAYLIST_REGEX.matcher(input);
        if (match1.matches())
            return match1;
        else
            return SPOTIFY_SECOND_PLAYLIST_REGEX.matcher(input);
    }

    private List<AudioTrack> doThingWithPlaylist(List<SearchResult> results) throws Exception {
        List<AudioTrack> playList = new ArrayList<>();
        if (results.size() > 0) {
            SearchResult video = results.get(0);
            ResourceId rId = video.getId();
            if (rId.getKind().equals("youtube#video")) {
                Video v = getVideoById(video.getId().getVideoId());
                playList.add(audioTrackFromVideo(v));
            }
        }
        return playList;
    }

    private AudioTrack audioTrackFromVideo(Video v) {
        return new SpotifyAudioTrack(new AudioTrackInfo(
                v.getSnippet().getTitle(),
                v.getSnippet().getChannelId(),
                toLongDuration(v.getContentDetails().getDuration()),
                v.getId(),
                false,
                "https://youtube.com/watch?v=" + v.getId()
        ), youtubeAudioSourceManager);
    }

    private long toLongDuration(String dur) {
        String time = dur.substring(2);
        long duration = 0L;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for (Object[] index1 : indexs) {
            int index = time.indexOf((String) index1[0]);
            if (index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (int) index1[1] * 1000;
                time = time.substring(value.length() + 1);
            }
        }
        return duration;
    }
}
