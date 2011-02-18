/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot.filestuff;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

/**
 * Settings class. Singleton object that manages settings.
 * Saves and loads settings from an .ini file.
 */
public class Settings {

    private static final String CONFFILE = "uqmatchbot_conf.ini"; // The configuration filename
    private static Settings instance; // Instance of this object.
    private HashMap<String, String> settings_Strings;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
            File f = new File(CONFFILE);
            if (f.exists()) {
                try {
                    Wini ini = new Wini(f);
                    instance.settings_Strings = new HashMap<String, String>(ini.get("Settings"));
                } catch (InvalidFileFormatException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Configuration .ini file ''{0}'' not found! Prepare for unforseen consequences. Maybe.", f.getAbsolutePath());
            }
        }
        return instance;
    }

    private Settings() {
        settings_Strings = new HashMap<String, String>();
    }

    public void setSetting(String settingName, String settingValue) {
        settings_Strings.put(settingName, settingValue);
    }

    public String getSetting(String settingName) {
        return settings_Strings.get(settingName);
    }

    public String getSetting(String settingName, String default_if_null) {
        if (!settings_Strings.containsKey(settingName)) {
            return default_if_null;
        } else {
            return settings_Strings.get(settingName);
        }
    }

    public void saveSettings() throws IOException {
        Wini ini = new Wini();
        for (String s : settings_Strings.keySet()) {
            ini.put("Settings", s, settings_Strings.get(s));
        }
        ini.store(new File(CONFFILE));
    }
}
