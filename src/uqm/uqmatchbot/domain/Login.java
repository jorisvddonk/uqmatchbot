/*
 * This file is part of UQMatchbot
 * see LICENSE.TXT for more information about the UQMatchbot project's license.
 */

package uqm.uqmatchbot.domain;

import java.io.Serializable;

/**
 * A bot login account.
 * Just used for logging in, and stuff...
 * @author joris
 */
public class Login implements Serializable {
    public String ident, password;
    public Login (String ident, String password) {
        this.ident = ident; //User identity. As in the one a user decides to register with.
        this.password = password; //TODO: encrypt this
    }

    public boolean checkCredentials (String ident, String password) {
        if (this.ident.equals(ident) && this.password.equals(password)) {
            return true;
        } else {
            return false;
        }
    }
}
