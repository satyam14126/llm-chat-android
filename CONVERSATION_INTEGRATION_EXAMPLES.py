"""
Example integration showing how to use the conversation management system
with the existing LLM chat client.
"""

import asyncio
import time
from uuid import UUID
from typing import Optional

from sqlalchemy.ext.asyncio import AsyncSession

from app.src.domain.conversation.services import ConversationService
from app.src.clients.chat_client import ChatClient  # Your existing chat client


class IntegratedChatManager:
    """Manages conversations with integrated LLM chat and analytics tracking."""

    def __init__(self, session: AsyncSession):
        self.conversation_service = ConversationService(session)
        self.chat_client = ChatClient()  # Your existing chat client

    async def create_conversation_session(
        self,
        user_id: UUID,
        title: str,
        description: Optional[str] = None,
        model: str = "grok",
    ) -> UUID:
        """Create a new conversation session."""
        conversation = await self.conversation_service.create_conversation(
            user_id, title, description, model
        )
        return conversation.id

    async def send_message_and_track(
        self,
        conversation_id: UUID,
        user_message: str,
        model_name: str = "grok",
    ) -> dict:
        """
        Send a message to LLM, get response, and track analytics.

        Returns:
            dict with user_message, assistant_response, and metrics
        """

        # 1. Add user message to conversation
        await self.conversation_service.add_message(
            conversation_id, "user", user_message
        )

        # 2. Get LLM response with timing
        start_time = time.time()
        try:
            # This calls your existing LLM client
            response = await self.chat_client.send_message(
                user_message, model=model_name
            )
            response_time = time.time() - start_time

            assistant_message = response.get("content", "")
            input_tokens = response.get("input_tokens", 0)
            output_tokens = response.get("output_tokens", 0)

        except Exception as e:
            response_time = time.time() - start_time
            assistant_message = f"Error: {str(e)}"
            input_tokens = 0
            output_tokens = 0

        # 3. Add assistant message with analytics
        await self.conversation_service.add_message(
            conversation_id,
            "assistant",
            assistant_message,
            response_time=response_time,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            model_name=model_name,
        )

        return {
            "user_message": user_message,
            "assistant_response": assistant_message,
            "response_time": response_time,
            "input_tokens": input_tokens,
            "output_tokens": output_tokens,
        }

    async def get_conversation_summary(
        self, conversation_id: UUID
    ) -> dict:
        """Get full conversation with analytics."""
        conversation = (
            await self.conversation_service.get_conversation(conversation_id)
        )
        analytics = (
            await self.conversation_service.get_conversation_analytics(conversation_id)
        )

        return {
            "id": str(conversation.id),
            "title": conversation.title,
            "model": conversation.model,
            "message_count": len(conversation.messages),
            "analytics": {
                "total_response_time": analytics.total_response_time,
                "average_response_time": analytics.average_response_time,
                "total_tokens_used": (
                    analytics.total_input_tokens + analytics.total_output_tokens
                ),
                "input_tokens": analytics.total_input_tokens,
                "output_tokens": analytics.total_output_tokens,
            },
        }

    async def list_user_conversations(
        self, user_id: UUID, skip: int = 0, limit: int = 10
    ) -> dict:
        """List user's conversations with pagination."""
        conversations, total = (
            await self.conversation_service.get_user_conversations(
                user_id, skip, limit
            )
        )

        return {
            "total": total,
            "conversations": [
                {
                    "id": str(c.id),
                    "title": c.title,
                    "model": c.model,
                    "created_at": c.created_at,
                    "messages": len(c.messages),
                }
                for c in conversations
            ],
        }

    async def search_conversations(
        self, user_id: UUID, query: str, limit: int = 20
    ) -> list:
        """Search user's conversations."""
        results = await self.conversation_service.search_conversations(
            user_id, query, limit
        )
        return [
            {
                "id": str(r["conversation_id"]),
                "title": r["title"],
                "match_type": r["match_type"],
                "match_excerpt": r["match_excerpt"],
            }
            for r in results
        ]

    async def export_conversation(
        self, conversation_id: UUID, format: str = "json"
    ) -> str:
        """Export conversation in specified format."""
        if format == "json":
            return await self.conversation_service.export_conversation_json(
                conversation_id
            )
        elif format == "csv":
            return await self.conversation_service.export_conversation_csv(
                conversation_id
            )
        else:
            raise ValueError(f"Unsupported export format: {format}")

    async def manage_conversation_tags(
        self, user_id: UUID, conversation_id: UUID, action: str, tag_name: Optional[str] = None
    ) -> dict:
        """Manage tags for conversations."""
        if action == "create_tag":
            tag = await self.conversation_service.create_tag(user_id, tag_name)
            return {"action": "create_tag", "tag_id": str(tag.id), "name": tag.name}

        elif action == "list_tags":
            tags = await self.conversation_service.get_user_tags(user_id)
            return {
                "action": "list_tags",
                "tags": [{"id": str(t.id), "name": t.name} for t in tags],
            }

        elif action == "add_tag":
            success = await self.conversation_service.add_tag_to_conversation(
                conversation_id, tag_id
            )
            return {
                "action": "add_tag",
                "success": success,
            }

        else:
            raise ValueError(f"Unknown action: {action}")


