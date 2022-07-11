/*
 * Copyright (c) 2022, Gluon
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

import java.util.Objects;

/**
 * ImageDecoration is a {@link Decoration} that can be applied to a fragment of text in order to place
 * an image at its location.
 */
public class ImageDecoration implements Decoration {

    private final int width;
    private final int height;
    private final String url;
    private final String link;

    public ImageDecoration(String url) {
        this(url, -1, -1, null);
    }

    public ImageDecoration(String url, int width, int height) {
        this(url, width, height, null);
    }

    public ImageDecoration(String url, String link) {
        this(url, -1, -1, link);
    }

    public ImageDecoration(String url, int width, int height, String link) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.link = link;
    }

    /**
     * Returns the string with the url of the image. It can be a resource path, a file path, or a valid URL.
     *
     * @return a string with the image's url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the width used for the image inserted in the RichTextArea control. Resizing, if needed,
     * is done preserving the aspect ratio of the image. If the value is -1,
     * the image will use its original width, but being limited to the control area.
     *
     * @defaultValue -1
     *
     * @return the width of the image in the text, or -1 to use the original image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height used for the image inserted in the RichTextArea control. Resizing, if needed,
     * is done preserving the aspect ratio of the image. If the value is -1,
     * the image will use its original height, but being limited to the control area.
     *
     * @defaultValue -1
     *
     * @return the height of the image in the text, or -1 to use the original image height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets a string with a URL, if any, that can be used to set a hyperlink on the image itself
     *
     * @defaultValue null
     *
     * @return a string with a URL or null
     */
    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageDecoration that = (ImageDecoration) o;
        return width == that.width && height == that.height && url.equals(that.url) && link.equals(that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, url, link);
    }

    @Override
    public String toString() {
        return "IDec{" +
                "width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
