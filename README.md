# geo-loc-demo
A geospacial microservice demo created with Spring Boot

## Running - The Easy Way

### Pre-requisites

Have docker and java 8+ installed

### Running The App

	./run.sh
	
Once running hit http://localhost:8080/cities?latitude=43.45&longitude=-80.5&distance=150&unit=km with your chosen REST client or browser to play with the app.

## Running - The Complicated Way

### Pre-requisites

1. A DB with geospacial capabilities, I recommend PostGIS
2. Create a blank DB (the app will handle everything else)

### Setup
Open the application-local.properties file and change lines 21-23 so that you can connect to your database.

### Running the App
The quickest way to run this demo is to import it into the [Spring Tool Suite](https://spring.io/tools) and run it there.  You can also run it via the command line if you dont like or don't use the Spring Tool Suite.

In either case make sure you set your profile to "local" in order for the proper properties file to be applied.

Once running hit http://localhost:8080/cities?latitude=43.45&longitude=-80.5&distance=150&unit=km with your chosen REST client or browser to play with the app.

## Tests
Nothing here just yet :P

## Attribution
The geographic data used for this demo was sourced from the [World City Database](https://simplemaps.com/data/world-cities).  Thank you for your work and free offering.
