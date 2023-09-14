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

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.logout.api.LogoutPreferencePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.getCurrentUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesRootPresenter @Inject constructor(
    private val logoutPresenter: LogoutPreferencePresenter,
    private val matrixClient: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    private val analyticsService: AnalyticsService,
    private val buildType: BuildType,
    private val versionFormatter: VersionFormatter,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val featureFlagService: FeatureFlagService,
) : Presenter<PreferencesRootState> {

    @Composable
    override fun present(): PreferencesRootState {
        val matrixUser: MutableState<MatrixUser?> = rememberSaveable {
            mutableStateOf(null)
        }
        LaunchedEffect(Unit) {
            initialLoad(matrixUser)
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        val hasAnalyticsProviders = remember { analyticsService.getAvailableAnalyticsProviders().isNotEmpty() }

        val showNotificationSettings = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            showNotificationSettings.value = featureFlagService.isFeatureEnabled(FeatureFlags.NotificationSettings)
        }

        // We should display the 'complete verification' option if the current session can be verified
        val showCompleteVerification by sessionVerificationService.canVerifySessionFlow.collectAsState(false)

        val accountManagementUrl: MutableState<String?> = remember {
            mutableStateOf(null)
        }

        LaunchedEffect(Unit) {
            initAccountManagementUrl(accountManagementUrl)
        }

        val logoutState = logoutPresenter.present()
        val showDeveloperSettings = buildType != BuildType.RELEASE
        return PreferencesRootState(
            logoutState = logoutState,
            myUser = matrixUser.value,
            version = versionFormatter.get(),
            showCompleteVerification = showCompleteVerification,
            accountManagementUrl = accountManagementUrl.value,
            showAnalyticsSettings = hasAnalyticsProviders,
            showDeveloperSettings = showDeveloperSettings,
            showNotificationSettings = showNotificationSettings.value,
            snackbarMessage = snackbarMessage,
        )
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        matrixUser.value = matrixClient.getCurrentUser()
    }

    private fun CoroutineScope.initAccountManagementUrl(accountManagementUrl: MutableState<String?>) = launch {
        accountManagementUrl.value = matrixClient.getAccountManagementUrl().getOrNull()
    }
}
