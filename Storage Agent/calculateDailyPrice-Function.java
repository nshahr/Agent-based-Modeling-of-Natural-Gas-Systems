int numOfLastDays = 365;
int numOfClosestDays = main.ClosestDayToTomorrowByTemperature;
int currentDay = main.currentDay;
int tomorrow = currentDay + 1;
int startDay = ((currentDay + main.StartSimulationOffset - numOfLastDays));
startDay = (startDay < 0) ? 0 : startDay;

PriceRange totalPrice = new PriceRange(StorageType);
double totalQuant = 0.0;
String traceStr = "";
addLog("DAY " + currentDay + " PRICE");
//========================================================================================================================================
if(main.FixedPrice || main.RandomPrice){
	// use fixed or random price for day price.
	double newPrice = calcDailyFixedOrRandomPrice();
	for(Hub hub : getConnectedHubs()){
		updateDailyPriceForHub(hub,newPrice);
	}
//========================================================================================================================================
}else{
	double tomorrowAvgTemp = main.getTomorrowAvgTemp();
	addLog("Tomorrow Average Temperature : " + tomorrowAvgTemp);
	List<Temperature> temperatures = getLastYearTemperatures(currentDay, startDay, tomorrowAvgTemp);
	//========================================================================================================================================
	String selectedDays ="";
	for(Hub hub : getConnectedHubs()){
		addLog("Hub Number : " + hub.nodeId);
		// find min, max, avg of price from hub reports.
		// if not enough report, use hub sample reports.
		PriceRange priceRange = calRangeFromHubReports(temperatures, hub, numOfClosestDays);
		//========================================================================================================================================
		// if main.UseSimilarTemperatureForComparison is true, use tomorrow average temperature else use the hub tomorrow's temperature
		// to have a scense of forecasting and making changes gradual, we use tomorrow's temperature in addition to the day after tomorrow 
		// and the day after that.  
		double diffTemp1 = calcTempDifferenceByTomorrow(1, hub, priceRange.averageTemp);
		double diffTemp2 = calcTempDifferenceByTomorrow(2, hub, priceRange.averageTemp);
		double diffTemp3 = calcTempDifferenceByTomorrow(3, hub, priceRange.averageTemp);
		traceStr = String.format("A--> Min : %s , Ave : %s ,Max : %s",priceRange.min,priceRange.avg,priceRange.max);
		//========================================================================================================================================
		// if enabled, uses either log or linear regression formula, otherwise uses just min-max range
		if(main.EnableDailyPriceLLRFormula){	
			//========================================================================================================================================
			// considering TemperatureChangeByTomorrow, if the change in temperature is significant in any of the 3 following days, use log formula
			// otherwise use LRNumofLastDays days to calculate linear regression estimating price min, max & avg
			if( Math.abs(diffTemp1) >= main.TemperatureThreshold ||
				Math.abs(diffTemp2) >= main.TemperatureThreshold ||
				Math.abs(diffTemp3) >= main.TemperatureThreshold ){
				addLog("Logarithm");
				priceRange = calcPriceUsingLogarithm(diffTemp1, diffTemp2, diffTemp3, priceRange);
			//========================================================================================================================================
			}else{
				addLog("Linear Regression");
				priceRange = calcPriceUsingLR(currentDay, hub, priceRange);
			}
		}		
		
		priceRange = calcPriceWithTransportCost(hub, priceRange);
		//traceln(traceStr);
			
		if(main.DifferentDailyPriceForHubs){
			if(main.EnableLearningParameters){
				priceRange = calcPriceWithQuantitySold(priceRange);
			}
			if(main.FixRange){
				priceRange.FixRange();
				addLog("Price Range After Fix --> Min : " + priceRange.min + " Avg : " + priceRange.avg + " Max : " + priceRange.max);
			}
			traceStr = String.format("E--> Min : %s , Ave : %s ,Max : %s",priceRange.min,priceRange.avg,priceRange.max);

			double newPrice = calcPriceUsingUniformNormal(priceRange);
			
			updateDailyPriceForHub(hub, newPrice);
			addLog("-----------------------------------------------");
			addLog("Different Daily Price For Each Hub--->  Price : " + newPrice);
			addLog("");
			addLog("");
			//traceln(nodeId + " TO " + hub.nodeId +  "  Range --->   " + priceRange.min+ "  " + priceRange.avg+ "  " + priceRange.max);
		}else{
			if(main.FixRange){
				priceRange.FixRange();
				addLog("Price Range After Fix --> Min : " + priceRange.min + " Avg : " + priceRange.avg + " Max : " + priceRange.max);
			}
			traceStr = String.format("E--> Min : %s , Ave : %s ,Max : %s",priceRange.min,priceRange.avg,priceRange.max);
	
			totalPrice.min += priceRange.min;
			totalPrice.max += priceRange.max;
			totalPrice.avg += priceRange.avg;
			//traceln(nodeId + " TO " + hub.nodeId  + "  " + selectedDays +  "  Range --->   " + priceRange.min+ "  " + priceRange.avg+ "  " + priceRange.max);
		}
		
	}
	
	if(main.DifferentDailyPriceForHubs){
	
	}else{
		totalPrice.min /= (getConnectedHubs().size() * 1.0);
		totalPrice.max /= (getConnectedHubs().size() * 1.0);
		totalPrice.avg /= (getConnectedHubs().size() * 1.0);
		addLog("Total Price For All Hubs  -->   Min : " + totalPrice.min + " Avg : " + totalPrice.avg + " Max : " + totalPrice.max );
		//traceln(nodeId  + "  " + selectedDays +  "  Range --->   " + totalPrice.min + "  " + totalPrice.avg + "  " + totalPrice.max);
		if(main.EnableLearningParameters){
			totalPrice = calcPriceWithQuantitySold(totalPrice);
		}
		
		if(main.FixRange){
			totalPrice.FixRange();
			addLog("Price Range After Fix --> Min : " + totalPrice.min + " Avg : " + totalPrice.avg + " Max : " + totalPrice.max);
		}
		
		double newPrice = calcPriceUsingUniformNormal(totalPrice);
		//traceln("Price after uniform : " + this.nodeId + "   " + totalMin + "  " + totalAve + "  " + totalMax + "  " + newPrice);
		
		
		//traceln("Price after Percentage : " + this.nodeId + "   " + totalMin + "  " + totalAve + "  " + totalMax + "  " + newPrice);
		addLog("-----------------------------------------------");
		addLog("Same Daily Price For Each Hub--->  Price : " + newPrice);
		addLog("");
		addLog("");
		for(Hub hub : getConnectedHubs()){
			updateDailyPriceForHub(hub , newPrice);
		}
	}
}