package com.wedbush.reconciliation.service;

import com.wedbush.reconciliation.model.MatchStatus;
import com.wedbush.reconciliation.model.Source;
import com.wedbush.reconciliation.model.Trade;
import com.wedbush.reconciliation.repository.TradeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Service
public class BrokerStatementExtractionService {

    private final ChatClient chatClient;
    private final TradeRepository tradeRepository;

    public BrokerStatementExtractionService(ChatClient.Builder chatClientBuilder,
                                            TradeRepository tradeRepository) {
        this.chatClient = chatClientBuilder.build();
        this.tradeRepository = tradeRepository;
    }

    public void extractTradesFromStatement(String fileName) {
        Resource imageResource = new ClassPathResource(fileName);
        BeanOutputConverter<List<Trade>> converter =
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<Trade>>() {});

        String prompt = """
                Extract the financial trade records from the provided broker statement image. 
                Pay strict attention to the Transaction IDs. Ensure the extracted 'transactionId' field perfectly matches the format 'TXN-INT-XXX' (e.g., TXN-INT-001).
                """ + "\n" + converter.getFormat();

        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .build();

        String responseContent = chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();

        List<Trade> extractedTrades = converter.convert(responseContent);

        for (Trade trade : extractedTrades) {
                trade.setId(null);
                trade.setSource(Source.BROKER);
                trade.setMatchStatus(MatchStatus.PENDING);
                // Add this line to see exactly what Gemini parsed:
                System.out.println("DEBUG - Gemini Extracted: " + trade.getTransactionId() + " | " + trade.getTickerSymbol() + " | $" + trade.getPrice());
        }

        List<Trade> savedTrades = tradeRepository.saveAll(extractedTrades);
        System.out.println("Successfully extracted and saved " + savedTrades.size() + " broker trade records.");
    }
}
