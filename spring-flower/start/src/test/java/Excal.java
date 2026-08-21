import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class Excal {
    /**
     * 写入excal
     */
    public static void writeExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Report");
        //rownum从0开始
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("Name");
        row.createCell(0).setCellValue("city");
        row = sheet.createRow(1);
        row.createCell(1).setCellValue("王五");
        row.createCell(1).setCellValue("北京");
        row = sheet.createRow(2);
        row.createCell(2).setCellValue("张三");
        row.createCell(2).setCellValue("北京");
        row = sheet.createRow(3);
        row.createCell(3).setCellValue("李四");
        row.createCell(3).setCellValue("上海");
        // 使用资源路径获取文件位置
        String resourcePath = ClassLoader.getSystemResource("static/excal/report.xlsx").getPath();
        FileOutputStream fileOutput = new FileOutputStream(resourcePath);
        workbook.write(fileOutput);
        fileOutput.close();
        workbook.close();
        
        System.out.println("Excel文件已写入: " + resourcePath);
    }
    /*
     *读取excal文件
     */
    public static void readExcel() throws IOException {
        // 使用资源路径获取文件位置
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("static/excal/report.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int last = sheet.getLastRowNum();
        for (int i = 0; i <= last; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row != null) {
                String cellValue = row.getCell(0).getStringCellValue();
                String cellValue1 = row.getCell(1).getStringCellValue();
                System.out.println(cellValue + " " + cellValue1);
            }
        }
        inputStream.close();
        workbook.close();
    }

    /**
     * 主程序
     */
    public static void main(String[] args) {
        try {
            writeExcel();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("finally writeExcel");
        }
        try {
            readExcel();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("finally readExcel");
        }
    }
}