package dockmind.demo.service;

import dockmind.demo.config.AppProperties;
import dockmind.demo.dto.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final AppProperties appProperties;
    private final ChatClient chatClient;

    public ChatResponseDto askQuestion(ChatRequestDto chatRequestDto) {
        long startTime = System.currentTimeMillis();

        String conversationId = chatRequestDto.getConversationId() != null ? chatRequestDto.getConversationId() : UUID.randomUUID().toString();

        List<Document> similarDocuments = this.retriveRelevantDocuments(chatRequestDto.getQuestion() , chatRequestDto.getDocumentId(), chatRequestDto.getTopK(), chatRequestDto.getMinSimilarity());

        List<CitationDto> citationDtos = similarDocuments.stream().map(this::mapToCitation).toList();

        String contextText = buildContextString(similarDocuments);

        String prompt = buildPrompt(chatRequestDto.getQuestion(), contextText);

        String answer  = this.chatClient.prompt()
                .user(prompt).call().content();

        long responseTime = System.currentTimeMillis() - startTime;

        return ChatResponseDto.builder().answer(answer).conversationId(chatRequestDto.getConversationId() != null ? chatRequestDto.getConversationId() : UUID.randomUUID().toString()).citation(citationDtos).responseTimeMS(responseTime).build();

    }

    private String buildContextString(List<Document> similarDocuments) {
        return similarDocuments.stream().map(doc -> {
            String fileName = (String) doc.getMetadata().getOrDefault("fileName", "Unknown File");
            Object page = doc.getMetadata().getOrDefault("pageNumber", "N/A");
            return String.format("[Source: %s | Page: %s]\n%s", fileName, page, doc.getText());
        }).collect(Collectors.joining("\n\n---\n\n"));
    }
    private String buildPrompt(String question , String contextText) {
        if (contextText != null && !contextText.isBlank()) {
            return String.format("""
                           Document Context:
                           ---------------------
                           %s
                           ---------------------                                        
                           User Message / Question: %s             
                           Instructions:
                           - If the user's question relates to the document context above, prioritize answering using that context and reference key sections.
                           - If the user is asking a general question, greeting, or discussing topics beyond the document context, respond helpfully and conversationally using your general knowledge while weaving in relevant document context if applicable
                    
                    """, contextText, question);
        } else {
            return String.format("""
                       User Message / Question:
                       %s                         
                       Instructions:
                          - Respond helpfully, accurately, and conversationally to the user's message using your broad knowledge base.
                    
                    """, question);
        }
    }

    public Flux<String> streamQuestionAnswer(ChatRequestDto requestDto) {
        List<Document> relevantDocuments = retriveRelevantDocuments(
                requestDto.getQuestion(),
                requestDto.getDocumentId(),
                requestDto.getTopK(),
                requestDto.getMinSimilarity()
        );
        String contextText = buildContextString(relevantDocuments);
        String userPrompt = buildPrompt(requestDto.getQuestion(), contextText);
        return chatClient.prompt()
                .user(userPrompt)
                .stream().content();
    }



    public SearchResultDto searchSimilarChunk(SearchRequestDto searchRequestDto){

        List<Document> matchedDocs = retriveRelevantDocuments(
                searchRequestDto.getQuery(),
                searchRequestDto.getDocumentId(),
                searchRequestDto.getTopK(),
                searchRequestDto.getSimilaritySearch()
        );

        List<CitationDto> citations = new ArrayList<>();

        for (Document doc : matchedDocs) {
            CitationDto citation = mapToCitation(doc);
            citations.add(citation);
        }

        return SearchResultDto.builder()
                .query(searchRequestDto.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();
    }

    private CitationDto mapToCitation(Document document) {

        Map<String, Object> meta = document.getMetadata();
        UUID docId = null;
        docId = UUID.fromString(meta.get("documentId").toString());

        Integer chunkIndex = null;
        if (meta.get("chunkIndex") instanceof Number n) {
            chunkIndex = n.intValue();
        }

        Integer pageNumber = null;
        if (meta.get("pageNumber") instanceof Number n) {
            pageNumber = n.intValue();
        } else if (meta.get("page_number") instanceof Number n) {
            pageNumber = n.intValue();
        }

        Double score = null;
        if (meta.get("distance") instanceof Number n) {
            score = 1.0 - n.doubleValue();
        }
        return CitationDto.builder().documentId(docId).fileName((String) meta.getOrDefault("fileName", "Unknown")).chunkIndex(chunkIndex).pageNumber(pageNumber).snippet(document.getText()).similarityScore(score).metadata(meta).build();
    }


    private List<Document> retriveRelevantDocuments(String query, UUID documentId, Integer topK, Double similaritySearch) {

        int effectiveTopK = appProperties.getRag().getTopK();
        double effectiveSimilarity=  appProperties.getRag().getSimilarityThreshold();

        //searchrequestbuilder
        SearchRequest.Builder searchRequestBuilder = SearchRequest.builder().
                query(query).
                topK(effectiveTopK).
                similarityThreshold(effectiveSimilarity);


        //filterexpression
        if (documentId != null) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();

            searchRequestBuilder.filterExpression(b.eq("documentId", documentId.toString()).build());
        }

        try {
            List<Document> documents = vectorStore.similaritySearch(searchRequestBuilder.build());
            log.info("Retrieved {} chunks for query: '{}' (scoped docId: {})", documents.size(), query, documentId);
            return documents;
        } catch (Exception e) {
            log.error("Similarity search failed for query: '{}'", query, e);
            return Collections.emptyList();
        }

    }

}
