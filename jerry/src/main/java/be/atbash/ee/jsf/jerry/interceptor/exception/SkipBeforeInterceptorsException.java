/*
 * Copyright 2014-2017 Rudy De Busscher
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
package be.atbash.ee.jsf.jerry.interceptor.exception;

/**
 * Marker exception that can be thrown by a before-method
 * ({@link be.atbash.ee.jsf.jerry.interceptor.RendererInterceptor})
 * to stop the execution of the subsequent interceptors.
 */
public class SkipBeforeInterceptorsException extends Exception {
    private static final long serialVersionUID = -418424051464814888L;
}