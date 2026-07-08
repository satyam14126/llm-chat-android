import csv
import json
from datetime import datetime
from io import StringIO
from typing import List, Optional, Tuple
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from .models import Conversation, Message
from .repositories import (
    ConversationAnalyticsRepository,
    ConversationRepository,
    ConversationSearchRepository,
    ConversationTagRepository,
    MessageAnalyticsRepository,
    MessageRepository,
)


class ConversationService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.conversation_repo = ConversationRepository(session)
        self.message_repo = MessageRepository(session)
        self.analytics_repo = ConversationAnalyticsRepository(session)
        self.message_analytics_repo = MessageAnalyticsRepository(session)
        self.tag_repo = ConversationTagRepository(session)
        self.search_repo = ConversationSearchRepository(session)

    async def create_conversation(
        self, user_id: UUID, title: str, description: Optional[str], model: str
    ) -> Conversation:
        """Create a new conversation for the user."""
        return await self.conversation_repo.create(user_id, title, description, model)

    async def get_conversation(self, conversation_id: UUID) -> Optional[Conversation]:
        """Get conversation with all messages and analytics."""
        return await self.conversation_repo.get_by_id(conversation_id)

    async def get_user_conversations(
        self,
        user_id: UUID,
        skip: int = 0,
        limit: int = 20,
        sort_by: str = "updated_at",
        order: str = "desc",
    ) -> Tuple[List[Conversation], int]:
        """Get all conversations for a user with pagination."""
        return await self.conversation_repo.get_by_user(user_id, skip, limit, sort_by, order)

    async def update_conversation(
        self,
        conversation_id: UUID,
        title: Optional[str] = None,
        description: Optional[str] = None,
    ) -> Optional[Conversation]:
        """Update conversation title or description."""
        return await self.conversation_repo.update(conversation_id, title, description)

    async def delete_conversation(self, conversation_id: UUID) -> bool:
        """Delete a conversation and all its messages."""
        return await self.conversation_repo.delete(conversation_id)

    async def add_message(
        self,
        conversation_id: UUID,
        role: str,
        content: str,
        response_time: Optional[float] = None,
        input_tokens: int = 0,
        output_tokens: int = 0,
        model_name: Optional[str] = None,
    ) -> Message:
        """Add a message to a conversation and update analytics."""
        message = await self.message_repo.create(conversation_id, role, content)

        analytics = await self.analytics_repo.get_by_conversation(conversation_id)
        if analytics:
            await self.analytics_repo.update_analytics(
                conversation_id, response_time, input_tokens, output_tokens
            )
            await self.message_analytics_repo.create(
                message.id,
                analytics.id,
                response_time,
                input_tokens,
                output_tokens,
                model_name,
            )

        await self.session.commit()
        return message

    async def get_conversation_messages(self, conversation_id: UUID) -> List[Message]:
        """Get all messages in a conversation."""
        return await self.message_repo.get_by_conversation(conversation_id)

    async def search_conversations(
        self, user_id: UUID, query: str, limit: int = 20
    ) -> List[dict]:
        """Search conversations by title, description, or message content."""
        return await self.search_repo.search(user_id, query, limit)

    async def export_conversation_json(self, conversation_id: UUID) -> str:
        """Export conversation as JSON."""
        conversation = await self.get_conversation(conversation_id)
        if not conversation:
            raise ValueError("Conversation not found")

        export_data = {
            "id": str(conversation.id),
            "title": conversation.title,
            "description": conversation.description,
            "model": conversation.model,
            "created_at": conversation.created_at.isoformat(),
            "updated_at": conversation.updated_at.isoformat(),
            "messages": [
                {
                    "id": str(msg.id),
                    "role": msg.role,
                    "content": msg.content,
                    "created_at": msg.created_at.isoformat(),
                }
                for msg in conversation.messages
            ],
            "analytics": {
                "total_messages": conversation.analytics[0].total_messages
                if conversation.analytics
                else 0,
                "total_response_time": conversation.analytics[0].total_response_time
                if conversation.analytics
                else 0.0,
                "average_response_time": conversation.analytics[0].average_response_time
                if conversation.analytics
                else 0.0,
                "total_input_tokens": conversation.analytics[0].total_input_tokens
                if conversation.analytics
                else 0,
                "total_output_tokens": conversation.analytics[0].total_output_tokens
                if conversation.analytics
                else 0,
            }
            if conversation.analytics
            else None,
            "tags": [{"id": str(tag.id), "name": tag.name} for tag in conversation.tags],
        }

        return json.dumps(export_data, indent=2)

    async def export_conversation_csv(self, conversation_id: UUID) -> str:
        """Export conversation messages as CSV."""
        conversation = await self.get_conversation(conversation_id)
        if not conversation:
            raise ValueError("Conversation not found")

        output = StringIO()
        writer = csv.writer(output)

        writer.writerow(["Message ID", "Role", "Content", "Created At"])
        for msg in conversation.messages:
            writer.writerow(
                [
                    str(msg.id),
                    msg.role,
                    msg.content,
                    msg.created_at.isoformat(),
                ]
            )

        return output.getvalue()

    async def create_tag(self, user_id: UUID, name: str):
        """Create a new tag for conversations."""
        return await self.tag_repo.create(user_id, name)

    async def get_user_tags(self, user_id: UUID) -> List:
        """Get all tags for a user."""
        return await self.tag_repo.get_by_user(user_id)

    async def add_tag_to_conversation(self, conversation_id: UUID, tag_id: UUID) -> bool:
        """Add a tag to a conversation."""
        return await self.tag_repo.add_to_conversation(conversation_id, tag_id)

    async def remove_tag_from_conversation(
        self, conversation_id: UUID, tag_id: UUID
    ) -> bool:
        """Remove a tag from a conversation."""
        return await self.tag_repo.remove_from_conversation(conversation_id, tag_id)

    async def delete_tag(self, tag_id: UUID) -> bool:
        """Delete a tag."""
        return await self.tag_repo.delete(tag_id)

    async def get_conversation_analytics(self, conversation_id: UUID):
        """Get analytics for a conversation."""
        return await self.analytics_repo.get_by_conversation(conversation_id)
