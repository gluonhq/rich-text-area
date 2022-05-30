package com.gluonhq.emoji;

import com.gluonhq.connect.converter.InputStreamIterableInputConverter;
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

    /**
     * Stores emojis with key's as their unified field
     */
    private static final Map<String, Emoji> EMOJI_UNICODE_MAP = new HashMap<>();

    static  {
        // https://github.com/iamcal/emoji-data/blob/master/emoji.json
        try (final InputStream emojiStream = EmojiData.class.getResourceAsStream("emoji.json")) {
            InputDataSource dataSource = new BasicInputDataSource(emojiStream);
            InputStreamIterableInputConverter<Emoji> converter = new EmojiIterableInputConverter(Emoji.class);
            ListDataReader<Emoji> listDataReader = new InputStreamListDataReader<>(dataSource, converter);
            for (Iterator<Emoji> it = listDataReader.iterator(); it.hasNext();) {
                Emoji e = it.next();
                EMOJI_UNICODE_MAP.put(e.getUnified(), e);
                e.getShort_name().ifPresent(s -> EMOJI_MAP.put(s, e));
                if (e.getSkin_variations() != null) {
                    e.getSkin_variations().values()
                            .forEach(v -> {
                                EMOJI_UNICODE_MAP.put(v.getUnified(), v);
                                v.getShort_name().ifPresent(s -> EMOJI_MAP.put(s, v));
                            });
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load emoji json file" + e.getMessage());
        }
    }

    /**
     * Returns Emoji from unicode string
     * @param unicodeText Unicode string representation
     * @return Emoji found for the string
     */
    public static Optional<Emoji> emojiFromUnicodeString(String unicodeText) {
        return EMOJI_MAP.values().stream()
                .filter(emoji -> emoji.character().equals(unicodeText))
                .findFirst();
    }

    /**
     * Returns Emoji from its hex code point string representation.
     * @param codePoint A string of one or more hex code points, concatenated using "-".
     * @return Emoji found for the codepoint string
     */
    public static Optional<Emoji> emojiFromCodepoints(String codePoint) {
        Emoji value = EMOJI_UNICODE_MAP.get(codePoint);
        if (value == null) {
            // try to qualify it
            value = EMOJI_UNICODE_MAP.get(codePoint + "-FE0F");
        }
        return Optional.ofNullable(value);
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

    public static List<Emoji> emojiFromCategory(String category) {
        return EMOJI_MAP.values().stream()
                .filter(emoji -> emoji.getCategory().isPresent())
                .filter(emoji -> category.contains(emoji.getCategory().get()))
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
