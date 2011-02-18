/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot.filestuff;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings class. Singleton object that manages settings. Saves and loads from an XML file
 * TODO: Move from XStream for saving this to some other library.
 */
public class Settings {
    private static final String CONFFILE = "uqmatchbot_conf.xml"; // The configuration filename
    private static Settings instance; // Instance of this object.
    private static XStream xstream; // Instance of the XStream class for serialization purposes
    @XStreamAlias("Strings")
    private HashMap<String, String> settings_Strings;
    @XStreamAlias("Booleans")
    private HashMap<String, Boolean> settings_Booleans;
    @XStreamAlias("Integers")
    private HashMap<String, Integer> settings_Integers;
    @XStreamAlias("Doubles")
    private HashMap<String, Double> settings_Doubles;

    public static Settings getInstance() {
        if (xstream == null) {
            xstream = new XStream(new DomDriver());
            xstream.alias("Settings", Settings.class);
            xstream.processAnnotations(Settings.class);
        }
        if (instance == null) {
            File f = new File(CONFFILE);
            if (f.exists()) {
                try {
                    instance = (Settings) xstream.fromXML(new FileInputStream(f));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Settings file ''{0}'' does not exist! Not loading settings. Prepare for unforseen consequences.", f.getAbsolutePath());
                instance = new Settings();
            }
        }
        instance.init_maps_if_null();
        return instance;
    }

    private Settings() {
        settings_Strings = new HashMap<String, String>();
        settings_Booleans = new HashMap<String, Boolean>();
        settings_Integers = new HashMap<String, Integer>();
        settings_Doubles = new HashMap<String, Double>();
    }

    private void init_maps_if_null() {
        if (settings_Strings == null) {
            settings_Strings = new HashMap<String, String>();
        }
        if (settings_Booleans == null) {
            settings_Booleans = new HashMap<String, Boolean>();
        }
        if (settings_Integers == null) {
            settings_Integers = new HashMap<String, Integer>();
        }
        if (settings_Doubles == null) {
            settings_Doubles = new HashMap<String, Double>();
        }
    }

    public void setSetting(String settingName, String settingValue) {
        settings_Strings.put(settingName, settingValue);
    }

    public void setSetting(String settingName, Boolean settingValue) {
        settings_Booleans.put(settingName, settingValue);
    }

    public void setSetting(String settingName, Integer settingValue) {
        settings_Integers.put(settingName, settingValue);
    }

    public void setSetting(String settingName, Double settingValue) {
        settings_Doubles.put(settingName, settingValue);
    }

    public String getSettingString(String settingName) {
        return settings_Strings.get(settingName);
    }

    public String getSettingString(String settingName, String default_if_null) {
        if (!settings_Strings.containsKey(settingName)) {
            return default_if_null;
        } else {
            return settings_Strings.get(settingName);
        }
    }

    public Integer getSettingInteger(String settingName) {
        return settings_Integers.get(settingName);
    }

    public Integer getSettingInteger(String settingName, Integer default_if_null) {
        if (settings_Integers.containsKey(settingName)) {
            return settings_Integers.get(settingName);
        } else {
            return default_if_null;
        }
    }

    public Double getSettingDouble(String settingName) {
        return settings_Doubles.get(settingName);
    }

    public Double getSettingDouble(String settingName, Double default_if_null) {
        if (settings_Doubles.containsKey(settingName)) {
            return settings_Doubles.get(settingName);
        } else {
            return default_if_null;
        }
    }

    public Boolean getSettingBoolean(String settingName) {
        return settings_Booleans.get(settingName);
    }

    public Boolean getSettingBoolean(String settingName, Boolean default_if_null) {
        if (settings_Booleans.containsKey(settingName)) {
            return settings_Booleans.get(settingName);
        } else {
            return default_if_null;
        }
    }

    public void saveSettings() throws IOException {
        FileWriter fo = null;
        String xml = xstream.toXML(this);
        fo = new FileWriter(Settings.CONFFILE);
        fo.write(xml);
        fo.close();
    }
}
