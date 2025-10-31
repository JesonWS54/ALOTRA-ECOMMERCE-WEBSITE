package nhom17.OneShop.service;

import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.request.MembershipTierRequest;

import java.util.List;

public interface MembershipTierService {
    List<MembershipTier> findAllSorted();
    void save(MembershipTierRequest request);
    void delete(int id);
}
