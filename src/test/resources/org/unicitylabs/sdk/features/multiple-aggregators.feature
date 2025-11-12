Feature: Bulk Commitment Testing with Multiple Aggregators

  Scenario: Submit commitments to multiple aggregators concurrently
    Given the aggregator URLs are configured
      | http://localhost:3000 |
#      | http://localhost:3001  |
#      | http://localhost:3002 |
#      | http://localhost:3003 |
#      | http://localhost:3004 |
#      | http://localhost:3005 |
#      | http://localhost:8080 |
    And trust-base.json is set
    And the aggregator clients are initialized
    And I configure 1 threads with 10 commitments each
    When I submit all mint commitments concurrently to all aggregators
    Then all mint commitments should receive inclusion proofs from all aggregators within 1380 seconds
    And I should see performance metrics for each aggregator

  Scenario: Submit commitments to correct aggregators shards in parallel
    Given the aggregator URLs are configured with shard_id_length 3
    And trust-base.json is set
    And the aggregator clients are initialized
    And I configure 100 threads with 100 commitments each
    When I submit all mint commitments to correct shards concurrently
    Then all shard mint commitments should receive inclusion proofs from all aggregators within 300 seconds
    And I should see performance metrics for each aggregator

  Scenario: Submit incorrect commitments to multiple aggregators concurrently
    Given the aggregator URLs are configured
    And trust-base.json is set
    And the aggregator clients are initialized
    And I configure 1 threads with 10 commitments each
    When I submit conflicting mint commitments concurrently to all aggregators
    Then all mint commitments should receive inclusion proofs from all aggregators within 30 seconds
    And I should see performance metrics for each aggregator

  Scenario: Submit mixed valid and conflicting commitments to multiple aggregators concurrently
    Given the aggregator URLs are configured
    And trust-base.json is set
    And the aggregator clients are initialized
    And I configure 2 threads with 1 commitments each
    When I submit mixed valid and conflicting commitments concurrently to all aggregators
    Then all mint commitments should receive inclusion proofs from all aggregators within 30 seconds
    And I should see performance metrics for each aggregator
