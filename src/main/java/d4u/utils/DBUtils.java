package d4u.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class DBUtils {

    // Database connection details
    private static final String HOST = "ec2-3-217-249-217.compute-1.amazonaws.com";
    private static final String PORT = "5432";
    private static final String DATABASE_NAME = "dfjs7psaki4b4p";
    private static final String USERNAME = "D4Uapp";
    private static final String PASSWORD = "pf358c850bd02bbff983c98f48715044e6805ee43e7cf75365ec2708e998f0c3a";

    // JDBC URL
    private static final String JDBC_URL = String.format("jdbc:postgresql://%s:%s/%s", HOST, PORT, DATABASE_NAME);

    // Method to make API call and fetch project SFIDs
    private static List<String> getProjectSFIDsFromAPI() {
        List<String> sfidList = new ArrayList<>();

        String apiUrl = "https://d4u.godrejproperties.com/d4uplus/rpa/getMappingValueWise?value=suryakant.rupnar@godrejproperties.com";

        // Make API call	
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get(apiUrl);

        if (response.statusCode() == 200) {
            // Extract SFIDs from the API response
            List<Map<String, Object>> resultList = response.jsonPath().getList("result");
            for (Map<String, Object> result : resultList) {
                String projectSfid = result.get("projectSfid").toString();
                sfidList.add(projectSfid);
            }
            System.out.println("Fetched SFIDs: " + sfidList);
        } else {
            System.err.println("Failed to fetch SFIDs. API Response: " + response.statusLine());
        }
        return sfidList;
    }

    // Method to fetch offer and contact details using SFIDs from the API
    public static Map<String, List<String>> getOfferAndContactDetails() {
        Map<String, List<String>> offerToSFIDsMap = new HashMap<>();

        // Fetch SFIDs from API
        List<String> sfids = getProjectSFIDsFromAPI();
        if (sfids.isEmpty()) {
            System.err.println("No SFIDs found. Exiting...");
            return offerToSFIDsMap;
        }

        // Construct the SQL query using fetched SFIDs
        String sfidInClause = String.join("', '", sfids);
        String query = "SELECT \r\n"
                + "    offer.offer_sfid AS OFFER_ID,\r\n"
                + "    offer.status AS STATUS,\r\n"
                + "    offer.booking_id AS BOOKING_ID,\r\n"
                + "    psoffer.name AS OFFER_NAME,\r\n"
                + "    app.enquiry_id,\r\n"
                + "    c.sfid AS CONTACT_SFID,\r\n"
                + "    CASE \r\n"
                + "        WHEN app.applicanttype = '1' THEN '1st Applicant'\r\n"
                + "        WHEN app.applicanttype = '2' THEN '2nd Applicant'\r\n"
                + "        WHEN app.applicanttype = '3' THEN '3rd Applicant'\r\n"
                + "        WHEN app.applicanttype = '4' THEN '4th Applicant'\r\n"
                + "        WHEN app.applicanttype = '5' THEN '5th Applicant'\r\n"
                + "        ELSE '' \r\n"
                + "    END AS applicanttype,\r\n"
                + "    c.firstname AS first_name,\r\n"
                + "    c.lastname AS last_name\r\n"
                + "FROM \r\n"
                + "    salesforce.nv_eoi_applicant_data app \r\n"
                + "INNER JOIN \r\n"
                + "    salesforce.nv_eoi_form eoi ON app.eoi_form_id_copy = eoi.id\r\n"
                + "INNER JOIN \r\n"
                + "    salesforce.gpl_cs_balance_details offer ON eoi.offersfid = offer.offer_sfid \r\n"
                + "    AND offer.isactive = 'A'\r\n"
                + "INNER JOIN \r\n"
                + "    salesforce.propstrength__offer__c psoffer ON offer.offer_sfid = psoffer.sfid \r\n"
                + "    AND psoffer.PropStrength__Status__c = 'Closed Won'\r\n"
                + "LEFT JOIN \r\n"
                + "    salesforce.contact c ON app.contactprimaryid::integer = c.id\r\n"
                + "LEFT JOIN \r\n"
                + "    salesforce.propstrength__application_booking__c booking ON psoffer.sfid = booking.propstrength__offer__c\r\n"
                + "WHERE \r\n"
                + "    offer.offer_sfid IS NOT NULL \r\n"
                + "    AND (offer.status IS NULL OR offer.status != 'COMPLETED')\r\n"
                + "    AND eoi.kycapproval_status = 'Y'\r\n"
                + "    AND offer.isactive = 'A'\r\n"
                + "    AND offer.rpa_error_log IS NULL\r\n"
                + "    AND offer.project_sfid IN ('" + sfidInClause + "')\r\n"
                + "    AND booking.name IS NULL\r\n"
                + "    AND psoffer.PropStrength__Request__c IS NOT NULL\r\n"
                + "    AND app.isupdated_applicant IS NULL\r\n"
                + "    AND app.isactive = 'Y'\r\n"
                + "ORDER BY \r\n"
                + "    psoffer.name, \r\n"
                + "    app.applicanttype;";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String offerName = resultSet.getString("OFFER_NAME");
                String contactSFID = resultSet.getString("CONTACT_SFID");

                // Print values to verify
                System.out.println("Offer Name: " + offerName);
                System.out.println("Contact SFID: " + contactSFID);

                offerToSFIDsMap
                        .computeIfAbsent(offerName, k -> new ArrayList<>())
                        .add(contactSFID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return offerToSFIDsMap;
    }
    public static void updateBookingId(String offerName , String BookingId) {
    	
    	String updateQuery = "UPDATE salesforce.gpl_cs_balance_details " +
                "SET status = 'COMPLETED', booking_id = '" + BookingId + "' " +
                "WHERE offer_sfid = (SELECT offer_sfid FROM salesforce.propstrength__offer__c WHERE name = '" + offerName + "')";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
             int rowsAffected = statement.executeUpdate(updateQuery);
             System.out.println("Updated booking status for offer: " + offerName + ". Rows affected: " + rowsAffected);
             } catch (Exception e) {
             e.printStackTrace();
    }
    	
  	
    }
    
}
