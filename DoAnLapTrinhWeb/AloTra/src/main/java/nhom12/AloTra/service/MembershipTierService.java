package nhom12.AloTra.service;

import nhom12.AloTra.entity.MembershipTier;
import nhom12.AloTra.request.MembershipTierRequest;

import java.util.List;

public interface MembershipTierService {
    List<MembershipTier> findAllSorted();
    void save(MembershipTierRequest request);
    void delete(int id);
}
