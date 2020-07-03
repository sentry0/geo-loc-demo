package geoloc.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import geoloc.dao.CityRepository;
import geoloc.domain.City;

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

@Service
public class LocationService {
	public static final int SRID = 4326;
			
	public static final int MIN_DISTANCE = 25;
	
	public static final int MAX_DISTANCE = 1000;
	
	public static final double MAX_LATITUDE = 90.0D;
	
	public static final double MIN_LATITUDE = -90.0D;
	
	public static final double MAX_LONGITUDE = 180.0D;
	
	public static final double MIN_LONGITUDE = -180.0D;
	
	private static final String UNIT_MILE = "m";
	
	private static double KM_PER_POINT_OF_LONGITUDE = 111.0;
	
	private static double MILES_PER_POINT_OF_LONGITUDE = 69.0;
	
	private static final double PRECISION = 1000d;
	
	@Autowired
	private CityRepository cityRepo;
	
	@Transactional
	public List<City> findNearbyCities(double latitude, double longitude, int distance, String unit) throws ParseException {		
		WKTReader wktReader = new WKTReader();
		Geometry centerPoint = wktReader.read(toWkt(latitude, longitude));
		centerPoint.setSRID(SRID);
		
		return cityRepo.findAdjacentCity(centerPoint, getDegreeFromDistance(distance, unit));
	}
	
	private String toWkt(double latitude, double longitude) {
		StringBuilder sb = new StringBuilder("POINT (");
		sb.append(latitude)
			.append(" ")
			.append(longitude)
			.append(")");
		
		return sb.toString();
	}

	private double getDegreeFromDistance(int distance, String unit) {
		double degree = 0D;
				
		if (unit.equalsIgnoreCase(UNIT_MILE)) {
			degree = distance / MILES_PER_POINT_OF_LONGITUDE;
		} else {
			degree = distance / KM_PER_POINT_OF_LONGITUDE;
		}
		
		return (double)Math.round(degree * PRECISION) / PRECISION;
	}
}
