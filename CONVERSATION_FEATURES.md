# Conversation Management System - Implementation Guide

## Overview

This document describes the comprehensive conversation management system added to llm-chat-client. The system provides 7 major features for managing LLM chat conversations, including history tracking, analytics, search, and export functionality.

## Features Implemented

### 1. **Conversation History** ✅
Stores complete conversation history with timestamps, allowing users to:
- Create new conversations with title and description
- Retrieve full conversation history with all messages
- Access message history in chronological order
- Persist conversations across sessions

**Key Models:**
- `Conversation` - Main conversation record
- `Message` - Individual messages with role (user/assistant) and content

**API Endpoints:**
- `POST /api/conversations` - Create conversation
- `GET /api/conversations/{conversation_id}` - Get conversation with all messages
- `POST /api/conversations/{conversation_id}/messages` - Add message
- `GET /api/conversations/{conversation_id}/messages` - Get all messages

---

### 2. **Conversation Management** ✅
Full CRUD operations for conversations:
- Create conversations with model selection
- List all conversations with pagination and sorting
- Update conversation metadata (title, description)
- Delete conversations and associated data

**API Endpoints:**
- `GET /api/conversations` - List conversations (paginated, sortable)
- `POST /api/conversations` - Create conversation
- `PUT /api/conversations/{conversation_id}` - Update conversation
- `DELETE /api/conversations/{conversation_id}` - Delete conversation

**Query Parameters:**
```python
# List conversations with pagination and sorting
GET /api/conversations?skip=0&limit=20&sort_by=updated_at&order=desc

# Supported sort_by values: created_at, updated_at, title
# Supported order values: asc, desc
```

---

### 3. **Message Analytics** ✅
Comprehensive tracking of LLM performance metrics:
- Response time tracking (in seconds)
- Token usage (input/output per message)
- Aggregate statistics per conversation
- Average response time calculation
- Total token consumption tracking

**Key Models:**
- `ConversationAnalytics` - Aggregated conversation statistics
- `MessageAnalytics` - Per-message performance data

**API Endpoints:**
- `GET /api/conversations/{conversation_id}/analytics` - Get conversation analytics

**Tracked Metrics:**
```python
{
    "total_messages": 10,
    "total_response_time": 5.25,      # seconds
    "average_response_time": 0.525,   # seconds
    "total_input_tokens": 1250,
    "total_output_tokens": 890
}
```

**Adding Message with Analytics:**
```python
POST /api/conversations/{conversation_id}/messages
{
    "role": "user",
    "content": "Your message here",
    "response_time": 1.5,          # Optional, in seconds
    "input_tokens": 125,           # Optional
    "output_tokens": 89,           # Optional
    "model_name": "grok"           # Optional
}
```

---

### 4. **User Sessions** ✅
Multi-user support with conversation isolation:
- Each conversation linked to a specific user
- User authentication required for all operations
- Automatic authorization checks
- User-scoped data access

**Features:**
- Conversations are isolated per user
- Only authenticated users can create/access conversations
- All operations require JWT authentication token
- Users can only access their own conversations

**Authentication:**
All endpoints require Bearer token:
```bash
Authorization: Bearer <jwt_token>
```

---

### 5. **Search Conversations** ✅
Full-text search across conversations:
- Search by conversation title
- Search by conversation description
- Search by message content
- Return match type and context excerpt

**API Endpoints:**
- `GET /api/conversations/search?q=query&limit=20` - Search conversations

**Search Response:**
```python
{
    "total": 3,
    "results": [
        {
            "id": "uuid",
            "title": "Conversation Title",
            "description": "Description",
            "model": "grok",
            "created_at": "2026-07-08T14:00:00Z",
            "match_type": "title",              # "title", "description", or "content"
            "match_excerpt": "Matching text..."
        }
    ]
}
```

**Search Features:**
- Case-insensitive matching
- Searches across all three fields (title, description, content)
- Returns match type and excerpt
- Limit results per query

---

### 6. **Export Functionality** ✅
Multiple export formats for data portability:
- JSON export with full conversation details
- CSV export of messages for spreadsheet analysis

**API Endpoints:**
- `GET /api/conversations/{conversation_id}/export/json` - Export as JSON

**JSON Export Format:**
```json
{
    "id": "uuid",
    "title": "Conversation Title",
    "description": "Description",
    "model": "grok",
    "created_at": "2026-07-08T14:00:00Z",
    "updated_at": "2026-07-08T14:30:00Z",
    "messages": [
        {
            "id": "uuid",
            "role": "user",
            "content": "Message content",
            "created_at": "2026-07-08T14:00:00Z"
        }
    ],
    "analytics": {
        "total_messages": 5,
        "total_response_time": 2.5,
        "average_response_time": 0.5,
        "total_input_tokens": 500,
        "total_output_tokens": 350
    },
    "tags": [
        {
            "id": "uuid",
            "name": "important"
        }
    ]
}
```

