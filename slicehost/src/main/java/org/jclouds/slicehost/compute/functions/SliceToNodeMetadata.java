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

package org.jclouds.slicehost.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.compute.util.ComputeServiceUtils.parseTagFromName;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.domain.Location;
import org.jclouds.logging.Logger;
import org.jclouds.slicehost.domain.Slice;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author Adrian Cole
 */
public class SliceToNodeMetadata implements Function<Slice, NodeMetadata> {
   private final Supplier<Location> location;
   private final Map<Slice.Status, NodeState> sliceToNodeState;
   private final Supplier<Set<? extends Image>> images;
   private final Supplier<Set<? extends Hardware>> hardwares;

   @Resource
   protected Logger logger = Logger.NULL;

   private static class FindImageForSlice implements Predicate<Image> {
      private final Slice slice;

      private FindImageForSlice(Slice slice) {
         this.slice = slice;
      }

      @Override
      public boolean apply(Image input) {
         return input.getProviderId().equals(slice.getImageId() + "");
      }
   }

   private static class FindHardwareForSlice implements Predicate<Hardware> {
      private final Slice slice;

      private FindHardwareForSlice(Slice slice) {
         this.slice = slice;
      }

      @Override
      public boolean apply(Hardware input) {
         return input.getProviderId().equals(slice.getFlavorId() + "");
      }
   }

   @Inject
   SliceToNodeMetadata(Map<Slice.Status, NodeState> sliceStateToNodeState, Supplier<Set<? extends Image>> images,
            Supplier<Location> location, Supplier<Set<? extends Hardware>> hardwares) {
      this.sliceToNodeState = checkNotNull(sliceStateToNodeState, "sliceStateToNodeState");
      this.images = checkNotNull(images, "images");
      this.location = checkNotNull(location, "location");
      this.hardwares = checkNotNull(hardwares, "hardwares");
   }

   @Override
   public NodeMetadata apply(Slice from) {
      String tag = parseTagFromName(from.getName());
      Image image = null;
      try {
         image = Iterables.find(images.get(), new FindImageForSlice(from));
      } catch (NoSuchElementException e) {
         logger.warn("could not find a matching image for slice %s in location %s", from, location);
      }
      Hardware hardware = null;
      try {
         hardware = Iterables.find(hardwares.get(), new FindHardwareForSlice(from));
      } catch (NoSuchElementException e) {
         logger.warn("could not find a matching hardware for slice %s", from);
      }
      return new NodeMetadataImpl(from.getId() + "", from.getName(), from.getId() + "", location.get(), null,
               ImmutableMap.<String, String> of(), tag, hardware, from.getImageId() + "", image != null ? image
                        .getOperatingSystem() : null, sliceToNodeState.get(from.getStatus()), Iterables.filter(from
                        .getAddresses(), new Predicate<String>() {

                  @Override
                  public boolean apply(String input) {
                     return !input.startsWith("10.");
                  }

               }), Iterables.filter(from.getAddresses(), new Predicate<String>() {

                  @Override
                  public boolean apply(String input) {
                     return input.startsWith("10.");
                  }

               }), null);
   }
}
