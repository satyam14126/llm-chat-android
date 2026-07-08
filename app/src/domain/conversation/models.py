from uuid import uuid4

from sqlalchemy import (
    UUID,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Table,
    Text,
    Index,
    func,
)
from sqlalchemy.orm import relationship

from app.src.core.database import Base

# Association table for conversation tags
conversation_tags = Table(
    "conversation_tags_association",
    Base.metadata,
    Column(
        "conversation_id",
        UUID(as_uuid=True),
        ForeignKey("conversations.id", ondelete="CASCADE"),
        primary_key=True,
    ),
    Column(
        "tag_id",
        UUID(as_uuid=True),
        ForeignKey("conversation_tags.id", ondelete="CASCADE"),
        primary_key=True,
    ),
)


class Conversation(Base):
    __tablename__ = "conversations"
    __table_args__ = (
        Index("idx_user_id", "user_id"),
        Index("idx_created_at", "created_at"),
        Index("idx_updated_at", "updated_at"),
    )

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=False)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    model = Column(String(50), nullable=False)  # "grok", "gemini", etc.
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )

    messages = relationship(
        "Message", back_populates="conversation", cascade="all, delete-orphan"
    )
    analytics = relationship(
        "ConversationAnalytics", back_populates="conversation", cascade="all, delete-orphan"
    )
    tags = relationship(
        "ConversationTag",
        secondary=conversation_tags,
        back_populates="conversations",
    )


class Message(Base):
    __tablename__ = "messages"
    __table_args__ = (
        Index("idx_conversation_id", "conversation_id"),
        Index("idx_created_at", "created_at"),
    )

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid4)
    conversation_id = Column(
        UUID(as_uuid=True), ForeignKey("conversations.id", ondelete="CASCADE"), nullable=False
    )
    role = Column(String(20), nullable=False)  # "user", "assistant"
    content = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    conversation = relationship("Conversation", back_populates="messages")
    analytics = relationship(
        "MessageAnalytics", back_populates="message", cascade="all, delete-orphan"
    )


class ConversationAnalytics(Base):
    __tablename__ = "conversation_analytics"
    __table_args__ = (Index("idx_conversation_id_analytics", "conversation_id"),)

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid4)
    conversation_id = Column(
        UUID(as_uuid=True), ForeignKey("conversations.id", ondelete="CASCADE"), nullable=False
    )
    total_messages = Column(Integer, default=0)
    total_response_time = Column(Float, default=0.0)  # in seconds
    average_response_time = Column(Float, default=0.0)  # in seconds
    total_input_tokens = Column(Integer, default=0)
    total_output_tokens = Column(Integer, default=0)
    updated_at = Column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )

    conversation = relationship("Conversation", back_populates="analytics")
    message_analytics = relationship(
        "MessageAnalytics", back_populates="conversation_analytics"
    )


class MessageAnalytics(Base):
    __tablename__ = "message_analytics"
    __table_args__ = (Index("idx_message_id", "message_id"),)

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid4)
    message_id = Column(
        UUID(as_uuid=True), ForeignKey("messages.id", ondelete="CASCADE"), nullable=False
    )
    conversation_analytics_id = Column(
        UUID(as_uuid=True),
        ForeignKey("conversation_analytics.id", ondelete="CASCADE"),
        nullable=True,
    )
    response_time = Column(Float, nullable=True)  # in seconds
    input_tokens = Column(Integer, default=0)
    output_tokens = Column(Integer, default=0)
    model_name = Column(String(50), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    message = relationship("Message", back_populates="analytics")
    conversation_analytics = relationship(
        "ConversationAnalytics", back_populates="message_analytics"
    )


class ConversationTag(Base):
    __tablename__ = "conversation_tags"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=False)
    name = Column(String(100), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    conversations = relationship(
        "Conversation",
        secondary=conversation_tags,
        back_populates="tags",
    )
