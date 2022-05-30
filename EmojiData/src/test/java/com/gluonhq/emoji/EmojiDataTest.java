package com.gluonhq.emoji;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.gluonhq.emoji.EmojiData.emojiForText;
import static com.gluonhq.emoji.EmojiData.emojiFromCodeName;
import static com.gluonhq.emoji.EmojiData.emojiFromCodepoints;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmojiDataTest {

    @Test
    public void emojiForTextKnown() {
        assertEquals("😄", emojiForText("smile"));
    }

    @Test
    public void emojiForTextUnknown() {
        assertEquals("unknown", emojiForText("unknown"));
    }

    @Test
    public void emojiForTextStripForKnown() {
        assertEquals("😄", emojiForText("smile", true));
    }

    @Test
    public void emojiForTextStripForUnknown() {
        assertEquals("", emojiForText("unknown", true));
    }

    @Test
    public void emojiForColonTextTest() {
        assertNotNull(emojiFromCodeName(":smile:"));
        assertNotNull(emojiFromCodeName(":kissing:"));
    }

    @Test
    public void emojiFromUnicodeTest() {
        Optional<Emoji> wavingHands = emojiFromCodepoints("1F44B");
        assertFalse(wavingHands.isEmpty());
        Emoji emoji = assertDoesNotThrow(wavingHands::get);
        assertNotNull(emoji);
        assertEquals("1F44B", emoji.getUnified());
        assertEquals("\uD83D\uDC4B", emoji.character());
    }

    @Test
    public void emojiForSkinToneTest() {
        Optional<Emoji> wavingHandsTone = emojiFromCodepoints("1F44B-1F3FC");
        assertFalse(wavingHandsTone.isEmpty());
        Emoji emoji = assertDoesNotThrow(wavingHandsTone::get);
        assertNotNull(emoji);
        assertEquals("1F44B-1F3FC", emoji.getUnified());
        assertEquals("\uD83D\uDC4B\uD83C\uDFFC", emoji.character());
    }

    @Test
    public void emojiForCountryFlagTest() {
        Optional<Emoji> scotland = emojiFromCodepoints("1F3F4-E0067-E0062-E0073-E0063-E0074-E007F");
        assertFalse(scotland.isEmpty());
        Emoji emoji = assertDoesNotThrow(scotland::get);
        assertNotNull(emoji);
        assertEquals("1F3F4-E0067-E0062-E0073-E0063-E0074-E007F", emoji.getUnified());
        assertEquals("\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F", emoji.character());
    }

    @Test
    public void invalidTwoEmojisUnicodeTest() {
        Optional<Emoji> twoEmojis = emojiFromCodepoints("1F603-1F606");
        assertTrue(twoEmojis.isEmpty());
    }

    @Test
    public void validEmojiConnectorUnicodeTest() {
        Optional<Emoji> family = emojiFromCodepoints("1F9D1-200D-1F91D-200D-1F9D1");
        assertFalse(family.isEmpty());
        Emoji emoji = assertDoesNotThrow(family::get);
        assertNotNull(emoji);
        assertEquals("1F9D1-200D-1F91D-200D-1F9D1", emoji.getUnified());
        assertEquals("\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1", emoji.character());
    }

    @Test
    public void invalidTwoFlagsUnicodeTest() {
        Optional<Emoji> twoFlags = emojiFromCodepoints("1F1E6-1F1E8-1F1E6-1F1E9");
        assertTrue(twoFlags.isEmpty());
    }
}