package shiroya.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shiroya.order.entity.Order;
import shiroya.order.service.OrderService;
import shiroya.orderEvent.OrderEvent;
import shiroya.orderEvent.OrderRequest;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderRequest request,
                                        HttpServletRequest Httprequest){

        return ResponseEntity.ok(service.createOrder(request, Httprequest));
    }
}
