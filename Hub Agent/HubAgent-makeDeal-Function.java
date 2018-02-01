main.DealCount++;

proposal.supplier.makeDeal(proposal);
proposal.demander.makeDeal(proposal);

double proposalDiff = proposal.proposalItem.demanderPrice - proposal.proposalItem.supplierPrice;
this.Money += proposal.proposalItem.quantity * proposalDiff;

PurchaseConfirmation purchaseConfirm = new PurchaseConfirmation(this, new Item(proposal.proposalItem.quantity, proposal.proposalItem.supplierPrice), main.TurnCount);
this.SupplyChannel.send(purchaseConfirm , proposal.supplier);

purchaseConfirm = new PurchaseConfirmation(this, new Item(proposal.proposalItem.quantity, proposal.proposalItem.demanderPrice) , main.TurnCount);
this.DemandChannel.send(purchaseConfirm , proposal.supplier);

Route whr = proposal.supplier.getRoute(this);
Route hdr = proposal.demander.getRoute(this);



PurchaseDetail purchaseDetail = new PurchaseDetail();
purchaseDetail.demander = proposal.demander;
purchaseDetail.hub = this;
purchaseDetail.supplier = proposal.supplier;
purchaseDetail.quantity = proposal.proposalItem.quantity;
purchaseDetail.demandRoute = proposal.demandRoute;
purchaseDetail.supplyRoute = proposal.supplyRoute;
purchaseDetail.demandPrice = proposal.proposalItem.demanderPrice;
purchaseDetail.supplyPrice = proposal.proposalItem.supplierPrice;
purchaseDetail.price = (proposal.proposalItem.demanderPrice - hdr.getCheapestRoute().routeCost + proposal.proposalItem.supplierPrice + whr.getCheapestRoute().routeCost) / 2.0;

TodayPurchases.add(purchaseDetail);
proposal.supplier.addPurchase(purchaseDetail);
proposal.demander.addPurchase(purchaseDetail);

whr.updateThroughput(proposal.supplyRoute, proposal.proposalItem.quantity);
hdr.updateThroughput(proposal.demandRoute, proposal.proposalItem.quantity);

LastDealInTurn = main.TurnCount;