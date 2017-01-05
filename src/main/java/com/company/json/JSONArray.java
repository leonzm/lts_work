package com.company.json;

import com.company.util.Tool_Jackson;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * json处理类
 */
public class JSONArray extends ArrayList<Object> {

    private static final long serialVersionUID = -5239743947576281952L;
    private static final Logger LOGGER = Logger.getLogger(JSONArray.class.getName());

    public String getAsString(Integer index, String _default) {
        Object object = this.get(index);
        return object != null ? (String) object : _default;
    }

    public Boolean getAsBoolean(Integer index, Boolean _default) {
        Object object = this.get(index);
        return object != null ? (Boolean) object : _default;
    }

    public Long getAsLong(Integer index, Long _default) {
        Object object = this.get(index);
        return object != null ? (Long) object : _default;
    }

    public Integer getAsInteger(Integer index, Integer _default) {
        Object object = this.get(index);
        return object != null ? (Integer) object : _default;
    }

    public Double getAsDouble(Integer index, Double _default) {
        Object object = this.get(index);
        return object != null ? (Double) object : _default;
    }

    public Float getAsFloat(Integer index, Float _default) {
        Object object = this.get(index);
        return object != null ? (Float) object : _default;
    }

    public <T> T getAsClass(Integer index, TypeReference<T> typereference, T _default) {
        Object object = this.get(index);
        if (object == null) {
            return _default;
        }
        try {
            return (T) Tool_Jackson.toObject(Tool_Jackson.toJson(object), typereference);
        } catch (IOException e) {
            LOGGER.error("json 转换对象失败:", e);
            return _default;
        }
    }

    public <T> T getAsClass(Integer index, Class<T> clazz, T _default) {
        Object object = this.get(index);
        if (object == null) {
            return _default;
        }
        try {
            return (T) Tool_Jackson.toObject(Tool_Jackson.toJson(object), clazz);
        } catch (IOException e) {
            LOGGER.error("json 转换对象失败:", e);
            return _default;
        }
    }

    public JSONObject getAsJSONObject(Integer index, JSONObject _default) {
        return getAsClass(index, new TypeReference<JSONObject>() {
        }, _default);
    }

    public JSONArray getAsJSONArray(Integer index, JSONArray _default) {
        return getAsClass(index, new TypeReference<JSONArray>() {
        }, _default);
    }
}
