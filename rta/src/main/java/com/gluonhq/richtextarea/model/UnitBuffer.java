/*
 * Copyright (c) 2023, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.model;


import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A UnitBuffer is a collection of units and related operations, like getting their internal
 * or exportable text.
 * A PieceTable uses two buffers of units, original and addition, but the UnitBuffer can be
 * used elsewhere as a rich string.
 * In its simplest implementation, this would be a String class.
 * An example of more complex units is: "Emoji: \ud83d\ude00!", where the unit buffer will
 * have three units: [TU{'Emoji: '}, EU{1F600}, TU{'!'}], and the internal text will be:
 * "Emoji: \u2063!".
 * Note that the length of the original text is 10, while the internal length, after
 * replacing the emoji characters with the anchor is 9.
 */
public class UnitBuffer {

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            TextBuffer.ZERO_WIDTH_NO_BREAK_SPACE_TEXT + "([@#])([\\p{L}\\p{N}\\p{P}\\s]*)" + TextBuffer.ZERO_WIDTH_NO_BREAK_SPACE_TEXT,
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private final List<Unit> unitList;

    public UnitBuffer() {
        this(List.of());
    }

    public UnitBuffer(Unit unit) {
        this(List.of(unit));
    }

    public UnitBuffer(Collection<Unit> units) {
        unitList = new ArrayList<>(units);
    }

    /**
     * Gets the exportable text of the unit buffer. It is useful for clipboard or
     * serializing the document, but shouldn't be used internally to do Piece
     * operations
     * @return a string with the exportable text content
     */
    public String getText() {
        final StringBuilder sb = new StringBuilder();
        unitList.forEach(unit -> sb.append(unit.getText()));
        return sb.toString();
    }

    /**
     * Gets the internal text of the unit buffer. For text units, matches the exportable text.
     * For non-text units, this is used as an anchor point
     * @return a string with the internal text representation
     */
    public String getInternalText() {
        final StringBuilder sb = new StringBuilder();
        unitList.forEach(unit -> sb.append(unit.getInternalText()));
        return sb.toString();
    }

    /**
     * Gets the internal length of the unit buffer. Useful for Piece operations
     * @return an integer value of the internal number of positions that the unit spans
     */
    public int length() {
        return getUnitList().stream().mapToInt(Unit::length).sum();
    }

    /**
     * Adds a new unit to the buffer
     * @param unit the unit that is added
     */
    public void append(Unit unit) {
        unitList.add(unit);
    }

    /**
     * Adds a list of units to the buffer
     * @param units a list of units to be added
     */
    public void append(List<Unit> units) {
        unitList.addAll(units);
    }

    /**
     * Inserts a Unit into the buffer after a given position within its
     * internal length, splitting in two the unit found at that position,
     * if needed
     * @param unit the unit to insert into this buffer
     * @param position the position within the buffer range
     */
    public void insert(Unit unit, int position) {
        if (unit == null) {
            return;
        }
        if (position < 0 || position > length()) {
            return;
        }
        int accum = 0;
        List<Unit> buffer = new ArrayList<>();
        for (Unit u : unitList) {
            buffer.add(u);
            if (u.isEmpty()) continue;
            if (accum <= position && position <= accum + u.length()) {
                if (u instanceof TextUnit) {
                    buffer.remove(u);
                    String substring0 = u.getText().substring(0, position - accum);
                    if (substring0.length() > 0) {
                        buffer.add(new TextUnit(substring0));
                    }
                    buffer.add(unit);
                    String substring1 = u.getText().substring(position - accum);
                    if (substring1.length() > 0) {
                        buffer.add(new TextUnit(substring1));
                    }
                } else {
                    buffer.add(unit);
                }
                accum += unit.length();
            }
            accum += u.length();
        }
        unitList.clear();
        unitList.addAll(buffer);
    }

    /**
     * Removes content from the buffer of a given range within its
     * internal length, splitting in two the units found at the limits,
     * if needed
     * @param start the initial position of the range
     * @param end the end position of the range
     */
    public void remove(int start, int end) {
        if (Math.min(start, end) < 0 || Math.max(start, end) > length()) {
            return;
        }
        TextUnit unit = new TextUnit("\u2065");
        insert(unit, Math.max(start, end));
        insert(unit, Math.min(start, end));
        unitList.subList(unitList.indexOf(unit), unitList.lastIndexOf(unit) + 1).clear();
    }

    /**
     * Gives the list of units of this buffer
     * @return the list of units
     */
    public List<Unit> getUnitList() {
        return unitList;
    }

    /**
     * Check if the buffer is empty or not
     * @return true if the list of units is null or empty
     */
    public boolean isEmpty() {
        return unitList == null || unitList.isEmpty();
    }

    /**
     * Finds the unit that contains a range of document coordinates.
     * Convenience method to find the unit that a piece belongs to.
     * @param start the initial value of the range
     * @param end the end value of the range
     * @return the unit that has this range or an empty TextUnit
     */
    public Unit getUnitWithRange(int start, int end) {
        int accum = 0;
        for (Unit unit : unitList) {
            if (unit.isEmpty()) continue;
            if (accum <= start && end <= accum + unit.length()) {
                return unit;
            }
            accum += unit.length();
        }
        return new TextUnit("");
    }

    @Override
    public String toString() {
        return "UnitBuffer{" + unitList + "}";
    }

    /**
     * Utility method that parses an external text that might contain emoji unicode characters
     * and returns a UnitBuffer
     * @param text a string that might contain emoji unicode characters
     * @return a UnitBuffer with a list of units.
     */
    public static UnitBuffer convertTextToUnits(String text) {
        List<Unit> units = new ArrayList<>();
        TextUtils.convertToStringAndEmojiObjects(text).stream()
                .map(o -> {
                    if (o instanceof Emoji) {
                        return List.of(new EmojiUnit((Emoji) o));
                    } else {
                        return createTextAndBlockUnits((String) o);
                    }
                })
                .forEach(units::addAll);
        return new UnitBuffer(units);
    }

    private static List<Unit> createTextAndBlockUnits(String text) {
        ArrayList<Unit> units = new ArrayList<>();
        Matcher matcher = BLOCK_PATTERN.matcher(text);
        int previousEnd = 0;
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            if (matchStart > previousEnd) {
                units.add(new TextUnit(text.substring(previousEnd, matchStart)));
            }
            units.add(new BlockUnit(new Block(matcher.group(1) + matcher.group(2).trim())));
            previousEnd = matchEnd;
        }
        if (previousEnd <= text.length() - 1) {
            units.add(new TextUnit(text.substring(previousEnd)));
        }
        return units;
    }

}
