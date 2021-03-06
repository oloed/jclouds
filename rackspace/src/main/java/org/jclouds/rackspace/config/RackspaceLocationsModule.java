/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
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
 * ====================================================================
 */

package org.jclouds.rackspace.config;

import java.util.Set;

import javax.inject.Singleton;

import org.jclouds.domain.Location;
import org.jclouds.domain.LocationScope;
import org.jclouds.domain.internal.LocationImpl;
import org.jclouds.rest.annotations.Provider;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Configures the locations used in Rackspace services
 * 
 * @author Adrian Cole
 */
public class RackspaceLocationsModule extends AbstractModule {

   @Override
   protected void configure() {
   }

   @Provides
   @Singleton
   Set<? extends Location> provideLocations(@Provider String providerName) {
      Location provider = new LocationImpl(LocationScope.PROVIDER, providerName, providerName, null);
      return ImmutableSet.of(new LocationImpl(LocationScope.ZONE, "DFW1", "Dallas, TX", provider));
   }
}