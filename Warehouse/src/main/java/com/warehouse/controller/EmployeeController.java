package com.warehouse.controller;

import com.warehouse.model.Employee;
import com.warehouse.model.MessageStatus;
import com.warehouse.service.AsyncService;
import com.warehouse.service.JDBCService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;

@RestController
@MapperScan("com.warehouse.service")
public class EmployeeController {

    @Autowired
    AsyncService asyncService;

    @Autowired
    JDBCService jdbcService;

    private static Reader reader;
    private static SqlSessionFactory sqlSessionFactory;
    private static SqlSession session;

    @PostConstruct
    public void init() throws Exception {
        reader = Resources.getResourceAsReader("employeeConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        session = sqlSessionFactory.openSession();
    }

    public void refresh() throws Exception{
        session.close();

        reader = Resources.getResourceAsReader("employeeConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        session = sqlSessionFactory.openSession();

        session.getConfiguration().addMapper(JDBCService.class);
        jdbcService = session.getMapper(JDBCService.class);
    }

    @GetMapping("/employee")
    public ResponseEntity<?> getAllEmployee() throws Exception {
        refresh();
        return new ResponseEntity<>(jdbcService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/employee/{id_employee}")
    public ResponseEntity<?> getAnEmployee(@PathVariable("id_employee") int id_employee) throws Exception {
        refresh();
        return new ResponseEntity<>(jdbcService.getAnEmployee(id_employee), HttpStatus.OK);
    }

    @PutMapping("/employee")
    public ResponseEntity<?> editAnEmployee(@RequestBody Employee employee) throws Exception {
        refresh();
        jdbcService.editEmployee(employee);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An employee has been updated"), HttpStatus.ACCEPTED);
    }

    @PostMapping("/employee")
    public ResponseEntity<?> addAnEmployee(@RequestBody Employee employee) throws Exception {
        refresh();
        jdbcService.addEmployee(employee);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An employee has been added"), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/employee/{id_employee}")
    public ResponseEntity<?> deleteAnEmployee(@PathVariable("id_employee") int id_employee) throws Exception {
        refresh();
        jdbcService.deleteEmployee(id_employee);
        session.commit();
        return new ResponseEntity<>(new MessageStatus("An employee has successfully been deleted"), HttpStatus.OK);
    }

    @PutMapping("/employee/{id_employee}/item/{id_item}/assign")
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
            return new ResponseEntity<>(new MessageStatus("You still have the same item at hand."), HttpStatus.EXPECTATION_FAILED);
        }

        CompletableFuture<MessageStatus> m1 = asyncService.employeeAssignItem(id_employee, id_item);
        CompletableFuture<MessageStatus> m2 = asyncService.employeeDecreaseItemAmount(id_item);

        CompletableFuture.allOf(m1,m2).join();

        return new ResponseEntity<>(new MessageStatus("The assignation completed."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/employee/{id_employee}/item/{id_item}/async/assign")
    public ResponseEntity<?> asyncEmployeeAssignItem(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception{
        refresh();
        jdbcService.assignedAnItem(id_employee, id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/item/{id_item}/async/employee-decrease-item-amount")
    public ResponseEntity<?> asyncEmployeeDecreaseItem(@PathVariable("id_item") int id_item) throws Exception{
        refresh();
        jdbcService.reduceItemAmount(id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/employee/{id_employee}/item/{id_item}/return")
    public ResponseEntity<?> returnAnItem(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception {
        refresh();
        // Check if employee is still borrowing the item
        if(jdbcService.checkAssignation(id_employee, id_item) < 1){
            return new ResponseEntity<>(new MessageStatus("You do not borrow the item. There is nothing to return"), HttpStatus.EXPECTATION_FAILED);
        }

        CompletableFuture<MessageStatus> m1 = asyncService.employeeReturnItem(id_employee, id_item);
        CompletableFuture<MessageStatus> m2 = asyncService.employeeIncreaseItemAmount(id_item);

        CompletableFuture.allOf(m1, m2).join();

        return new ResponseEntity<>(new MessageStatus("The item is returned successfully."), HttpStatus.ACCEPTED);
    }

    @PutMapping("/employee/{id_employee}/item/{id_item}/async/return")
    public ResponseEntity<?> asyncEmployeeReturnItem(@PathVariable("id_employee") int id_employee, @PathVariable("id_item") int id_item) throws Exception{
        refresh();
        jdbcService.returnAnItem(id_employee, id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/item/{id_item}/async/employee-increase-item-amount")
    public ResponseEntity<?> asyncEmployeeIncreaseItem(@PathVariable("id_item") int id_item) throws Exception{
        refresh();
        jdbcService.increaseItemAmount(id_item);
        session.commit();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/employee/{id_employee}/item")
    public ResponseEntity<?> borrowedItems(@PathVariable("id_employee") int id_employee) throws Exception {
        refresh();
        Employee employee = jdbcService.getAnEmployee(id_employee);
        employee.setItems(jdbcService.borrowedItems(id_employee));
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }
}
