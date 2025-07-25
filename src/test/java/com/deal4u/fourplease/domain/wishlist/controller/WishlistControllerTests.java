package com.deal4u.fourplease.domain.wishlist.controller;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genMember;
import static com.deal4u.fourplease.domain.auction.util.TestUtils.genWishlistResponsePageResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auth.BaseTokenTest;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class WishlistControllerTests extends BaseTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .memberId(1L)
                .email("test@nave.com")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        init(member);
    }

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

    @Test
    @DisplayName("GET /api/v1/wishlist가 성공하면 wishlistResponse 리스트와 200을 반환한다")
    void readAllWishlistShouldReturn200() throws Exception {
        Pageable pageable = PageRequest.of(0, 20,
                Sort.Direction.ASC, "createdAt");
        PageResponse<WishlistResponse> resp = genWishlistResponsePageResponse();

        when(memberRepository.findAll()).thenReturn(List.of(Mockito.mock(Member.class)));
        when(wishlistService.findAll(eq(pageable), any(Member.class))).thenReturn(resp);

        mockMvc.perform(
                        get("/api/v1/wishlist")
                                .param("page", "0")
                                .param("size", "20")
                                .param("order", "earliest")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("목도리"))
                .andExpect(jsonPath("$.content[1].name")
                        .value("축구공"))
                .andExpect(jsonPath("$.content[2].name")
                        .value("칫솔"))
                .andDo(print());
    }


    @Test
    @DisplayName("DELETE /api/v1/wishlist/{wishlistId}가 성공하면 wishlist를 삭제하고 204를 반환한다")
    void deleteWishlistShouldReturn204() throws Exception {

        Long wishlistId = 1L;

        mockMvc.perform(
                        delete("/api/v1/wishlist/{wishlistId}", wishlistId)
                ).andExpect(status().isNoContent())
                .andDo(print());

        verify(wishlistService).deleteByWishlistId(eq(wishlistId), any(Member.class));
    }
}