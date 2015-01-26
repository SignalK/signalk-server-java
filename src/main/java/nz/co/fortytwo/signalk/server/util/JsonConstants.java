/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nz.co.fortytwo.signalk.server.util;

public class JsonConstants {

	
	
	public static final String MSG_TYPE = "MSG_TYPE";
	public static final String SERIAL = "SERIAL";
	public static final String EXTERNAL_IP = "EXTERNAL_IP";
	public static final String INTERNAL_IP = "INTERNAL_IP";
	public static final String MSG_PORT = "MSG_PORT";
	public static final String MSG_APPROVAL = "MSG_APPROVAL";
	public static final Object REQUIRED = "REQUIRED";
	
	public static final String VESSELS = "vessels";
	public static final String SELF = Util.getConfigProperty(Constants.SELF);
	public static final String CONTEXT = "context";
	public static final String UPDATES = "updates";
	public static final String SUBSCRIBE = "subscribe";
	public static final String UNSUBSCRIBE = "unsubscribe";
	public static final String SOURCE = "source";
	public static final String DEVICE = "device";
	public static final String TIMESTAMP = "timestamp";
	public static final String SRC = "src";
	public static final String PGN = "pgn";
	public static final String VALUE = "value";
	public static final String VALUES = "values";
	public static final String PATH = "path";
	public static final String PERIOD = "period";
	public static final String MIN_PERIOD = "minPeriod";
	public static final String SIGNALK_FORMAT="SIGNALK_FORMAT";
	public static final String FORMAT="format";
	public static final String FORMAT_DELTA="delta";
	public static final String FORMAT_FULL="full";
	public static final String POLICY="policy";
	public static final String POLICY_FIXED = "fixed";
	public static final String POLICY_INSTANT = "instant";
	public static final String POLICY_IDEAL = "ideal";
	
	public static final String N2K_MESSAGE = "N2K_MESSAGE";
	
	
	//public static final String name = "name";
	//public static final String mmsi = "mmsi";
	public static final String source = "source";
	public static final String timezone = "timezone";
	

	public static final String SIGNALK_AUTH = "/signalk/auth";
	public static final String SIGNALK_API = "/signalk/api";
	public static final String SIGNALK_SUBSCRIBE = "/signalk/stream";
	public static final String SIGNALK_WS = "/signalk/stream";
	
