package org.gitlab4j.api;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;

import org.gitlab4j.api.utils.JacksonJson;
import org.gitlab4j.api.webhook.BuildEvent;
import org.gitlab4j.api.webhook.Event;
import org.gitlab4j.api.webhook.IssueEvent;
import org.gitlab4j.api.webhook.MergeRequestEvent;
import org.gitlab4j.api.webhook.NoteEvent;
import org.gitlab4j.api.webhook.PipelineEvent;
import org.gitlab4j.api.webhook.PushEvent;
import org.gitlab4j.api.webhook.TagPushEvent;
import org.gitlab4j.api.webhook.WikiPageEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestGitLabApiEvents {

    private static JacksonJson jacksonJson;

    public TestGitLabApiEvents() {
        super();
    }

    @BeforeClass
    public static void setup() {
        jacksonJson = new JacksonJson();
        jacksonJson.getObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Test
    public void testIssueEvent() throws Exception {

        Event issueEvent = makeFakeApiCall(IssueEvent.class, "issue-event");
        assertTrue(compareJson(issueEvent, "issue-event"));
    }

    @Test
    public void testMergeRequestEvent() throws Exception {

        Event mergeRequestEvent = makeFakeApiCall(MergeRequestEvent.class, "merge-request-event");
        assertTrue(compareJson(mergeRequestEvent, "merge-request-event"));
    }

    @Test
    public void testPipelineEvent() throws Exception {

        Event event = makeFakeApiCall(PipelineEvent.class, "pipeline-event");
        assertTrue(compareJson(event, "pipeline-event"));
    }

    @Test
    public void testPushEvent() throws Exception {

        Event pushEvent = makeFakeApiCall(PushEvent.class, "push-event");
        assertTrue(compareJson(pushEvent, "push-event"));
    }

    @Test
    public void testTagPushEvent() throws Exception {

        Event pushEvent = makeFakeApiCall(TagPushEvent.class, "tag-push-event");
        assertTrue(compareJson(pushEvent, "tag-push-event"));
    }

    @Test
    public void testNoteCommitEvent() throws Exception {

        Event noteEvent = makeFakeApiCall(NoteEvent.class, "note-commit-event");
        assertTrue(compareJson(noteEvent, "note-commit-event"));
    }

    @Test
    public void testNoteMergeRequestEvent() throws Exception {

        Event noteEvent = makeFakeApiCall(NoteEvent.class, "note-merge-request-event");
        assertTrue(compareJson(noteEvent, "note-merge-request-event"));
    }

    @Test
    public void testNoteIssueEvent() throws Exception {

        Event noteEvent = makeFakeApiCall(NoteEvent.class, "note-issue-event");
        assertTrue(compareJson(noteEvent, "note-issue-event"));
    }

    @Test
    public void testNoteSnippetEvent() throws Exception {

        Event noteEvent = makeFakeApiCall(NoteEvent.class, "note-snippet-event");
        assertTrue(compareJson(noteEvent, "note-snippet-event"));
    }

    @Test
    public void testBuildEvent() throws Exception {

        Event event = makeFakeApiCall(BuildEvent.class, "build-event");
        assertTrue(compareJson(event, "build-event"));
    }

    @Test
    public void testWikiPageEvent() throws Exception {

        Event event = makeFakeApiCall(WikiPageEvent.class, "wiki-page-event");
        assertTrue(compareJson(event, "wiki-page-event"));
    }

    @Test
    public void testPolymorphicEvent() throws Exception {

        Event event = makeFakeApiCall(Event.class, "build-event");
        assertTrue(compareJson(event, "build-event"));

        event = makeFakeApiCall(Event.class, "issue-event");
        assertTrue(compareJson(event, "issue-event"));

        event = makeFakeApiCall(Event.class, "merge-request-event");
        assertTrue(compareJson(event, "merge-request-event"));

        event = makeFakeApiCall(Event.class, "note-commit-event");
        assertTrue(compareJson(event, "note-commit-event"));

        event = makeFakeApiCall(Event.class, "note-issue-event");
        assertTrue(compareJson(event, "note-issue-event"));

        event = makeFakeApiCall(Event.class, "note-merge-request-event");
        assertTrue(compareJson(event, "note-merge-request-event"));

        event = makeFakeApiCall(Event.class, "note-snippet-event");
        assertTrue(compareJson(event, "note-snippet-event"));

        event = makeFakeApiCall(Event.class, "pipeline-event");
        assertTrue(compareJson(event, "pipeline-event"));

        event = makeFakeApiCall(Event.class, "push-event");
        assertTrue(compareJson(event, "push-event"));

        event = makeFakeApiCall(Event.class, "tag-push-event");
        assertTrue(compareJson(event, "tag-push-event"));

        event = makeFakeApiCall(Event.class, "wiki-page-event");
        assertTrue(compareJson(event, "wiki-page-event"));
    }

    private <T> T makeFakeApiCall(Class<T> returnType, String file) throws JsonParseException, JsonMappingException, IOException {

        InputStreamReader reader = new InputStreamReader(GitLabApi.class.getResourceAsStream(file + ".json"));
        ObjectMapper objectMapper = jacksonJson.getContext(returnType);
        return (objectMapper.readValue(reader, returnType));
    }

    private <T> boolean compareJson(T apiObject, String file) throws IOException {

        InputStreamReader reader = new InputStreamReader(GitLabApi.class.getResourceAsStream(file + ".json"));
        String objectJson = jacksonJson.marshal(apiObject);
        JsonNode tree1 = jacksonJson.getObjectMapper().readTree(objectJson.getBytes());
        JsonNode tree2 = jacksonJson.getObjectMapper().readTree(reader);

        boolean sameJson = tree1.equals(tree2);
        if (!sameJson) {
            System.out.println("JSON did not match:");
            sortedDump(tree1);
            sortedDump(tree2);
        }
        return (sameJson);
    }

    private void sortedDump(final JsonNode node) throws JsonProcessingException {
        final Object obj = jacksonJson.getObjectMapper().treeToValue(node, Object.class);
        System.out.println(jacksonJson.getObjectMapper().writeValueAsString(obj));
    }
}
