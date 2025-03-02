package com.sluv.server.domain.brand.service;

import com.sluv.server.domain.brand.dto.BrandSearchResDto;
import com.sluv.server.domain.brand.dto.RecentSelectBrandResDto;
import com.sluv.server.domain.brand.entity.RecentSelectBrand;
import com.sluv.server.domain.brand.repository.BrandRepository;
import com.sluv.server.domain.brand.repository.RecentSelectBrandRepository;
import com.sluv.server.domain.user.entity.User;
import com.sluv.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final RecentSelectBrandRepository recentSelectBrandRepository;
    private final UserRepository userRepository;


    public List<BrandSearchResDto> findAllBrand(String brandName, Pageable pageable){

        return brandRepository.findByAllBrandKrOrBrandEnStartingWith(brandName, pageable).stream()
                                                                    .map(data -> BrandSearchResDto.builder()
                                                                                                    .id(data.getId())
                                                                                                    .brandKr(data.getBrandKr())
                                                                                                    .brandEn(data.getBrandEn())
                                                                                                    .brandImgUrl(data.getBrandImgUrl())
                                                                                                    .build()
                                                                    ).collect(Collectors.toList());
    }

    public List<BrandSearchResDto> findTopBrand() {
        return brandRepository.findTop10By().stream()
                                            .map(data -> BrandSearchResDto.builder()
                                                                            .id(data.getId())
                                                                            .brandKr(data.getBrandKr())
                                                                            .brandEn(data.getBrandEn())
                                                                            .brandImgUrl(data.getBrandImgUrl())
                                                                            .build()
                                            ).collect(Collectors.toList());
    }

    public List<RecentSelectBrandResDto> findRecentSelectBrand(User user) {

        List<RecentSelectBrand> recentSelectBrandList = recentSelectBrandRepository.getRecentSelectBrandTop20(user);

        return recentSelectBrandList.stream().map(recentSelectBrand -> {
            Long brandId;
            String brandName;
            String flag = recentSelectBrand.getBrand() != null ? "Y" :"N";
            if(flag.equals("Y")){
                brandId = recentSelectBrand.getBrand().getId();
                brandName = recentSelectBrand.getBrand().getBrandKr();
            }else{
                brandId = recentSelectBrand.getNewBrand().getId();
                brandName = recentSelectBrand.getNewBrand().getBrandName();
            }
            return RecentSelectBrandResDto.builder()
                    .id(brandId)
                    .brandName(brandName)
                    .flag(flag)
                    .build();
        }).toList();
    }
}

