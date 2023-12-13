import SQL.basic_sql;
import java.sql.*;
import shopkeeper.ShopKeeper;
import customer.Customer;
import java.util.Scanner;

class Main {
    public static void main(String args[]) {
        try {

        basic_sql db = new basic_sql();
        ShopKeeper admin = new ShopKeeper();
        Customer cust= new Customer();
        Scanner sc = new Scanner(System.in);

        System.out.println("\n\n\t\tWELCOME TO APNA BAZAAR!!\n\n");

        start_menu(db,admin,cust);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void start_menu(basic_sql db,ShopKeeper admin,Customer cust) throws SQLException {
        Scanner sc = new Scanner(System.in);
        boolean exitRequested = false;

        while (!exitRequested) {
            System.out.println("1) Login\n2) Signup\n3) Exit\n");
            System.out.print("Choose any one to continue:");

            int choice = sc.nextInt();
            sc.nextLine();  // Consume the newline character

            switch (choice) {
                case 1:
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();

                    int user_id=db.authenticate(username, password);

                    if (user_id!=0) {
                        if(user_id!=1){
                        customerMenu(user_id,db,cust,sc); } else{
                        adminMenu(db,admin,sc);}
                    } else {
                        System.out.println("Invalid username or password. try again \n");
                    }
                    break;

                case 2:
                    System.out.print("Enter username: ");
                    String newUsername = sc.nextLine();
                    System.out.print("Enter password: ");
                    String newPassword = sc.nextLine();

                    db.signup(newUsername, newPassword);
                    System.out.println("Signup successful. Please log in with your new credentials.\n");
                    break;

                case 3:
                    exitRequested = true;
                    break;

                default:
                    System.out.println("Invalid choice. Please choose again.");
            }
        }
    }


    private static void customerMenu(int userId, basic_sql db, Customer cust, Scanner sc) throws SQLException {

        while (true) {
            System.out.println("Customer Menu:");
            System.out.println("1) See all available items");
            System.out.println("2) Search Specific Item");
            System.out.println("3) View Cart");
            System.out.println("4) Show Responses");
            System.out.println("5) Print order history");
            System.out.println("6) Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    db.print_all_items();
                    System.out.print("Do you want to buy anything? (yes/no): ");
                    String buyChoice = sc.nextLine().toLowerCase();

                    if (buyChoice.equals("yes")) {
                        System.out.print("Enter the item_id you want to buy: ");
                        int itemId = sc.nextInt();
                        System.out.print("Enter the item_quantity you want to buy: ");
                        int itemQuantity = sc.nextInt();

                        cust.add_items_to_cart(userId, itemId, itemQuantity);
                    }
                    break;

                case 2:
                    System.out.print("Enter the item you want to search: ");
                    String searchString = sc.nextLine();
                    db.search_an_item(searchString);

                    System.out.print("Do you want to buy anything? (yes/no): ");
                    buyChoice = sc.nextLine().toLowerCase();

                    if (buyChoice.equals("yes")) {
                        System.out.print("Enter the item_id you want to buy: ");
                        int itemId = sc.nextInt();
                        System.out.print("Enter the item_quantity you want to buy: ");
                        int itemQuantity = sc.nextInt();

                        cust.add_items_to_cart(userId, itemId, itemQuantity);
                    }
                    break;

                case 3:
                    cust.show_items_in_cart(userId);

                    System.out.print("Do you want to make an order request for items in your cart? (yes/no): ");
                    String orderChoice = sc.nextLine().toLowerCase();

                    if (orderChoice.equals("yes")) {
                        cust.postRequest(userId);
                    }
                    break;

                case 4:
                    cust.showRequests(userId);
                    break;

                case 5:
                    cust.writeHistory(userId);
                    break;

                case 6:
                    System.out.println("Exiting the customer menu.");
                    return;

                default:
                    System.out.println("Invalid choice. Please choose again.");
            }
        }
    }


    private static void adminMenu(basic_sql db, ShopKeeper admin, Scanner sc) throws SQLException {
    while (true) {
        System.out.println("Admin Menu:");
        System.out.println("1) See all available items");
        System.out.println("2) Search for an item");
        System.out.println("3) Handle order requests");
        System.out.println("4) Make payment");
        System.out.println("5) Exit");
        System.out.print("Choose an option: ");

        int choice = sc.nextInt();
        sc.nextLine(); // Consume the newline character

        switch (choice) {
            case 1:
                db.print_all_items();
                System.out.println("\nChoose any operation:");
                System.out.println("1)Add a new item");
                System.out.println("2)Update Price of an item");
                System.out.println("3)Update Stock of an item");
                System.out.println("4)Exit");
                System.out.print("Enter your Choice:");
                int itemchoice = sc.nextInt();
                sc.nextLine();
	   switch (itemchoice){
                case 1:
                    System.out.print("Enter the name of the new item: ");
                    String itemName = sc.nextLine();
                    System.out.print("Enter the price of the new item: ");
                    double itemPrice = sc.nextDouble();
                    System.out.print("Enter the quantity of the new item: ");
                    int itemQuantity = sc.nextInt();

                    admin.add_item_to_db(itemName, itemPrice, itemQuantity);
                break;
                 
                case 2:
                    System.out.print("Enter the item_id of the item you want to update: ");
                    int itemId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter the new price for the item: ");
                    double newPrice = sc.nextDouble();
                    admin.update_item_price(itemId, newPrice);
                    break;
 
                case 3:
                     System.out.print("Enter the item_id of the item you want to update: ");
                    itemId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter the new stock quantity for the item: ");
                    int newStock = sc.nextInt();
                    admin.update_item_quantity(itemId, newStock);
                    break; 
                case 4:
                    return; 
                default:
                    System.out.println("Enter Valid option\ngoing back to main menu\n")  ;                              
                }

            case 2:
                System.out.print("Enter the item you want to search: ");
                String searchString = sc.nextLine();
                db.search_an_item(searchString);
                System.out.println("\nChoose any operation:");
                System.out.println("1)Add a new item");
                System.out.println("2)Update Price of an item");
                System.out.println("3)Update Stock of an item");
                System.out.println("4)Exit");
                System.out.print("Enter your Choice:");
                itemchoice = sc.nextInt();
                sc.nextLine();
	   switch (itemchoice){
                case 1:
                    System.out.print("Enter the name of the new item: ");
                    String itemName = sc.nextLine();
                    System.out.print("Enter the price of the new item: ");
                    double itemPrice = sc.nextDouble();
                    System.out.print("Enter the quantity of the new item: ");
                    int itemQuantity = sc.nextInt();

                    admin.add_item_to_db(itemName, itemPrice, itemQuantity);
                break;
                 
                case 2:
                    System.out.print("Enter the item_id of the item you want to update: ");
                    int itemId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter the new price for the item: ");
                    Double newPrice = sc.nextDouble();
                    admin.update_item_price(itemId, newPrice);
                    break;
 
                case 3:
                     System.out.print("Enter the item_id of the item you want to update: ");
                    itemId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter the new stock quantity for the item: ");
                    int newStock = sc.nextInt();
                    admin.update_item_quantity(itemId, newStock);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Enter Valid option\ngoing back to main menu\n")  ;                                
                }
               
                break;

            case 3:
                admin.showRequests();
                System.out.print("Do you want to handle any request? (yes/no): ");
                String handleChoice = sc.nextLine().toLowerCase();

                if (handleChoice.equals("yes")) {
                    System.out.print("Enter the request_id you want to handle: ");
                    int requestId = sc.nextInt();
                    admin.giveResponse(requestId);
                }
                break;

            case 4:
                int a=admin.showRequests(1);
                if (a!=-1){
                System.out.print("Enter the request_id for which you want to finalize payment: ");
                int paymentRequestId = sc.nextInt();
                sc.nextLine(); // Consume the newline character

                System.out.print("Enter the amount paid by the user: ");
                double amountPaid = sc.nextDouble();

                admin.makePayment(paymentRequestId, amountPaid);
                break;} else {break;}

            case 5:
                System.out.println("Exiting the admin menu.");
                return;

            default:
                System.out.println("Invalid choice. Please choose again.");
        }
    }
 }
}
