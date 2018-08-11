package nl.erikduisters.pathfinder.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by Erik Duisters on 01-06-2018.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityContext {
}
