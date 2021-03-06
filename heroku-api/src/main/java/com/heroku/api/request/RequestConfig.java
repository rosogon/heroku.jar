package com.heroku.api.request;

import com.heroku.api.Heroku;
import com.heroku.api.parser.Json;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for an {@link EnumMap} to configure a {@link Request}. A {@link com.heroku.api.Heroku.RequestKey} serves as
 * a predetermined key for a name/value parameter.
 *
 * @author Naaman Newbold
 */
public class RequestConfig {

    private String appName;

    private final Map<Heroku.RequestKey, RequestConfig.Either> config = new EnumMap<Heroku.RequestKey, RequestConfig.Either>(Heroku.RequestKey.class);

    /**
     * Sets the {Heroku.RequestKey.AppName} parameter.
     * @param appName Name of the app to specify in the config.
     * @return A new {@link RequestConfig}
     */
    public RequestConfig app(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppName() {
        return this.appName;
    }

    /**
     * Sets a {@link com.heroku.api.Heroku.RequestKey} parameter.
     * @param key Heroku request key
     * @param value value of the property
     * @return A new {@link RequestConfig}
     */
    public RequestConfig with(Heroku.RequestKey key, String value) {
        RequestConfig newConfig = copy();
        newConfig.config.put(key, new Either(value));
        return newConfig;
    }

    /**
     * Sets a {@link com.heroku.api.Heroku.RequestKey} parameter.
     * @param key Heroku request key
     * @param value value of the property
     * @return A new {@link RequestConfig}
     */
    public RequestConfig with(Heroku.RequestKey key, Map<Heroku.RequestKey, Either> value) {
        RequestConfig newConfig = copy();
        newConfig.config.put(key, new Either(value));
        return newConfig;
    }

    public String get(Heroku.RequestKey key) {
        return config.get(key).string();
    }

    public Map<Heroku.RequestKey, Either> getMap(Heroku.RequestKey key) {
        return config.get(key).map();
    }

    public boolean has(Heroku.RequestKey key){
        return config.containsKey(key);
    }

    public String asJson() {
        return Json.encode(asMap());
    }

    public Map<String,Object> asMap() {
        return stringifyMap(config);
    }

    private Map<String,Object> stringifyMap(Map<Heroku.RequestKey, Either> map) {
        Map<String,Object> jsonMap = new HashMap<>();
        for (Heroku.RequestKey key : map.keySet()) {
            RequestConfig.Either either = map.get(key);
            if (either.is(String.class)) {
                jsonMap.put(key.queryParameter, either.string());
            } else if (either.is(Boolean.class)) {
                jsonMap.put(key.queryParameter, either.bool());
            } else {
                jsonMap.put(key.queryParameter, stringifyMap(either.map()));
            }
        }
        return jsonMap;
    }

    private RequestConfig copy() {
        RequestConfig newConfig = new RequestConfig();
        newConfig.app(this.appName);
        newConfig.config.putAll(config);
        return newConfig;
    }

    public static class Either {

        private Class type;

        private String string;

        private Boolean bool;

        private Map<Heroku.RequestKey, Either> map;

        public Either(Boolean value) {
            this.type = Boolean.class;
            this.bool = value;
        }

        public Either(String value) {
            this.type = String.class;
            this.string = value;
        }

        public Either(Map<Heroku.RequestKey, Either> value) {
            this.type = Map.class;
            this.map = value;
        }

        public String string() {
            return string;
        }

        public Boolean bool() {
            return bool;
        }

        public Map<Heroku.RequestKey, Either> map() {
            return map;
        }

        public Object value() {
            return (is(String.class)) ? string : map;
        }

        public Boolean is(Class c) {
            return c.equals(type);
        }
    }
}
