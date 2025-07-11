package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductService productService;

    @Transactional
    public void save(AuctionCreateRequest request, Member member) {

        ProductCreateDto productCreateDto = request.toProductCreateDto(member);
        Product product = productService.save(productCreateDto);

        Auction auction = request.toEntity(product);
        auctionRepository.save(auction);

    }
}
