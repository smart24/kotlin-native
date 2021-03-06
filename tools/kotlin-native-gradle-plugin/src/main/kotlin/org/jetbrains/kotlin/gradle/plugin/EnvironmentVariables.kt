/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KonanPlugin.ProjectProperty
import java.io.File

/**
 *  The plugin allows an IDE to specify some building parameters. These parameters
 *  are passed to the plugin via environment variables. Two variables are supported:
 *      - CONFIGURATION_BUILD_DIR    - An absolute path to a destination directory for all compilation tasks.
 *                                     The IDE should take care about specifying different directories
 *                                     for different targets. This setting has less priority than
 *                                     an explicitly specified destination directory in the build script.
 *
 *      - DEBUGGING_SYMBOLS          - If YES, the debug support will be enabled for all artifacts. This option has less
 *                                     priority than explicitly specified enableDebug option in the build script and
 *                                     enableDebug project property.
 *
 *      - KONAN_ENABLE_OPTIMIZATIONS - If YES, optimizations will be enabled for all artifacts by default. This option
 *                                     has less priority than explicitly specified enableOptimizations option in the
 *                                     build script.
 *
 *  Support for environment variables should be explicitly enabled by setting a project property:
 *      konan.useEnvironmentVariables = true.
 */

internal interface EnvironmentVariables {
    val configurationBuildDir: File?
    val debuggingSymbols: Boolean
    val enableOptimizations: Boolean
}

internal class EnvironmentVariablesUnused: EnvironmentVariables {
    override val configurationBuildDir: File?
        get() = null

    override val debuggingSymbols: Boolean
        get() = false

    override val enableOptimizations: Boolean
        get() = false
}

internal class EnvironmentVariablesImpl:  EnvironmentVariables {
    override val configurationBuildDir: File?
        get() = System.getenv("CONFIGURATION_BUILD_DIR")?.let {
            File(it).apply { check(isAbsolute) { "A path passed using CONFIGURATION_BUILD_DIR should be absolute" } }
        }

    override val debuggingSymbols: Boolean
        get() = System.getenv("DEBUGGING_SYMBOLS")?.toUpperCase() == "YES"

    override val enableOptimizations: Boolean
        get() = System.getenv("KONAN_ENABLE_OPTIMIZATIONS")?.toUpperCase() == "YES"
}

internal val Project.useEnvironmentVariables: Boolean
    get() = findProperty(ProjectProperty.KONAN_USE_ENVIRONMENT_VARIABLES)?.toString()?.toBoolean() ?: false

internal val Project.environmentVariables: EnvironmentVariables
    get() = if (useEnvironmentVariables) {
        EnvironmentVariablesImpl()
    } else {
        EnvironmentVariablesUnused()
    }
