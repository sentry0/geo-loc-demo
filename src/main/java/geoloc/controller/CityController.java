package geoloc.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vividsolutions.jts.io.ParseException;

import geoloc.domain.City;
import geoloc.service.LocationService;

/**
* Copyright 2020-Present Philip J. Guinchard
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*        http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @author Philip J. Guinchard
*/

@RestController
public class CityController {	
	@Autowired
	private LocationService locationService;
	
	@GetMapping("/cities")
	@Async("asyncExecutor")
	public CompletableFuture<List<City>> getNearbyCities(
			@RequestParam("latitude") double latitude, 
			@RequestParam("longitude") double longitude,
			@RequestParam("distance") int distance,
			@RequestParam("unit") String unit
			) throws ParseException {
		if (latitude < LocationService.MIN_LATITUDE) {
			latitude = LocationService.MIN_LATITUDE;
		} else if (latitude > LocationService.MAX_LATITUDE) {
			latitude = LocationService.MAX_LATITUDE;
		}
		
		if (longitude < LocationService.MIN_LONGITUDE) {
			longitude = LocationService.MIN_LONGITUDE;
		} else if (longitude > LocationService.MAX_LONGITUDE) {
			longitude = LocationService.MAX_LONGITUDE;
		}
		
		if (distance > LocationService.MAX_DISTANCE) {
			distance = LocationService.MAX_DISTANCE;
		} else if (distance <= 0) {
			distance = LocationService.MIN_DISTANCE;
		}
		
		return CompletableFuture.completedFuture(locationService.findNearbyCities(latitude, longitude, distance, unit));
	}
}
