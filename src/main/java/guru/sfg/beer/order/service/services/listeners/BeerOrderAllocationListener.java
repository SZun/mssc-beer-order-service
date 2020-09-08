package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult result) {
        Boolean allocationError = result.getAllocationError();
        Boolean pendingInventory = result.getPendingInventory();
        BeerOrderDto beerOrderDto = result.getBeerOrderDto();

        if(!allocationError && !pendingInventory) {
            beerOrderManager.beerOrderAllocationPassed(beerOrderDto);
        } else if (pendingInventory) {
            beerOrderManager.beerOrderAllocationPendingInventory(beerOrderDto);
        } else {
            beerOrderManager.beerOrderAllocationFailed(beerOrderDto);
        }
    }

}
