package com.wedbush.reconciliation.service;

import com.wedbush.reconciliation.model.BatchResolutionResponse;
import com.wedbush.reconciliation.model.MatchStatus;
import com.wedbush.reconciliation.model.Source;
import com.wedbush.reconciliation.model.Trade;
import com.wedbush.reconciliation.model.TradeResolution;
import com.wedbush.reconciliation.repository.TradeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AgenticResolutionService {

    private static final String BROKER_OPS_EMAIL = "broker-ops@wedbush.com";

    private final ChatClient chatClient;
    private final TradeRepository tradeRepository;

    public AgenticResolutionService(ChatClient.Builder chatClientBuilder,
                                    TradeRepository tradeRepository) {
        this.chatClient = chatClientBuilder.build();
        this.tradeRepository = tradeRepository;
    }

    public void resolveFlaggedTrades() {
        List<Trade> flaggedInternalTrades =
                tradeRepository.findBySourceAndMatchStatus(Source.INTERNAL, MatchStatus.FLAGGED);

        if (flaggedInternalTrades == null || flaggedInternalTrades.isEmpty()) {
            System.out.println("No flagged trades found requiring agentic resolution.");
            return;
        }

        StringBuilder tradesData = new StringBuilder();
        for (Trade trade : flaggedInternalTrades) {
            tradesData.append(String.format(
                "- Transaction ID: %s, Security: %s, Discrepancy: %s\n",
                trade.getTransactionId(), trade.getTickerSymbol(), trade.getDiscrepancyNotes()
            ));
        }

        String userPrompt = String.format("""
            You are a senior trade operations automation agent at Wedbush.
            Review the following list of flagged trade discrepancies between our internal ledger and the broker statements:
            
            %s
            
            For each discrepancy listed above, generate a professional, concise trade break resolution email addressed to the Broker Operations Team.
            Do not include subject lines, just generate the email body content.
            Ensure the output matches the required JSON structure precisely.
            """, tradesData.toString());

        try {
            System.out.println("Sending batch request to Gemini API...");
            
            BatchResolutionResponse batchResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .entity(BatchResolutionResponse.class);

            if (batchResponse != null && batchResponse.resolutions() != null) {
                for (TradeResolution resolution : batchResponse.resolutions()) {
                    String txId = resolution.transactionId();
                    String emailBody = resolution.emailBody();

                    // Trigger the desktop email client
                    openEmailClient(txId, emailBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to execute agentic reconciliation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEmailClient(String txId, String emailBody) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                String subject = "Trade Break Alert: " + txId;
                
                // Encode the text so the mail client reads spaces and line breaks properly
                String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20");
                String encodedBody = URLEncoder.encode(emailBody, StandardCharsets.UTF_8).replace("+", "%20");
                
                String mailtoUri = String.format("mailto:%s?subject=%s&body=%s", 
                                                 BROKER_OPS_EMAIL, encodedSubject, encodedBody);
                
                Desktop.getDesktop().mail(new URI(mailtoUri));
                System.out.println("Successfully opened email draft for " + txId);
            } else {
                System.out.println("Desktop mail client is not supported on this OS. Email body generated:");
                System.out.println(emailBody);
            }
        } catch (Exception e) {
            System.err.println("Could not open desktop mail client: " + e.getMessage());
        }
    }
}