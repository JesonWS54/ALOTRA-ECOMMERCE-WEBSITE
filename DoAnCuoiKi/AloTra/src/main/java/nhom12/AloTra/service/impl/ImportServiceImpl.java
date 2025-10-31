package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.request.ImportDetailRequest;
import nhom17.OneShop.request.ImportRequest;
import nhom17.OneShop.service.ImportService;
import nhom17.OneShop.specification.ImportSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private ImportRepository importRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImportDetailRepository importDetailRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public Page<Import> findAll(String keyword, Integer supplierId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").descending());

        Specification<Import> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            try {
                Integer id = Integer.parseInt(keyword);
                spec = spec.and(ImportSpecification.hasId(id));
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        if (supplierId != null) {
            spec = spec.and(ImportSpecification.hasSupplier(supplierId));
        }

        return importRepository.findAll(spec, pageable);
    }

    @Override
    public Import findById(int id) {
        return importRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ImportRequest importRequest) {
        validateImportDetails(importRequest.getChiTietPhieuNhapList());
        // Bước 1: Chuẩn bị đối tượng cha (Phiếu nhập) và nhà cung cấp
        Supplier supplier = supplierRepository.findById(importRequest.getMaNCC())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà cung cấp với ID: " + importRequest.getMaNCC()));
        Import phieuNhap = prepareImportEntity(importRequest);
        phieuNhap.setNhaCungCap(supplier);

        // Bước 2: (Chỉ khi sửa) Hoàn trả tồn kho từ các chi tiết cũ và xóa chúng
        if (phieuNhap.getMaPhieuNhap() != null) {
            handleInventoryForUpdate(phieuNhap);
        }

        // Bước 3: Xử lý các chi tiết mới từ request (tạo chi tiết, cập nhật tồn kho)
        List<ImportDetail> newChiTietList = processNewImportDetails(importRequest.getChiTietPhieuNhapList(), phieuNhap);

        // Bước 4: Cập nhật danh sách chi tiết của phiếu nhập và lưu
        phieuNhap.getChiTietPhieuNhapList().addAll(newChiTietList);
        importRepository.save(phieuNhap);
    }

    private void validateImportDetails(List<ImportDetailRequest> details) {
        Set<Integer> productIds = new HashSet<>();
        for (ImportDetailRequest detail : details) {
            if (!productIds.add(detail.getMaSanPham())) {
                throw new IllegalArgumentException("Sản phẩm không được trùng lặp trong một phiếu nhập.");
            }
        }
    }

    private Import prepareImportEntity(ImportRequest importRequest) {
        if (importRequest.getMaPhieuNhap() != null) {
            return findById(importRequest.getMaPhieuNhap());
        }
        Import newImport = new Import();
        newImport.setChiTietPhieuNhapList(new ArrayList<>()); // Khởi tạo list để tránh NullPointerException
        return newImport;
    }

    private void handleInventoryForUpdate(Import phieuNhap) {
        // Hoàn trả lại số lượng tồn kho từ các chi tiết cũ
        for (ImportDetail oldDetail : phieuNhap.getChiTietPhieuNhapList()) {
            updateInventory(oldDetail.getSanPham(), -oldDetail.getSoLuong());
        }
        phieuNhap.getChiTietPhieuNhapList().clear();
    }

    private List<ImportDetail> processNewImportDetails(List<ImportDetailRequest> detailRequests, Import phieuNhap) {
        List<ImportDetail> newDetails = new ArrayList<>();
        for (ImportDetailRequest detailRequest : detailRequests) {
            Product sanPham = productRepository.findById(detailRequest.getMaSanPham())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + detailRequest.getMaSanPham()));

            ImportDetail chiTiet = new ImportDetail();
            chiTiet.setPhieuNhap(phieuNhap);
            chiTiet.setSanPham(sanPham);
            chiTiet.setSoLuong(detailRequest.getSoLuong());
            chiTiet.setGiaNhap(detailRequest.getGiaNhap());
            newDetails.add(chiTiet);

            updateInventory(sanPham, detailRequest.getSoLuong());
        }
        return newDetails;
    }

    private void updateInventory(Product product, int quantityChange) {
        Inventory inventory = inventoryRepository.findById(product.getMaSanPham()).orElse(new Inventory());
        if (inventory.getSanPham() == null) {
            inventory.setSanPham(product);
            inventory.setMaSanPham(product.getMaSanPham());
        }
        int currentStock = inventory.getSoLuongTon() != null ? inventory.getSoLuongTon() : 0;
        inventory.setSoLuongTon(currentStock + quantityChange);

        if (quantityChange > 0) {
            inventory.setNgayNhapGanNhat(LocalDateTime.now());
        }
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void delete(int id) {
        Import phieuNhap = findById(id);
        // Hoàn trả tồn kho trước khi xóa
        for (ImportDetail detail : phieuNhap.getChiTietPhieuNhapList()) {
            updateInventory(detail.getSanPham(), -detail.getSoLuong()); // Trừ đi số lượng đã nhập
        }
        importRepository.delete(phieuNhap);
    }
}
