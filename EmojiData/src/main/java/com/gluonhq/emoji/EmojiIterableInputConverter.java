package com.gluonhq.emoji;

import com.gluonhq.connect.converter.InputStreamIterableInputConverter;
import com.gluonhq.connect.converter.JsonConverter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmojiIterableInputConverter extends InputStreamIterableInputConverter<Emoji> implements Iterator<Emoji> {

    private JsonArray jsonArray;
    private int index;
    private final JsonConverter<Emoji> converter;
    private final Map<String, Method> settersMappedByPropertyName = new HashMap<>();

    public EmojiIterableInputConverter(Class<Emoji> targetClass) {
        Method[] allMethods = targetClass.getMethods();
        for (Method method : allMethods) {
            if (method.getName().startsWith("set")) {
                settersMappedByPropertyName.put(method.getName().toLowerCase(Locale.ROOT).substring(3), method);
            }
        }
        converter = new JsonConverter<>(targetClass) {

            @Override
            public Emoji readFromJson(JsonObject json) {
                Emoji t = new Emoji();
                for (String property : settersMappedByPropertyName.keySet()) {
                    if (!json.containsKey(property)) {
                        continue;
                    }
                    Method setter = settersMappedByPropertyName.get(property);
                    Object[] args = new Object[1];
                    JsonValue jsonValue = json.get(property);
                    switch (jsonValue.getValueType()) {
                        case NULL:  args[0] = null; break;
                        case FALSE: args[0] = Boolean.FALSE; break;
                        case TRUE: args[0] = Boolean.TRUE; break;
                        case STRING:  args[0] = ((JsonString) jsonValue).getString(); break;
                        case NUMBER: args[0] = ((JsonNumber) jsonValue).intValue(); break;
                        case ARRAY:
                            JsonArray arrayProperty = (JsonArray) jsonValue;
                            List<Object> values = new ArrayList<>();
                            args[0] = values;
                            for (JsonValue arrayValue : arrayProperty) {
                                JsonString stringArrayValue = (JsonString) arrayValue;
                                values.add(stringArrayValue.getString());
                            }
                            break;
                        case OBJECT:
                            Map<String, Emoji> map = new HashMap<>();
                            for (String key : ((JsonObject) jsonValue).keySet()) {
                                JsonObject jsonObject = ((JsonObject) jsonValue).getJsonObject(key);
                                if (jsonObject != null) {
                                    JsonConverter<Emoji> jsonConverter = new JsonConverter<>(Emoji.class);
                                    map.put(key, jsonConverter.readFromJson(jsonObject));
                                }
                            }
                            args[0] = map;
                            break;
                        default: break;
                    }

                    try {
                        setter.invoke(t, args);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                        System.out.println("Failed to call setter " + setter + " with value " + property + " " + ex);
                    }
                }
                return t;
            }
        };
    }

    @Override
    public boolean hasNext() {
        return index < jsonArray.size();
    }

    @Override
    public Emoji next() {
        JsonObject jsonObject = jsonArray.getJsonObject(index++);
        return converter.readFromJson(jsonObject);
    }

    @Override
    public Iterator<Emoji> iterator() {
        index = 0;
        try (JsonReader reader = Json.createReader(getInputStream())) {
            jsonArray = reader.readArray();
        }
        return this;
    }
}
