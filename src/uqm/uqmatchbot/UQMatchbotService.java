/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import uqm.uqmatchbot.domain.Botuser;
import uqm.uqmatchbot.domain.Login;
import uqm.uqmatchbot.domain.GameResult;
import uqm.uqmatchbot.irc.IRCInterface;

/**
 * The main UQMatchbot service class.
 * This class handles all of the UQMatchbot logic, and needs hooks to the actual
 * IRC bot to be able to send IRC messages.
 *
 * See uqm.uqmatchbot.irc.UQMatchbot.java for the IRC logic.
 *
 * NOTE:
 * UQMatchbot used a different library earlier. I (joris) had to refuctor things here
 * and there, and attach the 'business logic' to an entirely new IRC back-end.
 * The result of this refuctoring, is that things may seem odd here and there.
 * In some cases, 'login' may actually refer to a 'username' (NOT A NICKNAME).
 * Furthermore, you should notice that we're passing along Strings when dealing with users,
 * rather than with the IRClib's 'IRCUser' class. Now you know why. :)
 *
 * TODO's:
 * -Read through the code and clean things up (*ESPECIALLY*
 * -Clean up usages of Botuser.java and Login.java:
 *   *Fix the differentiation between the two. I can't even remember why there are seperate botuser and login classes!
 *   *Stop passing strings around for user identities, and use instantiations of a botuser/login class instead
 *   *...
 * -Fix the HTML generation cruft. That shit ain't nice!
 * -Project against HTML injection
 * -Kill off Serialization; use XStream (or a custom self-written format?) instead.
 * -Encrypt passwords. :)
 * -Better documentation
 * -...
 */
public class UQMatchbotService {

    private ArrayList<Login> logins;
    private ArrayList<Botuser> botusers;
    private ArrayList<GameResult> resultList;
    private ArrayList<GameResult> playedgames;
    private Random random;
    private String matchhistory_html;
    private IRCInterface irc;



    private static final String GAMES_FILENAME = "games.bot";
    private static final String LOGINS_FILENAME = "users.bot";

    /**
     * Constructor for UQMatchbotService
     * @param matchhistory_html The location of the HTML file to log matches to. Can be null to disable logging.
     * @param irc The instantiation of an IRCInterface. For communication purposes.
     */
    public UQMatchbotService(String matchhistory_html, IRCInterface irc) {
        this.irc = irc;
        this.matchhistory_html = matchhistory_html;
        logins = loadLogins(LOGINS_FILENAME);
        if (logins == null) {
            logins = new ArrayList<Login>();
        }
        playedgames = loadGames(GAMES_FILENAME);
        if (playedgames == null) {
            playedgames = new ArrayList<GameResult>();
        }
        botusers = new ArrayList<Botuser>();
        resultList = new ArrayList<GameResult>();

        random = new Random();
    }

