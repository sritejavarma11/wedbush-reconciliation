package com.wedbush.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.wedbush")
@EnableJpaRepositories(basePackages = "com.wedbush.reconciliation.repository")
public class WedbushReconciliationApplication {

    public static void main(String[] args) {
        // Keep this so the email popup still works!
        System.setProperty("java.awt.headless", "false");

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // #region agent log
        try (java.io.FileWriter fw = new java.io.FileWriter("debug-aac969.log", true)) {
            fw.write("{\"sessionId\":\"aac969\",\"runId\":\"post-fix\",\"hypothesisId\":\"A\",\"location\":\"WedbushReconciliationApplication.main\",\"message\":\"main class package vs repository package\",\"data\":{\"mainClassPackage\":\""
                    + WedbushReconciliationApplication.class.getPackageName()
                    + "\",\"repositoryPackage\":\"com.wedbush.reconciliation.repository\"},\"timestamp\":"
                    + System.currentTimeMillis() + "}\n");
        } catch (Exception ignored) {
        }
        // #endregion
        
        SpringApplication.run(WedbushReconciliationApplication.class, args);
        
        System.out.println("\n✅ Wedbush Backend Server Running on Port 8080.");
        System.out.println("⏳ Waiting for Agentic Dashboard commands via React...");
    }
}