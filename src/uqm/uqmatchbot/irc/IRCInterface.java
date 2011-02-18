/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot.irc;

/**
 * This interface specifies the communication channel FROM UQMatchbotService TO UQMatchbot
 * Perhaps this can be removed, now that the change of IRC backends has been completed....
  */
public interface IRCInterface {

    public void sendMessageToAllChannels(String message);

    public void sendMessage(String target, String message);

    public String getBotNickname();
}
