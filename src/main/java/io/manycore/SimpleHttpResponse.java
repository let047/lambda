package io.manycore;

import java.util.List;
import java.util.Map;

public class SimpleHttpResponse {

    private final Integer responseCode;
    private final Map<String, List<String>> headers;
    private final String body;


    public SimpleHttpResponse(Integer responseCode, Map<String, List<String>> headers, String body) {
        this.responseCode = responseCode;
        this.headers = headers;
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "SimpleHttpResponse{" +
                "responseCode=" + responseCode +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}