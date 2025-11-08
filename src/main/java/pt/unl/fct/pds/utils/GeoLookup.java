package pt.unl.fct.pds.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Helper class to geolocate nodes based on IP address.
 * It can use a portable local database for fast lookups.
 * It may be wise to implement a way to make request to a REST API if the local database does not return a valid answer.
 */
public class GeoLookup {
    private final DatabaseReader reader;

    private static final String DEFAULT_DB = "GeoLite2-Country.mmdb";

    public GeoLookup(String dbPath) {
        File database = new File("geo/" + dbPath);
        try {
            this.reader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*
    public GeoLookup() {
        try (InputStream dbStream = getClass().getClassLoader()
                .getResourceAsStream("geo/GeoLite2-Country.mmdb")) {
            this.reader = new DatabaseReader.Builder(dbStream).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    public GeoLookup() {
        this(DEFAULT_DB);
    }

    public String locate(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            CityResponse response = reader.city(address);

            return String.format("%s, %s (%s)",
                    response.getCity().getName(),
                    response.getCountry().getName(),
                    response.getLocation().getLatitude() + "," + response.getLocation().getLongitude());
        } catch (IOException | GeoIp2Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String locateCountry(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            CountryResponse response = reader.country(address);

            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            throw new RuntimeException(e);
        }
    }
}
