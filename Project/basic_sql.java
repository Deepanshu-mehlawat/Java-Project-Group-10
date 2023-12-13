package SQL;

import java.sql.*;
import java.util.Scanner;

public class basic_sql{
    String url;
    String user;
    String password;
    Connection connection;

    public basic_sql() throws SQLException {
        url = "jdbc:postgresql://localhost:5432/postgres";
        user = "postgres";
        password = "9999247971";

        connection = DriverManager.getConnection(url, user, password);
    }

    public ResultSet selector(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet temp = statement.executeQuery(query);
        return temp;
    }

    public void updator(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
    }

    public void print_all_items() throws SQLException {
        String query = "SELECT * FROM items";
        ResultSet resultSet = selector(query);

        System.out.println("Items in Stock:");
        System.out.println("ID\tName");

        while (resultSet.next()) {
            int itemId = resultSet.getInt("item_id");
            String itemName = resultSet.getString("name");
            double itemPrice = resultSet.getDouble("price");
            int itemQuantity = resultSet.getInt("quantity");

            System.out.println(itemId + "\t" + itemName + "(Price Per Piece:" + itemPrice + " and items available:" + itemQuantity+")");
        }

        resultSet.close();
    }

    public void search_an_item(String searchString) throws SQLException {
        String query = "SELECT * FROM items WHERE name LIKE '%" + searchString + "%'";
        ResultSet resultSet = selector(query);

        System.out.println("Search results for items containing '" + searchString + "':");
        System.out.println("ID\tName");

        while (resultSet.next()) {
            int itemId = resultSet.getInt("item_id");
            String itemName = resultSet.getString("name");
            double itemPrice = resultSet.getDouble("price");
            int itemQuantity = resultSet.getInt("quantity");

            System.out.println(itemId + "\t" + itemName +  "(Price Per Piece:" + itemPrice + " and items available:" + itemQuantity+")");
        }

        resultSet.close();
    }

    public int authenticate(String username,String password) throws SQLException {
    String query=String.format("Select user_id from users where username='%s' and password='%s' ",username,password);
    ResultSet res= selector(query);
    int id=0;
    if (res.next()){
    id=res.getInt("user_id");
    } 
    return (id);
    }

    public void signup(String username,String password) throws SQLException {
    String query=String.format("insert into users(username,password) values('%s','%s');",username,password);
    updator(query);
    }

}