**CSV Export Format:**
```
Message ID,Role,Content,Created At
uuid,user,User message,2026-07-08T14:00:00Z
uuid,assistant,Assistant response,2026-07-08T14:00:10Z
```

---

### 7. **Conversation Tagging** ✅
Organize conversations with custom tags:
- Create custom tags per user
- Add/remove tags from conversations
- Filter conversations by tags
- Manage tag lifecycle

**API Endpoints:**
- `POST /api/conversations/tags` - Create tag
- `GET /api/conversations/tags` - Get user's tags
- `DELETE /api/conversations/tags/{tag_id}` - Delete tag
- `POST /api/conversations/{conversation_id}/tags/{tag_id}` - Add tag to conversation
- `DELETE /api/conversations/{conversation_id}/tags/{tag_id}` - Remove tag
- `GET /api/conversations/{conversation_id}/tags` - Get conversation's tags

**Tag Management:**
```python
# Create a tag
POST /api/conversations/tags
{
    "name": "important"
}

# Add tag to conversation
POST /api/conversations/{conversation_id}/tags/{tag_id}

# Remove tag from conversation
DELETE /api/conversations/{conversation_id}/tags/{tag_id}

# Get all user tags
GET /api/conversations/tags
```

---

## Database Schema

### Tables Created

1. **conversations**
   - `id` (UUID, primary key)
   - `user_id` (UUID, foreign key to users)
   - `title` (String, required)
   - `description` (Text, optional)
   - `model` (String, required) - "grok", "gemini", etc.
   - `created_at` (DateTime)
   - `updated_at` (DateTime)

2. **messages**
   - `id` (UUID, primary key)
   - `conversation_id` (UUID, foreign key)
   - `role` (String) - "user" or "assistant"
   - `content` (Text)
   - `created_at` (DateTime)

3. **conversation_analytics**
   - `id` (UUID, primary key)
   - `conversation_id` (UUID, foreign key)
   - `total_messages` (Integer)
   - `total_response_time` (Float)
   - `average_response_time` (Float)
   - `total_input_tokens` (Integer)
   - `total_output_tokens` (Integer)
   - `updated_at` (DateTime)

4. **message_analytics**
   - `id` (UUID, primary key)
   - `message_id` (UUID, foreign key)
   - `conversation_analytics_id` (UUID, foreign key)
   - `response_time` (Float, optional)
   - `input_tokens` (Integer)
   - `output_tokens` (Integer)
   - `model_name` (String, optional)
   - `created_at` (DateTime)

5. **conversation_tags**
   - `id` (UUID, primary key)
   - `user_id` (UUID, foreign key)
   - `name` (String)
   - `created_at` (DateTime)

6. **conversation_tags_association** (Junction table)
   - `conversation_id` (UUID, foreign key)
   - `tag_id` (UUID, foreign key)

### Indexes

- `idx_user_id` on conversations.user_id
- `idx_created_at` on conversations.created_at
- `idx_updated_at` on conversations.updated_at
- `idx_conversation_id` on messages.conversation_id
- `idx_created_at` on messages.created_at
- `idx_conversation_id_analytics` on conversation_analytics.conversation_id
- `idx_message_id` on message_analytics.message_id

---

## Running Database Migrations

Apply the migration to create all new tables:

```bash
# Using alembic
alembic upgrade head

# Or with Docker Compose
make dev
```

---

## File Structure

```
app/src/domain/conversation/
├── __init__.py
├── models.py              # SQLAlchemy models
├── schemas.py             # Pydantic request/response schemas
├── repositories.py        # Database access layer
├── services.py            # Business logic layer
└── v1/
    ├── __init__.py
    └── router.py          # FastAPI route handlers
```

---

## Usage Examples

### Example 1: Create Conversation and Add Messages

```python
# Create conversation
response = requests.post(
    "http://localhost:8001/api/conversations",
    headers={"Authorization": f"Bearer {token}"},
    json={
        "title": "Python Debugging Session",
        "description": "Discussing Python debugging techniques",
        "model": "grok"
    }
)
conversation_id = response.json()["id"]

# Add user message
requests.post(
    f"http://localhost:8001/api/conversations/{conversation_id}/messages",
    headers={"Authorization": f"Bearer {token}"},
    json={
        "role": "user",
        "content": "How do I debug Python code effectively?"
    }
)

# Add assistant message with analytics
requests.post(
    f"http://localhost:8001/api/conversations/{conversation_id}/messages",
    headers={"Authorization": f"Bearer {token}"},
    json={
        "role": "assistant",
        "content": "Here are some effective debugging techniques...",
        "response_time": 1.25,
        "input_tokens": 50,
        "output_tokens": 200,
        "model_name": "grok"
    }
)
```

