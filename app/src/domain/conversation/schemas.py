from uuid import UUID
from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field


# Message schemas
class MessageCreate(BaseModel):
    role: str  # "user" or "assistant"
    content: str


class MessageResponse(BaseModel):
    id: UUID
    conversation_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True


# Message Analytics schemas
class MessageAnalyticsResponse(BaseModel):
    response_time: Optional[float] = None
    input_tokens: int
    output_tokens: int
    model_name: Optional[str] = None

    class Config:
        from_attributes = True


# Conversation Analytics schemas
class ConversationAnalyticsResponse(BaseModel):
    total_messages: int
    total_response_time: float
    average_response_time: float
    total_input_tokens: int
    total_output_tokens: int

    class Config:
        from_attributes = True


# Tag schemas
class ConversationTagCreate(BaseModel):
    name: str = Field(..., max_length=100)


class ConversationTagResponse(BaseModel):
    id: UUID
    name: str
    created_at: datetime

    class Config:
        from_attributes = True


# Conversation schemas
class ConversationCreate(BaseModel):
    title: str = Field(..., max_length=255)
    description: Optional[str] = None
    model: str = Field(..., max_length=50)


class ConversationUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    description: Optional[str] = None


class ConversationResponse(BaseModel):
    id: UUID
    user_id: UUID
    title: str
    description: Optional[str]
    model: str
    created_at: datetime
    updated_at: datetime
    message_count: Optional[int] = 0
    tags: Optional[List[ConversationTagResponse]] = []

    class Config:
        from_attributes = True


class ConversationDetailResponse(ConversationResponse):
    messages: Optional[List[MessageResponse]] = []
    analytics: Optional[ConversationAnalyticsResponse] = None


# Conversation list with pagination
class ConversationListResponse(BaseModel):
    total: int
    page: int
    page_size: int
    conversations: List[ConversationResponse]


# Export schemas
class ConversationExportData(BaseModel):
    conversation: ConversationResponse
    messages: List[MessageResponse]
    analytics: Optional[ConversationAnalyticsResponse]
    tags: List[ConversationTagResponse]


# Search schemas
class ConversationSearchResult(BaseModel):
    id: UUID
    title: str
    description: Optional[str]
    model: str
    created_at: datetime
    match_type: str  # "title", "description", "content"
    match_excerpt: str


class ConversationSearchResponse(BaseModel):
    total: int
    results: List[ConversationSearchResult]
