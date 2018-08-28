/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Erik Duisters on 17-06-2018.
 */
@RunWith(AndroidJUnit4.class)
@Ignore("Does not work with minifyEnabled=true")
public class WaypointDaoTest {
    private PathfinderDatabase database;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), PathfinderDatabase.class).build();
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test
    public void insertWaypointWithInvalidTrackId_throwsException() {
        thrown.expect(SQLiteConstraintException.class);

        Waypoint waypoint = TestUtil.getTestWaypoint();
        waypoint.trackId = 2L;

        database.waypointDao().insert(waypoint);
    }

    private void assertWaypoints(Waypoint expected, Waypoint real) {
        assertEquals(expected.id, real.id);
        assertEquals(expected.name, real.name);
        assertEquals(expected.description, real.description);
        assertEquals(expected.elevation, real.elevation, 0);
        assertEquals(expected.latitude, real.latitude, 0);
        assertEquals(expected.longitude, real.longitude, 0);
        assertEquals(expected.trackId, real.trackId);
        assertEquals(expected.link, real.link);
        assertEquals(expected.type.code(), real.type.code());
    }


    @Test
    public void insertWaypointWithNullTrackId_waypointIsInsertedCorrectly() {
        Waypoint waypoint = TestUtil.getTestWaypoint();
        waypoint.trackId = null;

        waypoint.id = database.waypointDao().insert(waypoint);

        Waypoint readWaypoint = database.waypointDao().getWaypointById(waypoint.id);

        assertNotNull(readWaypoint);
        assertWaypoints(waypoint, readWaypoint);

    }

    private void insertTrackAndWaypoint(Track track, Waypoint waypoint) {
        track.id = database.trackDao().insert(track);
        waypoint.trackId = track.id;
        waypoint.id = database.waypointDao().insert(waypoint);
    }

    @Test
    public void insertWaypointWithValidTrackId_waypointIsInsertedCorrectly() {
        Track track = TestUtil.getTestTrack();
        Waypoint waypoint = TestUtil.getTestWaypoint();

        insertTrackAndWaypoint(track, waypoint);

        Waypoint readWaypoint = database.waypointDao().getWaypointById(waypoint.id);

        assertNotNull(readWaypoint);
        assertWaypoints(waypoint, readWaypoint);
    }

    @Test
    public void deleteWaypoint_waypointIsDeleted() {
        Track track = TestUtil.getTestTrack();
        Waypoint waypoint = TestUtil.getTestWaypoint();

        insertTrackAndWaypoint(track, waypoint);

        Waypoint readWaypoint = database.waypointDao().getWaypointById(waypoint.id);
        assertNotNull(readWaypoint);

        database.waypointDao().delete(waypoint);

        readWaypoint = database.waypointDao().getWaypointById(waypoint.id);

        assertNull(readWaypoint);
    }

    @Test
    public void deleteTrack_waypointIsAlsoDeleted() {
        Track track = TestUtil.getTestTrack();
        Waypoint waypoint = TestUtil.getTestWaypoint();

        insertTrackAndWaypoint(track, waypoint);

        database.trackDao().delete(track);

        Waypoint readWaypoint = database.waypointDao().getWaypointById(waypoint.id);

        assertNull(readWaypoint);
    }
}