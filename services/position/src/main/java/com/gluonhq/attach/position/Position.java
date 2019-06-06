/*
 * Copyright (c) 2016, 2019 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

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
package com.gluonhq.attach.position;

/**
 * A class that contains the latitude and longitude coordinates that map to a
 * specific point location on earth.
 *
 * @since 3.0.0
 */
public final class Position {

    private final double latitude;
    private final double longitude;
    private final double altitude;

    /**
     * Construct a new position with the specified <code>latitude</code> and
     * <code>longitude</code>. It sets the <code>altitude</code> as 0.0.
     *
     * @param latitude the latitude coordinate of the new position
     * @param longitude the longitude coordinate of the new position
     */
    public Position(double latitude, double longitude) {
        this(latitude, longitude, 0.0);
    }

    /**
     * Construct a new position with the specified <code>latitude</code>,
     * <code>longitude</code> and <code>altitude</code>.
     *
     * @param latitude the latitude coordinate of the new position
     * @param longitude the longitude coordinate of the new position
     * @param altitude the altitude of the new position
     */
    public Position(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Return the latitude coordinate of the position.
     *
     * @return a double representing the latitude coordinate
     */
    public final double getLatitude() {
        return this.latitude;
    }

    /**
     * Return the longitude coordinate of the position.
     *
     * @return a double representing the longitude coordinate
     */
    public final double getLongitude() {
        return this.longitude;
    }

    /**
     * Return the altitude of the position, above mean sea level, in meters.
     *
     * @return a double representing the altitude in meters
     */
    public final double getAltitude() {
        return altitude;
    }

    @Override
    public String toString() {
        return "Position{" + "latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + '}';
    }

}