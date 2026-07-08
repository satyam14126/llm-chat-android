"""Add conversation domain models for chat history and analytics.

Revision ID: 001_add_conversation_models
Revises: e2fe0b442080
Create Date: 2026-07-08 14:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '001_add_conversation_models'
down_revision: Union[str, None] = 'e2fe0b442080'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # Create conversations table
    op.create_table(
        'conversations',
        sa.Column('id', sa.UUID(as_uuid=True), primary_key=True),
        sa.Column('user_id', sa.UUID(as_uuid=True), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('title', sa.String(255), nullable=False),
        sa.Column('description', sa.Text, nullable=True),
        sa.Column('model', sa.String(50), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), onupdate=sa.func.now()),
    )
    op.create_index('idx_user_id', 'conversations', ['user_id'])
    op.create_index('idx_created_at', 'conversations', ['created_at'])
    op.create_index('idx_updated_at', 'conversations', ['updated_at'])

    # Create messages table
    op.create_table(
        'messages',
        sa.Column('id', sa.UUID(as_uuid=True), primary_key=True),
        sa.Column('conversation_id', sa.UUID(as_uuid=True), sa.ForeignKey('conversations.id', ondelete='CASCADE'), nullable=False),
        sa.Column('role', sa.String(20), nullable=False),
        sa.Column('content', sa.Text, nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index('idx_conversation_id', 'messages', ['conversation_id'])
    op.create_index('idx_created_at', 'messages', ['created_at'])

    # Create conversation_analytics table
    op.create_table(
        'conversation_analytics',
        sa.Column('id', sa.UUID(as_uuid=True), primary_key=True),
        sa.Column('conversation_id', sa.UUID(as_uuid=True), sa.ForeignKey('conversations.id', ondelete='CASCADE'), nullable=False),
        sa.Column('total_messages', sa.Integer, default=0),
        sa.Column('total_response_time', sa.Float, default=0.0),
        sa.Column('average_response_time', sa.Float, default=0.0),
        sa.Column('total_input_tokens', sa.Integer, default=0),
        sa.Column('total_output_tokens', sa.Integer, default=0),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), onupdate=sa.func.now()),
    )
    op.create_index('idx_conversation_id_analytics', 'conversation_analytics', ['conversation_id'])

    # Create message_analytics table
    op.create_table(
        'message_analytics',
        sa.Column('id', sa.UUID(as_uuid=True), primary_key=True),
        sa.Column('message_id', sa.UUID(as_uuid=True), sa.ForeignKey('messages.id', ondelete='CASCADE'), nullable=False),
        sa.Column('conversation_analytics_id', sa.UUID(as_uuid=True), sa.ForeignKey('conversation_analytics.id', ondelete='CASCADE'), nullable=True),
        sa.Column('response_time', sa.Float, nullable=True),
        sa.Column('input_tokens', sa.Integer, default=0),
        sa.Column('output_tokens', sa.Integer, default=0),
        sa.Column('model_name', sa.String(50), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index('idx_message_id', 'message_analytics', ['message_id'])

    # Create conversation_tags table
    op.create_table(
        'conversation_tags',
        sa.Column('id', sa.UUID(as_uuid=True), primary_key=True),
        sa.Column('user_id', sa.UUID(as_uuid=True), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('name', sa.String(100), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    # Create conversation_tags_association table
    op.create_table(
        'conversation_tags_association',
        sa.Column('conversation_id', sa.UUID(as_uuid=True), sa.ForeignKey('conversations.id', ondelete='CASCADE'), primary_key=True),
        sa.Column('tag_id', sa.UUID(as_uuid=True), sa.ForeignKey('conversation_tags.id', ondelete='CASCADE'), primary_key=True),
    )


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_table('conversation_tags_association')
    op.drop_table('conversation_tags')
    op.drop_table('message_analytics')
    op.drop_table('conversation_analytics')
    op.drop_table('messages')
    op.drop_table('conversations')
