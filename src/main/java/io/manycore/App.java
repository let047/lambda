package io.manycore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It's a proof of concept to investigate the performance difference between native code and Java code on AWS Lambda.
 * <p>
 * It contains a Lambda handler and a simplified runtime
 * <p>
 * This code does not listen to input (for no special reason) and sends back the same results all the time.
 * <p>
 * Liberally copied from https://github.com/andthearchitect/aws-lambda-java-runtime
 */
public final class App {

    private static final String LAMBDA_VERSION_DATE = "2018-06-01";
    private static final String LAMBDA_RUNTIME_URL_TEMPLATE = "http://{0}/{1}/runtime/invocation/next";
    private static final String LAMBDA_INVOCATION_URL_TEMPLATE = "http://{0}/{1}/runtime/invocation/{2}/response";

    private static String getEnv(String name) {
        return System.getenv(name);
    }

    public static void main(String[] arg) {
        String runtimeApi = getEnv("AWS_LAMBDA_RUNTIME_API");
        String runtimeUrl = MessageFormat.format(LAMBDA_RUNTIME_URL_TEMPLATE, runtimeApi, LAMBDA_VERSION_DATE);
        String requestId;

        while (true) {

            SimpleHttpResponse event = get(runtimeUrl);
            requestId = getHeaderValue("Lambda-Runtime-Aws-Request-Id", event.getHeaders());

            try {
                // Invoke Handler Method
                LambdaMethodHandler l = new LambdaMethodHandler();
                String result = l.handleRequest("refrem is redrum", null);
                // Post the results of Handler Invocation
                String invocationUrl = MessageFormat.format(LAMBDA_INVOCATION_URL_TEMPLATE, runtimeApi, LAMBDA_VERSION_DATE, requestId);
                post(invocationUrl, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static SimpleHttpResponse readResponse(HttpURLConnection conn) throws IOException {

        // Map Response Headers
        HashMap<String, List<String>> headers = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }

        // Map Response Body
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();

        String line;

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        rd.close();

        return new SimpleHttpResponse(conn.getResponseCode(), headers, result.toString());
    }

    private static void setBody(HttpURLConnection conn, String body) throws IOException {
        OutputStream os = null;
        OutputStreamWriter osw = null;

        try {
            os = conn.getOutputStream();
            osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);

            osw.write(body);
            osw.flush();
        } finally {
            osw.close();
            os.close();
        }
    }

    private static SimpleHttpResponse post(String remoteUrl, String body) {
        SimpleHttpResponse output = null;

        try {
            URL url = new URL(remoteUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            setBody(conn, body);
            conn.connect();

            // We can probably skip this for speed because we don't really care about the response
            output = readResponse(conn);
        } catch (IOException ioe) {
            System.out.println("POST: " + remoteUrl);
            ioe.printStackTrace();
        }

        return output;
    }

    private static String getHeaderValue(String header, Map<String, List<String>> headers) {
        List<String> values = headers.get(header);

        // We don't expect any headers with multiple values, so for simplicity we'll just concat any that have more than one entry.
        return String.join(",", values);
    }


    private static SimpleHttpResponse get(String remoteUrl) {

        SimpleHttpResponse output = null;

        try {
            URL url = new URL(remoteUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Parse the HTTP Response
            output = readResponse(conn);
        } catch (IOException e) {
            System.out.println("GET: " + remoteUrl);
            e.printStackTrace();
        }

        return output;
    }
}