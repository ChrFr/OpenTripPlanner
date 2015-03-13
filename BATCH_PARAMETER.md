# Parameter

## Spring-Beans und ihre Properties

### org.opentripplanner.analyst.batch.BatchProcessor

**date** 
  * Datum, an dem der Trip starten bzw. enden soll 
  * Format: "yyyy-MM-dd"

**time**
  * Start- bzw. Endzeitpunkt (*siehe RoutingRequest.arriveBy*)

**timeZone**
  * Zeitzone (z.B. "Europe/Berlin" für Deutschland)
  
**searchCutoffSeconds**
  * maximale Reisezeit (*in Sekunden*)

**outputPath** 
  * Pfad und Name der csv-Dateien mit den Ergebnissen 
  * benötigt "{}" im Namen (wird ersetzt)
  

### org.opentripplanner.analyst.batch.ResultSet2D
  
**defaultResult**
  * bestimmt, welcher Ergebnistyp als Standardausgabe verwendet wird (betrifft Aggregatoren etc.)
  * default: "TRAVELTIME"

**resultTypes** 
  * bestimmt, welche Ergebnisse in den csv-Dateien ausgegeben werden 
  * *BOARDINGS* - Einstiege in Verkehrsmittel
  * *TRAVELTIME* - gesamte Zeit des Trips
  * *STARTTIME* - Beginn des Trips
  * *ARRIVALTIME* - Ankunftszeit des Trips
  * *WALKINGDISTANCE* - Distanz, die auf dem Trip zu Fuß zurückgelegt wurde
  * default: defaultResult

**bestResultType** 
  * Typ (*siehe resultTypes*), der bestimmt, welcher nächstgelegene Knoten als günstiger erachtet wird
  * default: defaultResult
  

### org.opentripplanner.routing.core.RoutingRequest (Auszug der wichtigsten Properties)

**arriveBy** 
  * entweder soll der Trip zur angegeben Zeit(*siehe BatchProcessor.date*) enden (wenn *true*) oder starten (wenn *false*)
  
**optimize**
  * welche Route wird bei Optimierung der Trips genutzt?
  * *QUICK* - schnellste Route
  * *SAFE* - sicherste(?) Route
  * *FLAT* - ? (*"needs a rewrite"*)
  * *GREENWAYS* - grünste Route
  * *TRIANGLE* - ? (*"not supported yet"*)
  * *TRANSFERS* - obsolet, transferPenalty nutzen!
 
#### Verkehrsmittel

**modes**
  * Verkehrsmittel, die auf dem Trip genutzt werden dürfen
  * *WALK, BICYCLE, CAR, TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, GONDOLA, FUNICULAR, TRANSIT, TRAINISH, BUSISH, LEG_SWITCH, CUSTOM_MOTOR_VEHICLE* (Fahrzeug, das benutzerdefinierte Konfiguration benötigt, z.B. Trucks, Motorräder, Airport-Shuttle);
  * Achtung: keine Freizeichen erlaubt!
    
**parkAndRide**
  * Park and Ride erlauben (wenn *true*)

**kissAndRide**
  * Kiss and Ride erlauben (wenn *true*)
  
#### Maxima

**clampInitialWait**
  * maximale Zeit (*in Sekunden*), die der Nutzer gewillt ist, auf den Beginn des Trips zu warten 
  
**maxTransfers**
  * maximale Anzahl der Umstiege
  * default: 2

**maxWalkDistance** 
  * maximale Distanz (*in Metern*), die zu Fuß zurückgelegt werden darf (auf der ganzen Strecke)
  * weiches Maximum, wird schrittweise erhöht, wenn keine Route dieses erfüllt
  * default: Double.MAX_VALUE
  
**maxPreTransitTime**
  * maximale Anreisezeit (*in Sekunden*) zu anderen Verkehrsmitteln mit Park and Ride oder Kiss and Ride
  * default: Integer.MAX_VALUE
  
#### Straffaktoren
  
**walkReluctance**
  * Faktor, um den es schlechter ist zu laufen, als auf ein Verkehrsmittel zu warten
  * default: 2.0
  
**waitReluctance**
  * Faktor, um den es schlechter ist, auf ein Verkehrsmittel zu warten, als mit einem transportiert zu werden 
  * Startwartezeit ausgenommen, hat eigenen Faktor (*waitAtBeginningFactor*, default: 0.2)
  * default: 0.95
  
**transferPenalty**
  * Strafe für Umstiege
  * default: 0

*Fahrradoptionen aufführen?*


