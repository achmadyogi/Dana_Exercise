package com.warehouse.controller;

import com.warehouse.model.Item;
import com.warehouse.model.MessageStatus;
import com.warehouse.service.AsyncService;
import com.warehouse.service.JDBCService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;

@RestController
@MapperScan("com.warehouse.service")
public class ItemController {

    @Autowired
    AsyncService asyncService;

    @Autowired
    JDBCService jdbcService;

    private static Reader reader;
    private static SqlSessionFactory sqlSessionFactory;
    private static SqlSession session;

    @PostConstruct
    public void init() throws Exception {
        reader = Resources.getResourceAsReader("itemConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        session = sqlSessionFactory.openSession();
    }

    public void refresh() throws Exception{
        session.close();

        reader = Resources.getResourceAsReader("itemConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        session = sqlSessionFactory.openSession();

        session.getConfiguration().addMapper(JDBCService.class);
        jdbcService = session.getMapper(JDBCService.class);
    }

    @GetMapping("/item")
    public ResponseEntity<?> geAllItems() throws Exception {
        refresh();
        return new ResponseEntity<>(jdbcService.getAllItems(), HttpStatus.OK);
    }

    @GetMapping("/item/{id_item}")
    public ResponseEntity<?> getAnItem(@PathVariable("id_item") int id_item) throws Exception{
        refresh();
        return new ResponseEntity<>(jdbcService.getAnItem(id_item), HttpStatus.OK);
    }

    @PostMapping("/item")
    public ResponseEntity<?> addAnItem(@RequestBody Item item) throws Exception {
        refresh();
        jdbcService.addAnItem(item);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An item has successfully been added."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/item")
    public ResponseEntity<?> editAnItem(@RequestBody Item item) throws Exception {
        refresh();
        jdbcService.editAnItem(item);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An item has successfully been edited"), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/item/{id_item}")
    public ResponseEntity<?> deleteAnItem(@PathVariable("id_item") int id_item) throws Exception {
        refresh();
        jdbcService.deleteAnItem(id_item);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An item has successfully been deleted."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{id_item}/employee/{id_employee}/assign")
    public ResponseEntity<?> assignAnItem(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception {
        refresh();
        // Check if employee exists
        if(jdbcService.getAnEmployee(id_employee) == null){
            return new ResponseEntity<>(new MessageStatus("The employee does not exist."), HttpStatus.EXPECTATION_FAILED);
        }
        // Check if item exists
        if(jdbcService.getAnItem(id_item) == null){
            return new ResponseEntity<>(new MessageStatus("The item does not exist."), HttpStatus.EXPECTATION_FAILED);
        }
        // Check if item amount is enough
        if(jdbcService.getAnItem(id_item).getAmount() < 1){
            return new ResponseEntity<>(new MessageStatus("The item is not enough."), HttpStatus.EXPECTATION_FAILED);
        }

        // Check if employee has already borrow the item
        if(jdbcService.checkAssignation(id_employee, id_item) > 0){
            return new ResponseEntity<>(new MessageStatus("The employee is still borrowing this item."), HttpStatus.EXPECTATION_FAILED);
        }

        // Assign the item
        CompletableFuture<MessageStatus> m1 = asyncService.itemAssignEmployee(id_employee, id_item);
        CompletableFuture<MessageStatus> m2 = asyncService.itemDecreaseItemAmount(id_item);

        CompletableFuture.allOf(m1, m2).join();

        return new ResponseEntity<>(new MessageStatus("The assignation completed."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{id_item}/employee/{id_employee}/async/assign")
    public ResponseEntity<?> asyncItemAssignEmployee(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception {
        refresh();
        jdbcService.assignedAnItem(id_employee, id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/item/{id_item}/async/item-decrease-item-amount")
    public ResponseEntity<?> asyncItemDecreaseItemAmount(@PathVariable("id_item") int id_item) throws Exception {
        refresh();
        jdbcService.reduceItemAmount(id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/item/{id_item}/employee/{id_employee}/return")
    public ResponseEntity<?> returnAnItem(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception {
        refresh();
        // Check if employee is still borrowing the item
        if(jdbcService.checkAssignation(id_employee, id_item) < 1){
            return new ResponseEntity<>(new MessageStatus("The employee does not borrow this item. There is nothing to return"), HttpStatus.EXPECTATION_FAILED);
        }

        CompletableFuture<MessageStatus> m1 = asyncService.itemReturnedByEmployee(id_employee, id_item);
        CompletableFuture<MessageStatus> m2 = asyncService.itemIncreaseItemAmount(id_item);

        CompletableFuture.allOf(m1, m2).join();

        return new ResponseEntity<>(new MessageStatus("The item is returned successfully."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{id_item}/employee/{id_employee}/async/return")
    public ResponseEntity<?> asyncItemReturnEmployee(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception {
        refresh();
        jdbcService.returnAnItem(id_employee, id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/item/{id_item}/async/item-increase-item-amount")
    public ResponseEntity<?> asyncItemIncreaseItemAmount(@PathVariable("id_item") int id_item) throws Exception {
        refresh();
        jdbcService.increaseItemAmount(id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/item/{id_item}/employee")
    public ResponseEntity<?> borrower(@PathVariable("id_item") int id_item) throws Exception {
        refresh();
        Item item = jdbcService.getAnItem(id_item);
        item.setEmployees(jdbcService.borrowers(id_item));
        return new ResponseEntity<>(item, HttpStatus.OK);
    }
}
