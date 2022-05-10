package io.arex.foundation.services;

import io.arex.foundation.util.StringUtil;

import java.util.*;


public class IgnoreService {

    private static final Set<String> ignoreServices = new HashSet<>();
    private static final Set<String> enablePackages = new HashSet<>();

    static {
        ignoreServices.add("POST /artemis-discovery-service/api/discovery/lookup.json");
        ignoreServices.add("POST /api/CMSGetServer");
        ignoreServices.add("POST /api/storage/record/query");
        ignoreServices.add("POST /api/storage/record/save");
    }

    /**
     * Register a service that will not capture data and playback
     * @param method: http method, like POST
     * @param url: service address(No need for ip and port), like /api/XxxService
     */
    public static void registerIgnoreService(String method, String url) {
        if (StringUtil.isEmpty(method) || StringUtil.isEmpty(url)) {
            return;
        }

        ignoreServices.add(method + " " + url);
    }

    /**
     * Enable the target mock by config
     * default return true
     */
    public static boolean isServiceEnabled(String targetName) {
        return !ignoreServices.contains(targetName);
    }

}
