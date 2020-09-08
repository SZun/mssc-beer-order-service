package guru.sfg.beer.order.service.services.testcomponets;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/16/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg){
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        boolean pendingInventory = false;
        boolean allocationError = false;

        BeerOrderDto beerOrderDto = request.getBeerOrderDto();
        String customerRef = beerOrderDto.getCustomerRef();

        if(customerRef != null) {
            pendingInventory = customerRef.equals("partial-allocation");
            allocationError = customerRef.equals("fail-allocation");
        }

        boolean finalPendingInventory = pendingInventory;
        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            int num = finalPendingInventory ? 1 : 0;
            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - num);
        });

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                .beerOrderDto(beerOrderDto)
                .pendingInventory(pendingInventory)
                .allocationError(allocationError)
                .build());
    }
}
