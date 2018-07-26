package ml.enzodevelopment.enzobot.config;

import ml.enzodevelopment.enzobot.connections.database.DBManager;
import io.github.binaryoverload.JSONConfig;
import ml.enzodevelopment.enzobot.utils.MusicUtils;

import java.awt.*;
import java.io.FileNotFoundException;

public class Config {

    public static DBManager DB;
    public static MusicUtils musicUtils;
    public static final Color ENZO_BLUE = new Color(51, 102, 153);
    public static final String BOT_DEV_CHANNEL = "431463562393944064";


    public static String Discord_Token;
    public static String dbConnection;
    public static String fallback_prefix;
    public static String API_Key;
    public static String sentry_dsn;
    public static String google_api;
    public static String config_file;
    public static Boolean dev_mode = false;


    public void loadConfig() {
        try {
            if (dev_mode) config_file = "dev_config.json";
            JSONConfig config = new JSONConfig(config_file);
            Discord_Token = config.getString("Config.token").get();
            dbConnection = config.getString("Config.dbString").get();
            fallback_prefix = config.getString("Config.fallback").get();
            API_Key = config.getString("Config.api_key").get();
            sentry_dsn = config.getString("Config.sentry_dsn").get();
            google_api = config.getString("Config.google_key").get();
            DB = new DBManager();
            musicUtils = new MusicUtils();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