# ============================================================================
# USAGE EXAMPLES
# ============================================================================


async def example_basic_conversation(session: AsyncSession, user_id: UUID):
    """Example: Create conversation and exchange messages."""
    manager = IntegratedChatManager(session)

    # Create conversation
    conv_id = await manager.create_conversation_session(
        user_id,
        title="Python Debugging Tips",
        description="Learning effective Python debugging techniques",
        model="grok",
    )
    print(f"Created conversation: {conv_id}")

    # Send messages
    messages = [
        "What are the best Python debugging tools?",
        "How do I use pdb effectively?",
        "What about debugging async code?",
    ]

    for user_msg in messages:
        result = await manager.send_message_and_track(conv_id, user_msg)
        print(f"\nUser: {result['user_message']}")
        print(f"Assistant: {result['assistant_response']}")
        print(f"Response time: {result['response_time']:.2f}s")
        print(f"Tokens: {result['input_tokens']} in, {result['output_tokens']} out")

    # Get summary
    summary = await manager.get_conversation_summary(conv_id)
    print(f"\n=== Conversation Summary ===")
    print(f"Title: {summary['title']}")
    print(f"Messages: {summary['message_count']}")
    print(f"Avg Response Time: {summary['analytics']['average_response_time']:.2f}s")
    print(f"Total Tokens: {summary['analytics']['total_tokens_used']}")


async def example_search_and_export(session: AsyncSession, user_id: UUID):
    """Example: Search conversations and export data."""
    manager = IntegratedChatManager(session)

    # List conversations
    convs = await manager.list_user_conversations(user_id)
    print(f"Total conversations: {convs['total']}")

    # Search
    results = await manager.search_conversations(user_id, "debugging", limit=5)
    print(f"Search results for 'debugging': {len(results)} found")

    # Export first conversation
    if convs["conversations"]:
        conv_id = UUID(convs["conversations"][0]["id"])

        # Export as JSON
        json_data = await manager.export_conversation(conv_id, format="json")
        print(f"Exported JSON (first 500 chars): {json_data[:500]}...")

        # Export as CSV
        csv_data = await manager.export_conversation(conv_id, format="csv")
        print(f"Exported CSV (first 200 chars): {csv_data[:200]}...")


async def example_tagging(session: AsyncSession, user_id: UUID, conv_id: UUID):
    """Example: Create and manage conversation tags."""
    manager = IntegratedChatManager(session)

    # Create tags
    work_tag = await manager.manage_conversation_tags(
        user_id, conv_id, "create_tag", "work"
    )
    important_tag = await manager.manage_conversation_tags(
        user_id, conv_id, "create_tag", "important"
    )

    print(f"Created tags: {work_tag['name']}, {important_tag['name']}")

    # List all user tags
    tags = await manager.manage_conversation_tags(user_id, conv_id, "list_tags")
    print(f"User tags: {tags['tags']}")


# ============================================================================
# INTEGRATION WITH FASTAPI ENDPOINTS
# ============================================================================


async def handle_stream_with_history(
    user_id: UUID, conversation_id: UUID, message: str, session: AsyncSession
):
    """Example: Stream LLM response while tracking in conversation history."""
    manager = IntegratedChatManager(session)

    # Add user message
    await manager.conversation_service.add_message(
        conversation_id, "user", message
    )

    # Get streaming response
    start = time.time()
    full_response = ""

    async for chunk in manager.chat_client.stream_message(message):
        full_response += chunk
        # Yield to frontend (e.g., WebSocket)
        yield chunk

    response_time = time.time() - start

    # Save complete response with analytics
    await manager.conversation_service.add_message(
        conversation_id,
        "assistant",
        full_response,
        response_time=response_time,
        input_tokens=0,  # Can be filled from LLM response
        output_tokens=0,
    )


# ============================================================================
# DASHBOARD STATISTICS EXAMPLE
# ============================================================================


async def get_user_statistics(user_id: UUID, session: AsyncSession) -> dict:
    """Get comprehensive user statistics."""
    manager = IntegratedChatManager(session)
    conversations, total = await manager.conversation_service.get_user_conversations(
        user_id, skip=0, limit=1000
    )

    total_messages = 0
    total_time = 0.0
    total_tokens = 0
    model_usage = {}

    for conv in conversations:
        analytics = (
            await manager.conversation_service.get_conversation_analytics(conv.id)
        )
        if analytics:
            total_messages += analytics.total_messages
            total_time += analytics.total_response_time
            total_tokens += (
                analytics.total_input_tokens + analytics.total_output_tokens
            )

        if conv.model not in model_usage:
            model_usage[conv.model] = 0
        model_usage[conv.model] += len(conv.messages)

    return {
        "total_conversations": total,
        "total_messages": total_messages,
        "total_time_seconds": total_time,
        "average_response_time": (
            total_time / total_messages if total_messages > 0 else 0
        ),
        "total_tokens": total_tokens,
        "model_breakdown": model_usage,
    }


if __name__ == "__main__":
    # Note: These examples require proper setup with database session
    # See main.py for FastAPI integration
    print("This module provides conversation management integration examples.")
    print("Use IntegratedChatManager class with your async FastAPI routes.")
