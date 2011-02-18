/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */
package uqm.uqmatchbot.domain;

/**
 * A Bot User.
 * Used for internal bot logic purposes: to track people who are logged in, and
 * to track for how long they're ready to play a game.
 */
public class Botuser {
    public String nickname; //The user's IRC nickname
    public String username; //The user's IRC username (NOT NICKNAME; nicknames can be changed, usernames can only be changed by reconnecting).
    public String hostname; //The user's IRC hostname
    public boolean authorized;
    public String authorizedIdent; //The UQMatchbot accountname this user has been logged in with. TODO: make this an instantiation of a Login class
    public long readyuntil; //Specifies until how long this user is ready (timestamp)

    public Botuser(String nickname, String username, String hostname, String authorizedIdent) {
        this.nickname = nickname;
        this.username = username;
        this.hostname = hostname;
        authorized = true;
        this.authorizedIdent = authorizedIdent;
    }

    public void authorize(String ident) {
        authorized = true;
        authorizedIdent = ident;
    }

    public void ready(long duration) {
        readyuntil = java.util.Calendar.getInstance().getTimeInMillis() + duration;
    }

    public boolean isReady() {
        return (java.util.Calendar.getInstance().getTimeInMillis() < readyuntil);
    }



}
