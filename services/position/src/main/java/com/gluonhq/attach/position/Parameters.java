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
 * The Position Service can be configured with several parameters. These
 include desired accuracy, from lowest to highest. This may have a high impact
 in the user's battery, so it is developer's responsibility to use the less 
 accuracy as possible.

 Distance filter and timeInterval are the minimum values to get notification in
 location changes.

 Background mode will allow the service to operate even when the app is not in
 foreground.
 *
 * @since 3.8.0
 */
public class Parameters {

    /**
     * Possible accuracy values. Higher accuracy values return more precise 
     * locations with more frequency.
     * Warning: The higher the accuracy, the higher the battery consumption
     */
    public enum Accuracy {
        LOWEST(90000, 1000),
        LOW(30000, 100),
        MEDIUM(5000, 15),
        HIGH(1000, 1),
        HIGHEST(100, 0);

        final long timeInterval;
        final float distanceFilter;

        Accuracy(long timeInterval, float distanceFilter) {
            this.timeInterval = timeInterval;
            this.distanceFilter = distanceFilter;
        }

        /**
         *
         * @return Time interval in ms for the given accuracy value
         */
        public long getTimeInterval() {
            return timeInterval;
        }

        /**
         *
         * @return distance in m for the given accuracy value
         */
        public float getDistanceFilter() {
            return distanceFilter;
        }

    }

    /**
     * Desired accuracy
     * Warning: the higher the accuracy, the higher battery consumption
     */
    private final Accuracy accuracy;

    /**
     * The minimum number of milliseconds between location updates.
     */
    private final long timeInterval;

    /**
     * The minimum number of meters between location updates.
     */
    private final float distanceFilter;

    /**
     * Allow position updates even if app is not running on foreground
     */
    private final boolean backgroundModeEnabled;

    /**
     * Set the accuracy value and the given time interval and distance filter
     * specified for that value.
     *
     * @param accuracy Desired accuracy in location updates
     * @param backgroundModeEnabled allows position updates when the app is 
     * running in background 
     */
    public Parameters(Accuracy accuracy, boolean backgroundModeEnabled) {
        this(accuracy, accuracy.getTimeInterval(), accuracy.getDistanceFilter(), backgroundModeEnabled);
    }

    /**
     * Set the accuracy value, a time interval and distance filter.
     *
     * @param accuracy Desired accuracy in location updates
     * @param timeInterval minimum number of milliseconds between location 
     * updates.
     * @param distanceFilter minimum number of meters between location updates.
     * @param backgroundModeEnabled allows position updates when the app is 
     * running in background 
     */
    public Parameters(Accuracy accuracy, long timeInterval, float distanceFilter, boolean backgroundModeEnabled) {
        this.accuracy = accuracy;
        this.timeInterval = timeInterval;
        this.distanceFilter = distanceFilter;
        this.backgroundModeEnabled = backgroundModeEnabled;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public float getDistanceFilter() {
        return distanceFilter;
    }

    public boolean isBackgroundModeEnabled() {
        return backgroundModeEnabled;
    }

    @Override
    public String toString() {
        return "Parameters{" + "accuracy=" + accuracy + ", timeInterval=" + timeInterval + ", distanceFilter=" + distanceFilter + ", backgroundModeEnabled=" + backgroundModeEnabled + '}';
    }

}