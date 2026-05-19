# 섹터 버블 AI 분석 - Java 구현 계획

## 개요
ETF 클러스터 뷰에서 섹터 버블 클릭 시 AI 분석을 제공하는 기능의 Java 백엔드 구현

## 구현 항목

### 1. Entity 수정/생성

#### 1.1 EtfSectorCluster.java (수정) ✅
- AI 관련 필드 제거 (aiAnalysis, prompt)
- 버블 시각화 전용으로 단순화

#### 1.2 EtfSectorAiHistory.java (신규)
```java
@Entity
@Table(name = "etf_sector_ai_history")
public class EtfSectorAiHistory {
    private Long id;
    private Etf etf;
    private String groupCode;
    private String groupName;
    private BigDecimal weightPct;
    private Integer stockCount;
    private String topStocks;       // JSONB → String
    private String aiAnalysis;
    private String riskLevel;       // LOW / MEDIUM / HIGH
    private String keyPoint;
    private AiPrompt prompt;
    private LocalDate baseDate;
    private LocalDateTime createdAt;
}
```

### 2. Repository 생성

#### 2.1 EtfSectorAiHistoryRepository.java (신규)
```java
public interface EtfSectorAiHistoryRepository extends JpaRepository<EtfSectorAiHistory, Long> {
    // ETF의 특정 섹터에 대한 최신 AI 분석 조회
    Optional<EtfSectorAiHistory> findTopByEtfIdAndGroupCodeOrderByBaseDateDescCreatedAtDesc(
        Long etfId, String groupCode);

    // ETF의 모든 섹터 AI 분석 조회 (최신 base_date 기준)
    @Query("SELECT h FROM EtfSectorAiHistory h WHERE h.etf.id = :etfId AND h.baseDate = (SELECT MAX(h2.baseDate) FROM EtfSectorAiHistory h2 WHERE h2.etf.id = :etfId)")
    List<EtfSectorAiHistory> findLatestByEtfId(@Param("etfId") Long etfId);
}
```

### 3. DTO 수정/생성

#### 3.1 SectorBubbleResponse.java (신규)
섹터 버블 상세 조회 응답 DTO
```java
public class SectorBubbleResponse {
    private String groupCode;
    private String groupName;
    private BigDecimal weightPct;
    private Integer stockCount;
    private BigDecimal posX;
    private BigDecimal posY;
    private BigDecimal radius;

    // AI 분석 정보
    private String aiAnalysis;
    private String riskLevel;
    private String keyPoint;
    private List<TopStockDto> topStocks;
}
```

### 4. Service 수정

#### 4.1 EtfService.java (수정)
- 섹터 버블 상세 조회 메서드 추가
- etf_sector_cluster + etf_sector_ai_history JOIN 조회

### 5. Controller 수정

#### 5. 1 EtfController.java (수정)
```java
// 섹터 버블 상세 조회 (AI 분석 포함)
@GetMapping("/{etfId}/sectors/{groupCode}")
public ResponseEntity<SectorBubbleResponse> getSectorBubbleDetail(
    @PathVariable Long etfId,
    @PathVariable String groupCode);
```

## 구현 순서
1. ~~EtfSectorCluster.java AI 필드 제거~~ ✅
2. ~~EtfSectorAiHistory.java Entity 생성~~ ✅
3. ~~EtfSectorAiHistoryRepository.java 생성~~ ✅
4. ~~SectorBubbleDetailResponse.java DTO 생성~~ ✅
5. ~~Service 수정~~ ✅
6. ~~Controller 수정~~ ✅
7. ~~Python 스크립트 Anthropic API로 변경~~ ✅

## 완료된 파일 목록
- `EtfSectorCluster.java` - AI 필드 제거
- `EtfSectorAiHistory.java` - 신규 Entity
- `EtfSectorAiHistoryRepository.java` - 신규 Repository
- `SectorBubbleDetailResponse.java` - 신규 DTO
- `EtfService.java` - 인터페이스에 메서드 추가
- `EtfServiceImpl.java` - 구현체 수정
- `EtfController.java` - API 엔드포인트 추가
- `generate_sector_bubble_ai.py` - Anthropic API 직접 호출로 변경
- `config.py` - anthropic_api_key 설정 추가

## 사용법
```bash
# .env 파일에 Anthropic API 키 설정
ANTHROPIC_API_KEY=<your-anthropic-api-key>

# 전체 섹터 버블 AI 분석 생성
python -m scripts.generate_sector_bubble_ai

# 특정 ETF만 처리
python -m scripts.generate_sector_bubble_ai --etf-id 1

# 기존 분석 재생성
python -m scripts.generate_sector_bubble_ai --force

# 모델 지정
python -m scripts.generate_sector_bubble_ai --model claude-3-5-sonnet-20241022
```

## API 엔드포인트
```
GET /api/v1/etf/{etfId}/sectors/{groupCode}
```
- ETF 클러스터 뷰에서 섹터 버블 클릭 시 호출
- AI 분석 결과 포함
