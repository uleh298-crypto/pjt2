"""알림 관련 모델"""
from sqlalchemy import Column, BigInteger, String, Boolean, TIMESTAMP, Integer, ForeignKey, Text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func

from app.database import Base


class AlertType(Base):
    """알림 유형 코드 테이블"""
    __tablename__ = "alert_type"

    code = Column(String(30), primary_key=True)
    name = Column(String(100), nullable=False)
    category = Column(String(30), nullable=False)  # ETF / PORTFOLIO / NEWS / SYSTEM
    description = Column(String(200))
    is_active = Column(Boolean, default=True)
    display_order = Column(Integer, default=0)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<AlertType({self.code})>"


class AlertMessageTemplate(Base):
    """알림 메시지 템플릿 (버전 관리)"""
    __tablename__ = "alert_message_template"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    alert_type_code = Column(String(30), ForeignKey("alert_type.code", ondelete="CASCADE"), nullable=False)
    version = Column(String(20), nullable=False)
    title_template = Column(String(200), nullable=False)
    message_template = Column(Text, nullable=False)
    variables = Column(JSONB)  # ["etf_name", "date"]
    description = Column(String(200))
    is_active = Column(Boolean, default=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def render(self, **kwargs) -> dict:
        """템플릿에 변수를 적용하여 제목과 메시지 반환"""
        return {
            "title": self.title_template.format(**kwargs),
            "message": self.message_template.format(**kwargs)
        }

    def __repr__(self):
        return f"<AlertMessageTemplate({self.alert_type_code}, {self.version}, active={self.is_active})>"


class UserAlert(Base):
    """사용자 알림 (통합 알림 테이블)"""
    __tablename__ = "user_alert"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, nullable=False)  # User FK (다른 서비스에 있어 FK 제외)
    alert_type_code = Column(String(30), ForeignKey("alert_type.code", ondelete="CASCADE"), nullable=False)

    # 참조 대상 (다형성)
    reference_type = Column(String(30))  # ETF / PORTFOLIO / NEWS / DISCLOSURE
    reference_id = Column(BigInteger)  # 참조 대상 ID

    # 알림 내용
    title = Column(String(200), nullable=False)
    message = Column(String)

    # 상태
    is_read = Column(Boolean, default=False)
    read_at = Column(TIMESTAMP(timezone=True))
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<UserAlert(id={self.id}, type={self.alert_type_code}, read={self.is_read})>"


class UserNotificationSetting(Base):
    """사용자별 알림 설정"""
    __tablename__ = "user_notification_setting"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, nullable=False)  # User FK (다른 서비스에 있어 FK 제외)
    alert_type_code = Column(String(30), ForeignKey("alert_type.code", ondelete="CASCADE"), nullable=False)
    is_enabled = Column(Boolean, default=True)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
    updated_at = Column(TIMESTAMP(timezone=True), server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<UserNotificationSetting(user={self.user_id}, type={self.alert_type_code}, enabled={self.is_enabled})>"
