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

package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.erikduisters.gpx.util.TypeUtil;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Waypoint implements ExtensionsContainer, LinksContainer {
    private double latitude;
    private double longitude;
    private float elevation;
    private Date time;
    private float magneticVariation;
    private float geoidHeight;
    @Nullable private String name;
    @Nullable private String comment;
    @Nullable private String description;
    @Nullable private String source;
    @NonNull private List<Link> links;
    @Nullable private String symbol;
    @Nullable private String type;
    @Nullable private String fix;
    private int numSatellites;
    private float hdop;     // Horizontal dilution of precision
    private float vdop;     // Vertial dilution of precision
    private float pdop;     // Position dilution of precision
    private float ageOfDgpsData;
    private int dgpsId;     // 0 <= value <= 1023
    @NonNull private List<Gpx.Extension> extensions;

    public Waypoint() {
        elevation = Float.NaN;
        magneticVariation = Float.NaN;
        geoidHeight = Float.NaN;
        links = new ArrayList<>();
        numSatellites = -1;
        hdop = Float.NaN;
        vdop = Float.NaN;
        pdop = Float.NaN;
        ageOfDgpsData = Float.NaN;
        dgpsId = -1;
        extensions = new ArrayList<>();
    }

    @Override
    public List<Link> getLinks() {
        return links;
    }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = TypeUtil.assertValidLatitude(latitude); }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = TypeUtil.assertValidLongitude(longitude); }

    public boolean hasElevation() { return !Float.isNaN(elevation); }

    public float getElevation() { return elevation; }

    public void setElevation(float elevation) { this.elevation = elevation; }

    public Date getTime() { return time; }

    public void setTime(Date time) { this.time = time; }

    public boolean hasMagneticVariation() { return !Float.isNaN(magneticVariation); }

    public float getMagneticVariation() { return magneticVariation; }

    public void setMagneticVariation(float magneticVariation) {
        this.magneticVariation = TypeUtil.assertValidDegrees(magneticVariation);
    }

    public boolean hasGeoidHeight() { return !Float.isNaN(geoidHeight); }

    public float getGeoidHeight() { return geoidHeight; }

    public void setGeoidHeight(float geoidHeight) { this.geoidHeight = geoidHeight; }

    @Nullable
    public String getName() { return name; }

    public void setName(@Nullable String name) { this.name = name; }

    @Nullable
    public String getComment() { return comment; }

    public void setComment(@Nullable String comment) { this.comment = comment; }

    @Nullable
    public String getDescription() { return description; }

    public void setDescription(@Nullable String description) { this.description = description; }

    @Nullable
    public String getSource() { return source; }

    public void setSource(@Nullable String source) { this.source = source; }

    @Nullable
    public String getSymbol() { return symbol; }

    public void setSymbol(@Nullable String symbol) { this.symbol = symbol; }

    @Nullable
    public String getType() { return type; }

    public void setType(@Nullable String type) { this.type = type; }

    @Nullable
    public String getFix() { return fix; }

    public void setFix(@Nullable String fix) { this.fix = TypeUtil.assertValidFixType(fix); }

    public int getNumSatellites() { return numSatellites; }

    public void setNumSatellites(int numSatellites) {
        this.numSatellites = TypeUtil.assertNonNegativeInteger(numSatellites);
    }

    public boolean hasHdop() { return !Float.isNaN(hdop); }

    public float getHdop() { return hdop; }

    public void setHdop(float hdop) { this.hdop = hdop; }

    public boolean hasVdop() { return !Float.isNaN(vdop); }

    public float getVdop() { return vdop; }

    public void setVdop(float vdop) { this.vdop = vdop; }

    public boolean hasPdop() { return !Float.isNaN(pdop); }

    public float getPdop() { return pdop; }

    public void setPdop(float pdop) { this.pdop = pdop; }

    public boolean hasAgeOfDgpsData() { return !Float.isNaN(ageOfDgpsData); }

    public float getAgeOfDgpsData() { return ageOfDgpsData; }

    public void setAgeOfDgpsData(float ageOfDgpsData) { this.ageOfDgpsData = ageOfDgpsData; }

    public boolean hasDgpsId() { return dgpsId >= 0; }

    public int getDgpsId() { return dgpsId; }

    public void setDgpsId(int dgpsId) { this.dgpsId = TypeUtil.assertValidDgpsId(dgpsId); }

    @Override
    public List<Gpx.Extension> getExtensions() {
        return extensions;
    }
}
