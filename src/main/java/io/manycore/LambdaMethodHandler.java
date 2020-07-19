package io.manycore;

import com.amazonaws.services.lambda.runtime.Context;

public class LambdaMethodHandler {

    public String handleRequest(String event, Context context) {
        System.out.println("EVENT: " + event + "\n");
        System.out.println("EVENT TYPE: " + event.getClass().toString() + "\n");

        return "Hello World - " + event + "\n";
    }
}
