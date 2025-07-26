package com.deal4u.fourplease.domain.auction.controller;

import static com.deal4u.fourplease.testutil.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.testutil.TestUtils.genAuctionDetailResponse;
import static com.deal4u.fourplease.testutil.TestUtils.genAuctionListResponsePageResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionImageUrlResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionSearchRequest;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.auction.service.SaveAuctionImageService;
import com.deal4u.fourplease.domain.auth.BaseTokenTest;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuctionControllerTests extends BaseTokenTest {

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
    @DisplayName("POST /api/v1/auctions가 성공하면 경매를 등록한 후 201을 반환한다")
    void createAuctionShouldReturn201() throws Exception {

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
    void readAuctionShouldReturn200() throws Exception {

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
    void deleteAuctionShouldReturn204() throws Exception {

        Long auctionId = 1L;

        mockMvc.perform(
                        delete("/api/v1/auctions/{auctionId}", auctionId)
                ).andExpect(status().isNoContent())
                .andDo(print());

        verify(auctionService).deleteByAuctionId(eq(auctionId), any(Member.class));
    }

    @Test
    @DisplayName("POST /api/v1/auctions/images가 성공하면 200를 반환한다")
    void upload_image_should_return200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        Member member = Mockito.mock(Member.class);
        when(member.getNickName()).thenReturn("test");
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(saveAuctionImageService.upload(member, file)).thenReturn(
                new AuctionImageUrlResponse("test.com")
        );

        mockMvc.perform(multipart("/api/v1/auctions/images").file(file)
                ).andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/v1/auctions/images 이미지 파일이 아니면 400를 반환한다")
    void upload_image_should_return400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.txt",
                "plain/text",
                new byte[]{1, 2, 3, 4}
        );

        Member member = Mockito.mock(Member.class);
        when(member.getNickName()).thenReturn("test");
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(saveAuctionImageService.upload(member, file)).thenThrow(
                ErrorCode.INVALID_IMAGE_TYPE.toException()
        );

        mockMvc.perform(multipart("/api/v1/auctions/images").file(file)
                ).andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/v1/auctions/images 잘못된 파일이 아니면 400를 반환한다")
    void upload_image_is_invalid_file_should_return400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.txt",
                "plain/png",
                new byte[]{1, 2, 3, 4}
        );

        Member member = Mockito.mock(Member.class);
        when(member.getNickName()).thenReturn("test");
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(saveAuctionImageService.upload(member, file)).thenThrow(
                ErrorCode.INVALID_FILE.toException()
        );

        mockMvc.perform(multipart("/api/v1/auctions/images").file(file)
                ).andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("GET /api/v1/auctions가 성공하면 전체 경매 목록과 200을 반환한다")
    void readAllAuctionsShouldReturn200() throws Exception {

        AuctionSearchRequest req = new AuctionSearchRequest(
                0,
                20,
                "",
                4L,
                "latest"
        );

        PageResponse<AuctionListResponse> resp = genAuctionListResponsePageResponse();

        when(auctionService.findAll(req)).thenReturn(resp);

        mockMvc.perform(
                        get("/api/v1/auctions")
                                .param("page", "0")
                                .param("size", "20")
                                .param("keyword", req.keyword())
                                .param("categoryId", String.valueOf(req.categoryId()))
                                .param("order", req.order())
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
