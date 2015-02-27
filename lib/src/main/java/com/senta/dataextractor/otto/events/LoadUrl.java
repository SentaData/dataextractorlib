package com.senta.dataextractor.otto.events;

public class LoadUrl {
    private final String url;

    public LoadUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
