/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineItemRow
import io.element.android.features.messages.impl.timeline.aGroupedEvents
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.features.messages.impl.timeline.session.aSessionState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TimelineItemGroupedEventsRow(
    timelineItem: TimelineItem.GroupedEvents,
    showReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    highlightedItem: String?,
    sessionState: SessionState,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = rememberSaveable(key = timelineItem.identifier()) { mutableStateOf(false) }

    fun onExpandGroupClick() {
        isExpanded.value = !isExpanded.value
    }

    TimelineItemGroupedEventsRowContent(
        isExpanded = isExpanded.value,
        onExpandGroupClick = ::onExpandGroupClick,
        timelineItem = timelineItem,
        highlightedItem = highlightedItem,
        showReadReceipts = showReadReceipts,
        isLastOutgoingMessage = isLastOutgoingMessage,
        sessionState = sessionState,
        onClick = onClick,
        onLongClick = onLongClick,
        inReplyToClick = inReplyToClick,
        onUserDataClick = onUserDataClick,
        onTimestampClicked = onTimestampClicked,
        onReactionClick = onReactionClick,
        onReactionLongClick = onReactionLongClick,
        onMoreReactionsClick = onMoreReactionsClick,
        onReadReceiptClick = onReadReceiptClick,
        eventSink = eventSink,
        modifier = modifier,
    )
}

@Composable
private fun TimelineItemGroupedEventsRowContent(
    isExpanded: Boolean,
    onExpandGroupClick: () -> Unit,
    timelineItem: TimelineItem.GroupedEvents,
    highlightedItem: String?,
    showReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    sessionState: SessionState,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateContentSize()) {
        GroupHeaderView(
            text = pluralStringResource(
                id = R.plurals.room_timeline_state_changes,
                count = timelineItem.events.size,
                timelineItem.events.size
            ),
            isExpanded = isExpanded,
            isHighlighted = !isExpanded && timelineItem.events.any { it.identifier() == highlightedItem },
            onClick = onExpandGroupClick,
        )
        if (isExpanded) {
            Column {
                timelineItem.events.forEach { subGroupEvent ->
                    TimelineItemRow(
                        timelineItem = subGroupEvent,
                        showReadReceipts = showReadReceipts,
                        isLastOutgoingMessage = isLastOutgoingMessage,
                        highlightedItem = highlightedItem,
                        sessionState = sessionState,
                        userHasPermissionToSendMessage = false,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        inReplyToClick = inReplyToClick,
                        onUserDataClick = onUserDataClick,
                        onTimestampClicked = onTimestampClicked,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        onMoreReactionsClick = onMoreReactionsClick,
                        onReadReceiptClick = onReadReceiptClick,
                        eventSink = eventSink,
                        onSwipeToReply = {},
                    )
                }
            }
        } else if (showReadReceipts) {
            TimelineItemReadReceiptView(
                state = ReadReceiptViewState(
                    sendState = null,
                    isLastOutgoingMessage = false,
                    receipts = timelineItem.events.flatMap { it.readReceiptState.receipts }.toImmutableList(),
                ),
                showReadReceipts = true,
                onReadReceiptsClicked = { /* No op for group event */ })
        }
    }
}

@PreviewsDayNight
@Composable
fun TimelineItemGroupedEventsRowContentExpandedPreview() = ElementPreview {
    TimelineItemGroupedEventsRowContent(
        isExpanded = true,
        onExpandGroupClick = {},
        timelineItem = aGroupedEvents(),
        highlightedItem = null,
        showReadReceipts = true,
        isLastOutgoingMessage = false,
        sessionState = aSessionState(),
        onClick = {},
        onLongClick = {},
        inReplyToClick = {},
        onUserDataClick = {},
        onTimestampClicked = {},
        onReactionClick = { _, _ -> },
        onReactionLongClick = { _, _ -> },
        onMoreReactionsClick = {},
        onReadReceiptClick = {},
        eventSink = {},
    )
}

@PreviewsDayNight
@Composable
fun TimelineItemGroupedEventsRowContentCollapsePreview() = ElementPreview {
    TimelineItemGroupedEventsRowContent(
        isExpanded = false,
        onExpandGroupClick = {},
        timelineItem = aGroupedEvents(),
        highlightedItem = null,
        showReadReceipts = true,
        isLastOutgoingMessage = false,
        sessionState = aSessionState(),
        onClick = {},
        onLongClick = {},
        inReplyToClick = {},
        onUserDataClick = {},
        onTimestampClicked = {},
        onReactionClick = { _, _ -> },
        onReactionLongClick = { _, _ -> },
        onMoreReactionsClick = {},
        onReadReceiptClick = {},
        eventSink = {},
    )
}
