package shopkeeper;

import java.sql.*;
import java.util.Scanner;
import SQL.basic_sql;

public class ShopKeeper extends basic_sql{

    public ShopKeeper() throws SQLException {
        super();
    }

    public void add_item_to_db(String name,double price,int quantity) throws SQLException{
        String query=String.format("Insert into items(name,price,quantity) values('%s',%f,%d)",name,price,quantity);
        updator(query);
        System.out.println("Added "+quantity+" "+name+" to the stock");
    }


    public void update_item_price(int itemId, double newPrice) throws SQLException {
        String query = String.format("UPDATE items SET price = %f WHERE item_id = %d", newPrice, itemId);
        updator(query);
        System.out.println("Updated price for item with ID " + itemId + " to " + newPrice);
    }


    public void update_item_quantity(int itemId, int newQuantity) throws SQLException {
        String query = String.format("UPDATE items SET quantity = %d WHERE item_id = %d", newQuantity, itemId);
        updator(query);
        System.out.println("Updated quantity for item with ID " + itemId + " to " + newQuantity);
    }
    

        public void showRequests() throws SQLException {
        String query = "SELECT r.request_id, u.username, r.total_price, i.name AS item_name, ri.quantity " +
               "FROM requests r " +
               "JOIN users u ON r.user_id = u.user_id " +
               "JOIN request_items ri ON r.request_id = ri.request_id " +
               "JOIN items i ON ri.item_id = i.item_id " +
               "WHERE r.status = 0 " +
               "ORDER BY r.request_id, ri.item_id";
        ResultSet requestsResultSet = selector(query);

        int currentRequestId = -1;  // To track changes in request ID

        while (requestsResultSet.next()) {
            int requestId = requestsResultSet.getInt("request_id");

            // Display request information only when the request ID changes
            if (requestId != currentRequestId) {
                System.out.println(requestsResultSet.getInt("request_id")+
		        ")"+requestsResultSet.getString("username") +
                                   " - Total Price: $" + requestsResultSet.getDouble("total_price"));

                currentRequestId = requestId;  // Update current request ID
            }

            // Display item details
            System.out.println("   " + requestsResultSet.getString("item_name") +
                               " x " + requestsResultSet.getInt("quantity"));
        }

        if (currentRequestId == -1) {
            System.out.println("No requests available right now.");
        }
    }

    public int showRequests(int responseStatus) throws SQLException {
    String query = String.format("SELECT r.request_id, u.username, r.total_price, r.request_reply, i.name AS item_name, ri.quantity " +
            "FROM requests r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "JOIN request_items ri ON r.request_id = ri.request_id " +
            "JOIN items i ON ri.item_id = i.item_id " +
            "WHERE r.status = %d ORDER BY r.request_id, ri.item_id",responseStatus);
    ResultSet requestsResultSet = selector(query);

    int currentRequestId = -1;  // To track changes in request ID

    while (requestsResultSet.next()) {
        int requestId = requestsResultSet.getInt("request_id");

        System.out.println(requestsResultSet.getInt("request_id") +
                    ")" + requestsResultSet.getString("username") +
                    " - Total Price: $" + requestsResultSet.getDouble("total_price"));

        System.out.println("   Request Reply: " + requestsResultSet.getString("request_reply"));
            
        currentRequestId = requestId;  // Update current request ID

        System.out.println("   " + requestsResultSet.getString("item_name") +
                " x " + requestsResultSet.getInt("quantity"));
    }

    if (currentRequestId == -1) {
        System.out.println("No requests available right now.");
        return -1;
    }
    return 1;
    }


    public void giveResponse(int responseId) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for a response
        System.out.println("Enter your response for Request ID " + responseId + ":");
        String response = scanner.nextLine();

        // Update the corresponding request's status and reply in the requests table
        String updateRequestQuery = String.format("UPDATE requests SET status = 1, request_reply = '%s' WHERE request_id = %d", response, responseId);
        updator(updateRequestQuery);

        System.out.println("Response added successfully.");
    }

    
    public void makePayment(int requestId, double paidAmount) throws SQLException {
    // Step 1: Get request details
    String getRequestDetailsQuery = String.format("SELECT r.user_id, r.total_price " +
            "FROM requests r " +
            "WHERE r.request_id = %d", requestId);
    ResultSet requestDetailsResultSet = selector(getRequestDetailsQuery);

    if (!requestDetailsResultSet.next()) {
        System.out.println("Request not found.");
        return;
    }

    int userId = requestDetailsResultSet.getInt("user_id");
    double totalAmount = requestDetailsResultSet.getDouble("total_price");

    // Step 2: Insert order details into orders table
    String insertOrderQuery = String.format("INSERT INTO orders (user_id, total_price) " +
            "VALUES (%d, %f);", userId, totalAmount);
    updator(insertOrderQuery);

    // Step 3: Insert order items into order_items table
    String insertOrderItemsQuery = String.format("INSERT INTO order_items (order_id, item_id, quantity, price) " +
            "SELECT o.order_id, ri.item_id, ri.quantity, i.price " +
            "FROM request_items ri " +
            "JOIN items i ON ri.item_id = i.item_id " +
            "JOIN orders o ON o.user_id = %d " +  // Use the user_id from the inserted order
            "WHERE ri.request_id = %d order by o.order_id limit 1", userId, requestId);
    updator(insertOrderItemsQuery);
   
    String updateStockQuery = String.format("UPDATE items AS i " +
        "SET quantity = i.quantity - ri.quantity " +
        "FROM request_items ri " +
        "WHERE i.item_id = ri.item_id AND ri.request_id = %d", requestId);
    updator(updateStockQuery);


    double remainingAmount = totalAmount - paidAmount;
    String updateBalanceQuery = String.format("UPDATE users SET balance = balance + %f WHERE user_id = %d", remainingAmount, userId);
    updator(updateBalanceQuery);

    String deleteRequestQuery = String.format("DELETE FROM requests WHERE request_id = %d", requestId);
    updator(deleteRequestQuery);

    String deleteRequestItemsQuery = String.format("DELETE FROM request_items WHERE request_id = %d", requestId);
    updator(deleteRequestItemsQuery);

    System.out.println("Payment successful.");
    }




}
