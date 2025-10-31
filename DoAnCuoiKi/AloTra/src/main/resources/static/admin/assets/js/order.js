// File: order.js

document.addEventListener('DOMContentLoaded', function() {
    
    // --- LOGIC CHO MODAL SỬA ĐƠN VẬN CHUYỂN ---
    const editModalShipping = document.getElementById('editModal');
    if (editModalShipping) {
        editModalShipping.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const modal = this;

            // Lấy dữ liệu từ các thuộc tính data-* của nút
            const shippingId = button.dataset.id;
            const orderId = button.dataset.orderId;
            const trackingCode = button.dataset.trackingCode;
            const carrierId = button.dataset.carrierId;
            const status = button.dataset.status;
            const method = button.dataset.method;

            // Tìm các trường input/select trong modal
            const maVanChuyenInput = modal.querySelector('#editMaVanChuyen');
            const maDonHangInput = modal.querySelector('#editMaDonHang');
            const maVanDonText = modal.querySelector('#editMaVanDonText');
            const maNVCSelect = modal.querySelector('#editMaNVC');
            const trangThaiSelect = modal.querySelector('#editTrangThai');
            const phuongThucInput = modal.querySelector('#editPhuongThuc');

            // Gán dữ liệu vào các trường trong form
            if (maVanChuyenInput) maVanChuyenInput.value = shippingId;
            if (maDonHangInput) maDonHangInput.value = orderId;
            if (maVanDonText) maVanDonText.textContent = trackingCode;
            if (maNVCSelect) maNVCSelect.value = carrierId;
            if (trangThaiSelect) trangThaiSelect.value = status;
            if (phuongThucInput) phuongThucInput.value = method;
        });
    }

    // --- LOGIC CHO NÚT TẠO MÃ KHUYẾN MÃI ---
    const generateBtn = document.getElementById('generate-voucher-code-btn');
    const codeInput = document.querySelector('input[name="maKhuyenMai"]');

    if (generateBtn && codeInput && !codeInput.readOnly) {
        generateBtn.addEventListener('click', function() {
            const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
            let result = '';
            for (let i = 0; i < 10; i++) {
                result += chars.charAt(Math.floor(Math.random() * chars.length));
            }
            codeInput.value = result;
        });
    }

    const createShippingModal = document.getElementById('createShippingModal');
    if (createShippingModal) {
        createShippingModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const modal = this;

            const orderId = button.dataset.orderId;
            const phuongThuc = button.dataset.phuongthuc;

            modal.querySelector('#createShipping_maDonHang').value = orderId;
            modal.querySelector('#orderIdInModal').textContent = orderId;
            modal.querySelector('#phuongThucVanChuyen').value = phuongThuc;

            const allGroups = modal.querySelectorAll('#nhaVanChuyenSelect optgroup');
            allGroups.forEach(g => g.style.display = 'none');

            const currentGroup = modal.querySelector(`#nhaVanChuyenSelect optgroup[label='Đơn hàng #${orderId}']`);
            if (currentGroup) {
                currentGroup.style.display = '';
            }

            modal.querySelector('#nhaVanChuyenSelect').value = '';
        });
    }
});