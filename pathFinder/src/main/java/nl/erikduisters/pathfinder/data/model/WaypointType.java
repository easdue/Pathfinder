package nl.erikduisters.pathfinder.data.model;

import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 14-06-2018.
 */

//TODO: Find / Create svg images for all WaypointTypes
public enum WaypointType {
    GENERIC(0, "Generic", R.string.waypoint_type_generic),
    SUMMIT(1, "Summit", R.string.waypoint_type_summit),
    VALLEY(2, "Valley", R.string.waypoint_type_valley),
    MOUNTAIN_PASS(3, "Mountain pass", R.string.waypoint_type_mountain_pass),
    WATER(4, "Water", R.string.waypoint_type_water),
    FOOD(5, "Food", R.string.waypoint_type_food),
    DANGER(6, "Danger", R.string.waypoint_type_danger),
    FIRST_AID(7, "First Aid", R.string.waypoint_type_first_aid),
    SPRINT(8, "Sprint", R.string.waypoint_type_sprint),
    STRAIGHT(9, "Straight", R.string.waypoint_type_straight),
    LEFT(10, "Left", R.string.waypoint_type_left),
    LEFT_SLIGHT(11, "TSLL", R.string.waypoint_type_left_slight),
    LEFT_SHARP(12, "TSHL", R.string.waypoint_type_left_sharp),
    RIGHT(13, "Right", R.string.waypoint_type_right),
    RIGHT_SLIGHT(14, "TSLR", R.string.waypoint_type_right_slight),
    RIGHT_SHARP(15, "TSHR", R.string.waypoint_type_right_sharp),
    U_TURN(16, "TU", R.string.waypoint_type_u_turn),
    RESIDENCE(17, "Residence", R.string.waypoint_type_residence),
    LODGING(18,"Hostel", R.string.waypoint_type_lodging),
    PARKING(19, "Parking", R.string.waypoint_type_parking),
    STATION(20, "Station", R.string.waypoint_type_station),
    ATTENTION(21, "Attention", R.string.waypoint_type_attention),
    INFORMATION(22, "Information", R.string.waypoint_type_information),
    VIEWPOINT(23, "Viewpoint", R.string.waypoint_type_viewpoint),
    SIGHTSEEING(24, "Sightseeing", R.string.waypoint_type_sightseeing),
    BARBECUE(25, "Barbecue", R.string.waypoint_type_barbecue),
    CAMPING_GROUND(26, "Camping ground", R.string.waypoint_type_camping_ground),
    GEOCACHE(27, "Geocache", R.string.waypoint_type_geocache),
    PLAYGROUND_ZOO(28, "Playground", R.string.waypoint_type_playground_zoo),
    SWIMMING_OPPORTUNITIES(29, "Bathing facilities", R.string.waypoint_type_swimming_opportunities),
    SERVICE_STATION(30, "Service station",  R.string.waypoint_type_service_station);

    private final int code;
    @StringRes
    private final int nameResId;
    private final String gpxType;
    private final static WaypointType[] values;

    static {
        values = WaypointType.values();
    }

    WaypointType(int code, String gpxType, @StringRes int nameResId) {
        this.code = code;
        this.gpxType = gpxType;
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

    public static WaypointType fromGpxType(String gpxType) {
        for (WaypointType waypointType : values) {
            if (waypointType.gpxType.equals(gpxType)) {
                return waypointType;
            }
        }

        throw new RuntimeException("There is no WaypointType with gpxType: " + gpxType);
    }
}
