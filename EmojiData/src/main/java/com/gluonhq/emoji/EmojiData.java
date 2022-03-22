package com.gluonhq.emoji;

import com.gluonhq.connect.converter.InputStreamIterableInputConverter;
import com.gluonhq.connect.converter.JsonIterableInputConverter;
import com.gluonhq.connect.provider.InputStreamListDataReader;
import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.source.BasicInputDataSource;
import com.gluonhq.connect.source.InputDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class EmojiData {

    /**
     * Stores emojis with key's as their short name.
     */
    private static final Map<String, Emoji> EMOJI_MAP = new HashMap<>();

    static  {
        try (final InputStream emojiStream = EmojiData.class.getResourceAsStream("emoji.json")) {
            InputDataSource dataSource = new BasicInputDataSource(emojiStream);
            InputStreamIterableInputConverter<Emoji> converter = new JsonIterableInputConverter<>(Emoji.class);
            ListDataReader<Emoji> listDataReader = new InputStreamListDataReader<>(dataSource, converter);
            for (Iterator<Emoji> it = listDataReader.iterator(); it.hasNext();) {
                Emoji e = it.next();
                if (e.getShort_name().isPresent()) {
                    EMOJI_MAP.put(e.getShort_name().get(), e);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load emoji json file" + e.getMessage());
//            throw new IllegalStateException("Could not load emoji json file" + e.getMessage());
        }
    }
    
    public static Optional<Emoji> emojiFromShortName(String shortName) {
        return Optional.ofNullable(EMOJI_MAP.get(shortName));        
    }

    public static Optional<Emoji> emojiFromCodeName(String text) {
        if (text.startsWith(":") && text.endsWith(":")) {
            return emojiFromShortName(text.substring(1, text.length() - 1));
        }
        return Optional.empty();
    }

    public static Optional<Emoji> emojiFromUnicode(String unicodeText) {
        return EMOJI_MAP.values().stream()
                .filter(emoji -> emoji.character().equals(unicodeText))
                .findFirst();
    }

    public static List<Emoji> emojiFromCategory(String category) {
        return EMOJI_MAP.values().stream()
                .filter(emoji -> emoji.getCategory().isPresent())
                .filter(emoji -> emoji.getCategory().get().equalsIgnoreCase(category))
                .sorted(Comparator.comparingInt(Emoji::getSort_order))
                .collect(Collectors.toList());
    }
    
    public static List<Emoji> search(String text) {
        List<Emoji> emojis = new ArrayList<>();
        for (String s : text.split(" ")) {
            emojis.addAll(EMOJI_MAP.entrySet().stream()
                    .filter(es -> es.getKey().contains(s))
                    .map(Map.Entry::getValue)
                    .sorted(Comparator.comparingInt(Emoji::getSort_order))
                    .collect(Collectors.toList()));
        }
        return emojis;
    }
    
    public static Set<String> shortNames() {
        return EMOJI_MAP.keySet();
    }
    
    public static Set<String> categories() {
        return EMOJI_MAP.values().stream()
                .filter(emoji -> emoji.getCategory().isPresent())
                .map(emoji -> emoji.getCategory().get())
                .collect(Collectors.toSet());
    }
    
    public static String emojiForText(String shortName) {
        return emojiForText(shortName, false);
    }

    public static String emojiForText(String shortName, boolean strip) {
        final Emoji emoji = EMOJI_MAP.get(shortName);
        if (emoji == null) {
            return strip ? "" : shortName;
        }
        else return emoji.character();
    }
}
