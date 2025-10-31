document.addEventListener('DOMContentLoaded', function () {
    const carriersTable = document.getElementById('carriersTable');
    if (carriersTable) {
        const editModal = document.getElementById('editCarrierModal');
        editModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const phone = button.getAttribute('data-phone');
            const website = button.getAttribute('data-website');

            const modalForm = editModal.querySelector('form');
            modalForm.querySelector('[name="maNVC"]').value = id;
            modalForm.querySelector('[name="tenNVC"]').value = name;
            modalForm.querySelector('[name="soDienThoai"]').value = phone;
            modalForm.querySelector('[name="website"]').value = website;
        });
    }

    const tiersTable = document.getElementById('tiersTable');
        if (tiersTable) {
            const editModal = document.getElementById('editTierModal');
            editModal.addEventListener('show.bs.modal', function (event) {
                const button = event.relatedTarget;
                const id = button.getAttribute('data-id');
                const name = button.getAttribute('data-name');
                const points = button.getAttribute('data-points');
                const discount = button.getAttribute('data-discount');

                const modalForm = editModal.querySelector('form');
                modalForm.querySelector('[name="maHangThanhVien"]').value = id;
                modalForm.querySelector('[name="tenHang"]').value = name;
                modalForm.querySelector('[name="diemToiThieu"]').value = points;
                modalForm.querySelector('[name="phanTramGiamGia"]').value = discount;
            });
        }

    const shippingFeeDataHolder = document.getElementById('shippingFeeData');
        if (shippingFeeDataHolder) {

            // Vì Select2 yêu cầu jQuery, chúng ta sẽ dùng cú pháp jQuery ở đây
            const $provinceSelect = $('#provinceSelect');

            // Đọc dữ liệu từ thẻ div "data holder"
            const provincesJsonUrl = $(shippingFeeDataHolder).data('provinces-url');
            let selectedProvinces = $(shippingFeeDataHolder).data('selected-provinces');

            // ✅ Chuẩn hóa dữ liệu để luôn là mảng hợp lệ
            if (selectedProvinces) {
                if (Array.isArray(selectedProvinces)) {
                } else if (typeof selectedProvinces === 'string') {
                    selectedProvinces = selectedProvinces
                        .replace(/[\[\]]/g, '')
                        .split(',')
                        .map(p => p.trim())
                        .filter(p => p.length > 0);
                } else {
                    selectedProvinces = [];
                }
            } else {
                selectedProvinces = [];
            }

            // Kiểm tra xem có URL để tải JSON không
            if (!provincesJsonUrl) {
                console.error("Lỗi: Không tìm thấy thuộc tính 'data-provinces-url' trên #shippingFeeData.");
                return;
            }

            // Tải file JSON và khởi tạo Select2
            $.getJSON(provincesJsonUrl, function (data) {
                const provinces = data.provinces || [];
                $provinceSelect.empty();

                provinces.forEach(function (province) {
                    const option = new Option(province.name, province.name, false, false);
                    $provinceSelect.append(option);
                });

                // Đặt các giá trị đã chọn (nếu có, khi sửa)
                if (selectedProvinces && selectedProvinces.length > 0) {
                    $provinceSelect.val(selectedProvinces);
                }

                // Khởi tạo thư viện Select2 cho combobox
                $provinceSelect.select2({
                    placeholder: "Chọn các tỉnh thành áp dụng",
                    allowClear: true,
                    theme: "classic"
                });

            }).fail(function() {
                console.error("Lỗi: Không thể tải file JSON tỉnh thành tại: " + provincesJsonUrl);
                alert("Lỗi: Không thể tải danh sách tỉnh thành.");
            });
        }
});