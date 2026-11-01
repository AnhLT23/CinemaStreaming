package controller.admin;

import dao.MovieDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import dao.UserDeviceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import model.Transaction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Owner: Hữu Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminDashboardController.java.
 */
@WebServlet(
    name = "AdminDashboardController",
    urlPatterns = { "/admin", "/admin/dashboard" }
)
public class AdminDashboardController extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final UserDeviceDAO userDeviceDAO = new UserDeviceDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            if ("export".equals(req.getParameter("action"))) {
                exportTransactions(resp);
                return;
            }
            req.setAttribute("revenue", transactionDAO.getTotalRevenue());
            req.setAttribute("vipCount", userDAO.countVipActive());
            req.setAttribute(
                "onlineCount",
                userDeviceDAO.countOnlineDevices(5)
            );
            req.setAttribute("movieCount", movieDAO.countAll());
            req.setAttribute(
                "recentTransactions",
                transactionDAO.getRecent(10)
            );
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/dashboard.jsp"
            ).forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Không tải được dashboard.", e);
        }
    }

    private void exportTransactions(HttpServletResponse resp) throws Exception {
        List<Transaction> rows = transactionDAO.getRecent(1000);
        resp.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        resp.setHeader(
            "Content-Disposition",
            "attachment; filename=TVHAY-transactions.xlsx"
        );
        try (
            Workbook workbook = new XSSFWorkbook();
            OutputStream out = resp.getOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Transactions");
            String[] headers = {
                "Transaction ID",
                "Transaction Code",
                "Username",
                "Type",
                "Payment Method",
                "Status",
                "Amount",
                "Created At",
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper helper = workbook.getCreationHelper();
            dateStyle.setDataFormat(
                helper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
            );
            int rowNum = 1;
            for (Transaction t : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getTransactionId());
                row.createCell(1).setCellValue(t.getTransactionCode());
                row.createCell(2).setCellValue(t.getUsername());
                row.createCell(3).setCellValue(t.getTransactionType());
                row.createCell(4).setCellValue(t.getPaymentMethod());
                row.createCell(5).setCellValue(t.getPaymentStatus());
                row.createCell(6).setCellValue(
                    t.getAmount() == null ? 0 : t.getAmount().doubleValue()
                );
                if (t.getCreatedAt() != null) {
                    Cell date = row.createCell(7);
                    date.setCellValue(t.getCreatedAt());
                    date.setCellStyle(dateStyle);
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        }
    }
}
