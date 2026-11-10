package com.sqx.app.pet.archive.service;

import com.sqx.app.pet.archive.domain.bo.*;
import com.sqx.app.pet.archive.domain.vo.PetAlbumVo;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PetArchiveService {
    void saveInfo(PetArchiveSaveBo bo);
    IPage<PetArchiveVo> pageInfo(PetArchivePageBo bo);
    PetArchiveVo info(PetArchiveInfoBo bo);
    PetArchiveVo appInfo(PetArchiveInfoBo bo);

    PetArchiveVo adoptPetInfo(PetArchiveInfoBo bo);
    void updateInfo(PetArchiveUpdateBo bo);
    void delete(PetArchiveInfoBo bo);
    List<PetArchiveVo> list();

    List<PetArchiveVo> listByStoreId(Long storeId);

    List<PetArchiveVo> listByStoreId(Long storeId, String petType);

    List<PetAlbumVo> listDailyAlbum(Long petId);

    void saveDailyAlbum(PetAlbumBatchSaveBo bo);

    void audit(PetArchiveAuditBo bo);

    void batchAudit(PetArchiveBatchAuditBo bo);
}
