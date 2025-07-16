package com.deal4u.fourplease.domain.auction.controller;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuctionDetailResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.auction.service.SaveAuctionImageService;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuctionController.class)
class AuctionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuctionService auctionService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private SaveAuctionImageService saveAuctionImageService;

    @Test
    @DisplayName("POST /api/v1/auctions가 성공하면 경매를 등록한 후 201을 반환한다")
    void create_auction_should_return201() throws Exception {

        AuctionCreateRequest req = genAuctionCreateRequest();
        when(memberRepository.findAll()).thenReturn(List.of(Mockito.mock(Member.class)));
        mockMvc.perform(
                        post("/api/v1/auctions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isCreated())
                .andDo(print());

        verify(auctionService).save(any(AuctionCreateRequest.class), any(Member.class));

    }

    @Test
    @DisplayName("GET /api/v1/auctions/{auctionId}/description이 성공하면 Id의 경매 정보와 200을 반환한다")
    void read_auction_should_return200() throws Exception {

        Long auctionId = 1L;
        AuctionDetailResponse resp = genAuctionDetailResponse();

        when(auctionService.getByAuctionId(auctionId)).thenReturn(resp);

        mockMvc.perform(
                        get("/api/v1/auctions/{auctionId}/description", auctionId)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value(resp.productName()))
                .andExpect(jsonPath("$.description").value(resp.description()))
                .andDo(print());

    }

    @Test
    @DisplayName("DELETE /api/v1/auctions/{auctionId}가 성공하면 soft delete 후 204를 반환한다")
    void delete_auction_should_return204() throws Exception {

        Long auctionId = 1L;

        mockMvc.perform(
                        delete("/api/v1/auctions/{auctionId}", auctionId)
                ).andExpect(status().isNoContent())
                .andDo(print());

        verify(auctionService).deleteByAuctionId(auctionId);
    }
}