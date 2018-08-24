package nl.erikduisters.gpx;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;

import nl.erikduisters.gpx.model.GpsiesMetaDataExtensions;
import nl.erikduisters.gpx.model.GpsiesWptExtensions;
import nl.erikduisters.gpx.model.Gpx;

import static java.lang.Float.parseFloat;

/**
 * Created by Erik Duisters on 30-06-2018.
 */
public class GpsiesGPXReader extends GPXReader {
    //NOTE: GPSies.com erroneously sets its namespace to https://www.gpsies.com/GPX/1/0" when downloading tracks over https. Maybe add all rules twice (eg for http and https)
    private static final String GPSIES_V1_0_NS = "[https://www.gpsies.com/GPX/1/0]";

    private GpsiesMetaDataExtensions gpsiesMetaDataExtensions;
    private GpsiesWptExtensions gpsiesWptExtensions;

    private GpsiesMetaDataExtensions getGpsiesMetaDataExtensions() {
        if (gpsiesMetaDataExtensions == null) {
            gpsiesMetaDataExtensions = new GpsiesMetaDataExtensions();
            extensionsContainerDeque.peek().getExtensions().add(gpsiesMetaDataExtensions);
        }

        return gpsiesMetaDataExtensions;
    }

    private GpsiesWptExtensions getGpsiesWptExtensions() {
        if (gpsiesWptExtensions == null) {
            gpsiesWptExtensions = new GpsiesWptExtensions();
            extensionsContainerDeque.peek().getExtensions().add(gpsiesWptExtensions);
        }

        return gpsiesWptExtensions;
    }

    @Override
    void addGpxExtensionRules(String extensionPath) {
    }

    @Override
    void addMetaDataExtensionsRules(String extensionsPath) {
        rules.add(new DefaultRule<Gpx>(IRule.Type.TAG, extensionsPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    gpsiesMetaDataExtensions = null;
                }
            }
        });

        String gpsiesPath = addNameSpace("/property", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setProperty(text);
            }
        });

        gpsiesPath = addNameSpace("/trackLengthMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setTrackLengthMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/totalAscentMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setTotalAscentMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/totalDescentMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setTotalDescentMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/minHeightMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setMinHeightMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/maxHeightMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesMetaDataExtensions().setMaxHeightMeter(parseFloat(text));
            }
        });
    }

    @Override
    void addWptExtensionsRules(String extensionsPath) {
        rules.add(new DefaultRule<Gpx>(IRule.Type.TAG, extensionsPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    gpsiesWptExtensions = null;
                }
            }
        });

        String gpsiesPath = addNameSpace("/course", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesWptExtensions().setCourse(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/meterPerSecond", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                getGpsiesWptExtensions().setSpeed(parseFloat(text));
            }
        });
    }

    @Override
    void addRteExtensionsRules(String extensionsPath) {
    }

    @Override
    void addTrkExtensionsRules(String extensionsPath) {
    }

    @Override
    void addTrksegExtensionsRules(String extensionsPath) {
    }
}
