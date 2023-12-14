package io.github.googlebardclient;

import java.time.Duration;

public final class GoogleBardClientProvider {

    private GoogleBardClientProvider() {
    }

    public static GoogleBardClient newInstance(String secure1psid, String secure1psidts) {
        return new GoogleBardClientImpl(secure1psid, secure1psidts);
    }

    public static GoogleBardClient newInstance(String secure1psid, String secure1psidts, Duration timeout) {
        return new GoogleBardClientImpl(secure1psid, secure1psidts, timeout);
    }

}
