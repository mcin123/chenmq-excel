package com.github.mcin123.excel.poi;

import com.github.mcin123.excel.filter.ExcelException;
import com.github.mcin123.excel.annotation.ExcelSheet;
import com.github.mcin123.excel.utils.IoOptionUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @author chenmq
 * @version V1.0
 * ProjectName: chenmq-excel
 * Package com.github.mcin123.excel.poi
 * Description: 表格创建
 * date 2018-04-08 下午7:29
 */

public class ExportExcel {


    /**
     * HttpServletResponse导出excel
     * @param excelName 导出表格名称
     * @param enc 编码类型
     * @param response HttpServletResponse
     * @param list 数据列表
     */
    public static void downToExcel (String excelName,String enc,HttpServletResponse response,List<?>... list) throws UnsupportedEncodingException {

        Workbook wb = initWorkBook(list);
        OutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Type", "application/force-download");
        response.setHeader("Content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode(excelName, enc));

        IoOptionUtils.writeWorkbook(wb,out);
    }

    /**
     * 导出文件到磁盘
     * @param filePath 磁盘文件路径
     * @param list 数据列表
     */
    public static void exportToFile (String filePath,List<?> ... list) throws ExcelException {
        Workbook wb = initWorkBook(list);
        OutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(filePath);
        } catch (IOException e) {
            throw new ExcelException("找不到输出路径", e);
        } finally {
            IoOptionUtils.writeWorkbook(wb,fileOut);
        }
    }

    /**
     * 导出字节类型
     * @param sheetList 数据列表
     * @throws ExcelException ExcelException
     */
    public static byte[] exportToBytes(List<?>... sheetList) throws ExcelException{
        Workbook workbook = initWorkBook(sheetList);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] result = null;

        try {
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            result = byteArrayOutputStream.toByteArray();
            return result;

        } catch (IOException e) {
            throw new ExcelException("导出字节类型有异常",e);
        } finally {
            IoOptionUtils.closeByteArrayOutputStream(byteArrayOutputStream);
        }
    }

    /**
     * 初始化表格信息
     * @param sheelDataArr List<?>...
     */
    private static Workbook initWorkBook (List<?>... sheelDataArr){

        // HSSFWorkbook == 2007/xlsx || XSSFWorkbook == 2003/xls
        Workbook wb = new HSSFWorkbook();

        for (List<?> list:sheelDataArr) {

            Class clazz = list.get(0).getClass();

            String sheetName = clazz.getSimpleName();

            Sheet sheet = wb.createSheet(sheetName);

            // 设置表头
            Field[] fields = clazz.getDeclaredFields();
            Row row = sheet.createRow(0);
            CellStyle cellStyleTitle = wb.createCellStyle();

            for (Field f : fields) {
                ExcelSheet excelSheet = f.getAnnotation(ExcelSheet.class);
                Cell cell = row.createCell(excelSheet.sheetPosition());
                cell.setCellValue(excelSheet.cellName());
                cellStyleTitle.setAlignment(excelSheet.horizontalAlignment());
                cellStyleTitle.setVerticalAlignment(excelSheet.verticalAlignment());
                cell.setCellStyle(cellStyleTitle);
            }

            Row row1 = null;
            CreationHelper createHelper = wb.getCreationHelper();

            for (int i = 0; i < list.size(); i++) {
                row1 = sheet.createRow(i+1);
                for (Field f : fields) {

                    ExcelSheet excelSheet = f.getAnnotation(ExcelSheet.class);
                    f.setAccessible(true);

                    try {
                        if (null != f.get(list.get(i))) {

                            Cell cell = row1.createCell(excelSheet.sheetPosition());

                            CellStyle cellStyle = wb.createCellStyle();

                            Font font = wb.createFont();
                            font.setFontHeightInPoints(excelSheet.fontSize());
                            font.setFontName(excelSheet.fontName());
                            font.setItalic(excelSheet.isItalic());
                            font.setStrikeout(excelSheet.isStrikeout());
                            cellStyle.setFont(font);

                            if (f.get(list.get(i)).getClass() == Date.class) {

                                cell.setCellValue((Date) f.get(list.get(i)));
                                cellStyle.setDataFormat(
                                        createHelper.createDataFormat().getFormat(excelSheet.dateFormat()));

                            } else if (String.valueOf(f.get(list.get(i))).startsWith("http")){

                                font.setUnderline(Font.U_SINGLE);
                                font.setColor(IndexedColors.BLUE.getIndex());
                                cell.setCellValue(String.valueOf(f.get(list.get(i))));
                                Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                                link.setAddress(String.valueOf(f.get(list.get(i))));
                                cell.setHyperlink(link);

                            } else {
                                cell.setCellValue(f.get(list.get(i)).toString());
                            }

                            cellStyle.setAlignment(excelSheet.horizontalAlignment());
                            cellStyle.setVerticalAlignment(excelSheet.verticalAlignment());
                            cell.setCellStyle(cellStyle);
                        }

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return wb;
    }

}
