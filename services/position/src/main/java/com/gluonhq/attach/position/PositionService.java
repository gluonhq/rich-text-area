/*
 * Copyright (c) 2016, Gluon
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

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.Optional;

/**
 * The PositionService provides details about a device's current location on
 * earth.
 *
 * <p>The PositionService provides a read-only {@link #positionProperty() position property}
 * that will be updated at regular intervals by the underlying platform implementation.
 * A user of the PositionService can listen to changes of the position by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #positionProperty() position property}.</p>
 *
 * <p>The service gets started by calling either {@link #start() } or
 * {@link #start(com.gluonhq.attach.position.Parameters) }, and can be stopped
 * at any time by calling {@link #stop() }.</p>
 *
 * <p>The desired accuracy can be modified by setting the proper {@link Parameters},
 * but the developer has to be aware of the risk of using higher precision, in
 * terms of battery consumption.</p>
 *
 * <p>The service can be used also in background mode</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(PositionService.class).ifPresent(service -> {
 *      service.positionProperty().addListener((obs, ov, nv) ->
 *          System.out.printf("Current position: %.5f, %.5f",
 *              nv.getLatitude(), nv.getLongitude()));
 *      service.start();
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permissions <code>android.permission.ACCESS_COARSE_LOCATION</code> and/or
 * <code>android.permission.ACCESS_FINE_LOCATION</code> need to be added to the
 * Android manifest.</p>
 *
 * <p>For background updates, the service <code>positionBackgroundService</code>
 * has to be included as well. Note that in this mode the accuracy could be reduced
 * by the system.</p>

 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 *    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 *    ...
 *    <service android:name="com.gluonhq.attach.android.AndroidPositionBackgroundService"
 *             android:process=":positionBackgroundService" />
 *    <activity android:name="com.gluonhq.attach.android.PermissionRequestActivity" />
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 * <p>Typically, the following keys are required:</p>
 * <pre>
 * {@code <key>NSLocationUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 6+)</string>
 *  <key>NSLocationWhenInUseUsageDescription</key>
 *  <string>Reason to use Location Service (iOS 8+)</string>}</pre>
 *
 * <p>With Background mode enabled these are the required keys:</p>
 * <pre>
 * {@code
 *         <key>UIBackgroundModes</key>
 *         <array>
 *             <string>location</string>
 *         </array>
 *         <key>NSLocationUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 6+)</string>
 *         <key>NSLocationAlwaysUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+) in background</string>
 *         <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
 *         <string>Reason to use Location Service (iOS 8+) in background</string>
 * }</pre>
 * <p>Note that in this mode, UI updates are not allowed</p>
 *
 * @since 3.0.0
 */
public interface PositionService {

    /**
     * Returns an instance of {@link PositionService}.
     * @return An instance of {@link PositionService}.
     */
    static Optional<PositionService> create() {
        return Services.get(PositionService.class);
    }

    /**
     * Default parameters used when the service is initalized with {@link #start() }.
     * These include Medium accuracy and service only working when the app is
     * active on foreground.
     *
     * To use custom parameters, see {@link #start(com.gluonhq.attach.position.Parameters) }
     */
    Parameters DEFAULT_PARAMETERS = new Parameters(Parameters.Accuracy.MEDIUM, false);

    /**
     * A read-only property containing information about the device's current
     * location on earth. The property can contain a <code>null</code> object
     * when the position of the device could be determined.
     *
     * @return a read-only object property containing the device's current location
     */
    ReadOnlyObjectProperty<Position> positionProperty();

    /**
     * The current position on earth of the device. Can return <code>null</code>
     * when the position of the device could not be determined, for instance
     * when the GPS has been turned off.
     *
     * @return the current position of the device
     */
    Position getPosition();

    /**
     * Starts the service with {@link #DEFAULT_PARAMETERS}. Developer must call
     * either this method or {@link #start(com.gluonhq.attach.position.Parameters) }
     * to start the service
     *
     * @since 3.8.0
     */
    void start();

    /**
     * Starts the service. Developer must call either this method or
     * {@link #start() } to start the service
     *
     * @param parameters Parameters for configuring the service, including desired
     * accuracy, minimum distance and time interval between notifications, or
     * background mode
     *
     * @since 3.8.0
     */
    void start(Parameters parameters);

    /**
     * Stops the service. Developer may call this method to stop the service on
     * demand
     * @since 3.8.0
     */
    void stop();

}