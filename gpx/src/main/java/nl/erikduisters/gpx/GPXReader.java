package nl.erikduisters.gpx;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

import nl.erikduisters.gpx.model.Bounds;
import nl.erikduisters.gpx.model.Copyright;
import nl.erikduisters.gpx.model.Email;
import nl.erikduisters.gpx.model.ExtensionsContainer;
import nl.erikduisters.gpx.model.Gpx;
import nl.erikduisters.gpx.model.Link;
import nl.erikduisters.gpx.model.LinksContainer;
import nl.erikduisters.gpx.model.Metadata;
import nl.erikduisters.gpx.model.Person;
import nl.erikduisters.gpx.model.Route;
import nl.erikduisters.gpx.model.RouteOrTrack;
import nl.erikduisters.gpx.model.Track;
import nl.erikduisters.gpx.model.TrackSegment;
import nl.erikduisters.gpx.model.Waypoint;
import nl.erikduisters.gpx.model.WaypointsContainer;
import nl.erikduisters.gpx.util.DateUtil;

/**
 * Create by Erik Duisters on 28-06-2018.
 */

public abstract class GPXReader {
    private static final String GPX_V1_1_NS = "[http://www.topografix.com/GPX/1/1]";

    final private Pattern slashPattern;
    final private StringBuilder stringBuilder;
    final List<IRule<Gpx>> rules;

    protected Deque<LinksContainer> linksContainerDeque;
    protected Deque<ExtensionsContainer> extensionsContainerDeque;
    protected Deque<WaypointsContainer> waypointsContainerDeque;

    public GPXReader() {
        slashPattern = Pattern.compile("/");
        stringBuilder = new StringBuilder(1024);
        rules = new ArrayList<>();

        linksContainerDeque = new ArrayDeque<>();
        extensionsContainerDeque = new ArrayDeque<>();
        waypointsContainerDeque = new ArrayDeque<>();
    }

    abstract void addGpxExtensionRules(String extensionPath);
    abstract void addMetaDataExtensionsRules(String extensionsPath);
    abstract void addWptExtensionsRules(String extensionsPath);
    abstract void addRteExtensionsRules(String extensionsPath);
    abstract void addTrkExtensionsRules(String extensionsPath);
    abstract void addTrksegExtensionsRules(String extensionsPath);

    public Gpx doImport(File inFile) throws ImportException {
        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            return doImport(inputStream);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public Gpx doImport(InputStream inputStream) throws ImportException {
        //System.setProperty("sjxp.namespaces", "false");
        Gpx gpx = new Gpx();

        addGPXRules(gpx);

        @SuppressWarnings("unchecked")
        XMLParser<Gpx> xmlParser = new XMLParser<>(rules.toArray(new IRule[rules.size()]));

        try {
            xmlParser.parse(inputStream, gpx);
        } /*catch (OutOfMemoryError e) {
            return null;
        }*/ catch (IllegalArgumentException e) {
            throw new ImportException(e);
        } catch (XMLParserException e) {
            throw new ImportException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // To bad.
                }
            }
        }

