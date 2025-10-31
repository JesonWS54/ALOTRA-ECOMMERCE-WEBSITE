package nhom12.AloTra.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDataDTO {
    // Các chỉ số KPI chính
    private BigDecimal totalRevenue = BigDecimal.ZERO;      // Tổng Doanh Thu
    private BigDecimal totalCostOfGoodsSold = BigDecimal.ZERO; // Tổng Giá Vốn
    private BigDecimal totalProfit = BigDecimal.ZERO;         // Lợi Nhuận = Doanh thu - Giá vốn
    private double profitMargin = 0.0;                      // Tỷ lệ lợi nhuận (%)

    private long totalOrders = 0;                           // Tổng Đơn Hàng
    private BigDecimal averageOrderValue = BigDecimal.ZERO; // Giá Trị Đơn Trung Bình
    private long totalProductsSold = 0;                     // Tổng Sản Phẩm Đã Bán

    // Dữ liệu cho biểu đồ Doanh thu theo ngày
    private List<String> revenueByDayLabels;
    private List<BigDecimal> revenueByDayData;

    // Dữ liệu cho Top sản phẩm bán chạy
    private List<TopSellingProductDTO> topSellingProducts;

    // Dữ liệu cho biểu đồ Doanh thu theo danh mục
    private List<String> revenueByCategoryLabels;
    private List<BigDecimal> revenueByCategoryData;
}
