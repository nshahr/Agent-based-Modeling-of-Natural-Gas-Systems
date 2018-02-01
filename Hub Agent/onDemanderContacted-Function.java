//traceln("Demand Contacted ---->   hub: " +nodeId + "   " + msg.toString());
if (msg instanceof TradingMsg) {
	TradingMsg message = ((TradingMsg)msg);
	DemandCollection.add(message);
	//traceln("Demand Contacted  ------> hub:" + this.nodeId + " demand:" + message.storage.nodeId + " amount:" + message.item.quantity + " price:" + message.item.price);
	DemandReady = DemandCollection.size() == ConnectedDemands;
	
	Route hubRoute = message.storage.getRoute(this);
	double transportPrice = java.lang.Double.MAX_VALUE;
	if (hubRoute != null) {
		SingleRoute cheapest = hubRoute.getCheapestRoute();
		if(cheapest != null)transportPrice = cheapest.routeCost;
	}
	if(main.ZeroTransportCost == true)transportPrice = 0;
	message.transportPrice = transportPrice;
		
	addLog("Demand " + message.storage.nodeId + " Proposal -->  price: " + message.item.price + " transport: " + message.transportPrice + " price with transport: " + (message.item.price - message.transportPrice)+ "  quantity : " + message.item.quantity + " responded : " + message.responded);
} else if (msg instanceof HubMsg) {
	HubMsg hm = ((HubMsg)msg);
	addLog("");
	addLog("Final Response From Demand --> price : " + hm.item.price + "  quantity : " + hm.item.quantity + "  price status : " + hm.priceStatus + "  state : " + hm.state );
	processDemandReply(hm);
}else if (msg.toString().equals("Purchase Done")){
	ConnectedDemands--;
	DemandReady = DemandCollection.size() == ConnectedDemands;
}