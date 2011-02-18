/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */

package uqm.uqmatchbot.domain;


import java.io.Serializable;

/**
 * A result of a game.
 * These're stored and can perhaps be used someday for some awesome statistics stuff..
 */
public class GameResult implements Serializable {
    public String winner, loser, claimer;
    public long timestamp;
    public int scoreremain, scoremax;

    public GameResult(String claimer, String winner, String loser, long timestamp, int scoreremain, int scoremax) {
        this.claimer = claimer;
        this.winner = winner;
        this.loser = loser;
        this.timestamp = timestamp;
        this.scoreremain = scoreremain;
        this.scoremax = scoremax;
    }

    public GameResult(String claimer, String winner, String loser, long timestamp, int scoremax) {
        this(claimer, winner, loser, timestamp, -1, scoremax);
    }

    public GameResult(String claimer, String winner, String loser, long timestamp) {
        this(claimer, winner, loser, timestamp, -1, -1);
    }
}
