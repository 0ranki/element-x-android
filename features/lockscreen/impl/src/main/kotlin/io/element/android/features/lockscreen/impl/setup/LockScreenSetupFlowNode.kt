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

package io.element.android.features.lockscreen.impl.setup

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.setup.biometric.SetupBiometricNode
import io.element.android.features.lockscreen.impl.setup.pin.SetupPinNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class LockScreenSetupFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pinCodeManager: PinCodeManager,
    private val biometricUnlockManager: BiometricUnlockManager,
) : BackstackNode<LockScreenSetupFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Pin,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    interface Callback : Plugin {
        fun onSetupDone()
    }

    private fun onSetupDone() {
        plugins<Callback>().forEach { it.onSetupDone() }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Pin : NavTarget

        @Parcelize
        data object Biometric : NavTarget
    }

    private val pinCodeManagerCallback = object : DefaultPinCodeManagerCallback() {

        override fun onPinCodeCreated() {
            backstack.newRoot(NavTarget.Biometric)
        }
    }

    init {
        lifecycle.subscribe(
            onCreate = {
                pinCodeManager.addCallback(pinCodeManagerCallback)
            },
            onDestroy = {
                pinCodeManager.removeCallback(pinCodeManagerCallback)
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Pin -> {
                createNode<SetupPinNode>(buildContext)
            }
            NavTarget.Biometric -> {
                val callback = object : SetupBiometricNode.Callback {
                    override fun onBiometricSetupDone() {
                        onSetupDone()
                    }
                }
                createNode<SetupBiometricNode>(buildContext, plugins = listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
