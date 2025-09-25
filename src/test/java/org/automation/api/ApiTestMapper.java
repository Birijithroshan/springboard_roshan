package org.automation.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiTestMapper {
    private static final Map<String, String> TEST_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("testGetAllPosts", "US201");
        map.put("testGetSinglePost", "US202");
        map.put("testGetAllUsers", "US203");
        map.put("testGetSingleUser", "US204");
        map.put("testGetCommentsForPost", "US205");
        map.put("testCreatePost", "US206");
        map.put("testUpdatePost", "US207");
        map.put("testPatchPost", "US208");
        map.put("testDeletePost", "US209");
        map.put("testGetNonExistentPost", "US210");
        TEST_MAP = Collections.unmodifiableMap(map);
    }

    public static Map<String, String> apiTests() {
        return TEST_MAP;
    }
}
