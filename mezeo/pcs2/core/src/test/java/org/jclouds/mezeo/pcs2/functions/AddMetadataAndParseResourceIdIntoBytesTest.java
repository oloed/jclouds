/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.mezeo.pcs2.functions;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.testng.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.io.IOUtils;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpUtils;
import org.jclouds.mezeo.pcs2.PCSUtil;
import org.jclouds.mezeo.pcs2.domain.PCSFile;
import org.jclouds.rest.RuntimeDelegateImpl;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code UseResourceIdAsETag}
 * 
 * @author Adrian Cole
 */
@Test(groups = "unit", testName = "pcs2.AddMetadataAndParseResourceIdIntoBytesTest")
public class AddMetadataAndParseResourceIdIntoBytesTest {
   static {
      RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
   }
   HttpResponse response = new HttpResponse();

   PCSUtil createPCSUtil() {
      PCSUtil connection = createMock(PCSUtil.class);
      expect(
               connection
                        .put(
                                 eq(URI
                                          .create("http://localhost/contents/7F143552-AAF5-11DE-BBB0-0BC388ED913B/metadata/foo")),
                                 eq("bar"))).andReturn(true);
      expect(
               connection
                        .put(
                                 eq(URI
                                          .create("http://localhost/contents/7F143552-AAF5-11DE-BBB0-0BC388ED913B/metadata/biz")),
                                 eq("baz"))).andReturn(true);
      replay(connection);
      return connection;
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testNoArgs() {
      AddMetadataAndParseResourceIdIntoBytes function = new AddMetadataAndParseResourceIdIntoBytes(
               createPCSUtil());

      function.apply(response);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testNoRequest() {
      AddMetadataAndParseResourceIdIntoBytes function = new AddMetadataAndParseResourceIdIntoBytes(
               createPCSUtil());
      function.setContext(null, new Object[] { new PCSFile("key") });
      function.apply(response);
   }

   public void testGetEtag() {
      PCSUtil connection = createPCSUtil();
      AddMetadataAndParseResourceIdIntoBytes function = new AddMetadataAndParseResourceIdIntoBytes(
               connection);
      function.setContext(new HttpRequest("GET", URI.create("http://localhost:8080")),
               new Object[] { new PCSFile("key") });
      response.setContent(IOUtils
               .toInputStream("http://localhost/contents/7F143552-AAF5-11DE-BBB0-0BC388ED913B"));
      byte[] eTag = function.apply(response);

      byte[] expected = HttpUtils.fromHexString("7F143552AAF511DEBBB00BC388ED913B");
      assertEquals(eTag, expected);
   }

   public void testMetadataGetEtag() {
      PCSUtil connection = createPCSUtil();
      AddMetadataAndParseResourceIdIntoBytes function = new AddMetadataAndParseResourceIdIntoBytes(
               connection);
      PCSFile pcsFile = new PCSFile("key");
      pcsFile.getMetadata().getUserMetadata().put("foo", "bar");
      pcsFile.getMetadata().getUserMetadata().put("biz", "baz");

      function.setContext(new HttpRequest("GET", URI.create("http://localhost:8080")),
               new Object[] { pcsFile });
      response.setContent(IOUtils
               .toInputStream("http://localhost/contents/7F143552-AAF5-11DE-BBB0-0BC388ED913B"));
      byte[] eTag = function.apply(response);

      byte[] expected = HttpUtils.fromHexString("7F143552AAF511DEBBB00BC388ED913B");
      assertEquals(eTag, expected);
      verify(connection);
   }

}