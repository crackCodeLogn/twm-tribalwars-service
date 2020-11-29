package com.vv.personal.twm.tribalwars.automation.config;

/**
 * @author Vivek
 * @since 29/11/20
 */
public class Sso {

    private String user;
    private String cred;

    public Sso() {
    }

    public Sso(String user, String cred) {
        this.user = user;
        this.cred = cred;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Sso{");
        sb.append("user='").append(user).append('\'');
        sb.append(", cred='").append(cred).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getUser() {
        return user;
    }

    public Sso setUser(String user) {
        this.user = user;
        return this;
    }

    public String getCred() {
        return cred;
    }

    public Sso setCred(String cred) {
        this.cred = cred;
        return this;
    }
}
