package com.example.pbj2;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class WorkerConfiguration {
  private final List<AutoCloseable> workers = new CopyOnWriteArrayList<>();

  @Bean(destroyMethod = "close")
  ZeebeClient zeebeClient(@Value("${camunda.zeebe.gateway-address:localhost:26500}") String address,
                          @Value("${camunda.zeebe.plaintext:true}") boolean plaintext) {
    var builder = ZeebeClient.newClientBuilder().gatewayAddress(address);
    if (plaintext) builder.usePlaintext();
    return builder.build();
  }

  @Bean
  Object registerWorkers(ZeebeClient client) {
    register(client, "validate-applicant", (vars) -> {
      if ("WORKER_FAIL".equalsIgnoreCase(String.valueOf(vars.get("applicantId")))) throw new IllegalStateException("Intentional worker failure for recovery exercise");
      boolean valid = nonBlank(vars.get("applicantId")) && number(vars.get("monthlyIncome")) > 0 && number(vars.get("requestedAmount")) > 0;
      return Map.of("isValid", valid, "validationMessage", valid ? "Applicant fields valid" : "Missing applicant ID or non-positive income/amount");
    });
    register(client, "validate-order-check-stock", (vars) -> {
      String product = String.valueOf(vars.getOrDefault("productId", ""));
      if ("WORKER_FAIL".equalsIgnoreCase(product)) throw new IllegalStateException("Intentional worker failure for recovery exercise");
      int quantity = (int) number(vars.get("quantity"));
      Map<String, Double> prices = Map.of("P100", 1200.0, "P200", 28000.0, "P300", 65000.0);
      Map<String, Integer> inventory = Map.of("P100", 25, "P200", 3, "P300", 2);
      boolean valid = nonBlank(vars.get("customerReference")) && prices.containsKey(product) && quantity > 0;
      boolean stock = valid && quantity <= inventory.getOrDefault(product, 0);
      double total = valid ? prices.get(product) * quantity : 0.0;
      return Map.of("isValid", valid, "stockAvailable", stock, "orderTotal", total,
          "requiresVerification", total >= 50000.0, "orderId", vars.getOrDefault("orderId", "ORD-" + UUID.randomUUID()),
          "inventoryMessage", stock ? "Stock available" : "Insufficient stock or invalid order");
    });
    register(client, "fulfil-order", (vars) -> Map.of("fulfilmentStatus", "DISPATCHED", "dispatchReference", "DSP-" + UUID.randomUUID()));
    register(client, "validate-registration", (vars) -> {
      if ("WORKER_FAIL".equalsIgnoreCase(String.valueOf(vars.get("patientReference")))) throw new IllegalStateException("Intentional worker failure for recovery exercise");
      boolean valid = nonBlank(vars.get("patientReference")) && nonBlank(vars.get("appointmentPreference")) && nonBlank(vars.get("symptomsSummary"));
      return Map.of("isRegistrationValid", valid, "registrationMessage", valid ? "Registration valid" : "Required registration fields are missing");
    });
    register(client, "book-appointment", (vars) -> {
      Object response = vars.get("slotsResponse");
      List<?> slots = List.of();
      if (response instanceof Map<?, ?> m && m.get("availableSlots") instanceof List<?> l) slots = l;
      if (slots.isEmpty()) throw new IllegalStateException("No available slots in slotsResponse.availableSlots");
      Object chosen = slots.get(0);
      return Map.of("bookingStatus", "BOOKED", "bookedSlot", chosen, "bookingReference", "APT-" + UUID.randomUUID());
    });
    return new Object();
  }

  private interface JobLogic { Map<String, Object> apply(Map<String, Object> variables) throws Exception; }
  private void register(ZeebeClient client, String type, JobLogic logic) {
    var worker = client.newWorker().jobType(type).name("pbj2-" + type).handler((JobClient jobClient, ActivatedJob job) -> {
      try {
        Map<String,Object> output = logic.apply(job.getVariablesAsMap());
        jobClient.newCompleteCommand(job.getKey()).variables(output).send().join();
      } catch (Exception ex) {
        int retries = Math.max(0, job.getRetries() - 1);
        jobClient.newFailCommand(job.getKey()).retries(retries).errorMessage(ex.getMessage() == null ? "Worker failed" : ex.getMessage()).send().join();
      }
    }).open();
    workers.add(worker);
  }
  private static boolean nonBlank(Object o) { return o != null && !String.valueOf(o).isBlank() && !"null".equals(o); }
  private static double number(Object o) { try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0; } }
  @PreDestroy public void closeWorkers() { for (AutoCloseable w : workers) try { w.close(); } catch (Exception ignored) {} }
}