	public static final String resources="resources";
	public static final String resources_charts="resources.charts";
	public static final String resources_charts_id="resources.charts.UUID";
	public static final String resources_charts_id_chartFormat="resources.charts.UUID.chartFormat";
	public static final String resources_charts_id_chartUrl="resources.charts.UUID.chartUrl";
	public static final String resources_charts_id_description="resources.charts.UUID.description";
	public static final String resources_charts_id_identifier="resources.charts.UUID.identifier";
	public static final String resources_charts_id_name="resources.charts.UUID.name";
	public static final String resources_charts_id_tilemapUrl="resources.charts.UUID.tilemapUrl";
	public static final String resources_notes="resources.notes";
	public static final String resources_notes_id="resources.notes.UUID";
	public static final String resources_notes_id_description="resources.notes.UUID.description";
	public static final String resources_notes_id_mimeType="resources.notes.UUID.mimeType";
	public static final String resources_notes_id_region="resources.notes.UUID.region";
	public static final String resources_notes_id_title="resources.notes.UUID.title";
	public static final String resources_notes_id_url="resources.notes.UUID.url";
	public static final String resources_regions="resources.regions";
	public static final String resources_regions_id="resources.regions.UUID";
	public static final String resources_regions_id_description="resources.regions.UUID.description";
	public static final String resources_regions_id_name="resources.regions.UUID.name";
	public static final String resources_regions_id_waypoints="resources.regions.UUID.waypoints";
	public static final String resources_routes="resources.routes";
	public static final String resources_routes_id="resources.routes.UUID";
	public static final String resources_routes_id_description="resources.routes.UUID.description";
	public static final String resources_routes_id_distance="resources.routes.UUID.distance";
	public static final String resources_routes_id_name="resources.routes.UUID.name";
	public static final String resources_routes_id_waypoints="resources.routes.UUID.waypoints";
	public static final String resources_waypoints="resources.waypoints";
	public static final String resources_waypoints_id="resources.waypoints.UUID";
	public static final String resources_waypoints_id_description="resources.waypoints.UUID.description";
	public static final String resources_waypoints_id_name="resources.waypoints.UUID.name";
	public static final String resources_waypoints_id_position="resources.waypoints.UUID.position";
	public static final String resources_waypoints_id_position_altitude="resources.waypoints.UUID.position.altitude";
	public static final String resources_waypoints_id_position_latitude="resources.waypoints.UUID.position.latitude";
	public static final String resources_waypoints_id_position_longitude="resources.waypoints.UUID.position.longitude";
	public static final String resources_waypoints_id_type="resources.waypoints.UUID.type";
	public static final String self="self";
	public static final String version="version";
	public static final String vessels="vessels";
	public static final String vessels_id="vessels.UUID";
	public static final String alarms="alarms";
	public static final String alarms_anchorAlarmMethod="alarms.anchorAlarmMethod";
	public static final String alarms_anchorAlarmState="alarms.anchorAlarmState";
	public static final String alarms_autopilotAlarmMethod="alarms.autopilotAlarmMethod";
	public static final String alarms_autopilotAlarmState="alarms.autopilotAlarmState";
	public static final String alarms_engineAlarmMethod="alarms.engineAlarmMethod";
	public static final String alarms_engineAlarmState="alarms.engineAlarmState";
	public static final String alarms_fireAlarmMethod="alarms.fireAlarmMethod";
	public static final String alarms_fireAlarmState="alarms.fireAlarmState";
	public static final String alarms_gasAlarmMethod="alarms.gasAlarmMethod";
	public static final String alarms_gasAlarmState="alarms.gasAlarmState";
	public static final String alarms_gpsAlarmMethod="alarms.gpsAlarmMethod";
	public static final String alarms_gpsAlarmState="alarms.gpsAlarmState";
	public static final String alarms_maydayAlarmMethod="alarms.maydayAlarmMethod";
	public static final String alarms_maydayAlarmState="alarms.maydayAlarmState";
	public static final String alarms_panpanAlarmMethod="alarms.panpanAlarmMethod";
	public static final String alarms_panpanAlarmState="alarms.panpanAlarmState";
	public static final String alarms_powerAlarmMethod="alarms.powerAlarmMethod";
	public static final String alarms_powerAlarmState="alarms.powerAlarmState";
	public static final String alarms_silentInterval="alarms.silentInterval";
	public static final String alarms_windAlarmMethod="alarms.windAlarmMethod";
	public static final String alarms_windAlarmState="alarms.windAlarmState";
	public static final String communication="communication";
	public static final String communication_callsignDsc="communication.callsignDsc";
	public static final String communication_callsignHf="communication.callsignHf";
	public static final String communication_callsignVhf="communication.callsignVhf";
	public static final String communication_cellPhone="communication.cellPhone";
	public static final String communication_crewNames="communication.crewNames";
	public static final String communication_email="communication.email";
	public static final String communication_emailHf="communication.emailHf";
	public static final String communication_satPhone="communication.satPhone";
	public static final String communication_skipperName="communication.skipperName";
	public static final String design="design";
	public static final String design_airHeight="design.airHeight";
	public static final String design_beam="design.beam";
	public static final String design_canoeDraft="design.canoeDraft";
	public static final String design_displacement="design.displacement";
	public static final String design_draft="design.draft";
	public static final String design_loa="design.loa";
	public static final String design_loh="design.loh";
	public static final String design_lwl="design.lwl";
	public static final String design_name="design.name";
	public static final String design_sailArea="design.sailArea";
	public static final String env="environment";
	public static final String env_airPressure="environment.airPressure";
	public static final String env_airPressureChangeRateAlarm="environment.airPressureChangeRateAlarm";
	public static final String env_airTemp="environment.airTemp";
	public static final String env_depth="environment.depth";
	public static final String env_depth_belowKeel="environment.depth.belowKeel";
	public static final String env_depth_belowSurface="environment.depth.belowSurface";
	public static final String env_depth_belowTransducer="environment.depth.belowTransducer";
	public static final String env_depth_surfaceToTransducer="environment.depth.surfaceToTransducer";
	public static final String env_depth_transducerToKeel="environment.depth.transducerToKeel";
	public static final String env_humidity="environment.humidity";
	public static final String env_salinity="environment.salinity";
	public static final String env_tide="environment.tide";
	public static final String env_tide_heightHigh="environment.tide.heightHigh";
	public static final String env_tide_heightLow="environment.tide.heightLow";
	public static final String env_tide_heightNow="environment.tide.heightNow";
	public static final String env_tide_timeHigh="environment.tide.timeHigh";
	public static final String env_tide_timeLow="environment.tide.timeLow";
	public static final String env_waterTemp="environment.waterTemp";
	public static final String env_wind="environment.wind";
	public static final String env_wind_angleApparent="environment.wind.angleApparent";
	public static final String env_wind_angleTrue="environment.wind.angleTrue";
	public static final String env_wind_directionChangeAlarm="environment.wind.directionChangeAlarm";
	public static final String env_wind_directionMagnetic="environment.wind.directionMagnetic";
	public static final String env_wind_directionTrue="environment.wind.directionTrue";
	public static final String env_wind_speedAlarm="environment.wind.speedAlarm";
	public static final String env_wind_speedApparent="environment.wind.speedApparent";
	public static final String env_wind_speedOverGround="environment.wind.speedOverGround";
	public static final String env_wind_speedTrue="environment.wind.speedTrue";
	public static final String mmsi="mmsi";
	public static final String name="name";
	public static final String nav="navigation";
	public static final String nav_activeRoute="navigation.activeRoute";
	public static final String nav_activeRoute_bearingActual="navigation.activeRoute.bearingActual";
	public static final String nav_activeRoute_bearingDirect="navigation.activeRoute.bearingDirect";
	public static final String nav_activeRoute_courseRequired="navigation.activeRoute.courseRequired";
	public static final String nav_activeRoute_eta="navigation.activeRoute.eta";
	public static final String nav_activeRoute_route="navigation.activeRoute.route";
	public static final String nav_activeRoute_startTime="navigation.activeRoute.startTime";
	public static final String nav_activeRoute_waypoint="navigation.activeRoute.waypoint";
	public static final String nav_activeRoute_waypoint_last="navigation.activeRoute.waypoint.last";
	public static final String nav_activeRoute_waypoint_lastTime="navigation.activeRoute.waypoint.lastTime";
	public static final String nav_activeRoute_waypoint_next="navigation.activeRoute.waypoint.next";
	public static final String nav_activeRoute_waypoint_nextEta="navigation.activeRoute.waypoint.nextEta";
	public static final String nav_activeRoute_waypoint_xte="navigation.activeRoute.waypoint.xte";
	public static final String nav_anchor="navigation.anchor";
	public static final String nav_anchor_currentRadius="navigation.anchor.currentRadius";
	public static final String nav_anchor_maxRadius="navigation.anchor.maxRadius";
	public static final String nav_anchor_position="navigation.anchor.position";
	public static final String nav_anchor_position_altitude="navigation.anchor.position.altitude";
	public static final String nav_anchor_position_latitude="navigation.anchor.position.latitude";
	public static final String nav_anchor_position_longitude="navigation.anchor.position.longitude";
	public static final String nav_courseOverGroundMagnetic="navigation.courseOverGroundMagnetic";
	public static final String nav_courseOverGroundTrue="navigation.courseOverGroundTrue";
	public static final String nav_current="navigation.current";
	public static final String nav_current_drift="navigation.current.drift";
	public static final String nav_current_setMagnetic="navigation.current.setMagnetic";
	public static final String nav_current_setTrue="navigation.current.setTrue";
	public static final String nav_destination="navigation.destination";
	public static final String nav_destination_altitude="navigation.destination.altitude";
	public static final String nav_destination_eta="navigation.destination.eta";
	public static final String nav_destination_latitude="navigation.destination.latitude";
	public static final String nav_destination_longitude="navigation.destination.longitude";
	public static final String nav_gnss="navigation.gnss";
	public static final String nav_gnss_antennaAltitude="navigation.gnss.antennaAltitude";
	public static final String nav_gnss_differentialAge="navigation.gnss.differentialAge";
	public static final String nav_gnss_differentialReference="navigation.gnss.differentialReference";
	public static final String nav_gnss_geoidalSeparation="navigation.gnss.geoidalSeparation";
	public static final String nav_gnss_horizontalDilution="navigation.gnss.horizontalDilution";
	public static final String nav_gnss_quality="navigation.gnss.quality";
	public static final String nav_gnss_satellites="navigation.gnss.satellites";
	public static final String nav_headingMagnetic="navigation.headingMagnetic";
	public static final String nav_headingTrue="navigation.headingTrue";
	public static final String nav_log="navigation.log";
	public static final String nav_logTrip="navigation.logTrip";
	public static final String nav_magneticVariation="navigation.magneticVariation";
	public static final String nav_pitch="navigation.pitch";
	public static final String nav_position="navigation.position";
	public static final String nav_position_altitude="navigation.position.altitude";
	public static final String nav_position_latitude="navigation.position.latitude";
	public static final String nav_position_longitude="navigation.position.longitude";
	public static final String nav_rateOfTurn="navigation.rateOfTurn";
	public static final String nav_roll="navigation.roll";
	public static final String nav_speedOverGround="navigation.speedOverGround";
	public static final String nav_speedThroughWater="navigation.speedThroughWater";
	public static final String nav_state="navigation.state";
	public static final String nav_state_value="navigation.state.value";
	public static final String nav_yaw="navigation.yaw";
	public static final String propulsion="propulsion";
	public static final String propulsion_id="propulsion.UUID";
	public static final String propulsion_id_engineTemperature="propulsion.UUID.engineTemperature";
	public static final String propulsion_id_engineTemperatureAlarm="propulsion.UUID.engineTemperatureAlarm";
	public static final String propulsion_id_engineType="propulsion.UUID.engineType";
	public static final String propulsion_id_exhaustTemp="propulsion.UUID.exhaustTemp";
	public static final String propulsion_id_exhaustTempAlarm="propulsion.UUID.exhaustTempAlarm";
	public static final String propulsion_id_fuelUsageRate="propulsion.UUID.fuelUsageRate";
	public static final String propulsion_id_oilPressure="propulsion.UUID.oilPressure";
	public static final String propulsion_id_oilPressureAlarm="propulsion.UUID.oilPressureAlarm";
	public static final String propulsion_id_oilTemperature="propulsion.UUID.oilTemperature";
	public static final String propulsion_id_oilTemperatureAlarm="propulsion.UUID.oilTemperatureAlarm";
	public static final String propulsion_id_rpm="propulsion.UUID.rpm";
	public static final String propulsion_id_rpmAlarm="propulsion.UUID.rpmAlarm";
	public static final String propulsion_id_state="propulsion.UUID.state";
	public static final String propulsion_id_waterTemp="propulsion.UUID.waterTemp";
	public static final String propulsion_id_waterTempAlarm="propulsion.UUID.waterTempAlarm";
	public static final String sensors="sensors";
	public static final String sensors_id="sensors.UUID";
	public static final String sensors_id_fromBow="sensors.UUID.fromBow";
	public static final String sensors_id_fromCenter="sensors.UUID.fromCenter";
	public static final String sensors_id_name="sensors.UUID.name";
	public static final String sensors_id_sensorData="sensors.UUID.sensorData";
	public static final String sensors_id_sensorType="sensors.UUID.sensorType";
	public static final String steering="steering";
	public static final String steering_autopilot="steering.autopilot";
	public static final String steering_autopilot_alarmHeadingXte="steering.autopilot.alarmHeadingXte";
	public static final String steering_autopilot_backlash="steering.autopilot.backlash";
	public static final String steering_autopilot_deadZone="steering.autopilot.deadZone";
	public static final String steering_autopilot_gain="steering.autopilot.gain";
	public static final String steering_autopilot_headingSource="steering.autopilot.headingSource";
	public static final String steering_autopilot_maxDriveAmps="steering.autopilot.maxDriveAmps";
	public static final String steering_autopilot_maxDriveRate="steering.autopilot.maxDriveRate";
	public static final String steering_autopilot_mode="steering.autopilot.mode";
	public static final String steering_autopilot_portLock="steering.autopilot.portLock";
	public static final String steering_autopilot_starboardLock="steering.autopilot.starboardLock";
	public static final String steering_autopilot_state="steering.autopilot.state";
	public static final String steering_autopilot_targetHeadingMagnetic="steering.autopilot.targetHeadingMagnetic";
	public static final String steering_autopilot_targetHeadingNorth="steering.autopilot.targetHeadingNorth";
	public static final String steering_rudderAngle="steering.rudderAngle";
	public static final String steering_rudderAngleTarget="steering.rudderAngleTarget";
	public static final String tanks="tanks";
	public static final String tanks_id="tanks.UUID";
	public static final String tanks_id_alarmLevel="tanks.UUID.alarmLevel";
	public static final String tanks_id_capacity="tanks.UUID.capacity";
	public static final String tanks_id_level="tanks.UUID.level";
	public static final String tanks_id_tankType="tanks.UUID.tankType";
	
	
	
	
	
	
	
	public JsonConstants() {
		// TODO Auto-generated constructor stub
	}
	



}