        return gpx;
    }

    String addNameSpace(String locationPath, String nameSpace) {
        String[] split = slashPattern.split(locationPath);
        stringBuilder.setLength(0);

        int start = split[0].isEmpty() ? 1 : 0;
        int numSplits = split.length;

        for (int i=start; i < numSplits; i++) {
            stringBuilder.append("/");
            stringBuilder.append(nameSpace);
            stringBuilder.append(split[i]);
        }

        return stringBuilder.toString();
    }

    private void addGPXRules(Gpx gpx) {
        String currentLocationPath = addNameSpace("/gpx", GPX_V1_1_NS);

        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    extensionsContainerDeque.push(gpx);
                    waypointsContainerDeque.push(gpx);
                } else {
                    extensionsContainerDeque.pop();
                    waypointsContainerDeque.pop();
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "version", "creator") {
            @Override
            public void handleParsedAttribute(XMLParser parser, int index, String value, Gpx gpx) {
                switch (index) {
                    case 0:
                        gpx.setVersion(value);
                        break;
                    case 1:
                        gpx.setCreator(value);
                        break;
                }
            }
        });

        addMetaDataRules();
        addWptRules("/gpx/wpt");
        addRteRules();
        addTrkRules();
        addGpxExtensionRules(addNameSpace("/gpx/extensions", GPX_V1_1_NS));
    }

    private void addMetaDataRules() {
        final Metadata metadata = new Metadata();
        String currentLocationPath;

        currentLocationPath = addNameSpace("/gpx/metadata", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    gpx.setMetadata(metadata);
                    linksContainerDeque.push(metadata);
                    extensionsContainerDeque.push(metadata);
                } else {
                    linksContainerDeque.pop();
                    extensionsContainerDeque.pop();
                }
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/name", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                metadata.setName(text);
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/desc", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                metadata.setDescription(text);
            }
        });

        addMetadataAuthorRules(metadata);
        addMetadataCopyrightRules(metadata);
        addLinkRules("/gpx/metadata");

        currentLocationPath = addNameSpace("/gpx/metadata/time", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                metadata.setTime(DateUtil.parseXmlDate(text));
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/keywords", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                metadata.setKeywords(text);
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/bounds", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    metadata.setBounds(new Bounds());
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "minlat", "minlon", "maxlat", "maxlon") {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void handleParsedAttribute(XMLParser<Gpx> parser, int index, String value, Gpx gpx) {
                Double doubleValue = Double.valueOf(value);

                switch (index) {
                    case 0:
                        metadata.getBounds().setMinLatitude(doubleValue);
                        break;
                    case 1:
                        metadata.getBounds().setMinLongitude(doubleValue);
                        break;
                    case 2:
                        metadata.getBounds().setMaxLatitude(doubleValue);
                        break;
                    case 3:
                        metadata.getBounds().setMaxLongitude(doubleValue);
                        break;
                }
            }
        });

         currentLocationPath = addNameSpace("/gpx/metadata/extensions", GPX_V1_1_NS);
         addMetaDataExtensionsRules(currentLocationPath);
    }

    private void addMetadataAuthorRules(final Metadata metadata) {
        final Person author = new Person();
        String currentLocationPath;

        currentLocationPath = addNameSpace("/gpx/metadata/author", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    metadata.setAuthor(author);
                    linksContainerDeque.push(author);
                } else {
                    linksContainerDeque.pop();
                }
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/author/name", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                author.setName(text);
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/author/email", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    author.setEmail(new Email());
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "id", "domain") {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void handleParsedAttribute(XMLParser parser, int index, String value, Gpx gpx) {
                switch (index) {
                    case 0:
                        author.getEmail().setId(value);
                        break;
                    case 1:
                        author.getEmail().setDomain(value);
                        break;
                }
            }
        });

        addLinkRules("/gpx/metadata/author");
    }

    private void addMetadataCopyrightRules(final Metadata metadata) {
        String currentLocationPath;

        currentLocationPath = addNameSpace("/gpx/metadata/copyright", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    metadata.setCopyright(new Copyright());
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "author") {
            @Override
            public void handleParsedAttribute(XMLParser<Gpx> parser, int index, String value, Gpx gpx) {
                //noinspection ConstantConditions
                metadata.getCopyright().setAuthor(value);
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/copyright/year", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath){
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                //TODO: Handle xsd:gYear
                //noinspection ConstantConditions
                metadata.getCopyright().setYear(text);
            }
        });

        currentLocationPath = addNameSpace("/gpx/metadata/copyright/license", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                //TODO: Handle xsd:anyURI
                //noinspection ConstantConditions
                metadata.getCopyright().setLicense(text);
            }
        });
    }

    private void addLinkRules(String basePath) {
        final Link[] linkContainer = new Link[1];
        String currentLocationPath;

        currentLocationPath = addNameSpace(basePath + "/link", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    linkContainer[0] = new Link();
                    linksContainerDeque.peek().getLinks().add(linkContainer[0]);
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "href") {
            @Override
            public void handleParsedAttribute(XMLParser parser, int index, String value, Gpx gpx) {
                linkContainer[0].setHref(value);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/link/text", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                linkContainer[0].setText(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/link/type", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser parser, String text, Gpx gpx) {
                linkContainer[0].setType(text);
            }
        });
    }

    private void addWptRules(String basePath) {
        final Waypoint[] waypointContainer = new Waypoint[1];
        String currentLocationPath;

        currentLocationPath = addNameSpace(basePath, GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    waypointContainer[0] = new Waypoint();

                    waypointsContainerDeque.peek().getWaypoints().add(waypointContainer[0]);
                    linksContainerDeque.push(waypointContainer[0]);
                    extensionsContainerDeque.push(waypointContainer[0]);
                } else {
                    linksContainerDeque.pop();
                    extensionsContainerDeque.pop();
                }
            }
        });

        rules.add(new DefaultRule<Gpx>(Type.ATTRIBUTE, currentLocationPath, "lat", "lon") {
            @Override
            public void handleParsedAttribute(XMLParser<Gpx> parser, int index, String value, Gpx gpx) {
                switch (index) {
                    case 0:
                        waypointContainer[0].setLatitude(Double.parseDouble(value));
                        break;
                    case 1:
                        waypointContainer[0].setLongitude(Double.parseDouble(value));
                        break;
                }
            }
        });

        currentLocationPath = addNameSpace(basePath + "/ele", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setElevation(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/time", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setTime(DateUtil.parseXmlDate(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/magvar", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setMagneticVariation(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/geoidheight", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setGeoidHeight(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/name", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setName(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/cmt", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setComment(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/desc", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setDescription(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/src", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setSource(text);
            }
        });

        addLinkRules(basePath);

        currentLocationPath = addNameSpace(basePath + "/sym", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setSymbol(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/type", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setType(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/fix", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setFix(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/sat", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setNumSatellites(Integer.parseInt(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/hdop", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setHdop(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/vdop", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setVdop(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/pdop", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setPdop(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/ageofdgpsdata", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setAgeOfDgpsData(Float.parseFloat(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/dgpsid", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                waypointContainer[0].setDgpsId(Integer.parseInt(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/extensions", GPX_V1_1_NS);
        addWptExtensionsRules(currentLocationPath);
    }

    private void addRteRules() {
        final Route[] routeContainer = new Route[1];
        String currentLocationPath;

        currentLocationPath = addNameSpace("/gpx/rte", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    routeContainer[0] = new Route();
                    gpx.getRoutes().add(routeContainer[0]);

                    extensionsContainerDeque.push(routeContainer[0]);
                    linksContainerDeque.push(routeContainer[0]);
                    waypointsContainerDeque.push(routeContainer[0]);
                } else {
                    extensionsContainerDeque.pop();
                    linksContainerDeque.pop();
                    waypointsContainerDeque.pop();
                }
            }
        });

        addCommonRouteOrTrackRules("/gpx/rte", routeContainer);

        currentLocationPath = addNameSpace("/gpx/rte/extensions", GPX_V1_1_NS);

        addRteExtensionsRules(currentLocationPath);

        addWptRules("/gpx/rte/rtept");
    }

    private void addCommonRouteOrTrackRules(String basePath, final RouteOrTrack[] routeOrTrackContainer) {
        String currentLocationPath;

        currentLocationPath = addNameSpace(basePath + "/name", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setName(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/cmt", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setComment(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/desc", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setDescription(text);
            }
        });

        currentLocationPath = addNameSpace(basePath + "/src", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setSource(text);
            }
        });

        addLinkRules(basePath);

        currentLocationPath = addNameSpace(basePath + "/number", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setNumber(Integer.parseInt(text));
            }
        });

        currentLocationPath = addNameSpace(basePath + "/type", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.CHARACTER, currentLocationPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                routeOrTrackContainer[0].setType(text);
            }
        });
    }

    private void addTrkRules() {
        final Track[] trackContainer = new Track[1];
        String currentLocationPath;

        currentLocationPath = addNameSpace("/gpx/trk", GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    trackContainer[0] = new Track();
                    gpx.getTracks().add(trackContainer[0]);

                    extensionsContainerDeque.push(trackContainer[0]);
                    linksContainerDeque.push(trackContainer[0]);
                } else {
                    extensionsContainerDeque.pop();
                    linksContainerDeque.pop();
                }
             }
        });

        addCommonRouteOrTrackRules("/gpx/trk", trackContainer);

        currentLocationPath = addNameSpace("/gpx/trk/extensions", GPX_V1_1_NS);
        addTrkExtensionsRules(currentLocationPath);

        addTrksegRules("/gpx/trk/trkseg", trackContainer);
    }

    private void addTrksegRules(String basePath, final Track[] trackContainer) {
        String currentLocationPath;

        currentLocationPath = addNameSpace(basePath, GPX_V1_1_NS);
        rules.add(new DefaultRule<Gpx>(Type.TAG, currentLocationPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    TrackSegment trackSegment = new TrackSegment();
                    trackContainer[0].getTrackSegments().add(trackSegment);

                    extensionsContainerDeque.push(trackSegment);
                    waypointsContainerDeque.push(trackSegment);
                } else {
                    extensionsContainerDeque.pop();
                    waypointsContainerDeque.pop();
                }
            }
        });

        addWptRules(basePath + "/trkpt");

        currentLocationPath = addNameSpace(basePath + "/extensions", GPX_V1_1_NS);
        addTrksegExtensionsRules(currentLocationPath);
    }

    public class ImportException extends RuntimeException {
        ImportException(Exception cause) {
            super(cause);
        }
    }
}
