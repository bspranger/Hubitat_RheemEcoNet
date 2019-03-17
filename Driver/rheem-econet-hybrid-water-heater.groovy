/**
 *  Rheem Econet Hybrid Water Heater
 *
 *  Copyright 2017 Justin Huff
 *
 *  Github Link
 *  https://raw.githubusercontent.com/bspranger/Hubitat_RheemEcoNet/master/Driver/rheem-econet-hybrid-water-heater.groovy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 01-05-2019 by Brian Spranger
 *
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
 */
metadata {
	definition (name: "Rheem Econet Water Heater", namespace: "bspranger", author: "Brian Spranger") {
        capability "Thermostat"
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "ThermostatHeatingSetpoint"
        capability "TemperatureMeasurement"
		capability "ThermostatSetpoint"
		
		command "heatLevelUp"
		command "heatLevelDown"
        command "RequestEnergySave"
        command "RequestHighDemand"
        command "RequestOff"
        command "RequestHeatPumpOnly"
        command "RequestElectricOnly"
		command "updateDeviceData", ["string"]
	}
	
	preferences {
		input "isDebugEnabled", "bool", title: "Enable Debugging?", defaultValue: false
	}
}

def parse(String description) { }

def refresh() {
	logDebug "refresh"
	parent.refresh()
}

def setCoolingSetpoint(Number setPoint) {
    /* does not support cooling, but the thermostat capability does */
}

def setHeatingSetpoint(Number setPoint) {
	/*heatingSetPoint = (heatingSetPoint < deviceData.minTemp)? deviceData.minTemp : heatingSetPoint
	heatingSetPoint = (heatingSetPoint > deviceData.maxTemp)? deviceData.maxTemp : heatingSetPoint
    */
   	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
	sendEvent(name: "thermostatSetpoint", value: setPoint, unit: "F")
	 
	parent.setDeviceSetPoint(this.device, setPoint)
    refresh()
}

def heatLevelUp() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint + 1
	setHeatingSetpoint(setPoint)
}	

def heatLevelDown() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint - 1
    setHeatingSetpoint(setPoint)
}

def auto()          { RequestEnergySave() }
def cool()          {  }
def emergencyHeat() { RequestHighDemand() }
def heat()          { RequestHeatPumpOnly() }
def off()           { RequestOff() }


def RequestEnergySave(){
	logDebug("Requesting Energy Saver Mode")
	parent.setDeviceMode(this.device, "Energy Saver")
    parent.refresh()
}

def RequestHighDemand(){
	logDebug("Requesting High Demand Mode")
	parent.setDeviceMode(this.device, "High Demand")
    parent.refresh()
}
def RequestOff(){
	logDebug("Requesting Off")
	parent.setDeviceMode(this.device, "Off")
    parent.refresh()
}
def RequestHeatPumpOnly(){
	logDebug("Requesting Heat Pump Only")
	parent.setDeviceMode(this.device, "Heat Pump Only")
    parent.refresh()
}
def RequestElectricOnly(){
	logDebug("Requesting Electric Only")
	parent.setDeviceMode(this.device, "Electric-Only")
    parent.refresh()
}

/* the Thermostat Capability supports Fan modes, but the water heater does not */
def fanAuto()      {  }
def fanCirculate() {  }
def fanOn()        {  }
def fanOff()       {  }

def updateDeviceData(data) {
    sendEvent(name: "heatingSetpoint", value: data.setPoint, unit: "F")
    sendEvent(name: "thermostatOperatingState", value: data.inUse ? "heating" : "idle")
    sendEvent(name: "thermostatMode", value: data.mode)
    sendEvent(name: "lowerTemp", value: data.lowerTemp as Integer)
    sendEvent(name: "ambientTemp", value: data.ambientTemp as Integer)
    sendEvent(name: "temperature", value: data.upperTemp as Integer)
}

private logDebug(msg) {
	if (isDebugEnabled != false) {
		log.debug "$msg"
	}
}
