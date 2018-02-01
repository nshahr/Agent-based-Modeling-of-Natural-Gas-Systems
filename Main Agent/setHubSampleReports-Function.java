if(HubSampleReports == null){
	HubSampleReports = new HashMap<Integer, ArrayList<HubReport>>();
	HubSampleReportMonthly = new HashMap<Integer, ArrayList<HubMonthlyReport>>();
}
ArrayList<ArrayList<HubReport>> hubreports = new ArrayList<ArrayList<HubReport>>();

for(Hub hub : hubs){
	ArrayList<HubReport> temps = new ArrayList<HubReport>();
	ArrayList<HubMonthlyReport> tmps = new ArrayList<HubMonthlyReport>();
	HubSampleReports.put(hub.nodeId, temps);
	HubSampleReportMonthly.put(hub.nodeId, tmps);
	
	hubreports.add(temps);
}

List<Tuple> rows;
for(int hubIndex = 0 ; hubIndex < hubs.size() ; hubIndex++){
	rows = selectFrom(all_hub_reports_sample_2)
		.where(all_hub_reports_sample_2.hub_id.eq(hubIndex))
		.orderBy(all_hub_reports_sample_2.day.asc())
		.list();
	//for(int i = 0 ; i<rows.size() ; i++){
		//traceln(rows.get(i));
	//}
	HubMonthlyReport report = new HubMonthlyReport();
	for(Tuple row : rows){
		String tmp = row.get(all_hub_reports_sample_2.report);
		tmp = tmp.substring(1, tmp.length() -1);
		//traceln(tmp);
		String[] tmpList = tmp.split(","); // here the string will be changed to an array of three
		int day = row.get(all_hub_reports_sample_2.day);
		int hub_id = row.get(all_hub_reports_sample_2.hub_id);
		HubReport hb = new HubReport(Double.parseDouble(tmpList[0]) , 
										 Double.parseDouble(tmpList[1]) , 
										 Double.parseDouble(tmpList[2]));
		hubreports.get(hub_id).add(hb);
		report.averageHubPrice += Double.parseDouble(tmpList[1]);
		if((day+1) % 30 == 0){
			report.averageHubPrice /= 30.0;
			HubSampleReportMonthly.get(hubs.get(hub_id).nodeId).add(report);
			report = new HubMonthlyReport();
		}
	}
}


/**
HubMonthlyReport report = new HubMonthlyReport();
for(Tuple row : rows){
	for(int i = 2; i<row.size() ; i++){
		String tmp = row.get(i , String.class).substring(1, row.get(i, String.class).length() - 1);
		String[] tmpList = tmp.split(",");
		int day  = (Integer)row.get(1, Integer.class);
		HubReport hb = new HubReport(Double.parseDouble(tmpList[0]) , 
										 Double.parseDouble(tmpList[1]) , 
										 Double.parseDouble(tmpList[2]) , 
										 Double.parseDouble(tmpList[3]) , 
										 Double.parseDouble(tmpList[4]) , 
										 Double.parseDouble(tmpList[5]) );
		hubreports.get(i-2).add(hb);
		report.averageSupplyPrice += Double.parseDouble(tmpList[1]);
		report.averageDemandPrice += Double.parseDouble(tmpList[4]);
		if((day+1) % 30 == 0){
			report.averageSupplyPrice /= 30.0;
			report.averageDemandPrice /= 30.0;
			//HubSampleReportMonthly.put(hubs.get(i-2).nodeId ,
			HubSampleReportMonthly.get(hubs.get(i-2).nodeId).add(report);
			report = new HubMonthlyReport();
		}
	}
}
**/
/**
List<Tuple> rows0 = selectFrom(hub0_sample_reports).list();
List<Tuple> rows1 = selectFrom(hub1_sample_reports).list();
//HubMonthlyReport report = new HubMonthlyReport();
for (Tuple row : rows0) {
	int day  = (Integer)row.get(hub0_sample_reports.day);
	HubReport hb = new HubReport((Double)row.get(hub0_sample_reports.supply_min) , 
									 (Double)row.get(hub0_sample_reports.supply_ave)  , 
									 (Double)row.get(hub0_sample_reports.supply_max)  , 
									 (Double)row.get(hub0_sample_reports.demand_min) , 
									 (Double)row.get(hub0_sample_reports.demand_ave) , 
									 (Double)row.get(hub0_sample_reports.demand_max) );
	hubreports.get(0).add(hb);
	report.averageSupplyPrice += (Double)row.get(hub0_sample_reports.supply_ave);
	report.averageDemandPrice += (Double)row.get(hub0_sample_reports.demand_ave);
	if((day+1) % 30 == 0){
		report.averageSupplyPrice /= 30.0;
		report.averageDemandPrice /= 30.0;
		HubSampleReportMonthly.get(6).add(report);
		report = new HubMonthlyReport();
	}
	
}	
report = new HubMonthlyReport();
for (Tuple row : rows1) {
	int day  = (Integer)row.get(hub1_sample_reports.day);
	HubReport hb = new HubReport((Double)row.get(hub1_sample_reports.supply_min) , 
									 (Double)row.get(hub1_sample_reports.supply_ave)  , 
									 (Double)row.get(hub1_sample_reports.supply_max)  , 
									 (Double)row.get(hub1_sample_reports.demand_min) , 
									 (Double)row.get(hub1_sample_reports.demand_ave) , 
									 (Double)row.get(hub1_sample_reports.demand_max) );
	hubreports.get(1).add(hb);
	report.averageSupplyPrice += (Double)row.get(hub1_sample_reports.supply_ave);
	report.averageDemandPrice += (Double)row.get(hub1_sample_reports.demand_ave);
	if((day+1) % 30 == 0){
		report.averageSupplyPrice /= 30.0;
		report.averageDemandPrice /= 30.0;
		HubSampleReportMonthly.get(7).add(report);
		report = new HubMonthlyReport();
	}
}**/	
for(int i = 0 ; i<hubreports.size() ; i++){
	HubSampleReports.put(hubs.get(i).nodeId, hubreports.get(i));
}