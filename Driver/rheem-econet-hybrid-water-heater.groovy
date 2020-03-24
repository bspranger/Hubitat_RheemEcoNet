
Rheem Econet Water Heater
chat_bubble_outline
more_vert

Dashboards
Devices
Apps
Settings
Advanced
codeApps Code
codeDrivers Code
System Events
Logs
Rheem Econet Water HeaterImport   HelpDeleteSave
O
1
/**
2
 *  Rheem Econet Hybrid Water Heater
3
 *
4
 *  Copyright 2017 Justin Huff
5
 *
6
 *  Github Link
7
 *  https://raw.githubusercontent.com/bspranger/Hubitat_RheemEcoNet/master/Driver/rheem-econet-hybrid-water-heater.groovy
8
 *
9
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
10
 *  in compliance with the License. You may obtain a copy of the License at:
11
 *
12
 *      http://www.apache.org/licenses/LICENSE-2.0
13
 *
14
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
15
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
16
 *  for the specific language governing permissions and limitations under the License.
17
 *
18
 *  Last Updated : 01-05-2019 by Brian Spranger
19
 *
20
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
21
 */
22
metadata {
23
    definition (name: "Rheem Econet Water Heater", namespace: "bspranger", author: "Brian Spranger") {
24
        capability "Thermostat"
25
        capability "ThermostatSetpoint"
26
        capability "Polling"
27
        capability "Refresh"
28
        capability "Sensor"
29
        capability "TemperatureMeasurement"
30
        capability "Actuator"
31
        capability "ThermostatHeatingSetpoint"
32
        capability "ThermostatCoolingSetpoint"
33
        
34
        
35
        
36
        command "heatLevelUp"
37
        command "heatLevelDown"
38
        command "coolLevelUp"
39
        command "coolLevelDown"
40
        command "RequestEnergySave"
41
        command "RequestHighDemand"
42
        command "RequestOff"
43
        command "RequestHeatPumpOnly"
44
        command "RequestElectricOnly"
45
        command "away"
46
        command "present"
47
        command "updateDeviceData", ["string"]
48
    }
49
    
50
    preferences {
51
        input "isDebugEnabled", "bool", title: "Enable Debugging?", defaultValue: false
52
    }
53
}
54
​
55
def parse(String description) { }
56
​
57
def poll() {refresh()}
58
​
59
def refresh() {
60
    logDebug "refresh"
61
    def SupportedModes = ["auto", "off", "Energy Saver", "High Demand", "Heat Pump Only", "Electric-Only"]
Location: HubitatHome
Terms of Service
Documentation
Community
Support
Copyright 2019 Hubitat, Inc.
