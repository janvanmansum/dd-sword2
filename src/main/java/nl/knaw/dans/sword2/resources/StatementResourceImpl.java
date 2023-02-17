/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.sword2.resources;

import nl.knaw.dans.sword2.api.entry.Link;
import nl.knaw.dans.sword2.api.statement.Feed;
import nl.knaw.dans.sword2.api.statement.FeedAuthor;
import nl.knaw.dans.sword2.api.statement.FeedCategory;
import nl.knaw.dans.sword2.api.statement.FeedContent;
import nl.knaw.dans.sword2.api.statement.FeedEntry;
import nl.knaw.dans.sword2.api.statement.TextElement;
import nl.knaw.dans.sword2.core.DepositState;
import nl.knaw.dans.sword2.core.auth.Depositor;
import nl.knaw.dans.sword2.core.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.core.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.core.service.DepositHandler;
import nl.knaw.dans.sword2.core.service.ErrorResponseFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class StatementResourceImpl extends BaseResource implements StatementResource {
    private static final Logger log = LoggerFactory.getLogger(StatementResourceImpl.class);

    private final URI baseUrl;
    private final DepositHandler depositHandler;

    public StatementResourceImpl(URI baseUrl, DepositHandler depositHandler, ErrorResponseFactory errorResponseFactory) {
        super(errorResponseFactory);
        this.baseUrl = baseUrl;
        this.depositHandler = depositHandler;
    }

    @Override
    public Response getStatement(String depositId, HttpHeaders headers, Depositor depositor) {
        log.info("Received getStatement request for deposit with ID {} and user {}", depositId, depositor.getName());

        var url = baseUrl.resolve("/statement/" + depositId).toString();
        var feed = new Feed();
        feed.setId(url);
        feed.setTitle(new TextElement(String.format("Deposit %s", depositId), "text"));
        feed.addLink(new Link(URI.create(url), "self", null));
        feed.getAuthors().add(new FeedAuthor("DANS-EASY"));

        try {
            var deposit = depositHandler.getDeposit(depositId, depositor);

            System.out.println("DEPOSIT: " + deposit);
            feed.setUpdated(deposit.getCreated().toString());
            feed.setCategory(new FeedCategory("State", "http://purl.org/net/sword/terms/state",
                deposit.getState().toString(), deposit.getStateDescription()));

            /*
             * This code was responsible for the atom entry part of the feed element (in easy-sword2).
             * To make this work we only need the URI, which is just urn:uuid: + deposit ID,
             * an optional DOI and an optional URN.
             *
             * entry.setContent(new IRI(resource.getUri()), resource.getMediaType());
             * entry.setId(resource.getUri());
             * entry.setTitle("Resource " + resource.getUri());
             * entry.setSummary("Resource Part");
             * entry.setUpdated(new Date());
             * for (String linkHref : resource.getSelfLinks()) {
             *     entry.addLink(linkHref, Link.REL_SELF);
             * }
             *
             * yield new AtomStatement(statementIri, "DANS-EASY", s"Deposit $id", props.getLastModifiedTimestamp.get.toString) {
                  addState(state.toString, stateDesc)
                  val archivalResource = new ResourcePart(new URI(s"urn:uuid:$id").toASCIIString)
                  archivalResource.setMediaType("multipart/related")

                  optDoi.foreach(doi => {
                    archivalResource.addSelfLink(new URI(s"https://doi.org/$doi").toASCIIString)
                  })
                  optUrn.foreach(urn => {
                    archivalResource.addSelfLink(new URI(s"https://www.persistent-identifier.nl?identifier=$urn").toASCIIString)
                  })

                  addResource(archivalResource)
                }
                *
             */

            var entry = new FeedEntry();
            var id = String.format("urn:uuid:%s", depositId);
            entry.setId(id);
            entry.setContent(new FeedContent(id, "multipart/related"));
            entry.setTitle(new TextElement(String.format("Resource %s", id), "text"));
            entry.setSummary(new TextElement("Resource Part", "text"));
            entry.setUpdated(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            if (StringUtils.isNotBlank(deposit.getDoi())) {
                var uri = new URI("https://doi.org/" + deposit.getDoi());
                entry.getLinks().add(new Link(uri, "self", null));
            }

            if (StringUtils.isNotBlank(deposit.getUrn())) {
                var uri = new URI("https://www.persistent-identifier.nl?identifier=" + deposit.getUrn());
                entry.getLinks().add(new Link(uri, "self", null));
            }

            feed.addEntry(entry);

            return Response.status(Response.Status.OK)
                .entity(feed)
                .build();
        }
        catch (InvalidDepositException e) {
            feed.setCategory(new FeedCategory("State", "http://purl.org/net/sword/terms/state",
                DepositState.INVALID.toString(), e.getMessage()));

            log.error("Deposit with id {} is invalid", depositId, e);

            return Response.status(Response.Status.OK)
                .entity(feed)
                .build();
        }
        catch (DepositNotFoundException e) {
            log.error("Deposit with id {} could not be found", depositId, e);
            throw new WebApplicationException(404);
        }
        catch (URISyntaxException e) {
            log.error("Unable to create URI from ID {}", depositId, e);
            throw new WebApplicationException(500);
        }
    }
}
