from typing import List, Optional
from uuid import UUID

from sqlalchemy import and_, func, or_, select, desc
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from .models import (
    Conversation,
    ConversationAnalytics,
    ConversationTag,
    Message,
    MessageAnalytics,
)


class ConversationRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create(
        self, user_id: UUID, title: str, description: Optional[str], model: str
    ) -> Conversation:
        conversation = Conversation(
            user_id=user_id, title=title, description=description, model=model
        )
        self.session.add(conversation)
        await self.session.flush()

        analytics = ConversationAnalytics(conversation_id=conversation.id)
        self.session.add(analytics)
        await self.session.flush()

        return conversation

    async def get_by_id(self, conversation_id: UUID) -> Optional[Conversation]:
        stmt = (
            select(Conversation)
            .where(Conversation.id == conversation_id)
            .options(
                selectinload(Conversation.messages),
                selectinload(Conversation.analytics),
                selectinload(Conversation.tags),
            )
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_user(
        self,
        user_id: UUID,
        skip: int = 0,
        limit: int = 20,
        sort_by: str = "updated_at",
        order: str = "desc",
    ) -> tuple[List[Conversation], int]:
        count_stmt = select(func.count(Conversation.id)).where(
            Conversation.user_id == user_id
        )
        count_result = await self.session.execute(count_stmt)
        total = count_result.scalar()

        sort_column = getattr(Conversation, sort_by, Conversation.updated_at)
        order_func = desc if order == "desc" else lambda x: x

        stmt = (
            select(Conversation)
            .where(Conversation.user_id == user_id)
            .options(selectinload(Conversation.tags))
            .order_by(order_func(sort_column))
            .offset(skip)
            .limit(limit)
        )
        result = await self.session.execute(stmt)
        conversations = result.scalars().all()
        return conversations, total

    async def update(
        self, conversation_id: UUID, title: Optional[str], description: Optional[str]
    ) -> Optional[Conversation]:
        stmt = select(Conversation).where(Conversation.id == conversation_id)
        result = await self.session.execute(stmt)
        conversation = result.scalar_one_or_none()

        if conversation:
            if title is not None:
                conversation.title = title
            if description is not None:
                conversation.description = description
            await self.session.flush()

        return conversation

    async def delete(self, conversation_id: UUID) -> bool:
        stmt = select(Conversation).where(Conversation.id == conversation_id)
        result = await self.session.execute(stmt)
        conversation = result.scalar_one_or_none()

        if conversation:
            await self.session.delete(conversation)
            await self.session.flush()
            return True
        return False


class MessageRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create(
        self, conversation_id: UUID, role: str, content: str
    ) -> Message:
        message = Message(conversation_id=conversation_id, role=role, content=content)
        self.session.add(message)
        await self.session.flush()
        return message

    async def get_by_conversation(self, conversation_id: UUID) -> List[Message]:
        stmt = (
            select(Message)
            .where(Message.conversation_id == conversation_id)
            .order_by(Message.created_at)
            .options(selectinload(Message.analytics))
        )
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_by_id(self, message_id: UUID) -> Optional[Message]:
        stmt = select(Message).where(Message.id == message_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def delete_by_conversation(self, conversation_id: UUID) -> int:
        stmt = select(Message).where(Message.conversation_id == conversation_id)
        result = await self.session.execute(stmt)
        messages = result.scalars().all()
        for message in messages:
            await self.session.delete(message)
        await self.session.flush()
        return len(messages)


class ConversationAnalyticsRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_conversation(
        self, conversation_id: UUID
    ) -> Optional[ConversationAnalytics]:
        stmt = (
            select(ConversationAnalytics)
            .where(ConversationAnalytics.conversation_id == conversation_id)
            .options(selectinload(ConversationAnalytics.message_analytics))
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def update_analytics(
        self,
        conversation_id: UUID,
        response_time: Optional[float] = None,
        input_tokens: int = 0,
        output_tokens: int = 0,
    ) -> ConversationAnalytics:
        stmt = select(ConversationAnalytics).where(
            ConversationAnalytics.conversation_id == conversation_id
        )
        result = await self.session.execute(stmt)
        analytics = result.scalar_one_or_none()

        if analytics:
            analytics.total_messages += 1
            if response_time:
                analytics.total_response_time += response_time
                analytics.average_response_time = (
                    analytics.total_response_time / analytics.total_messages
                )
            analytics.total_input_tokens += input_tokens
            analytics.total_output_tokens += output_tokens
            await self.session.flush()

        return analytics


class MessageAnalyticsRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create(
        self,
        message_id: UUID,
        conversation_analytics_id: UUID,
        response_time: Optional[float] = None,
        input_tokens: int = 0,
        output_tokens: int = 0,
        model_name: Optional[str] = None,
    ) -> MessageAnalytics:
        analytics = MessageAnalytics(
            message_id=message_id,
            conversation_analytics_id=conversation_analytics_id,
            response_time=response_time,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            model_name=model_name,
        )
        self.session.add(analytics)
        await self.session.flush()
        return analytics


class ConversationTagRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create(self, user_id: UUID, name: str) -> ConversationTag:
        tag = ConversationTag(user_id=user_id, name=name)
        self.session.add(tag)
        await self.session.flush()
        return tag

    async def get_by_user(self, user_id: UUID) -> List[ConversationTag]:
        stmt = select(ConversationTag).where(ConversationTag.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_by_id(self, tag_id: UUID) -> Optional[ConversationTag]:
        stmt = select(ConversationTag).where(ConversationTag.id == tag_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def add_to_conversation(
        self, conversation_id: UUID, tag_id: UUID
    ) -> bool:
        conversation_stmt = select(Conversation).where(
            Conversation.id == conversation_id
        )
        conversation_result = await self.session.execute(conversation_stmt)
        conversation = conversation_result.scalar_one_or_none()

        tag_stmt = select(ConversationTag).where(ConversationTag.id == tag_id)
        tag_result = await self.session.execute(tag_stmt)
        tag = tag_result.scalar_one_or_none()

        if conversation and tag:
            conversation.tags.append(tag)
            await self.session.flush()
            return True
        return False

    async def remove_from_conversation(
        self, conversation_id: UUID, tag_id: UUID
    ) -> bool:
        conversation_stmt = select(Conversation).where(
            Conversation.id == conversation_id
        )
        conversation_result = await self.session.execute(conversation_stmt)
        conversation = conversation_result.scalar_one_or_none()

        tag_stmt = select(ConversationTag).where(ConversationTag.id == tag_id)
        tag_result = await self.session.execute(tag_stmt)
        tag = tag_result.scalar_one_or_none()

        if conversation and tag and tag in conversation.tags:
            conversation.tags.remove(tag)
            await self.session.flush()
            return True
        return False

    async def delete(self, tag_id: UUID) -> bool:
        stmt = select(ConversationTag).where(ConversationTag.id == tag_id)
        result = await self.session.execute(stmt)
        tag = result.scalar_one_or_none()

        if tag:
            await self.session.delete(tag)
            await self.session.flush()
            return True
        return False


class ConversationSearchRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def search(
        self, user_id: UUID, query: str, limit: int = 20
    ) -> List[tuple]:
        # Search in conversation titles, descriptions, and message content
        results = []

        # Search in titles
        title_stmt = (
            select(Conversation)
            .where(
                and_(
                    Conversation.user_id == user_id,
                    Conversation.title.ilike(f"%{query}%"),
                )
            )
            .limit(limit)
        )
        title_result = await self.session.execute(title_stmt)
        for conv in title_result.scalars().all():
            results.append(
                {
                    "conversation_id": conv.id,
                    "title": conv.title,
                    "description": conv.description,
                    "model": conv.model,
                    "created_at": conv.created_at,
                    "match_type": "title",
                    "match_excerpt": conv.title[:100],
                }
            )

        # Search in descriptions
        desc_stmt = (
            select(Conversation)
            .where(
                and_(
                    Conversation.user_id == user_id,
                    Conversation.description.ilike(f"%{query}%"),
                )
            )
            .limit(limit)
        )
        desc_result = await self.session.execute(desc_stmt)
        for conv in desc_result.scalars().all():
            if conv.id not in [r["conversation_id"] for r in results]:
                results.append(
                    {
                        "conversation_id": conv.id,
                        "title": conv.title,
                        "description": conv.description,
                        "model": conv.model,
                        "created_at": conv.created_at,
                        "match_type": "description",
                        "match_excerpt": conv.description[:100] if conv.description else "",
                    }
                )

        # Search in message content
        message_stmt = (
            select(Message, Conversation)
            .join(Conversation, Message.conversation_id == Conversation.id)
            .where(
                and_(
                    Conversation.user_id == user_id,
                    Message.content.ilike(f"%{query}%"),
                )
            )
            .limit(limit)
        )
        message_result = await self.session.execute(message_stmt)
        for message, conv in message_result.all():
            if conv.id not in [r["conversation_id"] for r in results]:
                results.append(
                    {
                        "conversation_id": conv.id,
                        "title": conv.title,
                        "description": conv.description,
                        "model": conv.model,
                        "created_at": conv.created_at,
                        "match_type": "content",
                        "match_excerpt": message.content[:100],
                    }
                )

        return results
