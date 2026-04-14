package shiroya.order.gateWayFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewayValidationFilter extends OncePerRequestFilter {

    private static final String SECRET = "mysecret";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String secret = request.getHeader("X-Gateway-Secret");

        if (!SECRET.equals(secret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Direct access not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
