package io.arex.inst.runtime.service;

public interface DataCollector {
    void start();

    void save(String mockData);

    String query(String postData);
}
