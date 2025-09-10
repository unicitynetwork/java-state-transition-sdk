Feature: Aggregator Connectivity and Basic Operations
  As a developer using the Unicity SDK
  I want to verify connectivity with the aggregator
  So that I can ensure the system is operational

  Background:
    Given the aggregator URL is configured
    And the aggregator client is initialized

  Scenario: Verify aggregator connectivity
    When I request the current block height
    Then the block height should be returned
    And the block height should be greater than 0

  Scenario Outline: Submit commitment with performance validation
    Given a random secret of <secretLength> bytes
    And a state hash from <stateLength> bytes of random data
    And transaction data "<txData>"
    When I submit a commitment with the generated data
    Then the commitment should be submitted successfully
    And the submission should complete in less than <maxDuration> milliseconds

    Examples:
      | secretLength | stateLength | txData                      | maxDuration |
      | 32          | 32          | test commitment performance | 5000        |
      | 16          | 24          | simple test data           | 3000        |

  Scenario Outline: Multi-threaded commitment performance
    Given I configure <threadCount> threads with <commitmentsPerThread> commitments each
    When I submit all commitments concurrently
    Then all commitments should be submitted successfully
    And all submissions should complete within a reasonable time

    Examples:
      | threadCount | commitmentsPerThread |
      | 10          | 5                    |
      | 50          | 10                   |
      | 100         | 10                   |