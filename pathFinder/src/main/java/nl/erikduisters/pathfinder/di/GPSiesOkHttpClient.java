package nl.erikduisters.pathfinder.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by Erik Duisters on 17-08-2018.
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface GPSiesOkHttpClient {
}