    /**
     * Save games.
     * @param games
     * @param gamesFilename
     * @return
     */
    private boolean saveGames(ArrayList<GameResult> games, String gamesFilename) {
        try {
            FileOutputStream file = new FileOutputStream(gamesFilename);
            ObjectOutputStream objectos = new ObjectOutputStream(file);
            objectos.writeObject(games);
            objectos.close();
            file.close();
            return true;
        } catch (Exception e) {
            System.err.println("Exception thrown in IrcBot.saveGames: " + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Load games
     * @param gamesFilename
     * @return
     */
    private ArrayList<GameResult> loadGames(String gamesFilename) {
        //ArrayList<Login> retlist = null;
        try {
            FileInputStream file = new FileInputStream(gamesFilename);
            ObjectInputStream objectis = new ObjectInputStream(file);
            java.util.ArrayList<GameResult> retlist = (java.util.ArrayList<GameResult>) objectis.readObject();
            objectis.close();
            file.close();
            return retlist;
        } catch (Exception e) {
            System.err.println("Exception thrown in IrcBot.loadGames: " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Save logins
     * @param logins
     * @param loginsFilename
     * @return
     */
    private boolean saveLogins(ArrayList<Login> logins, String loginsFilename) {
        try {
            FileOutputStream file = new FileOutputStream(loginsFilename);
            ObjectOutputStream objectos = new ObjectOutputStream(file);
            objectos.writeObject(logins);
            objectos.close();
            file.close();
            return true;
        } catch (Exception e) {
            System.err.println("Exception thrown in IrcBot.saveLogins: " + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Load logins
     * @param loginsFilename
     * @return
     */
    private ArrayList<Login> loadLogins(String loginsFilename) {
        //ArrayList<Login> retlist = null;
        try {
            FileInputStream file = new FileInputStream(loginsFilename);
            ObjectInputStream objectis = new ObjectInputStream(file);
            java.util.ArrayList<Login> retlist = (java.util.ArrayList<Login>) objectis.readObject();
            objectis.close();
            file.close();
            return retlist;
        } catch (Exception e) {
            System.err.println("Exception thrown in IrcBot.loadLogins: " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Confirm a gameResult that hasn't been confirmed yet.
     * @param r
     */
    private void confirmGame(GameResult r) {
        irc.sendMessageToAllChannels(r.winner + " won a " + (r.scoremax > -1 ? r.scoremax + "pt " : "") + "match against " + r.loser + (r.scoreremain > -1 ? " with " + r.scoreremain + " points remaining!" : "!"));
        playedgames.add(r);
        saveGames(playedgames, GAMES_FILENAME);
        resultList.remove(r);
        if (matchhistory_html != null) {
            try {
                boolean exists = true;
                File f = new File(matchhistory_html);
                if (!f.exists()) {
                    exists = false;
                    f.createNewFile();
                }

                // Create file
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                String timestring = cal.get(cal.DAY_OF_MONTH) + "-" + cal.get(cal.MONTH) + "-" + cal.get(cal.YEAR) + " " + cal.get(cal.HOUR_OF_DAY) + ":" + cal.get(cal.MINUTE);
                java.io.FileWriter fstream = new java.io.FileWriter(matchhistory_html, true);
                java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
                if (!exists) {
                    out.write("<title>UQMatchbot match history</title><font color=blue size=6>UQMatchbot match history</font><br><i>All times are in UTC, and dates in dd-mm-yyyy</i><hr>");
                }
                out.write("[" + timestring + "] <font color=blue size=4><b>" + r.winner + "</b></font> won a " + (r.scoremax > -1 ? "<i>" + r.scoremax + "pt</i> " : "") + "match against <font color=blue size=4><b>" + r.loser + "</b></font>" + (r.scoreremain > -1 ? " with <i>" + r.scoreremain + "</i> points remaining!" : "!") + "<br>");
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage() + " - " + e.toString());
            }
        }
    }

    /**
     * Return a string of user accounts that are ready to play a game.
     * TODO: return an array (or something) and do the ToString-ifying somewhere else?
     * @return
     */
    private String getReadyBotusers() {
        String tempstring = "";
        for (Botuser b : botusers) {
            if (b.isReady()) {
                String s = b.nickname + "(" + ((b.readyuntil - java.util.Calendar.getInstance().getTimeInMillis()) / 60000) + ")";
                if (tempstring.length() > 0) {
                    tempstring = tempstring + ", " + s;
                } else {
                    tempstring = s;
                }
            }
        }
        return tempstring;
    }

    /**
     * Get all bot users as string
     * @return
     */
    private String getAllBotusers() {
        String tempstring = "";
        for (Botuser b : botusers) {
            String s = b.nickname;
            if (tempstring.length() > 0) {
                tempstring = tempstring + ", " + s;
            } else {
                tempstring = s;
            }
        }
        return tempstring;
    }


    /**
     * Get all logins as string
     * @return
     */
    private String getAllLogins() {
        String tempstring = "";
        for (Login l : logins) {
            String s = l.ident;
            if (tempstring.length() > 0) {
                tempstring = tempstring + ", " + s;
            } else {
                tempstring = s;
            }
        }
        return tempstring;
    }

    /**
     * Is someone logged in?
     * @param username The IRC username (NOT NICKNAME) of a user
     * @param hostname The hostname of a user
     * @return
     */
    private boolean isLoggedin(String username, String hostname) {
        for (Botuser b : botusers) {
            if (b.username.equals(username) && b.hostname.equals(hostname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a botuser based on an IRC username and hostname
     * @param username
     * @param hostname
     * @return
     */
    private Botuser getBotuser(String username, String hostname) {
        for (Botuser b : botusers) {
            if (b.username.equals(username) && b.hostname.equals(hostname)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Process a message said in any of the channels the bot is active in
     * @param channel the channel. starts with #
     * @param senderNickname the IRC nickname of whoever sent the message
     * @param senderUsername the IRC username of whoever sent the message (NOT NICKNAME)
     * @param senderHostname the IRC hostname of whoever sent the message
     * @param message the message itself
     */
    public void onPublicMessage(String channel, String senderNickname,
            String senderUsername, String senderHostname, String message) {
        //System.out.println("##" + channel + "^" + sender + "^" + login + "^" + hostname + "^" + message);
        if (message.equalsIgnoreCase("!help")) {
            sendMessage(channel, " *Available public commands: !readyusers. Keep in mind that most commands are direct-message commands. For a list of those commands: /msg " + irc.getBotNickname() + " !help");
        }
        if (message.equalsIgnoreCase("!readyusers")) {
            sendMessage(channel, " *Ready players: " + getReadyBotusers());
        }
    }

    /**
     * Process a private message from anyone to the bot ('/msg [BOTNAME] [MESSAGE]').
     * @param senderNickname the IRC nickname of whoever sent the message
     * @param senderUsername the IRC username of whoever sent the message (NOT NICKNAME)
     * @param senderHostname the IRC hostname of whoever sent the message
     * @param message The message
     *
     * TODO - Clean this horrible function up. It's a huge mess. Sorry. It's been a long while since I've coded this cruft. D:
     */
    public void onPrivateMessage(String senderNickname, String senderUsername, String senderHostname, String message) {
        //System.out.println("@@" + sender + "^" + login + "^" + hostname + "^" + message);

        if (message.equalsIgnoreCase("!readyusers")) {
            sendMessage(senderNickname, " *Ready players: " + getReadyBotusers());
        } else if (message.startsWith("!login")) {
            boolean loggedin = false;
            String[] tempstrarray = message.split(" ");
            boolean isalreadyloggedin = isLoggedin(senderUsername, senderHostname);
            Botuser tempBotuser = getBotuser(senderUsername, senderHostname);
            if (!isalreadyloggedin) {
                if (tempstrarray.length >= 3) {
                    boolean login_in_use = false;
                    for (Botuser b : botusers) {
                        if (b.authorizedIdent.equals(tempstrarray[1])) {
                            login_in_use = true;
                        }
                    }
                    if (!login_in_use) {
                        for (Login l : logins) {
                            if (l.checkCredentials(tempstrarray[1], tempstrarray[2]) == true && !loggedin) {
                                sendMessage(senderNickname, "You are now logged in as " + tempstrarray[1]);
                                loggedin = true;
                                botusers.add(new Botuser(senderNickname, senderUsername, senderHostname, tempstrarray[1]));
                            }
                        }
                        if (!loggedin) {
                            sendMessage(senderNickname, "Unknown username/password combination!");
                        }
                    } else {
                        sendMessage(senderNickname, "Some other user is already logged in with this username!");
                    }
                } else {
                    sendMessage(senderNickname, "Not enough parameters. Usage: !login <username> <password>");
                }
            } else {
                sendMessage(senderNickname, "You are already logged in as user: " + tempBotuser.authorizedIdent);
            }
        } else if (message.startsWith("!logout") || message.startsWith("!logoff")) {
            boolean wasloggedin = false;
            Botuser tempb = null;
            for (Botuser b : botusers) {
                if (b.username.equals(senderUsername) && b.hostname.equals(senderHostname)) {
                    sendMessage(senderNickname, "Now logging off as user: " + b.authorizedIdent);
                    /*b.authorized = false;
                    b.authorizedIdent = "";*/
                    wasloggedin = true;
                    tempb = b;
                }
            }
            if (tempb != null) {
                botusers.remove(tempb); //TODO: figure out if this should set the botuser's 'authorize' boolean to FALSE and the 'authorizedIdent' to null.....
            }
            if (!wasloggedin) {
                sendMessage(senderNickname, "Can't log out: you are not logged in!");
            }
        } else if (message.startsWith("!ready")) {
            Botuser b = getBotuser(senderUsername, senderHostname);
            if (b != null) {
                String[] tempstrarray = message.split(" ");
                if (tempstrarray.length >= 2) {
                    long templong;
                    try {
                        templong = Long.valueOf(tempstrarray[1]);
                    } catch (Exception e) {
                        templong = -100;
                    }
                    if (templong >= 0) {
                        long msecs = templong * 60000;
                        b.ready(msecs);
                        if (templong > 0) {
                            sendMessageToAllChannels(b.nickname + " is now ready to play a game for a duration of " + Long.valueOf(tempstrarray[1]) + " minutes!");
                        } else {
                            sendMessageToAllChannels(b.nickname + " is no longer available to play a game.");
                        }
                    } else {
                        sendMessage(senderNickname, "Error. usage: !ready <time in minutes>. !ready 0 to unready. <time in minutes> has to be larger or equal than 0, and has to be a number.");
                    }
                } else {
                    sendMessage(senderNickname, "Too few parameters. usage: !ready <time in minutes>. !ready 0 to unready.");
                }
            } else {
                sendMessage(senderNickname, "You are not logged in!");
            }
        } else if (message.startsWith("!chpass")) {
            if (!isLoggedin(senderUsername, senderHostname)) {
                sendMessage(senderNickname, "Please log in before using !chpass using !login");
            } else {
                String[] tempstrarray = message.split(" ");
                if (tempstrarray.length >= 3) {
                    Botuser b = getBotuser(senderUsername, senderHostname);
                    for (Login l : logins) {
                        if (b.authorizedIdent.equals(l.ident)) {
                            if (tempstrarray[1].equals(l.password)) {
                                try {
                                    l.password = tempstrarray[2];
                                    saveLogins(logins, LOGINS_FILENAME);
                                    sendMessage(senderNickname, "Changed password to " + tempstrarray[2]);
                                } catch (Exception e) {
                                    sendMessage(senderNickname, "Error occured!");
                                }
                            } else {
                                sendMessage(senderNickname, "Incorrect old password entered.");
                            }
                        }
                    }
                } else {
                    sendMessage(senderNickname, "Too few parameters. Usage: !chpass <oldpassword> <newpassword>");
                }
            }
        } else if (message.startsWith("!register")) {
            String[] tempstrarray = message.split(" ");
            boolean b = false;
            if (tempstrarray.length >= 2) {
                for (Login l : logins) {
                    if (l.ident.equals(tempstrarray[1])) {
                        b = true;
                    }
                }
                if (!b) {
                    if (!tempstrarray[1].contains("<") && !tempstrarray[1].contains(">")) {
                        String pwd = String.format("%04d", random.nextInt(9999));
                        try {
                            logins.add(new Login(tempstrarray[1], pwd));
                            saveLogins(logins, LOGINS_FILENAME);
                            sendMessage(senderNickname, "Username " + tempstrarray[1] + " has been registered. Password: " + pwd + " (please write this down somewhere)");
                            sendMessage(senderNickname, "To change your password, use the !chpass command (note: passwords are NOT securely stored, so please use a password you do not use for any other accounts)");
                            sendMessage(senderNickname, "You can now login to your account with !login.");
                        } catch (Exception e) {
                            sendMessage(senderNickname, "Some error occured!");
                        }
                    } else {
                        sendMessage(senderNickname, "Nickname to register contained illegal characters.");
                    }
                } else {
                    sendMessage(senderNickname, "Username " + tempstrarray[1] + " has already been registered!");
                }
            } else {
                sendMessage(senderNickname, "Too few parameters. usage: !register <username>");
            }
        } else if (message.startsWith("!debuginfo")) {
            sendMessage(senderNickname, "Free memory: " + Runtime.getRuntime().freeMemory() + ", max memory: " + Runtime.getRuntime().maxMemory() + ", total memory: " + Runtime.getRuntime().totalMemory());
            sendMessage(senderNickname, "Botusers: " + getAllBotusers());
            sendMessage(senderNickname, "Logins: " + getAllLogins());
        } else if (message.startsWith("!result")) {
            //!result claim <opponent> <win/lose> [max points] [points left]
            //!result confirm <claimID (timestamp)>
            //!result reject <claimID (timestamp)>
            String[] tempstrarray = message.split(" ");
            if (tempstrarray.length >= 3) { //satisfied minimum required arguments
                Botuser fromB = getBotuser(senderUsername, senderHostname);
                Botuser toB = null;
                if (fromB != null) { // user is logged in
                    if (tempstrarray[1].equals("claim")) {
                        if (tempstrarray.length >= 4) {
                            for (Botuser b : botusers) {
                                if (b.authorizedIdent.equals(tempstrarray[2])) {
                                    toB = b;
                                }
                            }
                            if (toB != null && toB != fromB) {
                                GameResult cResult = null;
                                boolean win = false;
                                if (tempstrarray[3].equals("win")) {
                                    win = true;
                                    cResult = new GameResult(fromB.authorizedIdent, fromB.authorizedIdent, toB.authorizedIdent, java.util.Calendar.getInstance().getTimeInMillis());
                                } else if (tempstrarray[3].equals("lose")) {
                                    cResult = new GameResult(fromB.authorizedIdent, toB.authorizedIdent, fromB.authorizedIdent, java.util.Calendar.getInstance().getTimeInMillis());
                                } else {
                                    sendMessage(senderNickname, "Error in usage: fourth argument should either be 'win' or 'lose'.");
                                }
                                if (cResult != null) {
                                    boolean errorsthrown = false;
                                    if (tempstrarray.length >= 5) {
                                        try {
                                            cResult.scoremax = Integer.valueOf(tempstrarray[4]);
                                        } catch (Exception e) {
                                            sendMessage(senderNickname, "Error in fifth argument");
                                            errorsthrown = true;
                                        }
                                    }
                                    if (tempstrarray.length >= 6) {
                                        try {
                                            cResult.scoreremain = Integer.valueOf(tempstrarray[5]);
                                        } catch (Exception e) {
                                            sendMessage(senderNickname, "Error in sixth argument");
                                            errorsthrown = true;
                                        }
                                    }
                                    if (!errorsthrown) {
                                        sendMessage(toB.nickname, fromB.authorizedIdent + " claims that he/she has " + (win ? "won" : "lost") + " a " + (cResult.scoremax > -1 ? cResult.scoremax + "pt " : "") + "match against you" + (cResult.scoreremain > -1 ? " with " + cResult.scoreremain + " points remaining on the winner's side" : "") + "! To confirm this claim, use !result confirm " + cResult.timestamp);
                                        sendMessage(toB.nickname, "To reject this claim, use !result reject " + cResult.timestamp);
                                        resultList.add(cResult);
                                    }
                                }
                            } else {
                                if (toB != fromB) {
                                    sendMessage(senderNickname, "Opponent '" + tempstrarray[2] + "' is currently not logged in!");
                                } else {
                                    sendMessage(senderNickname, "You can't play games against yourself!");
                                }
                            }
                        } else {
                            sendMessage(senderNickname, "Too few arguments. Usage: !result claim <opponent> <win/lose> [max points] [points left]. max points and points left are optional.");
                        }
                    } else if (tempstrarray[1].equals("confirm")) {
                        try {
                            GameResult tempr = null;
                            for (GameResult r : resultList) {
                                if (r.timestamp == Long.valueOf(tempstrarray[2])) {
                                    tempr = r;
                                }
                            }
                            if (tempr != null) {
                                if (!tempr.claimer.equals(fromB.authorizedIdent)) {
                                    if (tempr.loser.equals(fromB.authorizedIdent) || tempr.winner.equals(fromB.authorizedIdent)) {
                                        confirmGame(tempr);
                                    } else {
                                        sendMessage(senderNickname, "You can't confirm claims not assigned to you!");
                                    }
                                } else {
                                    sendMessage(senderNickname, "You can't confirm your own claims!");
                                }
                            } else {
                                sendMessage(senderNickname, "ResultID to confirm not found!");
                            }
                        } catch (Exception e) {
                            sendMessage(senderNickname, "Some exception was thrown!");
                        }
                    } else if (tempstrarray[1].equals("reject")) {
                        try {
                            GameResult tempr = null;
                            for (GameResult r : resultList) {
                                if (r.timestamp == Long.valueOf(tempstrarray[2])) {
                                    tempr = r;
                                }
                            }
                            if (tempr != null) {
                                if (!tempr.claimer.equals(fromB.authorizedIdent)) {
                                    if (tempr.loser.equals(fromB.authorizedIdent) || tempr.winner.equals(fromB.authorizedIdent)) {
                                        sendMessageToAllChannels(fromB.authorizedIdent + " rejected a result claim made by " + tempr.claimer + ".");
                                        resultList.remove(tempr);
                                    } else {
                                        sendMessage(senderNickname, "You can't reject claims not assigned to you!");
                                    }
                                } else {
                                    sendMessage(senderNickname, "You can't reject your own claims!");
                                }
                            } else {
                                sendMessage(senderNickname, "ResultID to reject not found!");
                            }
                        } catch (Exception e) {
                            sendMessage(senderNickname, "Some exception was thrown!");
                        }
                    } else {
                        sendMessage(senderNickname, "Unknown sub-command of !result");
                    }
                } else {
                    sendMessage(senderNickname, "You're not logged in; please log in before trying to use !result.");
                }
            } else {
                sendMessage(senderNickname, "Too few parameters. Available sub-commands: claim, confirm, reject.");
            }
        }

        if (message.startsWith("!help")) {
            sendMessage(senderNickname, "Available commands: !username, !logout, !register, !chpass, !ready, !result");
        }
    }

    /**
     * Someone changed their nickname.
     * @param oldNick
     * @param username
     * @param hostname
     * @param newNick
     */
    public void onNickChange(String oldNick, String username, String hostname, String newNick) {
        Botuser b = getBotuser(username, hostname);
        if (b != null) {
            b.nickname = newNick;
        }
    }

    /**
     * Soeone left
     * @param channel
     * @param sender
     * @param username
     * @param hostname
     */
    public void onPart(String channel, String sender, String username, String hostname) {
        Botuser b = getBotuser(username, hostname);
        if (b != null) {
            botusers.remove(b);
        }
    }

    /**
     * Someone left
     * @param sourceNick
     * @param sourceusername
     * @param sourceHostname
     * @param reason
     */
    public void onQuit(String sourceNick, String sourceusername, String sourceHostname, String reason) {
        Botuser b = getBotuser(sourceusername, sourceHostname);
        if (b != null) {
            botusers.remove(b);
        }
    }

    /**
     * Send a message to a target (channel or username)
     * @param target
     * @param message
     */
    private void sendMessage(String target, String message) {
        irc.sendMessage(target, message);
    }

    /**
     * Send a message to all channels the bot is part of.
     * @param string
     */
    private void sendMessageToAllChannels(String string) {
        irc.sendMessageToAllChannels(string);
    }
}