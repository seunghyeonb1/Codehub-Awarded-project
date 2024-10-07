package io.clh.bookstore.untils;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

public class Metrics {
    private static final Counter totalRequests = Counter.build()
            .name("requests_total").help("Total requests.").register();
    private static final Histogram requestLatency = Histogram.build()
            .name("requests_latency_seconds").help("Request latency in seconds.").register();

    public static void incrementTotalRequests() {
        totalRequests.inc();
    }

    public static Histogram.Timer startRequestTimer() {
        return requestLatency.startTimer();
    }
}
