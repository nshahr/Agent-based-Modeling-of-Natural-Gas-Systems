// Consider how much capacity is sold during last days 
int numOfLastDays = main.NumberOfDaysForCapacity;
int startIndex = (Reports.size() > numOfLastDays) ? Reports.size() - numOfLastDays : 0;
int endIndex = Reports.size();

addLog("Price Range Befor Learning --> : " + priceRange.min + "  " + priceRange.avg + "   " + priceRange.max);
if(StorageType == "Demand"){
	double averageQuantityBought = 0;
	for(int i = startIndex ; i < endIndex  ; i++){
		averageQuantityBought += Reports.get(i).percentageOfCapacity;
	}
	averageQuantityBought /= (endIndex - startIndex) * 1.0 ;
	addLog("Average Quantity Bought : " + averageQuantityBought);
	if(averageQuantityBought >= main.BuyingCloseToCapacity){
		priceRange.Multiply(main.BuyingCloseToCapacityCoef);
		addLog("Buying Close to Capacity   ****   New Price : " + priceRange.min + "  " + priceRange.avg + "   " + priceRange.max);
	}else if(averageQuantityBought <= main.BuyingMuchLowerThanCapacity){
		priceRange.Multiply(main.BuyingMuchLowerThanCapacityCoef);
		addLog("Buying Much Lower Than Capacity   ****   New Price : " + priceRange.min + "  " + priceRange.avg + "   " + priceRange.max);
	}
}else if(StorageType == "Well"){
	double averageQuantitySold = 0;
	for(int i = startIndex ; i < endIndex  ; i++){
		averageQuantitySold += Reports.get(i).percentageOfCapacity;
	}
	averageQuantitySold /= (endIndex - startIndex)  * 1.0 ;
	addLog("Average Quantity Sold : " + averageQuantitySold);
	if(averageQuantitySold >= main.SellingCloseToProductionCapacity){
		priceRange.Multiply(main.SellingCloseToProductionCapacityCoef);
		addLog("Selling Close to Capacity   ****   New Price : " + priceRange.min + "  " + priceRange.avg + "   " + priceRange.max);
	}else if(averageQuantitySold <= main.SellingMuchLowerThanProductionCapacity){
		priceRange.Multiply(main.SellingMuchLowerThanProductionCapacityCoef);
		addLog("Selling Much Lower Than Capacity   ****   New Price : " + priceRange.min + "  " + priceRange.avg + "   " + priceRange.max);
	}
}	
return priceRange;