addLog("");
addLog("Turn ===> " + main.TurnCount);
addLog("");
List<List<TradingMsg>> listOfMessages = new ArrayList<>();

List<TradingMsg> currentDemands = new ArrayList<>();
currentDemands.addAll(DemandCollection);
listOfMessages.add(currentDemands);

List<TradingMsg> currentSupplies = new ArrayList<>();
currentSupplies.addAll(SupplyCollection);
listOfMessages.add(currentSupplies);


// Calcualtuing and adding transfer price
for(List<TradingMsg> list : listOfMessages){
	for (TradingMsg msg: list) {
		Route hubRoute = msg.storage.getRoute(this);
		double transportPrice = java.lang.Double.MAX_VALUE;
		if (hubRoute != null) {
			SingleRoute cheapest = hubRoute.getCheapestRoute();
			if(cheapest != null)transportPrice = cheapest.routeCost;
		}
		if(main.ZeroTransportCost == true)transportPrice = 0;
		msg.transportPrice = transportPrice;
	}
}

currentDemands = sortDescending(currentDemands, d -> d.item.price - d.transportPrice); 
currentSupplies = sortAscending(currentSupplies, s -> s.item.price + s.transportPrice);

// Match The first of both lists, as they both are the best in their lists

TradingMsg bestDemand = currentDemands.get(0);
TradingMsg worstDemand = currentDemands.get(currentDemands.size() - 1);
TradingMsg bestSupply = currentSupplies.get(0);
TradingMsg worstSupply = currentSupplies.get(currentSupplies.size() - 1);

// Used for segmentation of price
double bestDemandOveralPrice = bestDemand.item.price - bestDemand.transportPrice; 
double worstDemandOveralPrice = worstDemand.item.price - worstDemand.transportPrice;
double bestSupplyOveralPrice = bestSupply.item.price + bestSupply.transportPrice;
double worstSupplyOveralPrice = worstSupply.item.price + worstSupply.transportPrice;
double demandInterval = bestDemandOveralPrice - worstDemandOveralPrice;
double wellInterval = worstSupplyOveralPrice - bestSupplyOveralPrice;
addLog("Demand Range : " + demandInterval + " Min Price : " + worstDemandOveralPrice + " Max Price : " + bestDemandOveralPrice);
addLog("Supply Range : " + wellInterval + " Min Price : " + bestSupplyOveralPrice + " Max Price : " + worstSupplyOveralPrice);
addLog("");

Iterator<TradingMsg> demandItr = currentDemands.iterator();
Iterator<TradingMsg> supplyItr = currentSupplies.iterator();
double demandSpliter1 = worstDemandOveralPrice + demandInterval * 0.33;
double demandSpliter2 = worstDemandOveralPrice + demandInterval * 0.66;
double supplySpliter1 = bestSupplyOveralPrice + wellInterval * 0.33;
double supplySpliter2 = bestSupplyOveralPrice + wellInterval * 0.66;
double demandStableRange = 0.4;
double supplyStableRange = 0.4;
int maxMatch = main.MaxMatchForEachTurn;
int matchCount = 0;
Random random = new Random();

