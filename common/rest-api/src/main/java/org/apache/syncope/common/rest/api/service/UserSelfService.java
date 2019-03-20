/*
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
 */
package org.apache.syncope.common.rest.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.PATCH;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.patch.StatusPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.rest.api.RESTHeaders;

/**
 * REST operations for user self-management.
 */
@Api(tags = "UserSelf")
@Path("users/self")
public interface UserSelfService extends JAXRSService {

    /**
     * Returns the user making the service call.
     *
     * @return calling user data, including own UUID and entitlements
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @ApiResponses(
            @ApiResponse(code = 200, message = "Calling user data, including own UUID and entitlements",
                    response = UserTO.class, responseHeaders = {
                @ResponseHeader(name = RESTHeaders.RESOURCE_KEY, response = String.class,
                        description = "UUID of the calling user"),
                @ResponseHeader(name = RESTHeaders.OWNED_ENTITLEMENTS, response = String.class,
                        description = "List of entitlements owned by the calling user")
            }))
    @GET
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response read();

    /**
     * Self-registration for new user.
     *
     * @param userTO user to be created
     * @param storePassword whether password shall be stored internally
     * @return Response object featuring Location header of self-registered user as well as the user itself
     * enriched with propagation status information
     */
    @ApiImplicitParams(
            @ApiImplicitParam(name = RESTHeaders.PREFER, paramType = "header", dataType = "string",
                    value = "Allows the client to specify a preference for the result to be returned from the server",
                    defaultValue = "return-content", allowableValues = "return-content, return-no-content",
                    allowEmptyValue = true))
    @ApiResponses(
            @ApiResponse(code = 201,
                    message = "User successfully created enriched with propagation status information, as Entity,"
                    + "or empty if 'Prefer: return-no-content' was specified",
                    response = ProvisioningResult.class, responseHeaders = {
                @ResponseHeader(name = RESTHeaders.RESOURCE_KEY, response = String.class,
                        description = "UUID generated for the user created"),
                @ResponseHeader(name = HttpHeaders.LOCATION, response = String.class,
                        description = "URL of the user created"),
                @ResponseHeader(name = RESTHeaders.PREFERENCE_APPLIED, response = String.class,
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied") }))
    @POST
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response create(@NotNull UserTO userTO,
            @DefaultValue("true") @QueryParam("storePassword") boolean storePassword);

    /**
     * Self-updates user.
     *
     * @param patch modification to be applied to self
     * @return Response object featuring the updated user
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @ApiImplicitParams({
        @ApiImplicitParam(name = RESTHeaders.PREFER, paramType = "header", dataType = "string",
                value = "Allows the client to specify a preference for the result to be returned from the server",
                defaultValue = "return-content", allowableValues = "return-content, return-no-content",
                allowEmptyValue = true),
        @ApiImplicitParam(name = "key", paramType = "path", dataType = "string",
                value = "User's key") })
    @ApiResponses({
        @ApiResponse(code = 200,
                message = "User successfully updated enriched with propagation status information, as Entity",
                response = ProvisioningResult.class),
        @ApiResponse(code = 204,
                message = "No content if 'Prefer: return-no-content' was specified", responseHeaders =
                @ResponseHeader(name = RESTHeaders.PREFERENCE_APPLIED, response = String.class,
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied")) })
    @PATCH
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response update(@NotNull UserPatch patch);

    /**
     * Self-updates user.
     *
     * @param user complete update
     * @return Response object featuring the updated user
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @ApiImplicitParams({
        @ApiImplicitParam(name = RESTHeaders.PREFER, paramType = "header", dataType = "string",
                value = "Allows the client to specify a preference for the result to be returned from the server",
                defaultValue = "return-content", allowableValues = "return-content, return-no-content",
                allowEmptyValue = true),
        @ApiImplicitParam(name = "key", paramType = "path", dataType = "string",
                value = "User's key") })
    @ApiResponses({
        @ApiResponse(code = 200,
                message = "User successfully updated enriched with propagation status information, as Entity",
                response = ProvisioningResult.class),
        @ApiResponse(code = 204,
                message = "No content if 'Prefer: return-no-content' was specified", responseHeaders =
                @ResponseHeader(name = RESTHeaders.PREFERENCE_APPLIED, response = String.class,
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied")) })
    @PUT
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response update(@NotNull UserTO user);

    /**
     * Self-perform a status update.
     *
     * @param statusPatch status update details
     * @return Response object featuring the updated user enriched with propagation status information
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @ApiImplicitParams({
        @ApiImplicitParam(name = RESTHeaders.PREFER, paramType = "header", dataType = "string",
                value = "Allows the client to specify a preference for the result to be returned from the server",
                defaultValue = "return-content", allowableValues = "return-content, return-no-content",
                allowEmptyValue = true),
        @ApiImplicitParam(name = "key", paramType = "path", dataType = "string",
                value = "User's key") })
    @ApiResponses({
        @ApiResponse(code = 200,
                message = "User successfully updated enriched with propagation status information, as Entity",
                response = ProvisioningResult.class),
        @ApiResponse(code = 204,
                message = "No content if 'Prefer: return-no-content' was specified", responseHeaders =
                @ResponseHeader(name = RESTHeaders.PREFERENCE_APPLIED, response = String.class,
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied")) })
    @POST
    @Path("{key}/status")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response status(@NotNull StatusPatch statusPatch);

    /**
     * Self-deletes user.
     *
     * @return Response object featuring the deleted user
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response delete();

    /**
     * Changes own password when change was forced by an administrator.
     *
     * @param password the password value to update
     *
     * @return Response object featuring the updated user
     */
    @ApiOperation(value = "", authorizations = {
        @Authorization(value = "BasicAuthentication"),
        @Authorization(value = "Bearer") })
    @POST
    @Path("mustChangePassword")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    Response mustChangePassword(String password);

    /**
     * Provides answer for the security question configured for user matching the given username, if any.
     * If provided answer matches the one stored for that user, a password reset token is internally generated,
     * otherwise an error is returned.
     *
     * @param username username for which the security answer is provided
     * @param securityAnswer actual answer text
     */
    @ApiResponses(
            @ApiResponse(code = 204, message = "Operation was successful"))
    @POST
    @Path("requestPasswordReset")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    void requestPasswordReset(@NotNull @QueryParam("username") String username, String securityAnswer);

    /**
     * Reset the password value for the user matching the provided token, if available and still valid.
     * If the token actually matches one of users, and if it is still valid at the time of submission, the matching
     * user's password value is set as provided. The new password value will need anyway to comply with all relevant
     * password policies.
     *
     * @param token password reset token
     * @param password new password to be set
     */
    @ApiResponses(
            @ApiResponse(code = 204, message = "Operation was successful"))
    @POST
    @Path("confirmPasswordReset")
    @Produces({ MediaType.APPLICATION_JSON, SyncopeConstants.APPLICATION_YAML, MediaType.APPLICATION_XML })
    void confirmPasswordReset(@NotNull @QueryParam("token") String token, String password);
}