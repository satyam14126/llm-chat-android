# Conversation Features - Quick Reference

## 🚀 Quick Start

```bash
# 1. Apply migrations
alembic upgrade head

# 2. Test API
curl -X GET http://localhost:8001/api/conversations \
  -H "Authorization: Bearer YOUR_TOKEN"

# 3. View API docs
# Open http://localhost:8001/docs
```

## 📋 API Endpoints Summary

### Conversations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/conversations` | Create conversation |
| GET | `/api/conversations` | List conversations |
| GET | `/api/conversations/{id}` | Get conversation detail |
| PUT | `/api/conversations/{id}` | Update conversation |
| DELETE | `/api/conversations/{id}` | Delete conversation |

### Messages
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/conversations/{id}/messages` | Add message |
| GET | `/api/conversations/{id}/messages` | Get messages |

### Analytics & Search
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/conversations/{id}/analytics` | Get analytics |
| GET | `/api/conversations/search?q=query` | Full-text search |
| GET | `/api/conversations/{id}/export/json` | Export as JSON |

### Tags
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/conversations/tags` | Create tag |
| GET | `/api/conversations/tags` | List tags |
| DELETE | `/api/conversations/tags/{id}` | Delete tag |
| POST | `/api/conversations/{id}/tags/{tag_id}` | Add tag |
| DELETE | `/api/conversations/{id}/tags/{tag_id}` | Remove tag |

## 📊 Core Objects

### Conversation
```json
{
  "id": "uuid",
  "user_id": "uuid",
  "title": "string",
  "description": "string",
  "model": "grok|gemini",
  "created_at": "datetime",
  "updated_at": "datetime",
  "tags": []
}
```

### Message
```json
{
  "id": "uuid",
  "conversation_id": "uuid",
  "role": "user|assistant",
  "content": "string",
  "created_at": "datetime"
}
```

### Analytics
```json
{
  "total_messages": 10,
  "total_response_time": 5.25,
  "average_response_time": 0.525,
  "total_input_tokens": 1250,
  "total_output_tokens": 890
}
```

## 🔧 Common Operations

### Create & Chat
```python
# Create conversation
conv = await service.create_conversation(user_id, "My Chat", None, "grok")

# Add messages with analytics
await service.add_message(
    conv.id, "user", "Hello!",
    response_time=1.5, input_tokens=10, output_tokens=50, model_name="grok"
)
```

### Search & Export
```python
# Search
results = await service.search_conversations(user_id, "python")

# Export
json_data = await service.export_conversation_json(conv_id)
csv_data = await service.export_conversation_csv(conv_id)
```

### Manage Tags
```python
# Create tag
tag = await service.create_tag(user_id, "work")

# Add to conversation
await service.add_tag_to_conversation(conv_id, tag.id)

# Get tags
tags = await service.get_user_tags(user_id)
```

## 📁 File Structure

```
app/src/domain/conversation/
├── models.py          → Database models (6 models)
├── schemas.py         → Request/response schemas (15 schemas)
├── repositories.py    → Database layer (6 repositories)
├── services.py        → Business logic (20+ methods)
└── v1/router.py       → API endpoints (20+ endpoints)
```

## 🗄️ Database Tables

| Table | Purpose | Indexes |
|-------|---------|---------|
| conversations | Main records | user_id, created_at, updated_at |
| messages | Chat messages | conversation_id, created_at |
| conversation_analytics | Aggregate stats | conversation_id |
| message_analytics | Per-message metrics | message_id |
| conversation_tags | Tag definitions | - |
| conversation_tags_association | Tags <→ Conversations | - |

## 🔐 Authentication

All endpoints require JWT token:
```
Authorization: Bearer <jwt_token>
```

## 📈 Pagination & Sorting

### Pagination
```
GET /api/conversations?skip=0&limit=20
```

### Sorting
```
GET /api/conversations?sort_by=created_at&order=desc
# sort_by: created_at, updated_at, title
# order: asc, desc
```

## 📝 Error Handling

| Status | Meaning |
|--------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not found |
| 500 | Server error |

## 💡 Performance Tips

1. Use pagination for large lists
2. Limit search results (default: 20)
3. Indexes prevent slow queries
4. Cascade deletes clean up data
5. Analytics computed on insert

## 📚 Documentation

- **CONVERSATION_FEATURES.md** - Complete feature guide
- **CONVERSATION_SETUP.md** - Setup & troubleshooting
- **CONVERSATION_INTEGRATION_EXAMPLES.py** - Code examples
- **IMPLEMENTATION_SUMMARY.md** - Technical overview

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 error | Check JWT token validity |
| 404 error | Verify resource exists and is owned by user |
| Migration fails | Ensure PostgreSQL is running |
| Slow search | Add indexes or reduce limit |
| No auth | Include Authorization header |

## 🎯 Feature Checklist

- ✅ Conversation History
- ✅ Conversation Management (CRUD)
- ✅ Message Analytics
- ✅ User Sessions (isolation)
- ✅ Search Conversations
- ✅ Export (JSON/CSV)
- ✅ Conversation Tagging

## 🚀 Next Steps

1. Run migrations: `alembic upgrade head`
2. Test endpoints: `/docs` UI
3. Integrate with chat client
4. Add custom tags
5. Monitor analytics
6. Export conversations

---

**Version**: 1.0
**Status**: Production Ready
**Dependencies**: None (uses existing packages)
