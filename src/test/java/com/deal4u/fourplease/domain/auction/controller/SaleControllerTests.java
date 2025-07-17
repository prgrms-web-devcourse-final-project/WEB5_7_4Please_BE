package com.deal4u.fourplease.domain.auction.controller;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genSellerSaleListResponsePageResponse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auction.dto.PageResponse;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SaleControllerTests {

    @MockitoBean
    AuctionService auctionService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/sales/{sellerId}이 성공하면 id에 해당하는 판매자의 정보와 200을 반환한다")
    void getSalesShouldReturn200() throws Exception {

        Long sellerId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        PageResponse<SellerSaleListResponse> resp = genSellerSaleListResponsePageResponse();

        when(auctionService.findSalesBySellerId(sellerId, pageable)).thenReturn(resp);

        mockMvc.perform(
                        get("/api/v1/sales/{sellerId}", sellerId)
                                .param("page", "0")
                                .param("size", "20")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value(resp.getContent().get(0).name()))
                .andExpect(jsonPath("$.content[1].name")
                        .value(resp.getContent().get(1).name()))
                .andExpect(jsonPath("$.content[2].name")
                        .value(resp.getContent().get(2).name()))
                .andExpect(jsonPath("$.totalElements").value(resp.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(resp.getTotalPages()))
                .andExpect(jsonPath("$.page").value(resp.getPage()))
                .andExpect(jsonPath("$.size").value(resp.getSize()))
                .andDo(print());
    }


}