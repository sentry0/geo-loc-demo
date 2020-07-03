package geoloc.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vividsolutions.jts.geom.Geometry;

import geoloc.domain.City;

// Copyright 2020-Present Philip J. Guinchard
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public interface CityRepository extends JpaRepository<City, Long> {
	@Query("from City c where dwithin(c.coordinates, :point, :distance) = true order by distance(c.coordinates, :point)")
	List<City> findAdjacentCity(@Param("point") Geometry point, @Param("distance") double distance);
}
