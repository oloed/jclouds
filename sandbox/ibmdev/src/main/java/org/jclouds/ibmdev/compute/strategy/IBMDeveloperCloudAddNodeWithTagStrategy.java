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

package org.jclouds.ibmdev.compute.strategy;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.ibmdev.options.CreateInstanceOptions.Builder.authorizePublicKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.strategy.AddNodeWithTagStrategy;
import org.jclouds.ibmdev.IBMDeveloperCloudClient;
import org.jclouds.ibmdev.compute.domain.IBMImage;
import org.jclouds.ibmdev.compute.domain.IBMSize;
import org.jclouds.ibmdev.domain.Instance;

import com.google.common.base.Function;

/**
 * @author Adrian Cole
 */
@Singleton
public class IBMDeveloperCloudAddNodeWithTagStrategy implements AddNodeWithTagStrategy {

   private final IBMDeveloperCloudClient client;
   private final Function<Instance, NodeMetadata> instanceToNodeMetadata;

   @Inject
   protected IBMDeveloperCloudAddNodeWithTagStrategy(IBMDeveloperCloudClient client,
            Function<Instance, NodeMetadata> instanceToNodeMetadata) {
      this.client = checkNotNull(client, "client");
      this.instanceToNodeMetadata = checkNotNull(instanceToNodeMetadata, "instanceToNodeMetadata");
   }

   @Override
   public NodeMetadata execute(String tag, String name, Template template) {
      Instance instance = client.createInstanceInLocation(template.getLocation().getId(), name, IBMImage.class.cast(
               template.getImage()).getRawImage().getId(), IBMSize.class.cast(template.getSize()).getInstanceType()
               .getId(), authorizePublicKey(tag));
      return instanceToNodeMetadata.apply(client.getInstance(instance.getId()));
   }
}