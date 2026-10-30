package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.ProductOrder;

public interface ProductOrderService {
    public ProductOrder Send(ProductOrder productOrder) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ProductOrder productOrder) throws AxelorException;
   public PurchaseRequest CreateOrUpdatePurchaseRequest(ProductOrder productOrder) throws AxelorException;
    public ProductOrder SendLiveStockFeedFactory(ProductOrder productOrder) throws AxelorException;
    public ProductOrder CancelSendLiveStockFeedFactory(ProductOrder productOrder) throws AxelorException;
    public ProductOrder Validate(ProductOrder productOrder) throws AxelorException;
    public ProductOrder CancelSend(ProductOrder productOrder) throws AxelorException;
    public ProductOrder CancelReleaseOrder(ProductOrder productOrder) throws AxelorException;
    public ProductOrder CancelReceive(ProductOrder productOrder) throws AxelorException;
    public ProductOrder Receive(ProductOrder productOrder) throws AxelorException;

}
