/*
 * Copyright (c) 2022, 2023, Gluon
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
package com.gluonhq.richtextarea;

import com.gluonhq.richtextarea.model.TextBuffer;
import javafx.scene.control.IndexRange;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.regex.Pattern;

public class Tools {

    public static final Pattern URL_PATTERN = Pattern.compile(
            "\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private Tools() {}

    public static IndexRange NO_SELECTION = new IndexRange(-1,-1);

    public static boolean isIndexRangeValid( IndexRange range ) {
        return range.getStart() >= 0 && range.getEnd() >= 0;
    }

    public static String insertText( String text, int position, String textToInsert ) {
        return new StringBuilder(text).insert(position, textToInsert).toString();
    }

    public static String deleteText( String text, int start, int end) {
        return new StringBuilder(text).delete(start, end).toString();
    }

    private static final String os = System.getProperty("os.name");
    public static final boolean WINDOWS = os.startsWith("Windows");
    public static final boolean MAC = os.startsWith("Mac");
    public static final boolean LINUX = os.startsWith("Linux");

    public static int clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static String getFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "-";
        }
        return name.substring(0, 1);
    }

    private static final Text helperText = new Text();
    private static final double TEXT_WRAPPING_WIDTH = helperText.getWrappingWidth();
    private static final double TEXT_LINE_SPACING = helperText.getLineSpacing();
    private static final String TEXT_CONTENT = helperText.getText();
    private static final TextBoundsType TEXT_BOUNDS_TYPE = helperText.getBoundsType();

    public static double computeStringWidth(Font font, String text) {
        helperText.setText(text);
        helperText.setFont(font);
        helperText.setWrappingWidth(0);
        helperText.setLineSpacing(0);
        double width = Math.min(helperText.prefWidth(-1), Double.MAX_VALUE);
        helperText.setWrappingWidth((int) Math.ceil(width));
        width = helperText.getLayoutBounds().getWidth();
        helperText.setWrappingWidth(TEXT_WRAPPING_WIDTH);
        helperText.setLineSpacing(TEXT_LINE_SPACING);
        helperText.setText(TEXT_CONTENT);
        return width;
    }

    public static double computeStringHeight(Font font, String text) {
        helperText.setText(text);
        helperText.setFont(font);
        helperText.setWrappingWidth((int) Double.MAX_VALUE);
        helperText.setLineSpacing(0);
        helperText.setBoundsType(TextBoundsType.LOGICAL);
        final double height = helperText.getLayoutBounds().getHeight();
        helperText.setWrappingWidth(TEXT_WRAPPING_WIDTH);
        helperText.setLineSpacing(TEXT_LINE_SPACING);
        helperText.setText(TEXT_CONTENT);
        helperText.setBoundsType(TEXT_BOUNDS_TYPE);
        return height;
    }

    public static String formatTextWithAnchors(String text) {
        return text.replaceAll("\n", "<n>")
                .replaceAll(TextBuffer.ZERO_WIDTH_TEXT, "<a>")
                .replaceAll(TextBuffer.OBJECT_REPLACEMENT_CHARACTER_TEXT, "<b>")
                .replaceAll(TextBuffer.EMOJI_ANCHOR_TEXT, "<e>")
                .replaceAll("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, "<t>");
    }

    public static boolean isURL(String text) {
        if (text == null) return false;
        return URL_PATTERN.matcher(text).matches();
    }

}
