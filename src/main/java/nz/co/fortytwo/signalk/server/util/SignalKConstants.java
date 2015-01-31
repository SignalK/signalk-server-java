/*
 * 
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 * 
 * This file is part of the signalk-server-java project
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.fortytwo.signalk.server.util;

public class SignalKConstants {

	public static final String resources = "resources";
	public static final String resources_charts = "resources.charts";
	public static final String resources_charts_id = "resources.charts.*";
	public static final String resources_charts_id_chartFormat = "resources.charts.*.chartFormat";
	public static final String resources_charts_id_chartUrl = "resources.charts.*.chartUrl";
	public static final String resources_charts_id_description = "resources.charts.*.description";
	public static final String resources_charts_id_identifier = "resources.charts.*.identifier";
	public static final String resources_charts_id_name = "resources.charts.*.name";
	public static final String resources_charts_id_tilemapUrl = "resources.charts.*.tilemapUrl";
	public static final String resources_notes = "resources.notes";
	public static final String resources_notes_id = "resources.notes.*";
	public static final String resources_notes_id_description = "resources.notes.*.description";
	public static final String resources_notes_id_mimeType = "resources.notes.*.mimeType";
	public static final String resources_notes_id_region = "resources.notes.*.region";
	public static final String resources_notes_id_title = "resources.notes.*.title";
	public static final String resources_notes_id_url = "resources.notes.*.url";
	public static final String resources_regions = "resources.regions";
	public static final String resources_regions_id = "resources.regions.*";
	public static final String resources_regions_id_description = "resources.regions.*.description";
	public static final String resources_regions_id_name = "resources.regions.*.name";
	public static final String resources_regions_id_waypoints = "resources.regions.*.waypoints";
	public static final String resources_routes = "resources.routes";
	public static final String resources_routes_id = "resources.routes.*";
	public static final String resources_routes_id_description = "resources.routes.*.description";
	public static final String resources_routes_id_distance = "resources.routes.*.distance";
	public static final String resources_routes_id_name = "resources.routes.*.name";
	public static final String resources_routes_id_waypoints = "resources.routes.*.waypoints";
	public static final String resources_waypoints = "resources.waypoints";
	public static final String resources_waypoints_id = "resources.waypoints.*";
	public static final String resources_waypoints_id_description = "resources.waypoints.*.description";
	public static final String resources_waypoints_id_name = "resources.waypoints.*.name";
	public static final String resources_waypoints_id_position = "resources.waypoints.*.position";
	public static final String resources_waypoints_id_position_altitude = "resources.waypoints.*.position.altitude";
	public static final String resources_waypoints_id_position_latitude = "resources.waypoints.*.position.latitude";
	public static final String resources_waypoints_id_position_longitude = "resources.waypoints.*.position.longitude";
	public static final String resources_waypoints_id_type = "resources.waypoints.*.type";
	public static final String self = "self";
	public static final String version = "version";
	public static final String vessels = "vessels";
	public static final String vessels_id = "vessels.*";
	public static final String alarms = "alarms";
	public static final String alarms_anchorAlarmMethod = "alarms.anchorAlarmMethod";
	public static final String alarms_anchorAlarmState = "alarms.anchorAlarmState";
	public static final String alarms_autopilotAlarmMethod = "alarms.autopilotAlarmMethod";
	public static final String alarms_autopilotAlarmState = "alarms.autopilotAlarmState";
	public static final String alarms_engineAlarmMethod = "alarms.engineAlarmMethod";
	public static final String alarms_engineAlarmState = "alarms.engineAlarmState";
	public static final String alarms_fireAlarmMethod = "alarms.fireAlarmMethod";
	public static final String alarms_fireAlarmState = "alarms.fireAlarmState";
	public static final String alarms_gasAlarmMethod = "alarms.gasAlarmMethod";
	public static final String alarms_gasAlarmState = "alarms.gasAlarmState";
	public static final String alarms_gpsAlarmMethod = "alarms.gpsAlarmMethod";
	public static final String alarms_gpsAlarmState = "alarms.gpsAlarmState";
	public static final String alarms_maydayAlarmMethod = "alarms.maydayAlarmMethod";
	public static final String alarms_maydayAlarmState = "alarms.maydayAlarmState";
	public static final String alarms_panpanAlarmMethod = "alarms.panpanAlarmMethod";
	public static final String alarms_panpanAlarmState = "alarms.panpanAlarmState";
	public static final String alarms_powerAlarmMethod = "alarms.powerAlarmMethod";
	public static final String alarms_powerAlarmState = "alarms.powerAlarmState";
	public static final String alarms_silentInterval = "alarms.silentInterval";
	public static final String alarms_windAlarmMethod = "alarms.windAlarmMethod";
	public static final String alarms_windAlarmState = "alarms.windAlarmState";
	public static final String communication = "communication";
	public static final String communication_callsignDsc = "communication.callsignDsc";
	public static final String communication_callsignHf = "communication.callsignHf";
	public static final String communication_callsignVhf = "communication.callsignVhf";
	public static final String communication_cellPhone = "communication.cellPhone";
	public static final String communication_crewNames = "communication.crewNames";
	public static final String communication_email = "communication.email";
	public static final String communication_emailHf = "communication.emailHf";
	public static final String communication_satPhone = "communication.satPhone";
	public static final String communication_skipperName = "communication.skipperName";
	public static final String design = "design";
	public static final String design_airHeight = "design.airHeight";
	public static final String design_beam = "design.beam";
	public static final String design_canoeDraft = "design.canoeDraft";
	public static final String design_displacement = "design.displacement";
	public static final String design_draft = "design.draft";
	public static final String design_loa = "design.loa";
	public static final String design_loh = "design.loh";
	public static final String design_lwl = "design.lwl";
	public static final String design_name = "design.name";
	public static final String design_sailArea = "design.sailArea";
	public static final String env = "environment";
	public static final String env_airPressure = "environment.airPressure";
	public static final String env_airPressureChangeRateAlarm = "environment.airPressureChangeRateAlarm";
	public static final String env_airTemp = "environment.airTemp";
	public static final String env_depth = "environment.depth";
	public static final String env_depth_belowKeel = "environment.depth.belowKeel";
	public static final String env_depth_belowSurface = "environment.depth.belowSurface";
	public static final String env_depth_belowTransducer = "environment.depth.belowTransducer";
	public static final String env_depth_surfaceToTransducer = "environment.depth.surfaceToTransducer";
	public static final String env_depth_transducerToKeel = "environment.depth.transducerToKeel";
	public static final String env_humidity = "environment.humidity";
	public static final String env_salinity = "environment.salinity";
	public static final String env_tide = "environment.tide";
	public static final String env_tide_heightHigh = "environment.tide.heightHigh";
	public static final String env_tide_heightLow = "environment.tide.heightLow";
	public static final String env_tide_heightNow = "environment.tide.heightNow";
	public static final String env_tide_timeHigh = "environment.tide.timeHigh";
	public static final String env_tide_timeLow = "environment.tide.timeLow";
	public static final String env_waterTemp = "environment.waterTemp";
	public static final String env_wind = "environment.wind";
	public static final String env_wind_angleApparent = "environment.wind.angleApparent";
	public static final String env_wind_angleTrue = "environment.wind.angleTrue";
	public static final String env_wind_directionChangeAlarm = "environment.wind.directionChangeAlarm";
	public static final String env_wind_directionMagnetic = "environment.wind.directionMagnetic";
	public static final String env_wind_directionTrue = "environment.wind.directionTrue";
	public static final String env_wind_speedAlarm = "environment.wind.speedAlarm";
	public static final String env_wind_speedApparent = "environment.wind.speedApparent";
	public static final String env_wind_speedOverGround = "environment.wind.speedOverGround";
	public static final String env_wind_speedTrue = "environment.wind.speedTrue";
	public static final String mmsi = "mmsi";
	public static final String name = "name";
	public static final String nav = "navigation";
	public static final String nav_activeRoute = "navigation.activeRoute";
	public static final String nav_activeRoute_bearingActual = "navigation.activeRoute.bearingActual";
	public static final String nav_activeRoute_bearingDirect = "navigation.activeRoute.bearingDirect";
	public static final String nav_activeRoute_courseRequired = "navigation.activeRoute.courseRequired";
	public static final String nav_activeRoute_eta = "navigation.activeRoute.eta";
	public static final String nav_activeRoute_route = "navigation.activeRoute.route";
	public static final String nav_activeRoute_startTime = "navigation.activeRoute.startTime";
	public static final String nav_activeRoute_waypoint = "navigation.activeRoute.waypoint";
	public static final String nav_activeRoute_waypoint_last = "navigation.activeRoute.waypoint.last";
	public static final String nav_activeRoute_waypoint_lastTime = "navigation.activeRoute.waypoint.lastTime";
	public static final String nav_activeRoute_waypoint_next = "navigation.activeRoute.waypoint.next";
	public static final String nav_activeRoute_waypoint_nextEta = "navigation.activeRoute.waypoint.nextEta";
	public static final String nav_activeRoute_waypoint_xte = "navigation.activeRoute.waypoint.xte";
	public static final String nav_anchor = "navigation.anchor";
	public static final String nav_anchor_currentRadius = "navigation.anchor.currentRadius";
	public static final String nav_anchor_maxRadius = "navigation.anchor.maxRadius";
	public static final String nav_anchor_position = "navigation.anchor.position";
	public static final String nav_anchor_position_altitude = "navigation.anchor.position.altitude";
	public static final String nav_anchor_position_latitude = "navigation.anchor.position.latitude";
	public static final String nav_anchor_position_longitude = "navigation.anchor.position.longitude";
	public static final String nav_courseOverGroundMagnetic = "navigation.courseOverGroundMagnetic";
	public static final String nav_courseOverGroundTrue = "navigation.courseOverGroundTrue";
	public static final String nav_current = "navigation.current";
	public static final String nav_current_drift = "navigation.current.drift";
	public static final String nav_current_setMagnetic = "navigation.current.setMagnetic";
	public static final String nav_current_setTrue = "navigation.current.setTrue";
	public static final String nav_destination = "navigation.destination";
	public static final String nav_destination_altitude = "navigation.destination.altitude";
	public static final String nav_destination_eta = "navigation.destination.eta";
	public static final String nav_destination_latitude = "navigation.destination.latitude";
	public static final String nav_destination_longitude = "navigation.destination.longitude";
	public static final String nav_gnss = "navigation.gnss";
	public static final String nav_gnss_antennaAltitude = "navigation.gnss.antennaAltitude";
	public static final String nav_gnss_differentialAge = "navigation.gnss.differentialAge";
	public static final String nav_gnss_differentialReference = "navigation.gnss.differentialReference";
	public static final String nav_gnss_geoidalSeparation = "navigation.gnss.geoidalSeparation";
	public static final String nav_gnss_horizontalDilution = "navigation.gnss.horizontalDilution";
	public static final String nav_gnss_quality = "navigation.gnss.quality";
	public static final String nav_gnss_satellites = "navigation.gnss.satellites";
	public static final String nav_headingMagnetic = "navigation.headingMagnetic";
	public static final String nav_headingTrue = "navigation.headingTrue";
	public static final String nav_log = "navigation.log";
	public static final String nav_logTrip = "navigation.logTrip";
	public static final String nav_magneticVariation = "navigation.magneticVariation";
	public static final String nav_pitch = "navigation.pitch";
	public static final String nav_position = "navigation.position";
	public static final String nav_position_altitude = "navigation.position.altitude";
	public static final String nav_position_latitude = "navigation.position.latitude";
	public static final String nav_position_longitude = "navigation.position.longitude";
	public static final String nav_rateOfTurn = "navigation.rateOfTurn";
	public static final String nav_roll = "navigation.roll";
	public static final String nav_speedOverGround = "navigation.speedOverGround";
	public static final String nav_speedThroughWater = "navigation.speedThroughWater";
	public static final String nav_state = "navigation.state";
	public static final String nav_state_value = "navigation.state.value";
	public static final String nav_yaw = "navigation.yaw";
	public static final String propulsion = "propulsion";
	public static final String propulsion_id = "propulsion.*";
	public static final String propulsion_id_engineTemperature = "propulsion.*.engineTemperature";
	public static final String propulsion_id_engineTemperatureAlarm = "propulsion.*.engineTemperatureAlarm";
	public static final String propulsion_id_engineType = "propulsion.*.engineType";
	public static final String propulsion_id_exhaustTemp = "propulsion.*.exhaustTemp";
	public static final String propulsion_id_exhaustTempAlarm = "propulsion.*.exhaustTempAlarm";
	public static final String propulsion_id_fuelUsageRate = "propulsion.*.fuelUsageRate";
	public static final String propulsion_id_oilPressure = "propulsion.*.oilPressure";
	public static final String propulsion_id_oilPressureAlarm = "propulsion.*.oilPressureAlarm";
	public static final String propulsion_id_oilTemperature = "propulsion.*.oilTemperature";
	public static final String propulsion_id_oilTemperatureAlarm = "propulsion.*.oilTemperatureAlarm";
	public static final String propulsion_id_rpm = "propulsion.*.rpm";
	public static final String propulsion_id_rpmAlarm = "propulsion.*.rpmAlarm";
	public static final String propulsion_id_state = "propulsion.*.state";
	public static final String propulsion_id_waterTemp = "propulsion.*.waterTemp";
	public static final String propulsion_id_waterTempAlarm = "propulsion.*.waterTempAlarm";
	public static final String sensors = "sensors";
	public static final String sensors_id = "sensors.*";
	public static final String sensors_id_fromBow = "sensors.*.fromBow";
	public static final String sensors_id_fromCenter = "sensors.*.fromCenter";
	public static final String sensors_id_name = "sensors.*.name";
	public static final String sensors_id_sensorData = "sensors.*.sensorData";
	public static final String sensors_id_sensorType = "sensors.*.sensorType";
	public static final String steering = "steering";
	public static final String steering_autopilot = "steering.autopilot";
	public static final String steering_autopilot_alarmHeadingXte = "steering.autopilot.alarmHeadingXte";
	public static final String steering_autopilot_backlash = "steering.autopilot.backlash";
	public static final String steering_autopilot_deadZone = "steering.autopilot.deadZone";
	public static final String steering_autopilot_gain = "steering.autopilot.gain";
	public static final String steering_autopilot_headingSource = "steering.autopilot.headingSource";
	public static final String steering_autopilot_maxDriveAmps = "steering.autopilot.maxDriveAmps";
	public static final String steering_autopilot_maxDriveRate = "steering.autopilot.maxDriveRate";
	public static final String steering_autopilot_mode = "steering.autopilot.mode";
	public static final String steering_autopilot_portLock = "steering.autopilot.portLock";
	public static final String steering_autopilot_starboardLock = "steering.autopilot.starboardLock";
	public static final String steering_autopilot_state = "steering.autopilot.state";
	public static final String steering_autopilot_targetHeadingMagnetic = "steering.autopilot.targetHeadingMagnetic";
	public static final String steering_autopilot_targetHeadingNorth = "steering.autopilot.targetHeadingNorth";
	public static final String steering_rudderAngle = "steering.rudderAngle";
	public static final String steering_rudderAngleTarget = "steering.rudderAngleTarget";
	public static final String tanks = "tanks";
	public static final String tanks_id = "tanks.*";
	public static final String tanks_id_alarmLevel = "tanks.*.alarmLevel";
	public static final String tanks_id_capacity = "tanks.*.capacity";
	public static final String tanks_id_level = "tanks.*.level";
	public static final String tanks_id_tankType = "tanks.*.tankType";

	public SignalKConstants() {
		super();
	}

}