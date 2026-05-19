"""뉴스 중복 제거 유틸리티"""
import re
from difflib import SequenceMatcher
from typing import List, Optional
from sqlalchemy.orm import Session
from app.models.news import NewsArticle


def normalize_title(title: str) -> str:
    """제목 정규화 - 비교를 위해 특수문자, 공백 정리"""
    if not title:
        return ""
    # 특수문자 제거
    title = re.sub(r'[^\w\s가-힣]', '', title)
    # 연속 공백을 하나로
    title = re.sub(r'\s+', ' ', title)
    # 앞뒤 공백 제거 및 소문자 변환
    return title.strip().lower()


def calculate_similarity(title1: str, title2: str) -> float:
    """두 제목의 유사도 계산 (0.0 ~ 1.0)"""
    t1 = normalize_title(title1)
    t2 = normalize_title(title2)

    if not t1 or not t2:
        return 0.0

    return SequenceMatcher(None, t1, t2).ratio()


def is_duplicate_by_url(db: Session, url: str) -> bool:
    """URL 기반 중복 체크"""
    if not url:
        return False
    exists = db.query(NewsArticle).filter(
        NewsArticle.source_url == url
    ).first()
    return exists is not None


def is_duplicate_by_title(db: Session, title: str, threshold: float = 0.85,
                          hours_window: int = 24) -> Optional[NewsArticle]:
    """
    제목 유사도 기반 중복 체크
    - threshold: 유사도 임계값 (기본 0.85)
    - hours_window: 최근 N시간 내 뉴스만 비교 (기본 24시간)

    Returns: 중복인 경우 기존 뉴스 반환, 아니면 None
    """
    from datetime import datetime, timedelta

    if not title:
        return None

    normalized_new = normalize_title(title)
    cutoff_time = datetime.now() - timedelta(hours=hours_window)

    # 최근 뉴스들과 비교
    recent_news = db.query(NewsArticle).filter(
        NewsArticle.created_at >= cutoff_time
    ).all()

    for news in recent_news:
        if not news.title:
            continue
        similarity = calculate_similarity(title, news.title)
        if similarity >= threshold:
            return news

    return None


def check_duplicate(db: Session, title: str, url: str,
                    similarity_threshold: float = 0.85) -> dict:
    """
    종합 중복 체크

    Returns:
        {
            "is_duplicate": bool,
            "reason": str,  # "url" | "title_similarity" | None
            "existing_news": NewsArticle | None,
            "similarity": float | None
        }
    """
    # 1. URL 중복 체크
    if url and is_duplicate_by_url(db, url):
        return {
            "is_duplicate": True,
            "reason": "url",
            "existing_news": None,
            "similarity": 1.0
        }

    # 2. 제목 유사도 체크
    existing = is_duplicate_by_title(db, title, threshold=similarity_threshold)
    if existing:
        similarity = calculate_similarity(title, existing.title)
        return {
            "is_duplicate": True,
            "reason": "title_similarity",
            "existing_news": existing,
            "similarity": similarity
        }

    return {
        "is_duplicate": False,
        "reason": None,
        "existing_news": None,
        "similarity": None
    }


class DuplicateChecker:
    """배치 처리를 위한 중복 체커 클래스"""

    def __init__(self, db: Session, similarity_threshold: float = 0.85):
        self.db = db
        self.threshold = similarity_threshold
        self._cache: List[str] = []  # 현재 배치에서 추가된 제목들

    def is_duplicate(self, title: str, url: str) -> bool:
        """중복 여부 확인"""
        # URL 체크
        if url and is_duplicate_by_url(self.db, url):
            return True

        # DB 내 제목 유사도 체크
        if is_duplicate_by_title(self.db, title, self.threshold):
            return True

        # 현재 배치 내 중복 체크
        normalized = normalize_title(title)
        for cached_title in self._cache:
            if calculate_similarity(normalized, cached_title) >= self.threshold:
                return True

        # 캐시에 추가
        self._cache.append(normalized)
        return False

    def clear_cache(self):
        """배치 처리 완료 후 캐시 초기화"""
        self._cache.clear()
