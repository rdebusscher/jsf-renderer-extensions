/*
 * Copyright 2014-2020 Rudy De Busscher
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
package be.atbash.ee.jsf.jerry.utils;

import be.atbash.ee.jsf.jerry.component.ComponentInitializer;
import be.atbash.ee.jsf.jerry.interceptor.RendererInterceptor;
import be.atbash.ee.jsf.jerry.ordering.InvocationOrderComparator;
import be.atbash.util.CDIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class InvocationOrderedArtifactsProvider {

    private static final InvocationOrderedArtifactsProvider INSTANCE = new InvocationOrderedArtifactsProvider();
    private static final Object LOCK = new Object();

    private List<RendererInterceptor> rendererInterceptors;
    private List<ComponentInitializer> initializers;

    private InvocationOrderedArtifactsProvider() {
    }

    private void prepareRendererInterceptors() {
        // CDIUtils.retrieveInstances returns unmodifiable List.
        rendererInterceptors = new ArrayList<>(CDIUtils.retrieveInstances(RendererInterceptor.class));
        rendererInterceptors.sort(new InvocationOrderComparator<>());
    }

    public static List<RendererInterceptor> getRendererInterceptors() {
        if (INSTANCE.rendererInterceptors == null) {
            synchronized (LOCK) {
                INSTANCE.prepareRendererInterceptors();
            }
        }
        return INSTANCE.rendererInterceptors;
    }

    private void prepareInitializers() {
        // CDIUtils.retrieveInstances returns unmodifiable List.
        initializers = new ArrayList<>(CDIUtils.retrieveInstances(ComponentInitializer.class));
        initializers.sort(new InvocationOrderComparator<>());
    }

    public static List<ComponentInitializer> getComponentInitializers() {
        if (INSTANCE.initializers == null) {
            synchronized (LOCK) {
                INSTANCE.prepareInitializers();
            }
        }
        return INSTANCE.initializers;
    }

}
