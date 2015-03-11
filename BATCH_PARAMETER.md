### Parameter

#### Spring-Beans und ihre Properties

##### org.opentripplanner.analyst.batch.BatchProcessor

**date** 
  * Datum, an dem der Trip starten bzw. enden soll 
  * Format: "yyyy-MM-dd"

**time**
  * Start- bzw. Endzeitpunkt (*siehe RoutingRequest.arriveBy*)

**timeZone**
  * Zeitzone (z.B. "Europe/Berlin" für Deutschland

**outputPath** 
  * Pfad und Name der csv-Dateien mit den Ergebnissen 
  * benötigt "{}" im Namen (wird ersetzt)

##### org.opentripplanner.analyst.batch.Result

**resultModes** 
  * bestimmt, welche Ergebnisse in den csv-Dateien ausgegeben werden 
  * BOARDINGS - Einstiege in Verkehrsmittel
  * TRAVELTIME - gesamte Zeit des Trips
  * STARTTIME - Beginn des Trips
  * ARRIVALTIME - Ankunftszeit des Trips
  * WALKINGDISTANCE - Distanz, die auf dem Trip zu Fuß zurückgelegt wurde

**bestMode** 
  * Modus (*siehe resultModes*), der bestimmt, welcher nächstgelegene Knoten als günstiger erachtet wird

##### org.opentripplanner.routing.core.RoutingRequest (Auszug der wichtigsten Properties)

**maxWalkDistance** 
  * maximale Distanz (*in Metern*), die zu Fuß zurückgelegt werden darf (auf der ganzen Strecke)
  * default: Double.MAX_VALUE

**arriveBy** 
  * entweder soll der Trip zur angegeben Zeit(*siehe BatchProcessor.date*) enden (wenn *true*) oder starten (wenn *false*)

**modes**
  * Verkehrsmittel, die auf dem Trip genutzt werden dürfen
  * WALK, BICYCLE, CAR, TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, GONDOLA, FUNICULAR, TRANSIT, TRAINISH, BUSISH, LEG_SWITCH, CUSTOM_MOTOR_VEHICLE (Fahrzeug, das benutzerdefinierte Konfiguration benötigt, z.B. Trucks, Motorräder, Airport-Shuttle);
  
**clampInitialWait**
  * maximale Zeit (*in Sekunden*), die der Nutzer gewillt ist, auf den Beginn des Trips zu warten 
  
**maxTransfers**
  * maximale Anzahl der Umstiege
  * default: 2
  
**parkAndRide**
  * Park and Ride erlauben (wenn *true*)

**kissAndRide**
  * Kiss and Ride erlauben (wenn *true*)
  
**maxPreTransitTime**
  * maximale Anreisezeit (*in Sekunden*) zu anderen Verkehrsmitteln mit Park and Ride oder Kiss and Ride
  * default: Integer.MAX_VALUE
  
*Fahrradoptionen aufführen?*
