package com.sqx.app.pet.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.archive.db.service.IPetAlbumService;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetAlbum;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.app.pet.archive.domain.bo.*;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.archive.domain.vo.PetAlbumVo;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.archive.service.PetArchiveService;
import com.sqx.app.pet.community.db.service.ICommunityPostService;
import com.sqx.app.pet.community.domain.entity.CommunityPost;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PetArchiveServiceImpl implements PetArchiveService {

    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IPetStoreService iPetStoreService;
    @Resource
    private IPetAlbumService iPetAlbumService;
    @Resource
    private ICommunityPostService iCommunityPostService;

    @Override
    public void saveInfo(PetArchiveSaveBo bo) {
        PetArchive entity = new PetArchive();
        BeanUtils.copyProperties(bo, entity);
        normalizeArchiveFields(entity);
        iPetArchiveService.save(entity);
        syncDailyAlbum(entity.getId(), bo.getDailyAlbumUrls(), entity.getAvatarUrl());
    }

    @Override
    public IPage<PetArchiveVo> pageInfo(PetArchivePageBo bo) {
        Page<PetArchive> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetArchive> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(Objects.nonNull(bo.getStoreId()), PetArchive::getStoreId, bo.getStoreId())
                .like(StringUtils.isNotBlank(bo.getName()), PetArchive::getName, bo.getName())
                .eq(Objects.nonNull(bo.getPetType()), PetArchive::getPetType, bo.getPetType())
                .eq(Objects.nonNull(bo.getAdoptable()), PetArchive::getAdoptable, bo.getAdoptable())
                .eq(Objects.nonNull(bo.getAuditStatus()), PetArchive::getAuditStatus, bo.getAuditStatus())
                .orderByAsc(PetArchive::getSort);
        IPage<PetArchive> iPage = iPetArchiveService.page(page, qw);
        List<PetArchiveVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (PetArchive record : iPage.getRecords()) {
                PetArchiveVo vo = new PetArchiveVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
            fillStoreNames(voList);
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public PetArchiveVo info(PetArchiveInfoBo bo) {
        PetArchive entity = iPetArchiveService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        PetArchiveVo vo = new PetArchiveVo();
        BeanUtils.copyProperties(entity, vo);
        fillStoreName(vo);
        fillAlbums(vo);
        return vo;
    }

    @Override
    public PetArchiveVo appInfo(PetArchiveInfoBo bo) {
        PetArchive entity = iPetArchiveService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("档案不存在");
        }
        if (!Objects.equals(entity.getAuditStatus(), 1)) {
            if (Objects.equals(entity.getAuditStatus(), 2)) {
                String reason = StringUtils.isNotBlank(entity.getRejectReason())
                        ? entity.getRejectReason() : "未说明原因";
                throw new BizException("档案未通过审核：" + reason);
            }
            throw new BizException("档案审核中，暂不可查看");
        }
        PetArchiveVo vo = new PetArchiveVo();
        BeanUtils.copyProperties(entity, vo);
        fillStoreName(vo);
        fillAlbums(vo);
        return vo;
    }

    @Override
    public PetArchiveVo adoptPetInfo(PetArchiveInfoBo bo) {
        PetArchiveVo vo = appInfo(bo);
        if (!Objects.equals(vo.getAdoptable(), 1)) {
            throw new BizException("该宠物暂不可领养");
        }
        return vo;
    }

    private void fillStoreName(PetArchiveVo vo) {
        if (vo == null || vo.getStoreId() == null) {
            return;
        }
        PetStore store = iPetStoreService.getById(vo.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getName());
        }
    }

    private void fillStoreNames(List<PetArchiveVo> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return;
        }
        Set<Long> storeIds = voList.stream()
                .map(PetArchiveVo::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (storeIds.isEmpty()) {
            return;
        }
        Map<Long, String> storeMap = iPetStoreService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(PetStore::getId, PetStore::getName, (a, b) -> a));
        for (PetArchiveVo vo : voList) {
            if (vo.getStoreId() != null) {
                vo.setStoreName(storeMap.get(vo.getStoreId()));
            }
        }
    }

    @Override
    public void updateInfo(PetArchiveUpdateBo bo) {
        PetArchive entity = new PetArchive();
        BeanUtils.copyProperties(bo, entity);
        normalizeArchiveFields(entity);
        if (!Objects.equals(entity.getAuditStatus(), 2)) {
            entity.setRejectReason(null);
        }
        iPetArchiveService.updateById(entity);
        syncDailyAlbum(entity.getId(), bo.getDailyAlbumUrls(), entity.getAvatarUrl());
    }

    @Override
    public List<PetAlbumVo> listDailyAlbum(Long petId) {
        if (petId == null) {
            return Collections.emptyList();
        }
        return iPetAlbumService.lambdaQuery()
                .eq(PetAlbum::getPetId, petId)
                .orderByAsc(PetAlbum::getSort)
                .list()
                .stream()
                .map(item -> {
                    PetAlbumVo vo = new PetAlbumVo();
                    vo.setId(item.getId());
                    vo.setPetId(item.getPetId());
                    vo.setMediaUrl(item.getMediaUrl());
                    vo.setMediaType(item.getMediaType());
                    vo.setSort(item.getSort());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void saveDailyAlbum(PetAlbumBatchSaveBo bo) {
        PetArchive archive = iPetArchiveService.getById(bo.getPetId());
        if (archive == null) {
            throw new BizException("档案不存在");
        }
        syncDailyAlbum(bo.getPetId(), bo.getMediaUrls(), archive.getAvatarUrl());
    }

    @Override
    public void audit(PetArchiveAuditBo bo) {
        PetArchive entity = iPetArchiveService.getById(bo.getId());
        if (entity == null) {
            throw new BizException("档案不存在");
        }
        validateAuditStatus(bo.getAuditStatus(), bo.getRejectReason());
        PetArchive update = new PetArchive();
        update.setId(bo.getId());
        update.setAuditStatus(bo.getAuditStatus());
        update.setRejectReason(Objects.equals(bo.getAuditStatus(), 2) ? bo.getRejectReason() : null);
        iPetArchiveService.updateById(update);
    }

    @Override
    public void batchAudit(PetArchiveBatchAuditBo bo) {
        validateAuditStatus(bo.getAuditStatus(), bo.getRejectReason());
        for (Long id : bo.getIds()) {
            PetArchive update = new PetArchive();
            update.setId(id);
            update.setAuditStatus(bo.getAuditStatus());
            update.setRejectReason(Objects.equals(bo.getAuditStatus(), 2) ? bo.getRejectReason() : null);
            iPetArchiveService.updateById(update);
        }
    }

    private void validateAuditStatus(Integer auditStatus, String rejectReason) {
        if (!Objects.equals(auditStatus, 0) && !Objects.equals(auditStatus, 1) && !Objects.equals(auditStatus, 2)) {
            throw new BizException("审核状态无效");
        }
        if (Objects.equals(auditStatus, 2) && StringUtils.isBlank(rejectReason)) {
            throw new BizException("拒绝时必须填写原因");
        }
    }

    @Override
    public void delete(PetArchiveInfoBo bo) {
        iPetArchiveService.removeById(bo.getId());
    }

    @Override
    public List<PetArchiveVo> list() {
        List<PetArchive> list = iPetArchiveService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetArchiveVo vo = new PetArchiveVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PetArchiveVo> listByStoreId(Long storeId) {
        return listByStoreId(storeId, null);
    }

    @Override
    public List<PetArchiveVo> listByStoreId(Long storeId, String petType) {
        QueryWrapper<PetArchive> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(PetArchive::getStoreId, storeId)
                .eq(PetArchive::getAuditStatus, 1)
                .eq(StringUtils.isNotBlank(petType), PetArchive::getPetType, petType)
                .orderByAsc(PetArchive::getSort);
        List<PetArchive> list = iPetArchiveService.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            PetArchiveVo vo = new PetArchiveVo();
            BeanUtils.copyProperties(e, vo);
            fillStoreName(vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void fillAlbums(PetArchiveVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        List<String> dailyAlbum = iPetAlbumService.lambdaQuery()
                .eq(PetAlbum::getPetId, vo.getId())
                .orderByAsc(PetAlbum::getSort)
                .list()
                .stream()
                .map(PetAlbum::getMediaUrl)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (dailyAlbum.isEmpty() && StringUtils.isNotBlank(vo.getAvatarUrl())) {
            dailyAlbum = Collections.singletonList(vo.getAvatarUrl());
        }
        List<String> socialAlbum = iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getPetId, vo.getId())
                .eq(CommunityPost::getAuditStatus, 1)
                .eq(CommunityPost::getStatus, 1)
                .orderByDesc(CommunityPost::getCreateTime)
                .last("limit 20")
                .list()
                .stream()
                .flatMap(post -> parseMediaUrls(post.getMediaUrls()).stream())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        vo.setDailyAlbum(dailyAlbum);
        vo.setSocialAlbum(socialAlbum);
        vo.setAlbumImages(dailyAlbum);
        vo.setCommunityImages(socialAlbum);
    }

    private List<String> parseMediaUrls(String mediaUrls) {
        if (StringUtils.isBlank(mediaUrls)) {
            return Collections.emptyList();
        }
        return Arrays.stream(mediaUrls.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private void normalizeArchiveFields(PetArchive entity) {
        if (entity.getAuditStatus() == null) {
            entity.setAuditStatus(0);
        }
        if (entity.getAdoptable() == null) {
            entity.setAdoptable(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getLikeCount() == null) {
            entity.setLikeCount(0);
        }
        entity.setTags(toJsonArray(entity.getTags()));
        entity.setGoodAt(toJsonArray(entity.getGoodAt()));
        entity.setAvoidItems(toJsonArray(entity.getAvoidItems()));
    }

    private String toJsonArray(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            return trimmed;
        }
        List<String> items = Arrays.stream(trimmed.split("[,，、]"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            return null;
        }
        return "[" + items.stream()
                .map(item -> "\"" + item.replace("\"", "") + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

    private void syncDailyAlbum(Long petId, List<String> dailyAlbumUrls, String avatarUrl) {
        if (petId == null) {
            return;
        }
        List<String> urls = new ArrayList<>();
        if (StringUtils.isNotBlank(avatarUrl)) {
            urls.add(avatarUrl.trim());
        }
        if (CollectionUtils.isNotEmpty(dailyAlbumUrls)) {
            for (String url : dailyAlbumUrls) {
                if (StringUtils.isBlank(url)) {
                    continue;
                }
                String trimmed = url.trim();
                if (!urls.contains(trimmed)) {
                    urls.add(trimmed);
                }
            }
        }
        iPetAlbumService.lambdaUpdate()
                .eq(PetAlbum::getPetId, petId)
                .remove();
        if (urls.isEmpty()) {
            return;
        }
        int sort = 1;
        for (String url : urls) {
            PetAlbum album = new PetAlbum();
            album.setPetId(petId);
            album.setMediaUrl(url);
            album.setMediaType(1);
            album.setSort(sort++);
            iPetAlbumService.save(album);
        }
    }
}
