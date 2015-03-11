### Parameter

#### Spring-Beans und ihre Properties

##### org.opentripplanner.analyst.batch.BatchProcessor

* **date** 
- Datum, an dem der Trip starten bzw. enden soll 
- Format: "yyyy-MM-dd"

* **time**
- Start- bzw. Endzeiptunkt (*siehe RoutingRequest.arriveBy*)

* **timeZone**
- Zeitzone (z.B. "Europe/Berlin" f�r Deutschland

* **outputPath** 
- Pfad und Name der csv-Dateien mit den Ergebnissen 
- ben�tigt "{}" im Namen (wird ersetzt)

##### org.opentripplanner.analyst.batch.Result

* **resultModes** 
- bestimmt, welche Ergebnisse in den csv-Dateien ausgegeben werden 
- BOARDINGS - Einstiege in Verkehrsmittel
- TRAVELTIME - gesamte Zeit des Trips
- STARTTIME - Beginn des Trips
- ARRIVALTIME - Ankunftszeit des Trips
- WALKINGDISTANCE - Distanz, die auf dem Trip zu Fu� zur�ckgelegt wurde

* **bestMode** 
- Modus (*siehe resultModes*), der bestimmt, welcher n�chstgelegene Knoten als g�nstiger erachtet wird

##### org.opentripplanner.routing.core.RoutingRequest

* **maxWalkDistance** 
- maximale Distanz (*in Metern*), die zu Fu� zur�ckgelegt werden darf (auf der ganzen Strecke)

* **arriveBy** 
- entweder soll der Trip zur angegeben Zeit(*siehe BatchProcessor.date*) enden (wenn *true*) oder starten (wenn *false*)

* **modes**
- Verkehrsmittel, die auf dem Trip genutzt werden d�rfen
- WALK, BICYCLE, CAR, TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, 
GONDOLA, FUNICULAR, TRANSIT, TRAINISH, BUSISH, LEG_SWITCH, 
CUSTOM_MOTOR_VEHICLE (Fahrzeug, das benutzerdefinierte Konfiguration ben�tigt, z.B. Trucks, Motorr�der, Airport-Shuttle);


