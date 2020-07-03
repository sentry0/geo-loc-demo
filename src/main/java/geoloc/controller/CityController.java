package geoloc.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vividsolutions.jts.io.ParseException;

import geoloc.domain.City;
import geoloc.service.LocationService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

/**
 * Copyright 2020-Present Philip J. Guinchard
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @author Philip J. Guinchard
 */

@RestController
public class CityController {
	private static final int MAX_REQUESTS = 100;

	private static final int REFILL_MINUTES = 5;

	private static final int TOKENS_CONSUMED_PER_REQUEST = 1;

	private Bucket bucket = null;

	@Autowired
	private LocationService locationService;

	public CityController() {
		Bandwidth limit = Bandwidth.classic(MAX_REQUESTS,
				Refill.greedy(MAX_REQUESTS, Duration.ofMinutes(REFILL_MINUTES)));

		this.bucket = Bucket4j.builder().addLimit(limit).build();
	}

	@GetMapping("/cities")
	@Async("asyncExecutor")
	public CompletableFuture<List<City>> getNearbyCities(@RequestParam("latitude") double latitude,
			@RequestParam("longitude") double longitude, @RequestParam("distance") int distance,
			@RequestParam("unit") String unit, HttpServletResponse response) throws ParseException {
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

		final double lat = latitude;
		final double lng = longitude;
		final int dist = distance;
		final String unt = unit;

		CompletableFuture<ConsumptionProbe> limitCheck = this.bucket.asAsync()
				.tryConsumeAndReturnRemaining(TOKENS_CONSUMED_PER_REQUEST);

		return limitCheck.thenCompose(probe -> {
			if (!probe.isConsumed()) {
				return CompletableFuture.completedFuture(null);
			} else {
				List<City> cities = new ArrayList<City>();

				try {
					cities = locationService.findNearbyCities(lat, lng, dist, unt);
				} catch (ParseException e) {
					// Do nothing
				}

				return CompletableFuture.completedFuture(cities);
			}
		}).whenComplete((result, exception) -> {
			if (result == null) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(REFILL_MINUTES * 60));
			}

			return;
		});
	}
}
