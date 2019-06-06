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
package com.gluonhq.attach.orientation;

import java.util.Optional;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Orientation;

/**
 * With the orientation service you can detect whether the device is currently oriented
 * horizontally or vertically.
 *
 * <p>The OrientationService provides a read-only {@link #orientationProperty() orientation property}
 * that will be updated when the orientation of the device changes. A user of the OrientationService
 * can listen to changes of the orientation by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #orientationProperty() orientation property}.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(OrientationService.class).ifPresent(service -> {
 *      Orientation orientation = service.getOrientation();
 *      System.out.println("Current orientation: " + orientation.name());
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface OrientationService {

    /**
     * Returns an instance of {@link OrientationService}.
     * @return An instance of {@link OrientationService}.
     */
    static Optional<OrientationService> create() {
        return Services.get(OrientationService.class);
    }

    /**
     * A read-only property containing the current orientation of the device, which will be
     * updated automatically whenever the device orientation changes.
     * @return A read-only property containing the current orientation of the device 
     */
    ReadOnlyObjectProperty<Orientation> orientationProperty();

    /**
     * Returns the current orientation of the device.
     * @return An optional containing the orientation of the device, or empty if unknown
     */
    Optional<Orientation> getOrientation();
}