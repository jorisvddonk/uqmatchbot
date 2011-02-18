/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot;

import java.io.IOException;
import java.util.ArrayList;
import uqm.uqmatchbot.filestuff.Settings;
import uqm.uqmatchbot.irc.UQMatchbot;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String host = Settings.getInstance().getSetting("IRCServer", "chat.freenode.net");
        String userName = Settings.getInstance().getSetting("botUsername", "UQMatchbot");
        String realName = Settings.getInstance().getSetting("botRealname", "UQMatchbot");
        String nickName = Settings.getInstance().getSetting("botNickname", "UQMatchbot");
        Integer portMin = Integer.valueOf(Settings.getInstance().getSetting("IRCServerPortMin", "6667"));
        Integer portMax = Integer.valueOf(Settings.getInstance().getSetting("IRCServerPortMax", "6667"));
        String botIdent = Settings.getInstance().getSetting("botIdentityPassword");
        String htmlLog = Settings.getInstance().getSetting("botHTMLLogFile", null);
        String channel = Settings.getInstance().getSetting("botChannel", "#uqm-arena");

        ArrayList<String> channels = new ArrayList<String>();
        if (!channel.startsWith("#")) {
            channel = "#" + channel;
        }
        channels.add(channel);

        UQMatchbot ircbot = new UQMatchbot(host, portMin, portMax, botIdent, nickName, userName, realName, htmlLog, channels);
        while (true) {
        }
    }
}
