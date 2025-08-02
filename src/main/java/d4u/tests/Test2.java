package d4u.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Test2 {

    public List<String> getBookingNames() {
        List<String> bookingNames = new ArrayList<>();
        
        String host = "ec2-3-217-249-217.compute-1.amazonaws.com"; 
        String port = "5432"; // Default PostgreSQL port
        String databaseName = "dfjs7psaki4b4p"; 
        String username = "D4Uapp";  
        String password = "pf358c850bd02bbff983c98f48715044e6805ee43e7cf75365ec2708e998f0c3a";  

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, databaseName);
        String query = "SELECT offer.offer_sfid AS OFFER_ID, psoffer.name AS NAME FROM salesforce.gpl_cs_balance_details offer INNER JOIN salesforce.nv_eoi_form eoi on(offer.offer_sfid = eoi.offersfid) INNER JOIN salesforce.propstrength__offer__c psoffer on(offer.offer_sfid = psoffer.sfid and psoffer.PropStrength__Status__c = 'Closed Won') WHERE offer.offer_sfid IS NOT NULL AND (offer.status IS NULL OR offer.status != 'COMPLETED')";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // If ResultSet is empty
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No data available.");
            }

            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                bookingNames.add(name); // Add name to the list
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return bookingNames;
    }
    public static void main(String[] args) {
        Test2 test = new Test2();
        List<String> bookingNames = test.getBookingNames();

        for (String name : bookingNames) {
            System.out.println("Booking Name: " + name);
        }
    }
}
