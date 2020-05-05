package com.warehouse.service;

import com.warehouse.model.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {
    private static Logger log = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> employeeAssignItem(int id_employe, int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_employee",""+id_employe);
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/employee/{id_employee}/item/{id_item}/async/assign",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("The assignation completed."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> employeeIncreaseItemAmount(int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/async/employee-increase-item-amount",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Item is going back to warehouse."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> employeeReturnItem(int id_employe, int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_employee",""+id_employe);
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/employee/{id_employee}/item/{id_item}/async/return",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Return completed."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> employeeDecreaseItemAmount(int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/async/employee-decrease-item-amount",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Item has been sent from warehouse."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> itemAssignEmployee(int id_employe, int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_employee",""+id_employe);
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/employee/{id_employee}/async/assign",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("The assignation completed."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> itemIncreaseItemAmount(int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/async/item-increase-item-amount",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Item is going back to warehouse."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> itemReturnedByEmployee(int id_employe, int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_employee",""+id_employe);
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/employee/{id_employee}/async/return",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Return completed."));
    }

    @Async("asyncExecutor")
    public CompletableFuture<MessageStatus> itemDecreaseItemAmount(int id_item){

        Map<String, String> params = new HashMap<>();
        params.put("id_item",""+id_item);

        restTemplate.put("http://localhost:8080/item/{id_item}/async/item-decrease-item-amount",null,params);

        return CompletableFuture.completedFuture(new MessageStatus("Item has been sent from warehouse."));
    }
}
