Report rr = new Report(main.currentDay);
if(Purchases.size() == 0){
	rr.minPrice = 0;
	rr.maxPrice = 0;
	rr.averagePrice = 0;
	rr.percentageOfCapacity = 0;
	for(Hub hub : getConnectedHubs()){
		Route whr = getRoute(hub);
		rr.quantityPerHub.put(hub.nodeId , 0.0);
		rr.transportPerHub.put(hub.nodeId , whr.getCheapestRoute().routeCost);
	}
}else{
	PurchasePerHub = new HashMap<Integer, Integer>(); 
	for(Hub hub : getConnectedHubs()){
		Route whr = getRoute(hub);
		rr.quantityPerHub.put(hub.nodeId , 0.0);
		rr.transportPerHub.put(hub.nodeId , whr.getCheapestRoute().routeCost);
	}
	
	for(PurchaseDetail purchase : Purchases){
		int hubid = purchase.hub.nodeId;
		double newTPCost = 0.0;
		
		if(StorageType == "Well"){
			// Supply
			rr.minPrice = min(rr.minPrice,purchase.supplyPrice);
			rr.maxPrice = max(rr.maxPrice,purchase.supplyPrice);
			rr.averageTransportCost += purchase.supplyRoute.routeCost * purchase.quantity;
			rr.averagePrice += ( Double.isNaN(purchase.supplyPrice ) ? 0 : (purchase.supplyPrice) * purchase.quantity );
			rr.totalQuantity += ( Double.isNaN(purchase.supplyPrice ) ? 0 : purchase.quantity);
			
			if(PurchasePerHub.containsKey(hubid)){
				newTPCost = (rr.transportPerHub.get(hubid) * PurchasePerHub.get(hubid)) + purchase.supplyRoute.routeCost;
				rr.quantityPerHub.put(hubid, rr.quantityPerHub.get(hubid) + purchase.quantity);
				PurchasePerHub.put(hubid, PurchasePerHub.get(hubid) + 1);
				rr.transportPerHub.put(hubid, newTPCost / PurchasePerHub.get(hubid));
			}else{
				rr.quantityPerHub.put(hubid, purchase.quantity);
				rr.transportPerHub.put(hubid, purchase.supplyRoute.routeCost);
				PurchasePerHub.put(hubid, 1);
			}
		}else{
			// Demand
			rr.minPrice = min(rr.minPrice,purchase.demandPrice);
			rr.maxPrice = max(rr.maxPrice,purchase.demandPrice);
			rr.averageTransportCost += purchase.demandRoute.routeCost * purchase.quantity;
			rr.averagePrice += (Double.isNaN(purchase.demandPrice ) ? 0 : purchase.demandPrice * purchase.quantity);
			rr.totalQuantity += Double.isNaN(purchase.demandPrice ) ? 0 :purchase.quantity;
		
			if(PurchasePerHub.containsKey(hubid)){
				newTPCost =	(rr.transportPerHub.get(hubid) * PurchasePerHub.get(hubid)) + purchase.demandRoute.routeCost;	
				rr.quantityPerHub.put(hubid , rr.quantityPerHub.get(hubid) + purchase.quantity);
				PurchasePerHub.put(hubid , PurchasePerHub.get(hubid) + 1);
				rr.transportPerHub.put(hubid, newTPCost / PurchasePerHub.get(hubid));
			}else{
				rr.quantityPerHub.put(hubid, purchase.quantity);
				rr.transportPerHub.put(hubid, purchase.demandRoute.routeCost);
				PurchasePerHub.put(hubid, 1);
			}	
		}
	}
	
	rr.totalDeal = Purchases.size();
	rr.percentageOfCapacity = (DailyProduction != 0.0) ? rr.totalQuantity / DailyProduction * 100.0: 0.0;
	rr.averageTransportCost /= (rr.totalQuantity != 0) ? rr.totalQuantity * 1.0 : 1;
	rr.averagePrice /= (rr.totalQuantity != 0 ? rr.totalQuantity * 1.0 : 1);
	//rr.money = todayRevenue;
}
Reports.add(rr);