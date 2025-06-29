# Unicity Java State Transition SDK

A Java SDK for interacting with the Unicity network, enabling state transitions and token operations with cross-platform support for JVM and Android 12+.

## Features

- **Token Operations**: Mint, transfer, and manage fungible tokens
- **State Transitions**: Submit and verify state transitions on the Unicity network
- **Cross-Platform**: Works on standard JVM and Android 12+ (API level 31+)
- **Type-Safe**: Strongly typed API with comprehensive error handling
- **CBOR Support**: Built-in CBOR encoding/decoding using Jackson
- **Async Operations**: All network operations return `CompletableFuture` for non-blocking execution

## Requirements

- Java 11 or higher
- Android 12+ (API level 31+) for Android platform
- Gradle 8.8 or higher

## Installation

### Gradle (JVM)

```groovy
dependencies {
    implementation 'com.unicity.sdk:unicity-sdk:1.0-SNAPSHOT'
}
```

### Gradle (Android)

```groovy
dependencies {
    implementation 'com.unicity.sdk:unicity-sdk-android:1.0-SNAPSHOT'
}
```

### Maven (JVM)

```xml
<dependency>
    <groupId>com.unicity.sdk</groupId>
    <artifactId>unicity-sdk</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Initialize the Client

```java
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;

// Connect to the Unicity test network
String aggregatorUrl = "https://gateway-test.unicity.network";
AggregatorClient aggregatorClient = new AggregatorClient(aggregatorUrl);
StateTransitionClient client = new StateTransitionClient(aggregatorClient);
```

### Mint a Token

```java
import com.unicity.sdk.token.*;
import com.unicity.sdk.token.fungible.*;
import com.unicity.sdk.transaction.*;
import com.unicity.sdk.predicate.*;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.hash.HashAlgorithm;

// Create signing service from secret
byte[] secret = "your-secret-key".getBytes();
byte[] nonce = new byte[32]; // Generate random nonce
SigningService signingService = SigningService.createFromSecret(secret, nonce).get();

// Create token parameters
TokenId tokenId = TokenId.create(randomBytes(32));
TokenType tokenType = TokenType.create(randomBytes(32));

// Create predicate for ownership
MaskedPredicate predicate = MaskedPredicate.create(
    tokenId,
    tokenType,
    signingService,
    HashAlgorithm.SHA256,
    nonce
).get();

// Create token data
TestTokenData tokenData = new TestTokenData(randomBytes(32));

// Create coins
Map<CoinId, BigInteger> coins = new HashMap<>();
coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(100));
coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(50));
TokenCoinData coinData = new TokenCoinData(coins);

// Create mint transaction
MintTransactionData<TestTokenData> mintData = new MintTransactionData<>(
    tokenId,
    tokenType,
    predicate,
    tokenData,
    coinData,
    null,  // dataHash (optional)
    randomBytes(32)  // salt
);

// Submit transaction
Commitment<MintTransactionData<TestTokenData>> commitment = 
    client.submitMintTransaction(mintData).get();

// Wait for inclusion proof
InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
    client, 
    commitment,
    Duration.ofSeconds(30),
    Duration.ofSeconds(1)
).get();

// Create transaction with proof
Transaction<MintTransactionData<TestTokenData>> mintTransaction =
    client.createTransaction(commitment, inclusionProof).get();

// Create token
TokenState tokenState = TokenState.create(predicate, tokenData.getData());
Token<Transaction<MintTransactionData<?>>> token = 
    new Token<>(tokenState, (Transaction) mintTransaction);

System.out.println("Token minted with ID: " + token.getId().toJSON());
```

### Get Block Height

```java
Long blockHeight = client.getAggregatorClient().getBlockHeight().get();
System.out.println("Current block height: " + blockHeight);
```

### Transfer a Token

```java
// Create transfer transaction data
TransferTransactionData<TestTokenData> transferData = new TransferTransactionData<>(
    token,
    recipientPredicate,
    newTokenData,
    salt
);

// Submit transfer
Commitment<TransferTransactionData<TestTokenData>> transferCommitment =
    client.submitTransferTransaction(transferData, signingService).get();

// Wait for inclusion proof and create transaction
InclusionProof transferProof = InclusionProofUtils.waitInclusionProof(
    client, transferCommitment
).get();

Transaction<TransferTransactionData<TestTokenData>> transferTransaction =
    client.createTransaction(transferCommitment, transferProof).get();
```

## Building from Source

### Clone the Repository

```bash
git clone https://github.com/unicity/java-state-transition-sdk.git
cd java-state-transition-sdk
```

### Build the Project

```bash
./gradlew build
```

### Run Tests

```bash
# Run unit tests
./gradlew test

# Run integration tests (requires Docker)
./gradlew integrationTest

# Run E2E tests against deployed aggregator
AGGREGATOR_URL=https://gateway-test.unicity.network ./gradlew integrationTest --tests "*E2ETest"
```

## Platform-Specific Considerations

### Android Compatibility

The SDK is compatible with Android 12+ (API level 31+). It uses:
- OkHttp for HTTP operations instead of Java 11's HttpClient
- Android-compatible Guava version
- Animal Sniffer plugin to ensure API compatibility

### JVM Compatibility

The standard JVM version uses:
- Java 11 APIs
- Full Guava JRE version
- All Java 11 features

## Architecture

The SDK follows a modular architecture:

- **`api`**: Core API interfaces and aggregator client
- **`token`**: Token-related classes (TokenId, TokenType, TokenState)
- **`transaction`**: Transaction types (Mint, Transfer, Swap)
- **`predicate`**: Ownership predicates and authorization
- **`shared`**: Common utilities (CBOR, hashing, signing)
- **`utils`**: Helper utilities

## Error Handling

All async operations return `CompletableFuture` which can be handled with standard Java patterns:

```java
client.submitMintTransaction(mintData)
    .thenApply(commitment -> {
        System.out.println("Transaction submitted: " + commitment.getRequestId());
        return commitment;
    })
    .exceptionally(error -> {
        System.err.println("Transaction failed: " + error.getMessage());
        return null;
    });
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java naming conventions
- Add unit tests for new features
- Update documentation as needed
- Ensure compatibility with both JVM and Android platforms
- Run `./gradlew build` before submitting PR

## Testing

The SDK includes comprehensive test suites:

### Unit Tests
Located in `src/test/java`, these test individual components in isolation.

### Integration Tests
Located in `src/test/java/com/unicity/sdk/integration`:
- `TokenIntegrationTest`: Tests against Docker-based local aggregator
- `TokenE2ETest`: Tests against deployed aggregator (requires `AGGREGATOR_URL` env var)

### Running Tests

```bash
# All tests
./gradlew test

# Integration tests only
./gradlew integrationTest

# Specific test class
./gradlew test --tests "com.unicity.sdk.api.RequestIdTest"
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and feature requests, please use the [GitHub issue tracker](https://github.com/unicitynetwork/java-state-transition-sdk/issues).

For questions about the Unicity Labs, visit [unicity-labs.com](https://unicity-labs.com).

## Acknowledgments

- Built on the Unicity network protocol
- Uses Jackson for CBOR encoding
- Uses Bouncy Castle for cryptographic operations
- Uses OkHttp for Android-compatible HTTP operations