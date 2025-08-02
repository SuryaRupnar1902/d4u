package d4u.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelUtil {
	
	
	static String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    public static  final String FILE_PATH = ("C:\\Users\\LENOVO\\eclipse-workspace\\D4u\\test-output\\BookingReport_" + timestamp + ".xlsx");  // Path to save the Excel report
    private static Workbook workbook;
    private static Sheet sheet;
    private static int rowCount = 0;

    // Initialize the workbook and sheet for logging
    public static void initializeExcel() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Booking Results");

        // Create header row
        Row headerRow = sheet.createRow(rowCount++);
        headerRow.createCell(0).setCellValue("Booking Name");
        headerRow.createCell(1).setCellValue("SFID Status");
        headerRow.createCell(2).setCellValue("Booking ID");
        headerRow.createCell(3).setCellValue("Result");
    }

    // Write the result of a booking attempt to the Excel sheet
    public static void logBookingResult(String bookingName, String sfidStatus, String bookingId, String result) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0).setCellValue(bookingName);
        row.createCell(1).setCellValue(sfidStatus);
        row.createCell(2).setCellValue(bookingId != null ? bookingId : "N/A");  // If no bookingId, mark it as N/A
        row.createCell(3).setCellValue(result);
    }

    // Save the Excel file to disk
    public static void saveExcel() {
        try (FileOutputStream fileOut = new FileOutputStream(new File(FILE_PATH))) {
            workbook.write(fileOut);
            workbook.close();
            System.out.println("Booking report saved to: " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Failed to save Excel file: " + e.getMessage());
            e.printStackTrace();  // Print the stack trace for debugging
        }
    }

}
