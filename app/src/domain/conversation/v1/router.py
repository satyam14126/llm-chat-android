from typing import Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.src.core.dependencies.auth import get_current_user
from app.src.core.dependencies.db_session import get_db_session
from app.src.domain.user.models import User

from ..schemas import (
    ConversationCreate,
    ConversationDetailResponse,
    ConversationExportData,
    ConversationListResponse,
    ConversationResponse,
    ConversationSearchResponse,
    ConversationSearchResult,
    ConversationTagCreate,
    ConversationTagResponse,
    ConversationUpdate,
    MessageCreate,
    MessageResponse,
    ConversationAnalyticsResponse,
)
from ..services import ConversationService

router = APIRouter(
    prefix="/conversations",
    tags=["conversations"],
    dependencies=[Depends(get_current_user)],
)


@router.post("", response_model=ConversationResponse, status_code=status.HTTP_201_CREATED)
async def create_conversation(
    conversation_data: ConversationCreate,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Create a new conversation."""
    service = ConversationService(session)
    conversation = await service.create_conversation(
        current_user.id,
        conversation_data.title,
        conversation_data.description,
        conversation_data.model,
    )
    await session.commit()
    return conversation


@router.get("/{conversation_id}", response_model=ConversationDetailResponse)
async def get_conversation(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Get a specific conversation with all messages and analytics."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    analytics = await service.get_conversation_analytics(conversation_id)

    return {
        **{
            "id": conversation.id,
            "user_id": conversation.user_id,
            "title": conversation.title,
            "description": conversation.description,
            "model": conversation.model,
            "created_at": conversation.created_at,
            "updated_at": conversation.updated_at,
            "message_count": len(conversation.messages),
            "tags": conversation.tags,
            "messages": conversation.messages,
            "analytics": analytics,
        }
    }


@router.get("", response_model=ConversationListResponse)
async def list_conversations(
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    sort_by: str = Query("updated_at", regex="^(created_at|updated_at|title)$"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """List all conversations for the current user."""
    service = ConversationService(session)
    conversations, total = await service.get_user_conversations(
        current_user.id, skip, limit, sort_by, order
    )

    return {
        "total": total,
        "page": skip // limit + 1,
        "page_size": limit,
        "conversations": conversations,
    }


@router.put("/{conversation_id}", response_model=ConversationResponse)
async def update_conversation(
    conversation_id: UUID,
    update_data: ConversationUpdate,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Update a conversation's title or description."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    updated = await service.update_conversation(
        conversation_id, update_data.title, update_data.description
    )
    await session.commit()
    return updated


@router.delete("/{conversation_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_conversation(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Delete a conversation and all its messages."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    await service.delete_conversation(conversation_id)
    await session.commit()


@router.post(
    "/{conversation_id}/messages",
    response_model=MessageResponse,
    status_code=status.HTTP_201_CREATED,
)
async def add_message(
    conversation_id: UUID,
    message_data: MessageCreate,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Add a message to a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    message = await service.add_message(
        conversation_id, message_data.role, message_data.content
    )
    return message


@router.get("/{conversation_id}/messages", response_model=list[MessageResponse])
async def get_conversation_messages(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Get all messages in a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    messages = await service.get_conversation_messages(conversation_id)
    return messages


@router.get("/{conversation_id}/analytics", response_model=ConversationAnalyticsResponse)
async def get_conversation_analytics(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Get analytics for a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    analytics = await service.get_conversation_analytics(conversation_id)
    if not analytics:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Analytics not found"
        )
    return analytics


@router.get("/search", response_model=ConversationSearchResponse)
async def search_conversations(
    q: str = Query(..., min_length=1, max_length=100),
    limit: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Search conversations by title, description, or message content."""
    service = ConversationService(session)
    results = await service.search_conversations(current_user.id, q, limit)

    search_results = [
        ConversationSearchResult(
            id=result["conversation_id"],
            title=result["title"],
            description=result["description"],
            model=result["model"],
            created_at=result["created_at"],
            match_type=result["match_type"],
            match_excerpt=result["match_excerpt"],
        )
        for result in results
    ]

    return {
        "total": len(search_results),
        "results": search_results,
    }


@router.get("/{conversation_id}/export/json", response_model=ConversationExportData)
async def export_conversation_json(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Export conversation as JSON."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    try:
        json_data = await service.export_conversation_json(conversation_id)
        import json as json_module

        data = json_module.loads(json_data)
        return data
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e)
        )


@router.get("/{conversation_id}/tags", response_model=list[ConversationTagResponse])
async def get_conversation_tags(
    conversation_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Get all tags for a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    return conversation.tags


@router.post("/{conversation_id}/tags/{tag_id}", status_code=status.HTTP_200_OK)
async def add_tag_to_conversation(
    conversation_id: UUID,
    tag_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Add a tag to a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    success = await service.add_tag_to_conversation(conversation_id, tag_id)
    await session.commit()

    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Tag not found"
        )

    return {"message": "Tag added successfully"}


@router.delete(
    "/{conversation_id}/tags/{tag_id}", status_code=status.HTTP_204_NO_CONTENT
)
async def remove_tag_from_conversation(
    conversation_id: UUID,
    tag_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Remove a tag from a conversation."""
    service = ConversationService(session)
    conversation = await service.get_conversation(conversation_id)

    if not conversation or conversation.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found"
        )

    await service.remove_tag_from_conversation(conversation_id, tag_id)
    await session.commit()


@router.post("/tags", response_model=ConversationTagResponse, status_code=status.HTTP_201_CREATED)
async def create_tag(
    tag_data: ConversationTagCreate,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Create a new tag for organizing conversations."""
    service = ConversationService(session)
    tag = await service.create_tag(current_user.id, tag_data.name)
    await session.commit()
    return tag


@router.get("/tags", response_model=list[ConversationTagResponse])
async def get_user_tags(
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Get all tags for the current user."""
    service = ConversationService(session)
    tags = await service.get_user_tags(current_user.id)
    return tags


@router.delete("/tags/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_tag(
    tag_id: UUID,
    current_user: User = Depends(get_current_user),
    session: AsyncSession = Depends(get_db_session),
):
    """Delete a tag."""
    service = ConversationService(session)
    await service.delete_tag(tag_id)
    await session.commit()
