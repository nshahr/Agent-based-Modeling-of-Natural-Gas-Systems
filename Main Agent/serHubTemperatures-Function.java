// As Usual
HashMap<Integer , ArrayList<Double>> tempHashMap = new HashMap<Integer, ArrayList<Double>>(); 
ArrayList<Double> tempTemps;
List<Tuple> rows; 
if(TemperatureScenario == 0){
	// As Usual
	for(int stateIndex : StateIndexTable){
		rows = selectFrom(temperature_asusual)
			.where(temperature_asusual.state_id.eq(stateIndex))
			.orderBy(temperature_asusual.day.asc())
			.list(); 
		tempTemps = new ArrayList<Double>();
		for(Tuple row : rows){
			tempTemps.add(row.get(temperature_asusual.temperature));
		}
		tempHashMap.put(stateIndex , tempTemps);
	}
	TemperatureCollection.add(tempHashMap);
}else if (TemperatureScenario == 1){
	// Warm 
	for(int stateIndex : StateIndexTable){
		rows = selectFrom(temperature_warm)
			.where(temperature_warm.state_id.eq(stateIndex))
			.orderBy(temperature_warm.day.asc())
			.list();
			tempTemps = new ArrayList<Double>();
			for(Tuple row : rows){
				tempTemps.add(row.get(temperature_warm.temperature));
			}
			tempHashMap.put(stateIndex, tempTemps);
	}
	TemperatureCollection.add(tempHashMap);
}else if(TemperatureScenario == 2){
	// Cold
	for(int stateIndex : StateIndexTable){
		rows = selectFrom(temperature_cold)
			.where(temperature_cold.state_id.eq(stateIndex))
			.orderBy(temperature_cold.day.asc())
			.list();
			tempTemps = new ArrayList<Double>();
			for(Tuple row : rows){
				tempTemps.add(row.get(temperature_cold.temperature));
			}
			tempHashMap.put(stateIndex, tempTemps);
	}
	TemperatureCollection.add(tempHashMap);
}
// ClimateChange
/*tempHashMap = new HashMap<Integer, ArrayList<Integer>>();
for(int stateIndex : StateIndexTable){
	rows = selectFrom(temperature_climatechange)
		.where(temperature_climatechange.state_id.eq(stateIndex))
		.orderBy(temperature_climatechange.day.asc())
		.list();
		tempTemps = new ArrayList<Integer>();
		for(Tuple row : rows){
			tempTemps.add(row.get(temperature_climatechange.temperature));
		}
		tempHashMap.put(stateIndex, tempTemps);
}
TemperatureCollection.add(tempHashMap);
