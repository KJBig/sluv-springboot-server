package com.sluv.server.domain.item.service;

import com.sluv.server.domain.brand.entity.Brand;
import com.sluv.server.domain.brand.exception.BrandNotFoundException;
import com.sluv.server.domain.brand.repository.BrandRepository;
import com.sluv.server.domain.celeb.entity.Celeb;
import com.sluv.server.domain.celeb.exception.CelebNotFoundException;
import com.sluv.server.domain.celeb.repository.CelebRepository;
import com.sluv.server.domain.item.dto.TempItemPostReqDto;
import com.sluv.server.domain.item.dto.TempItemResDto;
import com.sluv.server.domain.item.entity.*;
import com.sluv.server.domain.item.entity.hashtag.Hashtag;
import com.sluv.server.domain.item.entity.hashtag.TempItemHashtag;
import com.sluv.server.domain.item.enums.ItemStatus;
import com.sluv.server.domain.item.exception.ItemCategoryNotFoundException;
import com.sluv.server.domain.item.exception.hashtag.NotFoundHashtagException;
import com.sluv.server.domain.item.repository.*;
import com.sluv.server.domain.item.repository.hashtag.HashtagRepository;
import com.sluv.server.domain.item.repository.hashtag.TempItemHashtagRepository;
import com.sluv.server.domain.user.entity.User;
import com.sluv.server.global.common.enums.ItemImgOrLinkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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


    public void postTempItem(User user, TempItemPostReqDto reqDto) {
        Celeb celeb = null;
        if(reqDto.getCelebId() != null){
            celeb = celebRepository.findById(reqDto.getCelebId())
                    .orElseThrow(CelebNotFoundException::new);
        }

        Brand brand = null;
        if(reqDto.getBrandId() != null){
            brand = brandRepository.findById(reqDto.getBrandId())
                    .orElseThrow(BrandNotFoundException::new);
        }
        ItemCategory itemCategory = null;
        if(reqDto.getCategoryId() != null){
            itemCategory = itemCategoryRepository.findById(reqDto.getCategoryId())
                    .orElseThrow(ItemCategoryNotFoundException::new);
        }

        TempItem tempitem = tempItemRepository.save(TempItem
                .builder()
                .user(user)
                .celeb(celeb)
                .newCelebName(reqDto.getNewCelebName())
                .category(itemCategory)
                .brand(brand)
                .newBrandName(reqDto.getNewBrandName())
                .name(reqDto.getItemName())
                .whenDiscovery(reqDto.getWhenDiscovery())
                .whereDiscovery(reqDto.getWhereDiscovery())
                .price(reqDto.getPrice())
                .additionalInfo(reqDto.getAdditionalInfo())
                .infoSource(reqDto.getInfoSource())
                .itemStatus(ItemStatus.ACTIVE)
                .build()
            );
        System.out.println("악");

        // ItemImg 테이블에 추가
        if(reqDto.getImgList() != null) {

            reqDto.getImgList().entrySet().stream()
                            .map(entry -> {
                                boolean flag = entry.getKey() == 1;
                                return TempItemImg.builder()
                                        .tempItem(tempitem)
                                        .tempItemImgUrl(entry.getValue())
                                        .representFlag(flag)
                                        .itemImgOrLinkStatus(ItemImgOrLinkStatus.ACTIVE)
                                        .build();
                            }).forEach(tempItemImgRepository::save);

        }

        // ItemLink 테이블에 추가
        if(reqDto.getLinkList() != null) {
            reqDto.getLinkList().entrySet().stream()
                            .map(entry ->
                                    TempItemLink.builder()
                                            .tempItem(tempitem)
                                            .linkName(entry.getKey())
                                            .tempItemLinkUrl(entry.getValue())
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
                                            .orElseThrow(NotFoundHashtagException::new)
                            )
                            .build()

            ).forEach(tempItemHashtagRepository::save);
        }

    }

//    public List<TempItemResDto> getTempItemList(User user, Pageable pageable){
//
//
//        return tempItemRepository.getTempItemList(user, pageable).stream().map(tempItem -> {
//
//            Map<Long, String> tempImgList = tempItemImgRepository.findAllByTempItem(tempItem)
//                    .stream().map(itemImg -> new Map);
//            List<Hashtag> tempHashtagList = tempItemHashtagRepository.findAllByTempItem(tempItem);
//            Map<String, String>> tempLinkList = tempItemLinkRepository.findAllByTempItem(tempItem);
//
//            return TempItemResDto.builder()
//                            .imgList(tempImgList)
//                            .celeb(tempItem.getCeleb())
//                            .whenDiscovery(tempItem.getWhenDiscovery())
//                            .whereDiscovery(tempItem.getWhereDiscovery())
//                            .category(tempItem.getCategory())
//                            .itemName(tempItem.getName())
//                            .price(tempItem.getPrice())
//                            .additionalInfo(tempItem.getAdditionalInfo())
//                            .hashTagList(tempHashtagList)
//                            .linkList(tempLinkList);
//                }
//            }
//        )
//    }
}
