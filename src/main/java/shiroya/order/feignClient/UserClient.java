package shiroya.order.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import shiroya.userEvent.UserDtoFeing;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

    @GetMapping("/users/id/{userId}")
    UserDtoFeing getUser(@PathVariable String userId,
                         @RequestHeader("Authorization") String token
                        );
}