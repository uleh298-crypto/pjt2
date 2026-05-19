"""ETF 공시 모델 (상장폐지, 정리매매, 투자유의 등)"""
from sqlalchemy import Column, BigInteger, Text, String, Date, TIMESTAMP, func
from app.database import Base
import enum


class DisclosureType(str, enum.Enum):
    """공시 유형"""
    DELISTING = "delisting"           # 상장폐지 결정
    LIQUIDATION = "liquidation"       # 정리매매 지정
    CAUTION = "caution"               # 투자유의 지정
    SURVEILLANCE = "surveillance"     # 투자경고
    OTHER = "other"                   # 기타


class EtfDisclosure(Base):
    """ETF 공시 정보 테이블"""
    __tablename__ = "etf_disclosure"

    id = Column(BigInteger, primary_key=True, autoincrement=True)

    # ETF 정보
    etf_code = Column(String(20), nullable=False, index=True)  # 종목코드
    etf_name = Column(String(200), nullable=False)             # 종목명

    # 공시 정보
    disclosure_type = Column(String(50), nullable=False)       # 공시 유형
    disclosure_title = Column(Text, nullable=False)            # 공시 제목
    disclosure_content = Column(Text)                          # 공시 내용 요약

    # 날짜 정보
    disclosure_date = Column(Date, nullable=False)             # 공시일
    effective_date = Column(Date)                              # 효력 발생일 (상장폐지일 등)

    # 출처
    source_url = Column(Text)                                  # KIND 원문 URL

    # 처리 상태
    is_notified = Column(String(1), default='N')               # 알림 발송 여부

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<EtfDisclosure {self.etf_code} - {self.disclosure_type}>"
