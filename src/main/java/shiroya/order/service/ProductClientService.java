package shiroya.order.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import shiroya.orderEvent.OrderRequest;
import shiroya.productEvent.ProductResponse;

import java.math.BigDecimal;

@Service
public class ProductClientService {

    @Autowired
    private RestTemplate restTemplate;

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public ProductResponse checkProduct(OrderRequest request, String token) {

        final String url = "http://localhost:8082/product/validateAndReduce";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, ProductResponse.class);

        return response.getBody() != null ? response.getBody() : new ProductResponse();
    }

    public ProductResponse productFallback(OrderRequest request, String token, Exception ex) {

        System.out.println("🔥 FALLBACK CALLED");

        ProductResponse response = new ProductResponse();
        response.setAvailable(false);
        response.setPrice(BigDecimal.ZERO);
        response.setProductId(request.getProductId());
        response.setName("XYZ");
        response.setQuantity(request.getQuantity());
        return response;
    }
}
