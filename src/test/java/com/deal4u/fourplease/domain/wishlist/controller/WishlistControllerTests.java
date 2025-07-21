package com.deal4u.fourplease.domain.wishlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auction.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WishlistController.class)
class WishlistControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private MemberRepository memberRepository;

    @Test
    @DisplayName("POST /api/v1/wishlist가 성공하면 200과 생성된 wishlist id를 반환한다")
    void createWishlistShouldReturn200AndWishlistId() throws Exception {

        WishlistCreateRequest req = new WishlistCreateRequest(1L);
        Long resp = 1L;

        when(memberRepository.findAll()).thenReturn(List.of(mock(Member.class)));
        when(wishlistService.save(eq(req), any(Member.class))).thenReturn(resp);

        mockMvc.perform(
                post("/api/v1/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isCreated())
                .andExpect(content().string(resp.toString()))
                .andDo(print());

    }

}