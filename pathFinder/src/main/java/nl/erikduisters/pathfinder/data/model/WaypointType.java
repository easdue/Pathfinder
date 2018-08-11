package nl.erikduisters.pathfinder.data.model;

import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
public enum WaypointType {
    GENERIC(0, R.string.waypoint_type_generic),
    SUMMIT(1, R.string.waypoint_type_summit),
    VALLEY(2, R.string.waypoint_type_valley),
    MOUNTAIN_PASS(3, R.string.waypoint_type_mountain_pass),
    WATER(4, R.string.waypoint_type_water),
    FOOD(5, R.string.waypoint_type_food),
    DANGER(6, R.string.waypoint_type_danger),
    FIRST_AID(7, R.string.waypoint_type_first_aid),
    SPRINT(8, R.string.waypoint_type_sprint),
    STRAIGHT(9, R.string.waypoint_type_straight),
    LEFT(10, R.string.waypoint_type_left),
    LEFT_SLIGHT(11, R.string.waypoint_type_left_slight),
    LEFT_SHARP(12, R.string.waypoint_type_left_sharp),
    RIGHT(13, R.string.waypoint_type_right),
    RIGHT_SLIGHT(14, R.string.waypoint_type_right_slight),
    RIGHT_SHARP(15, R.string.waypoint_type_right_sharp),
    U_TURN(16, R.string.waypoint_type_u_turn),
    RESIDENCE(17, R.string.waypoint_type_residence),
    LODGING(18, R.string.waypoint_type_lodging),
    PARKING(19, R.string.waypoint_type_parking),
    STATION(20, R.string.waypoint_type_station),
    ATTENTION(21, R.string.waypoint_type_attention),
    INFORMATION(22, R.string.waypoint_type_information),
    VIEWPOINT(23, R.string.waypoint_type_viewpoint),
    SIGHTSEEING(24, R.string.waypoint_type_sightseeing),
    BARBECUE(25, R.string.waypoint_type_barbecue),
    CAMPING_GROUND(26, R.string.waypoint_type_camping_ground),
    GEOCACHE(27, R.string.waypoint_type_geocache),
    PLAYGROUND_ZOO(28, R.string.waypoint_type_playground_zoo),
    SWIMMING_OPPORTUNITIES(29, R.string.waypoint_type_swimming_opportunities),
    SERVICE_STATION(30, R.string.waypoint_type_station);

    private final int code;
    @StringRes
    private final int nameResId;
    private final static WaypointType[] values;

    static {
        values = WaypointType.values();
    }

    WaypointType(int code, @StringRes int nameResId) {
        this.code = code;
        this.nameResId = nameResId;
    }

    public int code() { return code; }

    public static WaypointType fromInt(int code) {
        for (WaypointType waypointType : values) {
            if (waypointType.code == code) {
                return waypointType;
            }
        }

        throw new RuntimeException("There is no WaypointType with code: " + code);
    }
}
