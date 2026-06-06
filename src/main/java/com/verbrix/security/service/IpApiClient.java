package com.verbrix.security.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

@HttpExchange("https://ip-api.com")
public interface IpApiClient {

    @GetExchange("/json/{ip}")
    Map<String, Object> lookup(@PathVariable String ip);
}
