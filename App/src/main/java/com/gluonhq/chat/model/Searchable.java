package com.gluonhq.chat.model;

import java.util.Locale;

public abstract class Searchable {

    public abstract boolean contains(String keyword);

    static <T> boolean containsKeyword( final T source, final String keyword ) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return source != null && source.toString().toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }
}

