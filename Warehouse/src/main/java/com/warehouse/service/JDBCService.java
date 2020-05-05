package com.warehouse.service;

import com.warehouse.model.Employee;
import com.warehouse.model.Item;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface JDBCService {

    final String getAllEmployees = "SELECT * FROM employee";
    final String getAnEmployee = "SELECT * FROM employee WHERE id_employee = #{id_employee}";
    final String addEmployee = "INSERT INTO employee (employee_name) VALUES ( #{employee_name} )";
    final String editEmployee = "UPDATE employee SET name = #{employee_name} WHERE id_employee = #{id_employee} ";
    final String deleteEmployee = "DELETE FROM employee WHERE id_employee = #{id_employee}";
    final String assignedAnItem = "INSERT INTO holder (id_employee, id_item) VALUES ( #{id_employee} , #{id_item} )";
    final String returnAnItem = "UPDATE holder SET is_finished = 1 WHERE id_employee = #{id_employee} AND id_item = #{id_item} AND is_finished = 0";
    final String checkAssignation = "SELECT COUNT(*) AS amount FROM holder WHERE id_employee = #{id_employee} AND id_item = #{id_item} AND is_finished = 0";
    final String getAllItems = "SELECT * FROM item";
    final String getAnItem = "SELECT * FROM item WHERE id_item = #{id_item}";
    final String addAnItem = "INSERT INTO item (item_name, amount) VALUES ( #{item_name}, #{amount} )";
    final String editAnItem = "UPDATE item SET item_name = #{item_name}, amount = #{amount} WHERE id_item = #{id_item}";
    final String deleteAnItem = "DELETE FROM item WHERE id_item = #{id_item}";
    final String borrowers = "SELECT holder.id_employee, employee_name FROM holder LEFT JOIN employee ON employee.id_employee = holder.id_employee WHERE id_item = #{id_item} AND is_finished = 0";
    final String borrowedItems = "SELECT holder.id_item, item_name, '1' AS amount FROM holder LEFT JOIN item ON item.id_item = holder.id_item WHERE id_employee = #{id_employee} AND is_finished = 0";
    final String increaseItemAmount = "UPDATE item SET amount = amount + 1 WHERE id_item = #{id_item}";
    final String reduceItemAmount = "UPDATE item SET amount = amount - 1 WHERE id_item = #{id_item}";

    @Update(increaseItemAmount)
    void increaseItemAmount(@Param("id_item") int id_item);

    @Update(reduceItemAmount)
    void reduceItemAmount(@Param("id_item") int id_item);

    @Select(getAllEmployees)
    @Results(value = {
            @Result(property = "id_employee", column = "id_employee"),
            @Result(property = "employee_name", column = "employee_name")
    })
    List<Employee> getAllEmployees();

    @Select(getAnEmployee)
    @Results(value = {
            @Result(property = "id_employee", column = "id_employee"),
            @Result(property = "employee_name", column = "employee_name")
    })
    Employee getAnEmployee(@Param("id_employee") int id_employee);

    @Insert(addEmployee)
    void addEmployee(Employee employee);

    @Update(editEmployee)
    void editEmployee(Employee employee);

    @Delete(deleteEmployee)
    void deleteEmployee(@Param("id_employee") int id_employee);

    @Insert(assignedAnItem)
    void assignedAnItem(@Param("id_employee") int id_employee, @Param("id_item") int id_item);

    @Update(returnAnItem)
    void returnAnItem(@Param("id_employee") int id_employee, @Param("id_item") int id_item);

    @Select(checkAssignation)
    @Results(value = {
            @Result(column = "amount")
    })
    int checkAssignation(@Param("id_employee") int id_employee, @Param("id_item") int id_item);

    @Select(getAllItems)
    @Results(value = {
            @Result(property = "id_item", column = "id_item"),
            @Result(property = "item_name", column = "item_name"),
            @Result(property = "amount", column = "amount")
    })
    List<Item> getAllItems();

    @Select(getAnItem)
    @Results(value = {
            @Result(property = "id_item", column = "id_item"),
            @Result(property = "item_name", column = "item_name"),
            @Result(property = "amount", column = "amount")
    })
    Item getAnItem(@Param("id_item") int id_item);

    @Insert(addAnItem)
    void addAnItem(Item item);

    @Update(editAnItem)
    void editAnItem(Item item);

    @Delete(deleteAnItem)
    void deleteAnItem(@Param("id_item") int id_item);

    @Select(borrowers)
    @Results(value = {
            @Result(property = "id_employee", column = "id_employee"),
            @Result(property = "employee_name", column = "employee_name")
    })
    List<Employee> borrowers(@Param("id_item") int id_item);

    @Select(borrowedItems)
    @Results(value = {
            @Result(property = "id_item", column = "id_item"),
            @Result(property = "item_name", column = "item_name"),
            @Result(property = "amount", column = "amount")
    })
    List<Item> borrowedItems(@Param("id_employee") int id_employee);
}
