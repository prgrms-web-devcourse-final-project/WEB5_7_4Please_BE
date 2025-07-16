package com.deal4u.fourplease.domain.auction.controller;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genSellerSaleListResponseList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SaleController.class)
class SaleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    AuctionService auctionService;

    @Test
    @DisplayName("GET /api/v1/sales/{sellerId}이 성공하면 id에 해당하는 판매자의 정보와 200을 반환한다")
    void getSalesShouldReturn200() throws Exception {

        Long sellerId = 1L;

        List<SellerSaleListResponse> resp = genSellerSaleListResponseList();

        when(auctionService.findSalesBySellerId(sellerId)).thenReturn(resp);

        mockMvc.perform(
                get("/api/v1/sales/{sellerId}", sellerId)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(resp.get(0).name()))
                .andExpect(jsonPath("$[1].name").value(resp.get(1).name()))
                .andExpect(jsonPath("$[2].name").value(resp.get(2).name()))
                .andDo(print());
    }



}