### Example 2: Search and Export

```python
# Search conversations
search_response = requests.get(
    "http://localhost:8001/api/conversations/search",
    headers={"Authorization": f"Bearer {token}"},
    params={"q": "debugging", "limit": 20}
)

# Export conversation
export_response = requests.get(
    f"http://localhost:8001/api/conversations/{conversation_id}/export/json",
    headers={"Authorization": f"Bearer {token}"}
)
export_data = export_response.json()
```

### Example 3: Manage Tags

```python
# Create a tag
tag_response = requests.post(
    "http://localhost:8001/api/conversations/tags",
    headers={"Authorization": f"Bearer {token}"},
    json={"name": "work"}
)
tag_id = tag_response.json()["id"]

# Add tag to conversation
requests.post(
    f"http://localhost:8001/api/conversations/{conversation_id}/tags/{tag_id}",
    headers={"Authorization": f"Bearer {token}"}
)

# Get conversation's tags
tags = requests.get(
    f"http://localhost:8001/api/conversations/{conversation_id}/tags",
    headers={"Authorization": f"Bearer {token}"}
).json()
```

---

## Architecture

### Layered Architecture

```
Router (v1/router.py)
    ↓
Service (services.py)
    ↓
Repository (repositories.py)
    ↓
Model (models.py)
    ↓
Database
```

**Router Layer:** Handles HTTP requests/responses, validation, authentication
**Service Layer:** Business logic, data transformation
**Repository Layer:** Database queries, transactions
**Model Layer:** ORM mappings, relationships

### Key Design Decisions

1. **Async/Await:** All database operations are async-native
2. **Dependency Injection:** Services injected via FastAPI dependencies
3. **User Isolation:** Automatic authorization via user_id checking
4. **Cascade Deletes:** Conversations deletion cascades to messages
5. **Index Strategy:** Indexes on frequently queried columns (user_id, created_at, conversation_id)
6. **Analytics Model:** Separate tables for performance tracking without cluttering main message table

---

## Performance Considerations

1. **Pagination:** Use skip/limit for large result sets
2. **Indexes:** All frequently searched columns are indexed
3. **Lazy Loading:** Messages/tags loaded on demand via relationships
4. **Search:** Uses LIKE with limit to prevent large result sets
5. **Analytics:** Aggregate stored to avoid expensive calculations

---

## Security

1. **Authentication:** All endpoints require JWT token
2. **Authorization:** User can only access their own conversations
3. **SQL Injection:** Protected via SQLAlchemy parameterized queries
4. **Data Isolation:** Each user's data is isolated at the database level

---

## Future Enhancements

1. **Bulk Operations:** Batch create/update conversations
2. **Advanced Analytics:** ML-based performance analysis
3. **Sharing:** Allow conversations to be shared with other users
4. **Webhooks:** Real-time notifications on conversation updates
5. **Rate Limiting:** Limit API requests per user
6. **Soft Deletes:** Archive conversations instead of hard delete
7. **Full-Text Search:** PostgreSQL FTS for better search performance
8. **Backup/Restore:** Conversation backup functionality

---

## Integration with Chat Client

To integrate with the existing chat client:

```python
# In your chat handler
from app.src.domain.conversation.services import ConversationService

async def handle_chat_message(user_id, conversation_id, user_message):
    service = ConversationService(session)
    
    # Add user message
    await service.add_message(conversation_id, "user", user_message)
    
    # Get LLM response (existing code)
    response = await get_llm_response(user_message)
    
    # Add assistant message with analytics
    import time
    start = time.time()
    assistant_message = response.get("content")
    response_time = time.time() - start
    
    await service.add_message(
        conversation_id,
        "assistant",
        assistant_message,
        response_time=response_time,
        input_tokens=response.get("input_tokens", 0),
        output_tokens=response.get("output_tokens", 0),
        model_name=response.get("model")
    )
```

---

## Testing

Run tests for conversation endpoints:

```bash
# Run all tests
pytest

# Run conversation tests only
pytest tests/conversation/

# Run with coverage
pytest --cov=app.src.domain.conversation
```

---

## Troubleshooting

### Migration Issues
If migration fails, ensure PostgreSQL is running and DATABASE_URL is correct.

### Auth Errors
Verify JWT token is valid and included in Authorization header.

### Search Not Working
Ensure database indexes are created (run migration).

### Performance Issues
Check database connection pool size and consider adding more indexes.

---

## Support

For issues or questions, refer to:
1. API documentation at `/docs`
2. Database schema in `models.py`
3. Example usage in this document
