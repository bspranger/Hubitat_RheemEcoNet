/**
 *  Rheem Econet Water Heater
 *
 *  Copyright 2017 Justin Huff
 *
 *  Github Link
 *  https://github.com/bspranger/Hubitat_RheemEcoNet/blob/master/Driver/rheem-econet-hybrid-water-heater.groovy
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
		capability "Thermostat Heating Setpoint"
        capability "Temperature Measurement"
		
		command "heatLevelUp"
		command "heatLevelDown"
        command "togglevacation"
        command "RequestEnergySave"
        command "RequestHighDemand"
        command "RequestOff"
        command "RequestHeatPumpOnly"
        command "RequestElectricOnly"
		command "updateDeviceData", ["string"]
	}

	simulator { }

	tiles(scale: 2)  {
    multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
    tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
        attributeState("temp", label:'${currentValue}', unit:"dF", defaultState: true)
    }
    tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
        attributeState("VALUE_UP", action: "heatLevelUp")
        attributeState("VALUE_DOWN", action: "heatLevelDown")
    }
    tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
        attributeState("idle", label:'${name}', backgroundColor:"#228B22")
        attributeState("heating", label:'${name}', backgroundColor:"#FF0000")
    }
    tileAttribute("device.thermostatMode", key: "SECONDARY_CONTROL") {
        attributeState("Energy Saver", label:'${name}', icon:"none")
        attributeState("Heat Pump Only", label:'${name}', icon:"none")
        attributeState("High Demand", label:'${name}', icon:"none")
        attributeState("Off", label:'${name}', icon:"none")
        attributeState("Electric-Only", label:'${name}', icon:"none")
    }
    tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
        attributeState("heatingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)
    }
    }
        valueTile("uppertemperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state("temp", label:'${currentValue}°',
				backgroundColors:[
					[value: 90,  color: "#f49b88"],
					[value: 100, color: "#f28770"],
					[value: 110, color: "#f07358"],
					[value: 120, color: "#ee5f40"],
					[value: 130, color: "#ec4b28"],
					[value: 140, color: "#ea3811"]					
				]
			)
		}
        
        standardTile("ambientTemp","device.ambientTemp", decoration: "flat",width: 3, height: 2){
                state("temperature", label:'Ambient\n${currentValue}°',
                backgroundColors:[
                        // Fahrenheit color set
                        [value: 0, color: "#153591"],
                        [value: 5, color: "#1e9cbb"],
                        [value: 10, color: "#90d2a7"],
                        [value: 15, color: "#44b621"],
                        [value: 20, color: "#f1d801"],
                        [value: 25, color: "#d04e00"],
                        [value: 30, color: "#bc2323"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
						// Celsius color set (to switch, delete the 13 lines above anmd remove the two slashes at the beginning of the line below)
                        //[value: 0, color: "#153591"], [value: 7, color: "#1e9cbb"], [value: 15, color: "#90d2a7"], [value: 23, color: "#44b621"], [value: 28, color: "#f1d801"], [value: 35, color: "#d04e00"], [value: 37, color: "#bc2323"]
                    ]
                )
        }
     standardTile("lowerTemp", "device.lowerTemp", decoration: "flat", width: 3, height: 2){
                state("temperature", label:'Low\n${currentValue}°',
                backgroundColors:[
					[value: 90,  color: "#f49b88"],
					[value: 100, color: "#f28770"],
					[value: 110, color: "#f07358"],
					[value: 120, color: "#ee5f40"],
					[value: 130, color: "#ec4b28"],
					[value: 140, color: "#ea3811"]					
				]
                )
        }
        standardTile("HeatPumpOnly", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"RequestHeatPumpOnly", label: 'Rqst:Heat Pump')
		}
        standardTile("HighDemand", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"RequestHighDemand", label: 'Rqst:High Dmd')
		}
        standardTile("Off", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"RequestOff", label: 'Rqst:Off')
		}
        standardTile("EnergySaver", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"RequestEnergySave", label: 'Rqst:Enrgy Sav')
		}
        standardTile("ElectricOnly", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"RequestElectricOnly", label: 'Rqst:Elec Only')
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
        {
            state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }

              
		main "uppertemperature"
		details(["thermostatFull","lowerTemp","ambientTemp","HeatPumpOnly","HighDemand","Off", "EnergySaver", "ElectricOnly", "refresh"])
	}
}

def parse(String description) { }

def refresh() {
	log.debug "refresh"
	parent.refresh()
}

def setHeatingSetpoint(Number setPoint) {
	/*heatingSetPoint = (heatingSetPoint < deviceData.minTemp)? deviceData.minTemp : heatingSetPoint
	heatingSetPoint = (heatingSetPoint > deviceData.maxTemp)? deviceData.maxTemp : heatingSetPoint
    */
   	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
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

def RequestEnergySave(){
	parent.setDeviceMode(this.device, "Energy Saver")
    parent.refresh()
}

def RequestHighDemand(){
	parent.setDeviceMode(this.device, "High Demand")
    parent.refresh()
}
def RequestOff(){
	parent.setDeviceMode(this.device, "Off")
    parent.refresh()
}
def RequestHeatPumpOnly(){
	parent.setDeviceMode(this.device, "Heat Pump Only")
    parent.refresh()
}
def RequestElectricOnly(){
	parent.setDeviceMode(this.device, "Electric-Only")
    parent.refresh()
}

def updateDeviceData(data) {
	sendEvent(name: "heatingSetpoint", value: data.setPoint, unit: "F")
    sendEvent(name: "thermostatOperatingState", value: data.inUse ? "heating" : "idle")
    sendEvent(name: "thermostatMode", value: data.mode)
    sendEvent(name: "lowerTemp", value: data.lowerTemp as Integer)
    sendEvent(name: "ambientTemp", value: data.ambientTemp as Integer)
    sendEvent(name: "temperature", value: data.upperTemp as Integer)
}