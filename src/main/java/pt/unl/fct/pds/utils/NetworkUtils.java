package pt.unl.fct.pds.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class NetworkUtils {

    /**
     * Downloads a file from the given URL to the specified destination path.
     *
     * @param fileURL  the URL of the file to download
     * @param target   the local path where the file should be saved
     * @throws IOException if an I/O error occurs
     */
    public static void download(String fileURL, Path target, boolean createDirectories, Consumer<Long> progressCallback) throws IOException {
        if (createDirectories && target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(30_000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + responseCode
                    + " for " + fileURL);
        }

        Path temp = Files.createTempFile(target.getParent(), "download", ".tmp");
        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                total += read;
                if (progressCallback != null) {
                    progressCallback.accept(total);
                }

            }
        } finally {
            connection.disconnect();
        }
        Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    // TODO: Maybe add a method for extracting 16 subnet and then just use equals upstream. Reduces number of splits if one of the arguments in the function below never changes
    /**
     *
     * @param ipAddress - IPv4 address in string format
     * @param otherIpAddress - IPv4 address in string format
     * @return true if both are in the same /16 subnet, false otherwise
     */
    public static boolean same16Subnet(String ipAddress, String otherIpAddress) {
        String[] splitIpGuard = ipAddress.split("\\.");
        String[] splitIpExit = otherIpAddress.split("\\.");
        assert splitIpGuard.length == splitIpExit.length && splitIpGuard.length == 4;
        return splitIpExit[0].equals(splitIpGuard[0]) && splitIpExit[1].equals(splitIpGuard[1]);
    }
}
