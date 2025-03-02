package com.sluv.server.domain.item.service;

import com.sluv.server.domain.brand.entity.Brand;
import com.sluv.server.domain.brand.entity.NewBrand;
import com.sluv.server.domain.brand.entity.RecentSelectBrand;
import com.sluv.server.domain.brand.exception.BrandNotFoundException;
import com.sluv.server.domain.brand.exception.NewBrandNotFoundException;
import com.sluv.server.domain.brand.repository.BrandRepository;
import com.sluv.server.domain.brand.repository.NewBrandRepository;
import com.sluv.server.domain.brand.repository.RecentSelectBrandRepository;
import com.sluv.server.domain.celeb.dto.CelebDto;
import com.sluv.server.domain.celeb.entity.Celeb;
import com.sluv.server.domain.celeb.entity.NewCeleb;
import com.sluv.server.domain.celeb.entity.RecentSelectCeleb;
import com.sluv.server.domain.celeb.exception.CelebNotFoundException;
import com.sluv.server.domain.celeb.exception.NewCelebNotFoundException;
import com.sluv.server.domain.celeb.repository.CelebRepository;
import com.sluv.server.domain.celeb.repository.NewCelebRepository;
import com.sluv.server.domain.celeb.repository.RecentSelectCelebRepository;
import com.sluv.server.domain.item.dto.*;
import com.sluv.server.domain.item.entity.*;
import com.sluv.server.domain.item.entity.hashtag.Hashtag;
import com.sluv.server.domain.item.entity.hashtag.TempItemHashtag;
import com.sluv.server.domain.item.enums.ItemStatus;
import com.sluv.server.domain.item.exception.ItemCategoryNotFoundException;
import com.sluv.server.domain.item.exception.TempItemNotFoundException;
import com.sluv.server.domain.item.exception.hashtag.HashtagNotFoundException;
import com.sluv.server.domain.item.repository.*;
import com.sluv.server.domain.item.repository.hashtag.HashtagRepository;
import com.sluv.server.domain.item.repository.hashtag.TempItemHashtagRepository;
import com.sluv.server.domain.user.entity.User;
import com.sluv.server.global.common.enums.ItemImgOrLinkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TempItemService {
    private final TempItemRepository tempItemRepository;

    private final TempItemLinkRepository tempItemLinkRepository;
    private final TempItemImgRepository tempItemImgRepository;
    private final TempItemHashtagRepository tempItemHashtagRepository;

    private final HashtagRepository hashtagRepository;
    private final CelebRepository celebRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final BrandRepository brandRepository;
    private final NewBrandRepository newBrandRepository;
    private final NewCelebRepository newCelebRepository;
    private final RecentSelectCelebRepository recentSearchCelebRepository;
    private final RecentSelectBrandRepository recentSelectBrandRepository;


    public void postTempItem(User user, TempItemPostReqDto reqDto) {
        Celeb celeb = reqDto.getCelebId() != null ? celebRepository.findById(reqDto.getCelebId())
                .orElseThrow(CelebNotFoundException::new)
                : null;
        Brand brand = reqDto.getBrandId() != null ? brandRepository.findById(reqDto.getBrandId())
                .orElseThrow(BrandNotFoundException::new)
                : null;
        NewCeleb newCeleb = reqDto.getNewCelebId() != null ? newCelebRepository.findById(reqDto.getNewCelebId())
                .orElseThrow(NewCelebNotFoundException::new)
                : null;

        NewBrand newBrand = reqDto.getNewBrandId() != null ? newBrandRepository.findById(reqDto.getNewBrandId())
                .orElseThrow(NewBrandNotFoundException::new)
                : null;
        ItemCategory itemCategory = reqDto.getCategoryId() != null ? itemCategoryRepository.findById(reqDto.getCategoryId())
                .orElseThrow(ItemCategoryNotFoundException::new)
                : null;

        TempItem tempitem = tempItemRepository.save(TempItem
                .builder()
                .user(user)
                .celeb(celeb)
                .newCeleb(newCeleb)
                .category(itemCategory)
                .brand(brand)
                .newBrand(newBrand)
                .name(reqDto.getItemName())
                .whenDiscovery(reqDto.getWhenDiscovery())
                .whereDiscovery(reqDto.getWhereDiscovery())
                .price(reqDto.getPrice())
                .additionalInfo(reqDto.getAdditionalInfo())
                .infoSource(reqDto.getInfoSource())
                .itemStatus(ItemStatus.ACTIVE)
                .build()
            );

        // ItemImg 테이블에 추가
        if(reqDto.getImgList() != null) {

            reqDto.getImgList().stream()
                            .map(tempItemImg ->
                                TempItemImg.builder()
                                        .tempItem(tempitem)
                                        .tempItemImgUrl(tempItemImg.getImgUrl())
                                        .representFlag(tempItemImg.getRepresentFlag())
                                        .itemImgOrLinkStatus(ItemImgOrLinkStatus.ACTIVE)
                                        .build()
                            ).forEach(tempItemImgRepository::save);

        }

        // ItemLink 테이블에 추가
        if(reqDto.getLinkList() != null) {
            reqDto.getLinkList().stream()
                            .map(tempItemLink ->
                                    TempItemLink.builder()
                                            .tempItem(tempitem)
                                            .linkName(tempItemLink.getLinkName())
                                            .tempItemLinkUrl(tempItemLink.getItemLinkUrl())
                                            .itemImgOrLinkStatus(ItemImgOrLinkStatus.ACTIVE)
                                            .build()

                            ).forEach(tempItemLinkRepository::save);
        }

        // ItemHashtag 테이블에 추가
        if(reqDto.getHashTagIdList() != null) {
            reqDto.getHashTagIdList().stream().map(hashTag ->

                    TempItemHashtag.builder()
                            .tempItem(tempitem)
                            .hashtag(
                                    hashtagRepository.findById(hashTag)
                                            .orElseThrow(HashtagNotFoundException::new)
                            )
                            .build()

            ).forEach(tempItemHashtagRepository::save);
        }

        // Recent Search Celeb 테이블에 추가
        recentSearchCelebRepository.save(RecentSelectCeleb.builder()
                .user(user)
                .celeb(celeb)
                .newCeleb(newCeleb)
                .build()
        );

        recentSelectBrandRepository.save(RecentSelectBrand.builder()
                .user(user)
                .brand(brand)
                .newBrand(newBrand)
                .build()
        );

    }

    public List<TempItemResDto> getTempItemList(User user, Pageable pageable){


        return tempItemRepository.getTempItemList(user, pageable).stream().map(tempItem -> {

            List<ItemImgResDto> tempImgList = tempItemImgRepository.findAllByTempItem(tempItem)
                    .stream().map(tempItemImg -> ItemImgResDto.builder()
                            .imgUrl(tempItemImg.getTempItemImgUrl())
                            .representFlag(tempItemImg.getRepresentFlag())
                            .build()
                    ).collect(Collectors.toList());

            List<Hashtag> tempHashtagList = tempItemHashtagRepository.findAllByTempItem(tempItem)
                    .stream().map(TempItemHashtag::getHashtag).toList();

            List<ItemLinkResDto> tempLinkList = tempItemLinkRepository.findAllByTempItem(tempItem)
                    .stream().map(tempItemLink -> ItemLinkResDto.builder()
                            .linkName(tempItemLink.getLinkName())
                            .itemLinkUrl(tempItemLink.getTempItemLinkUrl())
                            .build()
                    ).collect(Collectors.toList());

                CelebDto celebDto = tempItem.getCeleb() != null ?
                        CelebDto.builder()
                        .id(tempItem.getId())
                        .celebNameKr(tempItem.getCeleb().getCelebNameKr())
                        .celebNameEn(tempItem.getCeleb().getCelebNameEn())
                        .categoryChild(tempItem.getCeleb().getCelebCategory().getName())
                        .categoryParent(tempItem.getCeleb().getCelebCategory().getParent().getName())
                        .parentCelebNameKr(tempItem.getCeleb().getParent() != null ? tempItem.getCeleb().getParent().getCelebNameKr() : null)
                        .parentCelebNameEn(tempItem.getCeleb().getParent() != null ? tempItem.getCeleb().getParent().getCelebNameEn() : null)
                        .build()
                        : null;

                ItemCategoryDto itemCategoryDto = tempItem.getCategory() != null ?
                        ItemCategoryDto.builder()
                                .id(tempItem.getCategory().getId())
                                .name(tempItem.getCategory().getName())
                                .parentName(tempItem.getCategory().getParent().getName())
                                .build()
                        : null;

            return TempItemResDto.builder()
                            .id(tempItem.getId())
                            .imgList(tempImgList)
                            .celeb(celebDto)
                            .whenDiscovery(tempItem.getWhenDiscovery())
                            .whereDiscovery(tempItem.getWhereDiscovery())
                            .category(itemCategoryDto)
                            .itemName(tempItem.getName())
                            .price(tempItem.getPrice())
                            .additionalInfo(tempItem.getAdditionalInfo())
                            .hashTagList(tempHashtagList)
                            .linkList(tempLinkList)
                    .infoSource(tempItem.getInfoSource())
                    .newCelebId(tempItem.getNewCeleb().getId())
                    .newBrandId(tempItem.getNewBrand().getId())
                    .updatedAt(tempItem.getUpdatedAt())
                    .build();

            }
        ).collect(Collectors.toList());
    }

    @Transactional
    public void putTempItem(User user, Long tempItemId, TempItemPostReqDto dto){

        TempItem tempItem = tempItemRepository.findById(tempItemId).orElseThrow(TempItemNotFoundException::new);
        Celeb celeb = dto.getCelebId() != null ? celebRepository.findById(dto.getCelebId()).orElseThrow(CelebNotFoundException::new) : null;
        ItemCategory itemCategory = dto.getCategoryId() != null ? itemCategoryRepository.findById(dto.getCategoryId()).orElseThrow(ItemCategoryNotFoundException::new) : null;
        Brand brand = dto.getBrandId() != null ?brandRepository.findById(dto.getBrandId()).orElseThrow(BrandNotFoundException::new) : null;
        NewCeleb newCeleb = dto.getNewCelebId() != null ?newCelebRepository.findById(dto.getNewCelebId()).orElseThrow(NewCelebNotFoundException::new) : null;
        NewBrand newBrand = dto.getNewBrandId() != null ?newBrandRepository.findById(dto.getNewBrandId()).orElseThrow(NewBrandNotFoundException::new) : null;

        // temp Item 변경.
        tempItem.setCeleb(celeb);
        tempItem.setNewCeleb(newCeleb);
        tempItem.setCategory(itemCategory);
        tempItem.setBrand(brand);
        tempItem.setNewBrand(newBrand);
        tempItem.setName(dto.getItemName());
        tempItem.setWhenDiscovery(dto.getWhenDiscovery());
        tempItem.setWhereDiscovery(dto.getWhereDiscovery());
        tempItem.setPrice(dto.getPrice());
        tempItem.setAdditionalInfo(dto.getAdditionalInfo());
        tempItem.setInfoSource(dto.getInfoSource());
        tempItem.setUser(user);

        /**
         * 위 작업을 병렬로 실행 -> 캐시호출 기준 0.1초 차이남.
         * 하지만 가독성을 위해 일단, 위 코드로 실행.
         */
//        Runnable runnable1 = ()-> tempItem.setCeleb(celeb);
//        Runnable runnable2 = () -> tempItem.setCeleb(celeb);
//        Runnable runnable3 = () -> tempItem.setNewCelebName(dto.getNewCelebName());
//        Runnable runnable4 = () -> tempItem.setCategory(itemCategory);
//        Runnable runnable5 = () -> tempItem.setBrand(brand);
//        Runnable runnable6 = () -> tempItem.setNewBrandName(dto.getNewBrandName());
//        Runnable runnable7 = () -> tempItem.setName(dto.getItemName());
//        Runnable runnable8 = () -> tempItem.setWhenDiscovery(dto.getWhenDiscovery());
//        Runnable runnable9 = () -> tempItem.setWhereDiscovery(dto.getWhereDiscovery());
//        Runnable runnable10 = () -> tempItem.setPrice(dto.getPrice());
//        Runnable runnable11 = () -> tempItem.setAdditionalInfo(dto.getAdditionalInfo());
//        Runnable runnable12 = () -> tempItem.setInfoSource(dto.getInfoSource());
//        Runnable runnable13 = () -> tempItem.setUser(user);
//        synchronized(tempItem) {
//            Stream.of(
//                    runnable1, runnable2, runnable3, runnable4, runnable5,runnable6,runnable7,runnable8,runnable9,runnable10,runnable11,runnable12, runnable13
//            ).parallel().forEach(Runnable::run);
//        }

        // tempItemImg 모두 삭제 후 변경
        tempItemImgRepository.deleteAllByTempItemId(tempItem.getId());
        if(dto.getImgList() != null) {
            dto.getImgList().stream().map(img -> TempItemImg.builder()
                    .tempItem(tempItem)
                    .tempItemImgUrl(img.getImgUrl())
                    .representFlag(img.getRepresentFlag())
                    .itemImgOrLinkStatus(ItemImgOrLinkStatus.ACTIVE)
                    .build()).forEach(tempItemImgRepository::save);
        }

        // tempItemLink 모두 삭제 후 변경
        tempItemLinkRepository.deleteAllByTempItemId(tempItem.getId());
        if(dto.getLinkList() != null) {
            dto.getLinkList().stream().map(link -> TempItemLink.builder()
                    .tempItem(tempItem)
                    .tempItemLinkUrl(link.getItemLinkUrl())
                    .linkName(link.getLinkName())
                    .itemImgOrLinkStatus(ItemImgOrLinkStatus.ACTIVE)
                    .build()).forEach(tempItemLinkRepository::save);
        }

        // tempItemHashtag 모두 삭제 후 변경
        tempItemHashtagRepository.deleteAllByTempItemId(tempItem.getId());
        if(dto.getHashTagIdList() != null) {
            dto.getHashTagIdList().stream().map(hashtag -> TempItemHashtag.builder()
                    .tempItem(tempItem)
                    .hashtag(hashtagRepository.findById(hashtag).orElseThrow(HashtagNotFoundException::new))
                    .build()).forEach(tempItemHashtagRepository::save);
        }

        tempItemRepository.save(tempItem);

        // Recent Search Celeb 테이블에 추가
        recentSearchCelebRepository.save(RecentSelectCeleb.builder()
                .user(user)
                .celeb(celeb)
                .newCeleb(newCeleb)
                .build()
        );

        recentSelectBrandRepository.save(RecentSelectBrand.builder()
                .user(user)
                .brand(brand)
                .newBrand(newBrand)
                .build()
        );

    }

    @Transactional
    public void deleteTempItem(Long tempItemId){
        // 관련된 삭제
        // 1. tempItemImg 삭제
        tempItemImgRepository.deleteAllByTempItemId(tempItemId);
        // 2. tempItemLink 삭제
        tempItemLinkRepository.deleteAllByTempItemId(tempItemId);
        // 3. tempItemHashtag 삭제
        tempItemHashtagRepository.deleteAllByTempItemId(tempItemId);

        // tempItem 삭제
        tempItemRepository.deleteById(tempItemId);
    }

    @Transactional
    public void deleteAllTempItem(User user){
        // 1. 해당 유저의 모든 TempItem 조회
        List<TempItem> tempItemList = tempItemRepository.findAllByUserId(user.getId());

        // 2. 모든 TempItem에 대한 관련된 삭제
        tempItemList.forEach(tempItem -> {
            // 2-1. tempItemImg 삭제
            tempItemImgRepository.deleteAllByTempItemId(tempItem.getId());
            // 2-2. tempItemLink 삭제
            tempItemLinkRepository.deleteAllByTempItemId(tempItem.getId());
            // 2-3. tempItemHashtag 삭제
            tempItemHashtagRepository.deleteAllByTempItemId(tempItem.getId());
        });

        // 3. 모든 TempItem 삭제
        tempItemRepository.deleteAllByUserId(user.getId());
    }

}
