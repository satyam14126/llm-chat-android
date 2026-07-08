# Conversation Features - Quick Setup Guide

## Installation & Setup

### 1. Run Database Migrations

The conversation models require new database tables. Apply the migration:

```bash
# Using Docker Compose (recommended)
make dev

# Or directly with alembic
alembic upgrade head
```

This creates:
- `conversations` table
- `messages` table
- `conversation_analytics` table
- `message_analytics` table
- `conversation_tags` table
- `conversation_tags_association` table

### 2. Verify Installation

Once the server is running, check the API documentation:

```
http://localhost:8001/docs
```

You should see new endpoints under the "conversations" tag:
- `/api/conversations` - List/Create
- `/api/conversations/{id}` - Get/Update/Delete
- `/api/conversations/{id}/messages` - Manage messages
- `/api/conversations/{id}/analytics` - Get analytics
- `/api/conversations/{id}/tags` - Manage tags
- `/api/conversations/search` - Search conversations

### 3. Test with cURL

```bash
# Get JWT token first
TOKEN=$(curl -X POST http://localhost:8001/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.access_token')

# Create conversation
curl -X POST http://localhost:8001/api/conversations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Conversation",
    "description": "Testing the API",
    "model": "grok"
  }'

# List conversations
curl -X GET http://localhost:8001/api/conversations \
  -H "Authorization: Bearer $TOKEN"

# Search
curl -X GET "http://localhost:8001/api/conversations/search?q=testing" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Python Integration

```python
import aiohttp
import asyncio
from uuid import UUID

class ConversationClient:
    def __init__(self, base_url: str, token: str):
        self.base_url = base_url
        self.headers = {"Authorization": f"Bearer {token}"}

    async def create_conversation(self, title: str, model: str = "grok"):
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{self.base_url}/api/conversations",
                headers=self.headers,
                json={"title": title, "model": model}
            ) as resp:
                return await resp.json()

    async def add_message(self, conversation_id: UUID, role: str, content: str):
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{self.base_url}/api/conversations/{conversation_id}/messages",
                headers=self.headers,
                json={"role": role, "content": content}
            ) as resp:
                return await resp.json()

    async def get_analytics(self, conversation_id: UUID):
        async with aiohttp.ClientSession() as session:
            async with session.get(
                f"{self.base_url}/api/conversations/{conversation_id}/analytics",
                headers=self.headers
            ) as resp:
                return await resp.json()


# Usage
async def main():
    client = ConversationClient("http://localhost:8001", "your_jwt_token")
    
    # Create
    conv = await client.create_conversation("My Conversation")
    conv_id = conv["id"]
    
    # Add messages
    await client.add_message(conv_id, "user", "Hello!")
    await client.add_message(conv_id, "assistant", "Hi there!")
    
    # Get analytics
    analytics = await client.get_analytics(conv_id)
    print(analytics)

asyncio.run(main())
```

## Key Features Overview

### 1. Conversation Management
- Create conversations with title and model selection
- List with pagination and sorting
- Update titles/descriptions
- Delete with automatic cascade

### 2. Message History
- Store all messages (user and assistant)
- Chronological ordering
- Full-text searchable content

### 3. Performance Analytics
- Per-message response time tracking
- Token usage tracking (input/output)
- Conversation-level aggregate statistics
- Average response time calculation

### 4. Search
- Full-text search across titles, descriptions, and content
- Returns match context and type

### 5. Export
- JSON export with full conversation details
- CSV export for spreadsheet analysis
- Includes analytics in export

### 6. Tags
- Create custom tags
- Organize conversations
- Add/remove tags dynamically

### 7. Multi-user Support
- Automatic user isolation
- JWT token authentication
- Per-user tag management

## Common Tasks

### Save Conversation During Chat
```python
async def chat_with_tracking(user_id, conv_id, user_msg, session):
    service = ConversationService(session)
    
    # Add user message
    await service.add_message(conv_id, "user", user_msg)
    
    # Get LLM response
    import time
    start = time.time()
    llm_response = await call_llm(user_msg)
    response_time = time.time() - start
    
    # Save with analytics
    await service.add_message(
        conv_id,
        "assistant",
        llm_response,
        response_time=response_time,
        input_tokens=llm_response.get("input_tokens", 0),
        output_tokens=llm_response.get("output_tokens", 0),
        model_name="grok"
    )
```

### Search and Export
```python
async def backup_conversations(user_id, session):
    service = ConversationService(session)
    
    # Find all conversations
    conversations, _ = await service.get_user_conversations(user_id, limit=1000)
    
    # Export all as JSON
    for conv in conversations:
        json_data = await service.export_conversation_json(conv.id)
        # Save to file
        with open(f"backup_{conv.id}.json", "w") as f:
            f.write(json_data)
```

### Tag Organization
```python
async def organize_conversations(user_id, session):
    service = ConversationService(session)
    
    # Create tags
    work_tag = await service.create_tag(user_id, "work")
    personal_tag = await service.create_tag(user_id, "personal")
    
    # Get user conversations
    conversations, _ = await service.get_user_conversations(user_id)
    
    # Tag them based on title
    for conv in conversations:
        if "work" in conv.title.lower():
            await service.add_tag_to_conversation(conv.id, work_tag.id)
        elif "personal" in conv.title.lower():
            await service.add_tag_to_conversation(conv.id, personal_tag.id)
```

## Troubleshooting

### Migrations Not Applied
```bash
# Check current revision
alembic current

# View migration history
alembic history

# Reset to specific revision if needed
alembic downgrade -1  # Go back one step
alembic upgrade head  # Reapply
```

### Authentication Issues
- Ensure you have a valid JWT token
- Include token in Authorization header: `Bearer <token>`
- Check token expiry

### Search Not Working
- Ensure migration has been applied
- Verify message content is being saved
- Try searching by title first

### Performance Issues
- Use pagination for large result sets
- Limit search results
- Consider indexing high-cardinality columns

## API Response Examples

### Create Conversation
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Python Debugging",
    "description": "Tips for debugging Python",
    "model": "grok",
    "created_at": "2026-07-08T14:00:00Z",
    "updated_at": "2026-07-08T14:00:00Z",
    "message_count": 0,
    "tags": []
}
```

### Get Analytics
```json
{
    "total_messages": 10,
    "total_response_time": 5.25,
    "average_response_time": 0.525,
    "total_input_tokens": 1250,
    "total_output_tokens": 890
}
```

### Search Results
```json
{
    "total": 2,
    "results": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "title": "Python Debugging Tips",
            "description": "Learning debugging",
            "model": "grok",
            "created_at": "2026-07-08T14:00:00Z",
            "match_type": "title",
            "match_excerpt": "Python Debugging Tips"
        }
    ]
}
```

## Next Steps

1. Review `CONVERSATION_FEATURES.md` for comprehensive documentation
2. Check `CONVERSATION_INTEGRATION_EXAMPLES.py` for code examples
3. Explore `/docs` endpoint for interactive API testing
4. Implement in your application using the integration examples

## Support

For issues:
1. Check database is running: `docker ps`
2. Verify migrations: `alembic current`
3. Check logs: `docker logs <container_name>`
4. Review FastAPI docs: `http://localhost:8001/docs`
