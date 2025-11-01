document.addEventListener('DOMContentLoaded', function () {

    const suppliersTable = document.getElementById('suppliersTable');
    if (suppliersTable) {
        const editModal = document.getElementById('editSupplierModal');
        editModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const phone = button.getAttribute('data-phone');
            const address = button.getAttribute('data-address');

            const modalForm = editModal.querySelector('form');
            modalForm.querySelector('[name="maNCC"]').value = id;
            modalForm.querySelector('[name="tenNCC"]').value = name;
            modalForm.querySelector('[name="sdt"]').value = phone;
            modalForm.querySelector('[name="diaChi"]').value = address;
        });
    }

    const importForm = document.getElementById('import-form');
        if (importForm) {
            const container = document.getElementById('details-container');
            const template = document.getElementById('detail-row-template');
            const addButton = document.getElementById('add-detail-row');
            let rowIndex = 0;

            // Hàm để thêm một dòng mới vào bảng
            function addRow(detail) {
                // Sao chép nội dung từ template
                const newRowFragment = template.content.cloneNode(true);
                const tr = newRowFragment.querySelector('tr');

                // Lấy các thẻ input/select trong dòng mới
                const productSelect = tr.querySelector('.product-select');
                const quantityInput = tr.querySelector('.quantity-input');
                const priceInput = tr.querySelector('.price-input');

                // Cập nhật thuộc tính 'name' với chỉ số (index) đúng
                // để Spring Boot có thể nhận diện danh sách
                productSelect.name = `chiTietPhieuNhapList[${rowIndex}].maSanPham`;
                quantityInput.name = `chiTietPhieuNhapList[${rowIndex}].soLuong`;
                priceInput.name = `chiTietPhieuNhapList[${rowIndex}].giaNhap`;

                // Nếu có dữ liệu ban đầu (chế độ Sửa), điền vào các ô
                if(detail) {
                    productSelect.value = detail.maSanPham;
                    quantityInput.value = detail.soLuong;
                    priceInput.value = detail.giaNhap;
                }

                // Thêm dòng mới vào bảng
                container.appendChild(newRowFragment);
                rowIndex++;
            }

            // Kiểm tra xem có dữ liệu ban đầu được truyền từ HTML không
            // (Biến 'initialDetails' được tạo bởi thẻ script trong file html)
            if (typeof initialDetails !== 'undefined' && initialDetails && initialDetails.length > 0) {
                // Nếu có, lặp qua và tạo các dòng tương ứng
                initialDetails.forEach(detail => addRow(detail));
            } else {
                // Nếu không (chế độ Thêm mới), tạo một dòng trống đầu tiên
                addRow(null);
            }

            // Gán sự kiện click cho nút "Thêm sản phẩm"
            addButton.addEventListener('click', () => addRow(null));

            // Gán sự kiện click cho các nút "Xóa" (sử dụng event delegation)
            container.addEventListener('click', function(event) {
                if (event.target && event.target.closest('.remove-detail-row')) {
                    // Tìm đến thẻ <tr> cha và xóa nó đi
                    event.target.closest('tr').remove();
                }
            });
        }
});