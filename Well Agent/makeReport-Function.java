Revenues.add(todayRevenue);
Report wr = new Report(main.currentDay);
//wr.startingPrice = startingPrice;
if(Purchases.size() == 0){
	wr.minPrice = 0;
	wr.maxPrice = 0;
	wr.averagePrice = 0;
	wr.percentageOfCapacity = 0;
	for(Hub hub : getConnectedHubs()){
		Route whr = getRoute(hub);
		wr.quantityPerHub.put(hub.nodeId , 0.0);
		double iii = whr.getCheapestRoute().routeCost;
		wr.transportPerHub.put(hub.nodeId , whr.getCheapestRoute().routeCost);
	}
}else{
	PurchasePerHub = new HashMap<Integer, Integer>(); 
	for(Hub hub : getConnectedHubs()){
		Route whr = getRoute(hub);
		wr.quantityPerHub.put(hub.nodeId , 0.0);
		wr.transportPerHub.put(hub.nodeId , whr.getCheapestRoute().routeCost);
	}
	
	for(PurchaseDetail purchase : Purchases){
		int hubid = purchase.hub.nodeId;
		double newTPCost = 0.0;
		
		wr.minPrice = min(wr.minPrice,purchase.supplyPrice);
		wr.maxPrice = max(wr.maxPrice,purchase.supplyPrice);
		wr.averageTransportCost += purchase.supplyRoute.routeCost * purchase.quantity;
		wr.averagePrice += ( Double.isNaN(purchase.supplyPrice ) ? 0 : (purchase.supplyPrice) * purchase.quantity );
		wr.totalQuantity += ( Double.isNaN(purchase.supplyPrice ) ? 0 : purchase.quantity);
		
		if(PurchasePerHub.containsKey(hubid)){
			newTPCost = (wr.transportPerHub.get(hubid) * PurchasePerHub.get(hubid)) + purchase.supplyRoute.routeCost;
			wr.quantityPerHub.put(hubid, wr.quantityPerHub.get(hubid) + purchase.quantity);
			PurchasePerHub.put(hubid, PurchasePerHub.get(hubid) + 1);
			wr.transportPerHub.put(hubid, newTPCost / PurchasePerHub.get(hubid));
		}else{
			wr.quantityPerHub.put(hubid, purchase.quantity);
			wr.transportPerHub.put(hubid, purchase.supplyRoute.routeCost);
			PurchasePerHub.put(hubid, 1);
		}	
	}
	
	wr.totalDeal = Purchases.size();
//	wr.percentageOfCapacity = (ProductionCapacity != 0.0) ? wr.totalQuantity / ProductionCapacity * 100.0: 0.0;
	wr.percentageOfCapacity = (DailyProduction != 0.0) ? wr.totalQuantity / DailyProduction * 100.0: 0.0;
	wr.averageTransportCost /= (wr.totalQuantity != 0) ? wr.totalQuantity * 1.0 : 1;
	wr.averagePrice /= (wr.totalQuantity != 0 ? wr.totalQuantity * 1.0 : 1);
	wr.money = todayRevenue;
}
Reports.add(wr);