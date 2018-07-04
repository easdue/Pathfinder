package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Email {
    @NonNull private String id;
    @NonNull private String domain;

    public Email() {
        id = "";
        domain = "";
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getDomain() {
        return domain;
    }

    public void setDomain(@NonNull String domain) {
        this.domain = domain;
    }
}
