/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot.irc;

import java.io.IOException;
import java.util.ArrayList;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import uqm.uqmatchbot.UQMatchbotService;

/**
 * The UQMatchbot IRC class. Uses the IRClib library.
 * This class processes the actual IRC logic, and has an instance of a UQMatchbotService
 * which is used to forward received messages to (for processing).
 *
 *
 *
 * @author joris
 */
public class UQMatchbot implements IRCInterface, IRCEventListener {

    private IRCConnection irc;
    private ArrayList<String> channels; //List of channels. These *MUST* have a # in front of them.
    private UQMatchbotService matchbotService;

    public UQMatchbot(String host, int portMin, int portMax, String pass, String nick, String username, String realname, String matchhistory_html, ArrayList<String> channels) throws IOException {
        this.channels = channels;
        irc = new IRCConnection(host, portMin, portMax, pass, nick, username, realname);
        irc.setPong(true);
        irc.addIRCEventListener(this);
        irc.connect();
        matchbotService = new UQMatchbotService(matchhistory_html, this);
    }

    public void sendMessageToAllChannels(String message) {
        for (String chan : channels) {
            irc.doPrivmsg(chan, message);
        }
    }

    public void sendMessage(String target, String message) {
        irc.doPrivmsg(target, message);
    }

    public String getBotNickname() {
        return irc.getNick();
    }

    private void printEvent(String contents) {
        System.out.println("[EVENT]" + contents);
    }

    /////////////
    public void onRegistered() {
        printEvent("registered");
        for (String chan : channels) {
            irc.doJoin(chan);
        }
    }

    public void onDisconnected() {
        printEvent("disconnected");
    }

    public void onError(String msg) {
        printEvent("error: " + msg);
    }

    public void onError(int num, String msg) {
        printEvent("error:[" + num + "] " + msg);
    }

    public void onInvite(String chan, IRCUser user, String passiveNick) {
        printEvent("invite: channel=" + chan + " user=" + user + " passiveNick=" + passiveNick);
    }

    public void onJoin(String chan, IRCUser user) {
        printEvent("join: channel=" + chan + " user=" + user);
    }

    public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
        printEvent("kick: chan=" + chan + " user=" + user + " passiveNick=" + passiveNick + " message=" + msg);
    }

    public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
        printEvent("onMode:TODO"); //TODO
    }

    public void onMode(IRCUser user, String passiveNick, String mode) {
        printEvent("onMode:TODO"); //TODO
    }

    public void onNick(IRCUser user, String newNick) {
        matchbotService.onNickChange(user.getNick(), user.getUsername(), user.getHost(), newNick);
        printEvent("onNick: user=" + user + " newNick=" + newNick);
    }

    public void onNotice(String target, IRCUser user, String msg) {
        printEvent("onNotice:TODO"); //TODO
    }

    public void onPart(String chan, IRCUser user, String msg) {
        printEvent("onPart:TODO"); //TODO
    }

    public void onPing(String ping) {
        //Ping events are automatically handled due ot the 'irc.setPong(true);' line above.
    }

    /**
     * (IRCEventListener method)
     * onPrivMsg is called whenever a 'PRIVMSG' is received from the IRC server.
     * Note that privmsg's are *NOT* by definition private messages.
     * If the 'target' is equal to the bot's nickname, *ONLY THEN* is this a private message. Keep that in mind. :)
     */
    public void onPrivmsg(String target, IRCUser user, String msg) {
        printEvent("onPrivmsg: target=" + target + " user=" + user + " message=" + msg);
        if (target.equals(irc.getNick())) {
            matchbotService.onPrivateMessage(user.getNick(), user.getUsername(), user.getHost(), msg); //Process a private message
        } else if (target.startsWith("#")) {
            if (channels.contains(target)) {
                matchbotService.onPublicMessage(target, user.getNick(), user.getUsername(), user.getHost(), msg); //Process a public message
            }
        }
    }

    public void onQuit(IRCUser user, String msg) {
        printEvent("onQuit:TODO"); //TODO
    }

    public void onReply(int num, String value, String msg) {
        printEvent("onReply:TODO"); //TODO
    }

    public void onTopic(String chan, IRCUser user, String topic) {
        printEvent("onTopic:TODO"); //TODO
    }

    public void unknown(String prefix, String command, String middle, String trailing) {
        printEvent("UNKNOWN: prefix=" + prefix + " command=" + command + " middle=" + middle + " trailing=" + trailing);
    }
}
