 /* Rheem EcoNet Connect
 *  
 *  Copyright 2017 Justin Huff
 *
 *  GitHub Link
 *  https://raw.githubusercontent.com/bspranger/Hubitat_RheemEcoNet/master/App/rheem-econet-connect.groovy
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
 *  Last Updated : 1/5/19 by Brian Spranger
 *
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
 *
 */
definition(
    name: "Rheem EcoNet (Connect)",
    namespace: "bspranger",
    author: "Brian Spranger",
    description: "Connect to Rheem EcoNet",
    category: "SmartThings Labs",
    iconUrl: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@1x.png",
    iconX2Url: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@2x.png",
    iconX3Url: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@3x.png")


preferences {
	page(name: "prefLogIn", title: "Rheem EcoNet")    
	page(name: "prefListDevice", title: "Rheem EcoNet")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
	return dynamicPage(name: "prefLogIn", title: "Connect to Rheem EcoNet", nextPage:"prefListDevice", uninstall:showUninstall, install: false) {
		section("Login Credentials"){
			input("username", "email", title: "Username", description: "Rheem EcoNet Email")
			input("password", "password", title: "Password", description: "Rheem EcoNet password (case sensitive)")
		} 
		section("Advanced Options"){
			input "isDebugEnabled", "bool", title: "Enable Debugging?", defaultValue: false
		}
	}
}

def prefListDevice() {	
  login()
	if (login()) {
		def waterHeaterList = getWaterHeaterList()
		if (waterHeaterList) {
			return dynamicPage(name: "prefListDevice",  title: "Devices", install:true, uninstall:true) {
				section("Select which water heater to use"){
					input(name: "waterheater", type: "enum", required:false, multiple:true, options:[waterHeaterList])
				}
			}
		} else {
			return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
				section(""){ paragraph "Could not find any devices"  }
			}
		}
	} else {
		return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
			section(""){ paragraph "The username or password you entered is incorrect. Try again. " }
		}  
	}
}


/* Initialization */
def installed() { 
	initialize() 
}
def updated() { 
	unsubscribe()
	initialize() 
}
def uninstalled() {
	unschedule()
    unsubscribe()
	getAllChildDevices().each { deleteChildDevice(it.deviceNetworkId) }
}

def initialize() {

	// Create selected devices
	def waterHeaterList = getWaterHeaterList()
    def selectedDevices = [] + getSelectedDevices("waterheater")
    selectedDevices.each {
    	def dev = getChildDevice(it)
        def name  = waterHeaterList[it]
        if (dev == null) {
	        try {
    			addChildDevice("bspranger", "Rheem Econet Water Heater", it, null, ["name": "Rheem Econet: " + name])
    	    } catch (e)	{
				logDebug "addChildDevice Error: $e"
          	}
        }
    }
    
	//Refresh devices
	refresh()
	runEvery1Minute(refresh)
}

def getSelectedDevices( settingsName ) {
	def selectedDevices = []
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName)))
	return selectedDevices
}


/* Data Management */
// Listing all the water heaters you have in Rheem EcoNet
private getWaterHeaterList() { 	 
	def deviceList = [:]
	apiGet("/locations", [] ) { response ->
    	if (response.status == 200) {
          	response.data.equipment[0].each { 
            	if (it.type.equals("Water Heater")) {
                	deviceList["" + it.id]= it.name
                }
            }
        }
    }
    return deviceList
}

// Refresh data
def refresh() {
	if (!login()) {
    	return
    }
    
	logDebug "Refreshing data..."

	// get all the children and send updates
	getChildDevices().each {
    	def id = it.deviceNetworkId
    	apiGet("/equipment/$id", [] ) { response ->
    		if (response.status == 200) {
            	logDebug "Got data: $response.data"
            	it.updateDeviceData(response.data)
            }
        }
    }
}

def setDeviceSetPoint(childDevice, setpoint) { 
	logDebug "setDeviceSetPoint: $childDevice.deviceNetworkId $setpoint" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId", [
        	body: [
                setPoint: setpoint,
            ]
        ])
    }

}
def setDeviceEnabled(childDevice, enabled) {
	logDebug "setDeviceEnabled: $childDevice.deviceNetworkId $enabled" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId", [
        	body: [
                isEnabled: enabled,
            ]
        ])
    }
}
def setDeviceMode(childDevice, mode) {
	logDebug "setDeviceEnabled: $childDevice.deviceNetworkId $enabled" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId/modes", [
        	body: [
                mode: mode,
            ]
        ])
    }
}
def setDeviceOnVacation(childDevice, OnVacation) {
	logDebug "setDeviceOnVacation: $childDevice.deviceNetworkId $OnVacation" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId", [
        	body: [
                isOnVacation: OnVacation,
            ]
        ])
    }
}

private login() {
	def apiParams = [
    	uri: getApiURL(),
        path: "/auth/token",
        headers: ["Authorization": "Basic Y29tLnJoZWVtLmVjb25ldF9hcGk6c3RhYmxla2VybmVs"],
        requestContentType: "application/x-www-form-urlencoded",
        body: [
        	username: settings.username,
        	password: settings.password,
        	"grant_type": "password"
        ],
    ]
    if (state.session?.expiration < now()) {
    	try {
			httpPost(apiParams) { response -> 
            	if (response.status == 200) {
                	logDebug "Login good!"
                	state.session = [ 
                    	accessToken: response.data.access_token,
                    	refreshToken: response.data.refresh_token,
                    	expiration: now() + 150000
                	]
                	return true
            	} else {
                	return false
            	} 	
        	}
		}	catch (e)	{
			logDebug "API Error: $e"
        	return false
		}
	} else { 
    	// TODO: do a refresh 
		return true
	}
}

/* API Management */
// HTTP GET call
private apiGet(apiPath, apiParams = [], callback = {}) {	
	// set up parameters
	apiParams = [ 
		uri: getApiURL(),
		path: apiPath,
        headers: ["Authorization": getApiAuth()],
        requestContentType: "application/json",
	] + apiParams
	logDebug "GET: $apiParams"
	try {
		httpGet(apiParams) { response -> 
        	callback(response)
        }
	}	catch (e)	{
		logDebug "API Error: $e"
	}
}

// HTTP PUT call
private apiPut(apiPath, apiParams = [], callback = {}) {	
	// set up parameters
	apiParams = [ 
		uri: getApiURL(),
		path: apiPath,
        headers: ["Authorization": getApiAuth()],
        requestContentType: "application/json",
	] + apiParams
	logDebug "apiPut: $apiParams"
	try {
		httpPut(apiParams) { response -> 
        	callback(response)
        }
	}	catch (e)	{
		logDebug "API Error: $e"
	}
}

private getApiURL() { 
	return "https://econet-api.rheemcert.com"
}
    
private getApiAuth() {
	return "Bearer " + state.session?.accessToken
}

private logDebug(msg) {
	if (isDebugEnabled != false) {
		log.debug "$msg"
	}
}
