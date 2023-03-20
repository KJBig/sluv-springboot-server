package com.sluv.server.domain.brand.service;

import com.sluv.server.domain.brand.dto.BrandSearchResDto;
import com.sluv.server.domain.brand.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

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
}
