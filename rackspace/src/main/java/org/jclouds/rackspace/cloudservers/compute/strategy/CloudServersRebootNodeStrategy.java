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

package org.jclouds.rackspace.cloudservers.compute.strategy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.strategy.GetNodeMetadataStrategy;
import org.jclouds.compute.strategy.RebootNodeStrategy;
import org.jclouds.rackspace.cloudservers.CloudServersClient;
import org.jclouds.rackspace.cloudservers.domain.RebootType;

/**
 * @author Adrian Cole
 */
@Singleton
public class CloudServersRebootNodeStrategy implements RebootNodeStrategy {
   private final CloudServersClient client;
   private final GetNodeMetadataStrategy getNode;

   @Inject
   protected CloudServersRebootNodeStrategy(CloudServersClient client, GetNodeMetadataStrategy getNode) {
      this.client = client;
      this.getNode = getNode;
   }

   @Override
   public NodeMetadata execute(String id) {
      int serverId = Integer.parseInt(id);
      // if false server wasn't around in the first place
      client.rebootServer(serverId, RebootType.HARD);
      return getNode.execute(id);
   }

}