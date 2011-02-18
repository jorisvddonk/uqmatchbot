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
        String host = Settings.getInstance().getSettingString("IRCServer", "chat.freenode.net");
        String userName = Settings.getInstance().getSettingString("botUsername", "UQMatchbot");
        String realName = Settings.getInstance().getSettingString("botRealname", "UQMatchbot");
        String nickName = Settings.getInstance().getSettingString("botNickname", "UQMatchbot");
        Integer portMin = Settings.getInstance().getSettingInteger("IRCServerPortMin", 6667);
        Integer portMax = Settings.getInstance().getSettingInteger("IRCServerPortMax", 6667);
        String botIdent = Settings.getInstance().getSettingString("botIdentityPassword");
        String htmlLog = Settings.getInstance().getSettingString("botHTMLLogFile");
        String channel = Settings.getInstance().getSettingString("botChannel");

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
