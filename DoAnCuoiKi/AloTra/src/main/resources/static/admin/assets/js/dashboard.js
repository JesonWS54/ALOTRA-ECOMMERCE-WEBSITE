document.addEventListener('DOMContentLoaded', function () {
    // Hàm format tiền tệ chung
    const currencyFormatter = (val) => {
        if (val >= 1000000) return (val / 1000000).toFixed(1) + 'tr';
        if (val >= 1000) return (val / 1000).toFixed(0) + 'k';
        return val;
    };

    // ✅ HÀM HELPER AN TOÀN ĐỂ PARSE JSON
    const safeJsonParse = (jsonString, fallback = []) => {
        try {
            // Thay thế dấu nháy đơn bằng dấu nháy kép để hợp lệ hóa JSON
            return JSON.parse(jsonString.replace(/'/g, '"'));
        } catch (e) {
            console.error("Failed to parse JSON string:", jsonString, e);
            return fallback;
        }
    };

    // 1. Vẽ Biểu đồ Doanh thu theo ngày
    const revenueAndProfitChartEl = document.querySelector('#revenueAndProfitChart');
    if (revenueAndProfitChartEl && typeof ApexCharts !== 'undefined') {
        const revenueData = safeJsonParse(revenueAndProfitChartEl.dataset.revenueData);
        const dayLabels = safeJsonParse(revenueAndProfitChartEl.dataset.dayLabels);

        if (revenueData.length > 0 && dayLabels.length > 0) {
            // ... (phần options của biểu đồ giữ nguyên)
            const options = {
                chart: { height: 350, type: 'area', parentHeightOffset: 0, toolbar: { show: false } },
                series: [{ name: 'Doanh thu', data: revenueData }],
                xaxis: { categories: dayLabels, labels: { style: { colors: '#697a8d', fontSize: '13px' } } },
                yaxis: { labels: { style: { colors: '#697a8d', fontSize: '13px' }, formatter: currencyFormatter } },
                dataLabels: { enabled: false },
                stroke: { width: 3, curve: 'smooth' },
                tooltip: { y: { formatter: (val) => new Intl.NumberFormat('vi-VN').format(val) + " ₫" } },
                fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.7, opacityTo: 0.3, stops: [0, 90, 100] } },
                grid: { borderColor: '#e0e0e0', strokeDashArray: 5 }
            };
            new ApexCharts(revenueAndProfitChartEl, options).render();
        } else {
            revenueAndProfitChartEl.innerHTML = '<div class="d-flex justify-content-center align-items-center h-100 text-muted">Không có dữ liệu để vẽ biểu đồ.</div>';
        }
    }

    // 2. Vẽ Biểu đồ tròn Doanh thu theo Danh mục
    const categoryChartEl = document.querySelector('#revenueByCategoryChart');
    if (categoryChartEl && typeof ApexCharts !== 'undefined') {
        const categoryLabels = safeJsonParse(categoryChartEl.dataset.categoryLabels);
        const categoryData = safeJsonParse(categoryChartEl.dataset.categoryData);

        if (categoryData.length > 0 && categoryLabels.length > 0) {
            // ... (phần options của biểu đồ giữ nguyên)
             const options = {
                chart: { height: 350, type: 'donut' },
                series: categoryData,
                labels: categoryLabels,
                legend: { position: 'bottom' },
                tooltip: { y: { formatter: (val) => new Intl.NumberFormat('vi-VN').format(val) + " ₫" } },
                plotOptions: { pie: { donut: { labels: { show: true, total: { show: true, label: 'Tổng', formatter: (w) => currencyFormatter(w.globals.seriesTotals.reduce((a, b) => a + b, 0)) } } } } }
            };
            new ApexCharts(categoryChartEl, options).render();
        } else {
            categoryChartEl.innerHTML = '<div class="d-flex justify-content-center align-items-center h-100 text-muted">Không có dữ liệu doanh thu theo danh mục.</div>';
        }
    }
});