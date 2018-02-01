double minPriceHub = java.lang.Double.MAX_VALUE;
double maxPriceHub = java.lang.Double.MIN_VALUE;
double hubPrice = 0.0;

double totalHubMoney = 0.0;

double totalQuantity = 0;

Set<Integer> supplies = new HashSet<Integer>();
Set<Integer> demands = new HashSet<Integer>();

for(PurchaseDetail detail : TodayPurchases){
	hubPrice = (detail.supplyPrice + detail.supplyRoute.routeCost + main.SupplyMarketingCost + detail.demandPrice - detail.demandRoute.routeCost - main.DemandMarketingCost) / 2.0;
	minPriceHub = min(minPriceHub , hubPrice);
	maxPriceHub = max(maxPriceHub , hubPrice);

	totalHubMoney += hubPrice * detail.quantity;
	totalQuantity += detail.quantity;
	
	supplies.add(detail.supplier.nodeId);
	demands.add(detail.demander.nodeId);
}
if(totalQuantity != 0){
	hubPrice = totalHubMoney / totalQuantity;
}
// Making the report
ReportDetail report = new ReportDetail(main.currentDay, true);
if(TodayPurchases.size() != 0){
	report.minPriceHub = minPriceHub;
	report.maxPriceHub = maxPriceHub;
	report.weightedAvgPriceHub = hubPrice;
	
	report.totalQuantity = totalQuantity;
	report.totalDeal = TodayPurchases.size();
	report.numOfSuppliers = supplies.size();
	report.numOfDemands = demands.size();
	report.totalTurn = LastDealInTurn;
}

// calculate weighted average vairance for last 7 days
int reportCounts = 14;
int vDayCount = 60;
int tomorrow = main.currentDay + 1;
int currentDay = main.currentDay;
int startDay = (tomorrow > vDayCount ) ? tomorrow - vDayCount : 0;
List<ReportDetail> lastReports = new ArrayList<ReportDetail>(Reports.subList(startDay, currentDay));
List<ReportDetail> weekReports = new ArrayList<ReportDetail>();
lastReports.add(report);
double weightedAverageMean = 0.0;
for(int i = lastReports.size()-1 ; i >= 0 && reportCounts != 0 ; i--){
	if(lastReports.get(i).totalQuantity != 0.0){
		reportCounts -= 1;
		weightedAverageMean += lastReports.get(i).weightedAvgPriceHub;
		weekReports.add(lastReports.get(i));
	}
}
/*
for(ReportDetail rd : weekReports){
	weightedAverageMean += rd.weightedAvgPriceHub;
}
*/
weightedAverageMean /= weekReports.size();
double variance = 0.0;
int averageTurns = 0;
String st = "";
for(ReportDetail rd : weekReports){
	averageTurns += rd.totalTurn;
	st += rd.totalTurn + ",  ";
	variance += Math.pow(rd.weightedAvgPriceHub - weightedAverageMean , 2);
}
report.weekAverageTurn = (weekReports.size() != 0) ? (averageTurns * 1.0 / weekReports.size()) : 0;
//traceln("hub : " + nodeId + "  turns for days : " + st + "averge : " + report.weekAverageTurn + " ~~~~~~~~~~~~~~~~~~~~~~");
report.hubWeightedAverageMean = weightedAverageMean;
report.hubVariance = variance;
report.hubStandardDeviation = Math.sqrt(variance);
report.hubCoefOfVariation = (weightedAverageMean == 0.0) ? 0.0 : (Math.sqrt(variance) / weightedAverageMean);

Reports.add(report);