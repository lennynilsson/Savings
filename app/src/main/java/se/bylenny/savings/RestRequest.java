package se.bylenny.savings;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A request to the REST API
 * @param <T> The response type
 */
public class RestRequest<T> implements Runnable {
    private static final String TAG = "ApiRequest";
    private static OkUrlFactory factory;
    private static ObjectMapper mapper;
    private static OkHttpClient client;
    private static boolean initialized = false;
    private static Cache cache = null;
    private String url;
    private ResponseListener<T> listener;
    private Class<T> type;

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
            client.setCache(cache);
            client.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
            client.setReadTimeout(3000, TimeUnit.MILLISECONDS);
            client.setWriteTimeout(3000, TimeUnit.MILLISECONDS);
        }
        return client;
    }

    /**
     * Setup disk cache
     * @param context The context
     */
    public static void setup(final Context context) {
        if (!initialized) {
            new GlideBuilder(context).setDiskCache(new DiskCache.Factory() {
                @Override
                public DiskCache build() {
                    long capacity = Environment.getExternalStorageDirectory().getUsableSpace();
                    int cacheSize = (int) Math.min(Integer.MAX_VALUE, capacity / 5);
                    return DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), cacheSize);
                }
            });
            initialized = true;
        }
    }

    private OkUrlFactory getConnectionFactory() {
        if (factory == null) {
            factory = new OkUrlFactory(getClient());
        }
        return factory;
    }

    private ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    public void call(String url, Class<T> type, AbstractExecutorService executor, ResponseListener<T> listener) {
        this.url = url;
        this.type = type;
        this.listener = listener;
        executor.execute(this);
    }

    @Override
    public void run() {
        InputStream stream = null;
        try {
            Log.d(TAG, "Fetching " + this.url);
            URL url = new URL(this.url);
            HttpURLConnection connection = getConnectionFactory().open(url);
            connection.setUseCaches(true);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                stream = connection.getInputStream();
                T translation = getMapper().readValue(stream, type);
                listener.onSuccess(translation);
            } else {
                Log.d(TAG, "Got error code " + connection.getResponseCode() + " from " + url);
                listener.onFailure(connection.getResponseMessage());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Error", e);
            listener.onFailure(e.getMessage());
        } catch (JsonParseException e) {
            Log.e(TAG, "Error in response", e);
            Scanner s = new Scanner(stream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            Log.d(TAG, response);
            listener.onFailure(e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
            listener.onFailure(e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }

    }

    /**
     * The REST API response listener
     * @param <T> The Response type
     */
    public interface ResponseListener<T> {

        /**
         * Receive a REST API response
         * @param response The response
         */
        public void onSuccess(T response);

        /**
         * Receive a REST API error
         * @param error The error
         */
        public void onFailure(String error);
    }
}
