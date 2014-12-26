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
	public static final String SOURCE = "source";
	public static final String DEVICE = "device";
	public static final String TIMESTAMP = "timestamp";
	public static final String SRC = "src";
	public static final String PGN = "pgn";
	public static final String VALUE = "value";
	public static final String VALUES = "values";
	public static final String PATH = "path";
	
	public static final String name = "name";
	public static final String mmsi = "mmsi";
	public static final String source = "source";
	public static final String timezone = "timezone";
	
	public static final String alarms = "alarms";
	public static final String alarms_gpsAlarmState = "alarms.gpsAlarmState";
	public static final String alarms_windAlarmState = "alarms.windAlarmState";
	public static final String alarms_anchorAlarmMethod = "alarms.anchorAlarmMethod";
	public static final String alarms_anchorAlarmState = "alarms.anchorAlarmState";
	public static final String alarms_windAlarmMethod = "alarms.windAlarmMethod";
	public static final String alarms_gasAlarmState = "alarms.gasAlarmState";
	public static final String alarms_gasAlarmMethod = "alarms.gasAlarmMethod";
	public static final String alarms_autopilotAlarmMethod = "alarms.autopilotAlarmMethod";
	public static final String alarms_autopilotAlarmState = "alarms.autopilotAlarmState";
	public static final String alarms_panpanAlarmState = "alarms.panpanAlarmState";
	public static final String alarms_gpsAlarmMethod = "alarms.gpsAlarmMethod";
	public static final String alarms_maydayAlarmMethod = "alarms.maydayAlarmMethod";
	public static final String alarms_powerAlarmState = "alarms.powerAlarmState";
	public static final String alarms_engineAlarmState = "alarms.engineAlarmState";
	public static final String alarms_silentInterval = "alarms.silentInterval";
	public static final String alarms_panpanAlarmMethod = "alarms.panpanAlarmMethod";
	public static final String alarms_engineAlarmMethod = "alarms.engineAlarmMethod";
	public static final String alarms_fireAlarmState = "alarms.fireAlarmState";
	public static final String alarms_fireAlarmMethod = "alarms.fireAlarmMethod";
	public static final String alarms_powerAlarmMethod = "alarms.powerAlarmMethod";
	public static final String alarms_maydayAlarmState = "alarms.maydayAlarmState";
	public static final String alarms_required = "alarms.required";
	public static final String communication = "communication";
	public static final String communication_callsignHf = "communication.callsignHf";
	public static final String communication_callsignVhf = "communication.callsignVhf";
	public static final String communication_satPhone = "communication.satPhone";
	public static final String communication_email = "communication.email";
	public static final String communication_cellPhone = "communication.cellPhone";
	public static final String communication_emailHf = "communication.emailHf";
	public static final String communication_skipperName = "communication.skipperName";
	
	public static final String communication_crewNames = "communication.crewNames";
	public static final String communication_callsignDsc = "communication.callsignDsc";
	public static final String environment = "environment";
	public static final String env_wind = "environment.wind";
	public static final String env_wind_directionChangeAlarm = "environment.wind.directionChangeAlarm";
	public static final String env_wind_directionApparent = "environment.wind.directionApparent";
	public static final String env_wind_directionTrue = "environment.wind.directionTrue";
	public static final String env_wind_speedAlarm = "environment.wind.speedAlarm";
	public static final String env_wind_speedTrue = "environment.wind.speedTrue";
	public static final String env_wind_speedApparent = "environment.wind.speedApparent";
	public static final String env_waterTemp = "environment.waterTemp";
	public static final String env_currentSpeed = "environment.currentSpeed";
	public static final String env_currentDirection = "environment.currentDirection";
	public static final String env_salinity = "environment.salinity";
	public static final String env_airTemp = "environment.airTemp";
	public static final String env_depth = "environment.depth";
	public static final String env_depth_belowTransducer = "environment.depth.belowTransducer";
	public static final String env_depth_belowSurface = "environment.depth.belowSurface";
	public static final String env_depth_surfaceToTransducer = "environment.depth.surfaceToTransducer";
	public static final String env_depth_belowKeel = "environment.depth.belowKeel";
	public static final String env_depth_transducerToKeel = "environment.depth.transducerToKeel";
	public static final String env_humidity = "environment.humidity";
	public static final String env_tide = "environment.tide";
	public static final String env_tide_heightNow = "environment.tide.heightNow";
	public static final String env_tide_heightHigh = "environment.tide.heightHigh";
	public static final String env_tide_heightLow = "environment.tide.heightLow";
	public static final String env_tide_timeLow = "environment.tide.timeLow";
	public static final String env_tide_timeHigh = "environment.tide.timeHigh";
	public static final String env_airPressure = "environment.airPressure";
	public static final String env_airPressureChangeRateAlarm = "environment.airPressureChangeRateAlarm";
	public static final String navigation = "navigation";
	public static final String nav_position = "navigation.position";
	public static final String nav_position_altitude = "navigation.position.altitude";
	public static final String nav_position_longitude = "navigation.position.longitude";
	public static final String nav_position_latitude = "navigation.position.latitude";
	public static final String nav_set = "navigation.set";
	public static final String nav_state = "navigation.state";
	public static final String nav_state_value = "navigation.state.value";
	public static final String nav_courseOverGroundTrue = "navigation.courseOverGroundTrue";
	public static final String nav_magneticVariation = "navigation.magneticVariation";
	public static final String nav_headingMagnetic = "navigation.headingMagnetic";
	public static final String nav_drift = "navigation.drift";
	public static final String nav_anchor = "navigation.anchor";
	public static final String nav_anchor_position = "navigation.anchor.position";
	public static final String nav_anchor_position_altitude = "navigation.anchor.position.altitude";
	public static final String nav_anchor_position_longitude = "navigation.anchor.position.longitude";
	public static final String nav_anchor_position_latitude = "navigation.anchor.position.latitude";
	public static final String nav_anchor_maxRadius = "navigation.anchor.maxRadius";
	public static final String nav_anchor_currentRadius = "navigation.anchor.currentRadius";
	public static final String nav_speedThroughWater = "navigation.speedThroughWater";
	public static final String nav_speedOverGround = "navigation.speedOverGround";
	public static final String nav_destination = "navigation.destination";
	public static final String nav_destination_eta = "navigation.destination.eta";
	public static final String nav_destination_altitude = "navigation.destination.altitude";
	public static final String nav_destination_longitude = "navigation.destination.longitude";
	public static final String nav_destination_latitude = "navigation.destination.latitude";
	public static final String nav_currentRoute = "navigation.currentRoute";
	public static final String nav_currentRoute_startTime = "navigation.currentRoute.startTime";
	public static final String nav_currentRoute_eta = "navigation.currentRoute.eta";
	public static final String nav_currentRoute_bearingActual = "navigation.currentRoute.bearingActual";
	public static final String nav_currentRoute_bearingDirect = "navigation.currentRoute.bearingDirect";
	public static final String nav_currentRoute_courseRequired = "navigation.currentRoute.courseRequired";
	public static final String nav_currentRoute_route = "navigation.currentRoute.route";
	public static final String nav_currentRoute_waypoint = "navigation.currentRoute.waypoint";
	public static final String nav_currentRoute_waypoint_nextEta = "navigation.currentRoute.waypoint.nextEta";
	public static final String nav_currentRoute_waypoint_last = "navigation.currentRoute.waypoint.last";
	public static final String nav_currentRoute_waypoint_next = "navigation.currentRoute.waypoint.next";
	public static final String nav_currentRoute_waypoint_lastTime = "navigation.currentRoute.waypoint.lastTime";
	public static final String nav_currentRoute_waypoint_xte = "navigation.currentRoute.waypoint.xte";
	public static final String nav_roll = "navigation.roll";
	public static final String nav_gnss = "navigation.gnss";
	public static final String nav_gnss_antennaAltitude = "navigation.gnss.antennaAltitude";
	public static final String nav_gnss_horizontalDilution = "navigation.gnss.horizontalDilution";
	public static final String nav_gnss_geoidalSeparation = "navigation.gnss.geoidalSeparation";
	public static final String nav_gnss_differentialAge = "navigation.gnss.differentialAge";
	public static final String nav_gnss_differentialReference = "navigation.gnss.differentialReference";
	public static final String nav_gnss_quality = "navigation.gnss.quality";
	public static final String nav_gnss_satellites = "navigation.gnss.satellites";
	public static final String nav_courseOverGroundMagnetic = "navigation.courseOverGroundMagnetic";
	public static final String nav_rateOfTurn = "navigation.rateOfTurn";
	public static final String nav_pitch = "navigation.pitch";
	public static final String nav_headingTrue = "navigation.headingTrue";
	
	public static final String propulsion = "propulsion";
	public static final String propulsion_exhaustTemp = "propulsion.exhaustTemp";
	public static final String propulsion_engineTemperatureAlarm = "propulsion.engineTemperatureAlarm";
	public static final String propulsion_waterTemp = "propulsion.waterTemp";
	public static final String propulsion_engineTemperature = "propulsion.engineTemperature";
	public static final String propulsion_oilTemperatureAlarm = "propulsion.oilTemperatureAlarm";
	public static final String propulsion_state = "propulsion.state";
	public static final String propulsion_fuelUsageRate = "propulsion.fuelUsageRate";
	public static final String propulsion_exhaustTempAlarm = "propulsion.exhaustTempAlarm";
	public static final String propulsion_oilPressure = "propulsion.oilPressure";
	public static final String propulsion_engineType = "propulsion.engineType";
	public static final String propulsion_oilPressureAlarm = "propulsion.oilPressureAlarm";
	public static final String propulsion_waterTempAlarm = "propulsion.waterTempAlarm";
	public static final String propulsion_rpm = "propulsion.rpm";
	public static final String propulsion_rpmAlarm = "propulsion.rpmAlarm";
	public static final String propulsion_oilTemperature = "propulsion.oilTemperature";
	public static final String resources = "resources";
	
	public static final String resources_charts = "resources.charts";
	public static final String resources_charts_tilemapUrl = "resources.charts.tilemapUrl";
	public static final String resources_charts_chartUrl = "resources.charts.chartUrl";
	public static final String resources_charts_chartFormat = "resources.charts.chartFormat";
	public static final String resources_charts_name = "resources.charts.name";
	public static final String resources_charts_identifier = "resources.charts.identifier";

	public static final String resources_routes = "resources.routes";
	public static final String resources_routes_distance = "resources.routes.distance";
	public static final String resources_routes_name = "resources.routes.name";
	public static final String resources_routes_waypoints = "resources.routes.waypoints";
	public static final String resources_routes_waypoints_items = "resources.routes.waypoints.items";

	public static final String resources_waypoints = "resources.waypoints";
	public static final String resources_waypoints_position = "resources.waypoints.position";
	public static final String resources_waypoints_position_altitude = "resources.waypoints.position.altitude";
	public static final String resources_waypoints_position_longitude = "resources.waypoints.position.longitude";
	public static final String resources_waypoints_position_latitude = "resources.waypoints.position.latitude";
	public static final String resources_waypoints_name = "resources.waypoints.name";

	public static final String resources_notes = "resources.notes";
	public static final String resources_notes_region = "resources.notes.region";
	public static final String resources_notes_mimeType = "resources.notes.mimeType";
	public static final String resources_notes_url = "resources.notes.url";
	
	public static final String resources_regions = "resources.regions";
	public static final String resources_regions_name = "resources.regions.name";
	public static final String resources_regions_waypoints = "resources.regions.waypoints";
	public static final String resources_regions_waypoints_items = "resources.regions.waypoints.items";
	public static final String sensors = "sensors";
	public static final String sensors_fromBow = "sensors.fromBow";
	public static final String sensors_fromCenter = "sensors.fromCenter";
	public static final String sensors_sensorType = "sensors.sensorType";
	public static final String sensors_name = "sensors.name";
	public static final String sensors_sensorData = "sensors.sensorData";
	public static final String steering = "steering";
	public static final String steering_rudderAngle = "steering.rudderAngle";
	public static final String steering_rudderAngleTarget = "steering.rudderAngleTarget";
	public static final String steering_autopilot = "steering.autopilot";
	public static final String steering_autopilot_maxDriveAmps = "steering.autopilot.maxDriveAmps";
	public static final String steering_autopilot_maxDriveRate = "steering.autopilot.maxDriveRate";
	public static final String steering_autopilot_starboardLock = "steering.autopilot.starboardLock";
	public static final String steering_autopilot_backlash = "steering.autopilot.backlash";
	public static final String steering_autopilot_state = "steering.autopilot.state";
	public static final String steering_autopilot_mode = "steering.autopilot.mode";
	public static final String steering_autopilot_gain = "steering.autopilot.gain";
	public static final String steering_autopilot_portLock = "steering.autopilot.portLock";
	public static final String steering_autopilot_deadZone = "steering.autopilot.deadZone";
	public static final String steering_autopilot_targetHeadingMagnetic = "steering.autopilot.targetHeadingMagnetic";
	public static final String steering_autopilot_headingSource = "steering.autopilot.headingSource";
	public static final String steering_autopilot_alarmHeadingXte = "steering.autopilot.alarmHeadingXte";
	public static final String steering_autopilot_targetHeadingNorth = "steering.autopilot.targetHeadingNorth";
	
	public static final String tanks = "tanks";
	public static final String tanks_level = "tanks.level";
	public static final String tanks_alarmLevel = "tanks.alarmLevel";
	public static final String tanks_capacity = "tanks.capacity";
	public static final String tanks_tankType = "tanks.tankType";
	public static final String SIGNALK_AUTH = "/signalk/auth/";
	public static final String SIGNALK_API = "/signalk/api/";
	public static final String SIGNALK_SUBSCRIBE = "/signalk/subscribe/";
	public static final String SIGNALK_WS = "/signalk/stream";
	
	
	
	
	public JsonConstants() {
		// TODO Auto-generated constructor stub
	}
	



}
