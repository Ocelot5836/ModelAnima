package io.github.ocelot.modelanima.core.client.util;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.conn.EofSensorWatcher;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ocelot
 */
public interface FileCache
{
    String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    /**
     * Requests an online resource from the specified URL.
     *
     * @param url           The url to download the resource from
     * @param ignoreMissing Whether or not to print an error when an exception is thrown
     * @return A future for the location of the resource locally
     */
    CompletableFuture<Path> requestResource(String url, boolean ignoreMissing);

    /**
     * Opens a GET stream to the specified URL.
     *
     * @param url The url to open a stream to
     * @return The opened stream to the resource
     * @throws IOException If any error occurs while trying to fetch resources
     */
    static InputStream get(String url) throws IOException
    {
        HttpGet get = new HttpGet(url);
        CloseableHttpClient client = HttpClients.custom().setUserAgent(USER_AGENT).build();
        CloseableHttpResponse response = client.execute(get);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200)
        {
            client.close();
            response.close();
            throw new IOException("Failed to connect to '" + url + "'. " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
        }
        return new EofSensorInputStream(response.getEntity().getContent(), new EofSensorWatcher()
        {
            @Override
            public boolean eofDetected(InputStream wrapped)
            {
                return true;
            }

            @Override
            public boolean streamClosed(InputStream wrapped) throws IOException
            {
                response.close();
                return true;
            }

            @Override
            public boolean streamAbort(InputStream wrapped) throws IOException
            {
                response.close();
                return true;
            }
        });
    }
}
