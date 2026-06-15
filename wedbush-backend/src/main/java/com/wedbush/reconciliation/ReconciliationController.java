package com.wedbush.reconciliation;

import com.wedbush.reconciliation.service.AgenticResolutionService;
import com.wedbush.reconciliation.service.BrokerStatementExtractionService;
import com.wedbush.reconciliation.service.ReconciliationService;
import com.wedbush.reconciliation.service.TradeIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/reconciliation")
@CrossOrigin(origins = "http://localhost:5173")
public class ReconciliationController {

    private final TradeIngestionService tradeIngestionService;
    private final BrokerStatementExtractionService brokerStatementExtractionService;
    private final ReconciliationService reconciliationService;
    private final AgenticResolutionService agenticResolutionService;

    // Injecting all your existing services
    public ReconciliationController(TradeIngestionService tradeIngestionService,
                                    BrokerStatementExtractionService brokerStatementExtractionService,
                                    ReconciliationService reconciliationService,
                                    AgenticResolutionService agenticResolutionService) {
        this.tradeIngestionService = tradeIngestionService;
        this.brokerStatementExtractionService = brokerStatementExtractionService;
        this.reconciliationService = reconciliationService;
        this.agenticResolutionService = agenticResolutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runPipeline(
            @RequestParam("csv") MultipartFile csvFile,
            @RequestParam("image") MultipartFile imageFile) {
        
        try {
            System.out.println("\n🚀 [API TRIGGERED] Agentic Pipeline Initialized via Frontend Dashboard");
            
            // 1. Temporarily save the uploaded files so your existing services can read them
            File internalCsv = new File("internal_trades.csv").getAbsoluteFile();
            File brokerImage = new File("broker_statement.png").getAbsoluteFile();
            
            csvFile.transferTo(internalCsv);
            imageFile.transferTo(brokerImage);

            // 2. Execute your actual pipeline!
            System.out.println("⚙️ Processing files through AI Engine...");
            tradeIngestionService.ingestInternalTrades(internalCsv.getName());
            brokerStatementExtractionService.extractTradesFromStatement(brokerImage.getName());
            reconciliationService.reconcilePendingTrades();

            // --- DEMO TOGGLE ---
            // Set this to 0 to show the "100% Match" success screen in React
            // Set this to 1 to show the "Exception Found" and trigger the Email pop-up
            int flaggedBreaks = 1; 

            if (flaggedBreaks == 0) {
                System.out.println("✅ 100% Match. No email required.");
                return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Reconciliation complete: 100% Match. No Agentic resolution required."
                ));
            } else {
                System.out.println("⚠️ Discrepancy found. Drafting email...");
                
                // 3. Trigger the email pop-up ONLY if there is a break
                agenticResolutionService.resolveFlaggedTrades();
                
                return ResponseEntity.ok(Map.of(
                    "status", "EXCEPTION_FOUND",
                    "message", "Reconciliation complete: 1 anomaly found. Agentic email drafted in desktop client."
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during pipeline execution: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
