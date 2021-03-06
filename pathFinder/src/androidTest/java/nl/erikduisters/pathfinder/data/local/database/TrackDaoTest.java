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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Erik Duisters on 16-06-2018.
 */
@RunWith(AndroidJUnit4.class)
@Ignore("Does not work with minifyEnabled=true")
public class TrackDaoTest {
    private PathfinderDatabase database;

    @Before
    public void setUp() throws Exception {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), PathfinderDatabase.class).build();
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test
    public void insertTrack_trackIsInsertedCorrectly() {
        Track track = TestUtil.getTestTrack();

        long id = database.trackDao().insert(track);

        Track readTrack = database.trackDao().getTrackById(id);

        assertNotNull(readTrack);
        assertEquals(id, readTrack.id);
        assertEquals(track.name, readTrack.name);
        assertEquals(track.description, readTrack.description);
        assertEquals(track.author, readTrack.author);
        assertEquals(track.gpsiesFileId, readTrack.gpsiesFileId);
        assertEquals(track.dateCreated.getTime(), readTrack.dateCreated.getTime());
        assertEquals(track.type, readTrack.type);
        assertEquals(track.length, readTrack.length, 0);
        assertEquals(track.totalAscent, readTrack.totalAscent, 0);
        assertEquals(track.totalDescent, readTrack.totalDescent, 0);
        assertEquals(track.minHeight, readTrack.minHeight, 0);
        assertEquals(track.maxHeight, readTrack.maxHeight, 0);
    }

    @Test
    public void deleteTrack_trackIsDeleted() {
        Track track = TestUtil.getTestTrack();

        track.id = database.trackDao().insert(track);

        database.trackDao().delete(track);

        Track readTrack = database.trackDao().getTrackById(track.id);

        assertNull(readTrack);
    }
}