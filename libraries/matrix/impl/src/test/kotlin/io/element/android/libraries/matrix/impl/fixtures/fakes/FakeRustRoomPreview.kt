/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomPreviewInfo
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomPreview
import org.matrix.rustcomponents.sdk.RoomPreviewInfo

class FakeRustRoomPreview(
    private val info: RoomPreviewInfo = aRustRoomPreviewInfo(),
) : RoomPreview(NoPointer) {
    override fun info(): RoomPreviewInfo {
        return info
    }
}
