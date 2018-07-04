package nl.erikduisters.gpx;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;

import java.io.File;

import nl.erikduisters.gpx.model.GpsiesMetaDataExtensions;
import nl.erikduisters.gpx.model.GpsiesWptExtensions;
import nl.erikduisters.gpx.model.Gpx;

import static java.lang.Float.parseFloat;

/**
 * Created by Erik Duisters on 30-06-2018.
 */
public class GpsiesGPXReader extends GPXReader {
    private static final String GPSIES_V1_0_NS = "[https://www.gpsies.com/GPX/1/0]";

    public GpsiesGPXReader(File gpxFile) {
        super(gpxFile);
    }

    @Override
    void addGpxExtensionRules(String extensionPath) {
    }

    @Override
    void addMetaDataExtensionsRules(String extensionsPath) {
        final GpsiesMetaDataExtensions[] gpsiesMetaDataExtensions = new GpsiesMetaDataExtensions[1];

        rules.add(new DefaultRule<Gpx>(IRule.Type.TAG, extensionsPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    gpsiesMetaDataExtensions[0] = new GpsiesMetaDataExtensions();
                    extensionsContainerDeque.peek().getExtensions().add(gpsiesMetaDataExtensions[0]);
                }
            }
        });

        String gpsiesPath = addNameSpace("/property", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setProperty(text);
            }
        });

        gpsiesPath = addNameSpace("/trackLengthMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setTrackLengthMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/totalAscentMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setTotalAscentMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/totalDescentMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setTotalDescentMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/minHeightMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setMinHeightMeter(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/maxHeightMeter", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesMetaDataExtensions[0].setMaxHeightMeter(parseFloat(text));
            }
        });
    }

    @Override
    void addWptExtensionsRules(String extensionsPath) {
        final GpsiesWptExtensions[] gpsiesWptExtensionsContainer = new GpsiesWptExtensions[1];

        rules.add(new DefaultRule<Gpx>(IRule.Type.TAG, extensionsPath) {
            @Override
            public void handleTag(XMLParser<Gpx> parser, boolean isStartTag, Gpx gpx) {
                if (isStartTag) {
                    gpsiesWptExtensionsContainer[0] = new GpsiesWptExtensions();
                    extensionsContainerDeque.peek().getExtensions().add(gpsiesWptExtensionsContainer[0]);
                }
            }
        });

        String gpsiesPath = addNameSpace("/course", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesWptExtensionsContainer[0].setCourse(parseFloat(text));
            }
        });

        gpsiesPath = addNameSpace("/meterPerSecond", GPSIES_V1_0_NS);
        rules.add(new DefaultRule<Gpx>(IRule.Type.CHARACTER, extensionsPath + gpsiesPath) {
            @Override
            public void handleParsedCharacters(XMLParser<Gpx> parser, String text, Gpx gpx) {
                gpsiesWptExtensionsContainer[0].setSpeed(parseFloat(text));
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
