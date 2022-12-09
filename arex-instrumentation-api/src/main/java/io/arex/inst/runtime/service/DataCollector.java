package io.arex.inst.runtime.service;

public interface DataCollector {

    void save(String mockData, String category, boolean isReplay);

    String query(String postData, String category);
}
