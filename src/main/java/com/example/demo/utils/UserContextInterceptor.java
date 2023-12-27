package com.example.demo.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Intercepta toda request http que se realice desde este microservicio, y le inyectamos los headers que necesitamos. En
 * nuestro ejemplo, el Correlation ID.
 * 
 * Tambien requiere que lo agreguemos en la configuración, donde creamos el Bean RestTemplate. En este proyecto
 * está en la clase Main.
 */
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);
	
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add(UserContext.CORRELATION_ID, UserContextHolder.getContext().getCorrelationId());
        headers.add(UserContext.AUTH_TOKEN, UserContextHolder.getContext().getAuthToken());
        
        logger.debug("UserContextInterceptor en LICENSING SERVICE Correlation id: {}", UserContextHolder.getContext().getCorrelationId());

        return execution.execute(request, body);
    }
}
