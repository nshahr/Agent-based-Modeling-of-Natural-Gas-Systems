ArrayList<Integer> removingIndices = new ArrayList<Integer>(); // we use it because during the for we can not delete while we are iterating in for loop
for(int i = 0 ; i < DrillingPlans.size()  ; i++){ //we go through drilling plans to check if the implementation day is passed, then implement it 
	SupplierCompanyDrillingPlan plan = DrillingPlans.get(i);
	if(plan.lastImplementationDay <= main.currentDay){
		removingIndices.add(i);
		int numberOfWells = plan.numberOfWells; 
		Pipeline_Node locationNode = plan.locationNode;
		Well well = main.add_wells(); //adding to the population of wells in main 
		                              //this function add a well to the population and returns it, then we can set its parameters
		well.nodeId = main.pipeline_Nodes.get(main.pipeline_Nodes.size()-1).nodeId + 1;
		well.age_in_month = 0;
		well.companyId = this.companyId;
		well.acre = plan.numberOfWells * 300; 
		well.WellOwner = this;
		well.StorageType = "Well";
		//well.startingDay = main.currentDay;
		//well.initialProduction = main.WellInitialProduction;
		
		well.countyId = locationNode.countyId;
		County county = main.findCounty(well.countyId);
		//traceln("Well -----------------------------------------------------> " + well.getName() + " -> " + county.id);
		//getEngine().pause();
		well.county = county;
		well.pipeline_id = locationNode.nodeId; //pipeline_id helps well to fine its rout to hub and in visualization we creat a pipeline from well to that pipeline_node
		county.addWell(well);
		county.calculateRemainingReserve(newWellReserveSize);
		
		//double minP,maxP;
		getCompanySize();
		
		ProductionCalculator pc = new ProductionCalculator(this, main); //this is to calculate initial production
		double eachWellProduction = locationNode.county.initialProduction * productivityRatio;
		well.initialProduction = eachWellProduction * plan.numberOfWells;
		well.MakeProductionStepList();
		well.calculateDailyProduction();
		
		double lat = triangular(county.minLat, county.maxLat, (county.minLat + county.maxLat)/2.0);
		double lon = triangular(county.minLon, county.maxLon, (county.minLon + county.maxLon)/2.0);
		
		Pipeline_Node node = main.add_pipeline_Nodes(); //adding the node of the new well
		node.nodeId = well.nodeId;
		node.isCounty = false;
		node.isMidNode = false;
		node.county = county;
		node.countyId = county.id;
		GISPoint gispoint = new GISPoint(main.map, lat , lon); //this is for locating the point in the map
		gispoint.setFillColor(Color.magenta); //TODO: it should be same collor later on 
		node.set_location(gispoint);
		StringBuilder sb = new StringBuilder(); //this is for the label we use on the map
		sb.append("W,"+this.companyId);//append is for adding to the end of the string (+ can do the same thing but since it creates a new object it makes the code slower, append doesn't make a new object and just add to the previous object)
		if(main.ShowDetailsInMap){
			sb.append("\n" + String.format("%.2f",well.DailyProduction) + "," + String.format("%.2f",well.TradedQuantity)); //\n is to go to next line in label and %.2f is for two numbers of decimals
		}
		node.set_label(sb.toString());
		node.setLocation(node.location);
		main.collectionNode.add(node);
		
		Pipeline pipe = main.add_pipelines();//this is the pipeline from well to it's pipeline_node of is county
		
		pipe.throughput = 5000; 
		Random rand = new Random();//just a small cost for this pipeline
		pipe.pipeCost = rand.nextDouble() * 0.2;//nextDouble generates a random number between 0 and 1 
		
		Pipeline_Node source = node;
		Pipeline_Node destination = locationNode;
		
		pipe.set_source(source);
		pipe.set_sourceId(source.nodeId);
		pipe.set_destination(destination);
		pipe.set_destinationId(destination.nodeId);
		
		GISRoute route = new GISRoute(main.map, new GISMarkupSegmentLine(
	                 					source.getLatitude(), 
										source.getLongitude(),
										destination.getLatitude(),
										destination.getLongitude()
				)
		);
		route.setLineStyle(LINE_STYLE_SOLID);
		route.setLineColor(new Color(255,153,153));
		route.setLineWidth(pipe.throughput /1500 +2);
		pipe.set_pipelineRoute(route);	
		
		
		
		
		well.setLatLon(lat, lon);
		
		for(Hub hub : main.hubs){//this is to conncet the new well to hubs
			if(main.getRoute(locationNode.nodeId, hub.nodeId)!=null){
				well.connectTo(hub);
				hub.ConnectedSupplies++;
				//traceln(well.getName() + " connected to " + hub.getName());
			}
		}
		addWell(well);
		well.Initialized = true;
		
		//Note!!!!!!!!!!!!!!!!!! it might be wrong because we should reduce all wells that are going to be drilled
		numberOfWells--;
		//Note!!!!!!!!!!!!!!!!! this might be un useful and should be deleted
		node.county.numberOfDrillingWells--;
	}
}
ArrayList<SupplierCompanyDrillingPlan> newDrillingPlans = new ArrayList<SupplierCompanyDrillingPlan>();
for(int i = 0 ; i < DrillingPlans.size() ; i++){
	if(!removingIndices.contains(i)){//this get filled up at the beginning of for loop
		newDrillingPlans.add(DrillingPlans.get(i));
	}
}
DrillingPlans = newDrillingPlans;