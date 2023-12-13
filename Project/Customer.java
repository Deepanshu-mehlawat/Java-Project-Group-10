package customer;

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import SQL.basic_sql;

public class Customer extends basic_sql{

    public Customer() throws SQLException {
        super();
    }


    public void add_items_to_cart(int userId, int itemId, int itemQuantity) throws SQLException {

    String query = String.format("SELECT name AS item_name,price FROM items WHERE item_id = %d", itemId);
    ResultSet item_name = selector(query);

    if (item_name.next()) {
        String itemName = item_name.getString("item_name"); 
        int itemPrice = item_name.getInt("price");

        String addToCartQuery = String.format("INSERT INTO cart (user_id, item_id, item_name, item_quantity, price) " +
                "VALUES (%d, %d, '%s', %d, %d)", userId, itemId, itemName, itemQuantity,itemPrice);
        updator(addToCartQuery);

        System.out.println("Item added to the cart.");
    } else {
        System.out.println("Item not found.");
    }
    }
 
    public void show_items_in_cart(int userId) throws SQLException {
    String query = String.format("SELECT * FROM cart WHERE user_id = %d", userId);
    ResultSet cartResultSet = selector(query);
    int totalprice=0;
    
    if (cartResultSet.next()) {
        System.out.println("Items in the cart for User ID " + userId + ":");
        do {
            System.out.println("Item ID: " + cartResultSet.getInt("item_id") +
                    " - Item Name: " + cartResultSet.getString("item_name") +
                    " - Quantity: " + cartResultSet.getInt("item_quantity") +
                    " - Price per piece: " + cartResultSet.getInt("price"));
                    totalprice+=cartResultSet.getInt("item_quantity")*cartResultSet.getInt("price");
        } while (cartResultSet.next());
    System.out.println("\n Total Price of Cart:"+ totalprice);
    } else {
        System.out.println("Cart is empty for User ID " + userId);
    }
    }


    public void postRequest(int userId) throws SQLException {

    String checkCartQuery = String.format("SELECT COUNT(*) AS cart_count FROM cart WHERE user_id = %d", userId);
    ResultSet cartCountResultSet = selector(checkCartQuery);

    int cartCount = 0;
    if (cartCountResultSet.next()) {
        cartCount = cartCountResultSet.getInt("cart_count");
    }

    if (cartCount == 0) {
        System.out.println("Nothing in cart to request!!");
        return; // Exit the function if the cart is empty
    }

    String total_price_query = String.format("SELECT SUM(item_quantity * c.price) AS total_price " +
                                              "FROM cart c " +
                                              "JOIN items i ON c.item_id = i.item_id " +
                                              "WHERE c.user_id = %d", userId);
    ResultSet totalPriceResultSet = selector(total_price_query);

    double totalPrice = 0.0;
    if (totalPriceResultSet.next()) {
        totalPrice = totalPriceResultSet.getDouble("total_price");
    }

    // Step 2: Insert a new request into the requests table
    String insertRequestQuery = String.format("INSERT INTO requests (user_id, total_price) " +
                                              "VALUES (%d, %f) RETURNING request_id", userId, totalPrice);
    ResultSet requestIdResultSet = selector(insertRequestQuery);

    int requestId = 0;
    if (requestIdResultSet.next()) {
        requestId = requestIdResultSet.getInt("request_id");
    }

    // Step 3: Insert items from the user's cart into the request_items table
    String insertRequestItemsQuery = String.format("INSERT INTO request_items (request_id, item_id, quantity, price) " +
                                                   "SELECT %d, c.item_id, c.item_quantity, i.price " +
                                                   "FROM cart c " +
                                                   "JOIN items i ON c.item_id = i.item_id " +
                                                   "WHERE c.user_id = %d", requestId, userId);
    updator(insertRequestItemsQuery);

    // Step 4: Remove items from the user's cart
    String removeItemsFromCartQuery = String.format("DELETE FROM cart WHERE user_id = %d", userId);
    updator(removeItemsFromCartQuery);

    System.out.println("Request posted successfully.");
    }

    public void showRequests(int user_id) throws SQLException {
        String query = String.format("SELECT r.request_id, u.username, r.total_price, i.name AS item_name , r.status,r.request_reply , ri.quantity " +
               "FROM requests r " +
               "JOIN users u ON r.user_id = u.user_id " +
               "JOIN request_items ri ON r.request_id = ri.request_id " +
               "JOIN items i ON ri.item_id = i.item_id " +
               "WHERE r.user_id=%d " +
               "ORDER BY r.request_id, ri.item_id",user_id);
        ResultSet requestsResultSet = selector(query);

        int currentRequestId = -1;  // To track changes in request ID

        while (requestsResultSet.next()) {
            int requestId = requestsResultSet.getInt("request_id");

            // Display request information only when the request ID changes
            if (requestId != currentRequestId) {
                System.out.println(requestsResultSet.getInt("request_id")+
		        ")"+requestsResultSet.getString("username") +
                                   " - Total Price: $" + requestsResultSet.getDouble("total_price"));
            if (requestsResultSet.getInt("status")==0){
            System.out.println("No Replies yet");} else {
            System.out.println("Request's Reply:"+requestsResultSet.getString("request_reply"));}

                currentRequestId = requestId;  // Update current request ID
            }

            // Display item details
            System.out.println("   " + requestsResultSet.getString("item_name") +
                               " x " + requestsResultSet.getInt("quantity")+"\n\n");
        }

        if (currentRequestId == -1) {
            System.out.println("No requests available right now.");
        }
    }   


    public void writeHistory(int user_id) throws SQLException {
        // Step 1: Get user balance
        String getBalanceQuery = String.format("SELECT balance FROM users WHERE user_id = %d", user_id);
        ResultSet balanceResultSet = selector(getBalanceQuery);

        double userBalance = 0.0;
        if (balanceResultSet.next()) {
            userBalance = balanceResultSet.getDouble("balance");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("order_history.txt"))) {
            // Step 2: Write user balance to the file
            writer.write(String.format("User Balance: $%.2f%n%n", userBalance));

            // Step 3: Get order details
            String getOrderDetailsQuery = String.format("SELECT o.order_id, o.total_price, i.name, oi.quantity " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.order_id = oi.order_id " +
                    "JOIN items i ON oi.item_id = i.item_id " +
                    "WHERE o.user_id = %d", user_id);
            ResultSet orderDetailsResultSet = selector(getOrderDetailsQuery);

            while (orderDetailsResultSet.next()) {
                int order_id = orderDetailsResultSet.getInt("order_id");
                double total_price = orderDetailsResultSet.getDouble("total_price");
                String itemName = orderDetailsResultSet.getString("name");
                int quantity = orderDetailsResultSet.getInt("quantity");

                // Write order details to the file
                writer.write(String.format("Order ID: %d%n", order_id));
                writer.write(String.format("Item: %s, Quantity: %d%n", itemName, quantity));
                writer.write(String.format("Total Price: $%.2f%n", total_price));
                writer.write("------------------------------\n");
            }

            System.out.println("Order history written to order_history.txt");
        } catch (IOException e) {
            System.out.println("Error writing order history to file: " + e.getMessage());
        }
    }    


}

