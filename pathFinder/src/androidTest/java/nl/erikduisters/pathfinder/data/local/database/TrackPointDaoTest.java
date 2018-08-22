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
 * Created by Erik Duisters on 16-06-2018.
 */
@RunWith(AndroidJUnit4.class)
@Ignore("Does not work with minifyEnabled=true")
public class TrackPointDaoTest {
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
    public void insertTrackPointWithInvalidTrackId_throwsException() {
        thrown.expect(SQLiteConstraintException.class);

        TrackPoint trackPoint = TestUtil.getTestTrackPoint();
        trackPoint.trackId = 2;

        database.trackPointDao().insert(trackPoint);
    }

    private void insertTrackAndTrackpoint(Track track, TrackPoint trackPoint) {
        track.id = database.trackDao().insert(track);
        trackPoint.trackId = track.id;
        trackPoint.id = database.trackPointDao().insert(trackPoint);
    }

    @Test
    public void insertTrackPointWithValidTrackId_trackPointIsInsertedCorrectly() {
        Track track = TestUtil.getTestTrack();
        TrackPoint trackPoint = TestUtil.getTestTrackPoint();

        insertTrackAndTrackpoint(track, trackPoint);

        TrackPoint readTrackPoint = database.trackPointDao().getTrackPointByid(trackPoint.id);

        assertNotNull(readTrackPoint);

        assertEquals(trackPoint.id, readTrackPoint.id);
        assertEquals(trackPoint.elevation, readTrackPoint.elevation, 0);
        assertEquals(trackPoint.latitude, readTrackPoint.latitude, 0);
        assertEquals(trackPoint.longitude, readTrackPoint.longitude, 0);
        assertEquals(trackPoint.segment, readTrackPoint.segment);
        assertEquals(trackPoint.trackId, readTrackPoint.trackId);
        assertEquals(trackPoint.time.getTime(), readTrackPoint.time.getTime());
    }

    @Test
    public void deleteTrackPoint_trackPointIsDeleted() {
        Track track = TestUtil.getTestTrack();
        TrackPoint trackPoint = TestUtil.getTestTrackPoint();

        insertTrackAndTrackpoint(track, trackPoint);

        TrackPoint readTrackPoint = database.trackPointDao().getTrackPointByid(trackPoint.id);
        assertNotNull(readTrackPoint);

        database.trackPointDao().delete(trackPoint);

        readTrackPoint = database.trackPointDao().getTrackPointByid(trackPoint.id);

        assertNull(readTrackPoint);
    }

    @Test
    public void deleteTrack_trackPointIsAlsoDeleted() {
        Track track = TestUtil.getTestTrack();
        TrackPoint trackPoint = TestUtil.getTestTrackPoint();

        insertTrackAndTrackpoint(track, trackPoint);

        database.trackDao().delete(track);

        TrackPoint readTrackPoint = database.trackPointDao().getTrackPointByid(trackPoint.id);

        assertNull(readTrackPoint);
    }
}