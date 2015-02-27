package com.senta.dataextractor.otto.events;

public class ApiCallFinished {

    private final boolean successfully;
    private final String response;

    public ApiCallFinished(String response, boolean wasSuccessful) {
        this.response = response;
        this.successfully = wasSuccessful;
    }

    public boolean successfully() {
        return successfully;
    }

    public String getResponse() {
        return response;
    }
}
