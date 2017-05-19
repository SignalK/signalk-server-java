/*
 * The SignalK developers license this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nz.co.fortytwo.signalk.processor;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import mjson.Json;
import nz.co.fortytwo.signalk.util.ConfigConstants;
import nz.co.fortytwo.signalk.util.Util;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RestApiProcessorTest {

	private static Logger logger = LogManager.getLogger(RestApiProcessorTest.class);
	
    @BeforeClass
    public static void initModel() throws Exception {
        Util.getConfig();
    }

    @Test
    public void shouldListAllEndpoints() {
        Json discovery = RestApiProcessor.discovery("localhost");
        logger.debug(discovery);
        Json endpoints = discovery.at("endpoints");
        String ver = Util.getConfigProperty(ConfigConstants.VERSION);
        Json version = endpoints.at("v1");
        assertThat(version.at("version").asString(), equalTo(ver.substring(0, 5)));
        assertThat(version.at("signalk-http").asString(), equalTo("http://localhost:8080/signalk/v1/api/"));
        assertThat(version.at("signalk-ws").asString(), equalTo("ws://localhost:3000/signalk/v1/stream"));
        assertThat(version.at("signalk-udp").asString(), equalTo("udp://localhost:55554"));
        assertThat(version.at("signalk-tcp").asString(), equalTo("tcp://localhost:55555"));
        assertThat(version.at("nmea-udp").asString(), equalTo("udp://localhost:55556"));
        assertThat(version.at("nmea-tcp").asString(), equalTo("tcp://localhost:55557"));
        assertThat(version.at("stomp").asString(), equalTo("stomp+nio://localhost:61613"));
        assertThat(version.at("mqtt").asString(), equalTo("mqtt://localhost:1883"));
    }
}