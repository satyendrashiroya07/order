package shiroya.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shiroya.order.entity.Order;
import shiroya.order.exception.UnAuthorizedUserException;
import shiroya.order.service.OrderService;
import shiroya.orderEvent.OrderEvent;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderEvent request,
                                        @RequestHeader("X-User-Id") String tokenUser){

        if(!tokenUser.equals((request.getUserId()))){
            throw new UnAuthorizedUserException("Unauthorized user!");
        }
            return ResponseEntity.ok(service.createOrder(request, tokenUser));
    }
}
