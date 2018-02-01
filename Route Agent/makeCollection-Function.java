List<String> routes = new ArrayList<String>(Arrays.asList(route.split("}"))); 
String tmpstr = "";
for(String rstring : routes){
	//traceln(rstring + " "  + beginingId + " "  + endId);
	tmpstr = rstring;
	if(tmpstr.charAt(0) == ','){
		tmpstr = tmpstr.substring(2, tmpstr.length());
	}else{
		tmpstr = tmpstr.substring(1, tmpstr.length());
	}
	
	if(tmpstr.length() == 0){
		SingleRoute singleRoute = new SingleRoute();
		singleRoute.nodes.add(beginingId);
		singleRoute.nodes.add(endId);
		singleRoute.routeCost = main.findPipeline(beginingId, endId).pipeCost;
		Routes.add(singleRoute);
		continue;
	}
	
	List<String> pipelineNodes = new ArrayList<String>(Arrays.asList(tmpstr.split(",")));
	double routecost = 0;
	///traceln(pipelineNodes.get(0));
	//traceln(Integer.parseInt(pipelineNodes.get(0)));
	routecost += main.findPipeline(beginingId , Integer.parseInt(pipelineNodes.get(0))).pipeCost;
	SingleRoute singleRoute = new SingleRoute();
	singleRoute.nodes.add(beginingId);
	for(int i = 0 ; i < pipelineNodes.size() ; i++){
		int nodeid = Integer.parseInt(pipelineNodes.get(i));
		singleRoute.nodes.add(nodeid);
		Pipeline pipelineAgent;
		if(i == pipelineNodes.size() - 1){
			pipelineAgent = main.findPipeline(nodeid, endId);
			singleRoute.nodes.add(endId);
		}else{
			pipelineAgent = main.findPipeline(singleRoute.nodes.get(i),nodeid);
		}
			routecost += pipelineAgent.pipeCost;
	}
	singleRoute.routeCost = routecost;
	Routes.add(singleRoute);
}