for(int i = 0 , j = 0 ; i < currentDemands.size() || j < currentSupplies.size(); i++,j++){
	TradingMsg demandCandid =  i < currentDemands.size() ? currentDemands.get(i): null;
	TradingMsg supplyCandid = j < currentSupplies.size() ? currentSupplies.get(j): null;

	long id = random.nextLong();
	
	HubMsg hubMsgDemand = new HubMsg();
	Item demanderItem = new Item();
	hubMsgDemand.id = id;
	hubMsgDemand.hub = this;


	HubMsg hubMsgSupply = new HubMsg();
	Item supplierItem = new Item();
	hubMsgSupply.id = id;
	hubMsgSupply.hub = this;
	
	if((demandCandid!=null && supplyCandid!=null) && 
		(demandCandid.item.price - demandCandid.transportPrice) > (supplyCandid.item.price + supplyCandid.transportPrice) && 
		(matchCount < maxMatch)){
			Route whr =  supplyCandid.storage.getRoute(this);
			Route hdr =  demandCandid.storage.getRoute(this);
			if((whr.getCheapestRoute() != null) &&
				(hdr.getCheapestRoute() != null) && 
				(whr.getCheapestRouteRemainingTP() != 0.0) && 
				(hdr.getCheapestRouteRemainingTP() != 0.0)){
				double hubQuantity = min(min(hdr.getCheapestRouteRemainingTP(),demandCandid.item.quantity) , min( whr.getCheapestRouteRemainingTP(),supplyCandid.item.quantity));

				demanderItem.quantity = hubQuantity;
				demanderItem.price = demandCandid.item.price;
				hubMsgDemand.state = ReplyType.ACCEPTED;
				waitingDemandDecisions++;
	
				supplierItem.quantity = hubQuantity;
				supplierItem.price = supplyCandid.item.price;
				hubMsgSupply.state = ReplyType.ACCEPTED;
				waitingSupplyDecisions++;
				
				
				ProposalItem proposalItem  = new ProposalItem(hubQuantity, demandCandid.item.price, supplyCandid.item.price);
				Proposal proposal = new Proposal(
					id, this, demandCandid.storage, supplyCandid.storage,
					proposalItem,whr.getCheapestRoute(),hdr.getCheapestRoute());
				ProposalCollection.add(proposal);
				
				matchCount++;
			}else{
				demanderItem.quantity = -1;
				demanderItem.price = -1;
				hubMsgDemand.state = ReplyType.REJECTED;
				
				supplierItem.price = -1;
				supplierItem.quantity = -1;
				hubMsgSupply.state = ReplyType.REJECTED;
			}
	}else{
			demanderItem.quantity = -1;
			demanderItem.price = -1;
			hubMsgDemand.state = ReplyType.REJECTED;
			
			supplierItem.price = -1;
			supplierItem.quantity = -1;
			hubMsgSupply.state = ReplyType.REJECTED;
			
	}
	if(demandCandid != null){
		demanderItem.id = id;
		double demandOveralPrice = demandCandid.item.price - demandCandid.transportPrice;
		if(demandInterval > demandStableRange){	
			if(demandOveralPrice <= demandSpliter1)
				hubMsgDemand.priceStatus =PriceStatus.VERY_LOW;
			else if(demandOveralPrice <= demandSpliter2)
				hubMsgDemand.priceStatus = PriceStatus.LOW;
			else
				hubMsgDemand.priceStatus = PriceStatus.APPROPRIATE;
		}else{
			hubMsgDemand.priceStatus = PriceStatus.MARKETSTABLE;	
		}
		hubMsgDemand.item = demanderItem;
		addLog("Response To Demand --> " + demandCandid.storage.nodeId + "  Price : " + demandOveralPrice + "  quantity : " + hubMsgDemand.item.quantity + "  price Status : " + hubMsgDemand.priceStatus + "  state  " + hubMsgDemand.state);
		this.DemandChannel.send(hubMsgDemand, demandCandid.storage);
	}
	if(supplyCandid != null){
		supplierItem.id = hubMsgDemand.id;
		hubMsgSupply.item = supplierItem; 
		double supplyOveralPrice = supplyCandid.item.price + supplyCandid.transportPrice;
		if(wellInterval > supplyStableRange){
			if(supplyOveralPrice <= supplySpliter1)
				hubMsgSupply.priceStatus = PriceStatus.APPROPRIATE;
			else if(supplyOveralPrice <= supplySpliter2)
				hubMsgSupply.priceStatus = PriceStatus.HIGH;
			else
				hubMsgSupply.priceStatus = PriceStatus.VERY_HIGH;
		}else{
			hubMsgSupply.priceStatus = PriceStatus.MARKETSTABLE;
		}
		//hubMsgSupply.priceStatus = PriceStatus.APPROPRIATE;
		addLog("Response To Supply --> " + supplyCandid.storage.nodeId + "  Price : " + supplyOveralPrice + "  quantity : " + hubMsgSupply.item.quantity + "  price Status : " + hubMsgSupply.priceStatus + "  state  " + hubMsgSupply.state);
		this.SupplyChannel.send(hubMsgSupply , supplyCandid.storage);
	}
}

statechart.fireEvent("Match Finished");
